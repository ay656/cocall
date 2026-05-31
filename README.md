# JianHu Call

An elderly-friendly Android calling app. Family members configure contacts first; the elder only taps a large familiar card to call.

This version uses native Android Java + Gradle. It does not use Kivy, Buildozer, or python-for-android.

## Features

- Native Android project that builds a debug APK with Gradle.
- Main screen with a soft background and glass-style contact cards.
- Large avatar, large contact title, and phone icon.
- Whole card is tappable.
- Tap a contact: TTS announces the call, a 1-second cancel dialog appears, then the app calls.
- If `CALL_PHONE` permission is missing, the app explains why it is needed before requesting it.
- If `CALL_PHONE` permission is denied, the app opens the system dialer.
- If direct-call permission is denied, the app explains the fallback before opening the dialer.
- Tap the title to enter Settings directly.
- Settings: add, delete, reorder, edit name, display name, phone number, and avatar.
- Leaving Settings with unsaved edits shows a confirmation dialog.
- Empty contact list shows an in-app setup hint.
- Invalid phone numbers are blocked on save.
- Broken avatar links automatically fall back to a default text avatar.
- Contacts are stored locally as JSON.

## Build With GitHub Actions

Upload this project to the repository root. The root must directly contain:

```text
settings.gradle
build.gradle
gradle.properties
app/build.gradle
app/src/main/AndroidManifest.xml
.github/workflows/android-debug.yml
```

Then:

1. Open the GitHub repository.
2. Go to `Actions`.
3. Run `Android Debug APK`.
4. Open the latest workflow run.
5. Scroll to the bottom.
6. Download the artifact named `jianhu-call-debug-apk`.
7. Unzip it and install `jianhu-call-debug.apk`.

The APK appears as a GitHub Actions artifact. It does not appear in the repository file list.

## Local Build

If Android Studio, Android SDK, and Gradle are installed:

```bash
gradle :app:assembleDebug
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Use

1. Install the APK.
2. Open the app.
3. Tap the top title to enter Settings directly.
4. Add or edit contacts.
5. Use `Up` / `Down` to reorder contacts.
6. Save and return to the main screen.
7. Tap a contact card. Cancel within 1 second if needed, otherwise the phone call starts.
8. Android back button on Settings asks before discarding unsaved edits.

## Notes

- Direct calling needs `CALL_PHONE`.
- If permission is denied, Android opens the system dialer.
- Avatar selection uses Android's system document picker and persists URI read permission.
