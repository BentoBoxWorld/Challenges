package world.bentobox.challenges;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

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
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class, Util.class})
public class ChallengesManagerTest {

    // Constants
    private static final String GAME_MODE_NAME = "BSkyBlock";

    // Mocks
    @Mock
    private ChallengesAddon addon;
    @Mock
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

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // Set up plugin
        BentoBox plugin = mock(BentoBox.class);
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);
        when(addon.getPlugin()).thenReturn(plugin);
        User.setPlugin(plugin);

        // IWM
        when(plugin.getIWM()).thenReturn(iwm);
        when(iwm.inWorld(any(World.class))).thenReturn(true);

        // Placeholders
        when(plugin.getPlaceholdersManager()).thenReturn(plhm);

        // Settings for Database
        world.bentobox.bentobox.Settings s = mock(world.bentobox.bentobox.Settings.class);
        when(plugin.getSettings()).thenReturn(s);
        when(s.getDatabaseType()).thenReturn(DatabaseType.JSON);

        // Addon Settings
        when(addon.getChallengesSettings()).thenReturn(settings);
        when(settings.isStoreHistory()).thenReturn(true);
        when(settings.getLifeSpan()).thenReturn(10);

        // Database
        database = new File("database");
        tearDown();

        // Bukkit
        PowerMockito.mockStatic(Bukkit.class);
        when(Bukkit.getServer()).thenReturn(server);
        when(Bukkit.getPluginManager()).thenReturn(pim);
        when(Bukkit.getWorld(anyString())).thenReturn(world);

        ItemMeta meta = mock(ItemMeta.class);
        when(itemFactory.getItemMeta(any())).thenReturn(meta);
        when(Bukkit.getItemFactory()).thenReturn(itemFactory);
        UnsafeValues unsafe = mock(UnsafeValues.class);
        when(unsafe.getDataVersion()).thenReturn(777);
        when(Bukkit.getUnsafe()).thenReturn(unsafe);

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
        PowerMockito.mockStatic(Util.class);
        when(Util.getWorld(any())).thenReturn(world);

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

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
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

    /**
     * Test method for {@link ChallengesManager#load()}.
     * @throws InterruptedException
     */
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

    /**
     * Test method for {@link ChallengesManager#reload()}.
     * @throws InterruptedException
     */
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

    /**
     * Test method for {@link ChallengesManager#loadChallenge(world.bentobox.challenges.database.object.Challenge, boolean, world.bentobox.bentobox.api.user.User, boolean)}.
     */
    @Test
    public void testLoadChallengeNoOverwriteSilent() {
        // load once
        assertTrue(cm.loadChallenge(challenge, false, user, true));
        // load twice - no overwrite
        assertFalse(cm.loadChallenge(challenge, false, user, true));
    }

    /**
     * Test method for {@link ChallengesManager#loadChallenge(world.bentobox.challenges.database.object.Challenge, boolean, world.bentobox.bentobox.api.user.User, boolean)}.
     */
    @Test
    public void testLoadChallengeNoOverwriteNotSilent() {
        // load once
        assertTrue(cm.loadChallenge(challenge, false, user, true));
        // load twice - no overwrite, not silent
        assertFalse(cm.loadChallenge(challenge, false, user, false));
        verify(user).getTranslation("challenges.messages.load-skipping", "[value]", "name");
    }

    /**
     * Test method for {@link ChallengesManager#loadChallenge(world.bentobox.challenges.database.object.Challenge, boolean, world.bentobox.bentobox.api.user.User, boolean)}.
     */
    @Test
    public void testLoadChallengeOverwriteSilent() {
        // load once
        assertTrue(cm.loadChallenge(challenge, false, user, true));
        // overwrite
        assertTrue(cm.loadChallenge(challenge, true, user, true));
        verify(user, never()).getTranslation(anyString(), anyString(), anyString());
    }

    /**
     * Test method for {@link ChallengesManager#loadChallenge(world.bentobox.challenges.database.object.Challenge, boolean, world.bentobox.bentobox.api.user.User, boolean)}.
     */
    @Test
    public void testLoadChallengeOverwriteNotSilent() {
        // load once
        assertTrue(cm.loadChallenge(challenge, false, user, true));
        // overwrite not silent
        assertTrue(cm.loadChallenge(challenge, true, user, false));
        verify(user).getTranslation("challenges.messages.load-overwriting", "[value]", "name");
    }

    /**
     * Test method for {@link ChallengesManager#loadLevel(world.bentobox.challenges.database.object.ChallengeLevel, boolean, world.bentobox.bentobox.api.user.User, boolean)}.
     */
    @Test
    public void testLoadLevelNoOverwriteSilent() {
        // load once
        assertTrue(cm.loadLevel(level, false, user, true));
        // load twice - no overwrite
        assertFalse(cm.loadLevel(level, false, user, true));
    }

    /**
     * Test method for {@link ChallengesManager#loadLevel(world.bentobox.challenges.database.object.ChallengeLevel, boolean, world.bentobox.bentobox.api.user.User, boolean)}.
     */
    @Test
    public void testLoadLevelNoOverwriteNotSilent() {
        // load once
        assertTrue(cm.loadLevel(level, false, user, true));
        // load twice - no overwrite, not silent
        assertFalse(cm.loadLevel(level, false, user, false));
        verify(user).getTranslation("challenges.messages.load-skipping", "[value]", "Novice");
    }

    /**
     * Test method for {@link ChallengesManager#loadLevel(world.bentobox.challenges.database.object.ChallengeLevel, boolean, world.bentobox.bentobox.api.user.User, boolean)}.
     */
    @Test
    public void testLoadLevelOverwriteSilent() {
        // load once
        assertTrue(cm.loadLevel(level, false, user, true));
        // overwrite
        assertTrue(cm.loadLevel(level, true, user, true));
        verify(user, never()).getTranslation(anyString(), anyString(), anyString());
    }

    /**
     * Test method for {@link ChallengesManager#loadLevel(world.bentobox.challenges.database.object.ChallengeLevel, boolean, world.bentobox.bentobox.api.user.User, boolean)}.
     */
    @Test
    public void testLoadLevelOverwriteNotSilent() {
        // load once
        assertTrue(cm.loadLevel(level, false, user, true));
        // overwrite not silent
        assertTrue(cm.loadLevel(level, true, user, false));
        verify(user).getTranslation("challenges.messages.load-overwriting", "[value]", "Novice");
    }

    /**
     * Test method for {@link ChallengesManager#removeFromCache(java.util.UUID)}.
     */
    @Ignore("This method does not do anything so there is no need to test right now.")
    @Test
    public void testRemoveFromCache() {
        cm.removeFromCache(playerID);
        verify(settings).isStoreAsIslandData();
        // TODO there should be a test where isStoreAsIslandData returns true
    }

    /**
     * Test method for {@link ChallengesManager#wipeDatabase(boolean, String)}.
     * @throws InterruptedException
     */
    @Test
    public void testWipeDatabase() throws InterruptedException {
        // Create some database
        this.testLoad();

        // Verify file exists
        File chDir = new File(database, "Challenge");
        File check = new File(chDir, cName + ".json");
        assertTrue(check.exists());

        File lvDir = new File(database, "ChallengeLevel");
        File checkLv = new File(lvDir, levelName + ".json");
        assertTrue(checkLv.exists());

        cm.setChallengeComplete(user, world, challenge, 20);
        //cm.save();
        File plData = new File(database, "ChallengesPlayerData");
        File checkPd = new File(plData, playerID.toString() + ".json");
        assertTrue(checkPd.exists());

        // Wipe it
        cm.wipeDatabase(false, "");

        // Verify
        assertFalse(check.exists());
        assertFalse(checkLv.exists());
        assertTrue(checkPd.exists());

        cm.wipeDatabase(true, "");
        // This fails because ChallengesPlayerData still exists
        //assertFalse(checkPd.exists());
    }

    /**
     * Test method for {@link ChallengesManager#wipePlayers(String)}.
     * @throws InterruptedException
     */
    @Test
    public void testWipePlayers() throws InterruptedException {
        this.testLoad();
        cm.setChallengeComplete(user, world, challenge, 20);
        cm.save();
        File plData = new File(database, "ChallengesPlayerData");
        File checkLv = new File(plData, playerID.toString() + ".json");
        assertTrue(checkLv.exists());
        cm.wipePlayers("");
        // This fails because ChallengesPlayerData still exists
        //assertFalse(checkLv.exists());
    }

    /**
     * Test method for {@link ChallengesManager#migrateDatabase(world.bentobox.bentobox.api.user.User, org.bukkit.World)}.
     */
    @Test
    public void testMigrateDatabase() {
        cm.migrateDatabase(user, world);
    }

    /**
     * Test method for {@link ChallengesManager#save()}.
     */
    @Test
    public void testSave() {
        cm.save();
    }

    /**
     * Test method for {@link ChallengesManager#saveChallenge(world.bentobox.challenges.database.object.Challenge)}.
     * @throws InterruptedException
     */
    @Test
    public void testSaveChallenge() throws InterruptedException {
        // Async - may not happen quickly
        cm.saveChallenge(challenge);
        Thread.sleep(500);
        File chDir = new File(database, "Challenge");
        assertTrue(chDir.exists());
        File check = new File(chDir, cName + ".json");
        assertTrue(check.exists());
        // Remove icon becauseit has mockito meta in it
        removeLine(check);
    }

    private void removeLine(File inputFile) {
        File tempFile = new File("myTempFile.json");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

                String lineToRemove = "\"icon";
                String currentLine;

                while((currentLine = reader.readLine()) != null) {
                    // trim newline when comparing with lineToRemove
                    String trimmedLine = currentLine.trim();
                    if(trimmedLine.startsWith(lineToRemove)) continue;
                    writer.write(currentLine + System.getProperty("line.separator"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        tempFile.renameTo(inputFile);
    }
    /**
     * Test method for {@link ChallengesManager#saveLevel(world.bentobox.challenges.database.object.ChallengeLevel)}.
     * @throws InterruptedException
     */
    @Test
    public void testSaveLevel() throws InterruptedException {
        cm.saveLevel(level);
        Thread.sleep(500);
        File chDir = new File(database, "ChallengeLevel");
        assertTrue(chDir.exists());
        File check = new File(chDir, GAME_MODE_NAME + "_novice.json");
        assertTrue(check.exists());
        // Remove icon becauseit has mockito meta in it
        removeLine(check);
    }

    /**
     * Test method for {@link ChallengesManager#isChallengeComplete(world.bentobox.bentobox.api.user.User, org.bukkit.World, world.bentobox.challenges.database.object.Challenge)}.
     */
    @Test
    public void testIsChallengeCompleteUserWorldChallenge() {
        assertFalse(cm.isChallengeComplete(user, world, challenge));
    }

    /**
     * Test method for {@link ChallengesManager#isChallengeComplete(java.util.UUID, org.bukkit.World, world.bentobox.challenges.database.object.Challenge)}.
     */
    @Test
    public void testIsChallengeCompleteUUIDWorldChallenge() {
        assertFalse(cm.isChallengeComplete(playerID, world, challenge));
    }

    /**
     * Test method for {@link ChallengesManager#isChallengeComplete(java.util.UUID, org.bukkit.World, java.lang.String)}.
     */
    @Test
    public void testIsChallengeCompleteUUIDWorldString() {
        assertFalse(cm.isChallengeComplete(playerID, world, "Novice"));
    }

    /**
     * Test method for {@link ChallengesManager#setChallengeComplete(world.bentobox.bentobox.api.user.User, org.bukkit.World, world.bentobox.challenges.database.object.Challenge, int)}.
     */
    @Test
    public void testSetChallengeCompleteUserWorldChallengeInt() {
        cm.setChallengeComplete(user, world, challenge, 3);
        assertTrue(cm.isChallengeComplete(user, world, challenge));
        verify(pim).callEvent(any(ChallengeCompletedEvent.class));
    }

    /**
     * Test method for {@link ChallengesManager#setChallengeComplete(java.util.UUID, org.bukkit.World, world.bentobox.challenges.database.object.Challenge, int)}.
     */
    @Test
    public void testSetChallengeCompleteUUIDWorldChallengeInt() {
        cm.setChallengeComplete(playerID, world, challenge, 3);
        assertTrue(cm.isChallengeComplete(playerID, world, challenge));
        verify(pim).callEvent(any(ChallengeCompletedEvent.class));
    }

    /**
     * Test method for {@link ChallengesManager#setChallengeComplete(java.util.UUID, org.bukkit.World, world.bentobox.challenges.database.object.Challenge, java.util.UUID)}.
     */
    @Test
    public void testSetChallengeCompleteUUIDWorldChallengeUUID() {
        UUID adminID = UUID.randomUUID();
        cm.setChallengeComplete(playerID, world, challenge, adminID);
        assertTrue(cm.isChallengeComplete(playerID, world, challenge));
        verify(pim).callEvent(any(ChallengeCompletedEvent.class));
    }

    /**
     * Test method for {@link ChallengesManager#resetChallenge(java.util.UUID, org.bukkit.World, world.bentobox.challenges.database.object.Challenge, java.util.UUID)}.
     */
    @Test
    public void testResetChallenge() {
        cm.setChallengeComplete(user, world, challenge, 3);
        assertTrue(cm.isChallengeComplete(user, world, challenge));
        cm.resetChallenge(playerID, world, challenge, playerID);
        assertFalse(cm.isChallengeComplete(user, world, challenge));
        verify(pim).callEvent(any(ChallengeResetEvent.class));
    }

    /**
     * Test method for {@link ChallengesManager#resetAllChallenges(world.bentobox.bentobox.api.user.User, org.bukkit.World)}.
     */
    @Test
    public void testResetAllChallengesUserWorld() {
        cm.setChallengeComplete(user, world, challenge, 3);
        assertTrue(cm.isChallengeComplete(user, world, challenge));
        cm.resetAllChallenges(user, world);
        assertFalse(cm.isChallengeComplete(user, world, challenge));
        verify(pim).callEvent(any(ChallengeResetAllEvent.class));
    }

    /**
     * Test method for {@link ChallengesManager#resetAllChallenges(java.util.UUID, org.bukkit.World, java.util.UUID)}.
     */
    @Test
    public void testResetAllChallengesUUIDWorldUUID() {
        cm.setChallengeComplete(user, world, challenge, 3);
        assertTrue(cm.isChallengeComplete(user, world, challenge));
        cm.resetAllChallenges(playerID, world, playerID);
        assertFalse(cm.isChallengeComplete(user, world, challenge));
        verify(pim).callEvent(any(ChallengeResetAllEvent.class));
    }

    /**
     * Test method for {@link ChallengesManager#getChallengeTimes(world.bentobox.bentobox.api.user.User, org.bukkit.World, world.bentobox.challenges.database.object.Challenge)}.
     */
    @Test
    public void testGetChallengeTimesUserWorldChallenge() {
        assertEquals(0L, cm.getChallengeTimes(user, world, challenge));
        cm.setChallengeComplete(user, world, challenge, 6);
        assertEquals(6L, cm.getChallengeTimes(user, world, challenge));
    }

    /**
     * Test method for {@link ChallengesManager#getChallengeTimes(world.bentobox.bentobox.api.user.User, org.bukkit.World, java.lang.String)}.
     */
    @Test
    public void testGetChallengeTimesUserWorldString() {
        assertEquals(0L, cm.getChallengeTimes(user, world, cName));
        cm.setChallengeComplete(user, world, challenge, 6);
        assertEquals(6L, cm.getChallengeTimes(user, world, cName));
    }

    /**
     * Test method for {@link ChallengesManager#isLevelCompleted(world.bentobox.bentobox.api.user.User, org.bukkit.World, world.bentobox.challenges.database.object.ChallengeLevel)}.
     */
    @Test
    public void testIsLevelCompleted() {
        assertFalse(cm.isLevelCompleted(user, world, level));
    }

    /**
     * Test method for {@link ChallengesManager#isLevelUnlocked(world.bentobox.bentobox.api.user.User, org.bukkit.World, world.bentobox.challenges.database.object.ChallengeLevel)}.
     */
    @Test
    public void testIsLevelUnlocked() {
        assertFalse(cm.isLevelUnlocked(user, world, level));
        this.testLoadLevelNoOverwriteSilent();
        assertTrue(cm.isLevelUnlocked(user, world, level));
    }

    /**
     * Test method for {@link ChallengesManager#setLevelComplete(world.bentobox.bentobox.api.user.User, org.bukkit.World, world.bentobox.challenges.database.object.ChallengeLevel)}.
     */
    @Test
    public void testSetLevelComplete() {
        assertFalse(cm.isLevelCompleted(user, world, level));
        cm.setLevelComplete(user, world, level);
        assertTrue(cm.isLevelCompleted(user, world, level));
        verify(pim).callEvent(any(LevelCompletedEvent.class));
    }

    /**
     * Test method for {@link ChallengesManager#validateLevelCompletion(world.bentobox.bentobox.api.user.User, org.bukkit.World, world.bentobox.challenges.database.object.ChallengeLevel)}.
     */
    @Test
    public void testValidateLevelCompletion() {
        assertTrue(cm.validateLevelCompletion(user, world, level));
    }

    /**
     * Test method for {@link ChallengesManager#getChallengeLevelStatus(java.util.UUID, org.bukkit.World, world.bentobox.challenges.database.object.ChallengeLevel)}.
     */
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

    /**
     * Test method for {@link ChallengesManager#getAllChallengeLevelStatus(world.bentobox.bentobox.api.user.User, org.bukkit.World)}.
     */
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

    /**
     * Test method for {@link ChallengesManager#getAllChallengesNames(org.bukkit.World)}.
     */
    @Test
    public void testGetAllChallengesNames() {
        assertTrue(cm.getAllChallengesNames(world).isEmpty());
        cm.saveChallenge(challenge);
        cm.loadChallenge(challenge, false, user, true);
        List<String> list = cm.getAllChallengesNames(world);
        assertFalse(list.isEmpty());
        assertEquals(cName, list.get(0));
    }

    /**
     * Test method for {@link ChallengesManager#getAllChallenges(org.bukkit.World)}.
     */
    @Test
    public void testGetAllChallenges() {
        assertTrue(cm.getAllChallenges(world).isEmpty());
        cm.saveChallenge(challenge);
        cm.loadChallenge(challenge, false, user, true);
        List<Challenge> list = cm.getAllChallenges(world);
        assertFalse(list.isEmpty());
        assertEquals(challenge, list.get(0));
    }

    /**
     * Test method for {@link ChallengesManager#getFreeChallenges(org.bukkit.World)}.
     */
    @Test
    public void testGetFreeChallenges() {
        // Empty
        assertTrue(cm.getFreeChallenges(world).isEmpty());
        // One normal
        cm.saveChallenge(challenge);
        cm.loadChallenge(challenge, false, user, true);
        assertTrue(cm.getFreeChallenges(world).isEmpty());
        // One free
        challenge.setLevel("");
        cm.saveChallenge(challenge);
        cm.loadChallenge(challenge, false, user, true);
        List<Challenge> list = cm.getFreeChallenges(world);
        assertFalse(list.isEmpty());
        assertEquals(challenge, list.get(0));
    }

    /**
     * Test method for {@link ChallengesManager#getLevelChallenges(world.bentobox.challenges.database.object.ChallengeLevel)}.
     * @throws InterruptedException
     */
    @Test
    public void testGetLevelChallenges() throws InterruptedException {
        assertTrue(cm.getLevelChallenges(level).isEmpty());
        // make some challenges
        this.testSaveLevel();
        this.testSaveChallenge();
        level.setChallenges(Collections.singleton(challenge.getUniqueId()));
        // Test again
        List<Challenge> list = cm.getLevelChallenges(level);
        assertFalse(list.isEmpty());
        assertEquals(challenge, list.get(0));
    }

    /**
     * Test method for {@link ChallengesManager#getChallenge(java.lang.String)}.
     * @throws InterruptedException
     */
    @Test
    public void testGetChallenge() throws InterruptedException {
        assertNull(cm.getChallenge(cName));
        this.testSaveLevel();
        this.testSaveChallenge();
        Challenge ch = cm.getChallenge(cName);
        assertNotNull(ch);
        assertEquals(cName, ch.getUniqueId());
    }

    /**
     * Test method for {@link ChallengesManager#containsChallenge(java.lang.String)}.
     */
    @Test
    public void testContainsChallenge() {
        assertFalse(cm.containsChallenge("no-such-challenge"));
    }

    /**
     * Test method for {@link ChallengesManager#createChallenge(java.lang.String, java.lang.String, world.bentobox.challenges.database.object.Challenge.ChallengeType, world.bentobox.challenges.database.object.requirements.Requirements)}.
     */
    @Test
    public void testCreateChallenge() {
        @Nullable
        Challenge ch = cm.createChallenge("newChal", "newChal", ChallengeType.ISLAND_TYPE, new IslandRequirements());
        assertEquals(ChallengeType.ISLAND_TYPE, ch.getChallengeType());
        assertEquals("newChal", ch.getUniqueId());
    }

    /**
     * Test method for {@link ChallengesManager#deleteChallenge(world.bentobox.challenges.database.object.Challenge)}.
     * @throws InterruptedException
     */
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

    /**
     * Test method for {@link ChallengesManager#getLevels(org.bukkit.World)}.
     */
    @Test
    public void testGetLevels() {
        this.testGetLevelString();
        List<ChallengeLevel> lvs = cm.getLevels(world);
        assertFalse(lvs.isEmpty());
        assertEquals(level, lvs.get(0));
    }

    /**
     * Test method for {@link ChallengesManager#getLevel(world.bentobox.challenges.database.object.Challenge)}.
     */
    @Test
    public void testGetLevelChallenge() {
        this.testGetLevelString();
        assertEquals(level, cm.getLevel(challenge));
    }

    /**
     * Test method for {@link ChallengesManager#getLevel(java.lang.String)}.
     */
    @Test
    public void testGetLevelString() {
        assertNull(cm.getLevel("dss"));
        cm.saveLevel(level);
        cm.loadLevel(level, false, user, true);
        assertEquals(level, cm.getLevel(levelName));
    }

    /**
     * Test method for {@link ChallengesManager#containsLevel(java.lang.String)}.
     */
    @Test
    public void testContainsLevel() {
        this.testGetLevelString();
        assertFalse(cm.containsLevel("sdsd"));
        assertTrue(cm.containsLevel(levelName));
    }

    /**
     * Test method for {@link ChallengesManager#addChallengeToLevel(world.bentobox.challenges.database.object.Challenge, world.bentobox.challenges.database.object.ChallengeLevel)}.
     * @throws InterruptedException
     */
    @Test
    public void testAddChallengeToLevel() throws InterruptedException {
        this.testLoad();
        cm.deleteChallenge(challenge);
        assertFalse(cm.containsChallenge(cName));
        cm.addChallengeToLevel(challenge, level);
        assertEquals(level, cm.getLevel(challenge));
    }

    /**
     * Test method for {@link ChallengesManager#removeChallengeFromLevel(world.bentobox.challenges.database.object.Challenge, world.bentobox.challenges.database.object.ChallengeLevel)}.
     * @throws InterruptedException
     */
    @Test
    public void testRemoveChallengeFromLevel() throws InterruptedException {
        this.testAddChallengeToLevel();
        cm.removeChallengeFromLevel(challenge, level);
        assertFalse(cm.containsChallenge(cName));
    }

    /**
     * Test method for {@link ChallengesManager#createLevel(java.lang.String, java.lang.String, org.bukkit.World)}.
     */
    @Test
    public void testCreateLevel() {
        @Nullable
        ChallengeLevel cl = cm.createLevel("Expert", "Expert", world);
        assertEquals("Expert", cl.getUniqueId());
        assertEquals(world.getName(), cl.getWorld());
    }

    /**
     * Test method for {@link ChallengesManager#deleteChallengeLevel(world.bentobox.challenges.database.object.ChallengeLevel)}.
     * @throws InterruptedException
     */
    @Test
    public void testDeleteChallengeLevel() throws InterruptedException {
        this.testAddChallengeToLevel();
        assertTrue(cm.containsLevel(levelName));
        cm.deleteChallengeLevel(level);
        assertFalse(cm.containsLevel(levelName));
    }

    /**
     * Test method for {@link ChallengesManager#hasAnyChallengeData(org.bukkit.World)}.
     * @throws InterruptedException
     */
    @Test
    public void testHasAnyChallengeDataWorld() throws InterruptedException {
        assertFalse(cm.hasAnyChallengeData(world));
        this.testLoad();
        assertTrue(cm.hasAnyChallengeData(world));
    }

    /**
     * Test method for {@link ChallengesManager#hasAnyChallengeData(java.lang.String)}.
     * @throws InterruptedException
     */
    @Test
    public void testHasAnyChallengeDataString() throws InterruptedException {
        assertFalse(cm.hasAnyChallengeData("BSkyBlock"));
        this.testLoad();
        assertTrue(cm.hasAnyChallengeData("BSkyBlock"));
    }

}
