package world.bentobox.challenges;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.UnsafeValues;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.Settings;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Addon.State;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.DatabaseSetup.DatabaseType;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.FlagsManager;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;

/**
 * @author tastybento
 *
 */
@SuppressWarnings("deprecation")
public class ChallengesAddonTest {

    @Mock
    private User user;
    @Mock
    private IslandsManager im;
    @Mock
    private Island island;

    private ChallengesAddon addon;
    @Mock
    private BentoBox plugin;
    @Mock
    private FlagsManager fm;
    @Mock
    private Settings settings;
    @Mock
    private GameModeAddon gameMode;
    @Mock
    private AddonsManager am;
    @Mock
    private BukkitScheduler scheduler;
    @Mock
    private PlaceholdersManager phm;

    private AutoCloseable closeable;
    private MockedStatic<Bukkit> mockedBukkit;

    @BeforeEach
    public void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);
        // Set up plugin
        WhiteBox.setInternalState(BentoBox.class, "instance", plugin);
        when(plugin.getLogger()).thenReturn(Logger.getAnonymousLogger());
        // Command manager
        CommandsManager cm = mock(CommandsManager.class);
        when(plugin.getCommandsManager()).thenReturn(cm);

        // Placeholders manager
        when(plugin.getPlaceholdersManager()).thenReturn(phm);

        // Player
        Player p = mock(Player.class);
        when(user.isOp()).thenReturn(false);
        UUID uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(p);
        when(user.getName()).thenReturn("tastybento");
        User.setPlugin(plugin);

        // Island World Manager
        IslandWorldManager iwm = mock(IslandWorldManager.class);
        when(plugin.getIWM()).thenReturn(iwm);

        // Player has island to begin with
        island = mock(Island.class);
        when(im.getIsland(Mockito.any(), Mockito.any(UUID.class))).thenReturn(island);
        when(plugin.getIslands()).thenReturn(im);

        // Locales
        when(user.getTranslation(Mockito.anyString())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(0, String.class));

        // Server
        mockedBukkit = Mockito.mockStatic(Bukkit.class);
        Server server = mock(Server.class);
        mockedBukkit.when(Bukkit::getServer).thenReturn(server);
        mockedBukkit.when(Bukkit::getLogger).thenReturn(Logger.getAnonymousLogger());
        mockedBukkit.when(Bukkit::getPluginManager).thenReturn(mock(PluginManager.class));

        // Addon
        addon = new ChallengesAddon();
        File jFile = new File("addon.jar");
        List<String> lines = Arrays.asList("# ChallengesAddon Configuration", "uniqueId: config");
        Path path = Paths.get("config.yml");
        Files.write(path, lines, StandardCharsets.UTF_8);
        try (JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(jFile))) {
            addToJar(tempJarOutputStream, path);
            addToJar(tempJarOutputStream, Paths.get("src/main/resources/panels/gamemode_panel.yml"));
            addToJar(tempJarOutputStream, Paths.get("src/main/resources/panels/main_panel.yml"));
            addToJar(tempJarOutputStream, Paths.get("src/main/resources/panels/multiple_panel.yml"));
            addToJar(tempJarOutputStream, Paths.get("src/main/resources/template.yml"));
            addToJar(tempJarOutputStream, Paths.get("src/main/resources/default.json"));
        }
        File dataFolder = new File("addons/Challenges");
        addon.setDataFolder(dataFolder);
        addon.setFile(jFile);
        AddonDescription desc = new AddonDescription.Builder("bentobox", "challenges", "1.3").description("test").authors("BONNe").build();
        addon.setDescription(desc);
        // Addons manager
        when(plugin.getAddonsManager()).thenReturn(am);
        // One game mode
        when(am.getGameModeAddons()).thenReturn(Collections.singletonList(gameMode));
        AddonDescription desc2 = new AddonDescription.Builder("bentobox", "BSkyBlock", "1.3").description("test").authors("tasty").build();
        when(gameMode.getDescription()).thenReturn(desc2);

        // Player command
        CompositeCommand cmd = mock(CompositeCommand.class);
        @NonNull
        Optional<CompositeCommand> opCmd = Optional.of(cmd);
        when(gameMode.getPlayerCommand()).thenReturn(opCmd);
        when(gameMode.getAdminCommand()).thenReturn(opCmd);

        // Flags manager
        when(plugin.getFlagsManager()).thenReturn(fm);
        when(fm.getFlags()).thenReturn(Collections.emptyList());

        when(plugin.getSettings()).thenReturn(settings);
        DatabaseType value = DatabaseType.JSON;
        when(settings.getDatabaseType()).thenReturn(value);

        // Bukkit
        mockedBukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
        ItemMeta meta = mock(ItemMeta.class);
        ItemFactory itemFactory = mock(ItemFactory.class);
        when(itemFactory.getItemMeta(any())).thenReturn(meta);
        mockedBukkit.when(Bukkit::getItemFactory).thenReturn(itemFactory);
        UnsafeValues unsafe = mock(UnsafeValues.class);
        when(unsafe.getDataVersion()).thenReturn(777);
        mockedBukkit.when(Bukkit::getUnsafe).thenReturn(unsafe);
    }

    private void addToJar(JarOutputStream tempJarOutputStream, Path path) throws IOException {
        try (FileInputStream fis = new FileInputStream(path.toFile())) {
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            JarEntry entry = new JarEntry(path.toString().replace("src/main/resources/", ""));
            tempJarOutputStream.putNextEntry(entry);
            while ((bytesRead = fis.read(buffer)) != -1) {
                tempJarOutputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        mockedBukkit.closeOnDemand();
        closeable.close();
        Mockito.framework().clearInlineMocks();
        new File("addon.jar").delete();
        new File("config.yml").delete();
        deleteAll(new File("addons"));
        deleteAll(new File("database"));
        deleteAll(new File("database_backup"));
    }

    private void deleteAll(File file) throws IOException {
        if (file.exists()) {
            Files.walk(file.toPath())
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
        }
    }

    @Test
    public void testOnLoad() {
        addon.onLoad();
        File check = new File("addons/Challenges", "config.yml");
        assertTrue(check.exists());
    }

    @Test
    public void testOnEnableDisabledPlugin() {
        when(plugin.isEnabled()).thenReturn(false);
        addon.onEnable();
        verify(plugin).logError("[challenges] BentoBox is not available or disabled!");
        assertEquals(Addon.State.DISABLED, addon.getState());
    }

    @Test
    public void testOnEnableDisabledAddon() {
        when(plugin.isEnabled()).thenReturn(true);
        addon.setState(State.DISABLED);
        addon.onEnable();
        verify(plugin).logError("[challenges] Challenges Addon is not available or disabled!");
    }

    @Test
    public void testOnEnableIncompatibleDatabase() {
        DatabaseType value = DatabaseType.YAML;
        when(settings.getDatabaseType()).thenReturn(value);
        when(plugin.isEnabled()).thenReturn(true);
        addon.setState(State.LOADED);
        addon.onEnable();
        verify(plugin).logError("[challenges] BentoBox database is not compatible with Challenges Addon.");
        verify(plugin).logError("[challenges] Please use JSON based database type.");
        assertEquals(State.INCOMPATIBLE, addon.getState());
    }

    @Test
    public void testOnEnableHooked() {
        addon.onLoad();
        when(plugin.isEnabled()).thenReturn(true);
        addon.setState(State.LOADED);
        addon.onEnable();
        verify(plugin).log("[challenges] Loading challenges...");
        verify(plugin, never()).logError("Challenges could not hook into AcidIsland or BSkyBlock so will not do anything!");
    }

    @Test
    public void testOnEnableNotHooked() {
        addon.onLoad();
        when(am.getGameModeAddons()).thenReturn(Collections.emptyList());
        when(plugin.isEnabled()).thenReturn(true);
        addon.setState(State.LOADED);
        addon.onEnable();
        verify(plugin).log("[challenges] Loading challenges...");
        verify(plugin).logError("[challenges] Challenges could not hook into AcidIsland or BSkyBlock so will not do anything!");
    }

    @Test
    public void testOnReloadNotHooked() {
        addon.onReload();
        verify(plugin, never()).log(anyString());
    }

    @Test
    public void testOnDisable() {
        this.testOnEnableHooked();
        addon.onDisable();

        File chDir = new File("database", "Challenge");
        assertTrue(chDir.exists());
        File lvDir = new File("database", "ChallengeLevel");
        assertTrue(lvDir.exists());
    }

    @Test
    public void testGetChallengesManager() {
        assertNull(addon.getChallengesManager());
        this.testOnEnableHooked();
        assertNotNull(addon.getChallengesManager());
    }

    @Test
    public void testGetPermissionPrefix() {
        assertEquals("addon.", addon.getPermissionPrefix());
    }

    @Test
    public void testGetImportManager() {
        assertNull(addon.getImportManager());
        this.testOnEnableHooked();
        assertNotNull(addon.getImportManager());
    }

    @Test
    public void testGetWebManager() {
        assertNull(addon.getWebManager());
        this.testOnEnableHooked();
        assertNotNull(addon.getWebManager());
    }

    @Test
    public void testGetChallengesSettings() {
        assertNull(addon.getChallengesSettings());
        addon.onLoad();
        assertNotNull(addon.getChallengesSettings());
    }

    @Test
    public void testIsEconomyProvided() {
        assertFalse(addon.isEconomyProvided());
    }

    @Test
    public void testGetEconomyProvider() {
        assertNull(addon.getEconomyProvider());
    }

    @Test
    public void testIsLevelProvided() {
        assertFalse(addon.isLevelProvided());
    }

    @Test
    public void testGetLevelAddon() {
        assertNull(addon.getLevelAddon());
    }

}
