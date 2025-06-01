package com.example.telepartyproject.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.telepartyproject.model.VideoMetadata
import com.example.telepartyproject.service.VideoMetadataService
import kotlinx.coroutines.launch

/**
 * ViewModel that manages video metadata state.
 */
class MetadataViewModel(application: Application) : AndroidViewModel(application) {
    
    // Service to fetch metadata
    private var metadataService = VideoMetadataService(application)
    
    // LiveData for UI state
    private val _videoId = MutableLiveData<String>("dQw4w9WgXcQ") // Default video ID - Rick Astley
    val videoId: LiveData<String> = _videoId
    
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _metadata = MutableLiveData<VideoMetadata?>(null)
    val metadata: LiveData<VideoMetadata?> = _metadata
    
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error
    
    // Initialize with default video metadata on creation
    init {
        fetchMetadata()
    }
    
    /**
     * Set the metadata service (for testing)
     */
    fun setMetadataService(service: VideoMetadataService) {
        this.metadataService = service
    }
    
    /**
     * Updates the current video ID.
     */
    fun updateVideoId(newVideoId: String) {
        _videoId.value = newVideoId.trim()
    }
    
    /**
     * Fetches metadata for the current video ID.
     */
    fun fetchMetadata() {
        val currentVideoId = videoId.value ?: return
        
        if (currentVideoId.isEmpty()) {
            _error.value = "Please enter a valid video ID"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Try to fetch from API
                val result = metadataService.fetchVideoMetadata(currentVideoId)
                _metadata.value = result
            } catch (e: Exception) {
                // Handle the error case - no fallback to mock data
                _error.value = "Error: ${e.message}"
                _metadata.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Clears any error messages.
     */
    fun clearError() {
        _error.value = null
    }
} 