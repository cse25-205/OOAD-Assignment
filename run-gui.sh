#!/usr/bin/env bash
set -e

need_tools=0
for tool in Xvfb x11vnc websockify fluxbox; do command -v "$tool" >/dev/null 2>&1 || need_tools=1; done
fc-list 2>/dev/null | grep -qi dejavu || need_tools=1
if [ "$need_tools" = "1" ]; then
  echo ">> Installing the virtual desktop tools (first run only)..."

  sudo rm -f /etc/apt/sources.list.d/yarn.list
  sudo apt-get update || true
  sudo apt-get install -y xvfb x11vnc novnc websockify fluxbox fontconfig fonts-dejavu-core \
       libgtk-3-0 libgl1 libxtst6 libxxf86vm1
fi


export DISPLAY=:1
port_open() { (echo > "/dev/tcp/127.0.0.1/$1") >/dev/null 2>&1; }
wait_for() {  
  local what="$1"; shift
  for _ in $(seq 1 60); do "$@" && return 0; sleep 0.5; done
  echo "!! Timed out waiting for $what"; exit 1
}
screen_ready() { [ -S /tmp/.X11-unix/X1 ]; }

screen_ready || { Xvfb :1 -screen 0 1280x800x24 >/dev/null 2>&1 & }
wait_for "the virtual screen" screen_ready
sleep 1
pgrep -x fluxbox >/dev/null || { fluxbox >/dev/null 2>&1 & }
port_open 5901 || { x11vnc -display :1 -forever -shared -nopw -rfbport 5901 -quiet >/dev/null 2>&1 & }
wait_for "the VNC server (port 5901)" port_open 5901
port_open 6080 || { websockify --web /usr/share/novnc 6080 localhost:5901 >/dev/null 2>&1 & }
wait_for "the browser viewer (port 6080)" port_open 6080

echo ""
echo ">> Open the PORTS tab, click the globe icon next to port 6080,"
echo "   then click 'vnc.html' / 'Connect'. The bank window appears in that browser tab."
echo ""


export JAVA_TOOL_OPTIONS="-Dprism.order=sw"
${RUN_CMD:-mvn javafx:run}
