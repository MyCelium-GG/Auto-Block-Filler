# AutoBlockRefiller

Never run out of blocks mid-build again. When placing blocks empties your hotbar
slot (or offhand) and more of the same block sit anywhere in your main inventory,
one full stack slides into the empty slot. Snowballs, ender pearls, firework
rockets and other items you use up by right-clicking refill the same way.
Server side, a couple of ticks later.

## Winter update

- **Snowflakes and sleigh bells**: every refill gives a small puff of snowflakes and a bell chime that climbs a note each time.
- **Snowball fights never stop**: snowballs always refill, even with refillItems off.
- **Turn it off any time**: /autoblockrefiller set winter false.

## Behavior

- Triggers on right-click with a block or a usable item; checks 2 ticks later.
- Refills only when the used slot is actually empty (failed placements and manual swaps are respected, never overwritten).
- Blocks match by block; items must match exactly, so a splash potion of healing is never swapped for poison.
- Armour and other wearables are never refilled, so putting on a chestplate leaves your hand empty.
- Sources: main inventory first; other hotbar slots only with searchHotbar. Armour, offhand and crafting slots are never taken from.
- A note above the hotbar says when the last stack went in, or when nothing is left.

## Command (/autoblockrefiller, also /abr)

- **/autoblockrefiller**: every setting with its value. Operators click a value to change it.
- **/autoblockrefiller toggle**: any player turns refilling off or on, just for themselves.
- **/autoblockrefiller set <setting> <value>**: operators change a setting, with tab completion.
- **/autoblockrefiller reset <setting>|all**: operators put settings back to their defaults.

## Anonymous stats

AutoBlockRefiller sends anonymous stats (how often slots get refilled, with which blocks, and errors in its own code) to its FastStats project through
Mycel. It follows Mycel's choice exactly: the one-time screen in your own
game, `shareStats` / `shareErrors` in `config/mycel.json` on servers.
Details: https://wiki.mycelium-gg.org/privacy#stats-autoblockrefiller

## Configuration (`config/autoblockrefiller.json`)

| Key | Default | Meaning |
|---|---|---|
| `enabled` | `true` | Master switch. |
| `searchHotbar` | `false` | Allow refills from the other hotbar slots. |
| `refillItems` | `true` | Also refill snowballs, ender pearls, rockets, bone meal and other used-up items. |
| `refillSound` | `true` | A quiet sound, only for that player, on each refill. |
| `runningLowWarning` | `true` | Note above the hotbar for the last stack and when you run out. |
| `playerToggle` | `true` | Players may turn refilling off for themselves. |
| `winter` | `true` | Snowflakes, sleigh bells and snowball refills. |

Change them in game with /autoblockrefiller, or with /mycel set autoblockrefiller <key> <value>.
A config screen is available from the mod list on Forge and NeoForge.
