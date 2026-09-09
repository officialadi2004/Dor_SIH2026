package com.artknower.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImageVariantDto(
    @SerialName("id")
    val id: String = "enhanced",
    @SerialName("filename")
    val filename: String = "enhanced.png",
    @SerialName("url")
    val url: String = ""
)

@Serializable
data class EnhanceResponseDto(
    @SerialName("success")
    val success: Boolean = true,
    @SerialName("status")
    val status: String? = null,
    @SerialName("image")
    val image: ImageVariantDto? = null,
    @SerialName("images")
    val images: List<ImageVariantDto> = emptyList()
) {
    val allImages: List<ImageVariantDto>
        get() {
            if (image != null && image.url.isNotBlank()) {
                val derivedFilename = if (image.filename.isNotBlank() && image.filename != "enhanced.png") {
                    image.filename
                } else {
                    image.url.substringAfterLast('/', "enhanced.png").substringBefore('?')
                }
                return listOf(image.copy(filename = derivedFilename))
            }
            return images
        }
}

data class EnhancedImageItem(
    val id: String,
    val filename: String,
    val storagePath: String,
    val displayUrl: String,
    var isKept: Boolean = true
)
