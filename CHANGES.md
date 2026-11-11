# Changes from cleanyourtv

## 1.0.1, first public release

1.0.0 was a private test build; 1.0.1 adopted the Northlight OS name and the Android application ID
`com.northlightos.launcher`. This is a new app identity and does not update an installation with the previous ID.

Northlight OS is cleanyourtv's launcher with these changes, all made for Vidda/Hisense China laser TVs:

| Change | Files |
|---|---|
| App identity: Northlight OS name, application ID `com.northlightos.launcher`, version 1.0.1, optional release key | `launcher/gradle.properties`, `launcher/app/build.gradle`, `values*/strings.xml` |
| Main activity is direct-boot aware, so a cold boot lands on Northlight OS instead of the Chinese SecondScreen home | `AndroidManifest.xml` |
| Inputs pill starts the Hisense Signal Source panel as a foreground service (Android 14 refuses a plain start) | `MainActivity.java` (`openSourcePanel`) |
| PIN-locked "System apps" folder beside the App library; the App library leaves those apps out | `PinDialog.java`, `AppLibraryDialog.java`, `AppAdapter.java`, `MainActivity.java`, `row_app_library.xml` |
| English "Quick" panel replacing the Chinese quick panel | `QuickPanel.java`, `activity_main.xml`, `ic_q_*.xml`, `strings.xml` (`qp_*`) |
| Network pill dropdown: Ethernet, VPN (On/Off), AirScreen | `MainActivity.java` (`showNetworkMenu`) |
| Config for this TV: dock, hidden apps, source panel, VPN app, App library on, date under the clock | `values/config.xml` |
| One-command setup and undo scripts, and the tested debloat list | `northlight-os-setup.sh`, `northlight-os-undo.sh`, `debloat.txt` |
