#!/usr/bin/env bash
# One full turn of the native-coverage loop (about 5 minutes):
#   promote what the last coverage run found -> regenerate -> rebuild -> run routes with coverage ->
#   Snes9x picture gate (exits non-zero if any picture got worse) -> summary
#   tests/coverage_iterate.sh [route names...]       (default: all routes)
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOFTWARE:-${BASH_SOURCE[0]}}")/.." && pwd)"
cd "$ROOT"
ROUTES=("$@"); [ ${#ROUTES[@]} -gt 0 ] || ROUTES=(route_attract route_smb1 route_ll route_smb2 route_smb3)
INGEST=(); for r in "${ROUTES[@]}"; do INGEST+=("build-game/cov/$r.ingest.json"); done
python3 tests/promote_from_coverage.py --banks=01-0F --regions "${INGEST[@]}"
python3 tests/promote_from_coverage.py --banks=01-0F "${INGEST[@]}"
bash tools/regen.sh 2>&1 | grep -E 'analysis: [0-9]+ roots|AOT-eligible|error|Error|rror:' || true
# Shared cart code the new routines reach stays in the interpreter (see the script); regenerate if it added any.
python3 tests/keep_shared_lle.py | tee /dev/stderr | grep -q "added 0 " || bash tools/regen.sh > /dev/null 2>&1
tests/build_game.sh | tail -1
tests/coverage_check.sh "${ROUTES[@]}" | tail -4
# Every round must still match Snes9x (Snes9x pictures in work/ref-<route>, see tests/ref/README.md).
python3 tests/ref/gate.py work/ref- build-game/cov/route_
python3 tests/interp_regions.py "${INGEST[@]}" | head -14
python3 tests/lle_report.py | head -3
