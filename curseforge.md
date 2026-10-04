# AutoBlockRefiller

Never run out of blocks mid-build again. When placing blocks empties your
hotbar slot (or offhand) and more of the same block are in your inventory, a
fresh stack slides into the empty slot a moment later. Silent and automatic.

- Refills only blocks, only into slots that are actually empty
- Searches the main inventory first, never touches armour or offhand stock
- Covers both hands: selected hotbar slot and offhand
- Requires **Mycel**: install it alongside this mod

## Configuration

`config/autoblockrefiller.json` (or the mod menu screen):

- `enabled` (default `true`): master switch
- `searchHotbar` (default `false`): also take refills from other hotbar slots

Reload live with `/mycel reload autoblockrefiller`.

## Details

- Minecraft 26.3 on Fabric, Forge and NeoForge
- Java 25, MIT license
