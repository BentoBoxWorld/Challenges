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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.UnsafeValues;
import org.bukkit.World;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.DatabaseSetup.DatabaseType;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.config.Settings;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.Challenge.ChallengeType;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.database.object.requirements.IslandRequirements;
import world.bentobox.challenges.events.ChallengeCompletedEvent;
import world.bentobox.challenges.events.ChallengeResetAllEvent;
import world.bentobox.challenges.events.ChallengeResetEvent;
import world.bentobox.challenges.events.LevelCompletedEvent;
import world.bentobox.challenges.managers.ChallengesManager;
import world.bentobox.challenges.utils.LevelStatus;

/**
 * @author tastybento
 *
 */
@SuppressWarnings("deprecation")
public class ChallengesManagerTest {

    // Constants
    private static final String GAME_MODE_NAME = "BSkyBlock";

    // Mocks
    @Mock
    private ChallengesAddon addon;

    private Settings settings;
    @Mock
    private IslandWorldManager iwm;
    @Mock
    private Server server;
    @Mock
    private PluginManager pim;
    @Mock
    private ItemFactory itemFactory;
    @Mock
    private User user;
    @Mock
    private World world;
    @Mock
    private GameModeAddon gameModeAddon;
    @Mock
    private PlaceholdersManager plhm;

    // Variable fields
    private ChallengesManager cm;
    private File database;
    private Challenge challenge;
    private @NonNull ChallengeLevel level;
    private final UUID playerID = UUID.randomUUID();
    private String cName;
    private String levelName;

    private AutoCloseable closeable;
    private ServerMock mbServer;
    private MockedStatic<Bukkit> mockedBukkit;
    private MockedStatic<Util> mockedUtil;

