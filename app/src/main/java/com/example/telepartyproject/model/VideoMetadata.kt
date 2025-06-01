package com.example.telepartyproject.model

import java.util.Locale

/**
 * Represents metadata for a video from a streaming service API.
 *
 * @property id Unique identifier of the video
 * @property contentId Content ID for DRM purposes
 * @property title Title of the video
 * @property description Description or summary of the video content
 * @property duration Duration of the video in seconds
 * @property licenseUrl URL for the DRM license server
 * @property drmScheme DRM scheme used (e.g., "Widevine")
 * @property thumbnailUrl URL to the video's thumbnail image
 */
data class VideoMetadata(
    val id: String,
    val contentId: String,
    val title: String,
    val description: String,
    val duration: Int,
    val licenseUrl: String,
    val drmScheme: String,
    val thumbnailUrl: String? = null
) {
    /**
     * Returns a formatted duration string in the format "H:MM:SS"
     */
    val formattedDuration: String
        get() {
            val hours = duration / 3600
            val minutes = (duration % 3600) / 60
            val seconds = duration % 60
            
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        }
} 