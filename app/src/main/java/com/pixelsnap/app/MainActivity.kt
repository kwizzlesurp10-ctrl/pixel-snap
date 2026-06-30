package com.pixelsnap.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pixelsnap.app.ui.navigation.PixelSnapNavGraph
import com.pixelsnap.app.ui.theme.PixelSnapTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PixelSnapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PixelSnapRoot()
                }
            }
        }
    }
}

@Composable
fun PixelSnapRoot() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            val showBottomBar = currentDestination?.route in listOf(
                com.pixelsnap.app.ui.navigation.Screen.Camera.route,
                com.pixelsnap.app.ui.navigation.Screen.Gallery.route,
                com.pixelsnap.app.ui.navigation.Screen.Memories.route,
                com.pixelsnap.app.ui.navigation.Screen.Studio.route
            )
            
            if (showBottomBar) {
                NavigationBar {
                    val items = listOf(
                        Pair(com.pixelsnap.app.ui.navigation.Screen.Camera, Pair("Camera", Icons.Default.CameraAlt)),
                        Pair(com.pixelsnap.app.ui.navigation.Screen.Gallery, Pair("Gallery", Icons.Default.PhotoLibrary)),
                        Pair(com.pixelsnap.app.ui.navigation.Screen.Memories, Pair("Memories", Icons.Default.AutoAwesome)),
                        Pair(com.pixelsnap.app.ui.navigation.Screen.Studio, Pair("Studio", Icons.Default.Brush))
                    )
                    
                    items.forEach { item ->
                        val screen = item.first
                        val label = item.second.first
                        val icon = item.second.second
                        
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = currentDestination?.route == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(com.pixelsnap.app.ui.navigation.Screen.Camera.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        PixelSnapNavGraph(
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
