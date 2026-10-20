#!/usr/bin/env python3
"""Writes the route_*.txt input scripts from small named pieces, so a change to the
menu walk-through is made once. Run:  python3 tests/gen_routes.py   (rewrites tests/route_*.txt)

Every piece is plain host-script text (press / wait / until / poke / dump ...).
Timings were measured on the real game and are repeatable: the emulation is
deterministic and the harness always starts from an empty save.
"""
import os

HERE = os.path.dirname(os.path.abspath(__file__))

def press(btn, n=4):  return [f"press {btn} {n}"]
def wait(n):          return [f"wait {n}"]
def dump(tag):        return [f"dump {tag}"]

# The Select Game carousel: Super Mario Bros. is in front; each Right moves one game on.
GAMES = {"smb1": 0, "ll": 1, "smb2": 2, "smb3": 3}

def to_game_title(game):
    """Power-on -> title -> Select Game -> that game -> File A (new) -> the game's own title."""
    return (wait(400) + press("start") + wait(400)
            + (press("right") + wait(90)) * GAMES[game]
            + dump("select") + press("start") + wait(200) + press("start") + wait(500))

def title_to_play(game):
    """The game's own title screen -> the first level, standing at the start."""
    if game in ("smb1", "ll"):          # 1 PLAYER GAME / MARIO GAME, then the WORLD 1-1 card
        return press("start") + wait(400)
    if game == "smb2":                  # skips the story; character select: Mario, then the level
        return press("start") + wait(300) + press("a") + wait(400)
    # smb3: curtain, 1 PLAYER GAME, the World 1 map; from START it is right, then up to level 1, then A
    return press("start") + wait(200) + press("start") + wait(700) + press("right", 8) + wait(90) + press("up", 8) + wait(90) + press("a") + wait(250)

def run_and_jump(reps, tag, every=4):
    """Run right, jumping now and then; a screenshot every `every` repeats (80 frames each)."""
    out = []
    for i in range(reps):
        out += press("right+y", 50) + press("right+y+b", 30)
        if (i + 1) % every == 0: out += dump(f"{tag}{(i + 1) // every:02d}")
    return out

def write(name, frames, lines, header=""):
    with open(os.path.join(HERE, name), "w") as f:
        if header: f.write("".join("# " + l + "\n" for l in header.strip().splitlines()))
        f.write(f"#@ frames={frames}\n")
        f.write("\n".join(lines) + "\n")

if __name__ == "__main__":
    write("route_attract.txt", 4000, sum((wait(250) + dump(f"a{i:02d}") for i in range(16)), []),
          "Hands off the controller: power-on, title, the Super Mario Bros. demo.")
    for g in GAMES:
        write(f"route_{g}.txt", 4000,
              to_game_title(g) + dump("title") + title_to_play(g) + dump("start") + run_and_jump(24, "p"),
              f"Select Game -> {g} -> File A -> first level, then run right and jump for ~2000 frames.")
