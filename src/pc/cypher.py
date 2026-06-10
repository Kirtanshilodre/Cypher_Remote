import sys
import threading
import pyautogui
import ctypes

pyautogui.FAILSAFE = False

# ─────────────────────────────────────────────────────────────
# Win32 POINT struct for fast mouse movement
# ─────────────────────────────────────────────────────────────
class POINT(ctypes.Structure):
    _fields_ = [("x", ctypes.c_long), ("y", ctypes.c_long)]


# ─────────────────────────────────────────────────────────────
# COMMAND HANDLER
# Protocol:
#   M:dx:dy          → mouse move           
#   C:L              → left click
#   C:R              → right click
#   C:D              → double click
#   S:delta          → scroll wheel
#   K:text           → type characters
#   K_RAW:key        → special key (backspace, enter, etc.)
#   K_MOD:mods:key   → modifier combo (ctrl+c, alt+tab, etc.)
#   MEDIA:action     → media key (playpause, volumeup, etc.)
# ─────────────────────────────────────────────────────────────
def handle_command(cmd: str):
    cmd = cmd.strip()
    if not cmd:
        return

    try:
        # ── Mouse Move ──
        if cmd.startswith("M:"):
            parts = cmd.split(":")
            dx = int(float(parts[1]) * 1.8)  # sensitivity — tune as needed
            dy = int(float(parts[2]) * 1.8)
            pt = POINT()
            ctypes.windll.user32.GetCursorPos(ctypes.byref(pt))
            ctypes.windll.user32.SetCursorPos(pt.x + dx, pt.y + dy)

        # ── Mouse Clicks ──
        elif cmd == "C:L":
            pyautogui.click()
        elif cmd == "C:R":
            pyautogui.rightClick()
        elif cmd == "C:D":
            pyautogui.doubleClick()

        # ── Scroll ──
        elif cmd.startswith("S:"):
            delta = int(cmd.split(":")[1])
            pyautogui.scroll(delta * 50)  # multiplier — tune as needed

        # ── Type Text ──
        elif cmd.startswith("K:"):
            text = cmd[2:]
            pyautogui.typewrite(text, interval=0.02)

        # ── Special Keys ──
        elif cmd.startswith("K_RAW:"):
            key = cmd.split(":", 1)[1].lower()
            KEY_MAP = {
                "backspace": "backspace", "enter": "enter", "return": "enter",
                "escape": "esc",          "tab": "tab",     "space": "space",
                "up": "up",               "down": "down",   "left": "left",   "right": "right",
                "delete": "delete",       "home": "home",   "end": "end",
                "pageup": "pageup",       "pagedown": "pagedown",
                "f1": "f1",  "f2": "f2",  "f3": "f3",  "f4": "f4",
                "f5": "f5",  "f6": "f6",  "f7": "f7",  "f8": "f8",
                "f9": "f9",  "f10": "f10","f11": "f11","f12": "f12",
            }
            pyautogui.press(KEY_MAP.get(key, key))

        # ── Modifier Combos ──
        elif cmd.startswith("K_MOD:"):
            parts = cmd.split(":", 2)
            mods = parts[1].lower().split("+")
            key  = parts[2].lower()
            MOD_MAP = {"ctrl": "ctrl", "alt": "alt", "shift": "shift", "win": "win", "cmd": "win"}
            mapped = [MOD_MAP.get(m, m) for m in mods if m]
            pyautogui.hotkey(*mapped, key)

        # ── Media Keys ──
        elif cmd.startswith("MEDIA:"):
            action = cmd.split(":", 1)[1].lower()
            MEDIA_MAP = {
                "playpause":  "playpause",
                "nexttrack":  "nexttrack",
                "prevtrack":  "prevtrack",
                "volumeup":   "volumeup",
                "volumedown": "volumedown",
                "volumemute": "volumemute",
            }
            if action in MEDIA_MAP:
                pyautogui.press(MEDIA_MAP[action])

    except Exception as e:
        print(f"[!] Command error '{cmd}': {e}")


# ─────────────────────────────────────────────────────────────
# TCP RECEIVER SERVER
# ─────────────────────────────────────────────────────────────
def run_receiver(port: int = 3000):
    import socket as sock

    server = sock.socket(sock.AF_INET, sock.SOCK_STREAM)
    server.setsockopt(sock.SOL_SOCKET, sock.SO_REUSEADDR, 1)
    server.bind(("0.0.0.0", port))
    server.listen(5)

    # Get local IP
    try:
        s = sock.socket(sock.AF_INET, sock.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        local_ip = s.getsockname()[0]
        s.close()
    except Exception:
        local_ip = "127.0.0.1"

    print(f"\n{'='*40}")
    print(f"  CYPHER — ACTIVE")
    print(f"  IP   : {local_ip}")
    print(f"  Port : {port}")
    print(f"  Enter this IP in the Android app.")
    print(f"{'='*40}\n")
    print("Waiting for connection...\n")

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

    while True:
        try:
            conn, addr = server.accept()
            threading.Thread(target=handle_client, args=(conn, addr), daemon=True).start()
        except KeyboardInterrupt:
            print("\n[Receiver] Stopped.")
            server.close()
            break


# ─────────────────────────────────────────────────────────────
# ENTRY POINT
# Usage:
#   python cypher.py            → default port 3000
#   python cypher.py 9999       → custom port
# ─────────────────────────────────────────────────────────────
if __name__ == "__main__":
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 3000
    run_receiver(port)
