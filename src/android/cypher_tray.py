import sys
import threading
import socket
import winreg
import os
import ctypes
import pyautogui
from PIL import Image
import pystray

pyautogui.FAILSAFE = False

# ─────────────────────────────────────────────────────────────
# CONFIG
# ─────────────────────────────────────────────────────────────
PORT        = 3000
APP_NAME    = "CypherPC"
ICON_FILE   = os.path.join(os.path.dirname(sys.executable if getattr(sys, 'frozen', False) else __file__), "cypher_icon.png")

# ─────────────────────────────────────────────────────────────
# STATE
# ─────────────────────────────────────────────────────────────
server_socket   = None
server_running  = False
connected_client = None
tray_icon       = None

# ─────────────────────────────────────────────────────────────
# WIN32 POINT for fast mouse
# ─────────────────────────────────────────────────────────────
class POINT(ctypes.Structure):
    _fields_ = [("x", ctypes.c_long), ("y", ctypes.c_long)]

# ─────────────────────────────────────────────────────────────
# COMMAND HANDLER
# ─────────────────────────────────────────────────────────────
def handle_command(cmd: str):
    cmd = cmd.strip()
    if not cmd:
        return
    try:
        if cmd.startswith("M:"):
            parts = cmd.split(":")
            dx = int(float(parts[1]) * 1.8)
            dy = int(float(parts[2]) * 1.8)
            pt = POINT()
            ctypes.windll.user32.GetCursorPos(ctypes.byref(pt))
            ctypes.windll.user32.SetCursorPos(pt.x + dx, pt.y + dy)

        elif cmd == "C:L":
            pyautogui.click()
        elif cmd == "C:R":
            pyautogui.rightClick()
        elif cmd == "C:D":
            pyautogui.doubleClick()

        elif cmd.startswith("S:"):
            delta = int(cmd.split(":")[1])
            pyautogui.scroll(delta * 50)

        elif cmd.startswith("K:"):
            pyautogui.typewrite(cmd[2:], interval=0.02)

        elif cmd.startswith("K_RAW:"):
            key = cmd.split(":", 1)[1].lower()
            KEY_MAP = {
                "backspace":"backspace","enter":"enter","return":"enter",
                "escape":"esc","tab":"tab","space":"space",
                "up":"up","down":"down","left":"left","right":"right",
                "delete":"delete","home":"home","end":"end",
                "pageup":"pageup","pagedown":"pagedown",
                "f1":"f1","f2":"f2","f3":"f3","f4":"f4","f5":"f5",
                "f6":"f6","f7":"f7","f8":"f8","f9":"f9",
                "f10":"f10","f11":"f11","f12":"f12",
            }
            pyautogui.press(KEY_MAP.get(key, key))

        elif cmd.startswith("K_MOD:"):
            parts   = cmd.split(":", 2)
            mods    = parts[1].lower().split("+")
            key     = parts[2].lower()
            MOD_MAP = {"ctrl":"ctrl","alt":"alt","shift":"shift","win":"win","cmd":"win"}
            mapped  = [MOD_MAP.get(m, m) for m in mods if m]
            pyautogui.hotkey(*mapped, key)

        elif cmd.startswith("MEDIA:"):
            action    = cmd.split(":", 1)[1].lower()
            MEDIA_MAP = {
                "playpause":"playpause","nexttrack":"nexttrack",
                "prevtrack":"prevtrack","volumeup":"volumeup",
                "volumedown":"volumedown","volumemute":"volumemute",
            }
            if action in MEDIA_MAP:
                pyautogui.press(MEDIA_MAP[action])

    except Exception as e:
        print(f"[!] Command error '{cmd}': {e}")

# ─────────────────────────────────────────────────────────────
# TCP SERVER
# ─────────────────────────────────────────────────────────────
def handle_client(conn, addr):
    global connected_client
    connected_client = addr[0]
    update_tray_title(f"Cypher — Connected: {addr[0]}")
    buffer = ""
    try:
        while server_running:
            data = conn.recv(1024).decode("utf-8", errors="ignore")
            if not data:
                break
            buffer += data
            while "\n" in buffer:
                line, buffer = buffer.split("\n", 1)
                handle_command(line)
    except Exception:
        pass
    finally:
        conn.close()
        connected_client = None
        update_tray_title("Cypher — Waiting...")

