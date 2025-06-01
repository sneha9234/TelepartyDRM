# Testing Strategy for Teleparty DRM Player

This document outlines the testing approach used in the Teleparty DRM Player application.

## Test Structure

The test suite is organized into the following categories:

### Unit Tests (JVM-based)

Located in `app/src/test/`, these tests run on the JVM without requiring an Android device or emulator:

- **Model Tests**: Test data classes and their functionality
  - `VideoMetadataTest`: Tests the `VideoMetadata` model class properties and formatted duration calculation
  - `VideoResolutionTest`: Tests the `VideoResolution` model class properties and string representation

### Instrumentation Tests (Device Required)

Located in `app/src/androidTest/`, these tests run on an Android device or emulator:

- **UI Tests**: Test the user interface components
  - `MetadataScreenTest`: Tests the Compose UI for the metadata screen
  - Other UI tests specific to app functionality

## Test Utilities

- `MockitoRule`: A custom JUnit rule to properly initialize Mockito for Kotlin tests

## Running Tests

### Unit Tests

```bash
./gradlew test
```

### Instrumentation Tests

```bash
./gradlew connectedAndroidTest
```

### Specific Test Classes

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.telepartyproject.model.*"
```

## Test Dependencies

The app includes the following test dependencies:

- JUnit for writing tests
- Mockito for mocking dependencies
- Architecture Components Testing utilities
- Coroutines Test for testing asynchronous code
- Compose UI Testing libraries for testing Compose-based UI

## Future Test Improvements

Future test development should focus on:

1. Expanding ViewModel tests with proper mocks of repository and service layers
2. Adding integration tests for the player functionality
3. Implementing more comprehensive UI tests for all screens
4. Adding end-to-end tests for complete user flows 