# Teleparty DRM Media Player

A sample Android application that demonstrates playing DRM-protected media content using ExoPlayer, with a modern MVVM architecture using Navigation Component and Jetpack Compose.

## Project Structure

This project uses a single activity architecture with the Navigation Component, following modern Android development practices:

- **Single Activity**: All UI is hosted in a single MainActivity
- **MVVM Architecture**: Uses ViewModels to separate business logic from UI
- **Navigation Component**: Used for navigating between fragments
- **Fragments**: PlayerFragment for video playback and MetadataComposeFragment for metadata display
- **Jetpack Compose**: Used for the metadata UI
- **ExoPlayer**: Used for DRM-protected video playback

## Architecture

The app follows the MVVM (Model-View-ViewModel) architecture pattern:

- **Models**: Data classes like `VideoResolution` and `VideoMetadata`, along with the `VideoMetadataService`
- **Views**: Fragments (`PlayerFragment`, `MetadataComposeFragment`) that observe and react to ViewModel state
- **ViewModels**: Manage business logic and expose state via LiveData (`PlayerViewModel`, `MetadataViewModel`)

This separation of concerns makes the code more maintainable, testable, and flexible.

## Features

- Play DRM-protected DASH media with Widevine
- Select different video resolutions dynamically
- View video metadata (title, description, release date, etc.)
- Modern UI with Material Design components
- Support for portrait and landscape orientations

## Code Organization

The code is well-organized with proper documentation:

- **PlayerFragment**: UI for video playback
- **PlayerViewModel**: Manages ExoPlayer state and DRM session management
- **MetadataComposeFragment**: UI for displaying metadata using Jetpack Compose
- **MetadataViewModel**: Manages metadata fetching and state
- **VideoResolution**: Data class representing available video resolutions
- **VideoMetadata**: Data class representing video metadata
- **VideoMetadataService**: Service for fetching and parsing video metadata

## Implementation Details

- Uses Widevine DRM for protected content playback
- Implements resolution selection through ExoPlayer's track selection API
- Features a clean single-activity architecture with Navigation Component
- Demonstrates the use of ViewBinding for traditional Views and Jetpack Compose for modern UI
- Leverages ViewModels and LiveData for reactive UI updates

## Testing

The project includes both unit tests and instrumentation tests:

### Unit Tests
- **VideoMetadataServiceTest**: Tests the video metadata service's mock data generation
- **VideoResolutionTest**: Tests the string formatting of video resolution objects

To run unit tests:
```
./gradlew test
```

## Package Name

The package name `com.example.telepartyproject` follows the standard naming convention for example or demo projects:

1. **Domain Prefix**: 'com.example' is the standard prefix for example applications
2. **Project Name**: 'telepartyproject' describes the specific project

This package name is appropriate for development, testing, and educational purposes. For a production app, a more specific package name based on the actual organization's domain would be used. 
