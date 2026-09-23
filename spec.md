# Minimalist Android Launcher — Technical Specification

**Stack:** Kotlin + Jetpack Compose
**Min SDK:** 26 (Android 8.0)
**Target SDK:** latest stable
**Architecture:** MVVM + Repository pattern

---

## 1. Product Overview

A distraction-free Android home screen replacement. No icon grid, no widgets by
default — just a short list of favorite apps as text, a clock, and a fast
searchable app drawer. Inspired by launchers like Olauncher and Minimalist
Phone.

---

## 2. Feature Scope

### MVP (v1)
- Home screen: vertical text-only list of 5–8 favorite apps
- Digital clock + date at top of home screen (tap → opens default clock app)
- Swipe up from home → alphabetical app drawer
- Type-to-filter search inside app drawer
- Swipe down from home → notification shade
- Long-press app in drawer → add/remove favorite, hide app
- Register as default `HOME` launcher; back button does nothing on home
- Dark / light / AMOLED-black theme (follows system by default)

### v2 (nice-to-have)
- Most-used-apps sorting (via `UsageStatsManager`)
- Double-tap to lock screen (via `AccessibilityService`)
- Notification dot badges next to app names
- Custom font size / font family
- Settings backup/restore (JSON export/import)
- Optional secondary page for widgets (kept opt-in, not default)

---

## 3. Tech Stack

| Layer | Choice |
|---|---|
| Language | Kotlin |
| UI toolkit | Jetpack Compose |
| Navigation | Compose Navigation |
| State management | StateFlow / ViewModel |
| Local storage | DataStore (Preferences) |
| Async | Kotlin Coroutines (Dispatchers.IO for app queries) |
| Key Android APIs | `PackageManager`, `LauncherApps`, `UsageStatsManager` (v2), `AccessibilityService` (v2) |

---

## 4. Architecture

```
app/
 ├── data/
 │    ├── AppRepository.kt         // wraps PackageManager / LauncherApps calls
 │    ├── SettingsDataStore.kt     // favorites, hidden apps, theme prefs
 │    └── model/
 │         └── AppInfo.kt
 ├── domain/
 │    └── usecases/
 │         ├── GetAppListUseCase.kt
 │         ├── ToggleFavoriteUseCase.kt
 │         └── LaunchAppUseCase.kt
 ├── ui/
 │    ├── home/
 │    │    ├── HomeScreen.kt
 │    │    └── HomeViewModel.kt
 │    ├── drawer/
 │    │    ├── AppDrawerScreen.kt
 │    │    └── AppDrawerViewModel.kt
 │    ├── settings/
 │    │    ├── SettingsScreen.kt
 │    │    └── SettingsViewModel.kt
 │    └── theme/
 │         ├── Color.kt
 │         ├── Type.kt
 │         └── Theme.kt
 ├── receiver/
 │    └── AppChangeReceiver.kt     // listens for PACKAGE_ADDED/REMOVED/REPLACED
 └── MainActivity.kt               // single Activity, hosts Compose NavHost
```

---

## 5. Data Model

```kotlin
data class AppInfo(
    val packageName: String,
    val label: String,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val launchCount: Int = 0
)
```

Settings stored in DataStore:
```kotlin
data class LauncherSettings(
    val favoritePackages: List<String> = emptyList(),
    val hiddenPackages: List<String> = emptyList(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM // LIGHT, DARK, AMOLED, SYSTEM
)
```

---

## 6. AndroidManifest Essentials

```xml
<activity
    android:name=".MainActivity"
    android:launchMode="singleTask"
    android:exported="true"
    android:theme="@style/Theme.Launcher">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.HOME" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

Back-press handling: override `onBackPressed()` / use `BackHandler {}` in
Compose to consume the event on the home screen so the launcher can't be
"closed."

---

## 7. Key Implementation Notes

- **Fetching installed apps:** query `PackageManager.queryIntentActivities()`
  with `Intent.ACTION_MAIN` + `Intent.CATEGORY_LAUNCHER`. Run on
  `Dispatchers.IO`, cache result in the repository, expose as `StateFlow`.
- **Live updates:** register a `BroadcastReceiver` for
  `ACTION_PACKAGE_ADDED` / `ACTION_PACKAGE_REMOVED` / `ACTION_PACKAGE_REPLACED`
  to refresh the cached app list without a full re-query.
- **Launching apps:**
  `context.startActivity(packageManager.getLaunchIntentForPackage(pkg))`.
- **No icons by default** — keeps rendering fast and matches the minimalist
  aesthetic; if icons are added later, cache `Drawable → Bitmap` conversions
  (expensive) in an LRU cache.
- **Usage stats (v2):** requires `PACKAGE_USAGE_STATS`, which can't be
  requested via normal runtime permission dialog — must deep-link the user to
  `Settings.ACTION_USAGE_ACCESS_SETTINGS` and detect grant on resume.
- **Testing:** launcher behavior (home button, recents, back-press) must be
  verified on a physical device, not just an emulator.

---

## 8. Build Order

1. Compose scaffold + set-as-default-launcher flow
2. App list fetching + drawer with live search
3. Home screen with configurable favorites
4. Settings screen (theme, hidden apps, favorites management)
5. Gestures (swipe up → drawer, swipe down → notifications)
6. Usage-based sorting (v2)
7. Polish: animations, AMOLED theme, font options

---

## 9. Suggested Build Prompt (for an AI coding assistant)

```
Build an Android minimalist launcher app in Kotlin using Jetpack Compose.

Requirements:
- Min SDK 26, target latest stable SDK
- Single Activity, MVVM architecture, Repository pattern
- Home screen: vertical list of 5–8 favorite apps shown as plain text (no
  icons), digital clock + date at the top, tapping clock opens the default
  clock app
- Swipe up from home opens an alphabetical app drawer with a search bar that
  filters apps as you type
- Long-press an app in the drawer to add/remove from favorites or hide it
- Settings screen: toggle dark/light/AMOLED theme, manage hidden apps, choose
  favorites
- Register the app as a HOME launcher (correct intent-filter categories) and
  handle back button so it doesn't close like a normal app
- Use PackageManager/LauncherApps APIs to enumerate installed apps
  efficiently off the main thread, and a BroadcastReceiver to refresh on app
  install/uninstall
- Store favorites/hidden apps/settings in DataStore Preferences
- Clean, minimal UI: system font, generous whitespace, no icons by default

Set up the project structure first (data/domain/ui layers), then implement
screen by screen: home screen → app drawer → settings → gestures.
Use idiomatic modern Android/Compose patterns (StateFlow, Compose Navigation).
```
