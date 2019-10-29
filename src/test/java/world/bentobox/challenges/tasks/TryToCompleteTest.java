package world.bentobox.challenges.tasks;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.configuration.WorldSettings;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.LocalesManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.ChallengesManager;
import world.bentobox.challenges.config.Settings;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.Challenge.ChallengeType;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.database.object.requirements.InventoryRequirements;
import world.bentobox.challenges.tasks.TryToComplete.ChallengeResult;
import world.bentobox.challenges.utils.Utils;


/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class, Util.class, Utils.class})
public class TryToCompleteTest {

    // Constants
    private static final String GAME_MODE_NAME = "BSkyBlock";
    private static final String[] NAMES = {"adam", "ben", "cara", "dave", "ed", "frank", "freddy", "george", "harry", "ian", "joe"};

    private TryToComplete ttc;
    private Challenge challenge;
    private @NonNull ChallengeLevel level;
    @Mock
    private ChallengesAddon addon;
    @Mock
    private User user;
    @Mock
    private World world;
    private String topLabel = "island";
    private String permissionPrefix = "perm.";
    private String cName;
    private String levelName;
    @Mock
    private ChallengesManager cm;
    @Mock
    private BentoBox plugin;
    @Mock
    private GameModeAddon gameMode;
    @Mock
    private AddonsManager am;
    @Mock
    private IslandsManager im;
    @Mock
    private Island island;
    @Mock
    private Player player;
    @Mock
    private Settings settings;
    @Mock
    private WorldSettings mySettings;
    private Map<String, Boolean> map;
    @Mock
    private @Nullable PlayerInventory inv;
    private ItemStack[] contents = {};

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // Set up plugin
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);
        // World
        when(user.getWorld()).thenReturn(world);
        when(world.getEnvironment()).thenReturn(Environment.NORMAL);

        // Addons manager
        when(plugin.getAddonsManager()).thenReturn(am);
        // One game mode
        when(am.getGameModeAddons()).thenReturn(Collections.singletonList(gameMode));
        AddonDescription desc2 = new AddonDescription.Builder("bentobox", GAME_MODE_NAME, "1.3").description("test").authors("tasty").build();
        when(gameMode.getDescription()).thenReturn(desc2);

        // Challenge Level
        level = new ChallengeLevel();
        levelName = GAME_MODE_NAME + "_novice";
        level.setUniqueId(levelName);
        level.setFriendlyName("Novice");
        // Set up challenge
        String uuid = UUID.randomUUID().toString();
        challenge = new Challenge();
        challenge.setUniqueId(GAME_MODE_NAME + "_" + uuid);
        challenge.setFriendlyName("name");
        challenge.setLevel(GAME_MODE_NAME + "_novice");
        challenge.setDescription(Collections.singletonList("A description"));
        challenge.setChallengeType(ChallengeType.INVENTORY);
        challenge.setDeployed(true);
        challenge.setIcon(new ItemStack(Material.EMERALD));
        challenge.setEnvironment(Collections.singleton(World.Environment.NORMAL));
        challenge.setLevel(levelName);
        challenge.setRepeatable(true);
        challenge.setMaxTimes(10);
        InventoryRequirements req = new InventoryRequirements();
        challenge.setRequirements(req);      
        // Util
        PowerMockito.mockStatic(Util.class);
        when(Util.getWorld(any())).thenReturn(world);
        when(Util.prettifyText(anyString())).thenCallRealMethod();

        // Island World Manager
        IslandWorldManager iwm = mock(IslandWorldManager.class);
        when(plugin.getIWM()).thenReturn(iwm);
        Optional<GameModeAddon> optionalGameMode = Optional.of(gameMode);
        when(iwm.getAddon(any())).thenReturn(optionalGameMode);

        // Island Manager
        when(addon.getIslands()).thenReturn(im);
        Optional<Island> opIsland = Optional.of(island);
        when(im.getIslandAt(any())).thenReturn(opIsland);
        // Player is on island
        when(im.locationIsOnIsland(any(), any())).thenReturn(true);
        // Island flags - everything is allowed by default
        when(island.isAllowed(any(), any())).thenReturn(true);

        // Challenges Manager
        when(addon.getChallengesManager()).thenReturn(cm);
        // All levels unlocked by default
        when(cm.isLevelUnlocked(any(), any(), any())).thenReturn(true);
        // Player has done this challenge 3 times (default max is 10)
        when(cm.getChallengeTimes(any(), any(), any(Challenge.class))).thenReturn(3L);

        // User has all perms by default
        when(user.hasPermission(anyString())).thenReturn(true);
        when(user.getPlayer()).thenReturn(player);
        when(user.getTranslation(Mockito.anyString())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(0, String.class));
        when(user.getName()).thenReturn("tastybento");
        when(user.getLocation()).thenReturn(mock(Location.class));
        when(user.getInventory()).thenReturn(inv);
        when(inv.getContents()).thenReturn(contents);
        // Locales
        User.setPlugin(plugin);
        LocalesManager lm = mock(LocalesManager.class);
        when(plugin.getLocalesManager()).thenReturn(lm);
        when(lm.get(any(), any())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(1, String.class));
        PlaceholdersManager phm = mock(PlaceholdersManager.class);
        when(plugin.getPlaceholdersManager()).thenReturn(phm);
        when(phm.replacePlaceholders(any(), any())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(1, String.class));
        
        // Survival by default
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
        
        // Addon
        when(addon.getChallengesSettings()).thenReturn(settings);
        when(settings.isBroadcastMessages()).thenReturn(true);
        
     // Bukkit - online players
        Map<UUID, String> online = new HashMap<>();

        Set<Player> onlinePlayers = new HashSet<>();
        for (int j = 0; j < NAMES.length; j++) {
            Player p1 = mock(Player.class);
            UUID uuid2 = UUID.randomUUID();
            when(p1.getUniqueId()).thenReturn(uuid2);
            when(p1.getName()).thenReturn(NAMES[j]);
            online.put(uuid2, NAMES[j]);
            onlinePlayers.add(p1);
        }
        PowerMockito.mockStatic(Bukkit.class);
        when(Bukkit.getOnlinePlayers()).then((Answer<Set<Player>>) invocation -> onlinePlayers);

        // World settings
        map = new HashMap<>();
        when(mySettings.getWorldFlags()).thenReturn(map);
        when(iwm.getWorldSettings(any())).thenReturn(mySettings);
        ChallengesAddon.CHALLENGES_WORLD_PROTECTION.setSetting(world, true);
        
        // ItemFsaxtory
        ItemFactory itemFactory = mock(ItemFactory.class);
        when(Bukkit.getItemFactory()).thenReturn(itemFactory);
        
     }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link world.bentobox.challenges.tasks.TryToComplete#TryToComplete(world.bentobox.challenges.ChallengesAddon, world.bentobox.bentobox.api.user.User, world.bentobox.challenges.database.object.Challenge, org.bukkit.World, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testTryToCompleteChallengesAddonUserChallengeWorldStringString() {
        ttc = new TryToComplete(addon,
                user,
                challenge,
                world,
                topLabel,
                permissionPrefix);
        verify(addon).getChallengesManager();

    }

    /**
     * Test method for {@link world.bentobox.challenges.tasks.TryToComplete#complete(world.bentobox.challenges.ChallengesAddon, world.bentobox.bentobox.api.user.User, world.bentobox.challenges.database.object.Challenge, org.bukkit.World, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringNotDeployed() {
        challenge.setDeployed(false);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).sendMessage("challenges.errors.not-deployed");
    }
    
    /**
     * Test method for {@link world.bentobox.challenges.tasks.TryToComplete#complete(world.bentobox.challenges.ChallengesAddon, world.bentobox.bentobox.api.user.User, world.bentobox.challenges.database.object.Challenge, org.bukkit.World, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringWrongWorld() {
        challenge.setUniqueId("test");
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).sendMessage("general.errors.wrong-world");
    }
    
    /**
     * Test method for {@link world.bentobox.challenges.tasks.TryToComplete#complete(world.bentobox.challenges.ChallengesAddon, world.bentobox.bentobox.api.user.User, world.bentobox.challenges.database.object.Challenge, org.bukkit.World, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringNotOnIsland() {
        ChallengesAddon.CHALLENGES_WORLD_PROTECTION.setSetting(world, true);
        when(im.locationIsOnIsland(any(Player.class), any(Location.class))).thenReturn(false);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).sendMessage("challenges.errors.not-on-island");
    }
    
    /**
     * Test method for {@link world.bentobox.challenges.tasks.TryToComplete#complete(world.bentobox.challenges.ChallengesAddon, world.bentobox.bentobox.api.user.User, world.bentobox.challenges.database.object.Challenge, org.bukkit.World, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringNotOnIslandButOk() {
        ChallengesAddon.CHALLENGES_WORLD_PROTECTION.setSetting(world, false);
        when(im.locationIsOnIsland(any(Player.class), any(Location.class))).thenReturn(false);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).sendMessage("challenges.messages.you-completed-challenge", "[value]", "name");
    }
    
    /**
     * Test method for {@link world.bentobox.challenges.tasks.TryToComplete#complete(world.bentobox.challenges.ChallengesAddon, world.bentobox.bentobox.api.user.User, world.bentobox.challenges.database.object.Challenge, org.bukkit.World, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringLevelNotUnlocked() {
        when(cm.isLevelUnlocked(any(), any(), any())).thenReturn(false);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).sendMessage("challenges.errors.challenge-level-not-available");
    }
    
    /**
     * Test method for {@link world.bentobox.challenges.tasks.TryToComplete#complete(world.bentobox.challenges.ChallengesAddon, world.bentobox.bentobox.api.user.User, world.bentobox.challenges.database.object.Challenge, org.bukkit.World, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringNotRepeatable() {
        challenge.setRepeatable(false);
        when(cm.isChallengeComplete(any(User.class), any(), any())).thenReturn(true);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).sendMessage("challenges.errors.not-repeatable");
    }
    
    /**
     * Test method for {@link world.bentobox.challenges.tasks.TryToComplete#complete(world.bentobox.challenges.ChallengesAddon, world.bentobox.bentobox.api.user.User, world.bentobox.challenges.database.object.Challenge, org.bukkit.World, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringNotRepeatableFirstTime() {
        challenge.setRepeatable(false);
        challenge.setMaxTimes(0);
        when(cm.getChallengeTimes(any(), any(), any(Challenge.class))).thenReturn(0L);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).sendMessage("challenges.messages.you-completed-challenge", "[value]", "name");
    }
    
    /**
     * Test method for {@link world.bentobox.challenges.tasks.TryToComplete#complete(world.bentobox.challenges.ChallengesAddon, world.bentobox.bentobox.api.user.User, world.bentobox.challenges.database.object.Challenge, org.bukkit.World, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringNoRank() {
        when(island.isAllowed(any(), any())).thenReturn(false);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).sendMessage("challenges.errors.no-rank");
    }

    /**
     * Test method for {@link world.bentobox.challenges.tasks.TryToComplete#complete(world.bentobox.challenges.ChallengesAddon, world.bentobox.bentobox.api.user.User, world.bentobox.challenges.database.object.Challenge, org.bukkit.World, java.lang.String, java.lang.String, int)}.
     */
    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringIntZero() {
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix, 0));
        verify(user).sendMessage("challenges.errors.not-valid-integer");
    }

    /**
     * Test method for {@link world.bentobox.challenges.tasks.TryToComplete#complete(world.bentobox.challenges.ChallengesAddon, world.bentobox.bentobox.api.user.User, world.bentobox.challenges.database.object.Challenge, org.bukkit.World, java.lang.String, java.lang.String, int)}.
     */
    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringIntNegative() {
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix, -10));
        verify(user).sendMessage("challenges.errors.not-valid-integer");
    }

    /**
     * Test method for {@link world.bentobox.challenges.tasks.TryToComplete#complete(world.bentobox.challenges.ChallengesAddon, world.bentobox.bentobox.api.user.User, world.bentobox.challenges.database.object.Challenge, org.bukkit.World, java.lang.String, java.lang.String, int)}.
     */
    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringIntPositiveWrongEnvinonment() {
        challenge.setEnvironment(Collections.singleton(Environment.NETHER));
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix, 100));
        verify(user).sendMessage("challenges.errors.wrong-environment");
    }
    
    /**
     * Test method for {@link world.bentobox.challenges.tasks.TryToComplete#complete(world.bentobox.challenges.ChallengesAddon, world.bentobox.bentobox.api.user.User, world.bentobox.challenges.database.object.Challenge, org.bukkit.World, java.lang.String, java.lang.String, int)}.
     */
    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringIntPositiveNoPerm() {
        InventoryRequirements req = new InventoryRequirements();
        req.setRequiredPermissions(Collections.singleton("perm-you-dont-have"));
        when(user.hasPermission(anyString())).thenReturn(false);
        challenge.setRequirements(req); 
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix, 100));
        verify(user).sendMessage("general.errors.no-permission");
    }

    /**
     * Test method for {@link world.bentobox.challenges.tasks.TryToComplete#complete(world.bentobox.challenges.ChallengesAddon, world.bentobox.bentobox.api.user.User, world.bentobox.challenges.database.object.Challenge, org.bukkit.World, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringSuccess() {
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).sendMessage("challenges.messages.you-completed-challenge", "[value]", "name");
    }
    
    /**
     * Test method for {@link world.bentobox.challenges.tasks.TryToComplete#complete(world.bentobox.challenges.ChallengesAddon, world.bentobox.bentobox.api.user.User, world.bentobox.challenges.database.object.Challenge, org.bukkit.World, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringSuccessSingleReq() {
        InventoryRequirements req = new InventoryRequirements();
        req.setRequiredItems(Collections.singletonList(new ItemStack(Material.EMERALD_BLOCK)));
        challenge.setRequirements(req); 
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).sendMessage("challenges.errors.not-enough-items", "[items]", "Emerald Block");
    }
    
    /**
     * Test method for {@link world.bentobox.challenges.tasks.TryToComplete#complete(world.bentobox.challenges.ChallengesAddon, world.bentobox.bentobox.api.user.User, world.bentobox.challenges.database.object.Challenge, org.bukkit.World, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringSuccessMultipleReq() {
        
        InventoryRequirements req = new InventoryRequirements();
        ItemStack itemStackMock = mock(ItemStack.class);
        when(itemStackMock.getAmount()).thenReturn(3);
        when(itemStackMock.getType()).thenReturn(Material.EMERALD_BLOCK);
        when(itemStackMock.clone()).thenReturn(itemStackMock);
        
        ItemStack itemStackMock2 = mock(ItemStack.class);
        when(itemStackMock2.getType()).thenReturn(Material.ENCHANTED_BOOK);
        when(itemStackMock2.getAmount()).thenReturn(10);
        when(itemStackMock2.clone()).thenReturn(itemStackMock2);
        
        ItemStack itemStackMock3 = mock(ItemStack.class);
        when(itemStackMock3.getType()).thenReturn(Material.EMERALD_BLOCK);
        when(itemStackMock3.getAmount()).thenReturn(15);
        when(itemStackMock3.clone()).thenReturn(itemStackMock3);
        // itemStackMock and 3 are same type
        when(itemStackMock3.isSimilar(eq(itemStackMock))).thenReturn(true);
        when(itemStackMock.isSimilar(eq(itemStackMock3))).thenReturn(true);
        
        req.setRequiredItems(Arrays.asList(itemStackMock , itemStackMock2));
        challenge.setRequirements(req); 
        ItemStack[] newContents = {itemStackMock3};
        when(inv.getContents()).thenReturn(newContents);

        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        // Sufficient emerald blocks
        verify(user, never()).sendMessage("challenges.errors.not-enough-items", "[items]", "Emerald Block");
        // Not enough apples
        verify(user).sendMessage("challenges.errors.not-enough-items", "[items]", "Enchanted Book");
    }
    
    /**
     * Test method for {@link world.bentobox.challenges.tasks.TryToComplete#complete(world.bentobox.challenges.ChallengesAddon, world.bentobox.bentobox.api.user.User, world.bentobox.challenges.database.object.Challenge, org.bukkit.World, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringSuccessCreative() {
        when(player.getGameMode()).thenReturn(GameMode.CREATIVE);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).sendMessage("challenges.messages.you-completed-challenge", "[value]", "name");
    }

    /**
     * Test method for {@link world.bentobox.challenges.tasks.TryToComplete#complete(world.bentobox.challenges.ChallengesAddon, world.bentobox.bentobox.api.user.User, world.bentobox.challenges.database.object.Challenge, org.bukkit.World, java.lang.String, java.lang.String, int)}.
     */
    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringIntMultipleTimesPositiveSuccess() {
        // Try to complete 10 times. Already done 3 times, and max is 10, so it should be only done 7 times
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix, 10));
        verify(user).sendMessage("challenges.messages.you-repeated-challenge-multiple", "[value]", "name", "[count]", "7");
    }

    /**
     * Test method for {@link world.bentobox.challenges.tasks.TryToComplete#build(int)}.
     */
    @Test
    public void testBuild() {
        this.testTryToCompleteChallengesAddonUserChallengeWorldStringString();
        ChallengeResult result = this.ttc.build(10);
    }

    /**
     * Test method for {@link world.bentobox.challenges.tasks.TryToComplete#removeItems(java.util.List, int)}.
     */
    @Test
    public void testRemoveItems() {
        this.testTryToCompleteChallengesAddonUserChallengeWorldStringString();
        ttc.removeItems(Collections.emptyList(), 1);
    }

}
