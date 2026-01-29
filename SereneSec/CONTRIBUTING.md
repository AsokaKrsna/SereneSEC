# Contributing to SereneSec

Thank you for your interest in contributing to SereneSec! This document provides guidelines and information for contributors.

## Code of Conduct

- Be respectful and inclusive
- Focus on constructive feedback
- Help maintain a welcoming environment

## How to Contribute

### Reporting Bugs

1. Check existing issues to avoid duplicates
2. Use the bug report template
3. Include:
   - Android version
   - Device model
   - Steps to reproduce
   - Expected vs actual behavior
   - Screenshots if applicable

### Suggesting Features

1. Check existing feature requests
2. Describe the problem you're trying to solve
3. Explain your proposed solution
4. Consider how it fits the project philosophy

### Pull Requests

1. Fork the repository
2. Create a feature branch from `main`
3. Follow the coding style
4. Write meaningful commit messages
5. Test your changes
6. Submit PR with clear description

## Development Setup

### Prerequisites

- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 35

### Building

```bash
git clone https://github.com/yourusername/serenesec.git
cd serenesec
./gradlew assembleDebug
```

### Project Structure

```
app/src/main/java/com/serenesec/
├── data/           # Data layer (Room, network, preferences)
├── di/             # Hilt dependency injection modules
├── domain/         # Domain models and use cases
├── service/        # Background services
├── ui/             # Jetpack Compose UI screens
├── util/           # Utility classes
├── widget/         # Home screen widget
└── worker/         # WorkManager workers
```

## Coding Style

- Follow Kotlin official style guide
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Keep functions small and focused
- Prefer immutability

## Architecture

SereneSec follows Clean Architecture with MVVM:

- **UI Layer**: Jetpack Compose screens + ViewModels
- **Domain Layer**: Use cases and domain models
- **Data Layer**: Repositories, DAOs, network clients

## Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## Questions?

Open a discussion or issue on GitHub.

---

Thank you for contributing! 🙏
