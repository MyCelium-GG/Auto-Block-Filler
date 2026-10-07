# AutoBlockRefiller

[![CI](https://github.com/MyCelium-GG/Auto-Block-Filler/actions/workflows/build.yml/badge.svg)](https://github.com/MyCelium-GG/Auto-Block-Filler/actions/workflows/build.yml)
[![License](https://img.shields.io/github/license/MyCelium-GG/Auto-Block-Filler)](LICENSE)
[![Last commit](https://img.shields.io/github/last-commit/MyCelium-GG/Auto-Block-Filler)](https://github.com/MyCelium-GG/Auto-Block-Filler/commits/main)

Never run out of blocks mid-build again. When placing blocks empties your hotbar
slot (or offhand) and more of the same block sit anywhere in your main inventory,
one full stack slides into the empty slot. Silent, server side, a couple of
ticks later.

Built on [Mycel](https://github.com/MyCelium-GG/MyCel-Lib) (`my.celium.org`), the shared
core for the MyCelium ecosystem. This mod is about 150 lines of feature code because
configuration, platform abstraction, scheduling and diagnostics are reused.

| | |
|---|---|
| Mod id | `autoblockrefiller` |
| Minecraft | **26.3** · Java **25** |
| Loaders | Fabric, Forge, NeoForge |

## Behavior

- Triggers on right-click placement with a block item; verifies 2 ticks later.
- Refills only when the used slot is actually empty (failed placements and manual
  swaps are respected, never overwritten).
- Sources: main inventory first; other hotbar slots only with `searchHotbar`.
- Armour, offhand and crafting slots are never taken from.
- Works per hand: selected hotbar slot and offhand are both covered.

## Configuration (`config/autoblockrefiller.json`)

| Key | Default | Meaning |
|---|---|---|
| `enabled` | `true` | Master switch (also toggleable live via `Mycel.setEnabled`). |
| `searchHotbar` | `false` | Allow refills from the other hotbar slots. |

Reload live with `/mycel reload autoblockrefiller`. A config screen is
available from the mod list on Forge and NeoForge.

## Building

JDK 25, then:

```sh
./gradlew build            # all loaders + tests
./gradlew :common:test     # unit tests only
```

Mycel itself comes from [JitPack](https://jitpack.io/#MyCelium-GG/MyCel-Lib)
as one universal jar for all loaders
(`com.github.MyCelium-GG:MyCel-Lib:1.2.1`). No local setup needed.
