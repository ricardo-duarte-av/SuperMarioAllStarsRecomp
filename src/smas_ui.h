/* Super Mario All-Stars options shown on the launcher's Game page and in the
 * in-game menu, and the About page text. */
#ifndef SMAS_UI_H
#define SMAS_UI_H

#include <stddef.h>

#include "recomp_runtime_ui.h"

#define SMAS_MENU_ITEM_COUNT 19
#define SMAS_LAUNCHER_ITEM_COUNT 13   /* the rows before the About section */
extern const RecompRuntimeUiItem kSmasMenuItems[SMAS_MENU_ITEM_COUNT];
extern const RecompRuntimeUiCallbacks kSmasMenuCallbacks;
extern const char kSmasAboutText[];

/* Reads smas.ini from the settings folder and applies it. Call once
 * the settings folder is the working directory (the host's after_config). */
void SmasUiLoad(void);

#endif
