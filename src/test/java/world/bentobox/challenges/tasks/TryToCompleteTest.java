package world.bentobox.challenges.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Player.Spigot;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.BoundingBox;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import net.md_5.bungee.api.chat.TextComponent;
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
import world.bentobox.challenges.WhiteBox;
import world.bentobox.challenges.config.Settings;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.Challenge.ChallengeType;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.database.object.requirements.InventoryRequirements;
import world.bentobox.challenges.database.object.requirements.IslandRequirements;
import world.bentobox.challenges.managers.ChallengesManager;
import world.bentobox.challenges.tasks.TryToComplete.ChallengeResult;

/**
 * @author tastybento
 *
 */
public class TryToCompleteTest {

    // Constants
    private static final String GAME_MODE_NAME = "BSkyBlock";
    private static final String[] NAMES = { "adam", "ben", "cara", "dave", "ed", "frank", "freddy", "george", "harry",
            "ian", "joe" };

    private TryToComplete ttc;
    private Challenge challenge;
    @Mock
    private ChallengesAddon addon;
    @Mock
    private User user;
    @Mock
    private World world;
    private final String topLabel = "island";
    private final String permissionPrefix = "perm.";

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
    @Mock
    private @Nullable PlayerInventory inv;
    private final ItemStack[] contents = {};
    @Mock
    private BoundingBox bb;
    private Set<Player> onlinePlayers;
    @Mock
    private Spigot spigot;

    private AutoCloseable closeable;
    private ServerMock mbServer;
    private MockedStatic<Bukkit> mockedBukkit;
    private MockedStatic<Util> mockedUtil;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        mbServer = MockBukkit.mock();
        @SuppressWarnings("unused")
        var unusedTagRef = org.bukkit.Tag.LEAVES;
        // Set up plugin
        WhiteBox.setInternalState(BentoBox.class, "instance", plugin);
        when(addon.getPlugin()).thenReturn(plugin);
        // World
        when(user.getWorld()).thenReturn(world);
        when(world.getName()).thenReturn("world");
        when(world.getEnvironment()).thenReturn(Environment.NORMAL);

        // Addons manager
        when(plugin.getAddonsManager()).thenReturn(am);
        // One game mode
        when(am.getGameModeAddons()).thenReturn(Collections.singletonList(gameMode));
        AddonDescription desc2 = new AddonDescription.Builder("bentobox", GAME_MODE_NAME, "1.3").description("test")
                .authors("tasty").build();
        when(gameMode.getDescription()).thenReturn(desc2);

        // Challenge Level
        @NonNull
        ChallengeLevel level = new ChallengeLevel();
        String levelName = GAME_MODE_NAME + "_novice";
        level.setUniqueId(levelName);
        level.setFriendlyName("Novice");
        // Set up challenge
        String uuid = UUID.randomUUID().toString();
        challenge = new Challenge();
        challenge.setUniqueId(GAME_MODE_NAME + "_" + uuid);
        challenge.setFriendlyName("name");
        challenge.setLevel(GAME_MODE_NAME + "_novice");
        challenge.setDescription(Collections.singletonList("A description"));
        challenge.setChallengeType(ChallengeType.INVENTORY_TYPE);
        challenge.setDeployed(true);
        challenge.setIcon(new ItemStack(Material.EMERALD));
        challenge.setEnvironment(Collections.singleton(World.Environment.NORMAL));
        challenge.setLevel(levelName);
        challenge.setRepeatable(true);
        challenge.setMaxTimes(10);
        InventoryRequirements req = new InventoryRequirements();

        challenge.setRequirements(req);
        // Util
        mockedUtil = Mockito.mockStatic(Util.class, Mockito.CALLS_REAL_METHODS);
        mockedUtil.when(() -> Util.getWorld(any())).thenReturn(world);
        mockedUtil.when(() -> Util.prettifyText(anyString())).thenCallRealMethod();
        mockedUtil.when(() -> Util.stripSpaceAfterColorCodes(anyString())).thenCallRealMethod();
        mockedUtil.when(() -> Util.translateColorCodes(anyString()))
                .thenAnswer((Answer<String>) invocation -> invocation.getArgument(0, String.class));

