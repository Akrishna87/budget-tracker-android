# Budget Tracker (Android)

A native Android budget tracker: expenses, income, recurring bills,
loans/credit cards with outstanding-balance tracking, savings goals, and
per-category budgets. Kotlin + Jetpack Compose + Room, fully local
(no account, no internet needed).

## Status: Stage 3 (core tracking, bills, debts/goals)

- Add income/expense entries (amount, date, category/payment method or
  income source, note), with quick-add chips and smart defaults.
- Editable categories with optional monthly budgets.
- Dashboard: month picker, income/expense/net, category budget progress,
  plus shortcuts into debts and savings goals.
- "Repeat last expense" one-tap button.
- History with filters (type, category, month, note search) and
  edit/delete.
- Recurring bills/subscriptions with due-date reminder notifications.
- Loans/credit cards tracked by outstanding principal remaining, with
  payment/charge recording.
- Savings goals with contribute/withdraw and a progress bar.

Not yet built (planned next stage): deeper trend analytics, a
home-screen quick-add widget, and biometric/PIN lock.

## Installing the latest build

**Direct download (recommended):** every push to `main` updates the
[latest debug build release](https://github.com/Akrishna87/budget-tracker-android/releases/tag/latest-debug)
with a fresh APK attached — no zip, just tap to download. Open that
release page and download the `app-debug-vNNN.apk` asset listed there
(the number increases every build, so always grab whatever is
currently listed — don't reuse a file you downloaded earlier).

Install it on an Android device with "install from unknown sources"
allowed for your file manager/browser. To confirm you're actually on
the new build afterwards, check **Settings > Apps > Budget Tracker >
App details > Version** — it should match the build number from the
release page.

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
