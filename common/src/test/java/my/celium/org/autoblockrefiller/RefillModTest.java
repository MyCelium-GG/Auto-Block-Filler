package my.celium.org.autoblockrefiller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import my.celium.org.Mycel;
import my.celium.org.testing.MycelTesting;

/** The mod boots on Mycel with a tiny, well-defaulted config. */
class RefillModTest {
    @BeforeAll
    static void initOnce(@TempDir Path tempDir) {
        MycelTesting.useConfigDir(tempDir);
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
        assertTrue(RefillMod.refillItems.get());
        assertTrue(RefillMod.refillSound.get());
        assertTrue(RefillMod.runningLowWarning.get());
        assertTrue(RefillMod.playerToggle.get());
        assertTrue(RefillMod.winter.get());
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
