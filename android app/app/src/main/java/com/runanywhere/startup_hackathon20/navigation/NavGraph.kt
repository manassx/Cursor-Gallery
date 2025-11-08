package com.runanywhere.startup_hackathon20.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.runanywhere.startup_hackathon20.data.local.AppPreferences
import com.runanywhere.startup_hackathon20.ui.screens.*
import com.runanywhere.startup_hackathon20.viewmodel.GalleryViewModel
import com.runanywhere.startup_hackathon20.viewmodel.CloudGalleryViewModel

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object GalleryList : Screen("gallery_list")
    object CloudDashboard : Screen("cloud_dashboard")
    object Settings : Screen("settings")
    object CreateGallery : Screen("create_gallery")
    object GalleryDetail : Screen("gallery_detail/{galleryId}") {
        fun createRoute(galleryId: Long) = "gallery_detail/$galleryId"
    }
    object CloudGalleryDetail : Screen("cloud_gallery_detail/{galleryId}") {
        fun createRoute(galleryId: String) = "cloud_gallery_detail/$galleryId"
    }
    object Preview : Screen("preview/{galleryId}") {
        fun createRoute(galleryId: Long) = "preview/$galleryId"
    }
    object CloudPreview : Screen("cloud_preview/{galleryId}") {
        fun createRoute(galleryId: String) = "cloud_preview/$galleryId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    galleryViewModel: GalleryViewModel,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }
    val cloudViewModel: CloudGalleryViewModel = viewModel()

    // Determine start destination
    val startDestination = when {
        prefs.isFirstTime -> Screen.Welcome.route
        !prefs.isLoggedIn -> Screen.Login.route
        else -> Screen.CloudDashboard.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Welcome Screen
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onGetStarted = {
                    prefs.isFirstTime = false
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateBack = {
                    if (prefs.isFirstTime) {
                        navController.navigate(Screen.Welcome.route)
                    }
                },
                onLoginSuccess = { email, name ->
                    prefs.isLoggedIn = true
                    prefs.userEmail = email
                    prefs.userName = name
                    navController.navigate(Screen.CloudDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup.route)
                }
            )
        }

        // Signup Screen
        composable(Screen.Signup.route) {
            SignupScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignupSuccess = { email, name ->
                    prefs.isLoggedIn = true
                    prefs.userEmail = email
                    prefs.userName = name
                    navController.navigate(Screen.CloudDashboard.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Cloud Dashboard Screen (NEW - synced with web)
        composable(Screen.CloudDashboard.route) {
            CloudDashboardScreen(
                viewModel = cloudViewModel,
                onGalleryClick = { galleryId ->
                    navController.navigate(Screen.CloudGalleryDetail.createRoute(galleryId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // Cloud Gallery Detail Screen (NEW - with loading/caching)
        composable(
            route = Screen.CloudGalleryDetail.route,
            arguments = listOf(navArgument("galleryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val galleryId = backStackEntry.arguments?.getString("galleryId") ?: return@composable
            CloudGalleryDetailScreen(
                galleryId = galleryId,
                viewModel = cloudViewModel,
                onBackClick = { navController.popBackStack() },
                onPreviewClick = {
                    navController.navigate(Screen.CloudPreview.createRoute(galleryId))
                }
            )
        }

        // Cloud Preview Screen (NEW - cursor trail with cached images)
        composable(
            route = Screen.CloudPreview.route,
            arguments = listOf(navArgument("galleryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val galleryId = backStackEntry.arguments?.getString("galleryId") ?: return@composable
            CloudPreviewScreen(
                galleryId = galleryId,
                viewModel = cloudViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                userName = prefs.userName ?: "User",
                userEmail = prefs.userEmail ?: "",
                isDarkTheme = isDarkTheme,
                onThemeToggle = {
                    onThemeChange(!isDarkTheme)
                },
                onLogout = {
                    prefs.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.CloudDashboard.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // OLD LOCAL SCREENS (kept for backward compatibility if needed)

        // Gallery List Screen
        composable(Screen.GalleryList.route) {
            GalleryListScreen(
                viewModel = galleryViewModel,
                onNavigateToCreate = { navController.navigate(Screen.CreateGallery.route) },
                onNavigateToDetail = { galleryId ->
                    navController.navigate(Screen.GalleryDetail.createRoute(galleryId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // Create Gallery Screen
        composable(Screen.CreateGallery.route) {
            CreateGalleryScreen(
                viewModel = galleryViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { galleryId ->
                    navController.navigate(Screen.GalleryDetail.createRoute(galleryId)) {
                        popUpTo(Screen.GalleryList.route)
                    }
                }
            )
        }

        // Gallery Detail Screen
        composable(
            route = Screen.GalleryDetail.route,
            arguments = listOf(navArgument("galleryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val galleryId = backStackEntry.arguments?.getLong("galleryId") ?: return@composable
            GalleryDetailScreen(
                galleryId = galleryId,
                viewModel = galleryViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPreview = {
                    navController.navigate(Screen.Preview.createRoute(galleryId))
                }
            )
        }

        // Preview Screen
        composable(
            route = Screen.Preview.route,
            arguments = listOf(navArgument("galleryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val galleryId = backStackEntry.arguments?.getLong("galleryId") ?: return@composable
            PreviewScreen(
                galleryId = galleryId,
                viewModel = galleryViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
