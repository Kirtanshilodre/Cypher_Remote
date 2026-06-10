<div align="center">

<img src="https://img.shields.io/badge/CYPHER-REMOTE-9b30ff?style=for-the-badge&labelColor=04000a&color=9b30ff" />

# CYPHER REMOTE

### Control your PC from your Android phone — no cloud, no subscription, no BS.

[![Download](https://img.shields.io/badge/⬇%20Download-Website-9b30ff?style=for-the-badge)](https://kirtanshilodre.github.io/Cypher_Remote)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Android-9b30ff?style=for-the-badge)](https://github.com/Kirtanshilodre/Cypher_Remote)

</div>

---

## What is Cypher Remote?

**Cypher Remote** is a local network PC remote control system. Your Android phone connects directly to your PC over WiFi or Bluetooth — no internet, no third-party servers, no data leaves your network.

```
Android App  ──── TCP ────▶  CypherPC / cypher_mac_app  (Port 3000)
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
| ⚡ **Shortcuts** | Custom profiles — Chrome, VLC, Presentation and more. Add your own. |
| 🎵 **Media Controls** | Play/Pause, Next, Prev, Volume Up/Down, Mute |
| 📶 **WiFi + Bluetooth** | Connect over local WiFi or Bluetooth RFCOMM |
| 🔒 **100% Private** | No cloud, no internet required, direct TCP only |
| 🚀 **Auto Start** | CypherPC launches automatically with Windows startup |
| 🎨 **Purple UI** | Dark purple/black theme throughout Android + PC apps |

---

## Downloads

| Platform | Download | Requirements |
|---|---|---|
| 🪟 Windows | [`CypherPC.zip`](https://github.com/Kirtanshilodre/Cypher_Remote/releases/download/v1.0/CypherPC.zip) | Windows 10/11 |
| 🍎 macOS | [`cypher_mac_app.zip`](https://github.com/Kirtanshilodre/Cypher_Remote/releases/download/v1.0/cypher_mac_app.zip) | macOS 11+ · Python 3 |
| 📱 Android | [`Cypher.apk`](https://github.com/Kirtanshilodre/Cypher_Remote/releases/download/v1.0/Cypher.apk) | Android 8.0+ |

👉 **[Full Download Page](https://kirtanshilodre.github.io/Cypher_Remote)**

---

## Quick Setup

### Step 1 — Same Network
Connect your PC and phone to the **same WiFi** (or use phone hotspot).

### Step 2 — Run the PC App

**Windows:**
```
Open CypherPC.exe → note the IP shown in system tray → allow through Firewall
```

**macOS:**
```
pip3 install pynput pyautogui
python3 cypher_mac_app.py
```
> Allow Accessibility permission when prompted (first time only).

### Step 3 — Connect from Phone
Open **Cypher Remote** → enter IP address → tap **Connect PC Socket** ✅

---

## Tech Stack

| Component | Technology |
|---|---|
| Android App | Kotlin + Jetpack Compose |
| PC Receiver (Windows) | Python — `pyautogui`, `ctypes`, `tkinter` |
| Mac Receiver | Python — `pyautogui`, `pynput`, `tkinter` |
| Website | HTML/CSS/JS — GitHub Pages |
| Connection | TCP Socket (WiFi) + Bluetooth RFCOMM |

---

## Command Protocol

| Command | Action |
|---|---|
| `M:dx:dy` | Move mouse by dx, dy |
| `C:L` | Left click |
| `C:R` | Right click |
| `C:D` | Double click |
| `S:delta` | Scroll |
| `K:key` | Key press |
| `T:text` | Type text |
| `MEDIA:action` | Media control |

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
    └── pc/                 # Python receiver scripts
        ├── cypher.py
        ├── cypher_mac.py
        ├── cypher_mac_app.py
        └── requirements.txt
```

---

<div align="center">

Made with 💜 by [Kirtanshilodre](https://github.com/Kirtanshilodre)

⭐ Star this repo if it helped you!

</div>
