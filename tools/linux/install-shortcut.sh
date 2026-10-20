#!/bin/sh
# Adds Super Mario All-Stars to the application menu for this user only.
here="$(cd "$(dirname "$0")" && pwd)"
mkdir -p "$HOME/.local/share/applications" "$HOME/.local/share/icons/hicolor/256x256/apps"
cp "$here/smas.png" "$HOME/.local/share/icons/hicolor/256x256/apps/smas.png"
sed "s|^Exec=.*|Exec=\"$here/SuperMarioAllStars\"|" "$here/smas.desktop" > "$HOME/.local/share/applications/smas.desktop"
echo "Added Super Mario All-Stars to your application menu."
