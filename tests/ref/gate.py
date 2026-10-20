#!/usr/bin/env python3
"""Fail if any route's picture got further from Snes9x than the saved baseline.

  python3 tests/ref/gate.py <snes9x dir prefix> <our runs prefix>
  e.g. python3 tests/ref/gate.py work/ref- build-game/cov/route_
reads <snes9x prefix><route>/<tag>.ppm and <ours prefix><route>/dumps/<tag>.fb.bgrx for every route in
tests/ref/scores-baseline.txt (strict scores, see strict.py), allows 0.5 points of noise per picture.
Exit 1 and list the pictures that got worse.
"""
import os, sys

HERE = os.path.dirname(os.path.abspath(__file__))

def score(ref, ours):
    d = open(ref, "rb").read().split(b"\n", 3); w, h = map(int, d[1].split()); a = d[3]; b = open(ours, "rb").read()
    n = sum(1 for i in range(w * h)
            if max(abs(a[i * 3] - b[i * 4 + 2]), abs(a[i * 3 + 1] - b[i * 4 + 1]), abs(a[i * 3 + 2] - b[i * 4])) > 6)
    return n * 100.0 / (w * h)

def main():
    refp, ourp = sys.argv[1], sys.argv[2]
    worse = []
    for line in open(os.path.join(HERE, "scores-baseline.txt")):
        route, rest = line.split(":", 1)
        for item in rest.split():
            tag, base = item.split(":")
            ours = f"{ourp}{route}/dumps/{tag}.fb.bgrx"
            if not os.path.exists(ours):
                worse.append(f"{route}/{tag}: no picture"); continue
            s = score(f"{refp}{route}/{tag}.ppm", ours)
            if s > float(base) + 0.5: worse.append(f"{route}/{tag}: {s:.2f} (baseline {base})")
    print("gate: " + ("PASS" if not worse else "FAIL " + "; ".join(worse)))
    sys.exit(1 if worse else 0)

main()
