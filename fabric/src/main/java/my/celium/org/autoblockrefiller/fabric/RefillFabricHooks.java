package my.celium.org.autoblockrefiller.fabric;

import my.celium.org.autoblockrefiller.RefillCommand;
import my.celium.org.autoblockrefiller.RefillMod;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

/** The real mod on Fabric, started only when Mycel is installed. */
final class RefillFabricHooks {
    private RefillFabricHooks() {
    }

    static void register() {
        RefillMod.init();
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                RefillMod.onUse(serverPlayer, hand);
            }
            return InteractionResult.PASS;
        });
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                RefillMod.onUse(serverPlayer, hand);
            }
            return InteractionResult.PASS;
        });
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> RefillCommand.register(dispatcher));
    }
}
