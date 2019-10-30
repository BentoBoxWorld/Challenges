package world.bentobox.challenges.panel.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.managers.BlueprintsManager;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.ChallengesManager;
import world.bentobox.challenges.config.Settings;
import world.bentobox.challenges.config.SettingsUtils.VisibilityMode;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.Challenge.ChallengeType;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.utils.LevelStatus;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class, ChatColor.class})
public class ChallengesGUITest {

    @Mock
    private User user;
    @Mock
    private IslandsManager im;
    @Mock
    private IslandWorldManager iwm;
    @Mock
    private BentoBox plugin;
    @Mock
    private Settings settings;
    @Mock
    private CompositeCommand ic;
    @Mock
    private BlueprintsManager bpm;
    @Mock
    private Inventory inv;
    @Mock
    private ItemMeta meta;
    @Mock
    private ChallengesAddon addon;
    @Mock
    private World world;

    private ChallengesGUI cg;

    @Mock
    private ChallengesManager chm;
    private UUID uuid;
    @Mock
    private Challenge challenge1;
    @Mock
    private Challenge challenge2;
    @Mock
    private Challenge challenge3;
    @Mock
    private Challenge challenge4;
    @Mock
    private LevelStatus levelStatus;

    private List<Challenge> freeChalls = new ArrayList<>();
    private List<Challenge> levelChalls = new ArrayList<>();

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // Set up plugin
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);

        PowerMockito.mockStatic(Bukkit.class);
        // Item Factory (needed for ItemStack)
        ItemFactory itemF = mock(ItemFactory.class);
        when(itemF.getItemMeta(Mockito.any())).thenReturn(meta);
        when(Bukkit.getItemFactory()).thenReturn(itemF);
        // Inventory
        when(Bukkit.createInventory(eq(null), anyInt(), anyString())).thenReturn(inv);

        // Addon
        when(addon.getChallengesManager()).thenReturn(chm);
        // Levels
        when(levelStatus.isUnlocked()).thenReturn(true);
        ChallengeLevel level = mock(ChallengeLevel.class);
        when(level.getFriendlyName()).thenReturn("Novice");
        when(level.getUniqueId()).thenReturn("novice");
        when(level.getIcon()).thenReturn(new ItemStack(Material.BIRCH_BOAT));
        when(level.getLockedIcon()).thenReturn(new ItemStack(Material.DARK_OAK_BOAT));
        when(levelStatus.getLevel()).thenReturn(level);
        List<LevelStatus> levels = Collections.singletonList(levelStatus);
        when(chm.getAllChallengeLevelStatus(any(), any())).thenReturn(levels);
        when(chm.getChallengeLevelStatus(any(), any(), any())).thenReturn(levelStatus);
        // Challenges exist
        when(chm.hasAnyChallengeData(any(World.class))).thenReturn(true);

        // Free challenges - have more than 18 so that the special processing kicks in
        when(chm.getFreeChallenges(any())).thenReturn(freeChalls);
        when(challenge1.isDeployed()).thenReturn(true);
        when(challenge2.isDeployed()).thenReturn(true);
        // 1 is repeatable, 2 is not
        when(challenge1.isRepeatable()).thenReturn(true);

        // Level challenges
        when(chm.getLevelChallenges(any())).thenReturn(levelChalls);
        // ChatColor
        PowerMockito.mockStatic(ChatColor.class);
        when(ChatColor.translateAlternateColorCodes(any(char.class), anyString())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(1, String.class));
        // Settings
        when(addon.getChallengesSettings()).thenReturn(settings);
        when(settings.getVisibilityMode()).thenReturn(VisibilityMode.VISIBLE);
        when(settings.isFreeChallengesFirst()).thenReturn(false);
        when(settings.isRemoveCompleteOneTimeChallenges()).thenReturn(false);

        // Player
        Player p = mock(Player.class);
        when(user.isOp()).thenReturn(false);
        uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(p);
        when(user.getName()).thenReturn("tastybento");
        when(user.getPermissionValue(anyString(), anyInt())).thenReturn(-1);
        when(user.isPlayer()).thenReturn(true);
        when(user.getTranslation(anyString())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(0, String.class));

        cg = new ChallengesGUI(addon, world, user, "island", "bskyblock.");
    }

    private void addLevelChallenges(int number) {
        for (int i = 0; i < number; i++) {
            Challenge c = mock(Challenge.class);
            when(c.isRepeatable()).thenReturn(true);
            when(c.getUniqueId()).thenReturn(String.valueOf(i) + "unique");
            when(c.getIcon()).thenReturn(new ItemStack(Material.EMERALD));
            when(c.getFriendlyName()).thenReturn(String.valueOf(i) + "name");
            when(c.getChallengeType()).thenReturn(ChallengeType.INVENTORY);
            when(c.getDescription()).thenReturn(Collections.singletonList("Description"));
            when(c.getEnvironment()).thenReturn(Collections.singleton(Environment.NORMAL));
            when(c.getLevel()).thenReturn("Novice");
            when(c.getRewardItems()).thenReturn(Collections.emptyList());
            when(c.isDeployed()).thenReturn(true);
            levelChalls.add(c);
        }

    }

    private void addFreeChallenges(int number) {
        for (int i = 0; i < number; i++) {
            Challenge c = mock(Challenge.class);
            when(c.getUniqueId()).thenReturn(String.valueOf(i) + "unique");
            when(c.getIcon()).thenReturn(new ItemStack(Material.DIAMOND));
            when(c.getFriendlyName()).thenReturn(String.valueOf(i) + "name");
            when(c.getChallengeType()).thenReturn(ChallengeType.INVENTORY);
            when(c.getDescription()).thenReturn(Collections.singletonList("Description"));
            when(c.getEnvironment()).thenReturn(Collections.singleton(Environment.NORMAL));
            when(c.getLevel()).thenReturn("Novice");
            when(c.getRewardItems()).thenReturn(Collections.emptyList());
            when(c.isDeployed()).thenReturn(true);
            when(c.isRepeatable()).thenReturn(true);
            freeChalls.add(c);
        }

    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link world.bentobox.challenges.panel.user.ChallengesGUI#build()}.
     */
    @Test
    public void testBuildNoChallenges() {
        when(chm.hasAnyChallengeData(any(World.class))).thenReturn(false);
        cg.build();
        verify(addon).logError("There are no challenges set up!");
        verify(user).sendMessage("challenges.errors.no-challenges");
    }


    /**
     * Test method for {@link world.bentobox.challenges.panel.user.ChallengesGUI#build()}.
     */
    @Test
    public void testBuild0Free0LevelChallenge() {
        when(settings.isFreeChallengesFirst()).thenReturn(true);
        cg.build();
        verify(user).getTranslation("challenges.gui.title.challenges");
        ArgumentCaptor<Integer> argument = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<ItemStack> argument2 = ArgumentCaptor.forClass(ItemStack.class);
        verify(inv).setItem(argument.capture(), argument2.capture());
        // Level
        assertTrue(argument.getAllValues().get(0) == 0);
        assertEquals(Material.BIRCH_BOAT, argument2.getAllValues().get(0).getType());
    }

    /**
     * Test method for {@link world.bentobox.challenges.panel.user.ChallengesGUI#build()}.
     */
    @Test
    public void testBuild10Free10LevelChallenge() {
        addFreeChallenges(10);
        addLevelChallenges(10);
        when(settings.isFreeChallengesFirst()).thenReturn(true);
        cg.build();
        verify(user).getTranslation("challenges.gui.title.challenges");
        ArgumentCaptor<Integer> argument = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<ItemStack> argument2 = ArgumentCaptor.forClass(ItemStack.class);
        verify(inv, times(21)).setItem(argument.capture(), argument2.capture());
        List<ItemStack> values = argument2.getAllValues();
        // Free challenges
        for (int i = 0; i < 10; i++) {
            assertEquals("Failed on " + i, Material.DIAMOND, values.get(i).getType());
        }
        // Level challenges
        for (int i = 11; i < 20; i++) {
            assertEquals("Failed on " + i, Material.EMERALD, values.get(i).getType());
        }
        // Level icons
        assertTrue(argument.getAllValues().get(20) == 36);
        assertEquals(Material.BIRCH_BOAT, argument2.getAllValues().get(20).getType());

    }
    /**
     * Test method for {@link world.bentobox.challenges.panel.user.ChallengesGUI#build()}.
     */
    @Test
    public void testBuild20Free20LevelChallenge() {
        addFreeChallenges(20);
        addLevelChallenges(20);
        when(settings.isFreeChallengesFirst()).thenReturn(true);
        cg.build();
        verify(user).getTranslation("challenges.gui.title.challenges");
        ArgumentCaptor<Integer> argument = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<ItemStack> argument2 = ArgumentCaptor.forClass(ItemStack.class);
        verify(inv, times(38)).setItem(argument.capture(), argument2.capture());
        List<ItemStack> values = argument2.getAllValues();
        // Free challenges
        for (int i = 0; i < 18; i++) {
            assertEquals("Failed on " + i, Material.DIAMOND, values.get(i).getType());
        }
        // Next page
        assertTrue(argument.getAllValues().get(18) == 18);
        assertEquals(Material.OAK_SIGN, argument2.getAllValues().get(18).getType());
        // Level challenges
        for (int i = 19; i < 37; i++) {
            assertEquals("Failed on " + i, Material.EMERALD, values.get(i).getType());
        }
        // Next page
        assertTrue(argument.getAllValues().get(37) == 45);
        assertEquals(Material.OAK_SIGN, argument2.getAllValues().get(37).getType());
    }
    /**
     * Test method for {@link world.bentobox.challenges.panel.user.ChallengesGUI#build()}.
     */
    @Test
    public void testBuildFreeChallenges10Free20LevelChallenge() {
        addFreeChallenges(10);
        addLevelChallenges(20);
        when(settings.isFreeChallengesFirst()).thenReturn(true);
        cg.build();
        verify(user).getTranslation("challenges.gui.title.challenges");
        ArgumentCaptor<Integer> argument = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<ItemStack> argument2 = ArgumentCaptor.forClass(ItemStack.class);
        verify(inv, times(30)).setItem(argument.capture(), argument2.capture());
        List<ItemStack> values = argument2.getAllValues();
        // Free challenges
        for (int i = 0; i < 10; i++) {
            assertEquals("Failed on " + i, Material.DIAMOND, values.get(i).getType());
        }
        // Level challenges
        for (int i = 10; i < 27; i++) {
            assertEquals("Failed on " + i, Material.EMERALD, values.get(i).getType());
        }
        // Next page
        assertTrue(argument.getAllValues().get(28) == 36);
        assertEquals(Material.OAK_SIGN, argument2.getAllValues().get(28).getType());
        // Level
        assertTrue(argument.getAllValues().get(29) == 45);
        assertEquals(Material.BIRCH_BOAT, argument2.getAllValues().get(29).getType());
    }

    /**
     * Test method for {@link world.bentobox.challenges.panel.user.ChallengesGUI#build()}.
     */
    @Test
    public void testBuildFreeChallenges20Free10LevelChallenge() {
        addFreeChallenges(20);
        addLevelChallenges(10);
        when(settings.isFreeChallengesFirst()).thenReturn(true);
        cg.build();
        verify(user).getTranslation("challenges.gui.title.challenges");
        ArgumentCaptor<Integer> argument = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<ItemStack> argument2 = ArgumentCaptor.forClass(ItemStack.class);
        verify(inv, times(30)).setItem(argument.capture(), argument2.capture());

        List<ItemStack> values = argument2.getAllValues();
        // Free challenges
        for (int i = 0; i < 18; i++) {
            assertEquals("Failed on " + i, Material.DIAMOND, values.get(i).getType());
        }
        // Next page
        assertTrue(argument.getAllValues().get(18) == 18);
        assertEquals(Material.OAK_SIGN, argument2.getAllValues().get(18).getType());
        // Level challenges
        for (int i = 19; i < 29; i++) {
            assertEquals("Failed on " + i, Material.EMERALD, values.get(i).getType());
        }

        // Level
        assertTrue(argument.getAllValues().get(29) == 45);
        assertEquals(Material.BIRCH_BOAT, argument2.getAllValues().get(29).getType());
    }

    /**
     * Test method for {@link world.bentobox.challenges.panel.user.ChallengesGUI#build()}.
     */
    @Test
    public void testBuildFreeChallengesLast20Free10LevelChallenge() {
        addFreeChallenges(20);
        addLevelChallenges(10);
        when(settings.isFreeChallengesFirst()).thenReturn(false);
        cg.build();
        verify(user).getTranslation("challenges.gui.title.challenges");
        ArgumentCaptor<Integer> argument = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<ItemStack> argument2 = ArgumentCaptor.forClass(ItemStack.class);
        verify(inv, times(30)).setItem(argument.capture(), argument2.capture());
        List<ItemStack> values = argument2.getAllValues();

        // Level challenges
        for (int i = 0; i < 10; i++) {
            assertEquals("Failed on " + i, Material.EMERALD, values.get(i).getType());
        }
        // Next page
        assertTrue(argument.getAllValues().get(10) == 18);
        assertEquals(Material.BIRCH_BOAT, argument2.getAllValues().get(10).getType());
        // Free challenges
        for (int i = 11; i < 29; i++) {
            assertEquals("Failed on " + i, Material.DIAMOND, values.get(i).getType());
        }

        // Level
        assertTrue(argument.getAllValues().get(29) == 45);
        assertEquals(Material.OAK_SIGN, argument2.getAllValues().get(29).getType());
    }

}
