package com.pixelsnap.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pixelsnap.app.MainViewModel
import com.pixelsnap.app.ui.camera.CameraScreen
import com.pixelsnap.app.ui.detail.SnapDetailScreen
import com.pixelsnap.app.ui.gallery.GalleryScreen

sealed class Screen(val route: String) {
    object Camera : Screen("camera")
    object Gallery : Screen("gallery")
    object Detail : Screen("detail/{snapId}") {
        fun createRoute(snapId: String) = "detail/$snapId"
    }
}

@Composable
fun PixelSnapNavGraph(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Camera.route
    ) {
        composable(Screen.Camera.route) {
            CameraScreen(
                viewModel = viewModel,
                onNavigateToGallery = {
                    navController.navigate(Screen.Gallery.route) {
                        popUpTo(Screen.Camera.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onSnapSaved = { snapId ->
                    // Navigate straight to detail after analyze/capture
                    navController.navigate(Screen.Detail.createRoute(snapId))
                }
            )
        }
        
        composable(Screen.Gallery.route) {
            GalleryScreen(
                viewModel = viewModel,
                onSnapClick = { snapId ->
                    navController.navigate(Screen.Detail.createRoute(snapId))
                },
                onNewSnapClick = {
                    navController.navigate(Screen.Camera.route) {
                        popUpTo(Screen.Gallery.route) { saveState = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("snapId") { 
                    type = NavType.StringType 
                }
            )
        ) { backStackEntry ->
            val snapId = backStackEntry.arguments?.getString("snapId") ?: return@composable
            SnapDetailScreen(
                snapId = snapId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onDeleted = {
                    navController.popBackStack(Screen.Gallery.route, inclusive = false)
                }
            )
        }
    }
}
