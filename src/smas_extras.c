/* Super Mario All-Stars cheats and extras. See smas_extras.h and notes/ram-map.md. */
#include "smas_extras.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "common_rtl.h"

SmasCheats g_smas_cheats;

/* ---- game RAM addresses (all proven in notes/ram-map.md) ---- */
enum {
    A_GAME        = 0x1FF00, /* $7F:FF00: 0 menu, 2 SMB1, 4 Lost Levels, 6 SMB2, 8 SMB3 */
    A_NES_LIVES   = 0x075A,  /* SMB1 / Lost Levels: lives minus one */
    A_NES_POWER   = 0x0756,  /* SMB1 / Lost Levels: 0 small, 1 Super, 2 Fire */
    A_NES_SIZE    = 0x0754,  /* SMB1 / Lost Levels: 0 big, 1 small */
    A_NES_TIME    = 0x07E9,  /* SMB1 / Lost Levels: timer digits, 3 bytes */
    A_SMB2_LIVES  = 0x04EE,
    A_SMB2_HEALTH = 0x04C3,  /* $0F per full heart */
    A_SMB2_HEARTS = 0x04C4,  /* hearts shown = value + 2 */
    A_SMB3_LIVES  = 0x0736,  /* Mario */
    A_SMB3_TIME   = 0x05EE,  /* timer digits, 3 bytes */
    A_SMB3_SUIT   = 0x00BB   /* 0 small, 1 Super, 2 Fire, 3 Raccoon, 4 Frog, 5 Tanooki, 6 Hammer */
};
enum { GAME_SMB1 = 2, GAME_LL = 4, GAME_SMB2 = 6, GAME_SMB3 = 8 };

/* A clock tick comes about every 24 frames; the end-of-level tally that turns leftover
 * time into points takes one off every frame. Only a lone tick is undone, so the
 * tally still runs to zero and the level can end. */
enum { TICK_GAP = 8 };

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

/* Developer / test switch, parsed once: SMAS_CHEATS="lives,time,health,power". */
static void SmasParseEnv(void)
{
    static int done;
    if (done) return;
    done = 1;
    const char *e = getenv("SMAS_CHEATS");
    if (!e || !*e) return;
    char buf[256];
    snprintf(buf, sizeof buf, "%s", e);
    for (char *tok = strtok(buf, ","); tok; tok = strtok(NULL, ",")) {
        if (!strcmp(tok, "lives")) g_smas_cheats.infinite_lives = 1;
        else if (!strcmp(tok, "time")) g_smas_cheats.infinite_time = 1;
        else if (!strcmp(tok, "health")) g_smas_cheats.full_health = 1;
        else if (!strcmp(tok, "power")) g_smas_cheats.power_up = 1;
        else fprintf(stderr, "[smas cheats] unknown SMAS_CHEATS entry '%s'\n", tok);
    }
}

static int timer_value(const uint8_t *d) { return d[0] * 100 + d[1] * 10 + d[2]; }

void SmasCheatsApplyAfterFrame(void)
{
    static uint8_t prev_game, prev_lives, have_lives;
    static int prev_time = -1, since_tick = TICK_GAP;
    uint8_t *m = g_ram;

    SmasRamTrace();
    SmasParseEnv();

    uint8_t game = m[A_GAME];
    if (game != prev_game) {            /* switched games (or back to the menu): forget the old values */
        prev_game = game;
        prev_time = -1;
        since_tick = TICK_GAP;
        have_lives = 0;
    }

    /* Lives: undo a drop of exactly one (a death); gains and new-game resets are left alone. */
    uint32_t lives_at = game == GAME_SMB1 || game == GAME_LL ? A_NES_LIVES
                      : game == GAME_SMB2 ? A_SMB2_LIVES
                      : game == GAME_SMB3 ? A_SMB3_LIVES : 0;
    if (lives_at) {
        if (g_smas_cheats.infinite_lives && have_lives && m[lives_at] == (uint8_t)(prev_lives - 1))
            m[lives_at] = prev_lives;
        prev_lives = m[lives_at];
        have_lives = 1;
    }

    /* Clock: undo a lone one-second tick; see TICK_GAP. A jump up is a new level or a new life. */
    uint32_t time_at = game == GAME_SMB1 || game == GAME_LL ? A_NES_TIME
                     : game == GAME_SMB3 ? A_SMB3_TIME : 0;
    if (time_at && m[time_at] <= 9 && m[time_at + 1] <= 9 && m[time_at + 2] <= 9) {
        int t = timer_value(&m[time_at]);
        if (prev_time >= 0 && t == prev_time - 1) {
            if (g_smas_cheats.infinite_time && since_tick >= TICK_GAP) {
                m[time_at] = (uint8_t)(prev_time / 100);
                m[time_at + 1] = (uint8_t)(prev_time / 10 % 10);
                m[time_at + 2] = (uint8_t)(prev_time % 10);
                t = prev_time;
            }
            since_tick = 0;
        } else if (since_tick < TICK_GAP) {
            since_tick++;
        }
        /* Powered up at the start of every level and every new life (the clock jumps up). A better
         * suit Mario already has (Frog, Tanooki, Hammer) is kept. */
        if (g_smas_cheats.power_up && t > prev_time + 1) {
            if (game == GAME_SMB3) {
                if (m[A_SMB3_SUIT] < 3) m[A_SMB3_SUIT] = 3;
            } else if (m[A_NES_POWER] < 2) {
                m[A_NES_POWER] = 2;
                m[A_NES_SIZE] = 0;
            }
        }
        prev_time = t;
    }

    /* Super Mario Bros. 2 hearts: refill while Mario is alive (a live value always ends in $F). */
    if (g_smas_cheats.full_health && game == GAME_SMB2) {
        uint8_t hearts = (uint8_t)(m[A_SMB2_HEARTS] + 2);
        uint8_t full = (uint8_t)(((hearts - 1) << 4) | 0x0F);
        uint8_t h = m[A_SMB2_HEALTH];
        if (hearts <= 4 && (h & 0x0F) == 0x0F && h < full)
            m[A_SMB2_HEALTH] = full;
    }
}
