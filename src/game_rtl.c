/*
 * Super Mario All-Stars — per-game runtime glue.
 *
 * THIS FILE IS THE PORT. Everything else the scaffold produced is layout,
 * build wiring, and packaging; this is where the actual work happens.
 *
 * The framework does not, and cannot, drive an arbitrary SNES game on its
 * own. Two responsibilities always land on the game host:
 *
 *   1. Deciding what "one frame" means for THIS title, and returning from
 *      run_frame() at that boundary. A real ROM's reset vector never
 *      returns — it enters a main loop that waits on vblank — so somebody
 *      has to choose the yield point.
 *   2. Delivering NMI/IRQ at the hardware edge (see
 *      snesrecomp/docs/FRAME_MODEL_HOSTS.md).
 *
 * A new port starts on the framework's beam-aligned driver
 * (runner/src/beam_frame_driver.h): one frame is one PPU field, cut at the
 * beam's vblank edge; interrupts -- raster splits and a coprocessor's IRQ
 * line -- are taken where the beam latches them; parked time still runs the
 * beam, APU and coprocessors; and the field is rasterized from the raster
 * journal with real HDMA. It is the framework's, so a fix to it reaches this
 * port on a submodule pull.
 *
 * This file used to hold its own copy of a simpler driver: frames measured
 * from wherever the CPU last stopped, NMI delivered at an arbitrary beam
 * position, the field drawn with end-of-frame register state. A game that
 * uploads in vblank between INIDISP=$80 and $0F flickered black, and one
 * that chains raster splits fell apart. Replace these calls only for what
 * the driver does not do; MetalWarriorsSNESRecomp's src/mw_rtl.c and
 * GundamWingEndlessDuelSNESRecomp's src/game_rtl.c are developed examples of
 * title-specific drivers.
 */

#include "game_rtl.h"

#include "beam_frame_driver.h"
#include "snes/dma.h"
#include "cpu_state.h"
#include "smas_extras.h"

void GameRunOneFrame(void)
{
    /* This game switches its screen effects on partway down the picture and
     * relies on them staying idle until the next field. */
    g_dma_hdma_midframe_inert = 2;
    static int excluded;
    if (!excluded) {
        /* Super Mario Bros. 3's per-frame handler (the cart's shared NMI at $00:836C hands it on through
         * $00:83B1 -> $20:8006) left to the original code: as translated code it cannot be paused for the
         * raster interrupt, and the level's far background comes out wrong (tests/ref/README.md). */
        static const uint32 kInterpTargets[] = { 0x0083B1, 0x8083B1, 0x208006, 0xA08006 };
        const char *only = getenv("SMAS_EXCLUDE");   /* dev switch: "0083B1,208006" replaces the list */
        if (only && *only) {
            uint32 list[64]; size_t n = 0;
            for (const char *q = only; *q && n < 64; ) {
                list[n++] = (uint32)strtoul(q, (char **)&q, 16);
                while (*q == ',') ++q;
            }
            interp_bridge_set_lle_bounce_exclusions(list, n);
        } else
            interp_bridge_set_lle_bounce_exclusions(kInterpTargets, sizeof kInterpTargets / sizeof kInterpTargets[0]);
        excluded = 1;
    }
    snes_beam_frame_driver_run_frame();
    SmasCheatsApplyAfterFrame();
}

void GameDrawPpuFrame(void)
{
    snes_beam_frame_driver_draw_ppu_frame();
}

void GameSessionReset(void)
{
    /* Rematch / soft-return: clear anything that must not survive a new
     * session. recomp-ai-rules/NETPLAY.md §3 — sticky state that "has always
     * been fine" is the usual desync culprit, because single-player never
     * re-enters the boot path twice in one process. */
    snes_beam_frame_driver_reset();
}

const RtlGameInfo kGameInfo = {
    .title = "supermarioallstars",
    .initialize = NULL,
    .run_frame = &GameRunOneFrame,
    .draw_ppu_frame = &GameDrawPpuFrame,
    .save_name_prefix = "save",
    .session_reset = &GameSessionReset,
};
