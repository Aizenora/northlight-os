#!/usr/bin/env bash
# Northlight OS: install the launcher, make it Home, debloat, and turn on ad-blocking DNS, in one go.
# Usage:  ./northlight-os-setup.sh <tv ip>            (e.g. ./northlight-os-setup.sh 192.168.1.50)
#         NO_DNS=1 ./northlight-os-setup.sh <tv ip>   (skip the AdGuard Private DNS)
# Before: on the TV, Settings > Device Preferences > About > press OK on Build 7 times, then
#         Developer options > USB debugging ON. Afterwards switch USB debugging OFF again.
set -u
cd "$(dirname "$0")"
TV="${1:-}"; [ -n "$TV" ] || { echo "usage: $0 <tv ip>" >&2; exit 2; }
export TV_HOST="$TV"; unset TV_PORT
if [ ! -x venv/bin/python ]; then
  echo "== first run: setting up the ADB client (needs internet once)"
  python3 -m venv venv && venv/bin/pip install -q -r tools/requirements.txt || exit 1
fi
T() { venv/bin/python -I tools/tv.py "$@"; }
PKG=com.northlightos.launcher; ACT=$PKG/com.gathin.tvhome.MainActivity
APK=$(ls -t Northlight-OS-*.apk 2>/dev/null | head -1); [ -n "$APK" ] || { echo "no Northlight-OS-*.apk next to this script" >&2; exit 1; }

echo "== checking the TV"
T --probe || { echo "Cannot reach $TV:5555. Is the TV on and USB debugging enabled? (macOS: allow Terminal under Privacy & Security > Local Network)" >&2; exit 1; }
SDK=$(T 'getprop ro.build.version.sdk' | tr -d '\r\n')
[ "${SDK:-0}" -ge 26 ] 2>/dev/null || { echo "Android SDK $SDK is too old for Northlight OS (needs 26+)" >&2; exit 1; }

echo "== installing $APK"
venv/bin/python -I tools/b64push.py "$APK" /data/local/tmp/northlight-os.apk || exit 1
T 'pm install -r /data/local/tmp/northlight-os.apk; rm -f /data/local/tmp/northlight-os.apk' | tail -1
T "pm list packages $PKG" | grep -q "$PKG" || { echo "install failed" >&2; exit 1; }
T "am start -W -n $ACT" >/dev/null
T "cmd package compile -m speed -f $PKG" >/dev/null
T "pm grant $PKG android.permission.ACCESS_FINE_LOCATION" >/dev/null 2>&1

echo "== debloat (one package at a time)"
for p in $(grep -vE '^\s*(#|$)' debloat.txt); do
  if T "pm list packages -e $p" | grep -qx "package:$p"; then
    T "pm disable-user --user 0 $p" | tail -1 | sed "s|^|  $p: |"
  fi
done

if [ "${NO_DNS:-0}" != 1 ]; then
  echo "== ad-blocking DNS (undo: settings put global private_dns_mode opportunistic)"
  T 'settings put global private_dns_specifier dns.adguard-dns.com'
  T 'settings put global private_dns_mode hostname'
fi

echo "== checking Home"
T "am start -W -a android.intent.action.MAIN -c android.intent.category.HOME" >/dev/null
sleep 2
T 'dumpsys activity activities | grep -m1 mFocusedApp'
echo
echo "Done. Press Home on the remote. Then switch USB debugging OFF on the TV:"
echo "Settings > Device Preferences > Developer options > USB debugging."
