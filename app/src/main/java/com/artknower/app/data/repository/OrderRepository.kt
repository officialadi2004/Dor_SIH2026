package com.artknower.app.data.repository

import com.artknower.app.data.model.BulkOrderMessageDto
import com.artknower.app.data.model.BulkOrderRequestDto
import com.artknower.app.data.model.InventoryDto
import com.artknower.app.data.model.InventoryMovementDto
import com.artknower.app.data.model.OrderDto
import com.artknower.app.data.remote.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object OrderRepository {
    private val client = SupabaseClientProvider.adminClient

    suspend fun getBulkOrderRequests(artisanProfileId: String): Result<List<BulkOrderRequestDto>> = withContext(Dispatchers.IO) {
        try {
            val requests = try {
                client.from("bulk_order_requests")
                    .select(columns = Columns.raw("*, business_profiles(*), products(*)")) {
                        filter {
                            eq("artisan_profile_id", artisanProfileId)
                        }
                    }
                    .decodeAs<List<BulkOrderRequestDto>>()
            } catch (e: Exception) {
                // Fallback: standard query without joins if foreign key names differ
                client.from("bulk_order_requests")
                    .select {
                        filter {
                            eq("artisan_profile_id", artisanProfileId)
                        }
                    }
                    .decodeAs<List<BulkOrderRequestDto>>()
            }

            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBulkOrderStatus(
        requestId: String,
        status: String,
        finalQuantity: Int? = null,
        finalUnitPrice: Double? = null,
        artisanNotes: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            client.from("bulk_order_requests").update({
                set("status", status.lowercase())
                if (finalQuantity != null) {
                    set("final_quantity", finalQuantity)
                }
                if (finalUnitPrice != null) {
                    set("final_unit_price", finalUnitPrice)
                }
                if (!artisanNotes.isNullOrBlank()) {
                    set("artisan_notes", artisanNotes)
                }
            }) {
                filter {
                    eq("id", requestId)
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBulkOrderMessages(requestId: String): Result<List<BulkOrderMessageDto>> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("B2B_CHAT_DEBUG", "FETCH START: bulk_order_request_id = '$requestId'")
            val messages = client.from("bulk_order_messages")
                .select {
                    filter {
                        eq("bulk_order_request_id", requestId)
                    }
                    order("created_at", order = Order.ASCENDING)
                }
                .decodeAs<List<BulkOrderMessageDto>>()

            android.util.Log.d("B2B_CHAT_DEBUG", "FETCH SUCCESS: found ${messages.size} messages for request ID '$requestId'")
            for (m in messages) {
                android.util.Log.d("B2B_CHAT_DEBUG", "  [MSG] id=${m.id}, sender=${m.senderUserId}, msg='${m.message}', at=${m.createdAt}")
            }
            Result.success(messages)
        } catch (e: Exception) {
            android.util.Log.e("B2B_CHAT_DEBUG", "FETCH FAILED for request ID '$requestId': ${e.message} | class: ${e.javaClass.name}", e)
            Result.failure(e)
        }
    }

    suspend fun sendBulkOrderMessage(
        requestId: String,
        senderUserId: String,
        messageText: String
    ): Result<BulkOrderMessageDto> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("B2B_CHAT_DEBUG", "INSERT START: bulk_order_request_id = '$requestId', sender_user_id = '$senderUserId', message = '${messageText.trim()}'")
            val payload = buildJsonObject {
                put("bulk_order_request_id", requestId)
                put("sender_user_id", senderUserId)
                put("message", messageText.trim())
            }

            val inserted = client.from("bulk_order_messages")
                .insert(payload) {
                    select()
                }
                .decodeSingle<BulkOrderMessageDto>()

            android.util.Log.d("B2B_CHAT_DEBUG", "INSERT SUCCESS: message id = ${inserted.id}")
            Result.success(inserted)
        } catch (e: Exception) {
            android.util.Log.e("B2B_CHAT_DEBUG", "INSERT FAILED for request ID '$requestId': ${e.message} | class: ${e.javaClass.name}", e)
            Result.failure(e)
        }
    }

    suspend fun getConfirmedOrders(artisanProfileId: String): Result<List<OrderDto>> = withContext(Dispatchers.IO) {
        try {
            val orders = try {
                client.from("orders")
                    .select(columns = Columns.raw("*, bulk_order_requests(*, products(*)), business_profiles(*)")) {
                        filter {
                            eq("artisan_profile_id", artisanProfileId)
                        }
                    }
                    .decodeAs<List<OrderDto>>()
            } catch (e: Exception) {
                client.from("orders")
                    .select {
                        filter {
                            eq("artisan_profile_id", artisanProfileId)
                        }
                    }
                    .decodeAs<List<OrderDto>>()
            }

            Result.success(orders.sortedByDescending { it.confirmedAt })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Confirms a bulk order request and safely creates the corresponding order,
     * reserving inventory and logging an inventory movement idempotently.
     */
    suspend fun confirmBulkOrder(requestId: String, userId: String): Result<OrderDto> = withContext(Dispatchers.IO) {
        try {
            // 1. Idempotency check: if an order already exists for this bulk_order_request_id, return it
            val existingOrders = client.from("orders").select {
                filter {
                    eq("bulk_order_request_id", requestId)
                }
            }.decodeAs<List<OrderDto>>()

            if (existingOrders.isNotEmpty()) {
                return@withContext Result.success(existingOrders.first())
            }

            // 2. Fetch the bulk_order_request
            val requests = client.from("bulk_order_requests").select {
                filter {
                    eq("id", requestId)
                }
            }.decodeAs<List<BulkOrderRequestDto>>()

            val request = requests.firstOrNull()
                ?: return@withContext Result.failure(IllegalStateException("Bulk order request not found."))

            // 3. Verify eligibility
            if (request.status.equals("rejected", ignoreCase = true) ||
                request.status.equals("cancelled", ignoreCase = true)) {
                return@withContext Result.failure(IllegalStateException("Request status '${request.status}' is not eligible for confirmation."))
            }

            if (request.businessProfileId.isBlank()) {
                return@withContext Result.failure(IllegalStateException("Business profile is missing."))
            }
            if (request.artisanProfileId.isBlank()) {
                return@withContext Result.failure(IllegalStateException("Artisan profile is missing."))
            }
            if (request.productId.isBlank()) {
                return@withContext Result.failure(IllegalStateException("Product is missing."))
            }

            val finalQty = request.finalQuantity
                ?: return@withContext Result.failure(IllegalStateException("Final quantity has not been set/agreed."))

            if (finalQty <= 0) {
                return@withContext Result.failure(IllegalStateException("Final quantity must be greater than 0."))
            }

            val finalPrice = request.finalUnitPrice
                ?: return@withContext Result.failure(IllegalStateException("Final unit price has not been set/agreed."))

            // 4. Verify inventory availability
            val inventoryList = client.from("inventory").select {
                filter {
                    eq("product_id", request.productId)
                }
            }.decodeAs<List<InventoryDto>>()

            val inventory = inventoryList.firstOrNull()
                ?: return@withContext Result.failure(IllegalStateException("Inventory record not found for this product."))

            val availableQuantity = inventory.quantityOnHand - inventory.reservedQuantity
            if (availableQuantity < finalQty) {
                return@withContext Result.failure(IllegalStateException("Insufficient available inventory for this order."))
            }

            // 5. Update bulk_order_requests status = confirmed (required by database trigger before order creation)
            val previousStatus = request.status
            client.from("bulk_order_requests").update({
                set("status", "confirmed")
            }) {
                filter {
                    eq("id", requestId)
                }
            }

            // 6. Create Order row (orders table has: bulk_order_request_id, business_profile_id, artisan_profile_id, status, confirmed_at)
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val nowIso = sdf.format(Date())

            val createdOrder = try {
                val insertedOrders = client.from("orders").insert(listOf(
                    mapOf(
                        "bulk_order_request_id" to request.id,
                        "business_profile_id" to request.businessProfileId,
                        "artisan_profile_id" to request.artisanProfileId,
                        "status" to "confirmed",
                        "confirmed_at" to nowIso
                    )
                )) {
                    select()
                }.decodeAs<List<OrderDto>>()

                insertedOrders.firstOrNull() ?: throw IllegalStateException("Failed to create confirmed order.")
            } catch (e: Exception) {
                // Rollback request status if order creation failed
                try {
                    client.from("bulk_order_requests").update({
                        set("status", previousStatus)
                    }) {
                        filter {
                            eq("id", requestId)
                        }
                    }
                } catch (_: Exception) {}
                throw e
            }

            // 7. Reserve inventory: increase reserved_quantity by final_quantity (do NOT change quantity_on_hand)
            val newReservedQuantity = inventory.reservedQuantity + finalQty
            client.from("inventory").update({
                set("reserved_quantity", newReservedQuantity)
            }) {
                filter {
                    eq("product_id", request.productId)
                }
            }

            // 8. Record inventory movement (movement_type = order_reservation, reference_type = order, reference_id = order.id)
            client.from("inventory_movements").insert(listOf(
                mapOf(
                    "product_id" to request.productId,
                    "movement_type" to "order_reservation",
                    "quantity" to finalQty,
                    "reference_type" to "order",
                    "reference_id" to createdOrder.id,
                    "created_by" to userId
                )
            ))

            Result.success(createdOrder)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
