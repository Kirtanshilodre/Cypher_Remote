# CYPHER REMOTE 🎮

> Control your Windows PC from your Android phone — mouse, keyboard, media keys and more. No cloud. No subscription. Just works.

**[⬇ Download](https://kirtanshilodre.github.io/Cypher_Remote)** &nbsp;•&nbsp; **[🌐 Website](https://kirtanshilodre.github.io/Cypher_Remote)**

---

## What is Cypher Remote?

Cypher Remote is a local network PC remote control system. It lets you control your Windows PC from your Android phone using a direct TCP connection — no internet required, no data sent to any server.

---

## Downloads

| Platform | File | Requirements |
|---|---|---|
| Windows PC | `CypherPC.zip` | Windows 10/11 |
| Android Phone | `Cypher.apk` | Android 8+ |
| macOS | `cypher_mac_app.zip` | macOS 11+ |


👉 **[Go to Downloads](https://kirtanshilodre.github.io/Cypher_Remote/index.html#download)**

---

## Features

- 🖱️ **Trackpad** — Move mouse, left/right click, two-finger scroll
- ⌨️ **Keyboard** — Type text, special keys, shortcuts (Ctrl+C, Alt+Tab, etc.)
- 🎵 **Media Keys** — Play/pause, volume, next/previous track
- ⚡ **Low Latency** — Direct TCP over local network, near-instant response
- 🔒 **Private** — No cloud, no internet required, everything stays local
- 🚀 **Auto Start** — CypherPC can launch automatically with Windows

---

## Setup Guide

### Step 1 — Same Network
Connect both your PC and phone to the same WiFi or hotspot.

### Step 2 — Run CypherPC on PC
Open `CypherPC.exe` on your Windows PC. A window will show your **IP address** and **port (3000)**.

> Allow CypherPC through Windows Firewall when prompted.

### Step 3 — Connect from Phone
Open the **Cypher Remote** app on your Android phone. Enter the IP address shown in CypherPC, set port to `3000`, and tap **Connect**.

✅ Done — you're connected!

---

## How It Works

```
Android App  ──TCP──▶  CypherPC.exe (Port 3000)
                              │
                              ▼
                     Mouse/Keyboard control via Windows API
```

The Android app sends commands over a direct TCP socket connection to the PC. The PC receiver translates those commands into actual mouse movements, keyboard inputs, and media key presses using Windows APIs.

---

## Tech Stack

| Part | Technology |
|---|---|
| Android App | Kotlin + Jetpack Compose |
| PC Receiver | Python (pyautogui, ctypes) |
| PC GUI | Python (tkinter) |
| Website | HTML/CSS/JS — GitHub Pages |

---

## Requirements

**PC:**
- Windows 10 or 11
- Python 3.8+ (if running `.py` directly)

**Phone:**
- Android 8.0 or higher
- Allow installation from unknown sources (for APK)

**Mac:**
- macOS 11+
- Python 3.8+
- Allow accessibility permissions (first time only)

---

## Running from Source

```bash
pip install pyautogui
python cypher_pc.py
```

Custom port:
```bash
python cypher_pc.py 9000
```

**Mac:**
```bash
python3 cypher_mac_app.py
```

---

## Project Structure

```
Cypher_Remote/
├── cypher_pc.py        # PC GUI app + receiver server
├── cypher.py           # Standalone receiver (no GUI)
├── Cypher.html         # Website (GitHub Pages)
|── Cypher.apk          # Android app
└── cypher_mac_app.zip  # macOS app
```

---

<p align="center">Made by <a href="https://github.com/Kirtanshilodre">Kirtanshilodre</a></p>
