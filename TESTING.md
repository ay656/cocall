# Test Checklist

- GitHub Actions uploads `jianhu-call-debug-apk`.
- The artifact contains `jianhu-call-debug.apk`.
- First launch shows default contact cards.
- Tapping the title enters Settings directly.
- Adding a contact and saving refreshes the main screen.
- Saving with an empty contact name fails with a message.
- Saving with an invalid non-empty phone number fails with a message.
- `Up` / `Down` changes contact order.
- Leaving Settings with unsaved edits asks for confirmation.
- Android back button on Settings uses the same unsaved-edits confirmation.
- Empty contact list shows a setup hint.
- Restarting the app keeps saved contacts.
- Avatar selection appears on the settings page and main screen.
- Broken avatar URI falls back to the default text avatar.
- Tapping a contact without a phone number shows a warning.
- Tapping a contact with a phone number triggers TTS and a 1-second cancel dialog.
- Not cancelling requests call permission or starts the call.
- Denying call permission shows a fallback message and opens the system dialer.
