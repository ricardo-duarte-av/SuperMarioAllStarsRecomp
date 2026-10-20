#!/usr/bin/env python3
"""Keep everything outside Super Mario Bros. / The Lost Levels code in the interpreter.

Promoting a routine in banks $01-$0F makes the recompiler also translate whatever shared cart code
($00) it calls, and translated shared code breaks Super Mario Bros. 2 and 3 (notes/native-coverage.md).
This lists every function src/gen translated outside $01-$0F that is not one of the scaffold's
starting entries, and adds a `force_lle` line for it to recomp/bank00.cfg.

  python3 tests/keep_shared_lle.py      prints how many were added; exit 0
  then: bash tools/regen.sh             (only needed when it added any)
"""
import glob, os, re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CFG = os.path.join(ROOT, "recomp", "bank00.cfg")
# The scaffold's own entry points (vectors and each game's hand-off); they were checked against Snes9x.
KEEP = {0x00836C, 0x00838B, 0x0083A2, 0x0083A7, 0x0083AC, 0x0083B1,
        0x038006, 0x0D8006, 0x118006, 0x208006}

def main():
    emitted = set()
    for f in glob.glob(os.path.join(ROOT, "src", "gen", "bank*_v2.c")):
        for m in re.finditer(r"cpu_trace_func_entry\(cpu, 0x([0-9A-F]{6})", open(f).read()):
            emitted.add(int(m.group(1), 16))
    text = open(CFG).read()
    have = {int(x, 16) for x in re.findall(r"^force_lle\s+([0-9A-Fa-f]{6})", text, re.M)}
    add = sorted(pc for pc in emitted
                 if not 0x01 <= (pc >> 16) & 0x7F <= 0x0F and pc not in KEEP and pc not in have)
    if add:
        with open(CFG, "a") as f:
            f.write("".join(f"force_lle {pc:06X}\n" for pc in add))
    print(f"keep_shared_lle: added {len(add)} force_lle lines ({len(have) + len(add)} total)")

if __name__ == "__main__":
    main()
