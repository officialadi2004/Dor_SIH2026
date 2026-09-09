package com.artknower.app.data.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ProfileDto(
    @SerialName("id")
    val id: String,
    @EncodeDefault
    @SerialName("role")
    val role: String = "artisan",
    @SerialName("full_name")
    val fullName: String? = null,
    @SerialName("phone")
    val phone: String? = null,
    @EncodeDefault
    @SerialName("preferred_language")
    val preferredLanguage: String = "en",
    @SerialName("avatar_path")
    val avatarPath: String? = null,
    @EncodeDefault
    @SerialName("is_active")
    val isActive: Boolean = true
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ArtisanProfileDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("display_name")
    val displayName: String,
    @SerialName("location_city")
    val locationCity: String? = null,
    @SerialName("location_state")
    val locationState: String? = null,
    @EncodeDefault
    @SerialName("location_country")
    val locationCountry: String = "India",
    @SerialName("craft_summary")
    val craftSummary: String? = null,
    @SerialName("experience_years")
    val experienceYears: Int? = null,
    @SerialName("profile_photo_path")
    val profilePhotoPath: String? = null,
    @EncodeDefault
    @SerialName("is_published")
    val isPublished: Boolean = true
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class BusinessProfileDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("business_name")
    val businessName: String,
    @SerialName("registration_number")
    val registrationNumber: String? = null,
    @SerialName("contact_person_name")
    val contactPersonName: String? = null,
    @SerialName("contact_phone")
    val contactPhone: String? = null,
    @SerialName("contact_email")
    val contactEmail: String? = null,
    @SerialName("industry")
    val industry: String? = null,
    @SerialName("location_city")
    val locationCity: String? = null,
    @SerialName("location_state")
    val locationState: String? = null,
    @EncodeDefault
    @SerialName("location_country")
    val locationCountry: String = "India",
    @SerialName("business_description")
    val businessDescription: String? = null,
    @EncodeDefault
    @SerialName("is_active")
    val isActive: Boolean = true
)
