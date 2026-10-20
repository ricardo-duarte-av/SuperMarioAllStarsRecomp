/* See smas_ui.h. */
#include "smas_ui.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "recomp_runtime_ui.h"
#include "desktop/sdl_compat.h"
#include "snes/dsp.h"          /* g_dsp_voice_mute_mask */
#include "smas_extras.h"
#include "smas_signature.h"

#define VOICES 8
/* The letter, shared by the launcher's About page and the Esc menu's About section. */
#define ABOUT_P1 "Hi, I'm Maddie. I make apps and games on my own at Tanooki Studios."
#define ABOUT_P2 "Super Mario All-Stars is one of my favorite SNES games, and I wanted it to run on a modern " \
                 "computer with crisp pixels, controllers that just work, and a few cheats for when a " \
                 "level is being mean."
#define ABOUT_P3 "It's free, and it'll stay free. You bring your own copy of the game; nothing from the " \
                 "original cartridge ships with this. It isn't made by or affiliated with Nintendo."
#define ABOUT_P4 "Making apps (including some decomps) is how I pay my bills. If this game brought you " \
                 "some nostalgia and you lost more lives in World 8 than you'd like to admit, feel free " \
                 "to drop me a tiny tip. It really helps!"

#define PARA(key, text) { key, "About", "", text, RECOMP_RUNTIME_UI_PARAGRAPH, 0, 0, 0, NULL, 0, NULL, NULL }
static const RecompRuntimeUiStrokes kSignature = { MADDIE_SIGNATURE_W, MADDIE_SIGNATURE_H, kMaddieSignatureData };

#define MUTE_DESC "Silences one of the SNES's eight sound voices. The game decides what each voice plays."
#define BOOL_ROW(key, label, desc) { key, "Cheats", label, desc, \
                      RECOMP_RUNTIME_UI_BOOL, 0, 1, 1, NULL, 0, NULL }

const RecompRuntimeUiItem kSmasMenuItems[SMAS_MENU_ITEM_COUNT] = {
    /* Voice numbers on screen are 1-8; the keys count from 0 (see mute_voice). */
    { "smas.mute.0", "Audio", "Mute voice 1", MUTE_DESC, RECOMP_RUNTIME_UI_BOOL, 0, 1, 1, NULL, 0, NULL },
    { "smas.mute.1", "Audio", "Mute voice 2", MUTE_DESC, RECOMP_RUNTIME_UI_BOOL, 0, 1, 1, NULL, 0, NULL },
    { "smas.mute.2", "Audio", "Mute voice 3", MUTE_DESC, RECOMP_RUNTIME_UI_BOOL, 0, 1, 1, NULL, 0, NULL },
    { "smas.mute.3", "Audio", "Mute voice 4", MUTE_DESC, RECOMP_RUNTIME_UI_BOOL, 0, 1, 1, NULL, 0, NULL },
    { "smas.mute.4", "Audio", "Mute voice 5", MUTE_DESC, RECOMP_RUNTIME_UI_BOOL, 0, 1, 1, NULL, 0, NULL },
    { "smas.mute.5", "Audio", "Mute voice 6", MUTE_DESC, RECOMP_RUNTIME_UI_BOOL, 0, 1, 1, NULL, 0, NULL },
    { "smas.mute.6", "Audio", "Mute voice 7", MUTE_DESC, RECOMP_RUNTIME_UI_BOOL, 0, 1, 1, NULL, 0, NULL },
    { "smas.mute.7", "Audio", "Mute voice 8", MUTE_DESC, RECOMP_RUNTIME_UI_BOOL, 0, 1, 1, NULL, 0, NULL },
    BOOL_ROW("smas.lives", "Infinite lives", "All four games: losing a life never lowers the count."),
    BOOL_ROW("smas.time", "Stop the clock", "Super Mario Bros., The Lost Levels and Super Mario Bros. 3: the level timer never runs out. The end-of-level time bonus still counts down."),
    BOOL_ROW("smas.health", "Full hearts", "Super Mario Bros. 2: your hearts refill after every hit. Falling into a pit still costs a life."),
    BOOL_ROW("smas.power", "Start powered up", "Every level and every new life starts as Fire Mario (Super Mario Bros., The Lost Levels) or Raccoon Mario (Super Mario Bros. 3). A hit still costs you the power-up."),
    /* The About section of the Esc menu: the letter as wrapped paragraphs, the signature, then the tip
     * button. The rows stay selectable so the keys can scroll through them. */
    PARA("smas.about.1", ABOUT_P1),
    PARA("smas.about.2", ABOUT_P2),
    PARA("smas.about.3", ABOUT_P3),
    PARA("smas.about.4", ABOUT_P4),
    { "smas.about.sig", "About", "", "", RECOMP_RUNTIME_UI_STROKES, 0, 0, 0, NULL, 0, NULL, &kSignature },
    { "smas.about.tip", "About", "Leave a Tip", "Opens tanookistudios.com/SupportMe in your browser.",
      RECOMP_RUNTIME_UI_ACTION, 0, 0, 0, NULL, 0, NULL, NULL },
};

