

# SafeSteps - Personal Safety Application

## 📌 Overview
SafeSteps is an Android safety application that enables users to send emergency alerts to trusted contacts with a single tap.  
Built with **Firebase** and **Google Location Services APIs**.

## 🎥 Demo Video
[👉 Watch Full Demonstration](link-to-your-youtube-video)

---

## 🚀 Features Implemented (Part 2)

### ✅ Core Features
- **Single Sign-On Authentication** – Firebase Authentication with Google Sign-In  
- **Biometric Security** – Fingerprint/face recognition for app access  
- **Emergency Alert System** – One-tap SOS with GPS location tracking  
- **Trusted Contacts Management** – Add/remove emergency contacts  
- **Alert History** – View past emergency alerts  
- **Profile Editing** – Update user information  
- **Settings** – Language preferences and app configuration

### 🧠 Technical Implementation
- **Firebase Firestore** – Real-time cloud database for alerts and contacts  
- **Firebase Authentication** – Secure user authentication  
- **Google Location Services API** – GPS coordinate capture  
- **MVVM Architecture** – ViewModel and Repository pattern  
- **Material Design 3** – Modern, accessible UI

---

## 🌐 APIs Used
1. Firebase Firestore API – Data storage  
2. Firebase Authentication API – User management  
3. Google Location Services API – GPS tracking

---

## 🛠️ Technologies
| Component         | Technology                     |
|--------------------|---------------------------------|
| Language          | Kotlin                         |
| Platform          | Android (API 24+)              |
| Architecture      | MVVM                           |
| Database          | Firebase Firestore             |
| Authentication    | Firebase Auth                  |
| CI/CD            | GitHub Actions                 |

---

## 🧰 Setup Instructions

### 📋 Prerequisites
- Android Studio **Hedgehog** or newer  
- Android SDK 24+  
- Google account for Firebase  
- `google-services.json` file from Firebase Console

### 🪄 Installation
```bash
# 1. Clone the repository
git clone https://github.com/ST10079108/SafeSteps.git

# 2. Open the project in Android Studio

# 3. Add your google-services.json to the app/ directory

# 4. Sync Gradle and run on a device or emulator


app/
├── activities/       # UI Activities
├── viewmodels/       # ViewModels
├── repository/       # Data layer
├── models/          # Data classes
└── adapters/        # RecyclerView adapters

| Field            | Type          |
| ---------------- | ------------- |
| userId           | String        |
| latitude         | Double        |
| longitude        | Double        |
| timestamp        | Timestamp     |
| notifiedContacts | Array<String> |
| isActive         | Boolean       |

trustedContacts
Field	Type
userId	String
contactUserId	String
contactName	String
contactEmail	String
contactPhone	String
isActive	Boolean
users
Field	Type
firebaseUid	String
email	String
displayName	String
phoneNumber	String
🧪 Testing

Unit tests in app/src/test

GitHub Actions automated testing

See .github/workflows/build.yml for CI configuration

👥 Team Members
Name	Student Number	Features
Ilyaas	ST10263164	Alert system, Contacts, Settings
William	ST10079108	Authentication, Biometrics
Nomvuselelo	ST10264503	UI Design
Michel	ST10391174	Navigation, Testing
🧭 Part 3 Implementations (Planned)

Firebase Cloud Messaging for push notifications

Geofencing with entry/exit alerts

Google Maps integration

Offline mode with local caching

Multi-language support (Afrikaans, isiZulu)

📜 License

Academic project for The Independent Institute of Education.

🙏 Acknowledgments

Firebase Documentation

Android Developers Guide

Material Design Guidelines

