#!/usr/bin/env bash
# Build the signed Android release: dist/SuperMarioAllStars-Android.apk (copied to ~/Downloads).
#
# Needs: the Android SDK (found through $ANDROID_HOME, $ANDROID_SDK_ROOT, ~/Library/Android/sdk,
# ~/Android/Sdk or ~/android-sdk; Gradle installs NDK 28.2 and CMake 3.22.1 into it if they are
# missing), JDK 17 or newer, src/gen/ from tools/regen.sh, and a release signing key in
# ~/.smas-android-signing/key.properties (storeFile, storePassword, keyAlias, keyPassword), or a path
# in $SMAS_ANDROID_SIGNING. With no key there, this script makes one for you the first time.
# Keep that folder backed up: Android only installs an update signed with the SAME key, so a lost key
# means uninstalling (and losing the saves) to update. Set SMAS_EXPECT_SIGNER (e.g. "Tanooki Studios")
# to refuse an APK signed by anyone else.
# The APK contains no ROM: tools/release_scan.py checks its contents before it is copied anywhere.
set -euo pipefail
cd "$(dirname "$0")/.."
ROOT="$PWD"
unset SDKROOT   # the macOS SDK pin for desktop builds must not leak into the NDK build
if [ -z "${JAVA_HOME:-}" ] && [ -d /opt/homebrew/opt/openjdk@17 ]; then export JAVA_HOME=/opt/homebrew/opt/openjdk@17; fi
KEYTOOL=keytool; [ -n "${JAVA_HOME:-}" ] && KEYTOOL="$JAVA_HOME/bin/keytool"

SDK=""
for d in "${ANDROID_HOME:-}" "${ANDROID_SDK_ROOT:-}" "$HOME/Library/Android/sdk" "$HOME/Android/Sdk" "$HOME/android-sdk"; do
    [ -n "$d" ] && [ -d "$d/platforms" ] && { SDK="$d"; break; }
done
[ -n "$SDK" ] || { echo "FAIL: no Android SDK found (set ANDROID_HOME; see the top of this script)" >&2; exit 1; }
[ -f android/local.properties ] || printf 'sdk.dir=%s\n' "$SDK" > android/local.properties
[ -d src/gen ] || { echo "FAIL: src/gen/ is missing: run tools/regen.sh first" >&2; exit 1; }

SIGNING="${SMAS_ANDROID_SIGNING:-$HOME/.smas-android-signing/key.properties}"
if [ ! -f "$SIGNING" ]; then
    echo "No signing key at $SIGNING: making one (back that folder up)."
    KEYDIR="$(dirname "$SIGNING")"; mkdir -p "$KEYDIR"; chmod 700 "$KEYDIR"
    PASS=$(od -An -N18 -tx1 /dev/urandom | tr -d ' \n')
    "$KEYTOOL" -genkeypair -keystore "$KEYDIR/smas-release.jks" -storetype PKCS12 -alias smas \
        -keyalg RSA -keysize 4096 -validity 36500 -storepass "$PASS" -keypass "$PASS" \
        -dname "CN=Super Mario All-Stars (self-built)" >/dev/null
    printf 'storeFile=%s\nstorePassword=%s\nkeyAlias=smas\nkeyPassword=%s\n' \
        "$KEYDIR/smas-release.jks" "$PASS" "$PASS" > "$SIGNING"
    chmod 600 "$SIGNING" "$KEYDIR/smas-release.jks"
fi
export SMAS_ANDROID_SIGNING="$SIGNING"

( cd android && ./gradlew -q assembleRelease )
APK=android/app/build/outputs/apk/release/app-release.apk
[ -f "$APK" ] || { echo "FAIL: $APK was not built" >&2; exit 1; }

BT=$(ls -d "$SDK"/build-tools/* | sort -V | tail -1)
CERTS=$("$BT/apksigner" verify --print-certs "$APK") || { echo "FAIL: the APK is not signed" >&2; exit 1; }
echo "$CERTS" | grep -m1 "certificate DN"
if [ -n "${SMAS_EXPECT_SIGNER:-}" ] && ! grep -q "$SMAS_EXPECT_SIGNER" <<<"$CERTS"; then
    echo "FAIL: the APK is not signed by $SMAS_EXPECT_SIGNER" >&2; exit 1
fi

TMP=$(mktemp -d); trap 'rm -rf "$TMP"' EXIT
unzip -q "$APK" -d "$TMP/apk"
python3 -I tools/release_scan.py "$TMP/apk"

mkdir -p dist
cp -f "$APK" dist/SuperMarioAllStars-Android.apk
mkdir -p "$HOME/Downloads" && cp -f dist/SuperMarioAllStars-Android.apk "$HOME/Downloads/"
echo "$HOME/Downloads/SuperMarioAllStars-Android.apk"
