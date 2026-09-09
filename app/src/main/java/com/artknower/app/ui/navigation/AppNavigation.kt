package com.artknower.app.ui.navigation

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.artknower.app.ui.components.HomeTab
import com.artknower.app.ui.screens.*

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val HOME = "home"
    const val STORY_FORM = "story_form"
    const val ARTICLE_PREVIEW = "article_preview"
    const val PRODUCTS = "products"
    const val COPILOT = "copilot"
    const val PROFILE = "profile"
    const val ADD_PRODUCT = "add_product"
    const val FEATURE_PLACEHOLDER = "feature_placeholder"
    const val ORDERS = "orders"
    const val ENQUIRIES = "enquiries"
    const val MARKETING_HUB = "marketing_hub"
    const val MARKET_PRODUCT = "market_product"
    const val MARKETING_REVIEW = "marketing_review"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn(animationSpec = tween(350)) + slideInHorizontally(initialOffsetX = { 200 }) },
        exitTransition = { fadeOut(animationSpec = tween(350)) + slideOutHorizontally(targetOffsetX = { -200 }) },
        popEnterTransition = { fadeIn(animationSpec = tween(350)) + slideInHorizontally(initialOffsetX = { -200 }) },
        popExitTransition = { fadeOut(animationSpec = tween(350)) + slideOutHorizontally(targetOffsetX = { 200 }) }
    ) {
        // 0. Splash Screen
        composable(Routes.SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // 1. Login & Sign Up Welcome Screen
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // 2. Home Screen
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToTab = { tab ->
                    when (tab) {
                        HomeTab.HOME -> { /* Already on Home */ }
                        HomeTab.PRODUCTS -> navController.navigate(Routes.PRODUCTS)
                        HomeTab.ADD -> navController.navigate(Routes.ADD_PRODUCT)
                        HomeTab.MARKETING -> navController.navigate(Routes.MARKETING_HUB)
                        HomeTab.ORDERS -> navController.navigate(Routes.ORDERS)
                    }
                },
                onTellStoryClick = {
                    navController.navigate(Routes.STORY_FORM)
                },
                onEnquiriesClick = {
                    navController.navigate(Routes.ENQUIRIES)
                },
                onAddProductClick = {
                    navController.navigate(Routes.ADD_PRODUCT)
                },
                onMarketingClick = {
                    navController.navigate(Routes.MARKETING_HUB)
                },
                onCopilotFeatureClick = { routeKey, title ->
                    if (routeKey == "marketing_help") {
                        navController.navigate(Routes.MARKETING_HUB)
                    } else {
                        val encTitle = Uri.encode(title)
                        val encDesc = Uri.encode("The '$title' AI copilot feature is coming soon. It will help optimize your craft presentation, pricing, and marketing.")
                        navController.navigate("${Routes.FEATURE_PLACEHOLDER}/$encTitle/$encDesc")
                    }
                },
                onProfileClick = {
                    navController.navigate(Routes.PROFILE)
                }
            )
        }

        // 3. Story Form Screen
        composable(Routes.STORY_FORM) {
            ArtisanStoryFormScreen(
                onBack = { navController.popBackStack() },
                onStorySubmitted = { name, location, craftType, storyText, imageUri ->
                    val encName = Uri.encode(name.ifEmpty { " " })
                    val encLocation = Uri.encode(location.ifEmpty { " " })
                    val encCraft = Uri.encode(craftType.ifEmpty { " " })
                    val encStory = Uri.encode(storyText.ifEmpty { " " })
                    val encImg = Uri.encode(imageUri?.toString()?.ifEmpty { " " } ?: " ")
                    
                    navController.navigate("${Routes.ARTICLE_PREVIEW}/$encName/$encLocation/$encCraft/$encStory/$encImg")
                }
            )
        }

        // 4. Article Preview Screen
        composable("${Routes.ARTICLE_PREVIEW}/{name}/{location}/{craft}/{story}/{imageUri}") { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val location = backStackEntry.arguments?.getString("location") ?: ""
            val craft = backStackEntry.arguments?.getString("craft") ?: ""
            val story = backStackEntry.arguments?.getString("story") ?: ""
            val imageUriString = backStackEntry.arguments?.getString("imageUri") ?: ""
            
            val imageUri = if (imageUriString.isNotEmpty()) Uri.parse(imageUriString) else null
            
            ArticlePreviewScreen(
                artisanName = name,
                location = location,
                craftType = craft,
                storyText = story,
                imageUri = imageUri,
                onBack = { navController.popBackStack() },
                onDone = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        // 5. Products Screen
        composable(Routes.PRODUCTS) {
            ProductsScreen(
                onNavigateToTab = { tab ->
                    if (tab == HomeTab.HOME) {
                        navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } }
                    } else if (tab != HomeTab.PRODUCTS) {
                        navController.navigate(tab.route)
                    }
                },
                onAddProductClick = {
                    navController.navigate(Routes.ADD_PRODUCT)
                },
                onMarketProductClick = { _ ->
                    navController.navigate(Routes.MARKET_PRODUCT)
                }
            )
        }

        // 6. Copilot Hub Screen
        composable(Routes.COPILOT) {
            PlaceholderScreen(
                title = "Artisan Studio",
                description = "Access tools and resources in one dedicated hub.",
                onBack = { navController.popBackStack() }
            )
        }

        // 7. Orders Screen
        composable(Routes.ORDERS) {
            OrdersScreen(
                onNavigateToTab = { tab ->
                    if (tab == HomeTab.HOME) {
                        navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } }
                    } else if (tab != HomeTab.ORDERS) {
                        navController.navigate(tab.route)
                    }
                }
            )
        }

        // 8. Enquiries Screen
        composable(Routes.ENQUIRIES) {
            EnquiriesScreen(
                onNavigateToTab = { tab ->
                    if (tab == HomeTab.HOME) {
                        navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } }
                    } else {
                        navController.navigate(tab.route)
                    }
                }
            )
        }

        // 9. Profile Screen
        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onNavigateToTab = { tab ->
                    if (tab == HomeTab.HOME) {
                        navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } }
                    } else {
                        navController.navigate(tab.route)
                    }
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 10. Add Product Screen
        composable(Routes.ADD_PRODUCT) {
            AddProductScreen(
                onBack = { navController.popBackStack() },
                onProductSaved = {
                    navController.navigate(Routes.PRODUCTS) {
                        popUpTo(Routes.ADD_PRODUCT) { inclusive = true }
                    }
                }
            )
        }

        // 11. Marketing Automation Hub Screen
        composable(Routes.MARKETING_HUB) {
            MarketingHubScreen(
                onBack = { navController.popBackStack() },
                onNavigateToTab = { tab ->
                    if (tab == HomeTab.HOME) {
                        navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } }
                    } else if (tab != HomeTab.MARKETING) {
                        navController.navigate(tab.route)
                    }
                },
                onMarketNewProductClick = {
                    navController.navigate(Routes.MARKET_PRODUCT)
                },
                onContentClick = { marketingContentId ->
                    navController.navigate("${Routes.MARKETING_REVIEW}/$marketingContentId")
                }
            )
        }

        // 12. Market Product Selection Screen
        composable(Routes.MARKET_PRODUCT) {
            MarketProductSelectionScreen(
                onBack = { navController.popBackStack() },
                onNavigateToReview = { marketingContentId ->
                    navController.navigate("${Routes.MARKETING_REVIEW}/$marketingContentId") {
                        popUpTo(Routes.MARKET_PRODUCT) { inclusive = true }
                    }
                }
            )
        }

        // 13. Marketing Review & Publish Screen
        composable(
            route = "${Routes.MARKETING_REVIEW}/{contentId}",
            arguments = listOf(navArgument("contentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val contentId = backStackEntry.arguments?.getString("contentId") ?: ""
            MarketingReviewScreen(
                marketingContentId = contentId,
                onBack = { navController.popBackStack() },
                onDone = {
                    navController.navigate(Routes.MARKETING_HUB) {
                        popUpTo(Routes.MARKETING_HUB) { inclusive = true }
                    }
                }
            )
        }

        composable("${Routes.FEATURE_PLACEHOLDER}/{title}/{desc}") { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: "Copilot Feature"
            val desc = backStackEntry.arguments?.getString("desc") ?: "Feature under development."
            
            PlaceholderScreen(
                title = title,
                description = desc,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
