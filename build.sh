#!/usr/bin/env bash
# Build the Northlight OS APK from source.
# Needs JDK 17 and the Android SDK (platform 34, build-tools 34.0.0). On macOS:
#   brew install openjdk@17 && brew install --cask android-commandlinetools
#   sdkmanager "platforms;android-34" "build-tools;34.0.0"
# Optional: KEYSTORE_PROPERTIES=/path/keystore.properties ./build.sh  signs with your own release key
# (storeFile, storePassword, keyAlias, keyPassword); without it the APK is signed with your debug key.
set -eu
cd "$(dirname "$0")/launcher"
export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}"
export ANDROID_HOME="${ANDROID_HOME:-/opt/homebrew/share/android-commandlinetools}"
[ -f local.properties ] || echo "sdk.dir=$ANDROID_HOME" > local.properties
ARGS=()
[ -n "${KEYSTORE_PROPERTIES:-}" ] && ARGS+=("-PreleaseKeystoreProperties=$KEYSTORE_PROPERTIES")
./gradlew --no-daemon assembleRelease -q --console=plain "${ARGS[@]+"${ARGS[@]}"}"
cp app/build/outputs/apk/release/app-release.apk ../Northlight-OS-1.0.1.apk
echo "Built Northlight-OS-1.0.1.apk"
