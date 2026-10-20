# Native coverage (measured 2026-10-20)

Plain-English version: **Super Mario Bros. and The Lost Levels run mostly as translated (native) code**, about
the same share as Super Punch-Out. **Super Mario Bros. 2, Super Mario Bros. 3 and the cartridge's own menus run the
original game code** in the engine's interpreter, which executes it instruction for instruction. That was a choice:
translated, they broke (below). The interpreter is fast enough: the slowest route still runs at about 160 frames
per second headless, well over the game's 60.

## Numbers (`tests/native_share.sh`, real work = non-waiting instructions)
| Route | Real work, all-interpreter | Still interpreted | Native share |
|---|---|---|---|
| route_attract (title + Super Mario Bros. demo) | 14,318,001 | 10,987,509 | 23.3% |
| route_smb1 | 15,517,664 | 5,559,703 | 64.2% |
| route_ll | 13,064,128 | 5,543,340 | 57.6% |
| route_smb2 | 12,274,909 | 12,270,527 | 0.0% |
| route_smb3 | 12,776,748 | 12,771,701 | 0.0% |
| all routes | 67,951,450 | 47,132,780 | 30.6% |

797 coverage-promoted functions, all in banks $01-$0F (Super Mario Bros. and The Lost Levels). More would need
routes through later worlds; the current routes only play World 1-1.

## Why Super Mario Bros. 2, 3 and the shared code stay in the interpreter
- Translated shared cart code ($00: Select Game, the main loop at `$00:80C7` that long-jumps into each game, the
  per-game hand-offs) together with translated Super Mario Bros. 2/3 code broke those two games: black screens,
  and in Super Mario Bros. 3 the stack pointer left `$01xx` (frame 1334 of route_smb3: `$01FC` -> `$FFCF`) while the
  game started up, after which a return landed at `$20:0000`. Both games use NES-style jump-table helpers that pull
  their own return address off the stack (`$20:FB1F`, `$11:8776`); the engine's static handling of those helpers plus
  the main loop's long jump is the suspect. Blocking the three obvious routines was not enough (Super Mario Bros. 3
  still hung after its title), so the whole area stays interpreted.
- Promoting only $01-$0F still made the recompiler translate $00 routines those functions call. 27 such routines
  are kept on the interpreter with `force_lle` in `recomp/bank00.cfg`, written by `tests/keep_shared_lle.py`, which
  the coverage loop runs every round.
- `src/game_rtl.c` `kInterpTargets` keeps Super Mario Bros. 3's per-frame handler hand-off in the interpreter
  (`$00:83B1`, `$20:8006`): translated, it drew the level's far background wrong.

## Proof
- Snes9x picture gate (`tests/ref/gate.py`, strict compare): every route equal to `tests/ref/scores-baseline.txt`
  after every coverage round; the title, the demo and the first level of Super Mario Bros., The Lost Levels and
  Super Mario Bros. 2 match Snes9x to the pixel.
- `tests/tier_check.sh`: native == interpreter game state at every dump of the four game routes (stack page, the
  helpers' scratch `$0000-$000F` and The Lost Levels' scratch `$00E4` blanked; see `tests/logic_digest.py`).
- `tests/check_baseline.sh`: logic and every-frame hashes equal to the recording in `tests/baseline/`.
