package com.artknower.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BulkOrderMessageDto(
    @SerialName("id") val id: String? = null,
    @SerialName("bulk_order_request_id") val bulkOrderRequestId: String? = null,
    @SerialName("sender_user_id") val senderUserId: String? = null,
    @SerialName("message") val message: String = "",
    @SerialName("created_at") val createdAt: String? = null
) {
    val displayMessageText: String
        get() = message
}
