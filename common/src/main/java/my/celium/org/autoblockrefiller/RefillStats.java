package my.celium.org.autoblockrefiller;

import my.celium.org.stats.ModStats;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/**
 * Anonymous stats for the FastStats project {@code auto-block-refiller}, sent through Mycel under
 * its consent: how often slots get refilled, with which blocks, and how often a slot runs empty
 * with nothing left to refill it.
 */
public final class RefillStats {
    private static final String TOKEN = "511f3e9ce72c2dba0b278f91a44991fe";

    private static volatile ModStats stats;

    private RefillStats() {
    }

    static void start() {
        stats = ModStats.builder(RefillMod.META, TOKEN)
                .bool("enabled", () -> RefillMod.enabled.get())
                .bool("search_hotbar", () -> RefillMod.searchHotbar.get())
                .counters("refilled_blocks")
                .errors("my.celium.org.autoblockrefiller.")
                .start();
    }

    /** A slot was refilled with {@code item}; {@code offhand} for the off-hand slot. */
    static void refilled(Item item, boolean offhand) {
        ModStats current = stats;
        if (current == null) {
            return;
        }
        current.count(offhand ? "refill_offhand" : "refill");
        Identifier key = BuiltInRegistries.ITEM.getKey(item);
        current.count("refilled_blocks", "minecraft".equals(key.getNamespace()) ? key.getPath() : key.toString());
    }

    /** The slot ran empty and no matching stack was left to refill it. */
    static void ranOut() {
        ModStats current = stats;
        if (current != null) {
            current.count("ran_out");
        }
    }
}
