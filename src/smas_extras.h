/*
 * Super Mario All-Stars cheats and extras.
 *
 * Everything here is OFF by default and does nothing until a switch is turned on.
 * The menu flips the fields of g_smas_cheats; the game loop calls
 * SmasCheatsApplyAfterFrame() once at the end of every frame.
 *
 * Each cheat acts only inside the game it belongs to ($7F:FF00 says which one is
 * running), and every address it touches is proven in notes/ram-map.md.
 *
 * Developer / test switch: SMAS_CHEATS="lives,time,health,power" turns cheats on from
 * the command line (read once, on the first frame).
 */
#ifndef SMAS_EXTRAS_H
#define SMAS_EXTRAS_H

#include <stdint.h>

typedef struct SmasCheats {
    uint8_t infinite_lives;   /* all four games: the lives counter never goes down */
    uint8_t infinite_time;    /* Super Mario Bros., The Lost Levels, Super Mario Bros. 3: the clock stays put */
    uint8_t full_health;      /* Super Mario Bros. 2: the hearts refill */
    uint8_t power_up;         /* every level and new life starts powered up: Fire Mario (Super Mario Bros., The Lost Levels), Raccoon Mario (Super Mario Bros. 3) */
} SmasCheats;

extern SmasCheats g_smas_cheats;

/* Call once at the end of every simulated frame. */
void SmasCheatsApplyAfterFrame(void);

#endif /* SMAS_EXTRAS_H */
