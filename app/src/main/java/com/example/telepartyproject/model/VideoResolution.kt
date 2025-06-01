package com.example.telepartyproject.model

/**
 * Represents a video resolution option from the DASH manifest.
 *
 * @property width Width of the video in pixels
 * @property height Height of the video in pixels
 * @property bitrate Bitrate of the video in bits per second
 * @property trackIndex Index of the track in the Media3 track group
 */
data class VideoResolution(
    val width: Int,
    val height: Int,
    val bitrate: Int,
    val trackIndex: Int
) {
    /**
     * Returns a string representation of the resolution in the format "Width x Height (Bitrate kbps)"
     */
    override fun toString(): String {
        return "${width}x${height} (${bitrate / 1000} kbps)"
    }
} 