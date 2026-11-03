# Cash Logger (UangKas) 🎾

A native Android application built with Kotlin and Jetpack Compose, designed to manage community cash flows for KSTC while providing essential match-day utility tools (**Qibla Compass** & **Tennis Score Tracker**). The app features a serverless backend, syncing financial data bi-directionally in real-time with Google Sheets via a Google Apps Script (GAS) Webhook.

---

## ✨ Features

### 💰 Cash Flow Management
- **Real-time Total Cash Dashboard:** Instantly fetches and displays the current total cash balance from Google Sheets upon app launch.
- **Recent Transaction History:** Automatically fetches and displays a scrollable list of the latest income and expense entries.
- **Smart Input Validation & Formatting:** Real-time `VisualTransformation` automatically formats currency inputs with "Rp" and thousands separators while typing. Built-in validation prevents empty submissions and double-clicks using interactive loading states.
- **Seamless Cash Entry:** Easily toggle between *Pemasukan* (Income) and *Pengeluaran* (Expense) to record transactions, with an optional field for the week number.
- **Quick Access to Raw Data:** Includes a dedicated text button to instantly open the connected Google Sheets database in the browser/app.

### 🧭 Qibla Compass (Fitur Tambahan)
- **Real-time GPS Location:** Integrates `FusedLocationProviderClient` to dynamically calculate the precise Qibla degree based on the user's current GPS coordinates (with Depok coordinates as fallback).
- **Sensor-Driven Rotation:** Uses Android's `TYPE_ROTATION_VECTOR` sensor to provide a smooth, 360-degree interactive compass dial.
- **Haptic Feedback:** Triggers a tactile vibration pulse when the device aligns directly with the Kaaba ($\pm 2.5^\circ$).
- **Adaptive Dark & Light Theme:** Full color palette adjustment for optimal visibility in both Dark and Light modes.

### 🎾 Tennis Score Tracker (Fitur Tambahan)
- **Immersive Fullscreen Landscape Mode:** Automatically locks orientation to landscape (`SCREEN_ORIENTATION_SENSOR_LANDSCAPE`) and hides status bars (`WindowInsetsControllerCompat`) for a clean court-side display.
- **Customizable Teams & Target Games:** Support for custom team/player names and configurable match targets (Presets: First to 4, 6, or 8 Games, or custom count).
- **Flexible Scoring Systems:** Toggle between **Golden Point (No-Ad)** (deciding point at 40-40) and **Advantage Mode (Standard Ad)**. Includes automatic Tiebreak detection.
- **Split-Screen Touch UI & Responsive Scaling:** Large touch targets split left/right with dynamic font scaling (`BoxWithConstraints`) to prevent text clipping on small screens (e.g., Xiaomi 15).
- **Undo Support & Slim Control Bar:** Dedicated undo stack to revert accidental score taps without disrupting match flow.
- **Scrollable Match Settings Dialog:** Responsive, scrollable settings modal for on-the-fly match configuration.
- **Match Logging & WhatsApp Export:** Automatically logs completed matches and features a one-tap Share Intent (`Intent.ACTION_SEND`) to generate formatted match summary cards for WhatsApp groups.

### 🎨 Theme & UI/UX
- **Adaptive Dark Mode:** Complete Dark and Light theme compatibility across all screens (`KasScreen`, `SettingsScreen`, `QiblaScreen`, `TennisScoreScreen`).
- **Fluid Navigation:** Bottom navigation bar with tab routing and a dedicated **Fitur Tambahan** sub-menu under Settings.
- **State Preservation & Crash Prevention:** Uses `rememberSaveable` state management and `configChanges` activity configurations in `AndroidManifest.xml` to prevent crashes and state loss during orientation switches.

---

## 🛠️ Tech Stack & Architecture

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material Design 3)
- **Architecture:** MVVM (Model-View-ViewModel) with `StateFlow` & `rememberSaveable` for reactive state management.
- **Networking:** [Retrofit 2](https://square.github.io/retrofit/) & GSON Converter for API communication.
- **Location & Sensors:** Google Play Services Location (`FusedLocationProviderClient`) & Android Sensor Manager (`TYPE_ROTATION_VECTOR`).
- **Haptics & System Insets:** Android `Vibrator` / `VibratorManager` and `WindowCompat` insets controllers.
- **Backend / Database:** Google Sheets + Google Apps Script (Serverless Web App).

---

## 📁 Project Structure Highlights

- `ui/KasScreen.kt`: Main Compose UI containing bottom navigation tabs (`Home`, `History`, `Settings`) and sub-screen routing logic.
- `ui/KasViewModel.kt`: Handles financial business logic, state handling (`UiState`), and asynchronous network calls.
- `ui/QiblaScreen.kt`: Qibla compass screen implementation featuring real-time GPS location, rotation sensors, and haptic feedback.
- `ui/TennisScoreScreen.kt`: Immersive landscape tennis scoreboard UI with responsive cards, scrollable settings dialog, and WhatsApp share integration.
- `ui/TennisScoreViewModel.kt`: Manages tennis scoring logic (No-Ad, Advantage, Tiebreak, Undo stack, match records, and WhatsApp text formatting).
- `ui/SettingsScreen.kt`: Settings screen featuring theme toggle and access to the **Fitur Tambahan** sub-menu.
- `util/VibrationUtils.kt`: Reusable utility helper for cross-API haptic vibration feedback.
- `data/network/GasApiService.kt`: Retrofit client interface defining `GET` and `POST` endpoints.