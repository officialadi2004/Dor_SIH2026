package com.artknower.app.data.repository

import com.artknower.app.data.model.DbPricingRequest
import com.artknower.app.data.model.PricingRecommendResponse
import com.artknower.app.data.model.ProductPricingRequest
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object PricingRepository {
    private const val PRICING_RECOMMEND_ENDPOINT = "https://artisan-pricing-ml.onrender.com/pricing/recommend"
    private const val PRICING_FROM_DB_ENDPOINT = "https://artisan-pricing-ml.onrender.com/pricing/recommend-from-db"
    
    private val httpClient = HttpClient(Android)
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Calls the deployed Render Pricing ML API:
     * POST https://artisan-pricing-ml.onrender.com/pricing/recommend
     *
     * Request Fields:
     * - category
     * - craft_type
     * - material
     * - dimensions
     * - state_or_location
     * - raw_material_cost
     * - labor_cost
     * - input_cost
     */
    suspend fun recommendPrice(request: ProductPricingRequest): Result<PricingRecommendResponse> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = json.encodeToString(ProductPricingRequest.serializer(), request)

            android.util.Log.d(
                "PricingRepository",
                "CALLING PRICING API: URL = $PRICING_RECOMMEND_ENDPOINT, body = $jsonBody"
            )

            val response = httpClient.post(PRICING_RECOMMEND_ENDPOINT) {
                contentType(ContentType.Application.Json)
                setBody(jsonBody)
            }

            val responseText = response.bodyAsText()

            android.util.Log.d(
                "PricingRepository",
                "PRICING API RESPONSE: status = ${response.status.value}, body = $responseText"
            )

            if (response.status.value !in 200..299) {
                return@withContext Result.failure(
                    Exception("Pricing API returned HTTP ${response.status.value}: $responseText")
                )
            }

            val result = json.decodeFromString<PricingRecommendResponse>(responseText)
            Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("PricingRepository", "Error calling pricing API: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Calls the DB-based pricing recommendation endpoint:
     * POST https://artisan-pricing-ml.onrender.com/pricing/recommend-from-db
     */
    suspend fun recommendPriceFromDb(
        productId: String,
        rawMaterialCost: Double = 0.0,
        laborCost: Double = 0.0,
        inputCost: Double = 0.0
    ): Result<PricingRecommendResponse> = withContext(Dispatchers.IO) {
        try {
            val requestPayload = DbPricingRequest(
                productId = productId,
                rawMaterialCost = rawMaterialCost,
                laborCost = laborCost,
                inputCost = inputCost
            )
            val jsonBody = json.encodeToString(DbPricingRequest.serializer(), requestPayload)

            android.util.Log.d(
                "PricingRepository",
                "CALLING PRICING DB API: URL = $PRICING_FROM_DB_ENDPOINT, body = $jsonBody"
            )

            val response = httpClient.post(PRICING_FROM_DB_ENDPOINT) {
                contentType(ContentType.Application.Json)
                setBody(jsonBody)
            }

            val responseText = response.bodyAsText()

            android.util.Log.d(
                "PricingRepository",
                "PRICING DB API RESPONSE: status = ${response.status.value}, body = $responseText"
            )

            if (response.status.value !in 200..299) {
                return@withContext Result.failure(
                    Exception("Pricing API returned HTTP ${response.status.value}: $responseText")
                )
            }

            val result = json.decodeFromString<PricingRecommendResponse>(responseText)
            Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("PricingRepository", "Error calling pricing DB API: ${e.message}", e)
            Result.failure(e)
        }
    }
}
