# AutoBlockRefiller

Never run out of blocks mid-build again. When placing blocks empties your hotbar slot or offhand and you have more of that block in your inventory, a fresh stack slides into the empty slot a moment later. Snowballs, ender pearls and firework rockets refill the same way.

![The last oak plank is placed and a stack of 64 fills the empty hotbar slot](https://wiki.mycelium-gg.org/autoblockrefiller/img/refill-hotbar.png)

## Winter update

- Every refill gives a small puff of snowflakes and a sleigh-bell chime
- Snowballs always refill, so snowball fights never stop
- Turn it off with `/autoblockrefiller set winter false`

## What it does

- Refills blocks, and items you use up by right-clicking: snowballs, eggs, ender pearls, rockets, bone meal, splash potions
- Items must match exactly, and armour is never refilled
- Takes from the main inventory first and never touches armor or offhand stock
- Covers both hands: the selected hotbar slot and the offhand
- A note above the hotbar tells you when the last stack goes in, or when you run out

![The inventory before and after the refill: a stack moves from the main inventory to the hotbar](https://wiki.mycelium-gg.org/autoblockrefiller/img/refill-inventory.png)

## Command

- `/autoblockrefiller` (or `/abr`) shows every setting. Operators click a value to change it.
- `/autoblockrefiller toggle` turns refilling off or on just for you.
- `/autoblockrefiller set <setting> <value>` and `reset` for operators.

## Install

Server side: install it on the server, or in your own game for singleplayer.

Requires [Mycel](https://www.curseforge.com/minecraft/mc-mods/mycel): install it alongside this mod. Without it the game still starts, and the mod tells you in chat that Mycel is missing. Minecraft 26.1, 26.2 and 26.3 on Fabric, Forge and NeoForge, Java 25. On Fabric you also need Fabric API.

Every setting and command is on the [wiki](https://wiki.mycelium-gg.org/autoblockrefiller/).

Also from MyCelium: [BedrockPlacing](https://www.curseforge.com/minecraft/mc-mods/bedrock-placing) · [InstantXP](https://www.curseforge.com/minecraft/mc-mods/instant-xp-pickup) · [ItemHighlighter](https://www.curseforge.com/minecraft/mc-mods/item-highlighter-mod)
