package world.bentobox.challenges.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.util.BoundingBox;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import net.md_5.bungee.api.chat.TextComponent;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.challenges.AbstractChallengesTest;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.Challenge.ChallengeType;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.database.object.requirements.InventoryRequirements;
import world.bentobox.challenges.database.object.requirements.IslandRequirements;
import world.bentobox.challenges.database.object.requirements.OtherRequirements;
import world.bentobox.challenges.database.object.requirements.StatisticRequirements;
import world.bentobox.challenges.database.object.requirements.StatisticRequirements.StatisticRec;
import world.bentobox.challenges.managers.ChallengesManager;
import world.bentobox.challenges.tasks.TryToComplete.ChallengeResult;
import world.bentobox.challenges.utils.Utils;
import world.bentobox.level.Level;

/**
 * @author tastybento
 */
class TryToCompleteTest extends AbstractChallengesTest {

    // Constants
    private static final String[] NAMES = { "adam", "ben", "cara", "dave", "ed", "frank", "freddy", "george", "harry",
            "ian", "joe" };

    private TryToComplete ttc;
    private Challenge challenge;
    private final String topLabel = "island";
    private final String permissionPrefix = "perm.";
    private final ItemStack[] contents = {};

    @Override
    @BeforeEach
    protected void setUp() {
        super.setUp();

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

        // Override inventory contents to use the local empty array
        when(inv.getContents()).thenReturn(contents);

        // Bukkit - online players
        Set<Player> onlinePlayers = new HashSet<>();
        for (String name : NAMES) {
            Player p1 = mock(Player.class);
            UUID uuid2 = UUID.randomUUID();
            when(p1.getUniqueId()).thenReturn(uuid2);
            when(p1.getName()).thenReturn(name);
            onlinePlayers.add(p1);
        }
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void setupOtherChallenge(OtherRequirements req) {
        challenge.setChallengeType(ChallengeType.OTHER_TYPE);
        challenge.setRequirements(req);
    }

    private void setupStatisticChallenge(List<StatisticRec> recs) {
        challenge.setChallengeType(ChallengeType.STATISTIC_TYPE);
        StatisticRequirements req = new StatisticRequirements();
        req.setStatisticList(recs);
        challenge.setRequirements(req);
    }

    private VaultHook mockEconomy(boolean has, double balance) {
        VaultHook vault = mock(VaultHook.class);
        when(addon.isEconomyProvided()).thenReturn(true);
        when(addon.getEconomyProvider()).thenReturn(vault);
        when(vault.has(any(), any(double.class))).thenReturn(has);
        when(vault.getBalance(any())).thenReturn(balance);
        return vault;
    }

    private Level mockLevelAddon(long islandLevel) {
        Level levelAddon = mock(Level.class);
        when(addon.isLevelProvided()).thenReturn(true);
        when(addon.getLevelAddon()).thenReturn(levelAddon);
        when(levelAddon.getIslandLevel(any(), any())).thenReturn(islandLevel);
        return levelAddon;
    }

    // -------------------------------------------------------------------------
    // Existing tests
    // -------------------------------------------------------------------------

    @Test
    void testTryToCompleteChallengesAddonUserChallengeWorldStringString() {
        ttc = new TryToComplete(addon, user, challenge, world, topLabel, permissionPrefix);
        verify(addon).getChallengesManager();
    }

    @Test
    void testCompleteChallengesAddonUserChallengeWorldStringStringNotDeployed() {
        challenge.setDeployed(false);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.not-deployed"));
    }

    @Test
    void testCompleteChallengesAddonUserChallengeWorldStringStringWrongWorld() {
        challenge.setUniqueId("test");
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("general.errors.wrong-world"));
    }

    @Test
    void testCompleteChallengesAddonUserChallengeWorldStringStringNotOnIsland() {
        ChallengesAddon.CHALLENGES_WORLD_PROTECTION.setSetting(world, true);
        when(im.locationIsOnIsland(any(Player.class), any(Location.class))).thenReturn(false);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.messages.not-on-island"));
    }

