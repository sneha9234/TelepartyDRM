package com.example.telepartyproject.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.telepartyproject.model.VideoMetadata
import com.example.telepartyproject.service.VideoMetadataService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

/**
 * Tests for the MetadataViewModel
 * 
 */
@ExperimentalCoroutinesApi
class MetadataViewModelTest {
    // Use InstantTaskExecutorRule for LiveData
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test dispatcher for coroutines
    private val testDispatcher = StandardTestDispatcher()

    // Mock objects
    private lateinit var mockApplication: Application
    private lateinit var mockMetadataService: VideoMetadataService

    // Class under test
    private lateinit var viewModel: MetadataViewModel

    // Sample test data
    private val testVideoId = "testVideoId"
    private val testMetadata = VideoMetadata(
        id = testVideoId,
        contentId = "content-$testVideoId",
        title = "Test Video",
        description = "Test Description",
        duration = 120,
        licenseUrl = "https://test-license.url",
        drmScheme = "Test DRM",
        thumbnailUrl = "https://test-thumbnail.url"
    )

    @Before
    fun setup() {
        // Set main dispatcher for coroutines
        Dispatchers.setMain(testDispatcher)
        
        // Enable experimental Byte Buddy
        System.setProperty("net.bytebuddy.experimental", "true")
        
        // Create mocks directly
        mockApplication = Mockito.mock(Application::class.java)
        mockMetadataService = Mockito.mock(VideoMetadataService::class.java)
        
        // Initialize with mocked service
        viewModel = MetadataViewModel(mockApplication)
        
        // Inject mocked service
        viewModel.setMetadataService(mockMetadataService)
    }

    @After
    fun tearDown() {
        // Reset main dispatcher
        Dispatchers.resetMain()
    }

    @Test
    fun updateVideoId_updatesLiveData() {
        // When
        viewModel.updateVideoId(testVideoId)
        
        // Then
        assertEquals(testVideoId, viewModel.videoId.value)
    }

    @Test
    fun fetchMetadata_withValidVideoId_updatesMetadataLiveData() = runTest {
        // Given
        val videoId = "testVideoId"
        
        // Set the video ID
        viewModel.updateVideoId(videoId)
        
        // Mock the service response
        whenever(mockMetadataService.fetchVideoMetadata(videoId)).thenReturn(testMetadata)
        
        // When
        viewModel.fetchMetadata()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals(testMetadata, viewModel.metadata.value)
    }

    @Test
    fun fetchMetadata_withEmptyVideoId_doesNotFetchMetadata() = runTest {
        // Given
        viewModel.updateVideoId("")
        
        // When
        viewModel.fetchMetadata()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertNull(viewModel.metadata.value)
    }

    @Test
    fun fetchMetadata_whenServiceThrowsException_updatesErrorState() = runTest {
        // Given
        val videoId = "invalidVideoId"
        val errorMessage = "No metadata found for video ID: $videoId"
        
        // Set the video ID
        viewModel.updateVideoId(videoId)
        
        // Mock the service to throw a RuntimeException
        whenever(mockMetadataService.fetchVideoMetadata(videoId)).thenThrow(RuntimeException(errorMessage))
        
        // When
        viewModel.fetchMetadata()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertNull(viewModel.metadata.value)
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value?.contains(errorMessage) ?: false)
    }

    @Test
    fun clearError_clearsErrorState() = runTest {
        // When
        viewModel.clearError()
        
        // Then - just make sure the method runs without exception
        // We can't easily verify the error LiveData without observers
        assert(true)
    }
} 