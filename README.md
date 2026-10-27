# 💊 MediReminder — Smart Medicine Reminder App

> Never miss a dose again. MediReminder helps you manage medications, track adherence, and keep caregivers in the loop.

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com)
[![Java](https://img.shields.io/badge/Language-Java-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 🌟 Features

| Feature | Description |
|---------|-------------|
| 🔐 **Firebase Auth** | Secure login & registration with email/password, password reset support |
| 💊 **Medicine Management** | Add medicines with dose quantity, frequency, stock count & expiry date |
| ⏰ **Smart Reminders** | Set multiple daily reminder times with exact alarm scheduling |
| 📊 **Adherence Tracking** | Real-time adherence score (taken vs missed doses) |
| 📦 **Stock Alerts** | Automatic low-stock and out-of-stock warnings |
| 📅 **Calendar History** | Visual calendar with day-by-day dose history |
| 👨‍👩‍👧 **Caregiver Mode** | Share your user code with family members to monitor doses remotely |
| 🔔 **Foreground Alarm Service** | Reliable notifications even when the app is closed |
| 🎨 **Beautiful UI** | Clean, modern green-themed design with gradient backgrounds |

---

## 🏗️ Project Structure

```
MediReminder3/
├── app/src/main/
│   ├── java/com/example/medireminder3/
│   │   ├── SplashActivity.java        # Animated splash screen
│   │   ├── LoginActivity.java         # Firebase auth (login/register/forgot)
│   │   ├── MainActivity.java          # Dashboard — medicine list & adherence
│   │   ├── AddMedicineActivity.java   # Add medicine with time picker
│   │   ├── CalendarActivity.java      # Calendar view with dose history
│   │   ├── CaregiverActivity.java     # Family caregiver monitoring
│   │   ├── Medicine.java              # Medicine data model
│   │   ├── MedicineAdapter.java       # ListView adapter for medicines
│   │   ├── DBHelper.java              # SQLite database operations
│   │   ├── DoseLogStore.java          # Dose log persistence
│   │   ├── AlarmHelper.java           # Alarm scheduling utilities
│   │   ├── AlarmService.java          # Foreground notification service
│   │   ├── ReminderReceiver.java      # Broadcast receiver for alarms
│   │   └── AlertHelper.java           # Custom toast/alert utilities
│   ├── res/
│   │   ├── layout/                    # XML layouts for all activities
│   │   ├── drawable/                  # Custom backgrounds & icons
│   │   └── values/                    # Colors, strings, themes
│   └── AndroidManifest.xml
├── build.gradle.kts                   # Project-level build config
└── gradle/                            # Gradle wrapper
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Arctic Fox or later)
- **JDK 11+**
- **Android SDK 33+** (API Level 33)
- A **Firebase project** with Authentication enabled

### Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/vaibhavraj082007/MediReminder3.git
   cd MediReminder3
   ```

2. **Firebase Setup:**
   - Go to [Firebase Console](https://console.firebase.google.com)
   - Create a new project (or use existing)
   - Add an Android app with package name: `com.example.medireminder3`
   - Download `google-services.json` and place it in `app/`
   - Enable **Email/Password Authentication** in Firebase Auth

3. **Open in Android Studio:**
   - Open Android Studio → File → Open → Select the project folder
   - Let Gradle sync complete
   - Connect an Android device or start an emulator

4. **Run the app:**
   - Click ▶️ Run or press `Shift + F10`

---

## 📱 App Flow

```
Splash Screen → Login/Register → Dashboard (Medicine List)
                                      ├── Add Medicine (with time pickers)
                                      ├── Calendar History
                                      ├── Caregiver Monitoring
                                      └── Share Code / Logout
```

---

## 🔧 Tech Stack

- **Language:** Java
- **UI:** Android XML Layouts with custom drawables
- **Database:** SQLite (local) + Firebase Realtime Database
- **Authentication:** Firebase Auth (Email/Password)
- **Notifications:** AlarmManager + Foreground Service
- **Min SDK:** 26 (Android 8.0 Oreo)
- **Target SDK:** 35

---

## 👤 Author

**Vaibhav Tripathi**
- GitHub: [@vaibhavraj082007](https://github.com/vaibhavraj082007)

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

<p align="center">
  Made with ❤️ for better health management
</p>
