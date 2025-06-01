package com.example.telepartyproject.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@ExperimentalCoroutinesApi
class VideoMetadataServiceTest {
    
    // No need to mock Context, we'll just test the methods that don't depend on it
    private val testDispatcher = StandardTestDispatcher()
    
    /**
     * Tests for getMockVideoMetadata which doesn't require Context
     */
    @Test
    fun getMockVideoMetadata_returnsExpectedMetadata() {
        // Create an instance with null context - we know getMockVideoMetadata doesn't use it
        val videoMetadataService = VideoMetadataService(null)
        
        // Given
        val videoId = "dQw4w9WgXcQ"
        
        // When
        val result = videoMetadataService.getMockVideoMetadata(videoId)
        
        // Then
        assertEquals(videoId, result.id)
        assertEquals("youtube-$videoId", result.contentId)
        assertEquals("Rick Astley - Never Gonna Give You Up", result.title)
        assertEquals(212, result.duration)
        assertEquals("https://i.ytimg.com/vi/$videoId/hqdefault.jpg", result.thumbnailUrl)
    }
    
    @Test
    fun getMockVideoMetadata_gangnamStyle_returnsCorrectData() {
        // Create an instance with null context - we know getMockVideoMetadata doesn't use it
        val videoMetadataService = VideoMetadataService(null)
        
        // Given
        val videoId = "9bZkp7q19f0"
        
        // When
        val result = videoMetadataService.getMockVideoMetadata(videoId)
        
        // Then
        assertEquals(videoId, result.id)
        assertEquals("youtube-$videoId", result.contentId)
        assertEquals("PSY - GANGNAM STYLE", result.title)
        assertEquals(253, result.duration)
    }
    
    @Test
    fun getMockVideoMetadata_firstYouTubeVideo_returnsCorrectData() {
        // Create an instance with null context - we know getMockVideoMetadata doesn't use it
        val videoMetadataService = VideoMetadataService(null)
        
        // Given
        val videoId = "jNQXAC9IVRw"
        
        // When
        val result = videoMetadataService.getMockVideoMetadata(videoId)
        
        // Then
        assertEquals(videoId, result.id)
        assertEquals("Me at the zoo", result.title)
        assertEquals(19, result.duration)
    }
    
    @Test
    fun getMockVideoMetadata_unknownId_returnsFallbackData() {
        // Create an instance with null context - we know getMockVideoMetadata doesn't use it
        val videoMetadataService = VideoMetadataService(null)
        
        // Given
        val videoId = "unknown123"
        
        // When
        val result = videoMetadataService.getMockVideoMetadata(videoId)
        
        // Then
        assertEquals(videoId, result.id)
        assertEquals("Sample Video Title", result.title)
        assertEquals(120, result.duration)
    }
    
    @Test
    fun parseDuration_correctlyParsesPT1H30M15S() = runTest(testDispatcher) {
        // Create an instance with null context - we know parseDuration doesn't use it
        val videoMetadataService = VideoMetadataService(null)
        
        // Given - use reflection to access private method
        val durationStr = "1H30M15S"
        val parseDurationMethod = VideoMetadataService::class.java.getDeclaredMethod(
            "parseDuration",
            String::class.java
        )
        parseDurationMethod.isAccessible = true
        
        // When
        val result = parseDurationMethod.invoke(videoMetadataService, durationStr) as Int
        
        // Then
        assertEquals(5415, result) // 1h (3600s) + 30m (1800s) + 15s = 5415s
    }
    
    @Test
    fun parseDuration_correctlyParsesPT10M30S() = runTest(testDispatcher) {
        // Create an instance with null context - we know parseDuration doesn't use it
        val videoMetadataService = VideoMetadataService(null)
        
        // Given - use reflection to access private method
        val durationStr = "10M30S"
        val parseDurationMethod = VideoMetadataService::class.java.getDeclaredMethod(
            "parseDuration",
            String::class.java
        )
        parseDurationMethod.isAccessible = true
        
        // When
        val result = parseDurationMethod.invoke(videoMetadataService, durationStr) as Int
        
        // Then
        assertEquals(630, result) // 10m (600s) + 30s = 630s
    }
    
    @Test
    fun parseDuration_correctlyParsesPT30S() = runTest(testDispatcher) {
        // Create an instance with null context - we know parseDuration doesn't use it
        val videoMetadataService = VideoMetadataService(null)
        
        // Given - use reflection to access private method
        val durationStr = "30S"
        val parseDurationMethod = VideoMetadataService::class.java.getDeclaredMethod(
            "parseDuration",
            String::class.java
        )
        parseDurationMethod.isAccessible = true
        
        // When
        val result = parseDurationMethod.invoke(videoMetadataService, durationStr) as Int
        
        // Then
        assertEquals(30, result) // 30s = 30s
    }
} 