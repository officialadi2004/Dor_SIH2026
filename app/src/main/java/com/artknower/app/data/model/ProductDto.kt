package com.artknower.app.data.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ProductDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("artisan_profile_id")
    val artisanProfileId: String,
    @SerialName("category_id")
    val categoryId: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("material")
    val material: String? = null,
    @SerialName("dimensions")
    val dimensions: String? = null,
    @SerialName("primary_colour")
    val primaryColour: String? = null,
    @SerialName("secondary_colour")
    val secondaryColour: String? = null,
    @SerialName("weight")
    val weight: String? = null,
    @SerialName("craft_type")
    val craftType: String? = null,
    @EncodeDefault
    @SerialName("tags")
    val tags: List<String> = emptyList(),
    @SerialName("additional_notes")
    val additionalNotes: String? = null,
    @SerialName("price")
    val price: Double? = null,
    @EncodeDefault
    @SerialName("currency")
    val currency: String = "INR",
    @EncodeDefault
    @SerialName("status")
    val status: String = "draft",
    @SerialName("published_at")
    val publishedAt: String? = null
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ProductImageDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("product_id")
    val productId: String,
    @SerialName("original_path")
    val originalPath: String,
    @SerialName("enhanced_path")
    val enhancedPath: String? = null,
    @SerialName("thumbnail_path")
    val thumbnailPath: String? = null,
    @SerialName("alt_text")
    val altText: String? = null,
    @EncodeDefault
    @SerialName("is_primary")
    val isPrimary: Boolean = true,
    @EncodeDefault
    @SerialName("sort_order")
    val sortOrder: Int = 0,
    @EncodeDefault
    @SerialName("processing_status")
    val processingStatus: String = "pending"
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class InventoryDto(
    @SerialName("product_id")
    val productId: String,
    @EncodeDefault
    @SerialName("quantity_on_hand")
    val quantityOnHand: Int = 1,
    @EncodeDefault
    @SerialName("reserved_quantity")
    val reservedQuantity: Int = 0,
    @EncodeDefault
    @SerialName("low_stock_threshold")
    val lowStockThreshold: Int = 5
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ProductCategoryDto(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("slug")
    val slug: String,
    @EncodeDefault
    @SerialName("is_active")
    val isActive: Boolean = true,
    @EncodeDefault
    @SerialName("sort_order")
    val sortOrder: Int = 0
)
