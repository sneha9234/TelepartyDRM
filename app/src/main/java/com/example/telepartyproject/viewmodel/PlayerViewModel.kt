package com.example.telepartyproject.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.example.telepartyproject.model.VideoResolution
import kotlinx.coroutines.launch

/**
 * ViewModel that manages the ExoPlayer instance and video playback state.
 */
@UnstableApi
class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val TAG = "PlayerViewModel"
    
    // DRM content URLs
    private val manifestUrl = "https://bitmovin-a.akamaihd.net/content/art-of-motion_drm/mpds/11331.mpd"
    private val licenseUrl = "https://cwip-shaka-proxy.appspot.com/no_auth"
    
    // ExoPlayer components
    private var player: ExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null
    
    // LiveData for UI state
    private val _videoResolutions = MutableLiveData<List<VideoResolution>>(emptyList())
    val videoResolutions: LiveData<List<VideoResolution>> = _videoResolutions
    
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error
    
    /**
     * Initializes the ExoPlayer and prepares it for playback.
     */
    fun initializePlayer(context: Context) {
        if (player != null) return
        
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Create track selector with default parameters
                trackSelector = DefaultTrackSelector(context).apply {
                    // Configure parameters to ensure video track selection works properly
                    setParameters(
                        buildUponParameters()
                            .setAllowVideoMixedMimeTypeAdaptiveness(true)
                            .setAllowVideoNonSeamlessAdaptiveness(true)
                            .setTunnelingEnabled(true)  // Enable tunneling for better performance
                    )
                }
                
                // Create ExoPlayer instance with video output enabled
                player = ExoPlayer.Builder(context)
                    .setTrackSelector(trackSelector!!)
                    .setHandleAudioBecomingNoisy(true)
                    .setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                    .build()
                
                // Set player event listeners
                player?.addListener(object : Player.Listener {
                    override fun onTracksChanged(tracks: Tracks) {
                        extractAvailableVideoResolutions(tracks)
                    }
                    
                    override fun onPlaybackStateChanged(state: Int) {
                        // Update loading state based on player state
                        _isLoading.value = state == Player.STATE_BUFFERING
                    }
                })
                
                // Prepare DRM content
                prepareDrmContent()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing player", e)
                _error.value = "Failed to initialize player: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Prepares the DRM-protected content for playback.
     */
    private fun prepareDrmContent() {
        try {
            // Create media item with DRM
            val mediaItem = MediaItem.Builder()
                .setUri(manifestUrl)
                .setDrmConfiguration(
                    MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                        .setLicenseUri(licenseUrl)
                        .build()
                )
                .build()
            
            // Prepare the player with the media item
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.playWhenReady = true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing DRM content", e)
            _error.value = "Error preparing DRM content: ${e.message}"
        }
    }
    
    /**
     * Extracts available video resolutions from the tracks.
     */
    private fun extractAvailableVideoResolutions(tracks: Tracks) {
        val resolutions = mutableListOf<VideoResolution>()
        
        tracks.groups.forEach { trackGroup ->
            if (trackGroup.type == C.TRACK_TYPE_VIDEO) {
                val mediaTrackGroup = trackGroup.mediaTrackGroup
                
                for (trackIndex in 0 until mediaTrackGroup.length) {
                    val format = mediaTrackGroup.getFormat(trackIndex)
                    
                    if (format.width > 0 && format.height > 0) {
                        resolutions.add(
                            VideoResolution(
                                width = format.width,
                                height = format.height,
                                bitrate = format.bitrate,
                                trackIndex = trackIndex
                            )
                        )
                    }
                }
            }
        }
        
        // Sort resolutions by height (ascending)
        resolutions.sortBy { it.height }
        
        _videoResolutions.value = resolutions
    }
    
    /**
     * Selects a specific video track based on the track index.
     */
    fun selectVideoTrack(trackIndex: Int) {
        try {
            val tracks = player?.currentTracks ?: return
            
            // Find the video track group
            for (i in 0 until tracks.groups.size) {
                val group = tracks.groups[i]
                if (group.type == C.TRACK_TYPE_VIDEO) {
                    val mediaTrackGroup = group.mediaTrackGroup
                    
                    Log.d(TAG, "Selecting video track: index=$trackIndex in group $i")
                    
                    // Create track selection parameters with the selected track
                    val parameters = trackSelector?.buildUponParameters()
                        ?.clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                        ?.setRendererDisabled(C.TRACK_TYPE_VIDEO, false)
                        ?.setTunnelingEnabled(true)
                        ?.build()
                    
                    // Apply the parameters
                    parameters?.let { trackSelector?.setParameters(it) }
                    
                    // Force selection of this track
                    player?.trackSelectionParameters = player?.trackSelectionParameters
                        ?.buildUpon()
                        ?.clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                        ?.setOverrideForType(
                            androidx.media3.common.TrackSelectionOverride(
                                mediaTrackGroup,
                                trackIndex
                            )
                        )
                        ?.build() ?: return
                    
                    Log.d(TAG, "Applied track selection for video track $trackIndex")
                    break
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error selecting video track", e)
            _error.value = "Error selecting video track: ${e.message}"
        }
    }
    
    /**
     * Returns the ExoPlayer instance to be used by the UI.
     */
    fun getPlayer(): ExoPlayer? = player
    
    /**
     * Releases player resources when no longer needed.
     */
    fun releasePlayer() {
        player?.release()
        player = null
        trackSelector = null
    }
    
    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
} 