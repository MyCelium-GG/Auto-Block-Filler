package my.celium.org.autoblockrefiller;

import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The refill brain: no loader APIs, no events - pure inventory logic plus a
 * two-tick-deferred entry point.
 *
 * <p>Why deferred? Loader hooks fire <em>before</em> the block is placed, so
 * the slot state isn't final yet. Two ticks later the placement has resolved:
 * if the recorded slot is empty, the placement consumed the last item and a
 * refill is due; if anything is there (placement failed, player switched
 * items), the slot is respected and nothing happens.
 */
public final class RefillBrain {
    private RefillBrain() {
    }

    /** Deferred check; safe to call even if the player logged out meanwhile. */
    public static void refillLater(MinecraftServer server, UUID playerId, int slot, Item item) {
        if (server == null || playerId == null || item == null) {
            return;
        }
        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        if (player == null || player.isRemoved()) {
            return;
        }
        refillSlot(player.getInventory(), slot, item, RefillMod.searchHotbar.get());
    }

    /**
     * Moves a whole matching stack into {@code slot} when it is empty.
     *
     * @param inventory any container (usually the player's inventory)
     * @param slot target slot index
     * @param item the block item to look for
     * @param searchHotbar also consider hotbar slots 0-8 as sources
     * @return whether a refill happened
     */
    public static boolean refillSlot(Container inventory, int slot, Item item, boolean searchHotbar) {
        if (slot < 0 || slot >= inventory.getContainerSize() || item == null) {
            return false;
        }
        if (!inventory.getItem(slot).isEmpty()) {
            return false;
        }
        int source = findSource(inventory, slot, item, searchHotbar);
        if (source < 0) {
            return false;
        }
        ItemStack moving = inventory.getItem(source);
        inventory.setItem(source, ItemStack.EMPTY);
        inventory.setItem(slot, moving);
        inventory.setChanged();
        return true;
    }

    /**
     * First matching source slot: main inventory first, then (optionally) the
     * hotbar. Armour, offhand and crafting slots are never sources.
     */
    static int findSource(Container inventory, int skipSlot, Item item, boolean searchHotbar) {
        int size = inventory.getContainerSize();
        for (int slot = 9; slot < Math.min(36, size); slot++) {
            if (slot != skipSlot && matches(inventory.getItem(slot), item)) {
                return slot;
            }
        }
        if (searchHotbar) {
            for (int slot = 0; slot < Math.min(9, size); slot++) {
                if (slot != skipSlot && matches(inventory.getItem(slot), item)) {
                    return slot;
                }
            }
        }
        return -1;
    }

    static boolean matches(ItemStack stack, Item item) {
        return !stack.isEmpty() && stack.is(item);
    }
}
