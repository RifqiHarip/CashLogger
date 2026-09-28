# Cash Logger 🎾

A native Android application built with Kotlin and Jetpack Compose, designed to track and manage community cash flows (income and expenses) for KSTC. The app features a serverless backend, syncing data bi-directionally in real-time with Google Sheets via a Google Apps Script (GAS) Webhook.

## ✨ Features

* **Real-time Total Cash Dashboard:** Instantly fetches and displays the current total cash balance from the Google Sheets backend the moment the app is launched.
* **Seamless Cash Entry:** Easily toggle between *Pemasukan* (Income) and *Pengeluaran* (Expense) to record transactions, with an optional field for the week number.
* **Fluid UI/UX:** Built entirely with Jetpack Compose featuring custom fonts (Plus Jakarta Sans), a branded Dark Green and Gold color scheme, and fluid spring-physics animations for a premium feel.
* **Smart State Management:** Forms automatically clear upon successful submission.
* **Interactive Confirmations:** Displays a clean, native `AlertDialog` summarizing the successfully submitted data and the updated total balance.

## 🛠️ Tech Stack & Architecture

* **Language:** [Kotlin](https://kotlinlang.org/)
* **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material Design 3)
* **Architecture:** MVVM (Model-View-ViewModel) with `StateFlow` for reactive state management.
* **Networking:** [Retrofit 2](https://square.github.io/retrofit/) & [GSON Converter](https://github.com/google/gson) for API communication.
* **Asynchronous Operations:** Kotlin Coroutines.
* **Backend / Database:** Google Sheets + Google Apps Script (Serverless Web App).

## 📁 Project Structure highlights
* `ui/KasScreen.kt`: The main Compose UI containing the dashboard, form, and dialogs.
* `ui/KasViewModel.kt`: Handles the business logic, state handling (`UiState`), and asynchronous network calls.
* `data/network/GasApiService.kt`: The Retrofit client interface defining the `GET` and `POST` endpoints.
* `AppConfig.kt`: A secure, pure-Kotlin configuration object to store the GAS Webhook endpoint, bypassing traditional `BuildConfig` limitations.
* `ui/theme/`: Custom color palettes (`KstcDarkGreen`, `KstcGold`) and Typography configurations.

## 🚀 Setup and Installation

### 1. Backend Setup (Google Sheets)
1. Create a new Google Sheet.
2. Go to **Extensions > Apps Script** and write your `doGet(e)` (to fetch total cash) and `doPost(e)` (to append new rows) functions.
3. Deploy the script as a **Web App** with access set to **"Anyone"**.
4. Copy the deployment URL. *(Note: Always redeploy as a "New Version" whenever you make changes to the script).*

### 2. Android App Setup
1. Clone this repository:
   ```bash
   git clone [https://github.com/your-username/uangkas.git](https://github.com/your-username/uangkas.git)
