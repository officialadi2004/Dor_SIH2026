package com.artknower.app.data.repository

import com.artknower.app.data.model.ArtisanProfileDto
import com.artknower.app.data.model.BusinessProfileDto
import com.artknower.app.data.model.ProfileDto
import com.artknower.app.data.remote.SupabaseClientProvider
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AuthRepository {
    private val client = SupabaseClientProvider.client
    private val db = SupabaseClientProvider.adminClient
    
    var currentUserId: String? = null
    var currentArtisanProfileId: String? = null
    var currentProfile: ProfileDto? = null
    var currentArtisanProfile: ArtisanProfileDto? = null

    private fun resolveAuthEmail(identifier: String): String {
        val trimmed = identifier.trim()
        return if (trimmed.contains("@")) {
            trimmed.lowercase()
        } else {
            val digits = trimmed.filter { it.isDigit() }
            "${digits}@artknower.app"
        }
    }

    suspend fun registerArtisan(
        name: String,
        email: String,
        phone: String,
        cityState: String,
        craft: String,
        password: String,
        role: String = "artisan",
        preferredLanguage: String = "en",
        experienceYears: Int? = null,
        country: String = "India"
    ): Result<ArtisanProfileDto> = withContext(Dispatchers.IO) {
        try {
            val trimmedEmail = email.trim().lowercase()
            val trimmedPhone = phone.trim()
            val authPassword = password.trim()
            val normalizedRole = role.trim().lowercase()

            // 1. Supabase Auth signup: creates user in auth.users with user metadata
            // Database trigger automatically sets profiles.role = raw_user_meta_data->>'role'
            val authUser = try {
                client.auth.signUpWith(Email) {
                    this.email = trimmedEmail
                    this.password = authPassword
                    this.data = kotlinx.serialization.json.buildJsonObject {
                        put("role", kotlinx.serialization.json.JsonPrimitive(normalizedRole))
                        put("full_name", kotlinx.serialization.json.JsonPrimitive(name.trim()))
                        put("phone", kotlinx.serialization.json.JsonPrimitive(trimmedPhone))
                    }
                }
                client.auth.currentUserOrNull()
            } catch (e: Exception) {
                // If user already exists in auth.users, sign in to acquire session
                try {
                    client.auth.signInWith(Email) {
                        this.email = trimmedEmail
                        this.password = authPassword
                    }
                    client.auth.currentUserOrNull()
                } catch (signInEx: Exception) {
                    return@withContext Result.failure(Exception(e.message ?: signInEx.message ?: "Sign up failed"))
                }
            }

            val userId = client.auth.currentUserOrNull()?.id
                ?: return@withContext Result.failure(
                    IllegalStateException("Account created! Please check your email ($trimmedEmail) to confirm your account before logging in, or disable email confirmation in your Supabase Auth settings.")
                )

            currentUserId = userId

            val locationParts = cityState.split(",").map { it.trim() }
            val city = locationParts.getOrNull(0)
            val state = locationParts.getOrNull(1) ?: locationParts.getOrNull(0)

            // 2. Insert/update public.profiles (safely keeping role to avoid role mutation triggers)
            val existingProfiles = try {
                db.from("profiles")
                    .select {
                        filter {
                            eq("id", userId)
                        }
                    }
                    .decodeAs<List<ProfileDto>>()
            } catch (_: Exception) {
                emptyList()
            }
            val roleToUse = existingProfiles.firstOrNull()?.role ?: normalizedRole

            val profile = ProfileDto(
                id = userId,
                fullName = name.trim(),
                phone = trimmedPhone,
                role = roleToUse,
                preferredLanguage = preferredLanguage,
                isActive = true
            )
            db.from("profiles").upsert(profile)
            currentProfile = profile

            // 3. Create role-specific profile
            if (roleToUse == "artisan") {
                val artisanProfile = ArtisanProfileDto(
                    userId = userId,
                    displayName = name.trim(),
                    locationCity = city,
                    locationState = state,
                    locationCountry = country,
                    craftSummary = craft.trim(),
                    experienceYears = experienceYears,
                    isPublished = true
                )
                db.from("artisan_profiles").upsert(artisanProfile)

                val insertedList = db.from("artisan_profiles")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeAs<List<ArtisanProfileDto>>()

                val resultProfile = insertedList.firstOrNull() ?: artisanProfile
                currentArtisanProfileId = resultProfile.id
                currentArtisanProfile = resultProfile
                Result.success(resultProfile)
            } else if (roleToUse == "business") {
                val businessProfile = BusinessProfileDto(
                    userId = userId,
                    businessName = name.trim(),
                    contactPersonName = name.trim(),
                    contactPhone = trimmedPhone,
                    contactEmail = trimmedEmail,
                    locationCity = city,
                    locationState = state,
                    locationCountry = country,
                    businessDescription = craft.trim(),
                    isActive = true
                )
                try {
                    db.from("business_profiles").upsert(businessProfile)
                } catch (_: Exception) {}

                // Synthetic artisan profile wrapper for navigation
                val fallbackArtisan = ArtisanProfileDto(
                    id = userId,
                    userId = userId,
                    displayName = name.trim(),
                    locationCity = city,
                    locationState = state,
                    locationCountry = country,
                    craftSummary = "Business Buyer: ${craft.trim()}"
                )
                Result.success(fallbackArtisan)
            } else {
                val customerArtisan = ArtisanProfileDto(
                    id = userId,
                    userId = userId,
                    displayName = name.trim(),
                    locationCity = city,
                    locationState = state,
                    locationCountry = country,
                    craftSummary = "Customer"
                )
                Result.success(customerArtisan)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginArtisan(identifier: String, password: String): Result<ArtisanProfileDto> = withContext(Dispatchers.IO) {
        try {
            val authEmail = resolveAuthEmail(identifier)
            val authPassword = password.trim()

            // 1. Authenticate with Supabase Auth (auth.users)
            client.auth.signInWith(Email) {
                email = authEmail
                this.password = authPassword
            }

            val authUser = client.auth.currentUserOrNull()
                ?: return@withContext Result.failure(IllegalStateException("Authentication succeeded but no active session was returned."))

            val userId = authUser.id
            currentUserId = userId

            // 2. Fetch public.profiles
            val profiles = db.from("profiles")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeAs<List<ProfileDto>>()

            val profile = profiles.firstOrNull()
            currentProfile = profile

            // 3. Fetch public.artisan_profiles
            val artisanProfiles = db.from("artisan_profiles")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeAs<List<ArtisanProfileDto>>()

            val artisan = artisanProfiles.firstOrNull()
                ?: ArtisanProfileDto(
                    id = userId,
                    userId = userId,
                    displayName = profile?.fullName ?: authEmail.substringBefore("@"),
                    locationCity = null,
                    locationState = null,
                    craftSummary = "Artisan / ${profile?.role ?: "user"}"
                )

            currentArtisanProfileId = artisan.id
            currentArtisanProfile = artisan
            Result.success(artisan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateArtisanProfile(
        displayName: String,
        phone: String,
        city: String,
        state: String,
        craftSummary: String,
        experienceYears: Int? = null,
        country: String = "India",
        preferredLanguage: String = "en"
    ): Result<ArtisanProfileDto> = withContext(Dispatchers.IO) {
        try {
            val userId = currentUserId ?: return@withContext Result.failure(IllegalStateException("No user currently logged in."))
            val artisanId = currentArtisanProfileId ?: return@withContext Result.failure(IllegalStateException("No artisan profile active."))

            val updatedProfile = ProfileDto(
                id = userId,
                fullName = displayName.trim(),
                phone = phone.trim(),
                role = currentProfile?.role ?: "artisan",
                preferredLanguage = preferredLanguage,
                isActive = true
            )
            db.from("profiles").upsert(updatedProfile)
            currentProfile = updatedProfile

            val updatedArtisan = ArtisanProfileDto(
                id = artisanId,
                userId = userId,
                displayName = displayName.trim(),
                locationCity = city.trim(),
                locationState = state.trim(),
                locationCountry = country.trim().ifBlank { "India" },
                craftSummary = craftSummary.trim(),
                experienceYears = experienceYears ?: currentArtisanProfile?.experienceYears,
                profilePhotoPath = currentArtisanProfile?.profilePhotoPath,
                isPublished = true
            )
            db.from("artisan_profiles").upsert(updatedArtisan)
            currentArtisanProfile = updatedArtisan

            Result.success(updatedArtisan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshCurrentProfile(): Result<Pair<ProfileDto?, ArtisanProfileDto?>> = withContext(Dispatchers.IO) {
        try {
            val userId = currentUserId ?: client.auth.currentUserOrNull()?.id
            if (userId.isNullOrBlank()) {
                return@withContext Result.success(Pair(currentProfile, currentArtisanProfile))
            }
            currentUserId = userId

            // 1. Fetch public.profiles
            val profiles = db.from("profiles")
                .select { filter { eq("id", userId) } }
                .decodeAs<List<ProfileDto>>()
            val profile = profiles.firstOrNull() ?: currentProfile
            currentProfile = profile

            // 2. Fetch public.artisan_profiles
            val artisanProfiles = db.from("artisan_profiles")
                .select { filter { eq("user_id", userId) } }
                .decodeAs<List<ArtisanProfileDto>>()
            val artisan = artisanProfiles.firstOrNull() ?: currentArtisanProfile
            if (artisan != null) {
                currentArtisanProfileId = artisan.id
                currentArtisanProfile = artisan
            }

            Result.success(Pair(profile, artisan))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadArtisanProfilePhoto(
        artisanProfileId: String,
        imageBytes: ByteArray,
        filename: String = "avatar_${System.currentTimeMillis()}.jpg"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val storagePath = "$artisanProfileId/avatar/$filename"
            val bucket = db.storage.from("artisan-media")
            bucket.upload(storagePath, imageBytes, upsert = true)

            // 1. Update artisan_profiles table with profile_photo_path
            val existingArtisan = currentArtisanProfile ?: db.from("artisan_profiles")
                .select { filter { eq("id", artisanProfileId) } }
                .decodeAs<List<ArtisanProfileDto>>()
                .firstOrNull()

            if (existingArtisan != null) {
                val updatedArtisan = existingArtisan.copy(
                    profilePhotoPath = storagePath
                )
                db.from("artisan_profiles").upsert(updatedArtisan)
                currentArtisanProfile = updatedArtisan
            }

            // 2. Also update profiles table avatar_path
            val userId = currentUserId ?: existingArtisan?.userId
            if (!userId.isNullOrBlank()) {
                val existingProfile = currentProfile ?: db.from("profiles")
                    .select { filter { eq("id", userId) } }
                    .decodeAs<List<ProfileDto>>()
                    .firstOrNull()

                if (existingProfile != null) {
                    val updatedProfile = existingProfile.copy(
                        avatarPath = storagePath
                    )
                    db.from("profiles").upsert(updatedProfile)
                    currentProfile = updatedProfile
                }
            }

            Result.success(storagePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getProfilePhotoUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        val cleanPath = path.trimStart('/')
        return "${com.artknower.app.BuildConfig.SUPABASE_URL}/storage/v1/object/public/artisan-media/$cleanPath"
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            client.auth.signOut()
        } catch (_: Exception) {}
        currentUserId = null
        currentArtisanProfileId = null
        currentProfile = null
        currentArtisanProfile = null
    }
}
