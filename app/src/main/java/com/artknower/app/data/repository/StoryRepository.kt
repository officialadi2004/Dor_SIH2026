package com.artknower.app.data.repository

import com.artknower.app.data.model.ArtisanStoryDto
import com.artknower.app.data.remote.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object StoryRepository {
    private val client = SupabaseClientProvider.adminClient

    suspend fun saveStory(
        artisanProfileId: String,
        storyText: String,
        generatedStoryText: String? = null,
        sourceLanguage: String = "en"
    ): Result<ArtisanStoryDto> = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch existing story to get its ID, if any.
            val existingStory = client.from("artisan_stories")
                .select { filter { eq("artisan_profile_id", artisanProfileId) } }
                .decodeAs<List<ArtisanStoryDto>>()
                .firstOrNull()

            val storyDto = ArtisanStoryDto(
                id = existingStory?.id ?: java.util.UUID.randomUUID().toString(),
                artisanProfileId = artisanProfileId,
                sourceText = storyText,
                generatedStory = generatedStoryText ?: storyText,
                approvedStory = generatedStoryText ?: storyText,
                sourceLanguage = sourceLanguage,
                isPublished = true
            )

            client.from("artisan_stories").upsert(storyDto)

            Result.success(storyDto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStory(artisanProfileId: String): Result<ArtisanStoryDto?> = withContext(Dispatchers.IO) {
        try {
            val stories = client.from("artisan_stories")
                .select {
                    filter {
                        eq("artisan_profile_id", artisanProfileId)
                    }
                }
                .decodeAs<List<ArtisanStoryDto>>()
            Result.success(stories.firstOrNull())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
