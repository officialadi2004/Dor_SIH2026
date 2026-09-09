package com.artknower.app.data.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class VoicePipelineRequest(
    @SerialName("audio_base64")
    val audioBase64: String,
    @SerialName("audio_encoding")
    val audioEncoding: String = "WEBM_OPUS",
    @SerialName("sample_rate_hertz")
    val sampleRateHertz: Int? = null,
    @SerialName("spoken_language")
    val spokenLanguage: String = "hi",
    @SerialName("product_id")
    val productId: String? = null,
    @EncodeDefault
    @SerialName("auto_store")
    val autoStore: Boolean = false
)

@Serializable
data class ExtractedProductDraftDto(
    @SerialName("name_candidate")
    val nameCandidate: String? = null,
    @SerialName("description_candidate")
    val descriptionCandidate: String? = null,
    @SerialName("material_hint")
    val materialHint: String? = null,
    @SerialName("color_hint")
    val colorHint: String? = null,
    @SerialName("price_hint")
    val priceHint: Int? = null,
    @SerialName("extraction_confidence")
    val extractionConfidence: Double = 0.0
)

@Serializable
data class TranslatedItemDto(
    @SerialName("language_code")
    val languageCode: String,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String? = null
)

@Serializable
data class TranslateCatalogResponseDto(
    @SerialName("product_id")
    val productId: String? = null,
    @SerialName("source_language")
    val sourceLanguage: String? = null,
    @SerialName("translations")
    val translations: List<TranslatedItemDto> = emptyList(),
    @SerialName("is_mock")
    val isMock: Boolean = false,
    @SerialName("skipped_languages")
    val skippedLanguages: List<String> = emptyList()
)

@Serializable
data class VoicePipelineResponse(
    @SerialName("transcript")
    val transcript: String,
    @SerialName("transcript_confidence")
    val transcriptConfidence: Double = 0.0,
    @SerialName("detected_language")
    val detectedLanguage: String,
    @SerialName("language_confidence")
    val languageConfidence: Double = 0.0,
    @SerialName("extracted_draft")
    val extractedDraft: ExtractedProductDraftDto? = null,
    @SerialName("generated_translations")
    val generatedTranslations: TranslateCatalogResponseDto? = null,
    @SerialName("stored")
    val stored: Boolean = false,
    @SerialName("skipped_languages")
    val skippedLanguages: List<String> = emptyList(),
    @SerialName("is_mock")
    val isMock: Boolean = false
)

@Serializable
data class SpeechTranscribeRequest(
    @SerialName("audio_base64")
    val audioBase64: String,
    @SerialName("language_code")
    val languageCode: String = "hi",
    @SerialName("audio_encoding")
    val audioEncoding: String = "WEBM_OPUS",
    @SerialName("sample_rate_hertz")
    val sampleRateHertz: Int? = null
)

@Serializable
data class SpeechTranscribeResponse(
    @SerialName("transcript")
    val transcript: String,
    @SerialName("confidence")
    val confidence: Double = 0.0,
    @SerialName("language_code")
    val languageCode: String = "hi",
    @SerialName("is_mock")
    val isMock: Boolean = false
)

@Serializable
data class DescriptionGenerateRequest(
    @SerialName("speech_input")
    val speechInput: String,
    @SerialName("language")
    val language: String = "hi"
)

@Serializable
data class DescriptionGenerateResponse(
    @SerialName("input_text")
    val inputText: String,
    @SerialName("language")
    val language: String,
    @SerialName("english_description")
    val englishDescription: String,
    @SerialName("generation_source")
    val generationSource: String
)
