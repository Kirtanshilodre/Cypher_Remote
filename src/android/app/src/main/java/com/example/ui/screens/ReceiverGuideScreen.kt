package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReceiverGuideScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val pythonScriptContent = """# import sys
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
            pyautogui.scroll(delta * 100)  # multiplier — tune as needed

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
def run_receiver(port: int = 8000):
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
"""

    fun copyToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("PC Receiver Script", pythonScriptContent)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Python script copied! Send to PC and run.", Toast.LENGTH_LONG).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Guide Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = null,
                tint = Color(0xFF00E676),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "PC Receiver Companion",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Set up the lightweight background desktop script",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Step 1: Install packages
        Text(
            "Step 1: Install Desktop Dependencies",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Open your computer's terminal (PowerShell, Command Prompt, or Linux bash) and install the native control dependencies:",
            color = Color.Gray,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF141419))
                .border(1.dp, Color(0xFF2E2E38), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                "pip install pyautogui pynput",
                color = Color(0xFFDCDCE6),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Step 2: Grab the python script
        Text(
            "Step 2: Grab the Companion Script",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Save the following code as a python file (e.g. pc_receiver.py) on your computer. Tap below to copy the full code instantly:",
            color = Color.Gray,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { copyToClipboard() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("copy_script_button")
        ) {
            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Copy Python Receiver Code", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Read-only Code View Board
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F12)),
            border = BorderStroke(1.dp, Color(0xFF2E2E38)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("pc_receiver.py", color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    Icon(Icons.Default.Code, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = pythonScriptContent,
                        color = Color(0xFF81C784),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Step 3: Run and connect
        Text(
            "Step 3: Launch Companion and Connect",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Start the receiver by typing: python pc_receiver.py inside your computer prompt.\n\n" +
            "If connecting over Bluetooth, ensure your PC is paired with this Android device under standard OS settings. Open the app's 'Connect' tab and click your PC from paired listings.\n\n" +
            "If using Wi-Fi local networking fallback, verify both devices are on the same local network, enter the IP address printed on your PC console into the app's 'Wi-Fi' section, and hit Connect!",
            color = Color.Gray,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}
