package com.artknower.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import com.artknower.app.data.MockDataSource
import com.artknower.app.data.repository.AuthRepository
import com.artknower.app.data.repository.ProductRepository
import com.artknower.app.data.repository.ProductStats
import com.artknower.app.ui.components.*
import androidx.compose.ui.res.stringResource
import com.artknower.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTab: (HomeTab) -> Unit,
    onTellStoryClick: () -> Unit,
    onEnquiriesClick: () -> Unit,
    onAddProductClick: () -> Unit,
    onMarketingClick: () -> Unit = {},
    onCopilotFeatureClick: (routeKey: String, title: String) -> Unit,
    onProfileClick: () -> Unit
) {
    val defaultWelcome = stringResource(R.string.welcome)
    var artisanProfile by remember { mutableStateOf(AuthRepository.currentArtisanProfile) }
    var userProfile by remember { mutableStateOf(AuthRepository.currentProfile) }
    var profilePhotoUrl by remember {
        mutableStateOf(
            AuthRepository.getProfilePhotoUrl(
                AuthRepository.currentArtisanProfile?.profilePhotoPath
                    ?: AuthRepository.currentProfile?.avatarPath
            )
        )
    }

    val artisanName = artisanProfile?.displayName
        ?: userProfile?.fullName
        ?: defaultWelcome
    var liveStats by remember { mutableStateOf<ProductStats?>(null) }

    LaunchedEffect(AuthRepository.currentArtisanProfileId, AuthRepository.currentUserId) {
        // Fetch fresh profile and photo from database
        AuthRepository.refreshCurrentProfile().onSuccess { (profile, artisan) ->
            userProfile = profile
            artisanProfile = artisan
            val path = artisan?.profilePhotoPath ?: profile?.avatarPath
            profilePhotoUrl = AuthRepository.getProfilePhotoUrl(path)
        }

        val profileId = AuthRepository.currentArtisanProfileId
        if (!profileId.isNullOrBlank()) {
            ProductRepository.getProductStats(profileId).onSuccess { stats ->
                liveStats = stats
            }
        }
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(
                currentTab = HomeTab.HOME,
                onTabSelected = onNavigateToTab
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Top Header (Greeting + Verified Badge + Notifications)
            HomeHeader(
                artisanName = artisanName,
                profilePhotoUrl = profilePhotoUrl,
                onProfileClick = onProfileClick
            )

            // 2. Hero Feature Banner ("Turn Your Craft into Opportunity")
            HeroBannerCard(
                onActionClick = onAddProductClick
            )

            // 3. Horizontal Category / Action Pills
            QuickActionsSection(
                onTellStoryClick = onTellStoryClick,
                onEnquiriesClick = onEnquiriesClick,
                onMarketingClick = onMarketingClick
            )

            // 4. Artisan Studio & Resource Banner
            CopilotSection(
                onFeatureClick = { routeKey, title ->
                    if (routeKey == "story_form") {
                        onTellStoryClick()
                    } else if (routeKey == "marketing_help") {
                        onMarketingClick()
                    } else {
                        onCopilotFeatureClick(routeKey, title)
                    }
                }
            )

            // 6. Business Insights Placeholder Section
            BusinessInsightsSection()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
