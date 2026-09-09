package com.artknower.app.data.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class MarketingContentDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("artisan_profile_id")
    val artisanProfileId: String,
    @SerialName("product_id")
    val productId: String,
    @EncodeDefault
    @SerialName("platform")
    val platform: String = "instagram",
    @SerialName("title")
    val title: String? = null,
    @SerialName("caption")
    val caption: String? = null,
    @EncodeDefault
    @SerialName("hashtags")
    val hashtags: List<String> = emptyList(),
    @SerialName("creative_path")
    val creativePath: String? = null,
    @EncodeDefault
    @SerialName("status")
    val status: String = "draft", // draft, ready, published, archived
    @SerialName("approved_at")
    val approvedAt: String? = null,
    @SerialName("published_at")
    val publishedAt: String? = null,
    @SerialName("external_post_id")
    val externalPostId: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class AiJobDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("requested_by")
    val requestedBy: String,
    @EncodeDefault
    @SerialName("job_type")
    val jobType: String = "marketing_generation",
    @EncodeDefault
    @SerialName("status")
    val status: String = "queued", // queued, processing, completed, failed, cancelled
    @SerialName("product_id")
    val productId: String? = null,
    @SerialName("artisan_profile_id")
    val artisanProfileId: String? = null,
    @SerialName("marketing_content_id")
    val marketingContentId: String? = null,
    @SerialName("error_message")
    val errorMessage: String? = null,
    @SerialName("completed_at")
    val completedAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class AiUsageDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("ai_job_id")
    val aiJobId: String? = null,
    @SerialName("user_id")
    val userId: String,
    @EncodeDefault
    @SerialName("provider")
    val provider: String = "gemini",
    @EncodeDefault
    @SerialName("model_name")
    val modelName: String = "gemini-1.5-flash",
    @EncodeDefault
    @SerialName("feature")
    val feature: String = "marketing_generation",
    @EncodeDefault
    @SerialName("request_count")
    val requestCount: Int = 1,
    @SerialName("token_count")
    val tokenCount: Int? = null,
    @EncodeDefault
    @SerialName("image_count")
    val imageCount: Int = 1,
    @SerialName("estimated_cost")
    val estimatedCost: Double? = 0.002,
    @SerialName("created_at")
    val createdAt: String? = null
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class AuditLogDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("action")
    val action: String,
    @SerialName("entity_type")
    val entityType: String,
    @SerialName("entity_id")
    val entityId: String,
    @SerialName("details")
    val details: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

data class MarketableProduct(
    val product: ProductDto,
    val primaryImageUrl: String? = null,
    val categoryName: String? = null,
    val quantityOnHand: Int = 1,
    val activeMarketingContentId: String? = null,
    val activeMarketingStatus: String? = null
)

data class MarketingGenerationResult(
    val marketingContentId: String,
    val aiJobId: String,
    val productId: String,
    val status: String
)

data class MarketingContentDetail(
    val content: MarketingContentDto,
    val product: ProductDto?,
    val productImageUrl: String?,
    val creativeImageUrl: String?,
    val aiJob: AiJobDto?
)

