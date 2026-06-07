# KBK - KiskiBreakKab 🚀

**KBK (KiskiBreakKab)** is a high-performance, Brutalist-designed university management and social synchronization platform. Built for students to optimize their campus life, KBK allows users to sync timetables, find free friends in real-time, and discover available tactical spaces (rooms) across multiple buildings.

## 🛠️ Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Modern Declarative UI)
- **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture
- **Dependency Injection:** Hilt (Dagger)
- **Database:** 
  - **Cloud:** Firebase Firestore (Real-time sync)
  - **Local:** Room Persistence Library (Offline capability)
- **Authentication:** Firebase Auth
- **Background Tasks:** WorkManager (Periodic "Friend Free" notifications)

## ✨ Core Features

- **Tactical Dashboard:** Real-time system status, current slot tracking, and network stats.
- **Social Sync:** Connect with friends and get instant notifications when they are free.
- **Room Finder:** Multi-building and multi-department room discovery with "I'm Here" tracking.
- **Brutalist 3D UI:** A bold, high-contrast visual identity with tactile 3D components and dual-theme support.
- **Admin Override:** Exclusive bulk-import tools for UIDs authorized to manage campus infrastructure.

## 🚀 Getting Started

1. Clone the repository.
2. Connect your Firebase project (add `google-services.json`).
3. Build and deploy to your Android device.

---

*“Sync timetables. Find friends. Make every campus break count.”*
