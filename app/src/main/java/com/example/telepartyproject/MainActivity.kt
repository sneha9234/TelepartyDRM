package com.example.telepartyproject

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.telepartyproject.ui.screens.MetadataScreen
import com.example.telepartyproject.ui.screens.PlayerScreen
import com.example.telepartyproject.ui.theme.TelepartyDRMTheme
import com.example.telepartyproject.viewmodel.MetadataViewModel
import com.example.telepartyproject.viewmodel.PlayerViewModel

/**
 * Single activity that hosts all screens using Compose Navigation.
 */
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Prevent screenshots for DRM content
        preventScreenCapture()
        
        // Enable hardware acceleration for better video playback
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        
        setContent {
            AppNavigation()
        }
    }
    
    @Composable
    private fun AppNavigation() {
        TelepartyDRMTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                val navController = rememberNavController()
                
                NavHost(
                    navController = navController,
                    startDestination = "player"
                ) {
                    composable("player") {
                        val playerViewModel = viewModel<PlayerViewModel>()
                        PlayerScreen(
                            viewModel = playerViewModel,
                            onNavigateToMetadata = {
                                navController.navigate("metadata")
                            }
                        )
                    }
                    
                    composable("metadata") {
                        val metadataViewModel = viewModel<MetadataViewModel>()
                        MetadataScreen(
                            viewModel = metadataViewModel,
                            onBackToPlayer = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Prevent screen capture for DRM protected content
     */
    private fun preventScreenCapture() {
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
    
    override fun onResume() {
        super.onResume()
    }
} 