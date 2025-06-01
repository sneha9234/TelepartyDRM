package com.example.telepartyproject.model

import org.junit.Assert.assertEquals
import org.junit.Test

class VideoMetadataTest {

    @Test
    fun `VideoMetadata properties should be accessible`() {
        // Given
        val id = "testId"
        val contentId = "test-content-id"
        val title = "Test Title"
        val description = "Test Description"
        val duration = 120
        val licenseUrl = "https://license.url"
        val drmScheme = "widevine"
        val thumbnailUrl = "https://thumbnail.url"
        
        // When
        val metadata = VideoMetadata(
            id = id,
            contentId = contentId,
            title = title,
            description = description,
            duration = duration,
            licenseUrl = licenseUrl,
            drmScheme = drmScheme,
            thumbnailUrl = thumbnailUrl
        )
        
        // Then
        assertEquals(id, metadata.id)
        assertEquals(contentId, metadata.contentId)
        assertEquals(title, metadata.title)
        assertEquals(description, metadata.description)
        assertEquals(duration, metadata.duration)
        assertEquals(licenseUrl, metadata.licenseUrl)
        assertEquals(drmScheme, metadata.drmScheme)
        assertEquals(thumbnailUrl, metadata.thumbnailUrl)
    }
    
    @Test
    fun `VideoMetadata should format duration correctly`() {
        // Given different durations
        val testCases = mapOf(
            3661 to "1:01:01",  // 1 hour, 1 min, 1 sec
            3600 to "1:00:00",  // 1 hour
            60 to "0:01:00",    // 1 minute
            61 to "0:01:01",    // 1 minute, 1 second
            30 to "0:00:30",    // 30 seconds
            0 to "0:00:00"      // 0 seconds
        )
        
        // Test each case
        testCases.forEach { (seconds, expected) ->
            val metadata = VideoMetadata(
                id = "test",
                contentId = "test-content",
                title = "Test",
                description = "Test",
                duration = seconds,
                licenseUrl = "",
                drmScheme = "",
                thumbnailUrl = ""
            )
            
            assertEquals("Duration formatting failed for $seconds seconds", 
                expected, metadata.formattedDuration)
        }
    }
} 