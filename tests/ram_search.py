#!/usr/bin/env python3
"""Find RAM bytes that behave like a counter across a series of snapshots (Snes9x or our dumps).

  tests/ram_search.py <dir with sNNN.wram.bin> down <start values, e.g. 4,5> [--bytes 0x2000]
    bytes that start at one of the values, never go up, and step down by exactly 1 at least twice
  tests/ram_search.py <dir> show 0x75A 0x756 ...
    print each address's value in every snapshot (run-length compressed)
"""
import glob, os, sys

def load(d, n):
    fs = sorted(glob.glob(os.path.join(d, "s*.wram.bin")))
    return [open(f, "rb").read()[:n] for f in fs]

def rle(v):
    out, prev, k = [], None, 0
    for x in v + [None]:
        if x == prev: k += 1
        else:
            if prev is not None: out.append(f"{prev}x{k}" if k > 1 else str(prev))
            prev, k = x, 1
    return " ".join(out)

def main():
    d, mode = sys.argv[1], sys.argv[2]
    n = 0x20000
    snaps = load(d, n)
    if mode == "down":
        starts = {int(x) for x in sys.argv[3].split(",")}
        for a in range(0x2000):
            v = [s[a] for s in snaps]
            steps = [v[i] - v[i + 1] for i in range(len(v) - 1)]
            if v[0] in starts and all(s in (0, 1) for s in steps) and steps.count(1) >= 2:
                print(hex(a), rle(v))
    else:
        for x in sys.argv[3:]:
            a = int(x, 16)
            print(hex(a), rle([s[a] for s in snaps]))

if __name__ == "__main__":
    main()
