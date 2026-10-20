#!/usr/bin/env python3
"""One number for "what state did the game end up in at every dump point of this route".
  python3 tests/logic_digest.py <run-dir> [last-tag]
SHA-1 over the game's RAM at each dump (in the order the route made them), with the bytes that
legitimately depend on sub-frame timing blanked out:
  $0100-$01FF   the stack page (leftover bytes below the stack pointer; where a frame is cut changes them)
  $0000-$000F   the jump-table helpers' scratch bytes, which translated code skips (snesrecomp
                docs/LLE_SCHEDULER.md, "dead scratch")
  $00E4         a scratch byte The Lost Levels' routines share; at one dump of route_ll it still held
                a mid-routine value in one tier and was equal again at every later dump
Everything else (lives, score, clock, sprites' logic, scene state, VRAM-feeding buffers) is in.
The same route run all-native and all-interpreter gives the same number; so does a rebuild that
did not change gameplay."""
import glob, hashlib, os, sys
run = sys.argv[1]; last = sys.argv[2] if len(sys.argv) > 2 else None
h = hashlib.sha1(); n = 0
for f in sorted(glob.glob(os.path.join(run, "dumps", "*.wram.bin")), key=os.path.getmtime):
    tag = os.path.basename(f)[:-9]
    d = bytearray(open(f, "rb").read())
    d[0x100:0x200] = bytes(0x100); d[0x0:0x10] = bytes(0x10); d[0xE4] = 0
    h.update(tag.encode()); h.update(bytes(d)); n += 1
    if tag == last: break
print(f"{h.hexdigest()} ({n} dumps)")
