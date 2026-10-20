# Checking the picture against Snes9x

The surest way to know our picture is right is to play the same inputs in a trusted emulator and put
the pictures side by side. This folder is that check. The emulator is **not** in this repo (and neither
is any game data): you download Snes9x's libretro core yourself.

## One-time setup
1. Download `snes9x_libretro.dylib.zip` (Mac, Apple chip) from
   https://buildbot.libretro.com/nightly/apple/osx/arm64/latest/ and unzip it somewhere outside the repo.
2. Build the little runner: `clang -O2 -o refhost tests/ref/refhost.c`

## Use
```sh
# Snes9x plays a route (only press / wait / dump lines, buttons joined with + ; `until` and `poke` need our own host)
mkdir -p OUT_REF && ./refhost snes9x_libretro.dylib rom/smas.sfc OUT_REF tests/route_smb1.txt
# our game plays the same route and saves its pictures
tests/run_route.sh tests/route_smb1.txt native OUT_OURS
# percent of pixels that differ at each `dump` (0.0 = identical)
python3 tests/ref/compare.py OUT_REF OUT_OURS/dumps
# ours on the left, Snes9x on the right, for the tags you name
python3 tests/ref/pair.py OUT_REF OUT_OURS/dumps pair.ppm start p01
```
Rules of thumb: with no button presses the two should match pixel for pixel (the title and the Super
Mario Bros. demo do). With button presses a game can drift apart a little (Mario 3 does), so judge those
by eye. `strict.py` (largest per-channel difference over 6) catches one-line shifts that
`compare.py` misses.

## What this found (October 2026, Super Mario All-Stars)
- Every sky gradient was one line late and line 0 showed the bottom color: the renderer skipped HDMA's
  step for line 0 and re-applied the vblank writes after HDMA's first transfer (`patches/0011`). With it,
  the title, the demo, Super Mario Bros., The Lost Levels and Super Mario Bros. 2 match Snes9x to the pixel
  (compare with a strict color tolerance; `compare.py`'s default hides one-line gradient shifts).
- Super Mario Bros. 3's per-frame handler ($00:83B1 -> $20:8006), run as translated code, drew the level's
  far background wrong. Both now run in the original code (`kInterpTargets` in `src/game_rtl.c`).
- Mario 3 frames after button presses still differ by moving sprites and animated map tiles a few frames
  apart (the game's memory drifts slightly between the two programs); judge those by eye.
