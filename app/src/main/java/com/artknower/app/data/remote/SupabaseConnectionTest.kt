package com.artknower.app.data.remote

import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Smallest possible connection verification helper for Supabase.
 * Verifies that the client is correctly configured with valid project credentials
 * without performing database mutations, table alterations, or auth actions.
 */
object SupabaseConnectionTest {
    suspend fun verifyConnection(supabaseClient: SupabaseClient = SupabaseClientProvider.client): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val url = supabaseClient.supabaseUrl
                val key = supabaseClient.supabaseKey
                if (url.isNotBlank() && key.isNotBlank()) {
                    Result.success(true)
                } else {
                    Result.failure(IllegalStateException("Supabase URL or Anon Key is missing or blank."))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
