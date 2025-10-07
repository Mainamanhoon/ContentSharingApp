package com.psyfen.taskapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.psyfen.taskapplication.com.psyfen.taskapplication.screen.content_tiles.ContentTilesScreen
import com.psyfen.taskapplication.com.psyfen.taskapplication.screen.loginScreen.LoginScreen
import com.psyfen.taskapplication.com.psyfen.taskapplication.screen.loginScreen.LoginViewModel
import com.psyfen.taskapplication.com.psyfen.taskapplication.screen.mainScreen.MainScreen
import com.psyfen.taskapplication.screen.auth.AuthViewModel
import com.psyfen.taskapplication.screen.files.FileManagementScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
    object ContentTiles : Screen("content_tiles")
    object FileManagement : Screen("file_management")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: LoginViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()

    // Determine start destination based on auth state
    val startDestination = if (authState.isAuthenticated) {
        Screen.Main.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToContentTiles = {
                    navController.navigate(Screen.ContentTiles.route)
                },
                onNavigateToFiles = {
                    navController.navigate(Screen.FileManagement.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ContentTiles.route) {
            ContentTilesScreen()
        }

        composable(Screen.FileManagement.route) {
            FileManagementScreen()
        }
    }
}