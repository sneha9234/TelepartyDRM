package com.example.telepartyproject.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.telepartyproject.model.VideoMetadata
import com.example.telepartyproject.ui.theme.TelepartyDRMTheme
import com.example.telepartyproject.viewmodel.MetadataViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MetadataScreen(
    onBackToPlayer: () -> Unit,
    viewModel: MetadataViewModel = viewModel()
) {
    val tag = "MetadataScreen"
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Observe ViewModel state
    val metadata by viewModel.metadata.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState(null)
    val currentVideoId by viewModel.videoId.observeAsState("")
    
    // Local state for input field that syncs with currentVideoId
    var videoIdInput by remember(currentVideoId) { mutableStateOf(currentVideoId) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Video Metadata") },
                navigationIcon = {
                    IconButton(onClick = onBackToPlayer) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to player"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Video ID input and search
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Enter YouTube Video ID",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = videoIdInput,
                            onValueChange = { 
                                videoIdInput = it
                                // Clear error when user starts typing
                                if (error != null) {
                                    viewModel.clearError()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text("Video ID") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    keyboardController?.hide()
                                    if (videoIdInput.isNotBlank()) {
                                        viewModel.updateVideoId(videoIdInput)
                                        viewModel.fetchMetadata()
                                    }
                                }
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                if (videoIdInput.isNotBlank()) {
                                    viewModel.updateVideoId(videoIdInput)
                                    viewModel.fetchMetadata()
                                }
                            },
                            enabled = !isLoading && videoIdInput.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Example IDs: dQw4w9WgXcQ, 9bZkp7q19f0, jNQXAC9IVRw",
                        style = MaterialTheme.typography.caption
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Content area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Show loading indicator
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    // Show metadata if available
                    metadata?.let { videoMetadata ->
                        MetadataContent(
                            metadata = videoMetadata,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        )
                    } ?: run {
                        // If there's an error, show error state
                        if (error != null) {
                            ErrorContent(
                                errorMessage = error ?: "Unknown error occurred",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Show instructions if no metadata available and no error
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Enter a video ID and tap search to see metadata",
                                    style = MaterialTheme.typography.body1
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Button to return to player
            Button(
                onClick = onBackToPlayer,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Back to Player")
            }
        }
    }
}

@Composable
private fun ErrorContent(
    errorMessage: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = MaterialTheme.colors.error,
            modifier = Modifier.height(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Could not load video metadata",
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.error,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Please try a different video ID",
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MetadataContent(
    metadata: VideoMetadata,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = metadata.title,
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Description:",
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = metadata.description,
                style = MaterialTheme.typography.body1
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            MetadataRow("Content ID", metadata.contentId)
            MetadataRow("Duration", "${metadata.duration} seconds (${metadata.formattedDuration})")
            MetadataRow("DRM Scheme", metadata.drmScheme)
            MetadataRow("License URL", metadata.licenseUrl)
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.subtitle2,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.body2
        )
    }
    
    Spacer(modifier = Modifier.height(8.dp))
}

// Sample data for previews
private val sampleMetadata = VideoMetadata(
    id = "dQw4w9WgXcQ",
    contentId = "youtube-dQw4w9WgXcQ",
    title = "Rick Astley - Never Gonna Give You Up",
    description = "The official video for Never Gonna Give You Up by Rick Astley. The song was a global number 1 in 1987 and was the first of Rick's 8 consecutive UK chart toppers.",
    duration = 212,
    licenseUrl = "https://www.youtube.com/api/timedtext?v=dQw4w9WgXcQ",
    drmScheme = "YouTube Standard",
    thumbnailUrl = "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg"
)

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun MetadataScreenPreview() {
    TelepartyDRMTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Video Metadata") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back to player"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Video ID input and search
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Enter YouTube Video ID",
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = "dQw4w9WgXcQ",
                                onValueChange = {},
                                modifier = Modifier.weight(1f),
                                label = { Text("Video ID") },
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Button(onClick = {}) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Example IDs: dQw4w9WgXcQ, 9bZkp7q19f0, jNQXAC9IVRw",
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Content area with metadata
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    MetadataContent(
                        metadata = sampleMetadata,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Button to return to player
                Button(
                    onClick = {},
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Back to Player")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MetadataContentPreview() {
    TelepartyDRMTheme {
        MetadataContent(
            metadata = sampleMetadata,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorContentPreview() {
    TelepartyDRMTheme {
        ErrorContent(
            errorMessage = "No metadata found for video ID: invalid123",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MetadataRowPreview() {
    TelepartyDRMTheme {
        MetadataRow(
            label = "Duration", 
            value = "212 seconds (0:03:32)"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingStatePreview() {
    TelepartyDRMTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    TelepartyDRMTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Enter a video ID and tap search to see metadata",
                style = MaterialTheme.typography.body1
            )
        }
    }
} 