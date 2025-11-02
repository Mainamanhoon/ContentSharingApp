# TaskStempedia

A modern Android application built with Jetpack Compose, featuring content management, file handling, and multimedia playback capabilities. The app follows Clean Architecture principles with a modular structure.

## 🚀 Features

- **Authentication System** - Secure user login and session management
- **Content Tiles** - Interactive content display with navigation to videos and web content
- **YouTube Integration** - Embedded YouTube video player
- **WebView Support** - In-app web browsing capabilities
- **File Management** - Upload, organize, and manage files
- **Firebase Integration** - Real-time data synchronization with Firestore
- **Modern UI** - Built with Jetpack Compose and Material Design 3

## 🏗️ Architecture

The project follows **Clean Architecture** principles with the following module structure:

```
📦 TaskApplication
├── 📁 app/                    # Main application module
├── 📁 domain/                 # Business logic and entities
├── 📁 data/                   # Data sources and repositories
├── 📁 common/                 # Shared utilities and components
└── 📁 build/                  # Build outputs
```

### Architecture Layers

- **Presentation Layer** (`app/`) - UI components, ViewModels, and navigation
- **Domain Layer** (`domain/`) - Business logic, use cases, and entities
- **Data Layer** (`data/`) - Repository implementations and data sources
- **Common Layer** (`common/`) - Shared utilities and reusable components

## 🛠️ Tech Stack

### Core Technologies
- **Kotlin** - Programming language
- **Jetpack Compose** - Modern UI toolkit
- **Material Design 3** - UI design system
- **Navigation Component** - App navigation

### Architecture & Dependency Injection
- **Clean Architecture** - Modular and testable architecture
- **Hilt** - Dependency injection framework
- **MVVM Pattern** - Architecture pattern with ViewModels

### Backend & Storage
- **Firebase Firestore** - Cloud database
- **Firebase Analytics** - App analytics
- **Google Services** - Google Play Services integration

### Media & UI Components
- **YouTube Player API** - Video playback
- **Lottie** - Animation library
- **Coil** - Image loading
- **CircleImageView** - Circular image components

### Development Tools
- **Gradle** - Build system
- **Version Catalog** - Dependency management
- **ProGuard** - Code obfuscation

## 📱 App Structure

### Main Screens
- **Splash Screen** - App initialization and authentication check
- **Login Screen** - User authentication
- **Main Screen** - Dashboard with navigation options
- **Content Tiles** - Interactive content grid
- **File Management** - File upload and organization
- **YouTube Player** - Video playback screen
- **WebView** - In-app browser

### Key Components
- **Authentication Flow** - Secure login/logout with state management
- **Navigation System** - Type-safe navigation with arguments
- **Repository Pattern** - Data abstraction layer
- **Use Cases** - Business logic encapsulation

## 🔧 Setup & Installation

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android SDK 24+ (API level 24)
- Firebase project setup

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd TaskStempedia
   ```

2. **Firebase Configuration**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Download `google-services.json` and place it in the `app/` directory
   - Enable Firestore Database and Analytics in your Firebase project

3. **Build Configuration**
   - Ensure `local.properties` contains your Android SDK path
   - Sync project with Gradle files

4. **Run the Application**
   ```bash
   ./gradlew assembleDebug
   ```
   Or use Android Studio's run configuration.

## 📋 Permissions

The app requires the following permissions:
- `INTERNET` - Network connectivity
- `ACCESS_NETWORK_STATE` - Network state monitoring
- `POST_NOTIFICATIONS` - Push notifications
- `READ_EXTERNAL_STORAGE` - File access (API ≤ 32)
- `READ_MEDIA_IMAGES` - Image file access (API 33+)
- `READ_MEDIA_VIDEO` - Video file access (API 33+)
- `READ_SMS` - SMS access (if applicable)

## 🔒 Security Features

- **Authentication State Management** - Secure session handling
- **ProGuard Rules** - Code obfuscation for release builds
- **Firebase Security Rules** - Database access control
- **Permission Management** - Runtime permission handling

## 📊 Build Variants

- **Debug** - Development build with debugging enabled
- **Release** - Production build with optimizations and obfuscation

## 🧪 Testing

The project includes test configurations for:
- **Unit Tests** - Business logic testing
- **Instrumented Tests** - UI and integration testing
- **Test Runners** - AndroidJUnitRunner for Android tests

## 📦 Dependencies

### Core Dependencies
- AndroidX Core KTX
- Lifecycle Runtime KTX
- Activity Compose
- Compose BOM
- Material Design 3

### Firebase
- Firebase BOM
- Firebase Analytics
- Firebase Firestore

### UI & Media
- Lottie Animations
- Coil Image Loading
- YouTube Player
- CircleImageView

### Architecture
- Hilt Dependency Injection
- Navigation Component
- Coroutines

## 🚀 Getting Started

1. **Authentication**: The app starts with a splash screen that checks authentication state
2. **Login**: New users will be directed to the login screen
3. **Main Dashboard**: Authenticated users access the main screen with navigation options
4. **Content Exploration**: Navigate through content tiles to access videos and web content
5. **File Management**: Upload and organize files through the dedicated file management screen

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support and questions, please open an issue in the repository or contact the development team.

---

**Built with ❤️ using Jetpack Compose and Clean Architecture**

