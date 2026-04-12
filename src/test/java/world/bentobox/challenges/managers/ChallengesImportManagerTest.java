package world.bentobox.challenges.managers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.AbstractChallengesTest;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.Challenge.ChallengeType;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.database.object.requirements.IslandRequirements;
import world.bentobox.challenges.utils.Utils;

/**
 * @author tastybento
 */
public class ChallengesImportManagerTest extends AbstractChallengesTest {

    private ChallengesImportManager cim;
    private File dataFolder;
    private MockedStatic<Utils> mockedUtils;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        // Data folder for file I/O tests
        dataFolder = new File("test-data-folder");
        dataFolder.mkdirs();
        when(addon.getDataFolder()).thenReturn(dataFolder);

        // Addon description for version
        AddonDescription addonDesc = new AddonDescription.Builder("bentobox", "Challenges", "1.5.0")
                .description("test").authors("tasty").build();
        when(addon.getDescription()).thenReturn(addonDesc);

        // Utils.getGameMode — static method that calls BentoBox.getInstance().getIWM().getAddon(world)
        // which is already mocked in the abstract base. We mock Utils directly for cleaner control.
        mockedUtils = Mockito.mockStatic(Utils.class, Mockito.CALLS_REAL_METHODS);
        mockedUtils.when(() -> Utils.getGameMode(any())).thenReturn(GAME_MODE_NAME);
        // Let sendMessage pass through to real implementation
        mockedUtils.when(() -> Utils.sendMessage(any(), any(), anyString())).thenCallRealMethod();

        // ChallengesManager stubs for loading
        when(cm.loadChallenge(any(), any(), anyBoolean(), any(), anyBoolean())).thenReturn(true);
        when(cm.loadLevel(any(), any(), anyBoolean(), any(), anyBoolean())).thenReturn(true);

