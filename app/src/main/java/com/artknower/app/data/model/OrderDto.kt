package com.artknower.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BulkOrderRequestDto(
    @SerialName("id") val id: String,
    @SerialName("business_profile_id") val businessProfileId: String,
    @SerialName("artisan_profile_id") val artisanProfileId: String,
    @SerialName("product_id") val productId: String,
    @SerialName("requested_quantity") val requestedQuantity: Int,
    @SerialName("requested_unit_price") val requestedUnitPrice: Double? = null,
    @SerialName("required_by_date") val requiredByDate: String? = null,
    @SerialName("buyer_notes") val buyerNotes: String? = null,
    @SerialName("artisan_notes") val artisanNotes: String? = null,
    @SerialName("final_quantity") val finalQuantity: Int? = null,
    @SerialName("final_unit_price") val finalUnitPrice: Double? = null,
    @SerialName("final_requirements") val finalRequirements: String? = null,
    @SerialName("status") val status: String,
    @SerialName("requested_at") val requestedAt: String,
    @SerialName("updated_at") val updatedAt: String,
    
    // Joined nested relations
    @SerialName("business_profiles") val businessProfile: BusinessProfileDto? = null,
    @SerialName("products") val product: ProductDto? = null
)

@Serializable
data class OrderDto(
    @SerialName("id") val id: String,
    @SerialName("bulk_order_request_id") val bulkOrderRequestId: String,
    @SerialName("business_profile_id") val businessProfileId: String,
    @SerialName("artisan_profile_id") val artisanProfileId: String,
    @SerialName("status") val status: String,
    @SerialName("confirmed_at") val confirmedAt: String,
    @SerialName("updated_at") val updatedAt: String,

    // Joined relations
    @SerialName("bulk_order_requests") val bulkOrderRequest: BulkOrderRequestDto? = null,
    @SerialName("business_profiles") val businessProfile: BusinessProfileDto? = null,
    @SerialName("products") val product: ProductDto? = null
)

@Serializable
data class InventoryMovementDto(
    @SerialName("id") val id: String? = null,
    @SerialName("product_id") val productId: String,
    @SerialName("movement_type") val movementType: String,
    @SerialName("quantity") val quantity: Int,
    @SerialName("reference_type") val referenceType: String? = null,
    @SerialName("reference_id") val referenceId: String? = null,
    @SerialName("note") val note: String? = null,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
