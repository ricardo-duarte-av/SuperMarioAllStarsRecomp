#!/usr/bin/env bash
# Proves the recompiled code plays the same game as the original code running in the interpreter.
# Runs each route twice (native build / every recompiled function blocked) and compares the game's state
# at every dump point with tests/logic_digest.py. PASS = identical digests.
#   tests/tier_check.sh [route names...]      (default: the four games' routes; route_attract is left out: the
#   cart's title demo runs on timing, see notes/native-coverage.md)
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
ROUTES=("$@"); [ ${#ROUTES[@]} -gt 0 ] || ROUTES=(route_smb1 route_ll route_smb2 route_smb3)
mkdir -p build-game/tier
for r in "${ROUTES[@]}"; do for m in native interp; do
  tests/run_route.sh "tests/$r.txt" $m "$ROOT/build-game/tier/$r-$m" > "build-game/tier/$r-$m.log" 2>&1 &
done; done
wait
rc=0
for r in "${ROUTES[@]}"; do
  n=$(python3 tests/logic_digest.py "build-game/tier/$r-native"); i=$(python3 tests/logic_digest.py "build-game/tier/$r-interp")
  if [ "$n" = "$i" ]; then echo "PASS $r native==interp $n"; else echo "FAIL $r native=$n interp=$i"; rc=1; fi
done
exit $rc