        // Class under test
        cim = new ChallengesImportManager(addon);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        if (mockedUtils != null) mockedUtils.closeOnDemand();
        super.tearDown();
        deleteFolder(dataFolder);
    }

    private void deleteFolder(File folder) throws IOException {
        if (folder != null && folder.exists()) {
            Files.walk(folder.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    // -------------------------------------------------------------------------
    // importFile (YAML)
    // -------------------------------------------------------------------------

    @Test
    public void testImportFileNotFound() {
        cim.importFile(user, world, "nonexistent");
        verify(user).getTranslation(any(World.class), eq("challenges.errors.no-file"),
                eq("[file]"), eq("nonexistent"));
    }

    @Test
    public void testImportFileSuccess() throws IOException {
        String yaml = """
                challenges:
                  mychallenge:
                    name: "My Challenge"
                    type: ISLAND_TYPE
                    deployed: true
                    order: 1
                    environments:
                      - NORMAL
                    requirements:
                      search-distance: 10
                      blocks:
                        STONE: 5
                      entities:
                        ZOMBIE: 2
                    rewards:
                      text: "Good job"
                      experience: 10
                levels:
                  novice:
                    name: "Novice"
                    order: 1
                    waiver: 0
                    challenges:
                      - mychallenge
                    rewards:
                      experience: 50
                """;
        File yamlFile = new File(dataFolder, "test.yml");
        Files.writeString(yamlFile.toPath(), yaml, StandardCharsets.UTF_8);

        // getChallenge must return the created challenge for level linking
        Challenge createdChallenge = new Challenge();
        createdChallenge.setUniqueId("bskyblock_mychallenge");
        when(cm.getChallenge("bskyblock_mychallenge")).thenReturn(createdChallenge);

        cim.importFile(user, world, "test");

        // Verify wipeDatabase was called with lowercase gamemode name
        verify(cm).wipeDatabase("bskyblock");
        // Verify challenge was saved and loaded
        verify(cm).saveChallenge(any(Challenge.class));
        verify(cm).loadChallenge(any(Challenge.class), eq(world), eq(true), isNull(), eq(true));
        // Verify level was saved and loaded
        verify(cm).saveLevel(any(ChallengeLevel.class));
        verify(cm).loadLevel(any(ChallengeLevel.class), eq(world), eq(true), isNull(), eq(true));
    }

    @Test
    public void testImportFileNoGameMode() throws IOException {
        // Write a valid YAML file
        File yamlFile = new File(dataFolder, "test.yml");
        Files.writeString(yamlFile.toPath(), "challenges:", StandardCharsets.UTF_8);

        // No game mode addon for this world
        when(iwm.getAddon(any())).thenReturn(java.util.Optional.empty());

        cim.importFile(user, world, "test");

        verify(user).getTranslation(any(World.class), eq("challenges.errors.not-a-gamemode-world"),
                eq("[world]"), anyString());
        verify(cm, never()).wipeDatabase(anyString());
    }

    @Test
    public void testImportFileNullUser() throws IOException {
        String yaml = """
                challenges:
                  simple:
                    name: "Simple"
                    type: INVENTORY_TYPE
                    deployed: true
                    requirements:
                      take-items: false
                """;
        File yamlFile = new File(dataFolder, "test.yml");
        Files.writeString(yamlFile.toPath(), yaml, StandardCharsets.UTF_8);

        // Should not throw with null user
        cim.importFile(null, world, "test");

        verify(cm).wipeDatabase("bskyblock");
        verify(cm).saveChallenge(any(Challenge.class));
    }

    // -------------------------------------------------------------------------
    // importDatabaseFile (JSON from file)
    // -------------------------------------------------------------------------

    @Test
    public void testImportDatabaseFileNullWorld() {
        mockedUtil.when(() -> Util.getWorld(any())).thenReturn(null);
        cim.importDatabaseFile(user, world, "test.json");
        verify(addon).logError("Given world is not part of BentoBox");
        verify(cm, never()).loadChallenge(any(), any(), anyBoolean(), any(), anyBoolean());
    }

    @Test
    public void testImportDatabaseFileNotFound() {
        when(cm.hasAnyChallengeData(anyString())).thenReturn(false);
        cim.importDatabaseFile(user, world, "nonexistent.json");
        // The file doesn't exist, so loadObject returns null
        verify(cm, never()).loadChallenge(any(), any(), anyBoolean(), any(), anyBoolean());
    }

    @Test
    public void testImportDatabaseFileWipesExisting() throws IOException {
        when(cm.hasAnyChallengeData(anyString())).thenReturn(true);

        // Write a minimal valid JSON file
        String json = """
                {
                    "challengeList": [],
                    "challengeLevelList": [],
                    "version": "1.0"
                }
                """;
        File jsonFile = new File(dataFolder, "test.json");
        Files.writeString(jsonFile.toPath(), json, StandardCharsets.UTF_8);

        cim.importDatabaseFile(user, world, "test");

        verify(cm).wipeDatabase("bskyblock");
    }

    @Test
    public void testImportDatabaseFileWithChallenges() throws IOException {
        when(cm.hasAnyChallengeData(anyString())).thenReturn(false);

        // JSON with a challenge and level — uses Gson with @Expose annotations
        // The DefaultDataHolder expects challengeList and challengeLevelList
        String json = """
                {
                    "challengeList": [
                        {
                            "uniqueId": "testchallenge",
                            "friendlyName": "Test",
                            "deployed": true,
                            "challengeType": "INVENTORY_TYPE",
                            "level": "testlevel",
                            "description": ["A test"],
                            "order": 1,
                            "repeatable": false,
                            "maxTimes": 0,
                            "environment": ["NORMAL"]
                        }
                    ],
                    "challengeLevelList": [
                        {
                            "uniqueId": "testlevel",
                            "friendlyName": "Test Level",
                            "world": "",
                            "order": 1,
                            "challenges": ["testchallenge"]
                        }
                    ],
                    "version": "1.0"
                }
                """;
        File jsonFile = new File(dataFolder, "test.json");
        Files.writeString(jsonFile.toPath(), json, StandardCharsets.UTF_8);

        cim.importDatabaseFile(user, world, "test");

        // Challenge should be loaded with prefixed ID
        verify(cm).loadChallenge(any(Challenge.class), eq(world), eq(false), eq(user), eq(false));
        // Level should be loaded with prefixed ID
        verify(cm).loadLevel(any(ChallengeLevel.class), eq(world), eq(false), eq(user), eq(false));
        // Should save after import
        verify(cm).saveChallenges();
        verify(cm).saveLevels();
    }

    // -------------------------------------------------------------------------
    // loadDownloadedChallenges (JSON string)
    // -------------------------------------------------------------------------

    @Test
    public void testLoadDownloadedChallengesNullWorld() {
        mockedUtil.when(() -> Util.getWorld(any())).thenReturn(null);
        cim.loadDownloadedChallenges(user, world, "{}");
        verify(addon).logError("Given world is not part of BentoBox");
    }

    @Test
    public void testLoadDownloadedChallengesExistingData() {
        when(cm.hasAnyChallengeData(anyString())).thenReturn(true);
        when(user.isPlayer()).thenReturn(true);
        cim.loadDownloadedChallenges(user, world, "{}");
        verify(user).getTranslation(any(World.class), eq("challenges.errors.exist-challenges-or-levels"));
        verify(cm, never()).loadChallenge(any(), any(), anyBoolean(), any(), anyBoolean());
    }

    @Test
    public void testLoadDownloadedChallengesExistingDataConsoleUser() {
        when(cm.hasAnyChallengeData(anyString())).thenReturn(true);
        when(user.isPlayer()).thenReturn(false);
        cim.loadDownloadedChallenges(user, world, "{}");
        verify(addon).logWarning("challenges.errors.exist-challenges-or-levels");
    }

    @Test
    public void testLoadDownloadedChallengesSuccess() {
        when(cm.hasAnyChallengeData(anyString())).thenReturn(false);

        String json = """
                {
                    "challengeList": [
                        {
                            "uniqueId": "dlchallenge",
                            "friendlyName": "Downloaded",
                            "deployed": true,
                            "challengeType": "ISLAND_TYPE",
                            "level": "dllevel",
                            "description": ["Downloaded challenge"],
                            "order": 1,
                            "repeatable": true,
                            "maxTimes": 5,
                            "environment": ["NORMAL"]
                        }
                    ],
                    "challengeLevelList": [
                        {
                            "uniqueId": "dllevel",
                            "friendlyName": "DL Level",
                            "world": "",
                            "order": 1,
                            "challenges": ["dlchallenge"]
                        }
                    ],
                    "version": "1.0"
                }
                """;

        cim.loadDownloadedChallenges(user, world, json);

        verify(cm).loadChallenge(any(Challenge.class), eq(world), eq(false), eq(user), eq(false));
        verify(cm).loadLevel(any(ChallengeLevel.class), eq(world), eq(false), eq(user), eq(false));
        verify(cm).saveChallenges();
        verify(cm).saveLevels();
    }

    @Test
    public void testLoadDownloadedChallengesEmptyLists() {
        when(cm.hasAnyChallengeData(anyString())).thenReturn(false);

        String json = """
                {
                    "challengeList": [],
                    "challengeLevelList": [],
                    "version": "1.0"
                }
                """;

        cim.loadDownloadedChallenges(user, world, json);

        verify(cm, never()).loadChallenge(any(), any(), anyBoolean(), any(), anyBoolean());
        verify(cm, never()).loadLevel(any(), any(), anyBoolean(), any(), anyBoolean());
        verify(cm).saveChallenges();
        verify(cm).saveLevels();
    }

    @Test
    public void testLoadDownloadedChallengesMultiple() {
        when(cm.hasAnyChallengeData(anyString())).thenReturn(false);

        String json = """
                {
                    "challengeList": [
                        {
                            "uniqueId": "ch1",
                            "friendlyName": "Challenge 1",
                            "deployed": true,
                            "challengeType": "INVENTORY_TYPE",
                            "level": "",
                            "description": ["First"],
                            "order": 1
                        },
                        {
                            "uniqueId": "ch2",
                            "friendlyName": "Challenge 2",
                            "deployed": true,
                            "challengeType": "ISLAND_TYPE",
                            "level": "lvl1",
                            "description": ["Second"],
                            "order": 2
                        }
                    ],
                    "challengeLevelList": [
                        {
                            "uniqueId": "lvl1",
                            "friendlyName": "Level 1",
                            "world": "",
                            "order": 1,
                            "challenges": ["ch2"]
                        }
                    ],
                    "version": "1.0"
                }
                """;

        cim.loadDownloadedChallenges(user, world, json);

        verify(cm, times(2)).loadChallenge(any(Challenge.class), eq(world), eq(false), eq(user), eq(false));
        verify(cm).loadLevel(any(ChallengeLevel.class), eq(world), eq(false), eq(user), eq(false));
    }

    // -------------------------------------------------------------------------
    // generateDatabaseFile (JSON export)
    // -------------------------------------------------------------------------

    @Test
    public void testGenerateDatabaseFileAlreadyExists() throws IOException {
        File existing = new File(dataFolder, "export.json");
        existing.createNewFile();
        when(user.isPlayer()).thenReturn(true);

        cim.generateDatabaseFile(user, world, "export");

        verify(user).getTranslation(any(World.class), eq("challenges.errors.file-exist"),
                eq("[file]"), eq("export"));
    }

    @Test
    public void testGenerateDatabaseFileSuccess() {
        when(user.isPlayer()).thenReturn(true);

        // Set up challenges and levels to export
        Challenge ch = new Challenge();
        ch.setUniqueId("bskyblock_testch");
        ch.setFriendlyName("Test");
        ch.setLevel("bskyblock_novice");
        ch.setChallengeType(ChallengeType.ISLAND_TYPE);
        ch.setRequirements(new IslandRequirements());
        ch.setDeployed(true);

        ChallengeLevel lvl = new ChallengeLevel();
        lvl.setUniqueId("bskyblock_novice");
        lvl.setFriendlyName("Novice");
        lvl.setWorld("world");
        lvl.setChallenges(Collections.singleton("bskyblock_testch"));

        when(cm.getAllChallenges(world)).thenReturn(List.of(ch));
        when(cm.getLevels(world)).thenReturn(List.of(lvl));

        cim.generateDatabaseFile(user, world, "export");

        // Verify the file was created
        File exported = new File(dataFolder, "export.json");
        assertTrue(exported.exists());

        // Verify completion message
        verify(user).getTranslation(any(World.class), eq("challenges.conversations.database-export-completed"),
                eq("[world]"), eq("world"), eq("[file]"), eq("export"));
    }

    @Test
    public void testGenerateDatabaseFileEmptyData() {
        when(user.isPlayer()).thenReturn(true);
        when(cm.getAllChallenges(world)).thenReturn(Collections.emptyList());
        when(cm.getLevels(world)).thenReturn(Collections.emptyList());

        cim.generateDatabaseFile(user, world, "empty-export");

        File exported = new File(dataFolder, "empty-export.json");
        assertTrue(exported.exists());
    }

    @Test
    public void testGenerateDatabaseFileConsoleUser() {
        when(user.isPlayer()).thenReturn(false);
        when(cm.getAllChallenges(world)).thenReturn(Collections.emptyList());
        when(cm.getLevels(world)).thenReturn(Collections.emptyList());

        cim.generateDatabaseFile(user, world, "console-export");

        File exported = new File(dataFolder, "console-export.json");
        assertTrue(exported.exists());
        verify(addon).logWarning("Database Export Completed");
    }

    @Test
    public void testGenerateDatabaseFileStripsPrefix() throws IOException {
        when(user.isPlayer()).thenReturn(true);

        Challenge ch = new Challenge();
        ch.setUniqueId("bskyblock_mychallenge");
        ch.setFriendlyName("My Challenge");
        ch.setLevel("bskyblock_novice");
        ch.setChallengeType(ChallengeType.INVENTORY_TYPE);
        ch.setDeployed(true);

        ChallengeLevel lvl = new ChallengeLevel();
        lvl.setUniqueId("bskyblock_novice");
        lvl.setFriendlyName("Novice");
        lvl.setWorld("world");
        lvl.setChallenges(Collections.singleton("bskyblock_mychallenge"));

        when(cm.getAllChallenges(world)).thenReturn(List.of(ch));
        when(cm.getLevels(world)).thenReturn(List.of(lvl));

        cim.generateDatabaseFile(user, world, "prefix-test");

        File exported = new File(dataFolder, "prefix-test.json");
        assertTrue(exported.exists());
        String content = Files.readString(exported.toPath(), StandardCharsets.UTF_8);
        // The exported JSON should have stripped the "bskyblock_" prefix
        assertTrue(content.contains("\"mychallenge\"") || content.contains("mychallenge"));
        assertFalse(content.contains("bskyblock_mychallenge"));
    }
}
