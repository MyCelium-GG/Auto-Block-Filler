package my.celium.org.autoblockrefiller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Headless tests for the refill brain over a fake container. */
class RefillBrainTest {
    static Item DIRT;
    static Item STONE;

    @BeforeAll
    static void bootstrap() {
        // Vanilla registries headless, then bind (empty) components so plain
        // ItemStacks can be built. Assigned here because Items' class-init
        // guard needs Bootstrap first.
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        DIRT = Items.DIRT;
        STONE = Items.STONE;
        DIRT.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
        STONE.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
    }

    private static ItemStack stackOf(Item item, int count) {
        return new ItemStack(Holder.direct(item), count);
    }

    /** Minimal {@link Container} backed by a list. */
    static final class FakeContainer implements Container {
        private final List<ItemStack> slots;

        FakeContainer(int size) {
            slots = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                slots.add(ItemStack.EMPTY);
            }
        }

        void set(int slot, ItemStack stack) {
            slots.set(slot, stack);
        }

        @Override
        public int getContainerSize() {
            return slots.size();
        }

        @Override
        public boolean isEmpty() {
            return slots.stream().allMatch(ItemStack::isEmpty);
        }

        @Override
        public ItemStack getItem(int slot) {
            return slots.get(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack stack = slots.get(slot);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack taken = stack.copyWithCount(Math.min(amount, stack.getCount()));
            stack.shrink(taken.getCount());
            if (stack.isEmpty()) {
                slots.set(slot, ItemStack.EMPTY);
            }
            return taken;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack stack = slots.get(slot);
            slots.set(slot, ItemStack.EMPTY);
            return stack;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            slots.set(slot, stack);
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < slots.size(); i++) {
                slots.set(i, ItemStack.EMPTY);
            }
        }
    }

    private static FakeContainer playerLike() {
        return new FakeContainer(41);
    }

    @Test
    void refillsEmptySlotFromMainInventory() {
        FakeContainer inv = playerLike();
        inv.set(15, stackOf(DIRT, 32));

        assertTrue(RefillBrain.refillSlot(inv, 3, DIRT, false));
        assertEquals(32, inv.getItem(3).getCount());
        assertTrue(inv.getItem(3).is(DIRT));
        assertTrue(inv.getItem(15).isEmpty());
    }

    @Test
    void respectsNonEmptySlot() {
        FakeContainer inv = playerLike();
        inv.set(3, stackOf(STONE, 5));
        inv.set(15, stackOf(DIRT, 32));

        assertFalse(RefillBrain.refillSlot(inv, 3, DIRT, false));
        assertTrue(inv.getItem(3).is(STONE));
        assertEquals(32, inv.getItem(15).getCount());
    }

    @Test
    void mismatchedItemsNeverRefill() {
        FakeContainer inv = playerLike();
        inv.set(15, stackOf(STONE, 32));

        assertFalse(RefillBrain.refillSlot(inv, 3, DIRT, false));
        assertTrue(inv.getItem(3).isEmpty());
    }

    @Test
    void nothingToTakeRefillsNothing() {
        FakeContainer inv = playerLike();

        assertFalse(RefillBrain.refillSlot(inv, 3, DIRT, true));
    }

    @Test
    void hotbarSourcesNeedOptIn() {
        FakeContainer main = playerLike();
        main.set(5, stackOf(DIRT, 10));
        assertFalse(RefillBrain.refillSlot(main, 3, DIRT, false));
        assertTrue(RefillBrain.refillSlot(main, 3, DIRT, true));
    }

    @Test
    void armourAndOffhandAreNeverSources() {
        FakeContainer inv = playerLike();
        inv.set(36, stackOf(DIRT, 10));
        inv.set(40, stackOf(DIRT, 10));

        assertFalse(RefillBrain.refillSlot(inv, 3, DIRT, true));
    }

    @Test
    void offhandSlotRefillsToo() {
        FakeContainer inv = playerLike();
        inv.set(20, stackOf(DIRT, 7));

        assertTrue(RefillBrain.refillSlot(inv, Inventory.SLOT_OFFHAND, DIRT, false));
        assertEquals(7, inv.getItem(Inventory.SLOT_OFFHAND).getCount());
    }

    @Test
    void rejectsBadInput() {
        FakeContainer inv = playerLike();
        inv.set(15, stackOf(DIRT, 1));

        assertFalse(RefillBrain.refillSlot(inv, -1, DIRT, false));
        assertFalse(RefillBrain.refillSlot(inv, 99, DIRT, false));
        assertFalse(RefillBrain.refillSlot(inv, 3, null, false));
    }
}
