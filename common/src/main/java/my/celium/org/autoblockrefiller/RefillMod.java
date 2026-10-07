package my.celium.org.autoblockrefiller;

import java.util.UUID;

import my.celium.org.Mycel;
import my.celium.org.config.ConfigBuilder;
import my.celium.org.config.ConfigValue;
import my.celium.org.config.MycelConfig;
import my.celium.org.metadata.ModMetadata;
import my.celium.org.schedule.MycelScheduler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * AutoBlockRefiller: keeps your building hand stocked.
 *
 * <p>When a right-click with a block empties the used hotbar slot (or the
 * offhand), the same kind of block is pulled from the main inventory a couple
 * of ticks later. Silent, server-side, and fully configurable.
 *
 * <p>The whole mod is this file plus {@link RefillBrain}: metadata, a
 * two-value config, and a pre-placement trigger. File IO, loader events,
 * platform detection and diagnostics all come from Mycel.
 */
public final class RefillMod {
    public static final String ID = "autoblockrefiller";

    public static final ModMetadata META = ModMetadata.builder(ID, "AutoBlockRefiller", "1.0.1")
            .author("MyCelium")
            .build();

    public static ConfigValue<Boolean> enabled;
    public static ConfigValue<Boolean> searchHotbar;
    public static MycelConfig config;

    private RefillMod() {
    }

    /** Called once from each loader's entrypoint. */
    public static void init() {
        Mycel.initialize();
        Mycel.registerMod(META);

        ConfigBuilder builder = Mycel.config(META);
        enabled = builder.booleanValue("enabled", true,
                "Whether empty building slots are refilled automatically.");
        searchHotbar = builder.booleanValue("searchHotbar", false,
                "Also take refills from other hotbar slots (the main inventory is always searched).");
        config = builder.build();
    }

    public static boolean isModEnabled() {
        return Mycel.isEnabled(ID) && enabled.get();
    }

    /**
     * Pre-placement trigger, called by each loader's native right-click hook
     * on the server side. Only records <em>what</em> might need refilling; the
     * actual check runs deferred (see {@link RefillBrain}), once the placement
     * has resolved and the slot state is final.
     */
    public static void onUseBlock(ServerPlayer player, InteractionHand hand) {
        if (!isModEnabled()) {
            return;
        }
        Inventory inventory = player.getInventory();
        if (hand == InteractionHand.MAIN_HAND) {
            int slot = inventory.getSelectedSlot();
            ItemStack held = inventory.getItem(slot);
            if (held.isEmpty() || !(held.getItem() instanceof BlockItem)) {
                return;
            }
            scheduleCheck(player, slot, held.getItem());
        } else if (hand == InteractionHand.OFF_HAND) {
            ItemStack held = player.getOffhandItem();
            if (held.isEmpty() || !(held.getItem() instanceof BlockItem)) {
                return;
            }
            scheduleCheck(player, Inventory.SLOT_OFFHAND, held.getItem());
        }
    }

    private static void scheduleCheck(ServerPlayer player, int slot, Item item) {
        UUID uuid = player.getUUID();
        MinecraftServer server = player.level().getServer();
        MycelScheduler.runLater(2, () -> RefillBrain.refillLater(server, uuid, slot, item));
    }
}
