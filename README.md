<div align="center">

<img src="https://img.shields.io/badge/CYPHER-REMOTE-9b30ff?style=for-the-badge&labelColor=04000a&color=9b30ff" />

# CYPHER REMOTE

### Control your PC from your Android phone — no cloud, no subscription, no BS.

[![Download](https://img.shields.io/badge/⬇%20Download-Website-9b30ff?style=for-the-badge&logo=android&logoColor=white)](https://kirtanshilodre.github.io/Cypher_Remote)
[![GitHub Releases](https://img.shields.io/github/v/release/Kirtanshilodre/Cypher_Remote?style=for-the-badge&color=9b30ff&label=Latest%20Release)](https://github.com/Kirtanshilodre/Cypher_Remote/releases)
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

## Features

| Feature | Description |
|---|---|
| 🖱️ **Trackpad** | Move mouse, left/right click, double click, scroll strip |
| ⌨️ **Keyboard** | Type text, special keys (Enter, Tab, Backspace, Escape, F-keys) |
| ⚡ **Shortcuts** | Custom shortcut profiles — Ctrl+C, Alt+Tab, Win+D and more |
| 🎵 **Media Controls** | Play/Pause, Next, Previous, Volume Up/Down, Mute |
| 📶 **WiFi + Bluetooth** | Connect over local WiFi or Bluetooth RFCOMM |
| 🔒 **100% Private** | No cloud, no internet required, direct TCP only |
| 🚀 **Auto Start** | CypherPC can launch automatically with Windows startup |
| 🎨 **Cypher UI** | Dark purple/black themed Android app built with Jetpack Compose |

---

## Downloads

| Platform | Download | Requirements |
|---|---|---|
| 🪟 Windows | [`CypherPC.zip`](https://github.com/Kirtanshilodre/Cypher_Remote/releases/download/v1.0/CypherPC.zip) | Windows 10 / 11 |
| 🍎 macOS | [`cypher_mac_app.zip`](https://github.com/Kirtanshilodre/Cypher_Remote/releases/download/v1.0/cypher_mac_app.zip) | macOS 11+ |
| 📱 Android | [`Cypher.apk`](https://github.com/Kirtanshilodre/Cypher_Remote/releases/download/v1.0/Cypher.apk) | Android 8.0+ |

👉 **[Full Download Page](https://kirtanshilodre.github.io/Cypher_Remote)**

---

## Quick Setup

### Step 1 — Same Network
Connect your PC and phone to the **same WiFi** (or use your phone's hotspot).

### Step 2 — Run the PC App

**Windows:**
```
Open CypherPC.exe → note the IP address and port (3000) shown on screen
Allow CypherPC through Windows Firewall when prompted
```

**macOS:**
```
Open cypher_mac_app → grant Accessibility permission when prompted (first time only)
Note the IP address shown on screen
```

### Step 3 — Connect from Phone
Open **Cypher Remote** on your Android → enter the IP address → tap **Connect**

✅ Done. You're in.

---

## Running from Source

**Windows / Linux:**
```bash
pip install pyautogui
python cypher_pc.py
```

Custom port:
```bash
python cypher_pc.py 9000
```

**macOS:**
```bash
pip3 install pyautogui
python3 cypher_mac_app.py
```
> First run: Go to **System Preferences → Privacy & Security → Accessibility** and allow the terminal / app.

---

## Tech Stack

| Component | Technology |
|---|---|
| Android App | Kotlin + Jetpack Compose |
| PC Receiver (Windows) | Python — `pyautogui`, `ctypes`, `tkinter` |
| Mac Receiver | Python — `pyautogui`, `tkinter` |
| Website | HTML / CSS / JS — GitHub Pages |
| Connection | Raw TCP Socket (WiFi) + Bluetooth RFCOMM |

---

## Command Protocol

The Android app sends plain-text TCP commands. The PC/Mac receiver parses and executes them:

| Command | Action |
|---|---|
| `M:dx:dy` | Move mouse by dx, dy |
| `C:L` | Left click |
| `C:R` | Right click |
| `C:D` | Double click |
| `S:delta` | Scroll (positive = up, negative = down) |
| `K:key` | Keyboard key press |
| `T:text` | Type text string |
| `MEDIA:action` | Media key (play, pause, next, prev, volup, voldown) |

---

## Project Structure

```
Cypher_Remote/
├── index.html            # Website (GitHub Pages)
├── cypher_pc.py          # Windows PC receiver — GUI + TCP server
├── cypher.py             # Standalone receiver (no GUI)
├── cypher_mac_app.zip    # macOS receiver app
├── Cypher.apk            # Android app (prebuilt)
└── README.md
```

---

## Requirements

**Windows PC:**
- Windows 10 or 11
- Python 3.8+ *(only if running `.py` directly — `.exe` is standalone)*

**macOS:**
- macOS 11 Big Sur or newer
- Python 3.8+
- Accessibility permission (one-time setup)

**Android Phone:**
- Android 8.0 or higher
- Enable *Install from Unknown Sources* for APK install

---

<div align="center">

Made with 💜 by [Kirtanshilodre](https://github.com/Kirtanshilodre)

⭐ Star this repo if it helped you!

</div>
