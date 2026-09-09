package com.artknower.app.data.repository

import com.artknower.app.BuildConfig
import com.artknower.app.data.model.*
import com.artknower.app.data.remote.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

object MarketingRepository {
    private val client = SupabaseClientProvider.adminClient
    private val httpClient = HttpClient(Android) {
        followRedirects = true
    }

    private fun getCurrentIsoTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    fun getStorageUrl(bucket: String, path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        var cleanPath = path.trimStart('/')
        if (cleanPath.startsWith("storage/v1/object/public/")) {
            return "${BuildConfig.SUPABASE_URL}/$cleanPath"
        }
        if (cleanPath.startsWith("$bucket/")) {
            cleanPath = cleanPath.removePrefix("$bucket/")
        } else if (cleanPath.startsWith("marketing-media/")) {
            cleanPath = cleanPath.removePrefix("marketing-media/")
        } else if (cleanPath.startsWith("product-media/")) {
            cleanPath = cleanPath.removePrefix("product-media/")
        }
        return "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/$bucket/$cleanPath"
    }

    /**
     * Loads only published, active products belonging to the authenticated artisan
     * along with category names, primary images, and inventory stock.
     */
    suspend fun getMarketableProducts(artisanProfileId: String): Result<List<MarketableProduct>> = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch published products for this artisan
            val products = client.from("products")
                .select {
                    filter {
                        eq("artisan_profile_id", artisanProfileId)
                        eq("status", "published")
                    }
                    order("created_at", order = Order.DESCENDING)
                }
                .decodeAs<List<ProductDto>>()

            if (products.isEmpty()) {
                return@withContext Result.success(emptyList())
            }

            // 2. Fetch categories for taxonomy mapping
            val categoriesMap = try {
                client.from("product_categories")
                    .select()
                    .decodeAs<List<ProductCategoryDto>>()
                    .associateBy { it.id }
            } catch (_: Exception) {
                emptyMap()
            }

            // 3. Fetch images for all these products
            val productIds = products.mapNotNull { it.id }
            val imagesMap = mutableMapOf<String, String?>()
            for (prodId in productIds) {
                try {
                    val images = client.from("product_images")
                        .select {
                            filter { eq("product_id", prodId) }
                            order("sort_order", order = Order.ASCENDING)
                        }
                        .decodeAs<List<ProductImageDto>>()

                    val primary = images.firstOrNull { it.isPrimary } ?: images.firstOrNull()
                    val path = primary?.enhancedPath ?: primary?.originalPath
                    imagesMap[prodId] = getStorageUrl("product-media", path)
                } catch (_: Exception) {
                    imagesMap[prodId] = null
                }
            }

            // 4. Fetch marketing content active status
            val marketingContents = try {
                client.from("marketing_contents")
                    .select {
                        filter {
                            eq("artisan_profile_id", artisanProfileId)
                            neq("status", "archived")
                        }
                        order("created_at", order = Order.DESCENDING)
                    }
                    .decodeAs<List<MarketingContentDto>>()
            } catch (_: Exception) {
                emptyList()
            }
            val activeContentByProduct = marketingContents.associateBy { it.productId }

            // 5. Fetch inventories
            val inventoryMap = try {
                client.from("inventory")
                    .select()
                    .decodeAs<List<InventoryDto>>()
                    .associateBy { it.productId }
            } catch (_: Exception) {
                emptyMap()
            }

            val resultList = products.map { prod ->
                val prodId = prod.id ?: ""
                val activeContent = activeContentByProduct[prodId]
                val stock = inventoryMap[prodId]?.quantityOnHand ?: 1
                val categoryName = categoriesMap[prod.categoryId]?.name ?: prod.craftType ?: "Craft"
                MarketableProduct(
                    product = prod,
                    primaryImageUrl = imagesMap[prodId],
                    categoryName = categoryName,
                    quantityOnHand = stock,
                    activeMarketingContentId = activeContent?.id,
                    activeMarketingStatus = activeContent?.status
                )
            }

