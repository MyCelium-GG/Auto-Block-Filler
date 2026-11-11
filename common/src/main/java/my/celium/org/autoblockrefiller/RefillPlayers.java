package my.celium.org.autoblockrefiller;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import my.celium.org.data.MycelDataFile;

/**
 * Players who turned refilling off for themselves with {@code /autoblockrefiller toggle}. Kept in
 * {@code config/autoblockrefiller_players.json}, so the choice survives restarts.
 */
final class RefillPlayers {
    private static final class Saved {
        Set<UUID> off = new HashSet<>();
    }

    private static MycelDataFile file;
    private static Saved saved;

    private RefillPlayers() {
    }

    private static synchronized Saved saved() {
        if (saved == null) {
            file = MycelDataFile.inConfigDir(RefillMod.META, "autoblockrefiller_players.json");
            saved = file.read(Saved.class, Saved::new);
            if (saved.off == null) {
                saved.off = new HashSet<>();
            }
        }
        return saved;
    }

    /** Whether {@code player} wants refills. Always true while the per-player switch is not allowed. */
    static synchronized boolean wantsRefills(UUID player) {
        return !RefillMod.playerToggle.get() || !saved().off.contains(player);
    }

    /** Flips the player's own switch and saves; returns whether refilling is now on for them. */
    static synchronized boolean toggle(UUID player) {
        Saved data = saved();
        boolean wasOff = data.off.remove(player);
        if (!wasOff) {
            data.off.add(player);
        }
        file.write(data);
        return wasOff;
    }
}
