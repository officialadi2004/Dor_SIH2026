package com.artknower.app.data.repository

import com.artknower.app.data.model.CustomerEnquiryDto
import com.artknower.app.data.remote.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object EnquiryRepository {
    private val client = SupabaseClientProvider.client

    suspend fun getCustomerEnquiries(artisanProfileId: String): Result<List<CustomerEnquiryDto>> = withContext(Dispatchers.IO) {
        try {
            val list = try {
                client.from("customer_enquiries")
                    .select(columns = Columns.raw("*, profiles(*), products(*)")) {
                        filter {
                            eq("artisan_profile_id", artisanProfileId)
                        }
                    }
                    .decodeAs<List<CustomerEnquiryDto>>()
            } catch (e: Exception) {
                // Fallback: standard query without joined profiles if foreign keys differ
                client.from("customer_enquiries")
                    .select {
                        filter {
                            eq("artisan_profile_id", artisanProfileId)
                        }
                    }
                    .decodeAs<List<CustomerEnquiryDto>>()
            }

            Result.success(list.sortedByDescending { it.createdAt })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun respondToEnquiry(enquiryId: String, responseText: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val nowIso = sdf.format(Date())

            client.from("customer_enquiries").update({
                set("artisan_response", responseText)
                set("status", "responded")
                set("updated_at", nowIso)
            }) {
                filter {
                    eq("id", enquiryId)
                }
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun closeEnquiry(enquiryId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val nowIso = sdf.format(Date())

            client.from("customer_enquiries").update({
                set("status", "closed")
                set("updated_at", nowIso)
            }) {
                filter {
                    eq("id", enquiryId)
                }
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
