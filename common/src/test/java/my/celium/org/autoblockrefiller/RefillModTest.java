package my.celium.org.autoblockrefiller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import my.celium.org.Mycel;
import my.celium.org.internal.PlatformHolder;

/** The mod boots on Mycel with a tiny, well-defaulted config. */
class RefillModTest {
    @BeforeAll
    static void initOnce(@TempDir Path tempDir) {
        PlatformHolder.setForTesting(new RefillTestPlatform(tempDir));
        RefillMod.init();
    }

    @Test
    void registersWithMycel() {
        assertTrue(Mycel.getRegisteredMods().contains(RefillMod.META));
        assertEquals("autoblockrefiller", RefillMod.META.id());
    }

    @Test
    void configDefaults() {
        assertTrue(RefillMod.enabled.get());
        assertTrue(!RefillMod.searchHotbar.get());
        assertTrue(RefillMod.enabled.isDefault());
    }

    @Test
    void enableToggleFlowsThrough() {
        assertTrue(RefillMod.isModEnabled());
        Mycel.setEnabled(RefillMod.ID, false);
        try {
            assertTrue(!RefillMod.isModEnabled());
        } finally {
            Mycel.setEnabled(RefillMod.ID, true);
        }
        assertTrue(RefillMod.isModEnabled());
    }
}
