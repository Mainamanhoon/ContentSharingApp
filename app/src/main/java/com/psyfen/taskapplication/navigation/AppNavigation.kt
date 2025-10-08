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
import com.psyfen.taskapplication.com.psyfen.taskapplication.webViewScreen.WebViewScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

//sealed class Screen(val route: String) {
//    object Splash : Screen("splash")
//    object Login : Screen("login")
//    object Main : Screen("main")
//    object ContentTiles : Screen("content_tiles")
//    object FileManagement : Screen("file_management")
//}
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
    Log.d("AppNavigation", "NavController created")

    val authViewModel: AuthViewModel = hiltViewModel()
    Log.d("AppNavigation", "AuthViewModel injected")

    val authState by authViewModel.authState.collectAsState()
    Log.d("AppNavigation", "AuthState collected: isAuthenticated=${authState.isAuthenticated}, user=${authState.user?.uid}")

    // Navigate based on auth state - FIXED VERSION
    LaunchedEffect(authState.isAuthenticated) {
        Log.d("AppNavigation", "LaunchedEffect triggered: isAuthenticated=${authState.isAuthenticated}")

        val currentRoute = navController.currentDestination?.route
        Log.d("AppNavigation", "Current route: $currentRoute")

        if (authState.isAuthenticated) {
            Log.d("AppNavigation", "User authenticated, navigating to Main")
            if (currentRoute != Screen.Main.route) {
                navController.navigate(Screen.Main.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        } else {
            Log.d("AppNavigation", "User NOT authenticated, navigating to Login")
            // Navigate to login if we're on splash or not on login already
            if (currentRoute == Screen.Splash.route || currentRoute == null) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }
    }

    Log.d("AppNavigation", "Creating NavHost with startDestination=${Screen.Splash.route}")

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
                    Log.d("AppNavigation", "Login success, navigating to Main")
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
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
                    Log.d("AppNavigation", "Logout, navigating to Login")
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ContentTiles.route) {
            ContentTilesScreen(
                onNavigateToWebView = { url, title ->
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

            WebViewScreen(
                url = url,
                title = title,
                onBackPressed = { navController.popBackStack() }
            )
        }
    }

    Log.d("AppNavigation", "=== NavHost setup complete ===")
}

@Composable
fun SplashScreen() {
    Log.d("SplashScreen", "SplashScreen composing")
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFFfd511e)
        )
    }
    Log.d("SplashScreen", "SplashScreen composed")
}