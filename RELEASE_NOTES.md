#### SafeSteps: Official Release Notes 🚀

We are thrilled to announce the official release of **SafeSteps**, a comprehensive safety and emergency tracking application! This version incorporates all core features outlined in our development plan, with a focus on **speed**, **security**, and **reliability** in critical moments.

---

### ✨ Innovative Features Delivered

The following features, designed for enhanced safety and user experience, have been fully implemented in this release:

* **🆘 One-Tap Emergency Alert System:** The very first screen shown to the user is the alert screen, featuring a large one-tap button for instant SOS alerts. Pressing this immediately notifies all trusted contacts with the user's location.
* **✋ Biometric-Secured Access:** We've introduced biometric authentication (fingerprint/face recognition) to increase security. It acts as a verification step when logging into the app and is a cancellation process for cancelling an SOS alert.
* **☁️ Offline Mode with Automatic Sync:** The app supports offline functionality so that some features will still be available even without internet access. Information is stored locally and automatically syncs to Firebase once the device has access to the internet.
* **🔔 Real-Time Push Notifications:** The app includes a real-time notification system to keep users updated. Push notifications are sent instantly for SOS alerts to trusted contacts using Firebase Cloud Messaging.
* **👤 User Registration and Authentication:** Users can securely register and log in using Single Sign-On (SSO) via Firebase Authentication.
* **🤝 Trusted Contacts Management:** Users can add and remove contacts, view their trusted contacts, and see when they were last online.
* **📍 Interactive Map View (Location Tracking):** This screen is the main place to view your contact’s live location with live pins. The app uses the Google Maps API/Fused Location Provider for live GPS tracking.
* **🕰️ Alert History Timeline:** The Alert History screen provides a beautiful timeline view of all past emergency alerts.
* **🌍 Multi-Language Support:** To maximize accessibility, SafeSteps supports English, Afrikaans, and isiZulu. Your preferred language can be selected and changed in the Settings menu.
* **⚙️ Settings and Profile Management:** A full Settings page is available for managing personal and app preferences. Users can change information such as their name, phone number, and language preference.

---

### 🛠️ Updates and Enhancements

* **💻 Technical Foundation:** The app is built with **Kotlin** on the **MVVM Architecture**, utilizing **Firebase Firestore** as the real-time cloud database and **Google Location Services API** for accurate GPS tracking.
* **🧪 CI/CD Integration:** Automated testing and build workflows are integrated using **GitHub Actions**, which run automated tests and build the APK debug and release files. 🚨 SafeSteps: Official Release Notes 🚀

