package com.artknower.app.data.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ArtisanStoryDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("artisan_profile_id")
    val artisanProfileId: String,
    @SerialName("source_text")
    val sourceText: String? = null,
    @SerialName("generated_story")
    val generatedStory: String? = null,
    @SerialName("approved_story")
    val approvedStory: String? = null,
    @EncodeDefault
    @SerialName("source_language")
    val sourceLanguage: String = "en",
    @EncodeDefault
    @SerialName("is_published")
    val isPublished: Boolean = false
)
