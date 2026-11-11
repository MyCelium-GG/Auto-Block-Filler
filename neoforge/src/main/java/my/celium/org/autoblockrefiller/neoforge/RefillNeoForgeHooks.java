package my.celium.org.autoblockrefiller.neoforge;

import my.celium.org.autoblockrefiller.RefillCommand;
import my.celium.org.autoblockrefiller.RefillMod;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** The real mod on NeoForge, started only when Mycel is installed. */
final class RefillNeoForgeHooks {
    private RefillNeoForgeHooks() {
    }

    static void register() {
        RefillMod.init();
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
            if (!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer player) {
                RefillMod.onUse(player, event.getHand());
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickItem event) -> {
            if (!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer player) {
                RefillMod.onUse(player, event.getHand());
            }
        });
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> RefillCommand.register(event.getDispatcher()));
    }
}
