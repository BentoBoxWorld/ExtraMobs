package world.bentobox.extramobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.extramobs.config.Settings;

/**
 * Tests for {@link ExtraMobsAddon}.
 */
class ExtraMobsAddonTest extends CommonTestSetup {

    private static final String CONFIG_YML = """
            disabled-gamemodes: []
            nether-chances:
              wither-skeleton: 0.01
              blaze: 0.1
            end-chances:
              shulker: 0.1
            overworld-chance:
              guardian: 0.1
            gamemode-settings: {}
            """;

    @Mock
    private AddonsManager am;

    private ExtraMobsAddon addon;
    private MockedStatic<DatabaseSetup> mockDb;

    @SuppressWarnings("unchecked")
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        // Database mock
        AbstractDatabaseHandler<Object> h = mock(AbstractDatabaseHandler.class);
        mockDb = Mockito.mockStatic(DatabaseSetup.class);
        DatabaseSetup dbSetup = mock(DatabaseSetup.class);
        mockDb.when(DatabaseSetup::getDatabase).thenReturn(dbSetup);
        when(dbSetup.getHandler(any())).thenReturn(h);
        when(h.saveObject(any())).thenReturn(CompletableFuture.completedFuture(true));

        // CommandsManager
        CommandsManager cm = mock(CommandsManager.class);
        when(plugin.getCommandsManager()).thenReturn(cm);

        // AddonsManager — no GameMode addons hooked by default
        when(plugin.getAddonsManager()).thenReturn(am);
        when(am.getGameModeAddons()).thenReturn(Collections.emptyList());

        // FlagsManager
        when(plugin.getFlagsManager()).thenReturn(fm);
        when(fm.getFlags()).thenReturn(Collections.emptyList());

        addon = new ExtraMobsAddon();
        File jFile = new File("addon.jar");
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jFile))) {
            addJarEntry(jos, "config.yml", CONFIG_YML);
        }
        File dataFolder = new File("addons/ExtraMobs");
        addon.setDataFolder(dataFolder);
        addon.setFile(jFile);
        AddonDescription desc = new AddonDescription.Builder("bentobox", "ExtraMobs", "1.0.0")
                .description("test").authors("BONNe").build();
        addon.setDescription(desc);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        if (mockDb != null) {
            mockDb.closeOnDemand();
        }
        super.tearDown();
        new File("addon.jar").delete();
        deleteAll(new File("addons"));
    }

    private static void addJarEntry(JarOutputStream jos, String name, String content) throws Exception {
        JarEntry entry = new JarEntry(name);
        jos.putNextEntry(entry);
        jos.write(content.getBytes(StandardCharsets.UTF_8));
        jos.closeEntry();
    }

    @Test
    void testGetSettingsNullBeforeLoad() {
        assertNull(addon.getSettings());
    }

    @Test
    void testIsHookedFalseBeforeEnable() {
        assertFalse(addon.isHooked());
    }

    @Test
    void testOnLoad() {
        addon.onLoad();
        assertNotNull(addon.getSettings());
    }

    @Test
    void testOnLoadSettingsDefaults() {
        addon.onLoad();
        Settings s = addon.getSettings();
        assertNotNull(s);
        assertEquals(0.01, s.getWitherSkeletonChance(), 1e-9);
        assertEquals(0.1, s.getBlazeChance(), 1e-9);
        assertEquals(0.1, s.getShulkerChance(), 1e-9);
        assertEquals(0.1, s.getGuardianChance(), 1e-9);
        assertNotNull(s.getDisabledGameModes());
        assertEquals(0, s.getDisabledGameModes().size());
    }

    @Test
    void testOnEnableWithoutGameModeDisablesAddon() {
        addon.onLoad();
        addon.onEnable();
        // No GameMode addons hooked → addon never sets hooked = true
        assertFalse(addon.isHooked());
    }

    @Test
    void testOnDisable() {
        addon.onDisable();
        assertNotNull(addon);
    }

    @Test
    void testOnReload() {
        addon.onLoad();
        addon.onReload();
        assertNotNull(addon.getSettings());
    }

    @Test
    void testOnReloadPreservesSettings() {
        addon.onLoad();
        addon.onReload();
        assertEquals(0.01, addon.getSettings().getWitherSkeletonChance(), 1e-9);
    }

    @Test
    void testGamemodeSettingsEmptyByDefault() {
        addon.onLoad();
        Settings s = addon.getSettings();
        assertNotNull(s.getGamemodeSettings());
        assertEquals(0, s.getGamemodeSettings().size());
    }

    @Test
    void testGetReplacementsEmptyWhenNoPerGamemodeConfig() {
        addon.onLoad();
        Settings s = addon.getSettings();
        assertTrue(s.getReplacements("BSkyBlock", "nether").isEmpty());
        assertTrue(s.getReplacements("BSkyBlock", "end").isEmpty());
        assertTrue(s.getReplacements("BSkyBlock", "world").isEmpty());
    }
}
