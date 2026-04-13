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

import java.util.Arrays;
import java.util.Collections;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
import world.bentobox.level.Level;

/**
 * @author tastybento
 */
public class TryToCompleteTest extends AbstractChallengesTest {

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
    public void setUp() {
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
    public void tearDown() throws Exception {
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
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(true);
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

    // -------------------------------------------------------------------------
    // OTHER_TYPE tests
    // -------------------------------------------------------------------------

    @Test
    public void testOtherTypeSuccessNoRequirements() {
        OtherRequirements req = new OtherRequirements();
        setupOtherChallenge(req);
        // Stub PAPI hook absent so the check is skipped
        when(plugin.getHooks()).thenReturn(mock(world.bentobox.bentobox.managers.HooksManager.class));
        when(plugin.getHooks().getHook("PlaceholderAPI")).thenReturn(Optional.empty());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
    }

    @Test
    public void testOtherTypeFailIslandLevelNoAddon() {
        OtherRequirements req = new OtherRequirements();
        req.setRequiredIslandLevel(100);
        setupOtherChallenge(req);
        when(addon.isLevelProvided()).thenReturn(false);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.missing-addon"));
    }

    @Test
    public void testOtherTypeFailMoneyNoEconomy() {
        OtherRequirements req = new OtherRequirements();
        req.setRequiredMoney(100.0);
        setupOtherChallenge(req);
        when(addon.isEconomyProvided()).thenReturn(false);
        when(addon.isLevelProvided()).thenReturn(false);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.missing-addon"));
    }

    @Test
    public void testOtherTypeFailMoneyInsufficient() {
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
    public void testOtherTypeSuccessMoney() {
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
    public void testOtherTypeFailExperienceInsufficient() {
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
    public void testOtherTypeSuccessExperienceCreative() {
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
    public void testOtherTypeFailIslandLevelInsufficient() {
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
    public void testOtherTypeSuccessIslandLevel() {
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
    public void testOtherTypeFulfillTakesMoney() {
        OtherRequirements req = new OtherRequirements();
        req.setRequiredMoney(50.0);
        req.setTakeMoney(true);
        setupOtherChallenge(req);
        when(addon.isLevelProvided()).thenReturn(false);
        VaultHook vault = mockEconomy(true, 100.0);
        when(plugin.getHooks()).thenReturn(mock(world.bentobox.bentobox.managers.HooksManager.class));
        when(plugin.getHooks().getHook("PlaceholderAPI")).thenReturn(Optional.empty());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(vault).withdraw(eq(user), eq(50.0));
    }

    @Test
    public void testOtherTypeFulfillTakesExperience() {
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
    public void testOtherTypeFulfillCreativeSkipsXpTake() {
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
    public void testStatisticTypeEmptyRequirements() {
        setupStatisticChallenge(Collections.emptyList());
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
    }

    @Test
    public void testStatisticTypeSuccessUntyped() {
        StatisticRec rec = new StatisticRec(Statistic.JUMP, null, null, 10, false);
        setupStatisticChallenge(List.of(rec));
        when(cm.getStatisticData(any(), any(), eq(Statistic.JUMP))).thenReturn(15);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
    }

    @Test
    public void testStatisticTypeFailUntypedInsufficient() {
        StatisticRec rec = new StatisticRec(Statistic.JUMP, null, null, 10, false);
        setupStatisticChallenge(List.of(rec));
        when(cm.getStatisticData(any(), any(), eq(Statistic.JUMP))).thenReturn(5);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.requirement-not-met"),
                eq("[number]"), eq("10"), eq("[statistic]"), anyString(), eq("[value]"), eq("5"));
    }

    @Test
    public void testStatisticTypeSuccessItemType() {
        StatisticRec rec = new StatisticRec(Statistic.MINE_BLOCK, null, Material.STONE, 20, false);
        setupStatisticChallenge(List.of(rec));
        when(cm.getStatisticData(any(), any(), eq(Statistic.MINE_BLOCK), eq(Material.STONE))).thenReturn(25);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
    }

    @Test
    public void testStatisticTypeFailItemInsufficient() {
        StatisticRec rec = new StatisticRec(Statistic.MINE_BLOCK, null, Material.STONE, 20, false);
        setupStatisticChallenge(List.of(rec));
        when(cm.getStatisticData(any(), any(), eq(Statistic.MINE_BLOCK), eq(Material.STONE))).thenReturn(5);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.requirement-not-met-material"),
                eq("[number]"), eq("20"), eq("[statistic]"), anyString(), eq("[material]"), anyString(),
                eq("[value]"), eq("5"));
    }

    @Test
    public void testStatisticTypeSuccessEntityType() {
        StatisticRec rec = new StatisticRec(Statistic.KILL_ENTITY, EntityType.ZOMBIE, null, 5, false);
        setupStatisticChallenge(List.of(rec));
        when(cm.getStatisticData(any(), any(), eq(Statistic.KILL_ENTITY), eq(EntityType.ZOMBIE))).thenReturn(10);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
    }

    @Test
    public void testStatisticTypeFailEntityInsufficient() {
        StatisticRec rec = new StatisticRec(Statistic.KILL_ENTITY, EntityType.ZOMBIE, null, 5, false);
        setupStatisticChallenge(List.of(rec));
        when(cm.getStatisticData(any(), any(), eq(Statistic.KILL_ENTITY), eq(EntityType.ZOMBIE))).thenReturn(2);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.requirement-not-met-entity"),
                eq("[number]"), eq("5"), eq("[statistic]"), anyString(), eq("[entity]"), anyString(),
                eq("[value]"), eq("2"));
    }

    @Test
    public void testStatisticTypeMultipleMixed() {
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
    public void testTimeoutBreached() {
        challenge.setTimeout(60000);
        when(cm.isBreachingTimeOut(any(), any(), any())).thenReturn(true);
        when(cm.getLastCompletionDate(any(), any(), any())).thenReturn(System.currentTimeMillis() - 10000);
        assertFalse(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(user).getTranslation(any(World.class), eq("challenges.errors.timeout"),
                eq("[timeout]"), anyString(), eq("[wait-time]"), anyString());
    }

    @Test
    public void testTimeoutNotBreached() {
        challenge.setTimeout(60000);
        when(cm.isBreachingTimeOut(any(), any(), any())).thenReturn(false);
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
    }

    // -------------------------------------------------------------------------
    // Reward distribution tests
    // -------------------------------------------------------------------------

    @Test
    public void testFirstTimeRewardMoney() {
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(false);
        challenge.setRewardMoney(100.0);
        VaultHook vault = mockEconomy(true, 1000.0);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(vault).deposit(eq(user), eq(100.0));
    }

    @Test
    public void testFirstTimeRewardExperience() {
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(false);
        challenge.setRewardExperience(50);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(player).giveExp(50);
    }

    @Test
    public void testRepeatRewardMoney() {
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(true);
        challenge.setRepeatMoneyReward(25.0);
        VaultHook vault = mockEconomy(true, 1000.0);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(vault).deposit(eq(user), eq(25.0));
    }

    @Test
    public void testRepeatRewardExperience() {
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(true);
        challenge.setRepeatExperienceReward(10);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(player).giveExp(10);
    }

    @Test
    public void testMultipleTimesFirstCompletion() {
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
    public void testLevelCompletionTriggered() {
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(false);
        // Non-free challenge with a level
        challenge.setLevel(GAME_MODE_NAME + "_novice");
        ChallengeLevel lvl = new ChallengeLevel();
        lvl.setUniqueId(GAME_MODE_NAME + "_novice");
        lvl.setFriendlyName("Novice");
        lvl.setRewardExperience(200);
        // Stub both overloads: getLevel(String) used in checkIfCanCompleteChallenge,
        // getLevel(Challenge) used in build() for level completion check
        when(cm.getLevel(eq(GAME_MODE_NAME + "_novice"))).thenReturn(lvl);
        when(cm.getLevel(any(Challenge.class))).thenReturn(lvl);
        when(cm.isLevelCompleted(any(), any(), any())).thenReturn(false);
        when(cm.validateLevelCompletion(any(), any(), any())).thenReturn(true);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(cm).setLevelComplete(any(), any(), eq(lvl));
        verify(player).giveExp(200);
    }

    @Test
    public void testLevelCompletionAlreadyDone() {
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(false);
        challenge.setLevel(GAME_MODE_NAME + "_novice");
        ChallengeLevel lvl = new ChallengeLevel();
        lvl.setUniqueId(GAME_MODE_NAME + "_novice");
        lvl.setFriendlyName("Novice");
        when(cm.getLevel(eq(GAME_MODE_NAME + "_novice"))).thenReturn(lvl);
        when(cm.getLevel(any(Challenge.class))).thenReturn(lvl);
        when(cm.isLevelCompleted(any(), any(), any())).thenReturn(true);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(cm, never()).setLevelComplete(any(), any(), any());
    }

    @Test
    public void testFreeChallengeNoLevelCheck() {
        when(cm.isChallengeComplete(any(world.bentobox.bentobox.api.user.User.class), any(), any())).thenReturn(false);
        challenge.setLevel(ChallengesManager.FREE);
        when(inv.addItem(any())).thenReturn(new HashMap<>());
        assertTrue(TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix));
        verify(cm, never()).getLevel(any(Challenge.class));
    }
}
