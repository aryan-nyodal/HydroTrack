# 💧 HydroTrack - Water Reminder & Tracker

**HydroTrack** is a production-quality Android application built with modern Android best practices: **Kotlin**, **Jetpack Compose (Material 3)**, **MVVM architecture**, **Room Database**, **Jetpack DataStore**, **Hilt Dependency Injection**, and **AlarmManager/WorkManager**.

---

## ✨ Features

### 1. Daily Water Goal & Smart Tracking
- **Onboarding Flow:** Welcome screen → Interactive goal slider (500–8000 ml) → Interval preferences → Notification permissions.
- **Wave Progress Indicator:** Smooth, animated sine-wave water fill inside a circular container showing real-time hydration percentage.
- **Quick-Add Actions:** One-tap `+250 ml`, `+500 ml`, and `Custom` ml logging with input validation (1–5000 ml).
- **Log Management:** Swipe-to-delete with red dismissal background and instant **Undo** snackbar. Total intake, points, and streak automatically recalculate upon deletion.

### 2. Notifications With Direct Actions
- Periodic reminders scheduled with `AlarmManager` (`setExactAndAllowWhileIdle`), surviving app kill, Doze mode, and device reboots (`BOOT_COMPLETED`).
- **3 Direct Action Buttons on Every Notification:**
  - `250 ml` → logs instantly in the background without opening the app.
  - `500 ml` → logs instantly in the background without opening the app.
  - `Custom` → uses Android **RemoteInput** inline reply with numeric hint so users can type any ml amount directly from the notification shade or lock screen.
- Reminders automatically pause once the daily goal is reached, sending a congratulatory milestone notification.
- Evening **"Streak-At-Risk"** alert (default 8:00 PM) if the goal is not yet achieved and a streak is active.

### 3. Streak & Gamification System
- **Single Source of Truth (`StreakCalculator`):** Consecutive successful days (intake $\ge$ goal) increment the streak.
- **Real Daily Snapshots:** Stored in Room (`DailyRecordEntity`) with `goalSnapshot`, ensuring past days are never retroactively corrupted when goals change.
- **Visuals:** Pulsing flame chip (`🔥 5 days`), weekly streak strip (7-day circles), and monthly streak calendar view.
- **Points & Rewards:**
  - `+1 pt` per 10 ml logged.
  - `+50 pts` daily goal bonus.
  - `+10 pts × current streak count` (capped at +100) on goal completion.
  - Unlockable achievement badges (First Sip, Early Bird, Goal Crusher, Century Club, etc.).

### 4. History, Analytics & Preferences
- **Weekly Bar Chart:** Drawn with Compose `Canvas` with an overlaid goal threshold line.
- **Monthly Streak Calendar:** Visual matrix highlighting completed days and continuous streaks.
- **All-Time Stats:** Daily averages (7-day / 30-day), all-time total, best day, and longest streak.
- **Settings:** Custom reminder intervals, active hours, Light/Dark/System theme, and Unit conversion (`ml` $\leftrightarrow$ `fl oz`).

---

## 🏗️ Architecture & Package Structure

```
com.hydrotrack/
├── HydroTrackApp.kt                    # Application class (Hilt + WorkManager + Channels)
├── MainActivity.kt                     # Single ComponentActivity
├── data/
│   ├── datastore/
│   │   └── SettingsDataStore.kt        # DataStore preferences
│   ├── local/
│   │   ├── HydroDatabase.kt            # Room database
│   │   ├── dao/
│   │   │   ├── WaterLogDao.kt
│   │   │   └── DailyRecordDao.kt
│   │   └── entity/
│   │       ├── WaterLogEntity.kt
│   │       └── DailyRecordEntity.kt
│   └── repository/
│       ├── WaterRepository.kt
│       └── SettingsRepository.kt
├── domain/
│   ├── model/                          # WaterLog, DailyRecord, StreakInfo, Badge, etc.
│   ├── usecase/                        # LogWaterUseCase, DeleteWaterLogUseCase, etc.
│   ├── StreakCalculator.kt             # Pure domain streak calculation
│   └── PointsCalculator.kt             # Pure domain points & bonus calculation
├── notifications/
│   ├── NotificationHelper.kt           # Channels, RemoteInput builders, actions
│   ├── ReminderReceiver.kt            # BroadcastReceiver handling inline actions
│   ├── ReminderScheduler.kt           # AlarmManager exact scheduling
│   ├── BootReceiver.kt                # Reschedule alarms on device boot
│   └── ReminderMessages.kt            # 12+ friendly, varied reminder quotes
├── ui/
│   ├── theme/                         # Material 3 Color (aqua/blue + amber), Type, Shape, Theme
│   ├── navigation/                    # HydroNavGraph, Screen, NavViewModel
│   ├── components/                    # WaveProgressIndicator, QuickAdd, StreakChip, etc.
│   ├── onboarding/                    # OnboardingScreen + OnboardingViewModel
│   ├── home/                          # HomeScreen + HomeViewModel
│   ├── history/                       # HistoryScreen + HistoryViewModel
│   ├── rewards/                       # RewardsScreen + RewardsViewModel
│   └── settings/                      # SettingsScreen + SettingsViewModel
├── util/
│   ├── Constants.kt
│   ├── DateUtils.kt
│   ├── InputValidator.kt
│   └── UnitConverter.kt
└── work/
    └── MidnightStreakWorker.kt         # HiltWorker for midnight rollover & streak check
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio Ladybug (2024.2.1)** or newer.
- **JDK 17**.
- Android SDK 35 (compileSdk & targetSdk), minSdk 26.

### Build and Run
1. Open the project folder in Android Studio:
   `c:\Users\ASUS\OneDrive\Desktop\project\HydroTrack`
2. Sync Project with Gradle Files.
3. Select an emulator or connected device running Android 8.0 (API 26) or higher.
4. Click **Run (`Shift + F10`)**.

---

## 🧪 Testing Reminders & Streak Logic

### 1. Testing Notification Actions (Inline Reply & Quick-Add)
1. Open the app and navigate to **Settings**.
2. Click **"Send Test Notification 🔔"**.
3. Pull down the Android system notification drawer:
   - Tap **"250 ml"** or **"500 ml"**: The notification updates to a confirmation message and writes the intake to Room in the background.
   - Tap **"Custom"**: The keyboard opens directly inside the notification. Type an amount (e.g. `350`) and press send. The intake will be validated and logged.

### 2. Testing the Streak System (Built-in Debug Simulator)
To test streak progression without waiting for multiple days:
1. Go to **Settings** → **🧪 Debug & Testing**.
2. Tap **"Simulate Yesterday Met"** or **"+3 Days Streak"**.
3. Return to the **Home** or **History** tab:
   - Notice the flame streak chip immediately increments (`🔥 3 days`).
   - The weekly streak circles fill in for past days.
   - The monthly streak calendar visually connects the streak run.

### 3. Running Unit Tests
Execute the unit test suite in Android Studio or via Gradle:
```bash
./gradlew test
```
- `StreakCalculatorTest`: Verifies consecutive streaks, gaps, mid-day goal changes, entry deletions, and edge cases.
- `PointsCalculatorTest`: Verifies point formulas, caps, daily bonus, and revocation.
- `InputValidationTest`: Verifies custom input ranges (1–5000 ml) and reject rules.
