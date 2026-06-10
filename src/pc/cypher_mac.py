"""
CYPHER REMOTE — Mac Receiver
=============================
Requirements:
    pip3 install pynput pyautogui

Run:
    python3 cypher_mac.py
    python3 cypher_mac.py 9000   (custom port)

To build .app (on Mac):
    pip3 install py2app
    py2applet --make-setup cypher_mac.py
    python3 setup.py py2app
"""

import sys
import threading
import socket

try:
    import pyautogui
    from pynput.mouse import Button, Controller as MouseController
    from pynput.keyboard import Key, Controller as KeyboardController
except ImportError:
    print("\n[!] Missing libraries. Run this first:")
    print("    pip3 install pynput pyautogui\n")
    sys.exit(1)

pyautogui.FAILSAFE = False

mouse    = MouseController()
keyboard = KeyboardController()

# ─────────────────────────────────────────────────────────────
# KEY MAPS
# ─────────────────────────────────────────────────────────────
KEY_MAP = {
    "backspace": Key.backspace,  "enter": Key.enter,    "return": Key.enter,
    "escape":    Key.esc,        "tab":   Key.tab,      "space":  Key.space,
    "up":        Key.up,         "down":  Key.down,     "left":   Key.left,
    "right":     Key.right,      "delete":Key.delete,   "home":   Key.home,
    "end":       Key.end,        "pageup":Key.page_up,  "pagedown":Key.page_down,
    "f1":Key.f1, "f2":Key.f2,   "f3":Key.f3,  "f4":Key.f4,
    "f5":Key.f5, "f6":Key.f6,   "f7":Key.f7,  "f8":Key.f8,
    "f9":Key.f9, "f10":Key.f10, "f11":Key.f11,"f12":Key.f12,
}

MOD_MAP = {
    "ctrl":  Key.ctrl,   "control": Key.ctrl,
    "alt":   Key.alt,    "option":  Key.alt,
    "shift": Key.shift,
    "win":   Key.cmd,    "cmd":     Key.cmd,   "command": Key.cmd,
}

MEDIA_MAP = {
    "playpause":  Key.media_play_pause,
    "nexttrack":  Key.media_next,
    "prevtrack":  Key.media_previous,
    "volumeup":   Key.media_volume_up,
    "volumedown": Key.media_volume_down,
    "volumemute": Key.media_volume_mute,
}

# ─────────────────────────────────────────────────────────────
# COMMAND HANDLER
# ─────────────────────────────────────────────────────────────
def handle_command(cmd: str):
    cmd = cmd.strip()
    if not cmd:
        return
    try:
        # ── Mouse Move ──
        if cmd.startswith("M:"):
            parts = cmd.split(":")
            dx = float(parts[1]) * 1.8
            dy = float(parts[2]) * 1.8
            x, y = mouse.position
            mouse.move(dx, dy)

        # ── Mouse Clicks ──
        elif cmd == "C:L":
            mouse.click(Button.left)
        elif cmd == "C:R":
            mouse.click(Button.right)
        elif cmd == "C:D":
            mouse.click(Button.left, 2)

        # ── Scroll ──
        elif cmd.startswith("S:"):
            delta = int(cmd.split(":")[1])
            mouse.scroll(0, delta * 3)

        # ── Type Text ──
        elif cmd.startswith("K:"):
            text = cmd[2:]
            keyboard.type(text)

        # ── Special Keys ──
        elif cmd.startswith("K_RAW:"):
            key = cmd.split(":", 1)[1].lower()
            k   = KEY_MAP.get(key)
            if k:
                keyboard.press(k)
                keyboard.release(k)
            else:
                # Single character
                keyboard.press(key)
                keyboard.release(key)

        # ── Modifier Combos ──
        elif cmd.startswith("K_MOD:"):
            parts = cmd.split(":", 2)
            mods  = [MOD_MAP.get(m.lower()) for m in parts[1].split("+") if MOD_MAP.get(m.lower())]
            key   = KEY_MAP.get(parts[2].lower()) or parts[2].lower()
            for mod in mods:
                keyboard.press(mod)
            keyboard.press(key)
            keyboard.release(key)
            for mod in reversed(mods):
                keyboard.release(mod)

        # ── Media Keys ──
        elif cmd.startswith("MEDIA:"):
            action = cmd.split(":", 1)[1].lower()
            k = MEDIA_MAP.get(action)
            if k:
                keyboard.press(k)
                keyboard.release(k)

    except Exception as e:
        print(f"[!] Command error '{cmd}': {e}")

# ─────────────────────────────────────────────────────────────
# TCP SERVER
# ─────────────────────────────────────────────────────────────
def handle_client(conn, addr):
    print(f"[+] Connected: {addr[0]}")
    buffer = ""
    try:
        while True:
            data = conn.recv(1024).decode("utf-8", errors="ignore")
            if not data:
                break
            buffer += data
            while "\n" in buffer:
                line, buffer = buffer.split("\n", 1)
                handle_command(line)
    except Exception as e:
        print(f"[!] Client error: {e}")
    finally:
        conn.close()
        print(f"[-] Disconnected: {addr[0]}")

def get_local_ip():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        return "127.0.0.1"

def run_receiver(port: int = 3000):
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind(("0.0.0.0", port))
    server.listen(5)

    print(f"\n{'='*40}")
    print(f"  CYPHER PC RECEIVER — MAC")
    print(f"  IP   : {get_local_ip()}")
    print(f"  Port : {port}")
    print(f"  Enter this IP in the Android app.")
    print(f"{'='*40}\n")
    print("Waiting for connection...\n")

    while True:
        try:
            conn, addr = server.accept()
            threading.Thread(target=handle_client, args=(conn, addr), daemon=True).start()
        except KeyboardInterrupt:
            print("\n[Receiver] Stopped.")
            server.close()
            break

if __name__ == "__main__":
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 3000
    run_receiver(port)
