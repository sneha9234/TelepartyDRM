package com.example.telepartyproject.service

import android.content.Context
import android.util.Log
import com.example.telepartyproject.BuildConfig
import com.example.telepartyproject.model.VideoMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Service to fetch video metadata from API or mock data.
 */
class VideoMetadataService(private val context: Context?) {
    
    companion object {
        private const val TAG = "VideoMetadataService"
        private const val BASE_URL = "https://www.googleapis.com/youtube/v3/videos"
    }

    /**
     * Fetches video metadata from YouTube API
     *
     * @param videoId The YouTube video ID
     * @return VideoMetadata object or null if fetching fails
     * @throws RuntimeException if metadata cannot be found or an error occurs
     */
    suspend fun fetchVideoMetadata(videoId: String): VideoMetadata? {
        // If context is null (in tests), return mock data
        if (context == null) {
            return getMockVideoMetadata(videoId)
        }
        
        val apiKey = BuildConfig.YOUTUBE_API_KEY
        
        // Print the API key for debugging (only first/last few chars for security)
        val keyStart = apiKey.take(5)
        val keyEnd = apiKey.takeLast(5)
        Log.d(TAG, "Using API key: $keyStart...$keyEnd (length: ${apiKey.length})")
        
        return withContext(Dispatchers.IO) {
            try {
                // Construct the URL directly to avoid any issues
                val urlString = "$BASE_URL?id=$videoId&key=$apiKey&part=snippet,contentDetails"
                Log.d(TAG, "Full URL: $urlString")
                
                val url = URL(urlString)
                
                // Set up the connection
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                
                // Try to connect
                Log.d(TAG, "Connecting to API...")
                connection.connect()
                
                val responseCode = connection.responseCode
                Log.d(TAG, "API response code: $responseCode")
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Connection successful, read the response
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()
                    
                    val responseStr = response.toString()
                    Log.d(TAG, "API response preview: ${responseStr.take(500)}...")
                    
                    // Try to parse the response
                    val metadata = parseYouTubeResponse(responseStr, videoId)
                    if (metadata != null) {
                        Log.d(TAG, "Successfully parsed metadata: ${metadata.title}")
                        return@withContext metadata
                    } else {
                        Log.e(TAG, "Failed to parse metadata")
                        // Let's print the full response to debug parsing issues
                        Log.e(TAG, "Full response: $responseStr")
                        throw RuntimeException("No metadata found for video ID: $videoId")
                    }
                } else {
                    // Connection failed with error code
                    var errorMessage = ""
                    try {
                        val errorStream = connection.errorStream
                        if (errorStream != null) {
                            val reader = BufferedReader(InputStreamReader(errorStream))
                            val error = StringBuilder()
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                error.append(line)
                            }
                            reader.close()
                            errorMessage = error.toString()
                            Log.e(TAG, "Error response: $errorMessage")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error reading error stream", e)
                    }
                    
                    val errorMsg = "Error fetching video metadata: HTTP $responseCode - $errorMessage"
                    Log.e(TAG, errorMsg)
                    throw RuntimeException(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception fetching video metadata", e)
                throw RuntimeException("Failed to fetch metadata: ${e.message}", e)
            }
        }
    }
    
    /**
     * Parses the YouTube API response to extract video metadata.
     *
     * @param response The JSON response from the YouTube API
     * @param videoId The video ID
     * @return VideoMetadata object or null if parsing fails
     */
    private fun parseYouTubeResponse(response: String, videoId: String): VideoMetadata? {
        return try {
            Log.d(TAG, "Parsing YouTube response...")
            val jsonObject = JSONObject(response)
            
            // Check if the "items" array exists
            if (!jsonObject.has("items")) {
                Log.e(TAG, "Response does not contain 'items' array")
                return null
            }
            
            val items = jsonObject.getJSONArray("items")
            Log.d(TAG, "Found ${items.length()} items in response")
            
            if (items.length() > 0) {
                val item = items.getJSONObject(0)
                
                // Check if required objects exist
                if (!item.has("snippet") || !item.has("contentDetails")) {
                    Log.e(TAG, "Item missing required objects: snippet or contentDetails")
                    return null
                }
                
                val snippet = item.getJSONObject("snippet")
                val contentDetails = item.getJSONObject("contentDetails")
                
                // Extract basic fields with error checking
                val title = if (snippet.has("title")) snippet.getString("title") else "Unknown Title"
                Log.d(TAG, "Parsed title: $title")
                
                val description = if (snippet.has("description")) snippet.getString("description") else "No description available"
                
                // Extract and parse duration
                var duration = 0
                if (contentDetails.has("duration")) {
                    val durationStr = contentDetails.getString("duration").replace("PT", "")
                    duration = parseDuration(durationStr)
                    Log.d(TAG, "Parsed duration: $duration seconds from $durationStr")
                } else {
                    Log.e(TAG, "No duration field found")
                }
                
                // Extract thumbnail URL
                var thumbnailUrl = ""
                if (snippet.has("thumbnails")) {
                    val thumbnails = snippet.getJSONObject("thumbnails")
                    thumbnailUrl = if (thumbnails.has("high")) {
                        thumbnails.getJSONObject("high").getString("url")
                    } else if (thumbnails.has("default")) {
                        thumbnails.getJSONObject("default").getString("url")
                    } else {
                        "https://i.ytimg.com/vi/$videoId/hqdefault.jpg" // Fallback
                    }
                    Log.d(TAG, "Parsed thumbnail URL: $thumbnailUrl")
                } else {
                    Log.e(TAG, "No thumbnails field found")
                    thumbnailUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg" // Fallback
                }
                
                // Create and return the metadata object
                VideoMetadata(
                    id = videoId,
                    contentId = "youtube-$videoId",
                    title = title,
                    description = description,
                    duration = duration,
                    licenseUrl = "https://www.youtube.com/api/timedtext?v=$videoId",
                    drmScheme = "YouTube Standard",
                    thumbnailUrl = thumbnailUrl
                )
            } else {
                Log.e(TAG, "No items found in YouTube API response")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing YouTube API response", e)
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Parses ISO 8601 duration string to seconds.
     * Example: "PT1H30M15S" -> 5415 seconds (1 hour, 30 minutes, 15 seconds)
     */
    private fun parseDuration(duration: String): Int {
        var seconds = 0
        var numStr = ""
        
        for (c in duration) {
            when (c) {
                'H' -> {
                    seconds += (numStr.toIntOrNull() ?: 0) * 3600
                    numStr = ""
                }
                'M' -> {
                    seconds += (numStr.toIntOrNull() ?: 0) * 60
                    numStr = ""
                }
                'S' -> {
                    seconds += numStr.toIntOrNull() ?: 0
                    numStr = ""
                }
                else -> {
                    if (c.isDigit()) {
                        numStr += c
                    }
                }
            }
        }
        
        return seconds
    }
    
    /**
     * Returns mock video metadata for testing purposes.
     *
     * @param videoId The YouTube video ID to create mock data for
     * @return A mock VideoMetadata object
     */
    fun getMockVideoMetadata(videoId: String): VideoMetadata {
        // We don't use Log here in case the context is null in tests
        // Different mock data based on video ID
        return when (videoId) {
            "dQw4w9WgXcQ" -> VideoMetadata(
                id = videoId,
                contentId = "youtube-$videoId",
                title = "Rick Astley - Never Gonna Give You Up",
                description = "The official video for Never Gonna Give You Up by Rick Astley. The song was a global number 1 in 1987 and was the first of Rick's 8 consecutive UK chart toppers.",
                duration = 212,
                licenseUrl = "https://www.youtube.com/api/timedtext?v=$videoId",
                drmScheme = "YouTube Standard",
                thumbnailUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
            )
            "9bZkp7q19f0" -> VideoMetadata(
                id = videoId,
                contentId = "youtube-$videoId",
                title = "PSY - GANGNAM STYLE",
                description = "Official music video for PSY - GANGNAM STYLE. This global hit became a cultural phenomenon and is one of the most-watched videos on YouTube.",
                duration = 253,
                licenseUrl = "https://www.youtube.com/api/timedtext?v=$videoId",
                drmScheme = "YouTube Standard",
                thumbnailUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
            )
            "jNQXAC9IVRw" -> VideoMetadata(
                id = videoId,
                contentId = "youtube-$videoId",
                title = "Me at the zoo",
                description = "The first video uploaded to YouTube, featuring YouTube co-founder Jawed Karim at the San Diego Zoo.",
                duration = 19,
                licenseUrl = "https://www.youtube.com/api/timedtext?v=$videoId",
                drmScheme = "YouTube Standard",
                thumbnailUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
            )
            else -> VideoMetadata(
                id = videoId,
                contentId = "youtube-$videoId",
                title = "Sample Video Title",
                description = "This is a mock description for video $videoId. In a real app, this would be fetched from the YouTube API.",
                duration = 120,
                licenseUrl = "https://www.youtube.com/api/timedtext?v=$videoId",
                drmScheme = "YouTube Standard",
                thumbnailUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
            )
        }
    }
} 