    @BeforeEach
    public void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);
        mbServer = MockBukkit.mock();
        @SuppressWarnings("unused")
        var unusedTagRef = org.bukkit.Tag.LEAVES;
        // Database folder must exist before any DatabaseType class init,
        // because YamlDatabaseConnector resolves it from BentoBox.getInstance().getDataFolder().
        database = new File("database");
        cleanupFiles();

        // Set up plugin
        BentoBox plugin = mock(BentoBox.class);
        when(plugin.getDataFolder()).thenReturn(new File("."));
        WhiteBox.setInternalState(BentoBox.class, "instance", plugin);
        when(addon.getPlugin()).thenReturn(plugin);
        User.setPlugin(plugin);

        // IWM
        when(plugin.getIWM()).thenReturn(iwm);
        when(iwm.inWorld(any(World.class))).thenReturn(true);

        // Placeholders
        when(plugin.getPlaceholdersManager()).thenReturn(plhm);

        // Force DatabaseType enum class init now, before any in-progress
        // Mockito stubbing — its <clinit> calls plugin.getDataFolder() (a mock
        // invocation), which would otherwise trip UnfinishedStubbingException.
        DatabaseType jsonType = DatabaseType.JSON;

        // Settings for Database
        world.bentobox.bentobox.Settings s = mock(world.bentobox.bentobox.Settings.class);
        when(plugin.getSettings()).thenReturn(s);
        when(s.getDatabaseType()).thenReturn(jsonType);

        // Addon Settings
        settings = new Settings();
        when(addon.getChallengesSettings()).thenReturn(settings);
        settings.setStoreAsIslandData(false);
        settings.setStoreHistory(true);
        settings.setLifeSpan(10);

        // Bukkit — delegate to MockBukkit's real ItemFactory.
        mockedBukkit = Mockito.mockStatic(Bukkit.class, Mockito.RETURNS_DEEP_STUBS);
        mockedBukkit.when(Bukkit::getServer).thenReturn(server);
        mockedBukkit.when(Bukkit::getPluginManager).thenReturn(pim);
        mockedBukkit.when(() -> Bukkit.getWorld(anyString())).thenReturn(world);
        mockedBukkit.when(Bukkit::getItemFactory).thenReturn(mbServer.getItemFactory());
        UnsafeValues unsafe = mock(UnsafeValues.class);
        when(unsafe.getDataVersion()).thenReturn(777);
        mockedBukkit.when(Bukkit::getUnsafe).thenReturn(unsafe);

        // Challenge
        challenge = new Challenge();
        String uuid = UUID.randomUUID().toString();
        challenge.setUniqueId(GAME_MODE_NAME + "_" + uuid);
        challenge.setFriendlyName("name");
        challenge.setLevel(GAME_MODE_NAME + "_novice");
        challenge.setDescription(Collections.singletonList("A description"));
        challenge.setRequirements(new IslandRequirements());

        // Challenge Level
        level = new ChallengeLevel();
        levelName = GAME_MODE_NAME + "_novice";
        level.setUniqueId(levelName);
        level.setFriendlyName("Novice");

        // User
        when(user.getUniqueId()).thenReturn(playerID);

        // Util
        mockedUtil = Mockito.mockStatic(Util.class, Mockito.CALLS_REAL_METHODS);
        mockedUtil.when(() -> Util.getWorld(any())).thenReturn(world);

        // Addon
        AddonDescription desc = new AddonDescription.Builder("main", GAME_MODE_NAME, "1.0").build();
        when(gameModeAddon.getDescription()).thenReturn(desc);
        Optional<GameModeAddon> opAddon = Optional.of(gameModeAddon);
        when(iwm.getAddon(any())).thenReturn(opAddon);

        // Challenge name
        cName = GAME_MODE_NAME + "_" + uuid;

        // Class under test
        cm = new ChallengesManager(addon);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (mockedBukkit != null) mockedBukkit.closeOnDemand();
        if (mockedUtil != null) mockedUtil.closeOnDemand();
        if (closeable != null) closeable.close();
        MockBukkit.unmock();
        Mockito.framework().clearInlineMocks();
        cleanupFiles();
    }

    private void cleanupFiles() throws IOException {
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
    public void testLoad() throws InterruptedException {
        verify(addon).log("Loading challenges...");
        verify(addon, never()).logError(anyString());
        this.testSaveLevel();
        this.testSaveChallenge();
        cm.load();
        verify(addon, times(2)).log("Loading challenges...");
        verify(addon, never()).logError(anyString());
        assertTrue(cm.containsChallenge(cName));
    }

    @Test
    public void testReload() throws InterruptedException {
        cm.reload();
        verify(addon).log("Reloading challenges...");
        this.testSaveLevel();
        this.testSaveChallenge();
        cm.reload();
        verify(addon, times(2)).log("Reloading challenges...");
        verify(addon, never()).logError(anyString());
        assertTrue(cm.containsChallenge(cName));
    }

    @Test
    public void testLoadChallengeNoOverwriteSilent() {
        assertTrue(cm.loadChallenge(challenge, world, false, user, true));
        assertFalse(cm.loadChallenge(challenge, world, false, user, true));
    }

    @Test
    public void testLoadChallengeNoOverwriteNotSilent() {
        assertTrue(cm.loadChallenge(challenge, world, false, user, true));
        assertFalse(cm.loadChallenge(challenge, world, false, user, false));
        verify(user).getTranslation(world, "challenges.messages.load-skipping", "[value]", "name");
    }

    @Test
    public void testLoadChallengeOverwriteSilent() {
        assertTrue(cm.loadChallenge(challenge, world, false, user, true));
        assertTrue(cm.loadChallenge(challenge, world, true, user, true));
        verify(user, never()).getTranslation(any(World.class), anyString(), anyString(), anyString());
    }

    @Test
    public void testLoadChallengeOverwriteNotSilent() {
        assertTrue(cm.loadChallenge(challenge, world, false, user, true));
        assertTrue(cm.loadChallenge(challenge, world, true, user, false));
        verify(user).getTranslation(world, "challenges.messages.load-overwriting", "[value]", "name");
    }

    @Test
    public void testLoadLevelNoOverwriteSilent() {
        assertTrue(cm.loadLevel(level, world, false, user, true));
        assertFalse(cm.loadLevel(level, world, false, user, true));
    }

    @Test
    public void testLoadLevelNoOverwriteNotSilent() {
        assertTrue(cm.loadLevel(level, world, false, user, true));
        assertFalse(cm.loadLevel(level, world, false, user, false));
        verify(user).getTranslation(world, "challenges.messages.load-skipping", "[value]", "Novice");
    }

    @Test
    public void testLoadLevelOverwriteSilent() {
        assertTrue(cm.loadLevel(level, world, false, user, true));
        assertTrue(cm.loadLevel(level, world, true, user, true));
        verify(user, never()).getTranslation(any(World.class), anyString(), anyString(), anyString());
    }

    @Test
    public void testLoadLevelOverwriteNotSilent() {
        assertTrue(cm.loadLevel(level, world, false, user, true));
        assertTrue(cm.loadLevel(level, world, true, user, false));
        verify(user).getTranslation(world, "challenges.messages.load-overwriting", "[value]", "Novice");
    }

    @Disabled("This method does not do anything so there is no need to test right now.")
    @Test
    public void testRemoveFromCache() {
        cm.removeFromCache(playerID);
        verify(settings).isStoreAsIslandData();
    }

    @Test
    public void testWipeDatabase() throws InterruptedException {
        this.testLoad();

        File chDir = new File(database, "Challenge");
        File check = new File(chDir, cName + ".json");
        assertTrue(check.exists());

        File lvDir = new File(database, "ChallengeLevel");
        File checkLv = new File(lvDir, levelName + ".json");
        assertTrue(checkLv.exists());

        cm.setChallengeComplete(user, world, challenge, 20);
        File plData = new File(database, "ChallengesPlayerData");
        File checkPd = new File(plData, playerID.toString() + ".json");
        assertTrue(checkPd.exists());

        cm.wipeDatabase(false, "");

        assertFalse(check.exists());
        assertFalse(checkLv.exists());
        assertTrue(checkPd.exists());

        cm.wipeDatabase(true, "");
    }

    @Test
    public void testWipePlayers() throws InterruptedException {
        this.testLoad();
        cm.setChallengeComplete(user, world, challenge, 20);
        cm.save();
        File plData = new File(database, "ChallengesPlayerData");
        File checkLv = new File(plData, playerID.toString() + ".json");
        assertTrue(checkLv.exists());
        cm.wipePlayers("");
    }

    @Test
    public void testMigrateDatabase() {
        cm.migrateDatabase(user, world);
    }

    @Test
    public void testSave() {
        cm.save();
    }

    @Test
    public void testSaveChallenge() throws InterruptedException {
        cm.saveChallenge(challenge);
        Thread.sleep(500);
        File chDir = new File(database, "Challenge");
        assertTrue(chDir.exists());
        File check = new File(chDir, cName + ".json");
        assertTrue(check.exists());
        removeLine(check);
    }

    private void removeLine(File inputFile) {
        File tempFile = new File("myTempFile.json");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                String lineToRemove = "\"icon";
                String currentLine;

                while ((currentLine = reader.readLine()) != null) {
                    String trimmedLine = currentLine.trim();
                    if (trimmedLine.startsWith(lineToRemove)) continue;
                    writer.write(currentLine + System.getProperty("line.separator"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        tempFile.renameTo(inputFile);
    }

    @Test
    public void testSaveLevel() throws InterruptedException {
        cm.saveLevel(level);
        Thread.sleep(500);
        File chDir = new File(database, "ChallengeLevel");
        assertTrue(chDir.exists());
        File check = new File(chDir, GAME_MODE_NAME + "_novice.json");
        assertTrue(check.exists());
        removeLine(check);
    }

    @Test
    public void testIsChallengeCompleteUserWorldChallenge() {
        assertFalse(cm.isChallengeComplete(user, world, challenge));
    }

    @Test
    public void testIsChallengeCompleteUUIDWorldChallenge() {
        assertFalse(cm.isChallengeComplete(playerID, world, challenge));
    }

    @Test
    public void testIsChallengeCompleteUUIDWorldString() {
        assertFalse(cm.isChallengeComplete(playerID, world, "Novice"));
    }

    @Test
    public void testSetChallengeCompleteUserWorldChallengeInt() {
        cm.setChallengeComplete(user, world, challenge, 3);
        assertTrue(cm.isChallengeComplete(user, world, challenge));
        verify(pim).callEvent(any(ChallengeCompletedEvent.class));
    }

    @Test
    public void testSetChallengeCompleteUUIDWorldChallengeInt() {
        cm.setChallengeComplete(playerID, world, challenge, 3);
        assertTrue(cm.isChallengeComplete(playerID, world, challenge));
        verify(pim).callEvent(any(ChallengeCompletedEvent.class));
    }

    @Test
    public void testSetChallengeCompleteUUIDWorldChallengeUUID() {
        UUID adminID = UUID.randomUUID();
        cm.setChallengeComplete(playerID, world, challenge, adminID);
        assertTrue(cm.isChallengeComplete(playerID, world, challenge));
        verify(pim).callEvent(any(ChallengeCompletedEvent.class));
    }

    @Test
    public void testResetChallenge() {
        cm.setChallengeComplete(user, world, challenge, 3);
        assertTrue(cm.isChallengeComplete(user, world, challenge));
        cm.resetChallenge(playerID, world, challenge, playerID);
        assertFalse(cm.isChallengeComplete(user, world, challenge));
        verify(pim).callEvent(any(ChallengeResetEvent.class));
    }

    @Test
    public void testResetAllChallengesUserWorld() {
        cm.setChallengeComplete(user, world, challenge, 3);
        assertTrue(cm.isChallengeComplete(user, world, challenge));
        cm.resetAllChallenges(user, world);
        assertFalse(cm.isChallengeComplete(user, world, challenge));
        verify(pim).callEvent(any(ChallengeResetAllEvent.class));
    }

    @Test
    public void testResetAllChallengesUUIDWorldUUID() {
        cm.setChallengeComplete(user, world, challenge, 3);
        assertTrue(cm.isChallengeComplete(user, world, challenge));
        cm.resetAllChallenges(playerID, world, playerID);
        assertFalse(cm.isChallengeComplete(user, world, challenge));
        verify(pim).callEvent(any(ChallengeResetAllEvent.class));
    }

    @Test
    public void testGetChallengeTimesUserWorldChallenge() {
        assertEquals(0L, cm.getChallengeTimes(user, world, challenge));
        cm.setChallengeComplete(user, world, challenge, 6);
        assertEquals(6L, cm.getChallengeTimes(user, world, challenge));
    }

    @Test
    public void testGetChallengeTimesUserWorldString() {
        assertEquals(0L, cm.getChallengeTimes(user, world, cName));
        cm.setChallengeComplete(user, world, challenge, 6);
        assertEquals(6L, cm.getChallengeTimes(user, world, cName));
    }

    @Test
    public void testIsLevelCompleted() {
        assertFalse(cm.isLevelCompleted(user, world, level));
    }

    @Test
    public void testIsLevelUnlocked() {
        assertFalse(cm.isLevelUnlocked(user, world, level));
        this.testLoadLevelNoOverwriteSilent();
        assertTrue(cm.isLevelUnlocked(user, world, level));
    }

    @Test
    public void testSetLevelComplete() {
        assertFalse(cm.isLevelCompleted(user, world, level));
        cm.setLevelComplete(user, world, level);
        assertTrue(cm.isLevelCompleted(user, world, level));
        verify(pim).callEvent(any(LevelCompletedEvent.class));
    }

    @Test
    public void testValidateLevelCompletion() {
        assertTrue(cm.validateLevelCompletion(user, world, level));
    }

    @Test
    public void testGetChallengeLevelStatus() {
        this.testLoadLevelNoOverwriteSilent();
        LevelStatus cls = cm.getChallengeLevelStatus(playerID, world, level);
        assertTrue(cls.getNumberOfChallengesStillToDo() == 0);
        assertEquals(level, cls.getLevel());
        assertTrue(cls.isComplete());
        assertTrue(cls.isUnlocked());
        assertEquals("BSkyBlock_novice", cls.getLevel().getUniqueId());
    }

    @Test
    public void testGetAllChallengeLevelStatus() {
        this.testLoadLevelNoOverwriteSilent();
        List<LevelStatus> list = cm.getAllChallengeLevelStatus(user, world);
        assertTrue(list.size() == 1);
        LevelStatus cls = list.get(0);
        assertTrue(cls.getNumberOfChallengesStillToDo() == 0);
        assertEquals(level, cls.getLevel());
        assertTrue(cls.isComplete());
        assertTrue(cls.isUnlocked());
        assertEquals("BSkyBlock_novice", cls.getLevel().getUniqueId());
    }

    @Test
    public void testGetAllChallengesNames() {
        assertTrue(cm.getAllChallengesNames(world).isEmpty());
        cm.saveChallenge(challenge);
        cm.loadChallenge(challenge, world, false, user, true);
        List<String> list = cm.getAllChallengesNames(world);
        assertFalse(list.isEmpty());
        assertEquals(cName, list.get(0));
    }

    @Test
    public void testGetAllChallenges() {
        assertTrue(cm.getAllChallenges(world).isEmpty());
        cm.saveChallenge(challenge);
        cm.loadChallenge(challenge, world, false, user, true);
        List<Challenge> list = cm.getAllChallenges(world);
        assertFalse(list.isEmpty());
        assertEquals(challenge, list.get(0));
    }

    @Test
    public void testGetFreeChallenges() {
        assertTrue(cm.getFreeChallenges(world).isEmpty());
        cm.saveChallenge(challenge);
        cm.loadChallenge(challenge, world, false, user, true);
        assertTrue(cm.getFreeChallenges(world).isEmpty());
        challenge.setLevel("");
        cm.saveChallenge(challenge);
        cm.loadChallenge(challenge, world, false, user, true);
        List<Challenge> list = cm.getFreeChallenges(world);
        assertFalse(list.isEmpty());
        assertEquals(challenge, list.get(0));
    }

    @Test
    public void testGetLevelChallenges() throws InterruptedException {
        assertTrue(cm.getLevelChallenges(level).isEmpty());
        this.testSaveLevel();
        this.testSaveChallenge();
        level.setChallenges(Collections.singleton(challenge.getUniqueId()));
        List<Challenge> list = cm.getLevelChallenges(level);
        assertFalse(list.isEmpty());
        assertEquals(challenge, list.get(0));
    }

    @Test
    public void testGetChallenge() throws InterruptedException {
        assertNull(cm.getChallenge(cName));
        this.testSaveLevel();
        this.testSaveChallenge();
        Challenge ch = cm.getChallenge(cName);
        assertNotNull(ch);
        assertEquals(cName, ch.getUniqueId());
    }

    @Test
    public void testContainsChallenge() {
        assertFalse(cm.containsChallenge("no-such-challenge"));
    }

    @Test
    public void testCreateChallenge() {
        @Nullable
        Challenge ch = cm.createChallenge("newChal", "newChal", ChallengeType.ISLAND_TYPE, new IslandRequirements());
        assertEquals(ChallengeType.ISLAND_TYPE, ch.getChallengeType());
        assertEquals("newChal", ch.getUniqueId());
    }

    @Test
    public void testDeleteChallenge() throws InterruptedException {
        this.testSaveLevel();
        this.testSaveChallenge();
        Challenge ch = cm.getChallenge(cName);
        assertNotNull(ch);
        assertEquals(cName, ch.getUniqueId());
        cm.deleteChallenge(challenge);
        ch = cm.getChallenge(cName);
        assertNull(ch);
    }

    @Test
    public void testGetLevels() {
        this.testGetLevelString();
        List<ChallengeLevel> lvs = cm.getLevels(world);
        assertFalse(lvs.isEmpty());
        assertEquals(level, lvs.get(0));
    }

    @Test
    public void testGetLevelChallenge() {
        this.testGetLevelString();
        assertEquals(level, cm.getLevel(challenge));
    }

    @Test
    public void testGetLevelString() {
        assertNull(cm.getLevel("dss"));
        cm.saveLevel(level);
        cm.loadLevel(level, world, false, user, true);
        assertEquals(level, cm.getLevel(levelName));
    }

    @Test
    public void testContainsLevel() {
        this.testGetLevelString();
        assertFalse(cm.containsLevel("sdsd"));
        assertTrue(cm.containsLevel(levelName));
    }

    @Test
    public void testAddChallengeToLevel() throws InterruptedException {
        this.testLoad();
        cm.deleteChallenge(challenge);
        assertFalse(cm.containsChallenge(cName));
        cm.addChallengeToLevel(challenge, level);
        assertEquals(level, cm.getLevel(challenge));
    }

    @Test
    public void testRemoveChallengeFromLevel() throws InterruptedException {
        this.testAddChallengeToLevel();
        cm.removeChallengeFromLevel(challenge, level);
        assertFalse(cm.containsChallenge(cName));
    }

    @Test
    public void testCreateLevel() {
        @Nullable
        ChallengeLevel cl = cm.createLevel("Expert", "Expert", world);
        assertEquals("Expert", cl.getUniqueId());
        assertEquals(world.getName(), cl.getWorld());
    }

    @Test
    public void testDeleteChallengeLevel() throws InterruptedException {
        this.testAddChallengeToLevel();
        assertTrue(cm.containsLevel(levelName));
        cm.deleteChallengeLevel(level);
        assertFalse(cm.containsLevel(levelName));
    }

    @Test
    public void testHasAnyChallengeDataWorld() throws InterruptedException {
        assertFalse(cm.hasAnyChallengeData(world));
        this.testLoad();
        assertTrue(cm.hasAnyChallengeData(world));
    }

    @Test
    public void testHasAnyChallengeDataString() throws InterruptedException {
        assertFalse(cm.hasAnyChallengeData("BSkyBlock"));
        this.testLoad();
        assertTrue(cm.hasAnyChallengeData("BSkyBlock"));
    }

}
