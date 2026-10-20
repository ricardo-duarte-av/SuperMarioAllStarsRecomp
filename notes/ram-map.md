# Super Mario All-Stars RAM map (only what is proven)

Every address here was proven by writing it and seeing the effect, or by watching it change at a
known event, in Snes9x (`tests/ref/refhost.c`, `poke` lines) and/or in our own build (`poke` in a
route). Bank $7E unless a full address is given. Research routes: `work/ram_*.txt`, `work/poke_*.txt`;
search tool: `tests/ram_search.py`.

## Which game is running
| Address | Meaning | Proof |
|---|---|---|
| `$7F:FF00` | 0 title / game select, 2 Super Mario Bros., 4 The Lost Levels, 6 Super Mario Bros. 2, 8 Super Mario Bros. 3 | read in all four games' snapshots; the shared NMI at `$00:836C` uses it as its jump-table index |

## Super Mario Bros. (same layout as the NES game)
| Address | Meaning | Proof |
|---|---|---|
| `$075A` | lives left minus one (starts at 4, the card shows 5) | counts 4,3,2,1,0 as Mario dies; poked 8 -> card showed x 8 after the next death |
| `$0756` | power-up: 0 small, 1 super, 2 fire | poked 2 with `$0754`=0 -> Fire Mario |
| `$0754` | size: 0 big, 1 small | as above |
| `$07E9`-`$07EB` | level timer digits (hundreds, tens, ones); NOT `$07F8` as on the NES | the on-screen clock's digits; poked 0,0,5 -> ran out, Mario died, lives 4 -> 3 |

## The Lost Levels
| Address | Meaning | Proof |
|---|---|---|
| `$075A` | lives left minus one (starts at 4) | 4 -> 3 when the poked timer ran out |
| `$07E9`-`$07EB` | level timer digits, as in Super Mario Bros. | poked 0,0,5 -> ran out, Mario died |

## Super Mario Bros. 2
| Address | Meaning | Proof |
|---|---|---|
| `$04EE` | lives left (starts at 5) | poked 9, health 0, walked into an enemy: 9 -> 8 |
| `$04C3` | health: $0F per heart filled ($1F = 2 full hearts) | dropped $1F -> $0F when hit; poked $3F (with `$04C4`=2) -> 4 full hearts |
| `$04C4` | extra hearts (hearts shown = value + 2) | as above |

## Super Mario Bros. 3
| Address | Meaning | Proof |
|---|---|---|
| `$0736` | Mario's lives left minus one (starts at 4) | 4 -> 3 when Mario died in 1-1, and again when the poked timer ran out |
| `$05EE`-`$05F0` | level timer digits (hundreds, tens, ones), as on the NES | poked 0,0,5 -> counted down, Mario died |
| (suit) | NOT `$00ED`, `$0746`, `$0578` or `$03F2` as on the NES: poking 5 left Mario small | open |

## Cheat proofs in our build (src/smas_extras.c, `SMAS_CHEATS=...`, routes in work/)
| Cheat | Run | Result |
|---|---|---|
| off | walk into Goombas, Super Mario Bros. | `$075A` 4,3,2,1,0,255: same as Snes9x (the cheats do nothing when off) |
| lives | same, Super Mario Bros. / Super Mario Bros. 3 | `$075A` / `$0736` stay 4 through every death |
| time | timer poked to 005, Super Mario Bros. / Super Mario Bros. 3 | the clock stays at 005, no time-out death |
| health | walk into enemies, Super Mario Bros. 2 | `$04C3` stays $1F (two full hearts) |
| fire | level start and after a time-out death, Super Mario Bros. | `$0756`=2 at the start of each life; a hit still shrinks Mario |
