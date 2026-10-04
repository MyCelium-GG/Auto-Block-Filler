package my.celium.org.autoblockrefiller.fabric;

import my.celium.org.autoblockrefiller.RefillMod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

/** Fabric entrypoint: forwards pre-placement right-clicks into the common refill trigger. */
public final class RefillFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        RefillMod.init();
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                RefillMod.onUseBlock(serverPlayer, hand);
            }
            return InteractionResult.PASS;
        });
    }
}
