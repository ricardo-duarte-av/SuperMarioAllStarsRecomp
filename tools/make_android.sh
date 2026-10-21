#!/usr/bin/env bash
# Build the signed Android release: dist/SuperMarioAllStars-Android.apk (copied to ~/Downloads).
#
# Needs: the Android SDK + NDK 28.2 (~/Library/Android/sdk), JDK 17, and the release signing key in
# ~/.smas-android-signing/key.properties (storeFile, storePassword, keyAlias, keyPassword), or a path in
# $SMAS_ANDROID_SIGNING. Keep that folder backed up: Android only installs an update signed with the
# SAME key, so a lost key means every player has to uninstall (and lose their saves) to update.
# The APK contains no ROM: tools/release_scan.py checks its contents before it is copied anywhere.
set -euo pipefail
cd "$(dirname "$0")/.."
ROOT="$PWD"
unset SDKROOT   # the macOS SDK pin for desktop builds must not leak into the NDK build
export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17}"
SIGNING="${SMAS_ANDROID_SIGNING:-$HOME/.smas-android-signing/key.properties}"
[ -f "$SIGNING" ] || { echo "FAIL: no signing key at $SIGNING (see the top of this script)" >&2; exit 1; }
[ -f android/local.properties ] || printf 'sdk.dir=%s\n' "$HOME/Library/Android/sdk" > android/local.properties

( cd android && ./gradlew -q assembleRelease )
APK=android/app/build/outputs/apk/release/app-release.apk
[ -f "$APK" ] || { echo "FAIL: $APK was not built" >&2; exit 1; }

BT=$(ls -d "$HOME"/Library/Android/sdk/build-tools/* | sort -V | tail -1)
"$BT/apksigner" verify --print-certs "$APK" | grep -q "Tanooki Studios" \
    || { echo "FAIL: the APK is not signed with the Tanooki Studios release key" >&2; exit 1; }

TMP=$(mktemp -d); trap 'rm -rf "$TMP"' EXIT
unzip -q "$APK" -d "$TMP/apk"
python3 -I tools/release_scan.py "$TMP/apk"

mkdir -p dist
cp -f "$APK" dist/SuperMarioAllStars-Android.apk
cp -f dist/SuperMarioAllStars-Android.apk "$HOME/Downloads/"
echo "$HOME/Downloads/SuperMarioAllStars-Android.apk"