        // Island World Manager
        IslandWorldManager iwm = mock(IslandWorldManager.class);
        when(plugin.getIWM()).thenReturn(iwm);
        Optional<GameModeAddon> optionalGameMode = Optional.of(gameMode);
        when(iwm.getAddon(any())).thenReturn(optionalGameMode);
        when(iwm.getIslandDistance(any())).thenReturn(400);
        when(iwm.inWorld(any(World.class))).thenReturn(true);
        when(iwm.getFriendlyName(any())).thenReturn("BSkyBlock");

        // Island Manager
        when(addon.getIslands()).thenReturn(im);
        Optional<Island> opIsland = Optional.of(island);
        when(im.getIslandAt(any())).thenReturn(opIsland);
        when(im.getIsland(any(), any(User.class))).thenReturn(island);
        // Player is on island
        when(im.locationIsOnIsland(any(), any())).thenReturn(true);
        // Island flags - everything is allowed by default
        when(island.isAllowed(any(), any())).thenReturn(true);

        @Nullable
        Location loc = mock(Location.class);
        when(loc.toString()).thenReturn("center");
        when(island.getCenter()).thenReturn(loc);

        // Challenges Manager
        when(addon.getChallengesManager()).thenReturn(cm);
        when(cm.isLevelUnlocked(any(), any(), any())).thenReturn(true);
        when(cm.getChallengeTimes(any(), any(), any(Challenge.class))).thenReturn(3L);

