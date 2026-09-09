package com.artknower.app.data

data class ArtisanProfile(
    var name: String = "Meera Devi",
    var craft: String = "Terracotta & Pottery",
    var location: String = "Bankura, West Bengal",
    var completionPercentage: Int = 70
)

data class BusinessStats(
    val totalProducts: Int = 12,
    val drafts: Int = 3,
    val published: Int = 9,
    val isDemoData: Boolean = true
)

object MockDataSource {
    var currentArtisan = ArtisanProfile()
    val stats = BusinessStats()

    fun updateProfile(name: String, craft: String, location: String) {
        currentArtisan = ArtisanProfile(
            name = name.ifBlank { "Meera Devi" },
            craft = craft.ifBlank { "Terracotta & Pottery" },
            location = location.ifBlank { "Bankura, West Bengal" },
            completionPercentage = 85
        )
    }
}

