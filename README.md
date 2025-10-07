
# SafeSteps - Personal Safety Application



## Overview
SafeSteps is an Android safety application that enables users to send emergency alerts to trusted contacts with a single tap. Built with Firebase and Google Location Services APIs.

## Demo Video
[Watch Full Demonstration](link-to-your-youtube-video)

## Features Implemented (Part 2)

### Core Features
- **Single Sign-On Authentication**: Firebase Authentication with Google Sign-In
- **Biometric Security**: Fingerprint/face recognition for app access
- **Emergency Alert System**: One-tap SOS with GPS location tracking
- **Trusted Contacts Management**: Add/remove emergency contacts
- **Alert History**: View past emergency alerts
- **Profile Editing**: Update user information
- **Settings**: Language preferences and app configuration

### Technical Implementation
- **Firebase Firestore**: Real-time cloud database for alerts and contacts
- **Firebase Authentication**: Secure user authentication
- **Google Location Services API**: GPS coordinate capture
- **MVVM Architecture**: ViewModel and Repository pattern
- **Material Design 3**: Modern, accessible UI

## APIs Used
1. Firebase Firestore API - Data storage
2. Firebase Authentication API - User management
3. Google Location Services API - GPS tracking

## Technologies
- **Language**: Kotlin
- **Platform**: Android (API 24+)
- **Architecture**: MVVM
- **Database**: Firebase Firestore
- **Authentication**: Firebase Auth
- **CI/CD**: GitHub Actions

## Setup Instructions

### Prerequisites
- Android Studio Hedgehog or newer
- Android SDK 24+
- Google account for Firebase
- `google-services.json` file from Firebase Console

### Installation
1. Clone repository
```bash
git clone https://github.com/ST10079108/SafeSteps.git
Open in Android Studio
Add google-services.json to app/ directory
Sync Gradle
Run on device or emulator

app/
├── activities/       # UI Activities
├── viewmodels/       # ViewModels
├── repository/       # Data layer
├── models/          # Data classes
└── adapters/        # RecyclerView adapters

Firebase Collections
emergencyAlerts

userId: String
latitude: Double
longitude: Double
timestamp: Timestamp
notifiedContacts: Array<String>
isActive: Boolean

trustedContacts

userId: String
contactUserId: String
contactName: String
contactEmail: String
contactPhone: String
isActive: Boolean

users

firebaseUid: String
email: String
displayName: String
phoneNumber: String

Testing

Unit tests in app/src/test
GitHub Actions automated testing
See .github/workflows/build.yml

Team Members

[Ilyaas] - ST10263164 (Features: Alert system, Contacts, Settings)
[William] - ST10079108 (Features: Authentication, Biometrics)
[Nomvuselelo] - ST10264503 (Features: UI Design)
[Michel] - ST10391174 (Features: Navigation, Testing)


Part 3 Implementations
Firebase Cloud Messaging for push notifications
Geofencing with entry/exit alerts
Google Maps integration
Offline mode with local caching
Multi-language support (Afrikaans, isiZulu)

License
Academic project for The Independent Institute of Education
Acknowledgments

Firebase Documentation
Android Developers Guide
Material Design Guidelines



