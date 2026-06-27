package com.pixelsnap.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.pixelsnap.app.ui.navigation.PixelSnapNavGraph
import com.pixelsnap.app.ui.theme.PixelSnapTheme

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
                    PixelSnapApp()
                }
            }
        }
    }
}

@Composable
fun PixelSnapApp() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    
    PixelSnapNavGraph(
        navController = navController,
        viewModel = viewModel
    )
}
