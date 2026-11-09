# Twitch Auto Collector v1.0 — Android Studio project

## Build
1. Install Android Studio with JDK 17 and Android SDK Platform 35.
2. Open this folder as a project. Allow Gradle sync to download Android Gradle Plugin 8.6.1.
3. Choose **Build > Build APK(s)**. The debug APK will be under `app/build/outputs/apk/debug/`.
4. Install the APK on an Android 8+ phone; Android 13 is supported.

## Usage
Open Twitch in the embedded browser, sign in if Twitch permits WebView login, and open a live channel. The app checks for a claimable channel points bonus every 5 seconds while the activity is active. The counter is **click attempts**, not verified awards.

## Limitations
- Twitch may block sign-in from embedded browsers; this project does not bypass authentication restrictions.
- Twitch can change its page structure, selectors, or bonus behavior. The scanner is an experimental baseline and may need updating after device testing.
- Scanning is active only while the app activity remains alive. It does not promise background collection.
- Twitch's terms and automation restrictions may apply. Use at your discretion.
- No screen capture, accessibility service, or account credentials are collected by native code.
- Gradle wrapper is not included; Android Studio's installed Gradle can be used to sync, or generate a wrapper with Gradle 8.7.
