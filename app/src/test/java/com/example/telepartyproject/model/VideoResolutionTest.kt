package com.example.telepartyproject.model

import org.junit.Assert.assertEquals
import org.junit.Test

class VideoResolutionTest {

    @Test
    fun `VideoResolution properties should be accessible`() {
        // Given
        val width = 1920
        val height = 1080
        val bitrate = 5000
        val trackIndex = 0
        
        // When
        val resolution = VideoResolution(
            width = width,
            height = height,
            bitrate = bitrate,
            trackIndex = trackIndex
        )
        
        // Then
        assertEquals(width, resolution.width)
        assertEquals(height, resolution.height)
        assertEquals(bitrate, resolution.bitrate)
        assertEquals(trackIndex, resolution.trackIndex)
    }
    
    @Test
    fun `VideoResolution should have correct string representation`() {
        // Given
        val resolutions = listOf(
            VideoResolution(1920, 1080, 5000000, 0),
            VideoResolution(1280, 720, 2500000, 1),
            VideoResolution(854, 480, 1000000, 2),
            VideoResolution(640, 360, 500000, 3),
            VideoResolution(426, 240, 250000, 4)
        )
        
        // Expected string formats: "widthxheight (bitrate kbps)"
        val expected = listOf(
            "1920x1080 (5000 kbps)",
            "1280x720 (2500 kbps)",
            "854x480 (1000 kbps)",
            "640x360 (500 kbps)",
            "426x240 (250 kbps)"
        )
        
        // Then
        resolutions.forEachIndexed { index, resolution ->
            assertEquals(expected[index], resolution.toString())
        }
    }
} 