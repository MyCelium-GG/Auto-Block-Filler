package my.celium.org.autoblockrefiller.forge;

import my.celium.org.Mycel;
import my.celium.org.autoblockrefiller.RefillMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Forge entrypoint: forwards pre-placement right-clicks into the common refill trigger.
 */
@Mod(RefillMod.ID)
public final class RefillForge {
    public RefillForge(@SuppressWarnings("unused") FMLJavaModLoadingContext context) {
        RefillMod.init();
        PlayerInteractEvent.RightClickBlock.BUS.addListener(this::onRightClickBlock);
    }

    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer player) {
            RefillMod.onUseBlock(player, event.getHand());
        }
    }
}
