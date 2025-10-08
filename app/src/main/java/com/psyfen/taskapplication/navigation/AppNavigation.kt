package com.psyfen.taskapplication.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.psyfen.taskapplication.com.psyfen.taskapplication.screen.auth.AuthViewModel
import com.psyfen.taskapplication.com.psyfen.taskapplication.screen.content_tiles.ContentTilesScreen
import com.psyfen.taskapplication.com.psyfen.taskapplication.screen.fileManagementScreen.FileManagementScreen
import com.psyfen.taskapplication.com.psyfen.taskapplication.screen.loginScreen.LoginScreen
import com.psyfen.taskapplication.com.psyfen.taskapplication.screen.mainScreen.MainScreen
import com.psyfen.taskapplication.com.psyfen.taskapplication.screen.webViewScreen.WebViewScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Main : Screen("main")
    object ContentTiles : Screen("content_tiles")
    object FileManagement : Screen("file_management")
    object WebView : Screen("webview/{url}/{title}") {
        fun createRoute(url: String, title: String): String {
            val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
            val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
            return "webview/$encodedUrl/$encodedTitle"
        }
    }
}

@Composable
fun AppNavigation() {
    Log.d("AppNavigation", "=== AppNavigation composable called ===")

    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()

    // Safe navigation based on auth state
    LaunchedEffect(authState.isAuthenticated) {
        val currentRoute = navController.currentDestination?.route
        Log.d("AppNavigation", "Auth state changed: isAuthenticated=${authState.isAuthenticated}, currentRoute=$currentRoute")

        // Only navigate if we're in a state that needs navigation
        when {
            authState.isAuthenticated && currentRoute == Screen.Splash.route -> {
                // User is authenticated and on splash, go to main
                Log.d("AppNavigation", "Navigating from Splash to Main")
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
            authState.isAuthenticated && currentRoute == Screen.Login.route -> {
                // User just logged in, go to main
                Log.d("AppNavigation", "Navigating from Login to Main")
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
            !authState.isAuthenticated && currentRoute == Screen.Splash.route -> {
                // User is not authenticated and on splash, go to login
                Log.d("AppNavigation", "Navigating from Splash to Login")
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
            !authState.isAuthenticated && currentRoute != Screen.Login.route && currentRoute != Screen.Splash.route -> {
                // User logged out, go to login
                Log.d("AppNavigation", "User logged out, navigating to Login")
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            Log.d("AppNavigation", "Rendering Splash screen")
            SplashScreen()
        }

        composable(Screen.Login.route) {
            Log.d("AppNavigation", "Rendering Login screen")
            LoginScreen(
                onLoginSuccess = {
                    Log.d("AppNavigation", "Login success")
                    // Navigation handled by LaunchedEffect
                }
            )
        }

        composable(Screen.Main.route) {
            Log.d("AppNavigation", "Rendering Main screen")
            MainScreen(
                onNavigateToContentTiles = {
                    Log.d("AppNavigation", "Navigating to ContentTiles")
                    navController.navigate(Screen.ContentTiles.route)
                },
                onNavigateToFiles = {
                    Log.d("AppNavigation", "Navigating to FileManagement")
                    navController.navigate(Screen.FileManagement.route)
                },
                onLogout = {
                    Log.d("AppNavigation", "Logout triggered")
                    // Just call logout, navigation handled by LaunchedEffect
                    authViewModel.logout()
                }
            )
        }

        composable(Screen.ContentTiles.route) {
            Log.d("AppNavigation", "Rendering ContentTiles screen")
            ContentTilesScreen(
                onNavigateToWebView = { url, title ->
                    Log.d("AppNavigation", "Navigating to WebView")
                    navController.navigate(Screen.WebView.createRoute(url, title))
                }
            )
        }

        composable(Screen.FileManagement.route) {
            Log.d("AppNavigation", "Rendering FileManagement screen")
            FileManagementScreen()
        }

        composable(
            route = Screen.WebView.route,
            arguments = listOf(
                navArgument("url") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
            val encodedTitle = backStackEntry.arguments?.getString("title") ?: ""

            val url = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
            val title = URLDecoder.decode(encodedTitle, StandardCharsets.UTF_8.toString())

            Log.d("AppNavigation", "Rendering WebView: $url")
            WebViewScreen(
                url = url,
                title = title,
                onBackPressed = {
                    Log.d("AppNavigation", "WebView back pressed")
                    navController.popBackStack()
                }
            )
        }
    }

    Log.d("AppNavigation", "=== NavHost setup complete ===")
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFFfd511e)
        )
    }
}