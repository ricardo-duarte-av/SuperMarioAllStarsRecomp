/* Super Mario All-Stars cheats and extras. See smas_extras.h and notes/ram-map.md. */
#include "smas_extras.h"

#include <stdio.h>
#include <stdlib.h>

#include "common_rtl.h"

SmasCheats g_smas_cheats;

/* Developer tool, off unless SMAS_RAM_TRACE=<file> is set: append the first
 * SMAS_RAM_TRACE_BYTES (default 0x2000) bytes of game RAM to <file> after every frame.
 * Used to find what changes when something happens in the game. */
static void SmasRamTrace(void)
{
    static int init;
    static FILE *f;
    static size_t bytes = 0x2000;
    if (!init) {
        init = 1;
        const char *p = getenv("SMAS_RAM_TRACE");
        if (p && *p) f = fopen(p, "wb");
        const char *n = getenv("SMAS_RAM_TRACE_BYTES");
        if (n && *n) bytes = (size_t)strtoul(n, NULL, 0);
        if (bytes > 0x20000) bytes = 0x20000;
    }
    if (f) fwrite(g_ram, 1, bytes, f);
}

void SmasCheatsApplyAfterFrame(void)
{
    /* shortcut: no cheats act yet; the Mario ones are added once their RAM is proven (notes/ram-map.md). */
    SmasRamTrace();
}
