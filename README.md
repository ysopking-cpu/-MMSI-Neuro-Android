# NeuroStats

NeuroStats is a professional Android application designed for neuro-scientific EEG data ingestion, real-time monitoring, and analysis. It serves as a mobile, clinical-grade biofeedback tool based on the core logic previously developed in Python.

## Technical Architecture

This application leverages modern Android development best practices to ensure high performance, maintainability, and data security:

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose for declarative and reactive UI.
- **Design System**: Material Design 3 (M3) for a modern, accessible interface.
- **Data Persistence**: Room Database for secure, local storage of EEG data and analysis results, ensuring compliance with clinical standards.
- **Asynchronous Processing**: Kotlin Coroutines and Flow for handling real-time data streams efficiently.

## Build Instructions

This project uses the Gradle build system.

1. Clone the repository.
2. Ensure you have the Android SDK configured.
3. Build the application using:
   ```bash
   ./gradlew assembleDebug
   ```

## Validation & Testing

To maintain clinical-grade reliability, the application includes automated unit tests for core validation logic (e.g., cognitive load calculation, trajectory validation).

Run the tests using:
```bash
./gradlew testDebugUnitTest
```