    @Test
    void testCompleteChallengesAddonUserChallengeWorldStringStringNotOnIslandButOk() {
        ChallengesAddon.CHALLENGES_WORLD_PROTECTION.setSetting(world, false);
        when(im.locationIsOnIsland(any(Player.class), any(Location.class))).thenReturn(false);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.messages.you-completed-challenge"), eq("[value]"),
                eq("name"));
    }

    @Test
    void testCompleteChallengesAddonUserChallengeWorldStringStringLevelNotUnlocked() {
        when(cm.isLevelUnlocked(any(), any(), any())).thenReturn(false);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.challenge-level-not-available"));
    }

    @Test
    void testCompleteChallengesAddonUserChallengeWorldStringStringNotRepeatable() {
        challenge.setRepeatable(false);
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(true);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.not-repeatable"));
    }

    @Test
    void testCompleteChallengesAddonUserChallengeWorldStringStringNotRepeatableFirstTime() {
        challenge.setRepeatable(false);
        challenge.setMaxTimes(0);
        when(cm.getChallengeTimes(any(), any(), any(Challenge.class))).thenReturn(0L);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.messages.you-completed-challenge"), eq("[value]"),
                eq("name"));
    }

    @Test
    void testCompleteChallengesAddonUserChallengeWorldStringStringNoRank() {
        when(island.isAllowed(any(), any())).thenReturn(false);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.messages.no-rank"));
    }

    @Test
    void testCompleteChallengesAddonUserChallengeWorldStringStringIntZero() {
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix, 0));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.not-valid-integer"));
    }

    @Test
    void testCompleteChallengesAddonUserChallengeWorldStringStringIntNegative() {
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix, -10));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.not-valid-integer"));
    }

    @Test
    void testCompleteChallengesAddonUserChallengeWorldStringStringIntPositiveWrongEnvinonment() {
        challenge.setEnvironment(Collections.singleton(Environment.NETHER));
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix, 100));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.wrong-environment"));
    }

    @Test
    void testCompleteChallengesAddonUserChallengeWorldStringStringIntPositiveNoPerm() {
        InventoryRequirements req = new InventoryRequirements();
        req.setRequiredPermissions(Collections.singleton("perm-you-dont-have"));
        when(user.hasPermission(anyString())).thenReturn(false);
        challenge.setRequirements(req);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix, 100));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.no-permission"));
    }

    @Test
    void testCompleteChallengesAddonUserChallengeWorldStringStringSuccess() {
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.messages.you-completed-challenge"), eq("[value]"),
                eq("name"));
    }

    @Test
    @Disabled("Method is too large for JVM")
    void testCompleteChallengesAddonUserChallengeWorldStringStringSuccessSingleReq() {
        InventoryRequirements req = new InventoryRequirements();
        req.setRequiredItems(Collections.singletonList(new ItemStack(Material.EMERALD_BLOCK)));
        challenge.setRequirements(req);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.not-enough-items"), eq("[items]"),
                eq("challenges.materials.emerald_block"));
    }

    @Test
    @Disabled("Too big for JVM")
    void testCompleteChallengesAddonUserChallengeWorldStringStringSuccessMultipleReq() {
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
        when(itemStackMock3.isSimilar(itemStackMock)).thenReturn(true);
        when(itemStackMock.isSimilar(itemStackMock3)).thenReturn(true);

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
    void testCompleteChallengesAddonUserChallengeWorldStringStringSuccessCreative() {
        when(player.getGameMode()).thenReturn(GameMode.CREATIVE);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(world, "challenges.messages.you-repeated-challenge-multiple", "[value]", "name",
                "[count]", "2");
    }

    @Test
    void testCompleteChallengesAddonUserChallengeWorldStringStringIslandBBTooLarge() {
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
    void testCompleteChallengesAddonUserChallengeWorldStringStringIslandSuccessNoEntities() {
        challenge.setChallengeType(ChallengeType.ISLAND_TYPE);
        IslandRequirements req = new IslandRequirements();
        req.setSearchRadius(1);
        challenge.setRequirements(req);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.messages.you-completed-challenge"), eq("[value]"),
                eq("name"));
    }

    @Test
    void testCompleteChallengesAddonUserChallengeWorldStringStringIslandFailEntities() {
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
    void testCompleteChallengesAddonUserChallengeWorldStringStringIslandFailMultipleEntities() {
        challenge.setChallengeType(ChallengeType.ISLAND_TYPE);
        IslandRequirements req = new IslandRequirements();
        Map<EntityType, Integer> requiredEntities = new EnumMap<>(EntityType.class);
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

    /**
     * Mocks the biome at the player's location and returns the requirements for further setup.
     */
    private IslandRequirements setupBiomeChallenge(String currentBiomeKey, Set<String> requiredBiomes) {
        challenge.setChallengeType(ChallengeType.ISLAND_TYPE);
        IslandRequirements req = new IslandRequirements();
        req.setSearchRadius(1);
        req.setRequiredBiomes(requiredBiomes);
        challenge.setRequirements(req);

        Block block = mock(Block.class);
        Biome biome = mock(Biome.class);
        String[] parts = currentBiomeKey.split(":");
        when(biome.getKey()).thenReturn(NamespacedKey.minecraft(parts[parts.length - 1]));
        when(block.getBiome()).thenReturn(biome);
        when(user.getLocation().getBlock()).thenReturn(block);
        return req;
    }

    @Test
    void testIslandChallengeSucceedsWhenInRequiredBiome() {
        setupBiomeChallenge("minecraft:plains", new HashSet<>(Set.of("minecraft:plains")));
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
    }

    @Test
    void testIslandChallengeFailsWhenNotInRequiredBiome() {
        setupBiomeChallenge("minecraft:plains", new HashSet<>(Set.of("minecraft:desert")));
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.wrong-biome"), eq("[biome]"),
                eq("Plains"));
    }

    @Test
    void testIslandChallengeIgnoresBiomeWhenNoneRequired() {
        // No required biomes: the biome gate is skipped and the (empty) island challenge completes.
        challenge.setChallengeType(ChallengeType.ISLAND_TYPE);
        IslandRequirements req = new IslandRequirements();
        req.setSearchRadius(1);
        challenge.setRequirements(req);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
    }

    @Test
    void testCompleteChallengesAddonUserChallengeWorldStringStringIslandFailPartialMultipleEntities() {
        challenge.setChallengeType(ChallengeType.ISLAND_TYPE);
        IslandRequirements req = new IslandRequirements();
        Map<EntityType, Integer> requiredEntities = new EnumMap<>(EntityType.class);
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
    void testCompleteChallengesAddonUserChallengeWorldStringStringIslandSuccess() {
        challenge.setChallengeType(ChallengeType.ISLAND_TYPE);
        IslandRequirements req = new IslandRequirements();
        Map<EntityType, Integer> requiredEntities = new EnumMap<>(EntityType.class);
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
    void testCompleteChallengesAddonUserChallengeWorldStringStringIslandPlayerInOtherEnvironment() {
        challenge.setEnvironment(Collections.singleton(Environment.NETHER));
        World netherWorld = mock(World.class);
        when(user.getWorld()).thenReturn(netherWorld);
        when(netherWorld.getName()).thenReturn("world_nether");
        when(netherWorld.getEnvironment()).thenReturn(Environment.NETHER);
        challenge.setChallengeType(ChallengeType.ISLAND_TYPE);
        IslandRequirements req = new IslandRequirements();
        Map<EntityType, Integer> requiredEntities = new EnumMap<>(EntityType.class);
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
    void testCompleteChallengesAddonUserChallengeWorldStringStringIntMultipleTimesPositiveSuccess() {
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix, 10));
        verify(user).getTranslation(any(World.class), eq("challenges.messages.you-repeated-challenge-multiple"),
                eq("[value]"), eq("name"), eq("[count]"), eq("7"));
    }

    @Test
    void testBuild() {
        this.testTryToCompleteChallengesAddonUserChallengeWorldStringString();
        ChallengeResult result = this.ttc.build(10);
        assertTrue(result.isMeetsRequirements());
    }

    @Test
    void testRemoveItemsNothing() {
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

    // -------------------------------------------------------------------------
    // OTHER_TYPE tests
    // -------------------------------------------------------------------------

    @Test
    void testOtherTypeSuccessNoRequirements() {
        OtherRequirements req = new OtherRequirements();
        setupOtherChallenge(req);
        // Stub PAPI hook absent so the check is skipped
        when(plugin.getHooks()).thenReturn(mock(world.bentobox.bentobox.managers.HooksManager.class));
        when(plugin.getHooks().getHook("PlaceholderAPI")).thenReturn(Optional.empty());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
    }

    @Test
    void testOtherTypeFailIslandLevelNoAddon() {
        OtherRequirements req = new OtherRequirements();
        req.setRequiredIslandLevel(100);
        setupOtherChallenge(req);
        when(addon.isLevelProvided()).thenReturn(false);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.missing-addon"));
    }

    @Test
    void testOtherTypeFailMoneyNoEconomy() {
        OtherRequirements req = new OtherRequirements();
        req.setRequiredMoney(100.0);
        setupOtherChallenge(req);
        when(addon.isEconomyProvided()).thenReturn(false);
        when(addon.isLevelProvided()).thenReturn(false);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.missing-addon"));
    }

    @Test
    void testOtherTypeFailMoneyInsufficient() {
        OtherRequirements req = new OtherRequirements();
        req.setRequiredMoney(100.0);
        setupOtherChallenge(req);
        when(addon.isLevelProvided()).thenReturn(false);
        mockEconomy(false, 50.0);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.not-enough-money"), eq("[value]"),
                eq("100.0"));
    }

    @Test
    void testOtherTypeSuccessMoney() {
        OtherRequirements req = new OtherRequirements();
        req.setRequiredMoney(50.0);
        setupOtherChallenge(req);
        when(addon.isLevelProvided()).thenReturn(false);
        mockEconomy(true, 100.0);
        when(plugin.getHooks()).thenReturn(mock(world.bentobox.bentobox.managers.HooksManager.class));
        when(plugin.getHooks().getHook("PlaceholderAPI")).thenReturn(Optional.empty());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
    }

    @Test
    void testOtherTypeFailExperienceInsufficient() {
        OtherRequirements req = new OtherRequirements();
        req.setRequiredExperience(100);
        setupOtherChallenge(req);
        when(addon.isLevelProvided()).thenReturn(false);
        when(player.getTotalExperience()).thenReturn(50);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.not-enough-experience"), eq("[value]"),
                eq("100"));
    }

    @Test
    void testOtherTypeSuccessExperienceCreative() {
        OtherRequirements req = new OtherRequirements();
        req.setRequiredExperience(100);
        setupOtherChallenge(req);
        when(addon.isLevelProvided()).thenReturn(false);
        when(player.getTotalExperience()).thenReturn(0);
        when(player.getGameMode()).thenReturn(GameMode.CREATIVE);
        when(plugin.getHooks()).thenReturn(mock(world.bentobox.bentobox.managers.HooksManager.class));
        when(plugin.getHooks().getHook("PlaceholderAPI")).thenReturn(Optional.empty());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
    }

    @Test
    void testOtherTypeFailIslandLevelInsufficient() {
        OtherRequirements req = new OtherRequirements();
        req.setRequiredIslandLevel(100);
        req.setRequiredMoney(0);
        setupOtherChallenge(req);
        mockLevelAddon(50);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.island-level"), eq("[number]"),
                eq("100"));
    }

    @Test
    void testOtherTypeSuccessIslandLevel() {
        OtherRequirements req = new OtherRequirements();
        req.setRequiredIslandLevel(50);
        req.setRequiredMoney(0);
        setupOtherChallenge(req);
        mockLevelAddon(100);
        when(plugin.getHooks()).thenReturn(mock(world.bentobox.bentobox.managers.HooksManager.class));
        when(plugin.getHooks().getHook("PlaceholderAPI")).thenReturn(Optional.empty());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
    }

    @Test
    void testOtherTypeFulfillTakesMoney() {
        OtherRequirements req = new OtherRequirements();
        req.setRequiredMoney(50.0);
        req.setTakeMoney(true);
        setupOtherChallenge(req);
        when(addon.isLevelProvided()).thenReturn(false);
        VaultHook vault = mockEconomy(true, 100.0);
        when(plugin.getHooks()).thenReturn(mock(world.bentobox.bentobox.managers.HooksManager.class));
        when(plugin.getHooks().getHook("PlaceholderAPI")).thenReturn(Optional.empty());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(vault).withdraw(user, 50.0);
    }

    @Test
    void testOtherTypeFulfillTakesExperience() {
        OtherRequirements req = new OtherRequirements();
        req.setRequiredExperience(30);
        req.setTakeExperience(true);
        setupOtherChallenge(req);
        when(addon.isLevelProvided()).thenReturn(false);
        when(player.getTotalExperience()).thenReturn(100);
        when(plugin.getHooks()).thenReturn(mock(world.bentobox.bentobox.managers.HooksManager.class));
        when(plugin.getHooks().getHook("PlaceholderAPI")).thenReturn(Optional.empty());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(player).setTotalExperience(70);
    }

    @Test
    void testOtherTypeFulfillCreativeSkipsXpTake() {
        OtherRequirements req = new OtherRequirements();
        req.setRequiredExperience(30);
        req.setTakeExperience(true);
        setupOtherChallenge(req);
        when(addon.isLevelProvided()).thenReturn(false);
        when(player.getTotalExperience()).thenReturn(100);
        when(player.getGameMode()).thenReturn(GameMode.CREATIVE);
        when(plugin.getHooks()).thenReturn(mock(world.bentobox.bentobox.managers.HooksManager.class));
        when(plugin.getHooks().getHook("PlaceholderAPI")).thenReturn(Optional.empty());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(player, never()).setTotalExperience(any(int.class));
    }

    // -------------------------------------------------------------------------
    // STATISTIC_TYPE tests
    // -------------------------------------------------------------------------

    @Test
    void testStatisticTypeEmptyRequirements() {
        setupStatisticChallenge(Collections.emptyList());
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
    }

    @Test
    void testStatisticTypeSuccessUntyped() {
        StatisticRec rec = new StatisticRec(Statistic.JUMP, null, null, 10, false);
        setupStatisticChallenge(List.of(rec));
        when(cm.getStatisticData(any(), any(), eq(Statistic.JUMP))).thenReturn(15);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
    }

    @Test
    void testStatisticTypeFailUntypedInsufficient() {
        StatisticRec rec = new StatisticRec(Statistic.JUMP, null, null, 10, false);
        setupStatisticChallenge(List.of(rec));
        when(cm.getStatisticData(any(), any(), eq(Statistic.JUMP))).thenReturn(5);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.requirement-not-met"),
                eq("[number]"), eq("10"), eq("[statistic]"), anyString(), eq("[value]"), eq("5"));
    }

    @Test
    void testStatisticTypeSuccessItemType() {
        StatisticRec rec = new StatisticRec(Statistic.MINE_BLOCK, null, Material.STONE, 20, false);
        setupStatisticChallenge(List.of(rec));
        when(cm.getStatisticData(any(), any(), eq(Statistic.MINE_BLOCK), eq(Material.STONE))).thenReturn(25);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
    }

    @Test
    void testStatisticTypeFailItemInsufficient() {
        StatisticRec rec = new StatisticRec(Statistic.MINE_BLOCK, null, Material.STONE, 20, false);
        setupStatisticChallenge(List.of(rec));
        when(cm.getStatisticData(any(), any(), eq(Statistic.MINE_BLOCK), eq(Material.STONE))).thenReturn(5);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.requirement-not-met-material"),
                eq("[number]"), eq("20"), eq("[statistic]"), anyString(), eq("[material]"), anyString(),
                eq("[value]"), eq("5"));
    }

    @Test
    void testStatisticTypeSuccessEntityType() {
        StatisticRec rec = new StatisticRec(Statistic.KILL_ENTITY, EntityType.ZOMBIE, null, 5, false);
        setupStatisticChallenge(List.of(rec));
        when(cm.getStatisticData(any(), any(), eq(Statistic.KILL_ENTITY), eq(EntityType.ZOMBIE))).thenReturn(10);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
    }

    @Test
    void testStatisticTypeFailEntityInsufficient() {
        StatisticRec rec = new StatisticRec(Statistic.KILL_ENTITY, EntityType.ZOMBIE, null, 5, false);
        setupStatisticChallenge(List.of(rec));
        when(cm.getStatisticData(any(), any(), eq(Statistic.KILL_ENTITY), eq(EntityType.ZOMBIE))).thenReturn(2);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.requirement-not-met-entity"),
                eq("[number]"), eq("5"), eq("[statistic]"), anyString(), eq("[entity]"), anyString(),
                eq("[value]"), eq("2"));
    }

    @Test
    void testStatisticTypeMultipleMixed() {
        StatisticRec rec1 = new StatisticRec(Statistic.JUMP, null, null, 10, false);
        StatisticRec rec2 = new StatisticRec(Statistic.KILL_ENTITY, EntityType.ZOMBIE, null, 5, false);
        setupStatisticChallenge(List.of(rec1, rec2));
        when(cm.getStatisticData(any(), any(), eq(Statistic.JUMP))).thenReturn(15);
        when(cm.getStatisticData(any(), any(), eq(Statistic.KILL_ENTITY), eq(EntityType.ZOMBIE))).thenReturn(10);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
    }

    // -------------------------------------------------------------------------
    // Timeout tests
    // -------------------------------------------------------------------------

    @Test
    void testTimeoutBreached() {
        challenge.setTimeout(60000);
        when(cm.isBreachingTimeOut(any(), any(), any())).thenReturn(true);
        when(cm.getLastCompletionDate(any(), any(), any())).thenReturn(System.currentTimeMillis() - 10000);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.timeout"),
                eq("[timeout]"), anyString(), eq("[wait-time]"), anyString());
    }

    @Test
    void testTimeoutNotBreached() {
        challenge.setTimeout(60000);
        when(cm.isBreachingTimeOut(any(), any(), any())).thenReturn(false);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
    }

    // -------------------------------------------------------------------------
    // Reward distribution tests
    // -------------------------------------------------------------------------

    @Test
    void testFirstTimeRewardMoney() {
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(false);
        challenge.setRewardMoney(100.0);
        VaultHook vault = mockEconomy(true, 1000.0);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(vault).deposit(user, 100.0);
    }

    @Test
    void testFirstTimeRewardExperience() {
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(false);
        challenge.setRewardExperience(50);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(player).giveExp(50);
    }

    @Test
    void testRepeatRewardMoney() {
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(true);
        challenge.setRepeatMoneyReward(25.0);
        VaultHook vault = mockEconomy(true, 1000.0);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(vault).deposit(user, 25.0);
    }

    @Test
    void testRepeatRewardExperience() {
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(true);
        challenge.setRepeatExperienceReward(10);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(player).giveExp(10);
    }

    @Test
    void testMultipleTimesFirstCompletion() {
        // First completion with factor > 1: first-time rewards once + repeat rewards (factor-1) times
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(false);
        challenge.setRewardMoney(100.0);
        challenge.setRepeatMoneyReward(25.0);
        challenge.setRewardExperience(50);
        challenge.setRepeatExperienceReward(10);
        VaultHook vault = mockEconomy(true, 1000.0);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        when(cm.getChallengeTimes(any(), any(), any(Challenge.class))).thenReturn(3L);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix, 5));
        // Factor = 5. First-time: deposit(100), repeat: deposit(25*4=100). Total: 2 deposit calls.
        ArgumentCaptor<Double> moneyCaptor = ArgumentCaptor.forClass(Double.class);
        verify(vault, org.mockito.Mockito.times(2)).deposit(eq(user), moneyCaptor.capture());
        List<Double> deposits = moneyCaptor.getAllValues();
        assertEquals(100.0, deposits.get(0)); // first-time reward
        assertEquals(100.0, deposits.get(1)); // repeat: 25 * 4
        // First-time: giveExp(50), repeat: giveExp(10*4=40)
        ArgumentCaptor<Integer> xpCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(player, org.mockito.Mockito.times(2)).giveExp(xpCaptor.capture());
        List<Integer> xps = xpCaptor.getAllValues();
        assertEquals(50, xps.get(0));
        assertEquals(40, xps.get(1));
    }

    // -------------------------------------------------------------------------
    // Level completion tests
    // -------------------------------------------------------------------------

    @Test
    void testLevelCompletionTriggered() {
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(false);
        // Non-free challenge with a level
        challenge.setLevel(GAME_MODE_NAME + "_novice");
        ChallengeLevel lvl = new ChallengeLevel();
        lvl.setUniqueId(GAME_MODE_NAME + "_novice");
        lvl.setFriendlyName("Novice");
        lvl.setRewardExperience(200);
        // Stub both overloads: getLevel(String) used in checkIfCanCompleteChallenge,
        // getLevel(Challenge) used in tryCompleteLevel()
        when(cm.getLevel(GAME_MODE_NAME + "_novice")).thenReturn(lvl);
        when(cm.getLevel(any(Challenge.class))).thenReturn(lvl);
        when(cm.isLevelCompleted(any(), any(), any())).thenReturn(false);
        when(cm.validateLevelCompletion(any(), any(), any())).thenReturn(true);
        // Mock tryCompleteLevel to return the level (which triggers reward logic)
        when(cm.tryCompleteLevel(any(), any(), any())).thenReturn(lvl);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        // Verify that tryCompleteLevel was called to complete the level
        verify(cm).tryCompleteLevel(any(), any(), eq(challenge));
        verify(player).giveExp(200);
    }

    @Test
    void testLevelCompletionAlreadyDone() {
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(false);
        challenge.setLevel(GAME_MODE_NAME + "_novice");
        ChallengeLevel lvl = new ChallengeLevel();
        lvl.setUniqueId(GAME_MODE_NAME + "_novice");
        lvl.setFriendlyName("Novice");
        when(cm.getLevel(GAME_MODE_NAME + "_novice")).thenReturn(lvl);
        when(cm.getLevel(any(Challenge.class))).thenReturn(lvl);
        when(cm.isLevelCompleted(any(), any(), any())).thenReturn(true);
        // Mock tryCompleteLevel to return null since level is already completed
        when(cm.tryCompleteLevel(any(), any(), any())).thenReturn(null);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        // Verify tryCompleteLevel was called but didn't complete the level (returned null)
        verify(cm).tryCompleteLevel(any(), any(), eq(challenge));
    }

    @Test
    void testFreeChallengeNoLevelCheck() {
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(false);
        challenge.setLevel(ChallengesManager.FREE);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(cm, never()).getLevel(any(Challenge.class));
    }

    // -------------------------------------------------------------------------
    // Reward chance tests
    // -------------------------------------------------------------------------

    @Test
    void testRewardChanceDefaultIs100() {
        Challenge c = new Challenge();
        assertEquals(100, c.getRewardChance());
    }

    @Test
    void testRewardChance100GivesRewards() {
        challenge.setRewardChance(100);
        challenge.setRewardExperience(50);
        challenge.setRewardMoney(100);
        challenge.setRewardItems(Collections.singletonList(new ItemStack(Material.EMERALD)));
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(false);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        mockEconomy(true, 1000);

        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        verify(inv, atLeast(1)).addItem(any());
        verify(addon.getEconomyProvider()).deposit(any(), eq(100.0));
        verify(player).giveExp(50);
    }

    @Test
    void testRewardChance0NoItems() {
        challenge.setRewardChance(0);
        challenge.setRewardExperience(50);
        challenge.setRewardMoney(100);
        challenge.setRewardItems(Collections.singletonList(new ItemStack(Material.EMERALD)));
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(false);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        mockEconomy(true, 1000);

        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        // Should not add reward items
        verify(inv, never()).addItem(any());
        // Should not deposit money
        verify(addon.getEconomyProvider(), never()).deposit(any(), eq(100.0));
        // Should not give experience
        verify(player, never()).giveExp(50);
    }

    @Test
    void testRewardChance0SendsNoRewardMessage() {
        challenge.setRewardChance(0);
        challenge.setRewardItems(Collections.singletonList(new ItemStack(Material.EMERALD)));
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(false);
        when(inv.addItem(any())).thenReturn(new HashMap<>());

        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        verify(user).getTranslation(any(World.class), eq("challenges.messages.no-reward-this-time"));
    }

    @Test
    void testRewardChance0NoRewardsConfiguredNoMessage() {
        challenge.setRewardChance(0);
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(false);
        when(inv.addItem(any())).thenReturn(new HashMap<>());

        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        verify(user, never()).getTranslation(any(World.class), eq("challenges.messages.no-reward-this-time"));
    }

    @Test
    void testRewardChance0RepeatSendsNoRewardMessage() {
        challenge.setRewardChance(0);
        challenge.setRepeatable(true);
        challenge.setRepeatItemReward(Collections.singletonList(new ItemStack(Material.DIAMOND)));
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(true);
        when(inv.addItem(any())).thenReturn(new HashMap<>());

        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        verify(user).getTranslation(any(World.class), eq("challenges.messages.no-reward-this-time"));
    }

    @Test
    void testRewardChance100NoMissMessage() {
        challenge.setRewardChance(100);
        challenge.setRewardItems(Collections.singletonList(new ItemStack(Material.EMERALD)));
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(false);
        when(inv.addItem(any())).thenReturn(new HashMap<>());

        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        verify(user, never()).getTranslation(any(World.class), eq("challenges.messages.no-reward-this-time"));
    }

    @Test
    void testRewardChanceRepeatRewards() {
        challenge.setRewardChance(100);
        challenge.setRepeatable(true);
        challenge.setRepeatExperienceReward(25);
        challenge.setRepeatMoneyReward(50);
        challenge.setRepeatItemReward(Collections.singletonList(new ItemStack(Material.DIAMOND)));
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(true);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        mockEconomy(true, 1000);

        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        // Repeat rewards with 100% chance should be given
        verify(inv, atLeast(1)).addItem(any());
        verify(addon.getEconomyProvider()).deposit(any(), eq(50.0));
        verify(player).giveExp(25);
    }

    @Test
    void testFirstTimeRewardIslandLevel() {
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(false);
        challenge.setRewardIslandLevel(50L);
        Level levelAddon = mockLevelAddon(100L);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(levelAddon).setIslandLevel(world, user.getUniqueId(), 150L);
    }

    @Test
    void testRepeatRewardIslandLevel() {
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(true);
        challenge.setRepeatIslandLevel(25L);
        Level levelAddon = mockLevelAddon(100L);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(levelAddon).setIslandLevel(world, user.getUniqueId(), 125L);
    }

    @Test
    void testFirstTimeRewardIslandLevelNoLevel() {
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(false);
        challenge.setRewardIslandLevel(50L);
        when(addon.isLevelProvided()).thenReturn(false);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        // Should not call Level addon methods
        verify(addon, never()).getLevelAddon();
    }

    @Test
    void testFirstTimeRewardIslandLevelZero() {
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(false);
        challenge.setRewardIslandLevel(0L);
        Level levelAddon = mockLevelAddon(100L);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        // Should not call setIslandLevel when reward is 0
        verify(levelAddon, never()).setIslandLevel(any(), any(), anyLong());
    }

    @Test
    void testLevelCompletionRewardIslandLevel() {
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(false);
        ChallengeLevel lvl = new ChallengeLevel();
        lvl.setUniqueId(GAME_MODE_NAME + "_novice");
        lvl.setFriendlyName("Novice");
        lvl.setRewardIslandLevel(100L);
        when(cm.getLevel(GAME_MODE_NAME + "_novice")).thenReturn(lvl);
        when(cm.getLevel(any(Challenge.class))).thenReturn(lvl);
        when(cm.tryCompleteLevel(any(), any(), any())).thenReturn(lvl);
        Level levelAddon = mockLevelAddon(100L);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(levelAddon).setIslandLevel(world, user.getUniqueId(), 200L);
    }

    // -------------------------------------------------------------------------
    // Tests for issue #320: Potion type comparison when metadata is ignored
    // -------------------------------------------------------------------------

    @Test
    void testPotionComparisonIgnoreMetadataSameType() {
        // Test that potions with same base type match when metadata is ignored
        // by using Utils.groupEqualItems which should group them together
        ItemStack swiftnessPotion1 = createPotion(Material.POTION, PotionType.SWIFTNESS);
        swiftnessPotion1.setAmount(1);
        ItemStack swiftnessPotion2 = createPotion(Material.POTION, PotionType.SWIFTNESS);
        swiftnessPotion2.setAmount(1);
        // Add different custom name to swiftnessPotion2 to ensure metadata differs
        PotionMeta meta = (PotionMeta) swiftnessPotion2.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("Fancy Swiftness");
            swiftnessPotion2.setItemMeta(meta);
        }

        // When grouping items with ignore-metadata set for potions,
        // potions with the same base type should be grouped together
        Set<Material> ignoreMetaData = Set.of(Material.POTION);
        List<ItemStack> requiredItems = Arrays.asList(swiftnessPotion1, swiftnessPotion2);
        List<ItemStack> grouped = Utils.groupEqualItems(requiredItems, ignoreMetaData);

        // Both swiftness potions should be grouped into one stack with amount 2
        assertEquals(1, grouped.size(), "Potions with same base type should be grouped together");
        assertEquals(2, grouped.get(0).getAmount(), "Grouped potion should have combined amount");
        assertEquals(Material.POTION, grouped.get(0).getType(), "Grouped potion should be POTION");
    }

    @Test
    void testPotionComparisonIgnoreMetadataDifferentType() {
        // Test that potions with different base types DON'T group when metadata is ignored
        ItemStack swiftnessPotion = createPotion(Material.POTION, PotionType.SWIFTNESS);
        swiftnessPotion.setAmount(1);
        ItemStack strengthPotion = createPotion(Material.POTION, PotionType.STRENGTH);
        strengthPotion.setAmount(1);

        // When grouping potions with different base types and ignore-metadata set,
        // they should NOT be grouped together
        Set<Material> ignoreMetaData = Set.of(Material.POTION);
        List<ItemStack> requiredItems = Arrays.asList(swiftnessPotion, strengthPotion);
        List<ItemStack> grouped = Utils.groupEqualItems(requiredItems, ignoreMetaData);

        // Different potion types should NOT be grouped
        assertEquals(2, grouped.size(), "Potions with different base types should NOT be grouped");
        assertEquals(1, grouped.get(0).getAmount(), "First potion should keep original amount");
        assertEquals(1, grouped.get(1).getAmount(), "Second potion should keep original amount");
    }

    @Test
    void testPotionComparisonHelperMethod() {
        // Unit test for the helper method that checks if items match with potion type comparison
        ItemStack swiftnessPotion1 = createPotion(Material.POTION, PotionType.SWIFTNESS);
        ItemStack swiftnessPotion2 = createPotion(Material.POTION, PotionType.SWIFTNESS);
        ItemStack strengthPotion = createPotion(Material.POTION, PotionType.STRENGTH);

        // Test that same potion type matches
        assertTrue(Utils.comparePotionType(swiftnessPotion1, swiftnessPotion2),
                "Potions with same base type should compare as equal");

        // Test that different potion types don't match
        assertFalse(Utils.comparePotionType(swiftnessPotion1, strengthPotion),
                "Potions with different base types should not compare as equal");

        // Test that potion-like check works
        assertTrue(Utils.isPotionLike(Material.POTION),
                "POTION should be recognized as potion-like");
        assertTrue(Utils.isPotionLike(Material.SPLASH_POTION),
                "SPLASH_POTION should be recognized as potion-like");
        assertTrue(Utils.isPotionLike(Material.LINGERING_POTION),
                "LINGERING_POTION should be recognized as potion-like");
        assertTrue(Utils.isPotionLike(Material.TIPPED_ARROW),
                "TIPPED_ARROW should be recognized as potion-like");
        assertFalse(Utils.isPotionLike(Material.DIRT),
                "DIRT should not be recognized as potion-like");
    }

    @Test
    void testNonPotionIgnoreMetadataUnchanged() {
        // Test that non-potion materials still use type-only comparison
        ItemStack dirt1 = new ItemStack(Material.DIRT);
        dirt1.setAmount(1);
        ItemStack dirt2 = new ItemStack(Material.DIRT);
        dirt2.setAmount(1);

        // When grouping non-potion items with ignore-metadata set,
        // they should be grouped together by type alone
        Set<Material> ignoreMetaData = Set.of(Material.DIRT);
        List<ItemStack> requiredItems = Arrays.asList(dirt1, dirt2);
        List<ItemStack> grouped = Utils.groupEqualItems(requiredItems, ignoreMetaData);

        // Both dirt items should be grouped into one stack with amount 2
        assertEquals(1, grouped.size(), "Non-potion items with same type should be grouped");
        assertEquals(2, grouped.get(0).getAmount(), "Grouped items should have combined amount");
    }

    /**
     * Helper method to create a potion ItemStack with specified base potion type
     */
    private ItemStack createPotion(Material material, PotionType potionType) {
        ItemStack potion = new ItemStack(material);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        if (meta != null) {
            meta.setBasePotionType(potionType);
            potion.setItemMeta(meta);
        }
        return potion;
    }

    // -------------------------------------------------------------------------
    // Consumption/Removal tests (Issue #111)
    // -------------------------------------------------------------------------

    @Test
    void testInventoryChallengeWithTakeItemsTrue() {
        // Setup inventory challenge with takeItems=true
        InventoryRequirements req = new InventoryRequirements();
        ItemStack requiredItem = new ItemStack(Material.EMERALD_BLOCK, 1);
        req.setRequiredItems(Collections.singletonList(requiredItem));
        req.setTakeItems(true);
        challenge.setRequirements(req);

        // Mock inventory with the required item
        ItemStack inventoryItem = new ItemStack(Material.EMERALD_BLOCK, 5);
        when(inv.getContents()).thenReturn(new ItemStack[]{inventoryItem});
        when(player.getInventory()).thenReturn(inv);

        // Complete the challenge
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        // Verify items were removed by checking the inventory item amount changed
        // The removeItems method modifies the ItemStack in-place via setAmount
        assertEquals(4, inventoryItem.getAmount(), "Item amount should be reduced by 1");
    }

    @Test
    void testInventoryChallengeWithTakeItemsFalse() {
        // Setup inventory challenge with takeItems=false
        InventoryRequirements req = new InventoryRequirements();
        ItemStack requiredItem = new ItemStack(Material.EMERALD_BLOCK, 1);
        req.setRequiredItems(Collections.singletonList(requiredItem));
        req.setTakeItems(false);
        challenge.setRequirements(req);

        // Mock inventory with the required item
        ItemStack inventoryItem = new ItemStack(Material.EMERALD_BLOCK, 5);
        when(inv.getContents()).thenReturn(new ItemStack[]{inventoryItem});
        when(player.getInventory()).thenReturn(inv);

        // Complete the challenge
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        // Verify items were NOT removed
        assertEquals(5, inventoryItem.getAmount(), "Item amount should remain unchanged when takeItems=false");
    }

    @Test
    void testInventoryChallengeMultipleItemsRemoved() {
        // Test that multiple required items are all removed
        InventoryRequirements req = new InventoryRequirements();
        ItemStack item1 = new ItemStack(Material.EMERALD, 2);
        ItemStack item2 = new ItemStack(Material.DIAMOND, 3);
        req.setRequiredItems(Arrays.asList(item1, item2));
        req.setTakeItems(true);
        challenge.setRequirements(req);

        // Mock inventory with required items
        ItemStack invEmerald = new ItemStack(Material.EMERALD, 10);
        ItemStack invDiamond = new ItemStack(Material.DIAMOND, 10);
        when(inv.getContents()).thenReturn(new ItemStack[]{invEmerald, invDiamond});
        when(player.getInventory()).thenReturn(inv);

        // Complete the challenge
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        // Verify both items were removed
        assertEquals(8, invEmerald.getAmount(), "Emeralds should be reduced by 2");
        assertEquals(7, invDiamond.getAmount(), "Diamonds should be reduced by 3");
    }

    @Test
    void testIslandChallengeWithRemoveBlocksTrue() {
        // Setup island challenge with removeBlocks=true
        challenge.setChallengeType(ChallengeType.ISLAND_TYPE);
        IslandRequirements req = new IslandRequirements();
        req.setSearchRadius(10);
        req.setRequiredBlocks(Collections.singletonMap(Material.STONE, 1));
        req.setRemoveBlocks(true);
        challenge.setRequirements(req);

        // Mock a block in the world
        Block mockBlock = mock(Block.class);
        when(mockBlock.getType()).thenReturn(Material.STONE);
        Location blockLoc = mock(Location.class);
        when(mockBlock.getLocation()).thenReturn(blockLoc);
        when(world.getBlockAt(anyInt(), anyInt(), anyInt())).thenReturn(mockBlock);

        // Complete the challenge
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        // Verify the block was set to AIR
        verify(mockBlock).setType(Material.AIR);
    }

    @Test
    void testIslandChallengeWithRemoveBlocksFalse() {
        // Setup island challenge with removeBlocks=false
        challenge.setChallengeType(ChallengeType.ISLAND_TYPE);
        IslandRequirements req = new IslandRequirements();
        req.setSearchRadius(10);
        req.setRequiredBlocks(Collections.singletonMap(Material.STONE, 1));
        req.setRemoveBlocks(false);
        challenge.setRequirements(req);

        // Mock a block in the world
        Block mockBlock = mock(Block.class);
        when(mockBlock.getType()).thenReturn(Material.STONE);
        Location blockLoc = mock(Location.class);
        when(mockBlock.getLocation()).thenReturn(blockLoc);
        when(world.getBlockAt(anyInt(), anyInt(), anyInt())).thenReturn(mockBlock);

        // Complete the challenge
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        // Verify the block was NOT removed (setType not called)
        verify(mockBlock, never()).setType(any());
    }

    @Test
    void testIslandChallengeWithRemoveEntitiesTrue() {
        // Setup island challenge with removeEntities=true
        challenge.setChallengeType(ChallengeType.ISLAND_TYPE);
        IslandRequirements req = new IslandRequirements();
        req.setSearchRadius(10);
        req.setRequiredEntities(Collections.singletonMap(EntityType.GHAST, 1));
        req.setRemoveEntities(true);
        challenge.setRequirements(req);

        // Mock an entity in the world
        Entity mockEntity = mock(Entity.class);
        when(mockEntity.getType()).thenReturn(EntityType.GHAST);
        Location entityLoc = mock(Location.class);
        when(mockEntity.getLocation()).thenReturn(entityLoc);
        List<Entity> entities = Collections.singletonList(mockEntity);
        when(world.getNearbyEntities(any(BoundingBox.class))).thenReturn(entities);

        // Complete the challenge
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        // Verify the entity was removed
        verify(mockEntity).remove();
    }

    @Test
    void testIslandChallengeWithRemoveEntitiesFalse() {
        // Setup island challenge with removeEntities=false
        challenge.setChallengeType(ChallengeType.ISLAND_TYPE);
        IslandRequirements req = new IslandRequirements();
        req.setSearchRadius(10);
        req.setRequiredEntities(Collections.singletonMap(EntityType.GHAST, 1));
        req.setRemoveEntities(false);
        challenge.setRequirements(req);

        // Mock an entity in the world
        Entity mockEntity = mock(Entity.class);
        when(mockEntity.getType()).thenReturn(EntityType.GHAST);
        Location entityLoc = mock(Location.class);
        when(mockEntity.getLocation()).thenReturn(entityLoc);
        List<Entity> entities = Collections.singletonList(mockEntity);
        when(world.getNearbyEntities(any(BoundingBox.class))).thenReturn(entities);

        // Complete the challenge
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        // Verify the entity was NOT removed
        verify(mockEntity, never()).remove();
    }

    @Test
    void testOtherChallengeMoneyWithdrawn() {
        // Setup OTHER challenge with money requirement and takeMoney=true
        OtherRequirements req = new OtherRequirements();
        req.setRequiredMoney(50.0);
        req.setTakeMoney(true);
        setupOtherChallenge(req);

        when(addon.isLevelProvided()).thenReturn(false);
        VaultHook vault = mockEconomy(true, 100.0);
        when(plugin.getHooks()).thenReturn(mock(world.bentobox.bentobox.managers.HooksManager.class));
        when(plugin.getHooks().getHook("PlaceholderAPI")).thenReturn(Optional.empty());

        // Complete the challenge
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        // Verify money was withdrawn
        verify(vault).withdraw(user, 50.0);
    }

    @Test
    void testOtherChallengeMoneyNotWithdrawn() {
        // Setup OTHER challenge with money but takeMoney=false
        OtherRequirements req = new OtherRequirements();
        req.setRequiredMoney(50.0);
        req.setTakeMoney(false);
        setupOtherChallenge(req);

        when(addon.isLevelProvided()).thenReturn(false);
        VaultHook vault = mockEconomy(true, 100.0);
        when(plugin.getHooks()).thenReturn(mock(world.bentobox.bentobox.managers.HooksManager.class));
        when(plugin.getHooks().getHook("PlaceholderAPI")).thenReturn(Optional.empty());

        // Complete the challenge
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        // Verify money was NOT withdrawn
        verify(vault, never()).withdraw(any(), any(double.class));
    }

    @Test
    void testOtherChallengeExperienceWithdrawn() {
        // Setup OTHER challenge with XP requirement and takeExperience=true
        OtherRequirements req = new OtherRequirements();
        req.setRequiredExperience(50);
        req.setTakeExperience(true);
        setupOtherChallenge(req);

        when(addon.isLevelProvided()).thenReturn(false);
        when(player.getTotalExperience()).thenReturn(100);
        when(plugin.getHooks()).thenReturn(mock(world.bentobox.bentobox.managers.HooksManager.class));
        when(plugin.getHooks().getHook("PlaceholderAPI")).thenReturn(Optional.empty());

        // Complete the challenge
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        // Verify XP was taken
        verify(player).setTotalExperience(50);
    }

    @Test
    void testOtherChallengeExperienceNotWithdrawn() {
        // Setup OTHER challenge with XP but takeExperience=false
        OtherRequirements req = new OtherRequirements();
        req.setRequiredExperience(50);
        req.setTakeExperience(false);
        setupOtherChallenge(req);

        when(addon.isLevelProvided()).thenReturn(false);
        when(player.getTotalExperience()).thenReturn(100);
        when(plugin.getHooks()).thenReturn(mock(world.bentobox.bentobox.managers.HooksManager.class));
        when(plugin.getHooks().getHook("PlaceholderAPI")).thenReturn(Optional.empty());

        // Complete the challenge
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        // Verify XP was NOT taken
        verify(player, never()).setTotalExperience(any(int.class));
    }

    @Test
    void testInventoryChallengeFailureDoesNotRemoveItems() {
        // Setup inventory challenge with an item we don't have
        InventoryRequirements req = new InventoryRequirements();
        ItemStack requiredItem = new ItemStack(Material.EMERALD_BLOCK, 10);
        req.setRequiredItems(Collections.singletonList(requiredItem));
        req.setTakeItems(true);
        challenge.setRequirements(req);

        // Mock inventory with only 5 items (not enough)
        ItemStack inventoryItem = new ItemStack(Material.EMERALD_BLOCK, 5);
        when(inv.getContents()).thenReturn(new ItemStack[]{inventoryItem});
        when(player.getInventory()).thenReturn(inv);

        // Try to complete (should fail)
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        // Verify no items were removed (amount should still be 5)
        assertEquals(5, inventoryItem.getAmount(), "Items should not be removed on failed completion");
    }

    @Test
    void testIslandChallengeBlockRemovalFailureDoesNotRemoveBlocks() {
        // Setup island challenge with a block we don't have
        challenge.setChallengeType(ChallengeType.ISLAND_TYPE);
        IslandRequirements req = new IslandRequirements();
        req.setSearchRadius(10);
        req.setRequiredBlocks(Collections.singletonMap(Material.DIAMOND_BLOCK, 1));
        req.setRemoveBlocks(true);
        challenge.setRequirements(req);

        // Mock world with no diamond blocks
        Block mockBlock = mock(Block.class);
        when(mockBlock.getType()).thenReturn(Material.STONE);
        when(world.getBlockAt(anyInt(), anyInt(), anyInt())).thenReturn(mockBlock);

        // Try to complete (should fail)
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        // Verify no blocks were modified
        verify(mockBlock, never()).setType(any());
    }

    @Test
    void testIslandChallengeEntityRemovalFailureDoesNotRemoveEntities() {
        // Setup island challenge with an entity we don't have
        challenge.setChallengeType(ChallengeType.ISLAND_TYPE);
        IslandRequirements req = new IslandRequirements();
        req.setSearchRadius(10);
        req.setRequiredEntities(Collections.singletonMap(EntityType.WITHER, 1));
        req.setRemoveEntities(true);
        challenge.setRequirements(req);

        // Mock world with wrong entity type
        Entity mockEntity = mock(Entity.class);
        when(mockEntity.getType()).thenReturn(EntityType.CHICKEN);
        Location entityLoc = mock(Location.class);
        when(mockEntity.getLocation()).thenReturn(entityLoc);
        List<Entity> entities = Collections.singletonList(mockEntity);
        when(world.getNearbyEntities(any(BoundingBox.class))).thenReturn(entities);

        // Try to complete (should fail)
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        // Verify entity was not removed
        verify(mockEntity, never()).remove();
    }

    @Test
    void testOtherChallengeFailureDoesNotWithdrawMoney() {
        // Setup OTHER challenge with money we don't have
        OtherRequirements req = new OtherRequirements();
        req.setRequiredMoney(100.0);
        req.setTakeMoney(true);
        setupOtherChallenge(req);

        when(addon.isLevelProvided()).thenReturn(false);
        VaultHook vault = mockEconomy(false, 50.0); // Only 50, need 100

        // Try to complete (should fail)
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        // Verify money was NOT withdrawn
        verify(vault, never()).withdraw(any(), any(double.class));
    }

    @Test
    void testOtherChallengeFailureDoesNotWithdrawExperience() {
        // Setup OTHER challenge with XP we don't have
        OtherRequirements req = new OtherRequirements();
        req.setRequiredExperience(100);
        req.setTakeExperience(true);
        setupOtherChallenge(req);

        when(addon.isLevelProvided()).thenReturn(false);
        when(player.getTotalExperience()).thenReturn(50); // Only 50, need 100

        // Try to complete (should fail)
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        // Verify XP was NOT taken
        verify(player, never()).setTotalExperience(any(int.class));
    }

    @Test
    void testMultipleInventoryItemsPartialRemovalFailure() {
        // Test that if ANY item removal fails, the entire challenge fails and items are returned
        InventoryRequirements req = new InventoryRequirements();
        ItemStack item1 = new ItemStack(Material.EMERALD, 2);
        ItemStack item2 = new ItemStack(Material.DIAMOND, 3);
        req.setRequiredItems(Arrays.asList(item1, item2));
        req.setTakeItems(true);
        challenge.setRequirements(req);

        // Mock inventory with only first item (missing diamonds)
        ItemStack invEmerald = new ItemStack(Material.EMERALD, 10);
        when(inv.getContents()).thenReturn(new ItemStack[]{invEmerald});
        when(player.getInventory()).thenReturn(inv);

        // Try to complete (should fail due to missing diamonds)
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));

        // Verify items were not removed
        assertEquals(10, invEmerald.getAmount(), "Items should not be removed when requirement fails");
    }
}
