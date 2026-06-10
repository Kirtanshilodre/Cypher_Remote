"""
CYPHER REMOTE — Mac GUI App
============================
Requirements:
    pip3 install pynput pyautogui

Run:
    python3 cypher_mac_app.py

Build .app (run on Mac):
    pip3 install py2app
    py2applet --make-setup cypher_mac_app.py
    python3 setup.py py2app -A
    # .app will be in dist/ folder
"""

import sys
import threading
import socket
import tkinter as tk

try:
    import pyautogui
    from pynput.mouse import Button, Controller as MouseController
    from pynput.keyboard import Key, Controller as KeyboardController
except ImportError:
    import tkinter.messagebox as mb
    root = tk.Tk(); root.withdraw()
    mb.showerror("Missing Libraries",
        "Run this in Terminal first:\n\npip3 install pynput pyautogui")
    sys.exit(1)

pyautogui.FAILSAFE = False
mouse    = MouseController()
keyboard = KeyboardController()

# ─────────────────────────────────────────────────────────────
# COLORS
# ─────────────────────────────────────────────────────────────
BG         = "#0a0010"
BG_CARD    = "#12001f"
PURPLE     = "#a020f0"
PURPLE_DIM = "#6a0dad"
PURPLE_LO  = "#2a0040"
TEXT       = "#f0e8ff"
TEXT_DIM   = "#b090d0"
GREEN      = "#00ff88"
RED        = "#ff3b3b"
GOLD       = "#ffd700"

# ─────────────────────────────────────────────────────────────
# KEY MAPS
# ─────────────────────────────────────────────────────────────
KEY_MAP = {
    "backspace":Key.backspace, "enter":Key.enter,   "return":Key.enter,
    "escape":Key.esc,          "tab":Key.tab,        "space":Key.space,
    "up":Key.up,               "down":Key.down,      "left":Key.left,
    "right":Key.right,         "delete":Key.delete,  "home":Key.home,
    "end":Key.end,             "pageup":Key.page_up, "pagedown":Key.page_down,
    "f1":Key.f1,"f2":Key.f2,"f3":Key.f3,"f4":Key.f4,
    "f5":Key.f5,"f6":Key.f6,"f7":Key.f7,"f8":Key.f8,
    "f9":Key.f9,"f10":Key.f10,"f11":Key.f11,"f12":Key.f12,
}
MOD_MAP = {
    "ctrl":Key.ctrl,"control":Key.ctrl,
    "alt":Key.alt,"option":Key.alt,
    "shift":Key.shift,
    "win":Key.cmd,"cmd":Key.cmd,"command":Key.cmd,
}
MEDIA_MAP = {
    "playpause":Key.media_play_pause, "nexttrack":Key.media_next,
    "prevtrack":Key.media_previous,   "volumeup":Key.media_volume_up,
    "volumedown":Key.media_volume_down,"volumemute":Key.media_volume_mute,
}

# ─────────────────────────────────────────────────────────────
# COMMAND HANDLER
# ─────────────────────────────────────────────────────────────
def handle_command(cmd: str):
    cmd = cmd.strip()
    if not cmd: return
    try:
        if cmd.startswith("M:"):
            parts = cmd.split(":")
            mouse.move(float(parts[1]) * 1.8, float(parts[2]) * 1.8)

        elif cmd == "C:L": mouse.click(Button.left)
        elif cmd == "C:R": mouse.click(Button.right)
        elif cmd == "C:D": mouse.click(Button.left, 2)

        elif cmd.startswith("S:"):
            mouse.scroll(0, int(cmd.split(":")[1]) * 3)

        elif cmd.startswith("K:"):
            keyboard.type(cmd[2:])

        elif cmd.startswith("K_RAW:"):
            key = cmd.split(":",1)[1].lower()
            k   = KEY_MAP.get(key, key)
            keyboard.press(k); keyboard.release(k)

        elif cmd.startswith("K_MOD:"):
            parts = cmd.split(":",2)
            mods  = [MOD_MAP.get(m.lower()) for m in parts[1].split("+") if MOD_MAP.get(m.lower())]
            key   = KEY_MAP.get(parts[2].lower(), parts[2].lower())
            for m in mods: keyboard.press(m)
            keyboard.press(key); keyboard.release(key)
            for m in reversed(mods): keyboard.release(m)

        elif cmd.startswith("MEDIA:"):
            k = MEDIA_MAP.get(cmd.split(":",1)[1].lower())
            if k: keyboard.press(k); keyboard.release(k)

    except Exception as e:
        print(f"[!] {cmd}: {e}")

