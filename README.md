# Shred Sheets 🎸

> An ultra-responsive, offline-first music suite for guitarists and string players built with Kotlin and Jetpack Compose.

---

## 🌟 Features

- **⚡ Precision Tuner**: Real-time microphone pitch detection supporting Guitar (6, 7 & 12 string), Bass (4 & 5 string), Ukulele, Violin, Cello, and Banjo with alternate tunings (Drop D, DADGAD, Open G, Half-Step Down, etc.). Includes animated Shaka ("hang loose") hand feedback on perfect in-tune lock.
- **🎸 Chord Library**: Interactive fretboard chord diagram viewer with multi-instrument finger placements, muted/open string badges, and one-tap synthesized audio preview.
- **⏱️ Click & Metronome**: High-accuracy rhythm generator featuring click and acoustic drum kit sounds, tempo tap detection, time signature selection (2/4, 3/4, 4/4, 6/8, etc.), and visual beat indicators.
- **🔁 2-Track Loop Station**: On-the-fly looping station with overdub, independent track volume, headphone monitor support, and local WAV export/sharing.
- **🎨 Custom Theming**: System dynamic color theming (Material 3), dark/light mode toggle, accent color selector, and accessibility options (Reduce Motion).

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.x
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM with reactive StateFlow & Coroutines
- **Database**: Android Jetpack Room (Offline-first persistence)
- **Audio DSP**: Low-latency PCM buffer analysis via `AudioRecord`, Yin/autocorrelation pitch detection, and dynamic sine/drum PCM synthesis via `AudioTrack`.
- **Target Platform**: Android 7.0+ (API 24 to API 36)

---

## 📦 Building and Running

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17 or JDK 21
- Android SDK Platform 36

### Build Debug APK
```bash
./gradlew assembleDebug
```
The debug APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

### Build Release Android App Bundle (AAB) for Google Play Console
```bash
./gradlew bundleRelease
```
The App Bundle will be generated at `app/build/outputs/bundle/release/app-release.aab`.

---

## 🚀 Google Play Console Release Checklist

1. **Application ID**: `com.aistudio.shredsheets.tuner`
2. **Version Code**: Set in `app/build.gradle.kts` (currently `versionCode = 2`, `versionName = "2.0"`).
3. **App Signing**:
   - For Google Play App Signing, generate an upload keystore:
     ```bash
     keytool -genkey -v -keystore my-upload-key.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
     ```
   - Pass signing credentials via environment variables during release builds:
     - `KEYSTORE_PATH`: Path to `my-upload-key.jks`
     - `STORE_PASSWORD`: Keystore password
     - `KEY_PASSWORD`: Key password
4. **App Assets**:
   - 512x512 High-Res Icon: Included in project root (`shred_sheets_icon_512.png`).
   - Feature Graphic & Screenshots: Exportable directly from running app / emulator.
5. **Permissions**:
   - `RECORD_AUDIO`: Strictly for on-device real-time pitch detection and loop recording.
   - `VIBRATE`: Tactile haptic feedback on beat ticks and tuning locks.
   - Zero background data tracking, zero third-party SDK telemetry.
6. **Privacy Policy**:
   - Hosted policy ready at `PRIVACY_POLICY.md` and `privacy-policy.html`.

---

## 🔒 Privacy

Shred Sheets collects **zero** user data. All audio processing is handled strictly in memory on the device and is never stored, tracked, or sent to any remote server. See [PRIVACY_POLICY.md](PRIVACY_POLICY.md) for complete details.

---

## 📄 License
All rights reserved © Kyle Silver.
