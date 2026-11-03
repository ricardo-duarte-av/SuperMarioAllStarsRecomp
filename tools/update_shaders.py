#!/usr/bin/env python3
"""Refresh assets/shaders/ from libretro's glsl-shaders collection.

  python3 tools/update_shaders.py [path/to/glsl-shaders checkout]

Without a path it clones https://github.com/libretro/glsl-shaders at COMMIT
into work/glsl-shaders. For every preset in PRESETS it copies the preset's
whole closure (every pass, every #include, every lookup texture) into
assets/shaders/_libretro/<same path as upstream>, and writes a top-level
assets/shaders/<Name>.glslp whose paths point there. The launcher lists the
top-level presets by file name ("CRT-Lottes" shows as "CRT Lottes") and hides
the _libretro/ folder, because it starts with an underscore.

Files are copied unchanged, license headers included; THIRD_PARTY_NOTICES.md
lists each preset's license. Change COMMIT or PRESETS here, re-run, and test
each preset in the game (OpenGL) before committing.
"""
import os
import re
import shutil
import subprocess
import sys

COMMIT = "1aaabbebdd88a8a89a82edfdc94e10580c99ffdd"   # libretro/glsl-shaders, 2026-11-02
URL = "https://github.com/libretro/glsl-shaders.git"

# Top-level name -> upstream preset. Keep this list to presets tested in the game, and to files
# that carry a license (many in the collection have none, so they cannot be redistributed);
# record each one's license in THIRD_PARTY_NOTICES.md.
PRESETS = {
    "CRT-Lottes": "crt/crt-lottes.glslp",
    "CRT-Lottes-Fast": "crt/crt-lottes-fast.glslp",
    "CRT-Easymode": "crt/crt-easymode.glslp",
    "CRT-Geom": "crt/crt-geom.glslp",
    "CRT-Aperture": "crt/crt-aperture.glslp",
    "CRT-Hyllian": "crt/crt-hyllian.glslp",
    "CRT-Royale": "crt/crt-royale.glslp",
    "CRT-Pi": "crt/crt-pi.glslp",
    "zFast-CRT": "crt/zfast-crt.glslp",
    "Scanlines": "scanlines/res-independent-scanlines.glslp",
    "Sharp-Bilinear": "interpolation/sharp-bilinear-simple.glslp",
    "Pixellate": "interpolation/pixellate.glslp",
    "xBRZ-Freescale": "xbrz/xbrz-freescale.glslp",
}

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "assets", "shaders")
LIB = "_libretro"

KV = re.compile(r'^\s*([A-Za-z0-9_]+)\s*=\s*(.*?)\s*$')
INCLUDE = re.compile(r'^\s*#include\s+"([^"]+)"')


def unquote(v):
    v = v.split("#", 1)[0].strip() if not v.startswith('"') else v
    m = re.match(r'^"([^"]*)"', v)
    return m.group(1) if m else v.strip()


def preset_paths(text):
    """(key, path) for every value in a preset that names a file."""
    pairs = []
    for line in text.splitlines():
        m = KV.match(line)
        if m:
            pairs.append((m.group(1), unquote(m.group(2))))
    keys = dict(pairs)
    textures = [t for t in keys.get("textures", "").split(";") if t]
    out = []
    for k, v in pairs:
        if re.fullmatch(r"shader\d+", k) or k in textures:
            out.append((k, v))
    return out


def closure(src, preset):
    """Every upstream file (relative to the checkout) the preset needs."""
    need = {preset}
    base = os.path.dirname(preset)
    text = open(os.path.join(src, preset), encoding="utf-8").read()
    todo = []
    for _, p in preset_paths(text):
        rel = os.path.normpath(os.path.join(base, p))
        need.add(rel)
        if rel.endswith(".glsl"):
            todo.append(rel)
    while todo:
        f = todo.pop()
        for line in open(os.path.join(src, f), encoding="utf-8", errors="replace"):
            m = INCLUDE.match(line)
            if m:
                rel = os.path.normpath(os.path.join(os.path.dirname(f), m.group(1)))
                if rel not in need:
                    need.add(rel)
                    todo.append(rel)
    for f in need:
        if not os.path.isfile(os.path.join(src, f)):
            sys.exit(f"update_shaders: {preset} needs {f}, which is not in the checkout")
    return need


def wrapper(src, preset):
    """The preset's text with every file path redirected into _libretro/."""
    base = os.path.dirname(preset)
    text = open(os.path.join(src, preset), encoding="utf-8").read()
    keys = {k for k, _ in preset_paths(text)}
    out = [f"# {os.path.basename(preset)} from libretro/glsl-shaders ({COMMIT[:12]}),",
           "# written by tools/update_shaders.py: edit that, not this file."]
    for line in text.splitlines():
        m = KV.match(line)
        if m and m.group(1) in keys:
            new = "/".join([LIB, base, unquote(m.group(2))]) if base else "/".join([LIB, unquote(m.group(2))])
            new = os.path.normpath(new).replace(os.sep, "/")
            line = f'{m.group(1)} = "{new}"'
        out.append(line)
    return "\n".join(out) + "\n"


def main():
    if len(sys.argv) > 1:
        src = os.path.abspath(sys.argv[1])
    else:
        src = os.path.join(ROOT, "work", "glsl-shaders")
        if not os.path.isdir(os.path.join(src, ".git")):
            subprocess.check_call(["git", "clone", "-q", URL, src])
        subprocess.check_call(["git", "-C", src, "fetch", "-q", "origin", COMMIT])
        subprocess.check_call(["git", "-C", src, "checkout", "-q", COMMIT])

    if os.path.isdir(OUT):
        shutil.rmtree(OUT)
    os.makedirs(OUT)
    files = set()
    for name, preset in PRESETS.items():
        files |= closure(src, preset)
        with open(os.path.join(OUT, name + ".glslp"), "w", encoding="utf-8", newline="\n") as f:
            f.write(wrapper(src, preset))
    for rel in sorted(files):
        if rel in PRESETS.values():
            continue
        dst = os.path.join(OUT, LIB, rel)
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        shutil.copyfile(os.path.join(src, rel), dst)
    print(f"assets/shaders: {len(PRESETS)} presets, {len(files) - len(PRESETS)} support files")


if __name__ == "__main__":
    main()
