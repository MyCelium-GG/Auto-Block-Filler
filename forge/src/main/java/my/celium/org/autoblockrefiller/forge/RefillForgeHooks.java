package my.celium.org.autoblockrefiller.forge;

import my.celium.org.autoblockrefiller.RefillCommand;
import my.celium.org.autoblockrefiller.RefillMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/** The real mod on Forge, started only when Mycel is installed. */
final class RefillForgeHooks {
    private RefillForgeHooks() {
    }

    static void register() {
        RefillMod.init();
        PlayerInteractEvent.RightClickBlock.BUS.addListener(event -> {
            if (!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer player) {
                RefillMod.onUse(player, event.getHand());
            }
        });
        PlayerInteractEvent.RightClickItem.BUS.addListener(event -> {
            if (!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer player) {
                RefillMod.onUse(player, event.getHand());
            }
        });
        RegisterCommandsEvent.BUS.addListener(event -> RefillCommand.register(event.getDispatcher()));
    }
}