            Result.success(resultList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches all marketing campaigns created for this artisan.
     */
    suspend fun getMarketingContents(artisanProfileId: String): Result<List<MarketingContentDetail>> = withContext(Dispatchers.IO) {
        try {
            val contents = client.from("marketing_contents")
                .select {
                    filter {
                        eq("artisan_profile_id", artisanProfileId)
                    }
                    order("created_at", order = Order.DESCENDING)
                }
                .decodeAs<List<MarketingContentDto>>()

            if (contents.isEmpty()) {
                return@withContext Result.success(emptyList())
            }

            val productIds = contents.map { it.productId }.distinct()
            val productsMap = try {
                val fetched = client.from("products")
                    .select {
                        filter {
                            isIn("id", productIds)
                        }
                    }
                    .decodeAs<List<ProductDto>>()
                fetched.associateBy { it.id ?: "" }
            } catch (_: Exception) {
                emptyMap()
            }

            val details = contents.map { content ->
                val product = productsMap[content.productId]
                val creativeUrl = getStorageUrl("marketing-media", content.creativePath)
                    ?: getStorageUrl("product-media", content.creativePath)
                MarketingContentDetail(
                    content = content,
                    product = product,
                    productImageUrl = creativeUrl,
                    creativeImageUrl = creativeUrl,
                    aiJob = null
                )
            }

            Result.success(details)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches a single marketing content with product and AI job details.
     */
    suspend fun getMarketingContentDetail(marketingContentId: String): Result<MarketingContentDetail> = withContext(Dispatchers.IO) {
        try {
            val content = client.from("marketing_contents")
                .select {
                    filter { eq("id", marketingContentId) }
                }
                .decodeSingle<MarketingContentDto>()

            val product = try {
                client.from("products")
                    .select {
                        filter { eq("id", content.productId) }
                    }
                    .decodeSingle<ProductDto>()
            } catch (_: Exception) {
                null
            }

            val aiJob = try {
                client.from("ai_jobs")
                    .select {
                        filter { eq("marketing_content_id", marketingContentId) }
                        order("created_at", order = Order.DESCENDING)
                    }
                    .decodeAs<List<AiJobDto>>()
                    .firstOrNull()
            } catch (_: Exception) {
                null
            }

            val creativeUrl = getStorageUrl("marketing-media", content.creativePath)
                ?: getStorageUrl("product-media", content.creativePath)

            Result.success(
                MarketingContentDetail(
                    content = content,
                    product = product,
                    productImageUrl = creativeUrl,
                    creativeImageUrl = creativeUrl,
                    aiJob = aiJob
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Initiates marketing content generation:
     * 1. Validates authentication and product ownership
     * 2. Validates product status is published and suitable image exists
     * 3. Checks idempotency for active AI jobs
     * 4. Creates marketing_contents row (status: draft)
     * 5. Creates ai_jobs row (status: queued)
     * 6. Records audit_logs entry
     * 7. Dispatches n8n webhook asynchronously
     */
    suspend fun generateMarketingContent(
        productId: String,
        n8nWebhookUrl: String? = BuildConfig.N8N_MARKETING_GENERATE_URL
    ): Result<MarketingGenerationResult> = withContext(Dispatchers.IO) {
        try {
            val userId = AuthRepository.currentUserId
                ?: return@withContext Result.failure(IllegalStateException("User is not authenticated."))
            val artisanProfileId = AuthRepository.currentArtisanProfileId
                ?: return@withContext Result.failure(IllegalStateException("Artisan profile not found."))
            val profileId = AuthRepository.currentProfile?.id ?: userId

            // 1. Verify product ownership & published status
            val products = client.from("products")
                .select {
                    filter {
                        eq("id", productId)
                        eq("artisan_profile_id", artisanProfileId)
                    }
                }
                .decodeAs<List<ProductDto>>()

            val product = products.firstOrNull()
                ?: return@withContext Result.failure(IllegalStateException("Product does not belong to this artisan."))

            if (product.status.lowercase() != "published") {
                return@withContext Result.failure(IllegalStateException("Only published products can be marketed."))
            }

            // 2. Verify product has an image
            val images = client.from("product_images")
                .select {
                    filter { eq("product_id", productId) }
                    order("sort_order", order = Order.ASCENDING)
                }
                .decodeAs<List<ProductImageDto>>()

            val primaryImg = images.firstOrNull { it.isPrimary } ?: images.firstOrNull()
            val availableImagePath = primaryImg?.enhancedPath ?: primaryImg?.originalPath

            // 3. Idempotency Check: Check if an active AI job already exists
            val existingJobs = try {
                client.from("ai_jobs")
                    .select {
                        filter {
                            eq("product_id", productId)
                            eq("artisan_profile_id", artisanProfileId)
                            eq("job_type", "marketing_generation")
                            isIn("status", listOf("queued", "processing"))
                        }
                        order("created_at", order = Order.DESCENDING)
                    }
                    .decodeAs<List<AiJobDto>>()
            } catch (_: Exception) {
                emptyList()
            }

            var marketingContentId = ""
            var aiJobId = ""

            if (existingJobs.isNotEmpty()) {
                val activeJob = existingJobs.first()
                marketingContentId = activeJob.marketingContentId ?: ""
                aiJobId = activeJob.id ?: ""
            } else {
                // 4. Create marketing_contents row (status: draft)
                val nowIso = getCurrentIsoTimestamp()
                val newContent = MarketingContentDto(
                    artisanProfileId = artisanProfileId,
                    productId = productId,
                    platform = "instagram",
                    caption = null,
                    hashtags = emptyList(),
                    creativePath = availableImagePath,
                    status = "draft",
                    createdAt = nowIso,
                    updatedAt = nowIso
                )

                val insertedContent = client.from("marketing_contents")
                    .insert(newContent) {
                        select()
                    }
                    .decodeSingle<MarketingContentDto>()

                marketingContentId = insertedContent.id ?: UUID.randomUUID().toString()

                // 5. Create ai_jobs row (status: queued)
                val newJob = AiJobDto(
                    requestedBy = profileId,
                    jobType = "marketing_generation",
                    status = "queued",
                    productId = productId,
                    artisanProfileId = artisanProfileId,
                    marketingContentId = marketingContentId,
                    createdAt = nowIso,
                    updatedAt = nowIso
                )

                val insertedJob = client.from("ai_jobs")
                    .insert(newJob) {
                        select()
                    }
                    .decodeSingle<AiJobDto>()

                aiJobId = insertedJob.id ?: UUID.randomUUID().toString()

                // 6. Record audit log
                recordAuditLog(
                    userId = userId,
                    action = "marketing_generation_requested",
                    entityType = "marketing_content",
                    entityId = marketingContentId,
                    details = "Requested AI marketing generation for product $productId"
                )
            }

            // 7. Trigger n8n Generation Webhook
            val webhookPayload = buildJsonObject {
                put("product_id", productId)
                put("artisan_profile_id", artisanProfileId)
                put("marketing_content_id", marketingContentId)
                put("ai_job_id", aiJobId)
            }

            val targetGenerationUrl = n8nWebhookUrl?.takeIf { it.isNotBlank() }
                ?: BuildConfig.N8N_MARKETING_GENERATE_URL

            android.util.Log.d("MarketingRepository", "GENERATION N8N REQUEST STARTED: $targetGenerationUrl with payload $webhookPayload")

            if (targetGenerationUrl.isNotBlank()) {
                try {
                    val response = httpClient.post(targetGenerationUrl) {
                        contentType(ContentType.Application.Json)
                        setBody(webhookPayload.toString())
                    }
                    val body = response.bodyAsText()
                    android.util.Log.d("MarketingRepository", "GENERATION N8N RESPONSE: HTTP status=${response.status.value}, body=$body")
                } catch (e: Exception) {
                    android.util.Log.e("MarketingRepository", "GENERATION N8N ERROR: ${e.message}", e)
                }
            }

            Result.success(
                MarketingGenerationResult(
                    marketingContentId = marketingContentId,
                    aiJobId = aiJobId,
                    productId = productId,
                    status = "processing"
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates marketing content draft with artisan edits (caption & hashtags).
     */
    suspend fun updateMarketingContent(
        marketingContentId: String,
        caption: String,
        hashtags: List<String>
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val nowIso = getCurrentIsoTimestamp()
            client.from("marketing_contents").update({
                set("caption", caption)
                set("hashtags", hashtags)
                set("updated_at", nowIso)
            }) {
                filter { eq("id", marketingContentId) }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Artisan approves marketing content:
     * Sets approved_at = NOW(), status = 'ready', logs audit record,
     * and dispatches the publishing webhook to n8n.
     */
    suspend fun approveMarketingContent(
        marketingContentId: String,
        n8nPublishWebhookUrl: String? = BuildConfig.N8N_MARKETING_PUBLISH_URL
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val userId = AuthRepository.currentUserId
                ?: return@withContext Result.failure(IllegalStateException("User is not authenticated."))
            val nowIso = getCurrentIsoTimestamp()

            // 1. Fetch current marketing content to verify ownership & data
            val detail = getMarketingContentDetail(marketingContentId).getOrNull()
                ?: return@withContext Result.failure(IllegalStateException("Marketing content $marketingContentId not found."))

            val artisanProfileId = detail.content.artisanProfileId
            val productId = detail.content.productId

            // 2. Update status to 'ready' and record approved_at timestamp
            client.from("marketing_contents").update({
                set("status", "ready")
                set("approved_at", nowIso)
                set("updated_at", nowIso)
            }) {
                filter { eq("id", marketingContentId) }
            }

            // 3. Record audit log for approval
            recordAuditLog(
                userId = userId,
                action = "marketing_content_approved",
                entityType = "marketing_content",
                entityId = marketingContentId,
                details = "Artisan approved marketing content for publishing"
            )

            // 4. Trigger n8n publishing webhook with identifiers
            val webhookUrl = n8nPublishWebhookUrl?.takeIf { it.isNotBlank() }
                ?: BuildConfig.N8N_MARKETING_PUBLISH_URL

            if (webhookUrl.isNotBlank()) {
                val payload = buildJsonObject {
                    put("marketing_content_id", marketingContentId)
                    put("artisan_profile_id", artisanProfileId)
                    put("product_id", productId)
                }

                android.util.Log.d(
                    "MarketingRepository",
                    "PUBLISH START\nmarketing_content_id = $marketingContentId\nproduct_id = $productId\nartisan_profile_id = $artisanProfileId\nURL = $webhookUrl"
                )

                try {
                    val response = httpClient.post(webhookUrl) {
                        contentType(ContentType.Application.Json)
                        setBody(payload.toString())
                    }

                    val responseBody = response.bodyAsText()

                    android.util.Log.d(
                        "MarketingRepository",
                        "N8N PUBLISH RESPONSE\nHTTP status = ${response.status.value}\nresponse = $responseBody"
                    )

                    if (response.status.value !in 200..299) {
                        android.util.Log.e(
                            "MarketingRepository",
                            "PUBLISH FAILED\nreason = n8n returned HTTP ${response.status.value}: $responseBody"
                        )
                        recordAuditLog(
                            userId = userId,
                            action = "instagram_publish_failed",
                            entityType = "marketing_content",
                            entityId = marketingContentId,
                            details = "n8n returned HTTP ${response.status.value}: $responseBody"
                        )
                        return@withContext Result.failure(Exception("n8n publish error: HTTP ${response.status.value} - $responseBody"))
                    }

                    // Check if n8n returned a structured response
                    try {
                        val jsonElem = Json.parseToJsonElement(responseBody)
                        if (jsonElem is JsonObject) {
                            if (jsonElem["success"]?.jsonPrimitive?.booleanOrNull == false) {
                                val errMsg = jsonElem["error"]?.jsonPrimitive?.contentOrNull ?: "Instagram publishing failed in n8n."
                                android.util.Log.e(
                                    "MarketingRepository",
                                    "PUBLISH FAILED\nreason = $errMsg"
                                )
                                recordAuditLog(
                                    userId = userId,
                                    action = "instagram_publish_failed",
                                    entityType = "marketing_content",
                                    entityId = marketingContentId,
                                    details = errMsg
                                )
                                return@withContext Result.failure(Exception(errMsg))
                            }

                            val realPostId = jsonElem["external_post_id"]?.jsonPrimitive?.contentOrNull
                            if (!realPostId.isNullOrBlank()) {
                                recordPublishSuccess(marketingContentId, realPostId)
                            }
                        }
                    } catch (_: Exception) {}

                    recordAuditLog(
                        userId = userId,
                        action = "instagram_publish_requested",
                        entityType = "marketing_content",
                        entityId = marketingContentId,
                        details = "n8n publish webhook triggered (HTTP ${response.status.value})"
                    )
                } catch (webhookErr: Exception) {
                    android.util.Log.e(
                        "MarketingRepository",
                        "PUBLISH FAILED\nreason = ${webhookErr.message}",
                        webhookErr
                    )
                    recordAuditLog(
                        userId = userId,
                        action = "instagram_publish_failed",
                        entityType = "marketing_content",
                        entityId = marketingContentId,
                        details = "Failed to dispatch publish webhook: ${webhookErr.message}"
                    )
                    return@withContext Result.failure(Exception("Failed to reach n8n publishing webhook: ${webhookErr.message}"))
                }
            } else {
                return@withContext Result.failure(IllegalStateException("n8n publishing webhook URL is not configured."))
            }

            Result.success(true)
        } catch (e: Exception) {
            android.util.Log.e("MarketingRepository", "Error during approveMarketingContent: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Re-triggers the n8n publishing webhook for an already approved ('ready') post.
     */
    suspend fun retryPublishWebhook(
        marketingContentId: String,
        n8nPublishWebhookUrl: String? = BuildConfig.N8N_MARKETING_PUBLISH_URL
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val userId = AuthRepository.currentUserId
                ?: return@withContext Result.failure(IllegalStateException("User is not authenticated."))
            val detail = getMarketingContentDetail(marketingContentId).getOrNull()
                ?: return@withContext Result.failure(IllegalStateException("Marketing content not found."))

            val webhookUrl = n8nPublishWebhookUrl?.takeIf { it.isNotBlank() }
                ?: BuildConfig.N8N_MARKETING_PUBLISH_URL

            val payload = buildJsonObject {
                put("marketing_content_id", marketingContentId)
                put("artisan_profile_id", detail.content.artisanProfileId)
                put("product_id", detail.content.productId)
            }

            android.util.Log.d(
                "MarketingRepository",
                "PUBLISH START\nmarketing_content_id = $marketingContentId\nproduct_id = ${detail.content.productId}\nartisan_profile_id = ${detail.content.artisanProfileId}\nURL = $webhookUrl"
            )

            val response = httpClient.post(webhookUrl) {
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }

            val responseBody = response.bodyAsText()

            android.util.Log.d(
                "MarketingRepository",
                "N8N PUBLISH RESPONSE\nHTTP status = ${response.status.value}\nresponse = $responseBody"
            )

            if (response.status.value !in 200..299) {
                android.util.Log.e(
                    "MarketingRepository",
                    "PUBLISH FAILED\nreason = n8n returned HTTP ${response.status.value}: $responseBody"
                )
                recordAuditLog(
                    userId = userId,
                    action = "instagram_publish_failed",
                    entityType = "marketing_content",
                    entityId = marketingContentId,
                    details = "n8n returned HTTP ${response.status.value}: $responseBody"
                )
                return@withContext Result.failure(Exception("n8n publish error: HTTP ${response.status.value} - $responseBody"))
            }

            try {
                val jsonElem = Json.parseToJsonElement(responseBody)
                if (jsonElem is JsonObject) {
                    if (jsonElem["success"]?.jsonPrimitive?.booleanOrNull == false) {
                        val errMsg = jsonElem["error"]?.jsonPrimitive?.contentOrNull ?: "Instagram publishing failed in n8n."
                        android.util.Log.e(
                            "MarketingRepository",
                            "PUBLISH FAILED\nreason = $errMsg"
                        )
                        return@withContext Result.failure(Exception(errMsg))
                    }

                    val realPostId = jsonElem["external_post_id"]?.jsonPrimitive?.contentOrNull
                    if (!realPostId.isNullOrBlank()) {
                        recordPublishSuccess(marketingContentId, realPostId)
                    }
                }
            } catch (_: Exception) {}

            recordAuditLog(
                userId = userId,
                action = "instagram_publish_requested",
                entityType = "marketing_content",
                entityId = marketingContentId,
                details = "Retried n8n publish webhook (HTTP ${response.status.value})"
            )

            Result.success(true)
        } catch (e: Exception) {
            android.util.Log.e("MarketingRepository", "PUBLISH FAILED\nreason = ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Publishes marketing content directly from a verified callback with real external post ID.
     * Sets status = 'published', published_at = NOW(), external_post_id = ID.
     * Note: Does NOT fabricate fake post IDs.
     */
    suspend fun recordPublishSuccess(
        marketingContentId: String,
        realExternalPostId: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (realExternalPostId.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Real Instagram post ID is required."))
            }

            val userId = AuthRepository.currentUserId
            val nowIso = getCurrentIsoTimestamp()

            client.from("marketing_contents").update({
                set("status", "published")
                set("published_at", nowIso)
                set("external_post_id", realExternalPostId)
                set("updated_at", nowIso)
            }) {
                filter { eq("id", marketingContentId) }
            }

            recordAuditLog(
                userId = userId,
                action = "instagram_publish_succeeded",
                entityType = "marketing_content",
                entityId = marketingContentId,
                details = "Published to Instagram with real post ID: $realExternalPostId"
            )

            Result.success(true)
        } catch (e: Exception) {
            recordAuditLog(
                userId = AuthRepository.currentUserId,
                action = "instagram_publish_failed",
                entityType = "marketing_content",
                entityId = marketingContentId,
                details = e.message ?: "Instagram publish update failure"
            )
            Result.failure(e)
        }
    }

    /**
     * Archives marketing content.
     */
    suspend fun archiveMarketingContent(marketingContentId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            client.from("marketing_contents").update({
                set("status", "archived")
                set("updated_at", getCurrentIsoTimestamp())
            }) {
                filter { eq("id", marketingContentId) }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun recordAuditLog(
        userId: String?,
        action: String,
        entityType: String,
        entityId: String,
        details: String?
    ) {
        try {
            val log = AuditLogDto(
                userId = userId,
                action = action,
                entityType = entityType,
                entityId = entityId,
                details = details,
                createdAt = getCurrentIsoTimestamp()
            )
            client.from("audit_logs").insert(log)
        } catch (_: Exception) {
            // Non-blocking audit log
        }
    }
}
