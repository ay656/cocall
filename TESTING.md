# Test Checklist

- GitHub Actions uploads `jianhu-call-debug-apk`.
- The artifact contains `jianhu-call-debug.apk`.
- First launch shows default contact cards.
- Long-pressing the title for 3 seconds enters Settings directly.
- Adding a contact and saving refreshes the main screen.
- `Up` / `Down` changes contact order.
- Leaving Settings with unsaved edits asks for confirmation.
- Android back button on Settings uses the same unsaved-edits confirmation.
- Empty contact list shows a setup hint.
- Restarting the app keeps saved contacts.
- Avatar selection appears on the settings page and main screen.
- Tapping a contact without a phone number shows a warning.
- Tapping a contact with a phone number triggers TTS and a 1-second cancel dialog.
- Not cancelling requests call permission or starts the call.
- Denying call permission shows a fallback message and opens the system dialer.
