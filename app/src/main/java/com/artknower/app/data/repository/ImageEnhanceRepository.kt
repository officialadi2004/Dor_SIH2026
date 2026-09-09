package com.artknower.app.data.repository

import com.artknower.app.BuildConfig
import com.artknower.app.data.model.*
import com.artknower.app.data.remote.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

object ImageEnhanceRepository {
    private const val API_BASE_URL = "https://artisans-2-0.onrender.com"
    private val client = SupabaseClientProvider.adminClient
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun getStorageUrl(path: String?): String {
        if (path.isNullOrBlank()) return ""
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        val cleanPath = path.trimStart('/')
        return "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/product-media/$cleanPath"
    }

    /**
     * Step 1: Upload original image to Supabase Storage.
     * Path: {userUuid}/products/{productUuid}/normal_image/{originalFilename}
     */
    suspend fun uploadOriginalImage(
        userId: String,
        productId: String,
        imageBytes: ByteArray,
        filename: String = "artisan-1.jpg"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val storagePath = "$userId/products/$productId/normal_image/$filename"
            val bucket = client.storage.from("product-media")
            bucket.upload(storagePath, imageBytes, upsert = true)
            Result.success(storagePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Step 2: Call AI enhancement API with selected image.
     * POST https://artisans-2-0.onrender.com/api/products/{productId}/enhance
     * Multipart field: image
     */
    suspend fun callEnhancementApi(
        productId: String,
        imageBytes: ByteArray,
        filename: String = "artisan-1.jpg"
    ): Result<EnhanceResponseDto> = withContext(Dispatchers.IO) {
        try {
            val boundary = "Boundary-${System.currentTimeMillis()}"
            val endpointUrl = URL("$API_BASE_URL/api/products/$productId/enhance")
            val conn = endpointUrl.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.connectTimeout = 60000
            conn.readTimeout = 60000
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

            val contentType = when {
                filename.endsWith(".png", ignoreCase = true) -> "image/png"
                filename.endsWith(".webp", ignoreCase = true) -> "image/webp"
                else -> "image/jpeg"
            }

            val output = DataOutputStream(conn.outputStream)
            output.writeBytes("--$boundary\r\n")
            output.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"$filename\"\r\n")
            output.writeBytes("Content-Type: $contentType\r\n\r\n")
            output.write(imageBytes)
            output.writeBytes("\r\n--$boundary--\r\n")
            output.flush()
            output.close()

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val responseBody = conn.inputStream.bufferedReader().use { it.readText() }
                val response = json.decodeFromString<EnhanceResponseDto>(responseBody)
                Result.success(response)
            } else {
                val errorBody = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                val errorMessage = when (responseCode) {
                    400 -> "Invalid image format. Please select a valid JPG, PNG, or WEBP image."
                    401, 403 -> "AI enhancement provider authentication failure."
                    429 -> "AI enhancement rate limit reached. Please try again shortly."
                    500 -> "AI enhancement provider failure. Please try again."
                    503 -> "AI enhancement service is temporarily unavailable."
                    504 -> "AI enhancement request timed out. Please try again."
                    else -> "AI Enhancement failed (HTTP $responseCode): $errorBody"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Step 3: Download enhanced variants from external URLs and upload them into Supabase Storage.
     * Path: {userUuid}/products/{productUuid}/enhanced/{returnedFilename}
     */
    suspend fun downloadAndUploadEnhancedVariants(
        userId: String,
        productId: String,
        variants: List<ImageVariantDto>
    ): Result<List<EnhancedImageItem>> = withContext(Dispatchers.IO) {
        try {
            val bucket = client.storage.from("product-media")
            val uploadedItems = mutableListOf<EnhancedImageItem>()

            for (variant in variants) {
                if (variant.url.isBlank()) continue
                val filename = if (variant.filename.isNotBlank()) variant.filename else "enhanced.png"
                val storagePath = "$userId/products/$productId/enhanced/$filename"
                var displayUrl = variant.url

                try {
                    val dlUrl = URL(variant.url)
                    val conn = dlUrl.openConnection() as HttpURLConnection
                    conn.connectTimeout = 30000
                    conn.readTimeout = 30000
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0")

                    val imageBytes = conn.inputStream.use { it.readBytes() }
                    bucket.upload(storagePath, imageBytes, upsert = true)
                    val publicStorageUrl = getStorageUrl(storagePath)
                    if (publicStorageUrl.isNotBlank()) {
                        displayUrl = publicStorageUrl
                    }
                } catch (e: Exception) {
                    // Fall back to direct backend URL if Supabase storage upload encounters issues
                    displayUrl = variant.url
                }

                uploadedItems.add(
                    EnhancedImageItem(
                        id = variant.id,
                        filename = filename,
                        storagePath = storagePath,
                        displayUrl = displayUrl,
                        isKept = true
                    )
                )
            }

            Result.success(uploadedItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Step 4: Save final product with inventory and kept image records.
     */
    suspend fun saveProductWithImages(
        product: ProductDto,
        initialStock: Int = 1,
        lowStockThreshold: Int = 5,
        originalStoragePath: String,
        keptEnhancedPaths: List<String>
    ): Result<ProductDto> = withContext(Dispatchers.IO) {
        try {
            // 1. Insert or update product
            val insertedProduct = client.from("products")
                .insert(product) {
                    select()
                }
                .decodeSingle<ProductDto>()

            val prodId = insertedProduct.id ?: product.id ?: ""

            // 2. Insert initial inventory
            if (prodId.isNotBlank()) {
                val inventory = InventoryDto(
                    productId = prodId,
                    quantityOnHand = initialStock,
                    reservedQuantity = 0,
                    lowStockThreshold = lowStockThreshold
                )
                try {
                    client.from("inventory").upsert(inventory)
                } catch (_: Exception) {}
            }

            // 3. Insert original image row
            val originalImageDto = ProductImageDto(
                productId = prodId,
                originalPath = originalStoragePath,
                enhancedPath = null,
                thumbnailPath = null,
                altText = "Original Product Image",
                isPrimary = keptEnhancedPaths.isEmpty(),
                sortOrder = 0,
                processingStatus = "completed"
            )
            client.from("product_images").insert(originalImageDto)

            // 4. Insert only kept enhanced images
            for ((index, path) in keptEnhancedPaths.withIndex()) {
                val enhancedDto = ProductImageDto(
                    productId = prodId,
                    originalPath = originalStoragePath,
                    enhancedPath = path,
                    thumbnailPath = null,
                    altText = "AI Enhanced Variant ${index + 1}",
                    isPrimary = (index == 0),
                    sortOrder = index + 1,
                    processingStatus = "completed"
                )
                client.from("product_images").insert(enhancedDto)
            }

            Result.success(insertedProduct)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
