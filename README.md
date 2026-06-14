<div align="center">

<img src="https://img.shields.io/badge/CYPHER-REMOTE-9b30ff?style=for-the-badge&labelColor=04000a&color=9b30ff" />

# CYPHER REMOTE

### Control your PC from your Android phone — no cloud, no subscription, no BS.

[![Download](https://img.shields.io/badge/⬇%20Download-Website-9b30ff?style=for-the-badge)](https://kirtanshilodre.github.io/Cypher_Remote)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Android-9b30ff?style=for-the-badge)](https://github.com/Kirtanshilodre/Cypher_Remote)

</div>

---

## What is Cypher Remote?

**Cypher Remote** is a local network PC remote control system. Your Android phone connects directly to your PC over WiFi — no internet, no third-party servers, no data leaves your network.

```
Android App  ──── TCP ────▶  CypherPC (Windows) / CypherMAC (macOS)
                                        │
                                        ▼
                             Mouse · Keyboard · Media · Shortcuts
```

---

## Screenshots

### Android App

<div align="center">

| Connect | Mouse | Keyboard | Media | Shortcuts |
|:---:|:---:|:---:|:---:|:---:|
| <img src="https://raw.githubusercontent.com/Kirtanshilodre/Cypher_Remote/main/assets/images/android-connect.png" width="160"/> | <img src="https://raw.githubusercontent.com/Kirtanshilodre/Cypher_Remote/main/assets/images/android-trackpad.png" width="160"/> | <img src="https://raw.githubusercontent.com/Kirtanshilodre/Cypher_Remote/main/assets/images/android-keyboard.png" width="160"/> | <img src="https://raw.githubusercontent.com/Kirtanshilodre/Cypher_Remote/main/assets/images/android-media.png" width="160"/> | <img src="https://raw.githubusercontent.com/Kirtanshilodre/Cypher_Remote/main/assets/images/android-shortcuts.png" width="160"/> |

</div>

### Windows & Mac

<div align="center">

| Windows Tray | Mac GUI |
|:---:|:---:|
| <img src="https://raw.githubusercontent.com/Kirtanshilodre/Cypher_Remote/main/assets/images/windows-tray.png" width="300"/> | <img src="https://raw.githubusercontent.com/Kirtanshilodre/Cypher_Remote/main/assets/images/mac-gui.jpg" width="300"/> |

</div>

---

## Features

| Feature | Description |
|---|---|
| 🖱️ **Trackpad** | Move mouse, left/right/double click, scroll strip, adjustable sensitivity |
| ⌨️ **Keyboard** | Type text, special keys, modifiers (Ctrl, Alt, Shift, Win) |
| ⚡ **Shortcuts** | Custom profiles — Chrome, VLC, Presentation and more |
| 🎵 **Media Controls** | Play/Pause, Next, Prev, Volume Up/Down, Mute |
| 📷 **QR Code Connect** | Scan QR from PC tray — IP fills automatically, no typing |
| 📋 **Clipboard Sync** | Copy on phone → paste on PC and vice versa |
| 🕐 **Connection History** | Last 5 IPs saved — one tap reconnect |
| 📳 **Haptic Feedback** | Vibration on every button press |
| 🔒 **Lock / Sleep** | Lock or sleep your PC from phone instantly |
| 📶 **WiFi + Bluetooth** | Connect over local WiFi or Bluetooth RFCOMM |
| 🔒 **100% Private** | No cloud, no internet required, direct TCP only |
| 🎨 **Purple UI** | Dark purple/black theme throughout |


---

## Quick Setup

### Step 1 — Same Network
Connect your PC and phone to the **same WiFi** (or use phone hotspot).

### Step 2 — Run the PC App

**Windows:**
```
Open CypherPC.exe → note IP in system tray → allow through Firewall
```

**macOS:**
```
Open CypherMAC.app → allow Accessibility permission when prompted
Note the IP shown in the app window
```
> First run: macOS will ask for Accessibility permission — allow it for mouse/keyboard control.

### Step 3 — Connect from Phone
Open **Cypher Remote** → WiFi tab → scan QR or enter IP → tap **Connect PC Socket** ✅

---

## Tech Stack

| Component | Technology |
|---|---|
| Android App | Kotlin + Jetpack Compose |
| PC Receiver (Windows) | Python — `pyautogui`, `pystray`, `qrcode` |
| Mac Receiver | Swift + SwiftUI — Native macOS app |
| Website | HTML/CSS/JS — GitHub Pages |
| Connection | TCP Socket (WiFi) + Bluetooth RFCOMM |

---

## Command Protocol

| Command | Action |
|---|---|
| `M:dx:dy` | Move mouse |
| `C:L / C:R / C:D` | Left / Right / Double click |
| `S:delta` | Scroll |
| `K:text` | Type text |
| `K_RAW:key` | Special key press |
| `K_MOD:mods:key` | Modifier combo |
| `MEDIA:action` | Media control |
| `SYS:LOCK` | Lock screen |
| `SYS:SLEEP` | Sleep PC |
| `CLIP:PUSH:text` | Phone → PC clipboard |
| `CLIP:PULL` | PC → Phone clipboard |
| `FILE:START/CHUNK/END` | File transfer |

---

## Project Structure

```
Cypher_Remote/
├── index.html              # GitHub Pages website
├── README.md
├── .gitignore
├── assets/
│   └── images/             # App screenshots
└── src/
    ├── android/            # Kotlin/Jetpack Compose source
    └── pc/                 # Python receiver + Mac Swift source
        ├── cypher.py
        ├── cypher_mac.py
        ├── cypher_mac_app.py
        └── requirements.txt
```

---

## What's New in v1.1

- 🆕 **QR Code Connect** — scan from PC tray, instant connect
- 🆕 **Connection History** — last 5 IPs remembered
- 🆕 **Haptic Feedback** — vibration on all buttons
- 🆕 **Clipboard Sync** — bidirectional phone ↔ PC
- 🆕 **Lock / Sleep** — quick system controls
- 🆕 **CypherMAC** — native Swift/SwiftUI Mac app (no Python needed)

---

<div align="center">

Made with 💜 by [Kirtanshilodre](https://github.com/Kirtanshilodre)

⭐ Star this repo if it helped you!

</div>
