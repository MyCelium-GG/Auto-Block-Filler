package my.celium.org.autoblockrefiller.neoforge;

import my.celium.org.autoblockrefiller.MissingMycel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * NeoForge entrypoint. Without Mycel the mod stays idle and tells joining players Mycel is
 * missing; with it, {@link RefillNeoForgeHooks} wires the refill trigger and the command. This
 * class must not reference anything that loads Mycel.
 */
@Mod("autoblockrefiller")
public final class RefillNeoForge {
    public RefillNeoForge(@SuppressWarnings("unused") IEventBus modBus,
            @SuppressWarnings("unused") ModContainer container) {
        if (!ModList.get().isLoaded("mycel")) {
            MissingMycel.logOnce();
            NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
                if (event.getEntity() instanceof ServerPlayer player) {
                    MissingMycel.tell(player);
                }
            });
            return;
        }
        RefillNeoForgeHooks.register();
    }
}
