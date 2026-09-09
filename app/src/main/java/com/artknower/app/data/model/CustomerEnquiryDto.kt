package com.artknower.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomerEnquiryDto(
    @SerialName("id") val id: String,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("artisan_profile_id") val artisanProfileId: String,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("subject") val subject: String? = null,
    @SerialName("message") val message: String = "",
    @SerialName("artisan_response") val artisanResponse: String? = null,
    @SerialName("status") val status: String = "open",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,

    // Joined relations
    @SerialName("products") val product: ProductDto? = null
)