static void save(void) {
    FILE *f = fopen("smas.ini", "w");
    if (!f) return;
    fprintf(f, "mute_mask=%u\nlives=%d\ntime=%d\nhealth=%d\npower=%d\n",
            (unsigned)g_dsp_voice_mute_mask, g_smas_cheats.infinite_lives, g_smas_cheats.infinite_time,
            g_smas_cheats.full_health, g_smas_cheats.power_up);
    fclose(f);
}

void SmasUiLoad(void) {
    FILE *f = fopen("smas.ini", "r");
    if (!f) return;
    char line[64];
    while (fgets(line, sizeof(line), f)) {
        unsigned v;
        if (sscanf(line, "mute_mask=%u", &v) == 1) g_dsp_voice_mute_mask = (uint8_t)v;
        else if (sscanf(line, "lives=%u", &v) == 1) g_smas_cheats.infinite_lives = v != 0;
        else if (sscanf(line, "time=%u", &v) == 1) g_smas_cheats.infinite_time = v != 0;
        else if (sscanf(line, "health=%u", &v) == 1) g_smas_cheats.full_health = v != 0;
        else if (sscanf(line, "power=%u", &v) == 1) g_smas_cheats.power_up = v != 0;
    }
    fclose(f);
}

static int mute_voice(const RecompRuntimeUiItem *it) {
    if (!it->key || strncmp(it->key, "smas.mute.", 9)) return -1;
    int v = atoi(it->key + 9);
    return v >= 0 && v < VOICES ? v : -1;
}

static int get_value(void *ctx, const RecompRuntimeUiItem *it, int *out) {
    (void)ctx;
    int v = mute_voice(it);
    if (v >= 0) *out = (g_dsp_voice_mute_mask >> v) & 1;
    else if (!strcmp(it->key, "smas.lives")) *out = g_smas_cheats.infinite_lives;
    else if (!strcmp(it->key, "smas.time")) *out = g_smas_cheats.infinite_time;
    else if (!strcmp(it->key, "smas.health")) *out = g_smas_cheats.full_health;
    else if (!strcmp(it->key, "smas.power")) *out = g_smas_cheats.power_up;
    else return 0;
    return 1;
}

static int set_value(void *ctx, const RecompRuntimeUiItem *it, int value) {
    (void)ctx;
    int v = mute_voice(it);
    if (v >= 0) {
        if (value) g_dsp_voice_mute_mask |= (uint8_t)(1u << v);
        else g_dsp_voice_mute_mask &= (uint8_t)~(1u << v);
    } else if (!strcmp(it->key, "smas.lives")) g_smas_cheats.infinite_lives = value != 0;
    else if (!strcmp(it->key, "smas.time")) g_smas_cheats.infinite_time = value != 0;
    else if (!strcmp(it->key, "smas.health")) g_smas_cheats.full_health = value != 0;
    else if (!strcmp(it->key, "smas.power")) g_smas_cheats.power_up = value != 0;
    else return 0;
    save();
    return 1;
}

static int run_action(void *ctx, const RecompRuntimeUiItem *it) {
    (void)ctx;
    if (!strcmp(it->key, "smas.about.tip")) { SDL_OpenURL("https://tanookistudios.com/SupportMe"); return 1; }
    return 0;
}

static void save_cb(void *ctx) { (void)ctx; }

const RecompRuntimeUiCallbacks kSmasMenuCallbacks = {
    .context = NULL, .get_value = get_value, .set_value = set_value,
    .run_action = run_action, .is_enabled = NULL, .save = save_cb,
};

const char kSmasAboutText[] = ABOUT_P1 "\n\n" ABOUT_P2 "\n\n" ABOUT_P3 "\n\n" ABOUT_P4;
