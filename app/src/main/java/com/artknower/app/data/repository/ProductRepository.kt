package com.artknower.app.data.repository

import com.artknower.app.data.model.InventoryDto
import com.artknower.app.data.model.ProductCategoryDto
import com.artknower.app.data.model.ProductDto
import com.artknower.app.data.model.ProductImageDto
import com.artknower.app.data.remote.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class ProductStats(
    val totalProducts: Int = 0,
    val draftCount: Int = 0,
    val publishedCount: Int = 0
)

object ProductRepository {
    private val client = SupabaseClientProvider.adminClient

    suspend fun getCategories(): Result<List<ProductCategoryDto>> = withContext(Dispatchers.IO) {
        try {
            val categories = client.from("product_categories")
                .select {
                    filter {
                        eq("is_active", true)
                    }
                    order("sort_order", order = Order.ASCENDING)
                }
                .decodeAs<List<ProductCategoryDto>>()
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductStats(artisanProfileId: String): Result<ProductStats> = withContext(Dispatchers.IO) {
        try {
            val products = client.from("products")
                .select {
                    filter {
                        eq("artisan_profile_id", artisanProfileId)
                    }
                }
                .decodeAs<List<ProductDto>>()

            val drafts = products.count { it.status.lowercase() == "draft" }
            val published = products.count { it.status.lowercase() == "published" }

            Result.success(
                ProductStats(
                    totalProducts = products.size,
                    draftCount = drafts,
                    publishedCount = published
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProducts(artisanProfileId: String): Result<List<ProductDto>> = withContext(Dispatchers.IO) {
        try {
            val products = client.from("products")
                .select {
                    filter {
                        eq("artisan_profile_id", artisanProfileId)
                    }
                }
                .decodeAs<List<ProductDto>>()
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createProduct(
        product: ProductDto,
        initialStock: Int = 1,
        lowStockThreshold: Int = 5
    ): Result<ProductDto> = withContext(Dispatchers.IO) {
        try {
            val insertedProduct = client.from("products")
                .insert(product) {
                    select()
                }
                .decodeSingle<ProductDto>()

            val prodId = insertedProduct.id
            if (!prodId.isNullOrBlank()) {
                val inventory = InventoryDto(
                    productId = prodId,
                    quantityOnHand = initialStock,
                    reservedQuantity = 0,
                    lowStockThreshold = lowStockThreshold
                )
                try {
                    client.from("inventory").upsert(inventory)
                } catch (_: Exception) {
                    // Non-fatal if inventory trigger exists or duplicate
                }
            }

            Result.success(insertedProduct)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveDraftProduct(product: ProductDto): Result<ProductDto> = withContext(Dispatchers.IO) {
        try {
            val savedProduct = client.from("products")
                .upsert(product) {
                    select()
                }
                .decodeSingle<ProductDto>()
            Result.success(savedProduct)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProductImage(
        userId: String,
        productId: String,
        imageBytes: ByteArray,
        filename: String = "original.jpg",
        isPrimary: Boolean = true
    ): Result<ProductImageDto> = withContext(Dispatchers.IO) {
        try {
            val storagePath = "$userId/products/$productId/original/$filename"
            
            // Upload binary to 'product-media' bucket in Supabase Storage
            val bucket = client.storage.from("product-media")
            bucket.upload(storagePath, imageBytes, upsert = true)

            // Create row in public.product_images
            val dto = ProductImageDto(
                productId = productId,
                originalPath = storagePath,
                enhancedPath = null,
                thumbnailPath = null,
                altText = "Product Image",
                isPrimary = isPrimary,
                sortOrder = 0,
                processingStatus = "completed"
            )

            val insertedImage = client.from("product_images")
                .insert(dto) {
                    select()
                }
                .decodeSingle<ProductImageDto>()

            Result.success(insertedImage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductImages(productId: String): Result<List<ProductImageDto>> = withContext(Dispatchers.IO) {
        try {
            val images = client.from("product_images")
                .select {
                    filter {
                        eq("product_id", productId)
                    }
                }
                .decodeAs<List<ProductImageDto>>()

            Result.success(images)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun publishProduct(
        productId: String,
        artisanProfileId: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch product & images for validation
            val productList = client.from("products")
                .select {
                    filter {
                        eq("id", productId)
                        eq("artisan_profile_id", artisanProfileId)
                    }
                }
                .decodeAs<List<ProductDto>>()

            val product = productList.firstOrNull()
                ?: return@withContext Result.failure(IllegalStateException("Product not found or unauthorized."))

            if (product.name.isNullOrBlank() || product.description.isNullOrBlank() || product.price == null) {
                return@withContext Result.failure(IllegalStateException("Product name, description, and price are required."))
            }

            val imageList = getProductImages(productId).getOrDefault(emptyList())
            if (imageList.isEmpty()) {
                return@withContext Result.failure(IllegalStateException("At least one product image must be uploaded before publishing."))
            }

            // 2. Set status = 'published' and published_at = now()
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val nowIso = sdf.format(Date())

            client.from("products").update({
                set("status", "published")
                set("published_at", nowIso)
            }) {
                filter {
                    eq("id", productId)
                }
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
