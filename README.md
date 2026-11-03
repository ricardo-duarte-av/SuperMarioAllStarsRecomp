<p align="center">
  <img src="assets/icon/smas-256.png" width="128" alt="Super Mario All-Stars icon">
</p>

<h1 align="center">Super Mario All-Stars, native for Mac, Windows, Linux and Android</h1>

<p align="center">
  The 1993 Super Nintendo collection (Super Mario Bros., The Lost Levels, Super Mario Bros. 2 and 3),<br>
  rebuilt as a real program for your computer. Crisp pixels at any size, a proper launcher, controller support, and cheats.<br>
  <b>Free. Bring your own copy of the game.</b>
</p>

**Website:** [supermarioallstars.tanookistudios.com](https://supermarioallstars.tanookistudios.com)

## Which ROM you need

This version works with **one exact file**: your own dump of the original 1993 US cartridge,
**Super Mario All-Stars (USA)**. Any other file is refused, including the 2010 re-release
"Super Mario All-Stars (USA) (Super Mario All-Stars - 25th Anniversary)", Europe/Japan versions,
"Super Mario All-Stars + Super Mario World", and patched or hacked ROMs.

| | |
|---|---|
| Game | Super Mario All-Stars (USA), the 1993 cartridge |
| Internal title | `SUPER MARIO ALL_STARS` |
| Size | 2,097,152 bytes (2 MiB, no copier header) |
| **SHA-256** | **`a9e3e57d591e995e8e0dd228b619b6aed42205eaf55316fa8ff33f236b3a32b3`** |
| SHA-1 | `c05817c5b7df2fbfe631563e0b37237156a8f6b6` |
| MD5 | `53c038150ba00d5f8d8574b4d36283f2` |
| CRC32 | `925637c7` |

To check your file's SHA-256:
- **Linux:** `sha256sum "Super Mario All-Stars (USA).sfc"`
- **Mac:** `shasum -a 256 "Super Mario All-Stars (USA).sfc"`
- **Windows (PowerShell):** `Get-FileHash "Super Mario All-Stars (USA).sfc"`

If your file has a 512-byte copier header (2,097,664 bytes), strip it first; the hash above is of the file without it.
The game's translated code is generated from this ROM, so a build only ever accepts the ROM it was made from.

## Download

> **Note:** the downloads below are the original project's builds. They were made from the 25th Anniversary
> ROM and will **not** accept the 1993 cartridge. For the 1993 cartridge, build it yourself
> ([Building it yourself](#building-it-yourself)): every platform has a one-command package script.

| Your computer | Download | |
|---|---|---|
| **Mac** (Apple chip or Intel, macOS 11+) | [**SuperMarioAllStars-Mac.zip**](https://github.com/TanookiStudios/SuperMarioAllStarsRecomp/releases/latest/download/SuperMarioAllStars-Mac.zip) | Signed and notarized by Apple |
| **Windows** (64-bit) | [**SuperMarioAllStars-Windows.zip**](https://github.com/TanookiStudios/SuperMarioAllStarsRecomp/releases/latest/download/SuperMarioAllStars-Windows.zip) | |
| **Linux** (64-bit, most desktops from 2020 on) | [**SuperMarioAllStars-Linux.tar.gz**](https://github.com/TanookiStudios/SuperMarioAllStarsRecomp/releases/latest/download/SuperMarioAllStars-Linux.tar.gz) | |
| **Android** (64-bit phones and tablets, Android 7+) | [**SuperMarioAllStars-Android.apk**](https://github.com/TanookiStudios/SuperMarioAllStarsRecomp/releases/latest/download/SuperMarioAllStars-Android.apk) | Not on Google Play: install the APK |

Every version and its notes are on the [Releases page](https://github.com/TanookiStudios/SuperMarioAllStarsRecomp/releases).

## How to play

1. Download the version for your computer and unzip it.
   - **Mac:** drag **Super Mario All-Stars** into Applications (or anywhere) and double-click it.
   - **Windows:** open the folder and double-click **SuperMarioAllStars.exe**.
   - **Linux:** open the folder and double-click **SuperMarioAllStars** (or run `./SuperMarioAllStars`).
   - **Android:** open the downloaded **SuperMarioAllStars-Android.apk**. If Android asks, allow your browser or
     file app to install apps ("Install unknown apps"), then tap **Install**. The first time you open it, tap
     **Choose file** and pick your game file (a `.zip` with it inside works too).
2. The launcher asks for your game file. Pick your own **Super Mario All-Stars (USA)** `.sfc` file: the 1993 cartridge. The 2010 re-release, "Super Mario All-Stars (USA) (Super Mario All-Stars - 25th Anniversary)", is a different file and is not accepted.
   The launcher checks it and shows **ROM verified** when it's the right one.
3. Set any options you like (the **Game** button has the cheats and sound switches), then press **Play**.
   Pick a game on the cartridge's own Select Game screen, just like on the console.

No game files come with this download. You need your own copy of the cartridge's data: see
[Which ROM you need](#which-rom-you-need) (SHA-256 `a9e3e57d591e995e8e0dd228b619b6aed42205eaf55316fa8ff33f236b3a32b3`).

### Controls

| SNES | Keyboard |
|---|---|
| D-pad | Arrow keys |
| B | Z |
| A | X |
| Y | A |
| X | S |
| L | C |
| R | V |
| Start | Enter |
| Select | Right Shift |
| **Menu** | **Esc** |

**On Android** an on-screen SNES controller sits beside the picture (it hides while a controller is
connected), and the ☰ button, the Back gesture or a controller's Home button opens the menu: cheats,
sound, save and load states, reset, About.

**Controllers:** plug one in and play, no setup. Xbox, PlayStation (3, 4 and 5), Switch Pro and Joy-Con,
8BitDo, and over 2,000 other pads are recognized by name. Any other USB pad still works with a standard layout.
The keyboard keeps working alongside the first controller; a second controller plays as player 2. Everything
can be remapped in **Settings**, and the **Esc** menu opens from a controller with the Guide / Home button
(or Select plus a click of the left stick). Own mappings: put a `gamecontrollerdb.txt` in the settings folder
(Mac: `~/Library/Application Support/SuperMarioAllStars`, Windows and Linux: next to the program).

## What's in it

- **A real app.** It runs as a real Mac, Windows or Linux program, not an emulator window. Super Mario Bros. and The Lost Levels
  are mostly translated into native code; Super Mario Bros. 2 and 3 run the game's original code in a built-in
  interpreter, at full speed.
- **Crisp at any size.** Whole-number scaling keeps every pixel sharp, from a small window to a 4K screen.
- **Shaders (Mac, Windows, Linux).** CRT and smoothing filters: in the launcher open **Settings** and pick one in
  the **Shader** row (CRT Lottes, CRT Royale, CRT Geom, CRT Easymode, CRT Aperture, CRT Hyllian, CRT Pi, zFast CRT,
  Scanlines, Sharp Bilinear, Pixellate, xBRZ Freescale and more). Choosing one switches the picture to OpenGL.
  **Browse** loads any other libretro `.glsl` / `.glslp` shader, for example from
  [libretro/glsl-shaders](https://github.com/libretro/glsl-shaders), and **Folder** opens `assets/shaders`, where
  presets you drop in join the list. Not on Android.
- **Launcher.** Set everything before you play: display, sound, controls, cheats.
- **In-game menu (Esc).** Change settings, save and load states, reset the game, or close it.
- **Sound.** Volume, and a mute switch for each of the Super Nintendo's eight sound voices.
- **Cheats.** Infinite lives (all four games), stop the clock (Super Mario Bros., The Lost Levels, Super Mario Bros. 3),
  full hearts (Super Mario Bros. 2), and start every level powered up (Fire Mario, or Raccoon Mario in Super Mario Bros. 3).

## Screenshots

<p>
  <img src="docs/screenshots/gameplay-1.png" width="32%" alt="The Super Mario All-Stars title screen">
  <img src="docs/screenshots/gameplay-2.png" width="32%" alt="Select Game">
  <img src="docs/screenshots/gameplay-3.png" width="32%" alt="Super Mario Bros., World 1-1">
</p>
<p>
  <img src="docs/screenshots/gameplay-4.png" width="32%" alt="The Lost Levels, World 1-1">
  <img src="docs/screenshots/gameplay-5.png" width="32%" alt="Super Mario Bros. 2">
  <img src="docs/screenshots/gameplay-6.png" width="32%" alt="Super Mario Bros. 3, World 1-1">
</p>
<p>
  <img src="docs/screenshots/launcher-main.png" width="49%" alt="The launcher">
  <img src="docs/screenshots/launcher-game.png" width="49%" alt="Cheats and sound before you play">
</p>
<p>
  <img src="docs/screenshots/launcher-settings.png" width="49%" alt="Launcher settings">
  <img src="docs/screenshots/launcher-about.png" width="49%" alt="The About page">
</p>
<p>
  <img src="docs/screenshots/ingame-menu.png" width="49%" alt="The in-game menu on Esc">
  <img src="docs/screenshots/gameplay-7.png" width="49%" alt="The Super Mario Bros. 3 world map">
</p>

## Building it yourself

You need your own ROM for this too: the build reads it to translate the game, and nothing from it is stored in this
repository. It must be the 1993 cartridge, **Super Mario All-Stars (USA)**, SHA-256
`a9e3e57d591e995e8e0dd228b619b6aed42205eaf55316fa8ff33f236b3a32b3`. A build only accepts the ROM it was made from.

### What you need first

Everywhere: Git, Python 3.9+, CMake 3.20+, Ninja, a C/C++ compiler, and Rust (the translator's analyzer is
written in Rust: install it from [rustup.rs](https://rustup.rs), then open a new terminal so `cargo` is on your PATH).

- **Mac:** `xcode-select --install`, then `brew install cmake ninja python`.
- **Linux, nothing installed on your system:** with [Podman](https://podman.io) or Docker you can skip the
  development packages below. Run `./setup.sh`, copy the ROM and run `bash tools/regen.sh` as below, then
  `tools/make_linux.sh` instead of the two `cmake` lines. It builds everything inside an Ubuntu 20.04 container and
  leaves the finished game in `dist/SuperMarioAllStars-Linux/` (and `~/Downloads/SuperMarioAllStars-Linux.tar.gz`).
  Rust is still needed on your system for `tools/regen.sh`.
- **Linux (Fedora):** `sudo dnf install gcc gcc-c++ cmake ninja-build python3 git libX11-devel libXext-devel
  libXrandr-devel libXcursor-devel libXi-devel libXfixes-devel libXScrnSaver-devel libxkbcommon-devel wayland-devel
  wayland-protocols-devel libdecor-devel mesa-libEGL-devel mesa-libGL-devel pipewire-devel pulseaudio-libs-devel alsa-lib-devel`
- **Linux (Ubuntu/Debian):** `sudo apt install build-essential cmake ninja-build python3 git libx11-dev libxext-dev
  libxrandr-dev libxcursor-dev libxi-dev libxfixes-dev libxss-dev libxkbcommon-dev libwayland-dev wayland-protocols
  libdecor-0-dev libegl1-mesa-dev libgl1-mesa-dev libpipewire-0.3-dev libpulse-dev libasound2-dev`
- **Windows:** build inside [WSL](https://learn.microsoft.com/windows/wsl/install) (Ubuntu) with the Linux steps,
  then make the Windows version with `tools/make_windows_zip.sh` (below).

### Build and play

```sh
./setup.sh                                  # fetches the engine at the tested versions and applies patches/
cp "/path/to/Super Mario All-Stars (USA).sfc" rom/smas.sfc
bash tools/regen.sh                         # checks your ROM, then translates it into C (kept out of git)
cmake -S . -B build -G Ninja -DCMAKE_BUILD_TYPE=Release
cmake --build build
./build/SuperMarioAllStarsRecomp            # the launcher opens; pick the same ROM
```

### Packaged versions (after the steps above, up to `tools/regen.sh`)

| For | Command | Runs on | Needs |
|---|---|---|---|
| Linux | `tools/make_linux.sh` | Linux or Mac | Docker or Podman (Colima on a Mac). Builds in Ubuntu 20.04, so it runs on most desktops from 2020 on |
| Windows | `tools/make_windows_zip.sh` | Linux, Mac or WSL | MinGW-w64 (`brew install mingw-w64`), or just Docker or Podman: without MinGW it builds in a container |
| Android | `tools/make_android.sh` | Linux, Mac or WSL | Android SDK (`ANDROID_HOME`), JDK 17+. Gradle fetches the NDK. Makes a signing key on first use |
| Mac | `tools/make_app.sh` | Mac | Signed ad-hoc unless you have a Developer ID (`--notarize` for Apple's notarization) |

Each one writes to `dist/` and copies the result to `~/Downloads`. The container scripts use Docker when it runs,
otherwise Podman; `CONTAINER_ENGINE=podman` picks Podman. The Android script keeps its signing key in
`~/.smas-android-signing/`: back it up, because Android only installs updates signed with the same key.
Screenshots: `tools/capture-screenshots.sh`. Bundled shaders: `python3 tools/update_shaders.py` refreshes
`assets/shaders/` from a pinned libretro commit (licenses in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)).

## About Maddie

Hi, I'm Maddie. I make apps and games on my own at [Tanooki Studios](https://tanookistudios.com).

Super Mario All-Stars is one of my favorite SNES games, and I wanted it to run on a modern computer with crisp pixels,
controllers that just work, and a few cheats for when a level is being mean.

It's free, and it'll stay free. Making apps (including some decomps) is how I pay my bills. If this game brought you some nostalgia and you lost more lives in World 8 than you'd like to admit, feel free to drop me a tiny tip. It really helps!

<img src="docs/signature.svg" width="200" alt="Signed, Maddie">

**[Leave a Tip](https://tanookistudios.com/SupportMe)**

## Credits and legal

- Built on [snesrecomp](https://github.com/mstan/snesrecomp) by Matthew Stanley (PolyForm Noncommercial 1.0.0)
  and [recomp-ui](https://github.com/mstan/recomp-ui) (MIT), with [SDL](https://libsdl.org) (zlib) and
  [Dear ImGui](https://github.com/ocornut/imgui) (MIT), and the [SDL_GameControllerDB](https://github.com/mdqinc/SDL_GameControllerDB) controller list (zlib). Full texts: [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
- This project's own code is under the [PolyForm Noncommercial License 1.0.0](LICENSE). It may never be sold.
- Super Mario All-Stars, Super Mario Bros., Mario and Super Nintendo are trademarks of Nintendo, which published the game.
  This project is not made by, endorsed by, or affiliated with Nintendo. It contains no game data: you supply your own.
