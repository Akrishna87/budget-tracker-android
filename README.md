# Budget Tracker (Android)

A native Android budget tracker: expenses, income, recurring monthly
expenses, and per-category budgets. Kotlin + Jetpack Compose + Room,
fully local (no account, no internet needed).

## Status: rebuilt as a basic app, more features added incrementally

- Add income/expense entries (amount, date, category/payment method or
  income source, note), with quick-add chips and smart defaults.
- Editable categories with optional monthly budgets.
- Dashboard: month picker, income/expense/net, category budget progress
  — tap a category to see its individual entries.
- "Repeat last expense" one-tap button, reachable via a FAB.
- History with filters (type, category, month, note search) and
  edit/delete, with Undo on every delete.
- Recurring expenses (rent, subscriptions, etc.): configure a fixed
  monthly cost once, and it's auto-logged as a real expense on its due
  day every month — no reminders to act on, no manual "mark paid".

Debts/credit cards, savings goals, trend analytics, app lock, and the
home-screen widget from an earlier version were cut in this rebuild to
keep the app simple; any of these can be added back incrementally.

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

**One-time note:** builds through v16 were each signed with a
different, randomly-generated debug key, because CI ran on a fresh
machine every time with no persistent keystore. That made every build
"update-incompatible" with the last, so installing a new one without
first uninstalling the old one failed with "App not installed." As of
the commit that added `app/debug.keystore`, all future builds share one
stable signature — so starting with that build, you'll be able to
update in place with no uninstall needed. If you're coming from v16 or
earlier, you'll need to uninstall once more; after that, never again.

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
