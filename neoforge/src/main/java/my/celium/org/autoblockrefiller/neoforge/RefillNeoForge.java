package my.celium.org.autoblockrefiller.neoforge;

import my.celium.org.autoblockrefiller.RefillMod;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * NeoForge entrypoint: forwards pre-placement right-clicks into the common refill trigger.
 */
@Mod(RefillMod.ID)
public final class RefillNeoForge {
    public RefillNeoForge(@SuppressWarnings("unused") IEventBus modBus,
            @SuppressWarnings("unused") ModContainer container) {
        RefillMod.init();
        NeoForge.EVENT_BUS.addListener(this::onRightClickBlock);
    }

    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer player) {
            RefillMod.onUseBlock(player, event.getHand());
        }
    }
}
