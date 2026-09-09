package com.artknower.app.data.repository

import com.artknower.app.data.model.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object SpeechRepository {
    private const val BASE_URL = "https://artisan-speech-translation.onrender.com"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val httpClient = HttpClient(Android) {
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000L
            connectTimeoutMillis = 30_000L
            socketTimeoutMillis = 60_000L
        }
    }

    /**
     * Executes the complete artisan voice cataloging pipeline:
     * 1. Speech-to-text
     * 2. Language identification
     * 3. Product/craft extraction
     * 4. 3-line rich description generation
     * 5. Translation into English, Hindi, and Marathi
     *
     * Auto store is false to allow artisan review before saving.
     */
    suspend fun runVoiceCatalogPipeline(
        audioBase64: String,
        audioEncoding: String = "WEBM_OPUS",
        spokenLanguage: String = "hi",
        sampleRateHertz: Int? = 16000,
        productId: String? = null,
        autoStore: Boolean = false
    ): Result<VoicePipelineResponse> = withContext(Dispatchers.IO) {
        try {
            val request = VoicePipelineRequest(
                audioBase64 = audioBase64,
                audioEncoding = audioEncoding,
                sampleRateHertz = sampleRateHertz,
                spokenLanguage = spokenLanguage,
                productId = productId,
                autoStore = autoStore
            )

            val requestBody = json.encodeToString(VoicePipelineRequest.serializer(), request)

            val response = httpClient.post("$BASE_URL/api/v1/pipeline/voice-catalog") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            val responseText = response.bodyAsText()

            if (response.status.isSuccess()) {
                val pipelineResponse = json.decodeFromString<VoicePipelineResponse>(responseText)
                Result.success(pipelineResponse)
            } else {
                Result.failure(Exception("Voice pipeline returned status ${response.status.value}: $responseText"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generates a polished artisan description from transcript text.
     */
    suspend fun generateArtisanDescription(
        speechInput: String,
        language: String = "hi"
    ): Result<DescriptionGenerateResponse> = withContext(Dispatchers.IO) {
        try {
            val request = DescriptionGenerateRequest(
                speechInput = speechInput,
                language = language
            )

            val requestBody = json.encodeToString(DescriptionGenerateRequest.serializer(), request)

            val response = httpClient.post("$BASE_URL/api/v1/description/generate") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            val responseText = response.bodyAsText()

            if (response.status.isSuccess()) {
                val generateResponse = json.decodeFromString<DescriptionGenerateResponse>(responseText)
                Result.success(generateResponse)
            } else {
                Result.failure(Exception("Description generation returned status ${response.status.value}: $responseText"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