def server_loop():
    global server_socket, server_running
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server_socket.bind(("0.0.0.0", PORT))
    server_socket.listen(5)
    server_socket.settimeout(1.0)
    update_tray_title("Cypher — Waiting...")
    while server_running:
        try:
            conn, addr = server_socket.accept()
            threading.Thread(target=handle_client, args=(conn, addr), daemon=True).start()
        except socket.timeout:
            continue
        except Exception:
            break
    try:
        server_socket.close()
    except Exception:
        pass

def start_server():
    global server_running
    if server_running:
        return
    server_running = True
    threading.Thread(target=server_loop, daemon=True).start()

def stop_server():
    global server_running, server_socket
    server_running = False
    try:
        server_socket.close()
    except Exception:
        pass
    update_tray_title("Cypher — Stopped")

# ─────────────────────────────────────────────────────────────
# WINDOWS AUTO-START (Registry)
# ─────────────────────────────────────────────────────────────
def set_autostart(enable: bool):
    key_path = r"Software\Microsoft\Windows\CurrentVersion\Run"
    exe_path = sys.executable if getattr(sys, 'frozen', False) else f'"{sys.executable}" "{os.path.abspath(__file__)}"'
    try:
        key = winreg.OpenKey(winreg.HKEY_CURRENT_USER, key_path, 0, winreg.KEY_SET_VALUE)
        if enable:
            winreg.SetValueEx(key, APP_NAME, 0, winreg.REG_SZ, exe_path)
        else:
            try:
                winreg.DeleteValue(key, APP_NAME)
            except FileNotFoundError:
                pass
        winreg.CloseKey(key)
        return True
    except Exception as e:
        print(f"[!] Autostart error: {e}")
        return False

def is_autostart_enabled() -> bool:
    key_path = r"Software\Microsoft\Windows\CurrentVersion\Run"
    try:
        key = winreg.OpenKey(winreg.HKEY_CURRENT_USER, key_path, 0, winreg.KEY_READ)
        winreg.QueryValueEx(key, APP_NAME)
        winreg.CloseKey(key)
        return True
    except FileNotFoundError:
        return False

def get_local_ip() -> str:
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        return "127.0.0.1"

# ─────────────────────────────────────────────────────────────
# TRAY ICON
# ─────────────────────────────────────────────────────────────
def update_tray_title(title: str):
    global tray_icon
    if tray_icon:
        tray_icon.title = title

def create_tray_icon():
    global tray_icon

    # Load icon
    try:
        icon_img = Image.open(ICON_FILE).resize((64, 64))
    except Exception:
        # Fallback — generate icon on the fly if file missing
        from PIL import ImageDraw
        import math
        SIZE = 64
        icon_img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
        d = ImageDraw.Draw(icon_img)
        d.ellipse([2, 2, SIZE-2, SIZE-2], fill=(15, 5, 25, 255))
        d.ellipse([2, 2, SIZE-2, SIZE-2], outline=(138, 43, 226, 255), width=4)
        d.arc([14, 12, SIZE-14, SIZE-12], start=40, end=320, fill=(180, 100, 255, 255), width=8)

    local_ip = get_local_ip()

    # ── Menu actions ──
    def on_start(icon, item):
        if not server_running:
            start_server()

    def on_stop(icon, item):
        if server_running:
            stop_server()

    def on_autostart_toggle(icon, item):
        currently = is_autostart_enabled()
        set_autostart(not currently)

    def on_exit(icon, item):
        stop_server()
        icon.stop()

    menu = pystray.Menu(
        pystray.MenuItem(f"IP: {local_ip}  |  Port: {PORT}", None, enabled=False),
        pystray.Menu.SEPARATOR,
        pystray.MenuItem("▶  Start Server",  on_start),
        pystray.MenuItem("■  Stop Server",   on_stop),
        pystray.Menu.SEPARATOR,
        pystray.MenuItem(
            "Auto-start with Windows",
            on_autostart_toggle,
            checked=lambda item: is_autostart_enabled()
        ),
        pystray.Menu.SEPARATOR,
        pystray.MenuItem("✕  Exit", on_exit),
    )

    tray_icon = pystray.Icon(APP_NAME, icon_img, "Cypher — Starting...", menu)
    return tray_icon

# ─────────────────────────────────────────────────────────────
# MAIN
# ─────────────────────────────────────────────────────────────
if __name__ == "__main__":
    start_server()
    icon = create_tray_icon()
    icon.run()
