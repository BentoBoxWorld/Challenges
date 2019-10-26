package world.bentobox.challenges;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.UnsafeValues;
import org.bukkit.World;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.DatabaseSetup.DatabaseType;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.challenges.config.Settings;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.ChallengeLevel;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class})
public class ChallengesManagerTest {

    @Mock
    private ChallengesAddon addon;
    @Mock
    private Settings settings;
    @Mock
    private IslandWorldManager iwm;

    private ChallengesManager cm;
    private File database;
    @Mock
    private Server server;
    @Mock
    private PluginManager pim;
    @Mock
    private ItemFactory itemFactory;
    @Mock
    private User user;
    private String uuid;
    @Mock
    private World world;

    /**
     * @throws java.lang.Exception
     */
    @SuppressWarnings("deprecation")
    @Before
    public void setUp() throws Exception {
        // Set up plugin
        BentoBox plugin = mock(BentoBox.class);
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);
        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIWM()).thenReturn(iwm);
        when(iwm.inWorld(any(World.class))).thenReturn(true);

        // Settings for Database        
        world.bentobox.bentobox.Settings s = mock(world.bentobox.bentobox.Settings.class);
        when(plugin.getSettings()).thenReturn(s);
        when(s.getDatabaseType()).thenReturn(DatabaseType.JSON);

        // Settings
        when(addon.getChallengesSettings()).thenReturn(settings);
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

        cm = new ChallengesManager(addon);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        // Clean up JSON database
        // Clean up file system

            if (database.exists()) {
                Files.walk(database.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
            }
        
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#load()}.
     */
    @Test
    public void testLoad() {
        verify(addon).log("Loading challenges...");
        verify(addon, never()).logError(anyString());
        this.testSaveLevel();
        this.testSaveChallenge();
        cm.load();
       verify(addon, times(2)).log("Loading challenges...");
        verify(addon, never()).logError(anyString());
        assertTrue(cm.containsChallenge(uuid));
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#reload()}.
     */
    @Test
    public void testReload() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#loadChallenge(world.bentobox.challenges.database.object.Challenge, boolean, world.bentobox.bentobox.api.user.User, boolean)}.
     */
    @Test
    public void testLoadChallenge() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#loadLevel(world.bentobox.challenges.database.object.ChallengeLevel, boolean, world.bentobox.bentobox.api.user.User, boolean)}.
     */
    @Test
    public void testLoadLevel() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#removeFromCache(java.util.UUID)}.
     */
    @Test
    public void testRemoveFromCache() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#wipeDatabase(boolean)}.
     */
    @Test
    public void testWipeDatabase() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#wipePlayers()}.
     */
    @Test
    public void testWipePlayers() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#migrateDatabase(world.bentobox.bentobox.api.user.User, org.bukkit.World)}.
     */
    @Test
    public void testMigrateDatabase() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#save()}.
     */
    @Test
    public void testSave() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#saveChallenge(world.bentobox.challenges.database.object.Challenge)}.
     */
    @Test
    public void testSaveChallenge() {
        Challenge challenge = new Challenge();
        uuid = UUID.randomUUID().toString();
        challenge.setUniqueId(uuid);
        challenge.setFriendlyName("name");
        challenge.setLevel("novice");
        challenge.setDescription(Collections.singletonList("A description"));
        cm.saveChallenge(challenge);
        File chDir = new File(database, "Challenge");
        assertTrue(chDir.exists());
        File check = new File(chDir, uuid + ".json");
        assertTrue(check.exists());
        // Remove icon becauseit has mockito meta in it
        removeLine(check);
    }

    private boolean removeLine(File inputFile) {
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
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return tempFile.renameTo(inputFile);
    }
    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#saveLevel(world.bentobox.challenges.database.object.ChallengeLevel)}.
     */
    @Test
    public void testSaveLevel() {
        ChallengeLevel level = new ChallengeLevel();
        level.setUniqueId("novice");
        level.setFriendlyName("name");
        cm.saveLevel(level);
        File chDir = new File(database, "ChallengeLevel");
        assertTrue(chDir.exists());
        File check = new File(chDir, "novice.json");
        assertTrue(check.exists());
     // Remove icon becauseit has mockito meta in it
        removeLine(check);
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#isChallengeComplete(world.bentobox.bentobox.api.user.User, org.bukkit.World, world.bentobox.challenges.database.object.Challenge)}.
     */
    @Test
    public void testIsChallengeCompleteUserWorldChallenge() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#isChallengeComplete(java.util.UUID, org.bukkit.World, world.bentobox.challenges.database.object.Challenge)}.
     */
    @Test
    public void testIsChallengeCompleteUUIDWorldChallenge() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#isChallengeComplete(java.util.UUID, org.bukkit.World, java.lang.String)}.
     */
    @Test
    public void testIsChallengeCompleteUUIDWorldString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#setChallengeComplete(world.bentobox.bentobox.api.user.User, org.bukkit.World, world.bentobox.challenges.database.object.Challenge, int)}.
     */
    @Test
    public void testSetChallengeCompleteUserWorldChallengeInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#setChallengeComplete(java.util.UUID, org.bukkit.World, world.bentobox.challenges.database.object.Challenge, int)}.
     */
    @Test
    public void testSetChallengeCompleteUUIDWorldChallengeInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#setChallengeComplete(java.util.UUID, org.bukkit.World, world.bentobox.challenges.database.object.Challenge, java.util.UUID)}.
     */
    @Test
    public void testSetChallengeCompleteUUIDWorldChallengeUUID() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#resetChallenge(java.util.UUID, org.bukkit.World, world.bentobox.challenges.database.object.Challenge, java.util.UUID)}.
     */
    @Test
    public void testResetChallenge() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#resetAllChallenges(world.bentobox.bentobox.api.user.User, org.bukkit.World)}.
     */
    @Test
    public void testResetAllChallengesUserWorld() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#resetAllChallenges(java.util.UUID, org.bukkit.World, java.util.UUID)}.
     */
    @Test
    public void testResetAllChallengesUUIDWorldUUID() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#getChallengeTimes(world.bentobox.bentobox.api.user.User, org.bukkit.World, world.bentobox.challenges.database.object.Challenge)}.
     */
    @Test
    public void testGetChallengeTimesUserWorldChallenge() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#getChallengeTimes(world.bentobox.bentobox.api.user.User, org.bukkit.World, java.lang.String)}.
     */
    @Test
    public void testGetChallengeTimesUserWorldString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#isLevelCompleted(world.bentobox.bentobox.api.user.User, org.bukkit.World, world.bentobox.challenges.database.object.ChallengeLevel)}.
     */
    @Test
    public void testIsLevelCompleted() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#isLevelUnlocked(world.bentobox.bentobox.api.user.User, org.bukkit.World, world.bentobox.challenges.database.object.ChallengeLevel)}.
     */
    @Test
    public void testIsLevelUnlocked() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#setLevelComplete(world.bentobox.bentobox.api.user.User, org.bukkit.World, world.bentobox.challenges.database.object.ChallengeLevel)}.
     */
    @Test
    public void testSetLevelComplete() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#validateLevelCompletion(world.bentobox.bentobox.api.user.User, org.bukkit.World, world.bentobox.challenges.database.object.ChallengeLevel)}.
     */
    @Test
    public void testValidateLevelCompletion() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#getChallengeLevelStatus(java.util.UUID, org.bukkit.World, world.bentobox.challenges.database.object.ChallengeLevel)}.
     */
    @Test
    public void testGetChallengeLevelStatus() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#getAllChallengeLevelStatus(world.bentobox.bentobox.api.user.User, org.bukkit.World)}.
     */
    @Test
    public void testGetAllChallengeLevelStatus() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#getAllChallengesNames(org.bukkit.World)}.
     */
    @Test
    public void testGetAllChallengesNames() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#getAllChallenges(org.bukkit.World)}.
     */
    @Test
    public void testGetAllChallenges() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#getFreeChallenges(org.bukkit.World)}.
     */
    @Test
    public void testGetFreeChallenges() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#getLevelChallenges(world.bentobox.challenges.database.object.ChallengeLevel)}.
     */
    @Test
    public void testGetLevelChallenges() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#getChallenge(java.lang.String)}.
     */
    @Test
    public void testGetChallenge() {
        assertNull(cm.getChallenge("name"));
        this.testSaveLevel();
        this.testSaveChallenge();
        assertNotNull(cm.getChallenge(uuid));
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#containsChallenge(java.lang.String)}.
     */
    @Test
    public void testContainsChallenge() {
        assertFalse(cm.containsChallenge("no-such-challenge"));
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#createChallenge(java.lang.String, world.bentobox.challenges.database.object.Challenge.ChallengeType, world.bentobox.challenges.database.object.requirements.Requirements)}.
     */
    @Test
    public void testCreateChallenge() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#deleteChallenge(world.bentobox.challenges.database.object.Challenge)}.
     */
    @Test
    public void testDeleteChallenge() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#getLevels(org.bukkit.World)}.
     */
    @Test
    public void testGetLevels() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#getLevel(world.bentobox.challenges.database.object.Challenge)}.
     */
    @Test
    public void testGetLevelChallenge() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#getLevel(java.lang.String)}.
     */
    @Test
    public void testGetLevelString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#containsLevel(java.lang.String)}.
     */
    @Test
    public void testContainsLevel() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#addChallengeToLevel(world.bentobox.challenges.database.object.Challenge, world.bentobox.challenges.database.object.ChallengeLevel)}.
     */
    @Test
    public void testAddChallengeToLevel() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#removeChallengeFromLevel(world.bentobox.challenges.database.object.Challenge, world.bentobox.challenges.database.object.ChallengeLevel)}.
     */
    @Test
    public void testRemoveChallengeFromLevel() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#createLevel(java.lang.String, org.bukkit.World)}.
     */
    @Test
    public void testCreateLevel() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#deleteChallengeLevel(world.bentobox.challenges.database.object.ChallengeLevel)}.
     */
    @Test
    public void testDeleteChallengeLevel() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#hasAnyChallengeData(org.bukkit.World)}.
     */
    @Test
    public void testHasAnyChallengeDataWorld() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link world.bentobox.challenges.ChallengesManager#hasAnyChallengeData(java.lang.String)}.
     */
    @Test
    public void testHasAnyChallengeDataString() {
        fail("Not yet implemented");
    }

}