# ─────────────────────────────────────────────────────────────
# SERVER
# ─────────────────────────────────────────────────────────────
PORT           = 3000
server_socket  = None
server_running = False
command_count  = 0

def handle_client(conn, addr, app):
    global command_count
    command_count = 0
    app.on_connected(addr[0])
    buffer = ""
    try:
        while server_running:
            data = conn.recv(1024).decode("utf-8", errors="ignore")
            if not data: break
            buffer += data
            while "\n" in buffer:
                line, buffer = buffer.split("\n", 1)
                handle_command(line)
                command_count += 1
                app.on_command(command_count)
    except Exception: pass
    finally:
        conn.close()
        app.on_disconnected()

def server_loop(app):
    global server_socket, server_running
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server_socket.bind(("0.0.0.0", PORT))
    server_socket.listen(5)
    server_socket.settimeout(1.0)
    app.on_waiting()
    while server_running:
        try:
            conn, addr = server_socket.accept()
            threading.Thread(target=handle_client, args=(conn, addr, app), daemon=True).start()
        except socket.timeout: continue
        except Exception: break
    try: server_socket.close()
    except Exception: pass

def get_local_ip():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80)); ip = s.getsockname()[0]; s.close()
        return ip
    except: return "127.0.0.1"

# ─────────────────────────────────────────────────────────────
# GUI
# ─────────────────────────────────────────────────────────────
class CypherMacApp:
    def __init__(self, root):
        self.root    = root
        self.running = False
        self.local_ip = get_local_ip()
        self._build_ui()

    def _build_ui(self):
        r = self.root
        r.title("Cypher PC")
        r.geometry("360x500")
        r.resizable(False, False)
        r.configure(bg=BG)
        r.protocol("WM_DELETE_WINDOW", self._on_close)

        # Header
        tk.Frame(r, bg=BG, pady=20).pack(fill="x")
        tk.Label(r, text="⬡", font=("Menlo", 34), bg=BG, fg=PURPLE).pack()
        tk.Label(r, text="C Y P H E R", font=("Menlo", 20, "bold"), bg=BG, fg=TEXT).pack()
        tk.Label(r, text="MAC REMOTE SERVER", font=("Menlo", 9), bg=BG, fg=TEXT_DIM).pack()
        tk.Label(r, text="macOS", font=("Menlo", 8), bg=BG, fg=PURPLE_DIM).pack(pady=(2,0))

        tk.Frame(r, bg=PURPLE_DIM, height=1).pack(fill="x", padx=20, pady=12)

        # Card
        card = tk.Frame(r, bg=BG_CARD, padx=20, pady=16,
                        highlightbackground=PURPLE_DIM, highlightthickness=1)
        card.pack(fill="x", padx=20, pady=(0,12))

        # Status
        sr = tk.Frame(card, bg=BG_CARD); sr.pack(fill="x", pady=(0,12))
        tk.Label(sr, text="STATUS", font=("Menlo",8), bg=BG_CARD, fg=TEXT_DIM).pack(side="left")
        self.status_dot = tk.Label(sr, text="●", font=("Menlo",14), bg=BG_CARD, fg=TEXT_DIM)
        self.status_dot.pack(side="right")
        self.status_lbl = tk.Label(sr, text="OFFLINE", font=("Menlo",11,"bold"), bg=BG_CARD, fg=TEXT_DIM)
        self.status_lbl.pack(side="right", padx=6)

        # IP
        tk.Label(card, text="IP ADDRESS", font=("Menlo",8), bg=BG_CARD, fg=TEXT_DIM).pack(anchor="w", pady=(8,2))
        tk.Label(card, text=self.local_ip, font=("Menlo",13,"bold"), bg=BG_CARD, fg=TEXT).pack(anchor="w")

        # Port
        tk.Label(card, text="PORT", font=("Menlo",8), bg=BG_CARD, fg=TEXT_DIM).pack(anchor="w", pady=(8,2))
        tk.Label(card, text=str(PORT), font=("Menlo",13,"bold"), bg=BG_CARD, fg=PURPLE).pack(anchor="w")

        # Device
        tk.Label(card, text="CONNECTED DEVICE", font=("Menlo",8), bg=BG_CARD, fg=TEXT_DIM).pack(anchor="w", pady=(10,2))
        self.device_lbl = tk.Label(card, text="—", font=("Menlo",11), bg=BG_CARD, fg=TEXT_DIM)
        self.device_lbl.pack(anchor="w")

        # Commands
        tk.Frame(card, bg=PURPLE_LO, height=1).pack(fill="x", pady=10)
        cr = tk.Frame(card, bg=BG_CARD); cr.pack(fill="x")
        tk.Label(cr, text="COMMANDS RECEIVED", font=("Menlo",8), bg=BG_CARD, fg=TEXT_DIM).pack(side="left")
        self.cmd_lbl = tk.Label(cr, text="0", font=("Menlo",11,"bold"), bg=BG_CARD, fg=PURPLE)
        self.cmd_lbl.pack(side="right")

        # Button
        self.main_btn = tk.Button(
            r, text="▶  START SERVER",
            font=("Menlo", 12, "bold"),
            bg=PURPLE, fg="white",
            activebackground=PURPLE_DIM, activeforeground="white",
            relief="flat", bd=0, padx=20, pady=14,
            cursor="pointinghand", command=self._toggle
        )
        self.main_btn.pack(fill="x", padx=20, pady=(0,10))

        # Footer
        tk.Frame(r, bg=PURPLE_DIM, height=1).pack(fill="x", padx=20, pady=10)
        tk.Label(r, text="Keep this window open while using the mobile app.",
                 font=("Menlo",8), bg=BG, fg=TEXT_DIM, wraplength=300).pack()

    def _toggle(self):
        if self.running: self._stop()
        else: self._start()

    def _start(self):
        global server_running
        server_running = True; self.running = True
        threading.Thread(target=server_loop, args=(self,), daemon=True).start()
        self.main_btn.config(text="■  STOP SERVER", bg=RED)

    def _stop(self):
        global server_running, server_socket
        server_running = False; self.running = False
        try: server_socket.close()
        except: pass
        self.main_btn.config(text="▶  START SERVER", bg=PURPLE)
        self._set_status("OFFLINE", TEXT_DIM)
        self.device_lbl.config(text="—", fg=TEXT_DIM)
        self.cmd_lbl.config(text="0")

    def _set_status(self, text, color):
        self.status_lbl.config(text=text, fg=color)
        self.status_dot.config(fg=color)

    def on_waiting(self):
        self.root.after(0, lambda: self._set_status("WAITING...", GOLD))

    def on_connected(self, ip):
        self.root.after(0, lambda: [
            self._set_status("CONNECTED", GREEN),
            self.device_lbl.config(text=ip, fg=GREEN),
        ])

    def on_disconnected(self):
        self.root.after(0, lambda: [
            self._set_status("WAITING...", GOLD),
            self.device_lbl.config(text="—", fg=TEXT_DIM),
            self.cmd_lbl.config(text="0"),
        ])

    def on_command(self, count):
        self.root.after(0, lambda: self.cmd_lbl.config(text=str(count)))

    def _on_close(self):
        self._stop(); self.root.destroy()

if __name__ == "__main__":
    root = tk.Tk()
    app  = CypherMacApp(root)
    app._start()
    root.mainloop()
