# Digital Guardian — Women Safety App

A personal safety Android application designed to protect women in emergency situations. The app provides **one-tap SOS alerts** with GPS location, **covert audio recording**, **hardware-triggered panic activation**, and proximity-based **safe spot mapping** — all running seamlessly on-device with no internet dependency for core features.

## Features

### 🚨 SOS Emergency Alert
- **One-tap panic button** triggers the full SOS sequence instantly
- Sends **SMS with live GPS coordinates** (Google Maps link) to all trusted contacts
- Works with the device's native SMS — **no internet required**
- **Screen dims automatically** during SOS to avoid drawing attention

### 🎙️ Covert Audio Recording
- **Foreground service** starts audio recording silently when SOS is triggered
- Records using `MediaRecorder` API with `AMR_NB` encoding
- Saves timestamped `.3gp` evidence files to device storage
- Runs persistently via Android's foreground notification system

### 🔘 Hardware Panic Trigger (Power Button)
- **Press the power button 5 times** within 3 seconds to silently trigger SOS
- Runs as a **background foreground service** to detect `SCREEN_OFF` events
- Works even when the phone screen is off — **no need to unlock the phone**
- SOS is broadcast via `BroadcastReceiver` to the main activity

### ⏱️ Safe Walk Timer
- Set a **countdown timer** before walking alone (e.g., "I'll be home in 10 minutes")
- If timer expires without pressing "I'm Safe" → **SOS is automatically triggered**
- Timer broadcasts `TRIGGER_SOS` intent on expiration

### 📞 Fake Incoming Call
- Simulates a **fake incoming phone call** with Accept/Decline buttons
- Provides an **escape route** from uncomfortable or dangerous situations
- Full-screen activity styled to look like a real call

### 🗺️ Safe Spots (Google Maps + Places API)
- Displays a **Google Map** centered on the user's current location
- Locates **nearby police stations** using Google Places API
- Marks safe spots on the map with markers for quick navigation

### 👥 Trusted Contacts Management
- Add, view, and delete **trusted emergency contacts**
- Stored locally using `SharedPreferences` (no cloud dependency)
- Long-press to delete contacts with confirmation dialog

### 🔄 Boot Persistence
- `BootReceiver` automatically restarts the `PowerButtonService` after device reboot
- Ensures the hardware panic trigger is **always active**

## Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                        MainActivity                          │
│  • SOS Button → requestPermissions → startSosLogic()         │
│  • Gets GPS location → sends SMS to trusted contacts         │
│  • Starts RecordingService → dims screen                     │
│  • BroadcastReceiver listens for TRIGGER_SOS intent          │
├──────────────────────────────────────────────────────────────┤
│                        Services                              │
│  ┌─────────────────────┐  ┌──────────────────────────┐       │
│  │  RecordingService   │  │  PowerButtonService      │       │
│  │  • Foreground svc   │  │  • Foreground svc        │       │
│  │  • MediaRecorder    │  │  • Detects 5x power btn  │       │
│  │  • Saves .3gp audio │  │  • Broadcasts TRIGGER_SOS│       │
│  └─────────────────────┘  └──────────────────────────┘       │
├──────────────────────────────────────────────────────────────┤
│                        Activities                            │
│  ┌────────────────┐ ┌────────────────┐ ┌─────────────────┐   │
│  │ContactsActivity│ │SafeWalkActivity│ │SafeSpotsActivity │   │
│  │• Add/Delete    │ │• CountdownTimer│ │• GoogleMap       │   │
│  │• SharedPrefs   │ │• Auto-SOS      │ │• Places API      │   │
│  └────────────────┘ └────────────────┘ └─────────────────┘   │
│  ┌────────────────┐ ┌────────────────┐                       │
│  │FakeCallActivity│ │  BootReceiver  │                       │
│  │• Fake call UI  │ │• Restart svc   │                       │
│  └────────────────┘ └────────────────┘                       │
└──────────────────────────────────────────────────────────────┘
```

## Tech Stack

| Technology | Usage |
|---|---|
| **Java 11** | Primary language |
| **Android SDK 34** | Target platform (min SDK 23 / Android 6.0) |
| **Google Maps SDK** | Map display and safe spot visualization |
| **Google Places API** | Find nearby police stations |
| **Android Location API** | GPS coordinate retrieval (`LocationManager`) |
| **Android SMS API** | Send emergency SMS (`SmsManager`) |
| **MediaRecorder API** | Covert audio recording |
| **Foreground Services** | Persistent background recording and power button detection |
| **BroadcastReceiver** | Inter-component SOS communication and boot detection |
| **SharedPreferences** | Local storage for trusted contacts |
| **Gradle** | Build system |

## Permissions

| Permission | Purpose |
|---|---|
| `ACCESS_FINE_LOCATION` | Get precise GPS coordinates for SOS messages |
| `ACCESS_COARSE_LOCATION` | Fallback location provider |
| `SEND_SMS` | Send emergency SMS to trusted contacts |
| `RECORD_AUDIO` | Covert audio recording during emergencies |
| `FOREGROUND_SERVICE` | Keep recording and power button service alive |
| `RECEIVE_BOOT_COMPLETED` | Auto-restart services after reboot |

## Project Structure

```
app/src/main/
├── AndroidManifest.xml
├── java/com/sankalp/womensafe/
│   ├── MainActivity.java           # SOS button, GPS, SMS, screen dimming
│   ├── ContactsActivity.java       # Trusted contacts CRUD
│   ├── SafeWalkActivity.java       # Countdown timer with auto-SOS
│   ├── FakeCallActivity.java       # Fake incoming call screen
│   ├── SafeSpotsActivity.java      # Google Maps + police stations
│   ├── RecordingService.java       # Foreground audio recording service
│   ├── PowerButtonService.java     # Hardware panic trigger (5x power btn)
│   └── BootReceiver.java           # Auto-restart services on boot
└── res/
    ├── layout/                     # Activity layouts (5 XML files)
    ├── menu/                       # Options menu
    ├── drawable/                   # Icons and SOS button background
    └── values/                     # Colors, strings, themes
```

## Setup & Build

### Prerequisites
- Android Studio (Hedgehog or newer)
- Android SDK 34
- Google Maps API key ([Get one here](https://console.cloud.google.com/apis/library/maps-backend.googleapis.com))

### Configuration
1. Clone the repository
2. Add your Google Maps API key in `app/src/main/res/values/strings.xml`:
   ```xml
   <string name="google_maps_key">YOUR_API_KEY_HERE</string>
   ```
3. Open the project in Android Studio
4. Sync Gradle and run on a device (emulator won't support all hardware features)

### Build
```bash
./gradlew assembleDebug
```

## Key Technical Concepts

| Concept | Implementation |
|---|---|
| **Foreground Services** | `RecordingService` and `PowerButtonService` run persistently with notifications |
| **BroadcastReceiver** | `PowerButtonService` → `TRIGGER_SOS` → `MainActivity` inter-component communication |
| **Boot Persistence** | `BootReceiver` restarts services on `BOOT_COMPLETED` |
| **Runtime Permissions** | Dynamic permission requests for Location, SMS, Audio |
| **Hardware Interaction** | Power button detection via `ACTION_SCREEN_OFF` events |
| **Location Services** | `LocationManager` + `FusedLocationProviderClient` |
| **Local Storage** | `SharedPreferences` for trusted contacts (no cloud dependency) |
| **UI State Management** | Visibility toggling for SOS active/inactive states |

## Author

**Yogendhra Gadhanchetty**
