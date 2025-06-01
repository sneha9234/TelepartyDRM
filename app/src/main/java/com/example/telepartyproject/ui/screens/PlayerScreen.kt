package com.example.telepartyproject.ui.screens

import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.telepartyproject.viewmodel.PlayerViewModel

@UnstableApi
@Composable
fun PlayerScreen(
    onNavigateToMetadata: () -> Unit,
    viewModel: PlayerViewModel = viewModel()
) {
    val tag = "PlayerScreen"
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Observe view model state
    val resolutions by viewModel.videoResolutions.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState(null)
    
    var selectedResolutionIndex by remember { mutableStateOf(-1) }
    var showResolutionDropdown by remember { mutableStateOf(false) }
    
    // Handle player lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    viewModel.initializePlayer(context)
                }
                Lifecycle.Event.ON_STOP -> {
                    viewModel.releasePlayer()
                }
                else -> {}
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Show error as Toast
    error?.let { errorMessage ->
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        Log.e(tag, "Error: $errorMessage")
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Player view takes most of the space
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // ExoPlayer view wrapped in AndroidView
            AndroidView(
                factory = { ctx ->
                    // Create a FrameLayout to properly size the PlayerView
                    FrameLayout(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        
                        // Create and add the PlayerView to the FrameLayout
                        val playerView = PlayerView(ctx).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            
                            // Set player when ready
                            player = viewModel.getPlayer()
                            
                            // Use surface view for video display - better for performance
                            useController = true
                            setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                            
                            // Set resize mode to fit video in the view
                            resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }
                        
                        // Add player view to the frame layout
                        addView(playerView)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { frameLayout ->
                    // Find the PlayerView in the FrameLayout
                    val playerView = frameLayout.getChildAt(0) as? PlayerView
                    
                    // Update player if necessary
                    playerView?.player = viewModel.getPlayer()
                    
                    // Show loading animation as needed
                    playerView?.setShowBuffering(
                        if (isLoading) PlayerView.SHOW_BUFFERING_ALWAYS 
                        else PlayerView.SHOW_BUFFERING_WHEN_PLAYING
                    )
                }
            )
            
            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        
        // Resolution selector
        if (resolutions.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedButton(
                    onClick = { showResolutionDropdown = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (selectedResolutionIndex >= 0 && selectedResolutionIndex < resolutions.size) 
                            "Resolution: ${resolutions[selectedResolutionIndex]}" 
                        else 
                            "Select Resolution"
                    )
                }
                
                DropdownMenu(
                    expanded = showResolutionDropdown,
                    onDismissRequest = { showResolutionDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    resolutions.forEachIndexed { index, resolution ->
                        DropdownMenuItem(onClick = {
                            selectedResolutionIndex = index
                            showResolutionDropdown = false
                            
                            // Apply the selected resolution
                            viewModel.selectVideoTrack(resolution.trackIndex)
                            
                            // Show feedback
                            Toast.makeText(
                                context,
                                "Switching to $resolution",
                                Toast.LENGTH_SHORT
                            ).show()
                        }) {
                            Text(text = resolution.toString())
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Metadata button
        Button(
            onClick = onNavigateToMetadata,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
        ) {
            Text(text = "Video Metadata")
        }
    }
} 