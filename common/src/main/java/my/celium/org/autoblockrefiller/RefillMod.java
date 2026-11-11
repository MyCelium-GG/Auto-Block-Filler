package my.celium.org.autoblockrefiller;

import java.util.UUID;

import my.celium.org.Mycel;
import my.celium.org.config.ConfigBuilder;
import my.celium.org.config.ConfigValue;
import my.celium.org.config.MycelConfig;
import my.celium.org.metadata.ModMetadata;
import my.celium.org.schedule.MycelScheduler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * AutoBlockRefiller: keeps your building hand stocked.
 *
 * <p>When a right-click with a block empties the used hotbar slot (or the
 * offhand), the same kind of block is pulled from the main inventory a couple
 * of ticks later. The same goes for items used up by right-clicking, like
 * snowballs, ender pearls and firework rockets. Server-side and configurable,
 * in game with {@code /autoblockrefiller}.
 *
 * <p>Only runs when Mycel is installed; the loader entrypoints check that
 * first and fall back to {@link MissingMycel} otherwise.
 */
public final class RefillMod {
    public static final String ID = "autoblockrefiller";
    public static final String NAME = "AutoBlockRefiller";

    public static final ModMetadata META = ModMetadata.builder(ID, NAME, "1.2.0")
            .author("MyCelium")
            .website("https://www.curseforge.com/minecraft/mc-mods/auto-block-refiller")
            .updateUrl("https://wiki.mycelium-gg.org/updates/autoblockrefiller.json")
            .build();

    public static ConfigValue<Boolean> enabled;
    public static ConfigValue<Boolean> searchHotbar;
    public static ConfigValue<Boolean> refillItems;
    public static ConfigValue<Boolean> refillSound;
    public static ConfigValue<Boolean> runningLowWarning;
    public static ConfigValue<Boolean> playerToggle;
    public static ConfigValue<Boolean> winter;
    public static MycelConfig config;

    private RefillMod() {
    }

    /** Called once from each loader's entrypoint, only when Mycel is installed. */
    public static void init() {
        Mycel.initialize();
        Mycel.registerMod(META);

        ConfigBuilder builder = Mycel.config(META);
        enabled = builder.booleanValue("enabled", true,
                "Whether empty slots are refilled automatically.");
        searchHotbar = builder.booleanValue("searchHotbar", false,
                "Also take refills from other hotbar slots (the main inventory is always searched).");
        refillItems = builder.booleanValue("refillItems", true,
                "Also refill items used up by right-clicking, like snowballs, ender pearls, firework rockets and bone meal.");
        refillSound = builder.booleanValue("refillSound", true,
                "Play a quiet sound, only for that player, when a slot is refilled.");
        runningLowWarning = builder.booleanValue("runningLowWarning", true,
                "Show a note above the hotbar when the last stack goes in, or when nothing is left to refill with.");
        playerToggle = builder.booleanValue("playerToggle", true,
                "Let every player turn refilling off for themselves with /autoblockrefiller toggle.");
        winter = builder.booleanValue("winter", true,
                "Winter update: refills sparkle with snowflakes and chime like sleigh bells, and snowballs always refill.");
        config = builder.build();
        RefillStats.start();
    }

    public static boolean isModEnabled() {
        return Mycel.isEnabled(ID) && enabled.get();
    }

    /**
     * Right-click trigger, called by each loader's native hooks on the server
     * side, both for clicks on a block and clicks in the air. Only records
     * <em>what</em> might need refilling; the actual check runs deferred (see
     * {@link RefillBrain}), once the use has resolved and the slot state is final.
     */
    public static void onUse(ServerPlayer player, InteractionHand hand) {
        if (!isModEnabled()) {
            return;
        }
        Inventory inventory = player.getInventory();
        int slot = hand == InteractionHand.OFF_HAND ? Inventory.SLOT_OFFHAND : inventory.getSelectedSlot();
        ItemStack held = hand == InteractionHand.OFF_HAND ? player.getOffhandItem() : inventory.getItem(slot);
        if (held.isEmpty()) {
            return;
        }
        boolean block = held.getItem() instanceof BlockItem;
        if (!block && !refillsItem(held)) {
            return;
        }
        if (!RefillPlayers.wantsRefills(player.getUUID())) {
            return;
        }
        UUID uuid = player.getUUID();
        MinecraftServer server = player.level().getServer();
        ItemStack used = held.copyWithCount(1);
        MycelScheduler.runLater(2, () -> RefillBrain.refillLater(server, uuid, slot, used, block));
    }

    /**
     * Whether a non-block item is refilled. Wearables are left out: right-clicking armour puts it
     * on, which empties the hand without using anything up.
     */
    static boolean refillsItem(ItemStack held) {
        if (held.has(DataComponents.EQUIPPABLE)) {
            return false;
        }
        return refillItems.get() || (winter.get() && held.is(Items.SNOWBALL));
    }
}
