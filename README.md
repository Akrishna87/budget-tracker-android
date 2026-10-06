# Budget Tracker (Android)

A native Android budget tracker: expenses, income, recurring bills,
loans/credit cards with outstanding-balance tracking, savings goals, and
per-category budgets. Kotlin + Jetpack Compose + Room, fully local
(no account, no internet needed).

## Status: Stage 1 (core tracking)

- Add income/expense entries (amount, date, category/payment method or
  income source, note), with quick-add chips and smart defaults.
- Editable categories with optional monthly budgets.
- Dashboard: month picker, income/expense/net, category budget progress.
- "Repeat last expense" one-tap button.
- History with filters (type, category, month, note search) and
  edit/delete.

Not yet built (planned next stages): recurring bills + due notifications,
loans/credit cards with outstanding-balance tracking, savings goals,
trend analytics, a home-screen quick-add widget, and biometric/PIN lock.

## Installing the latest build

**Direct download (recommended):** every push to `main` updates the
[latest debug build release](https://github.com/Akrishna87/budget-tracker-android/releases/tag/latest-debug)
with a fresh APK attached — no zip, just tap to download:

https://github.com/Akrishna87/budget-tracker-android/releases/download/latest-debug/app-debug.apk

Install it on an Android device with "install from unknown sources"
allowed for your file manager/browser.

Alternatively, every build is also uploaded as a zipped artifact under
the **Actions** tab (useful for grabbing an APK from a specific older
commit, since the release above always reflects the newest one).

To build locally instead, open this folder in Android Studio (Koala or
newer) and run the `app` configuration, or from a terminal with the
Android SDK installed:

```
./gradlew assembleDebug
```

The resulting APK is at `app/build/outputs/apk/debug/app-debug.apk`.

## Tech

- Kotlin, Jetpack Compose (Material 3), Navigation Compose
- Room (SQLite) for local persistence, no network/account required
- MVVM: Repository + ViewModel (StateFlow) per screen
- minSdk 26, compileSdk/targetSdk 35
