package my.celium.org.autoblockrefiller.forge;

import my.celium.org.autoblockrefiller.MissingMycel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Forge entrypoint. Without Mycel the mod stays idle and tells joining players Mycel is missing;
 * with it, {@link RefillForgeHooks} wires the refill trigger and the command. This class must not
 * reference anything that loads Mycel.
 */
@Mod("autoblockrefiller")
public final class RefillForge {
    public RefillForge(@SuppressWarnings("unused") FMLJavaModLoadingContext context) {
        if (!ModList.isLoaded("mycel")) {
            MissingMycel.logOnce();
            PlayerEvent.PlayerLoggedInEvent.BUS.addListener(event -> {
                if (event.getEntity() instanceof ServerPlayer player) {
                    MissingMycel.tell(player);
                }
            });
            return;
        }
        RefillForgeHooks.register();
    }
}
