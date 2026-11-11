#!/usr/bin/env bash
# Undo Northlight OS: re-enable every package in debloat.txt, turn the DNS back to normal, remove the launcher.
# Usage: ./northlight-os-undo.sh <tv ip>      (USB debugging on). A factory reset also undoes everything.
set -u
cd "$(dirname "$0")"
TV="${1:-}"; [ -n "$TV" ] || { echo "usage: $0 <tv ip>" >&2; exit 2; }
export TV_HOST="$TV"; unset TV_PORT
[ -x venv/bin/python ] || { python3 -m venv venv && venv/bin/pip install -q -r tools/requirements.txt || exit 1; }
T() { venv/bin/python -I tools/tv.py "$@"; }
for p in $(grep -vE '^\s*(#|$)' debloat.txt); do T "pm enable $p" | tail -1 | sed "s|^|  $p: |"; done
T 'cmd package install-existing com.jamdeo.tv.vod' | tail -1
T 'settings put global private_dns_mode opportunistic'
T 'pm uninstall com.northlightos.launcher' | tail -1
echo "Done. The stock Hisense home is back. Switch USB debugging OFF on the TV."
