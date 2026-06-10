@echo off
echo Building Cypher PC App...

pip install pyinstaller pystray pillow pyautogui -q

pyinstaller --onefile --windowed --noconsole ^
  --name "CypherPC" ^
  --icon "cypher_icon.ico" ^
  --add-data "cypher_icon.png;." ^
  cypher_tray.py

echo.
echo Done! CypherPC.exe is in the dist folder.
pause
