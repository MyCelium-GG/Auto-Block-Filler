package my.celium.org.autoblockrefiller;

import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * The refill brain: no loader APIs, no events - pure inventory logic plus a
 * two-tick-deferred entry point.
 *
 * <p>Why deferred? Loader hooks fire <em>before</em> the block is placed (or
 * the snowball thrown), so the slot state isn't final yet. Two ticks later the
 * use has resolved: if the recorded slot is empty, it used up the last item and
 * a refill is due; if anything is there (placement failed, player switched
 * items), the slot is respected and nothing happens.
 *
 * <p>Blocks match by item alone, so a renamed stack of stone still refills
 * stone. Everything else (snowballs, rockets, potions) must match exactly,
 * so a splash potion of healing is never replaced by one of poison.
 */
public final class RefillBrain {
    private RefillBrain() {
    }

    /** Deferred check; safe to call even if the player logged out meanwhile. */
    public static void refillLater(MinecraftServer server, UUID playerId, int slot, ItemStack used, boolean block) {
        if (server == null || playerId == null || used == null || used.isEmpty()) {
            return;
        }
        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        if (player == null || player.isRemoved()) {
            return;
        }
        Inventory inventory = player.getInventory();
        boolean empty = slot >= 0 && slot < inventory.getContainerSize() && inventory.getItem(slot).isEmpty();
        if (!empty) {
            return;
        }
        if (refillSlot(inventory, slot, used, block, RefillMod.searchHotbar.get())) {
            RefillStats.refilled(used.getItem(), slot == Inventory.SLOT_OFFHAND);
            RefillFeedback.refilled(player, used, countLeft(inventory, slot, used, block, RefillMod.searchHotbar.get()));
        } else {
            RefillStats.ranOut();
            RefillFeedback.ranOut(player, used);
        }
    }

    /**
     * Moves a whole matching stack into {@code slot} when it is empty.
     *
     * @param inventory any container (usually the player's inventory)
     * @param slot target slot index
     * @param used what was in the slot before it ran out
     * @param block match by item alone (blocks) instead of item plus components
     * @param searchHotbar also consider hotbar slots 0-8 as sources
     * @return whether a refill happened
     */
    public static boolean refillSlot(Container inventory, int slot, ItemStack used, boolean block, boolean searchHotbar) {
        if (slot < 0 || slot >= inventory.getContainerSize() || used == null || used.isEmpty()) {
            return false;
        }
        if (!inventory.getItem(slot).isEmpty()) {
            return false;
        }
        int source = findSource(inventory, slot, used, block, searchHotbar);
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
    static int findSource(Container inventory, int skipSlot, ItemStack used, boolean block, boolean searchHotbar) {
        int size = inventory.getContainerSize();
        for (int slot = 9; slot < Math.min(36, size); slot++) {
            if (slot != skipSlot && matches(inventory.getItem(slot), used, block)) {
                return slot;
            }
        }
        if (searchHotbar) {
            for (int slot = 0; slot < Math.min(9, size); slot++) {
                if (slot != skipSlot && matches(inventory.getItem(slot), used, block)) {
                    return slot;
                }
            }
        }
        return -1;
    }

    /** How many more refills are waiting: matching stacks left in the places refills come from. */
    static int countLeft(Container inventory, int skipSlot, ItemStack used, boolean block, boolean searchHotbar) {
        int left = 0;
        int size = inventory.getContainerSize();
        for (int slot = searchHotbar ? 0 : 9; slot < Math.min(36, size); slot++) {
            if (slot != skipSlot && matches(inventory.getItem(slot), used, block)) {
                left++;
            }
        }
        return left;
    }

    static boolean matches(ItemStack stack, ItemStack used, boolean block) {
        if (stack.isEmpty()) {
            return false;
        }
        return block ? stack.is(used.getItem()) : ItemStack.isSameItemSameComponents(stack, used);
    }
}