        // User has all perms by default
        when(user.hasPermission(anyString())).thenReturn(true);
        when(user.getPlayer()).thenReturn(player);
        UUID uniqueId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uniqueId);
        when(player.getWorld()).thenReturn(world);
        when(player.spigot()).thenReturn(spigot);
        when(user.getUniqueId()).thenReturn(uniqueId);
        when(user.getTranslation(anyString()))
                .thenAnswer((Answer<String>) invocation -> invocation.getArgument(0, String.class));
        when(user.getTranslation(anyString(), anyString()))
                .thenAnswer((Answer<String>) invocation -> invocation.getArgument(0, String.class));
        when(user.getTranslationOrNothing(anyString()))
                .thenAnswer((Answer<String>) invocation -> invocation.getArgument(0, String.class));
        when(user.getName()).thenReturn("tastybento");
        User.getInstance(player);
        @Nullable
        Location userLoc = mock(Location.class);
        when(userLoc.toString()).thenReturn("location");
        when(user.getLocation()).thenReturn(userLoc);
        when(user.getInventory()).thenReturn(inv);
        when(inv.getContents()).thenReturn(contents);
        when(player.getBoundingBox()).thenReturn(bb);
        when(bb.clone()).thenReturn(bb);
        when(bb.toString()).thenReturn("BoundingBox");
        // Locales
        User.setPlugin(plugin);
        LocalesManager lm = mock(LocalesManager.class);
        when(plugin.getLocalesManager()).thenReturn(lm);
        when(lm.get(any(), any())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(1, String.class));
        PlaceholdersManager phm = mock(PlaceholdersManager.class);
        when(plugin.getPlaceholdersManager()).thenReturn(phm);
        when(phm.replacePlaceholders(any(), any()))
                .thenAnswer((Answer<String>) invocation -> invocation.getArgument(1, String.class));

        // Survival by default
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);

        // Addon
        when(addon.getChallengesSettings()).thenReturn(settings);
        when(settings.isBroadcastMessages()).thenReturn(true);

        // Bukkit - online players
        Map<UUID, String> online = new HashMap<>();

        onlinePlayers = new HashSet<>();
        for (String name : NAMES) {
            Player p1 = mock(Player.class);
            UUID uuid2 = UUID.randomUUID();
            when(p1.getUniqueId()).thenReturn(uuid2);
            when(p1.getName()).thenReturn(name);
            online.put(uuid2, name);
            onlinePlayers.add(p1);
        }
        mockedBukkit = Mockito.mockStatic(Bukkit.class, Mockito.RETURNS_DEEP_STUBS);
        mockedBukkit.when(Bukkit::getServer).thenReturn(mbServer);
        mockedBukkit.when(Bukkit::getOnlinePlayers).then((Answer<Set<Player>>) invocation -> Set.of(player));

        // World settings
        Map<String, Boolean> map = new HashMap<>();
        when(mySettings.getWorldFlags()).thenReturn(map);
        when(iwm.getWorldSettings(any())).thenReturn(mySettings);
        ChallengesAddon.CHALLENGES_WORLD_PROTECTION.setSetting(world, true);

        // ItemFactory — delegate to MockBukkit's real one.
        mockedBukkit.when(Bukkit::getItemFactory).thenReturn(mbServer.getItemFactory());
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (mockedBukkit != null) mockedBukkit.closeOnDemand();
        if (mockedUtil != null) mockedUtil.closeOnDemand();
        try {
            if (closeable != null) closeable.close();
        } catch (Exception e) {
            throw new IOException(e);
        }
        MockBukkit.unmock();
        User.clearUsers();
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void testTryToCompleteChallengesAddonUserChallengeWorldStringString() {
        ttc = new TryToComplete(addon, user, challenge, world, topLabel, permissionPrefix);
        verify(addon).getChallengesManager();
    }

    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringNotDeployed() {
        challenge.setDeployed(false);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.not-deployed"));
    }

    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringWrongWorld() {
        challenge.setUniqueId("test");
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("general.errors.wrong-world"));
    }

    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringNotOnIsland() {
        ChallengesAddon.CHALLENGES_WORLD_PROTECTION.setSetting(world, true);
        when(im.locationIsOnIsland(any(Player.class), any(Location.class))).thenReturn(false);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.messages.not-on-island"));
    }

    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringNotOnIslandButOk() {
        ChallengesAddon.CHALLENGES_WORLD_PROTECTION.setSetting(world, false);
        when(im.locationIsOnIsland(any(Player.class), any(Location.class))).thenReturn(false);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.messages.you-completed-challenge"), eq("[value]"),
                eq("name"));
    }

    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringLevelNotUnlocked() {
        when(cm.isLevelUnlocked(any(), any(), any())).thenReturn(false);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.challenge-level-not-available"));
    }

    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringNotRepeatable() {
        challenge.setRepeatable(false);
        when(cm.isChallengeComplete(any(User.class), any(), any())).thenReturn(true);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.not-repeatable"));
    }

    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringNotRepeatableFirstTime() {
        challenge.setRepeatable(false);
        challenge.setMaxTimes(0);
        when(cm.getChallengeTimes(any(), any(), any(Challenge.class))).thenReturn(0L);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.messages.you-completed-challenge"), eq("[value]"),
                eq("name"));
    }

    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringNoRank() {
        when(island.isAllowed(any(), any())).thenReturn(false);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.messages.no-rank"));
    }

    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringIntZero() {
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix, 0));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.not-valid-integer"));
    }

    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringIntNegative() {
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix, -10));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.not-valid-integer"));
    }

    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringIntPositiveWrongEnvinonment() {
        challenge.setEnvironment(Collections.singleton(Environment.NETHER));
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix, 100));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.wrong-environment"));
    }

    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringIntPositiveNoPerm() {
        InventoryRequirements req = new InventoryRequirements();
        req.setRequiredPermissions(Collections.singleton("perm-you-dont-have"));
        when(user.hasPermission(anyString())).thenReturn(false);
        challenge.setRequirements(req);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix, 100));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.no-permission"));
    }

    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringSuccess() {
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.messages.you-completed-challenge"), eq("[value]"),
                eq("name"));
    }

    @Test
    @Disabled("Method is too large for JVM")
    public void testCompleteChallengesAddonUserChallengeWorldStringStringSuccessSingleReq() {
        InventoryRequirements req = new InventoryRequirements();
        req.setRequiredItems(Collections.singletonList(new ItemStack(Material.EMERALD_BLOCK)));
        challenge.setRequirements(req);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.not-enough-items"), eq("[items]"),
                eq("challenges.materials.emerald_block"));
    }

    @Test
    @Disabled("Too big for JVM")
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
        when(itemStackMock3.isSimilar(eq(itemStackMock))).thenReturn(true);
        when(itemStackMock.isSimilar(eq(itemStackMock3))).thenReturn(true);

        req.setRequiredItems(Arrays.asList(itemStackMock, itemStackMock2));
        challenge.setRequirements(req);
        ItemStack[] newContents = { itemStackMock3 };
        when(inv.getContents()).thenReturn(newContents);

        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user, never()).getTranslation(any(World.class), eq("challenges.errors.not-enough-items"), eq("[items]"),
                eq("challenges.materials.emerald_block"));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.not-enough-items"), eq("[items]"),
                eq("challenges.materials.enchanted_book"));
    }

    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringSuccessCreative() {
        when(player.getGameMode()).thenReturn(GameMode.CREATIVE);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(world, "challenges.messages.you-repeated-challenge-multiple", "[value]", "name",
                "[count]", "2");
    }

    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringIslandBBTooLarge() {
        challenge.setChallengeType(ChallengeType.ISLAND_TYPE);
        IslandRequirements req = new IslandRequirements();
        req.setSearchRadius(1);
        challenge.setRequirements(req);
        when(bb.getWidthX()).thenReturn(50000D);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(addon).logError(
                "BoundingBox is larger than SearchRadius.  | BoundingBox: BoundingBox | Search Distance: 1 | Location: location | Center: center | Range: 0");
        verify(bb).expand(1);
    }

    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringIslandSuccessNoEntities() {
        challenge.setChallengeType(ChallengeType.ISLAND_TYPE);
        IslandRequirements req = new IslandRequirements();
        req.setSearchRadius(1);
        challenge.setRequirements(req);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.messages.you-completed-challenge"), eq("[value]"),
                eq("name"));
    }

    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringIslandFailEntities() {
        challenge.setChallengeType(ChallengeType.ISLAND_TYPE);
        IslandRequirements req = new IslandRequirements();
        Map<EntityType, Integer> requiredEntities = Collections.singletonMap(EntityType.GHAST, 3);
        req.setRequiredEntities(requiredEntities);
        req.setSearchRadius(1);
        challenge.setRequirements(req);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.you-still-need"), eq("[amount]"), eq("3"),
                eq("[item]"), eq("challenges.entities.ghast.name"));
    }

    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringIslandFailMultipleEntities() {
        challenge.setChallengeType(ChallengeType.ISLAND_TYPE);
        IslandRequirements req = new IslandRequirements();
        Map<EntityType, Integer> requiredEntities = new HashMap<>();
        requiredEntities.put(EntityType.GHAST, 3);
        requiredEntities.put(EntityType.CHICKEN, 5);
        requiredEntities.put(EntityType.PUFFERFISH, 1);
        req.setRequiredEntities(requiredEntities);
        req.setSearchRadius(1);
        challenge.setRequirements(req);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.you-still-need"), eq("[amount]"), eq("3"),
                eq("[item]"), eq("challenges.entities.ghast.name"));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.you-still-need"), eq("[amount]"), eq("1"),
                eq("[item]"), eq("challenges.entities.pufferfish.name"));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.you-still-need"), eq("[amount]"), eq("5"),
                eq("[item]"), eq("challenges.entities.chicken.name"));
    }

    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringIslandFailPartialMultipleEntities() {
        challenge.setChallengeType(ChallengeType.ISLAND_TYPE);
        IslandRequirements req = new IslandRequirements();
        Map<EntityType, Integer> requiredEntities = new HashMap<>();
        requiredEntities.put(EntityType.GHAST, 3);
        requiredEntities.put(EntityType.CHICKEN, 5);
        requiredEntities.put(EntityType.PUFFERFISH, 1);
        req.setRequiredEntities(requiredEntities);
        req.setSearchRadius(1);
        challenge.setRequirements(req);
        Entity ent = mock(Entity.class);
        when(ent.getType()).thenReturn(EntityType.PUFFERFISH);
        Location loc = mock(Location.class);
        when(ent.getLocation()).thenReturn(loc);
        List<Entity> list = Collections.singletonList(ent);
        when(world.getNearbyEntities(any(BoundingBox.class))).thenReturn(list);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.you-still-need"), eq("[amount]"), eq("3"),
                eq("[item]"), eq("challenges.entities.ghast.name"));
        verify(user, never()).getTranslation(any(World.class), eq("challenges.errors.you-still-need"), eq("[amount]"),
                eq("1"), eq("[item]"), eq("challenges.entities.pufferfish.name"));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.you-still-need"), eq("[amount]"), eq("5"),
                eq("[item]"), eq("challenges.entities.chicken.name"));
    }

    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringIslandSuccess() {
        challenge.setChallengeType(ChallengeType.ISLAND_TYPE);
        IslandRequirements req = new IslandRequirements();
        Map<EntityType, Integer> requiredEntities = new HashMap<>();
        requiredEntities.put(EntityType.PUFFERFISH, 1);
        req.setRequiredEntities(requiredEntities);
        req.setSearchRadius(1);
        challenge.setRequirements(req);
        Entity ent = mock(Entity.class);
        when(ent.getType()).thenReturn(EntityType.PUFFERFISH);
        Location loc = mock(Location.class);
        when(ent.getLocation()).thenReturn(loc);
        List<Entity> list = Collections.singletonList(ent);
        when(world.getNearbyEntities(any(BoundingBox.class))).thenReturn(list);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.messages.you-completed-challenge"), eq("[value]"),
                eq("name"));
    }

    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringIslandPlayerInOtherEnvironment() {
        challenge.setEnvironment(Collections.singleton(Environment.NETHER));
        World netherWorld = mock(World.class);
        when(user.getWorld()).thenReturn(netherWorld);
        when(netherWorld.getName()).thenReturn("world_nether");
        when(netherWorld.getEnvironment()).thenReturn(Environment.NETHER);
        challenge.setChallengeType(ChallengeType.ISLAND_TYPE);
        IslandRequirements req = new IslandRequirements();
        Map<EntityType, Integer> requiredEntities = new HashMap<>();
        requiredEntities.put(EntityType.PUFFERFISH, 1);
        req.setRequiredEntities(requiredEntities);
        req.setSearchRadius(1);
        challenge.setRequirements(req);
        Entity ent = mock(Entity.class);
        when(ent.getType()).thenReturn(EntityType.PUFFERFISH);
        Location loc = mock(Location.class);
        when(ent.getLocation()).thenReturn(loc);
        List<Entity> list = Collections.singletonList(ent);
        when(world.getNearbyEntities(any(BoundingBox.class))).thenReturn(list);
        when(netherWorld.getNearbyEntities(any(BoundingBox.class))).thenReturn(Collections.emptyList());
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.you-still-need"), eq("[amount]"), eq("1"),
                eq("[item]"), eq("challenges.entities.pufferfish.name"));
    }

    @Test
    public void testCompleteChallengesAddonUserChallengeWorldStringStringIntMultipleTimesPositiveSuccess() {
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix, 10));
        verify(user).getTranslation(any(World.class), eq("challenges.messages.you-repeated-challenge-multiple"),
                eq("[value]"), eq("name"), eq("[count]"), eq("7"));
    }

    @Test
    public void testBuild() {
        this.testTryToCompleteChallengesAddonUserChallengeWorldStringString();
        ChallengeResult result = this.ttc.build(10);
        assertTrue(result.isMeetsRequirements());
    }

    @Test
    public void testRemoveItemsNothing() {
        this.testTryToCompleteChallengesAddonUserChallengeWorldStringString();
        assertTrue(ttc.removeItems(Collections.emptyList(), 1).isEmpty());
    }

    public void checkSpigotMessage(String expectedMessage) {
        checkSpigotMessage(expectedMessage, 1);
    }

    public void checkSpigotMessage(String expectedMessage, int expectedOccurrences) {
        ArgumentCaptor<TextComponent> captor = ArgumentCaptor.forClass(TextComponent.class);
        verify(spigot, atLeast(0)).sendMessage(captor.capture());
        List<TextComponent> capturedMessages = captor.getAllValues();
        long actualOccurrences = capturedMessages.stream().map(component -> component.toLegacyText())
                .filter(messageText -> messageText.contains(expectedMessage))
                .count();
        assertEquals(expectedOccurrences, actualOccurrences,
                "Expected message occurrence mismatch: " + expectedMessage);
    }

}
