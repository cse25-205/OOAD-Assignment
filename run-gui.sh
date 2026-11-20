#!/usr/bin/env bash
# Runs the JavaFX Banking System in GitHub Codespaces.
# It starts a virtual desktop and shows it in your browser on port 6080.
# Usage:  bash run-gui.sh
set -e

# 1. Install the desktop tools once (about a minute)
if ! command -v Xvfb >/dev/null 2>&1 || ! command -v x11vnc >/dev/null 2>&1 || ! command -v websockify >/dev/null 2>&1; then
  echo ">> Installing the virtual desktop tools (first run only)..."
  sudo apt-get update
  sudo apt-get install -y xvfb x11vnc novnc websockify fluxbox libgtk-3-0 libgl1 libxtst6 libxxf86vm1
fi

# 2. Start the virtual screen, a simple window manager, and the browser viewer
export DISPLAY=:1
pgrep -x Xvfb >/dev/null || { Xvfb :1 -screen 0 1280x800x24 >/dev/null 2>&1 & }
sleep 1
pgrep -x fluxbox >/dev/null || { fluxbox >/dev/null 2>&1 & }
port_open() { (echo > "/dev/tcp/127.0.0.1/$1") >/dev/null 2>&1; }
port_open 5901 || { x11vnc -display :1 -forever -shared -nopw -rfbport 5901 -quiet >/dev/null 2>&1 & }
sleep 1
port_open 6080 || { websockify --web /usr/share/novnc 6080 localhost:5901 >/dev/null 2>&1 & }
sleep 2

echo ""
echo ">> Open the PORTS tab, click the globe icon next to port 6080,"
echo "   then click 'vnc.html' / 'Connect'. The bank window appears in that browser tab."
echo ""

# 3. Start the application (software rendering works without a graphics card)
export JAVA_TOOL_OPTIONS="-Dprism.order=sw"
${RUN_CMD:-mvn javafx:run}
