# MedRemind 💊
### Advanced Medication Tracker & Smart Reminder System

**MedRemind** is a professional, production-ready Android application built to help users adhere to their treatment plans with precision. It leverages modern Android standards to provide a reliable, offline-first experience with a focus on high-performance scheduling.

---

## 📸 App Preview

| **Home & Daily Doses** | **Treatment Calendar** | **Medication Inventory** |
|:---:|:---:|:---:|
| ![Home](screenshots/home.jpg) | ![Calendar](screenshots/calender.jpg) | ![Meds](screenshots/meds.jpg) |
| *Real-time dose tracking.* | *Visual timeline of plans.* | *Manage all medications.* |

| **AI Camera Entry (OCR)** | **Manual Add Form** | **Plan & Schedule Editor** | **Details of Medication & Plan** |
|:---:|:---:|:---:|:---:|
| ![Camera](screenshots/ocr.jpg) | ![Manual](screenshots/manual.jpg) | ![Plan](screenshots/med_plan.jpg) | ![Details](screenshots/med_preview.jpg) |
| *Auto-scan via Camera.* | *Detailed manual input.* | *Custom scheduling logic.* |

---

## 🛠 Tech Stack

- **UI:** Jetpack Compose (Material 3).
- **Architecture:** Clean Architecture (Domain, Data, Presentation).
- **Dependency Injection:** Koin.
- **Local Database:** Room (Offline-first).
- **Background Tasks:** - **AlarmManager:** Precision-timed medication reminders.
    - **BroadcastReceivers:** System-level event handling (Boot & Alarms).
- **Language:** Kotlin Coroutines & Flow.

---

## 🏗 Key Engineering Challenges Solved

### 1. Reliable Scheduling Engine
Implementing a robust reminder system using `AlarmManager`. I solved the challenge of **system reboots** by implementing a `BOOT_COMPLETED` receiver that automatically reschedules all active doses from the **Room database** back into the system alarm service.

### 2. Actionable Notifications
Designed custom notifications with **direct actions** (Done / Snooze). Used **KoinComponent** inside `BroadcastReceivers` to inject repositories and update the database state without requiring the user to open the app.

### 3. AI-Powered Data Entry
Integrated **ML Kit** for Optical Character Recognition (OCR) to allow users to scan medication labels, significantly reducing manual data entry errors.

---

## 📁 Project Structure
```text
com.abdo.medremind
├── data
│   ├── local (Room DB, DAOs, Entities)
│   ├── repository (Implementation)
│   └── alarm (AlarmManager logic & Receivers)
├── domain
│   ├── model (Clean entities)
│   ├── repository (Interfaces)
│   └── use_case (Business Logic)
└── presentation
    ├── screens (Compose UI)
    └── viewmodel (State management)
