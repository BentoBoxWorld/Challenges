package world.bentobox.challenges.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.managers.ChallengesManager;
import world.bentobox.challenges.config.Settings;
import world.bentobox.challenges.config.SettingsUtils.VisibilityMode;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.tasks.TryToComplete;
import world.bentobox.challenges.utils.Utils;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class, ChatColor.class, Utils.class, TryToComplete.class, Util.class})
public class CompleteChallengeCommandTest {

    @Mock
    private CompositeCommand ic;
    @Mock
    private User user;
    @Mock
    private IslandsManager im;
    @Mock
    private Island island;
    @Mock
    private ChallengesAddon addon;

    private CompleteChallengeCommand cc;
    @Mock
    private World world;
    @Mock
    private ChallengesManager chm;
    @Mock
    private IslandWorldManager iwm;
    @Mock
    private GameModeAddon gameModeAddon;

    @Mock
    private Challenge challenge;

    /**
     */
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        // Set up plugin
        BentoBox plugin = mock(BentoBox.class);
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);
        User.setPlugin(plugin);

        // Command manager
        CommandsManager cm = mock(CommandsManager.class);
        when(plugin.getCommandsManager()).thenReturn(cm);
        // Addon
        when(ic.getAddon()).thenReturn(addon);
        when(ic.getPermissionPrefix()).thenReturn("bskyblock.");
        when(ic.getLabel()).thenReturn("island");
        when(ic.getTopLabel()).thenReturn("island");
        when(ic.getWorld()).thenReturn(world);
        when(ic.getTopLabel()).thenReturn("bsb");

        // IWM friendly name
        when(iwm.getFriendlyName(any())).thenReturn("BSkyBlock");
        when(iwm.inWorld(any(World.class))).thenReturn(true);
        Optional<GameModeAddon> optionalAddon = Optional.of(gameModeAddon);
        when(iwm.getAddon(any())).thenReturn(optionalAddon);
        when(plugin.getIWM()).thenReturn(iwm);

        // Game Mode Addon
        @NonNull
        Optional<CompositeCommand> optionalAdmin = Optional.of(ic);
        when(gameModeAddon.getAdminCommand()).thenReturn(optionalAdmin);

        // World
        when(world.toString()).thenReturn("world");

        // Player
        Player p = mock(Player.class);
        // Sometimes use Mockito.withSettings().verboseLogging()
        when(user.isOp()).thenReturn(false);
        UUID uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(p);
        when(user.getName()).thenReturn("tastybento");
        when(user.getPermissionValue(anyString(), anyInt())).thenReturn(-1);
        when(user.isPlayer()).thenReturn(true);

        // Mock item factory (for itemstacks)
        PowerMockito.mockStatic(Bukkit.class);
        ItemFactory itemFactory = mock(ItemFactory.class);
        when(Bukkit.getItemFactory()).thenReturn(itemFactory);
        ItemMeta itemMeta = mock(ItemMeta.class);
        when(itemFactory.getItemMeta(any())).thenReturn(itemMeta);

        // Addon & Challenge Manager
        when(addon.getChallengesManager()).thenReturn(chm);
        when(chm.getAllChallengeLevelStatus(any(), any())).thenReturn(Collections.emptyList());
        // Challenges exist
        when(chm.hasAnyChallengeData(any(World.class))).thenReturn(true);
        // Challenges
        when(chm.getChallenge(anyString())).thenReturn(challenge);
        List<String> nameList = Arrays.asList("world_maker", "world_placer", "bad_challenge_name", "world_breaker");
        when(chm.getAllChallengesNames(any())).thenReturn(nameList);


        // ChatColor
        PowerMockito.mockStatic(ChatColor.class);
        when(ChatColor.translateAlternateColorCodes(any(char.class), anyString())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(1, String.class));

        // Settings
        Settings settings = new Settings();
        when(addon.getChallengesSettings()).thenReturn(settings);
        settings.setVisibilityMode(VisibilityMode.VISIBLE);

        // Island
        when(plugin.getIslands()).thenReturn(im);
        when(im.getIsland(any(), any(User.class))).thenReturn(island);

        // Utils
        PowerMockito.mockStatic(Utils.class);
        when(Utils.getGameMode(any())).thenReturn("world");

        // Try to complete
        PowerMockito.mockStatic(TryToComplete.class);
        // All challenges are successful!
        when(TryToComplete.complete(any(), any(), any(), any(), anyString(), anyString(), anyInt())).thenReturn(true);

        // Util
        PowerMockito.mockStatic(Util.class);
        when(Util.tabLimit(any(), any())).thenAnswer((Answer<List<String>>) invocation -> (List<String>)invocation.getArgument(0, List.class));

        // Command under test
        cc = new CompleteChallengeCommand(addon, ic);
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.CompleteChallengeCommand#CompleteChallengeCommand(world.bentobox.bentobox.api.addons.Addon, world.bentobox.bentobox.api.commands.CompositeCommand)}.
     */
    @Test
    public void testCompleteChallengeCommand() {
        assertEquals("complete", cc.getLabel());
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.CompleteChallengeCommand#setup()}.
     */
    @Test
    public void testSetup() {
        assertEquals("bskyblock.challenges", cc.getPermission());
        assertEquals("challenges.commands.user.complete.parameters", cc.getParameters());
        assertEquals("challenges.commands.user.complete.description", cc.getDescription());
        assertTrue(cc.isOnlyPlayer());
        // No sub commands
        assertEquals(0, cc.getSubCommands(true).size());
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.CompleteChallengeCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringNoArgs() {
        assertFalse(cc.execute(user, "complete", Collections.emptyList()));
        verify(user).getTranslation(eq("challenges.errors.no-name"));
        verify(user).sendMessage(eq("commands.help.header"), eq(TextVariables.LABEL), eq("BSkyBlock"));
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.CompleteChallengeCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringUnknownChallenge() {
        when(chm.getChallenge(anyString())).thenReturn(null);
        assertFalse(cc.execute(user, "complete", Collections.singletonList("mychal")));
        verify(user).getTranslation(eq("challenges.errors.unknown-challenge"));
        verify(user).sendMessage(eq("commands.help.header"), eq(TextVariables.LABEL), eq("BSkyBlock"));
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.CompleteChallengeCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringKnownChallengeFail() {
        when(TryToComplete.complete(any(), any(), any(), any(), anyString(), anyString(), anyInt())).thenReturn(false);
        assertFalse(cc.execute(user, "complete", Collections.singletonList("mychal")));
        verify(user, never()).sendMessage(any());
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.CompleteChallengeCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringKnownChallengeSuccess() {
        assertTrue(cc.execute(user, "complete", Collections.singletonList("mychal")));
        verify(user, never()).sendMessage(any());
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.CompleteChallengeCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringKnownChallengeSuccessMultipleTimesNoPerm() {
        assertTrue(cc.execute(user, "complete", Arrays.asList("mychal", "5")));
        verify(user).getTranslation(eq("challenges.error.no-multiple-permission"));
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.CompleteChallengeCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringKnownChallengeSuccessMultipleTimesHasPerm() {
        when(user.hasPermission(anyString())).thenReturn(true);
        assertTrue(cc.execute(user, "complete", Arrays.asList("mychal", "5")));
        verify(user, never()).sendMessage(any());
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.CompleteChallengeCommand#tabComplete(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testTabCompleteUserStringListOfStringNoArgs() {
        cc.tabComplete(user, "complete", Collections.emptyList());
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.CompleteChallengeCommand#tabComplete(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testTabCompleteUserStringListOfStringOneArg() {
        List<String> list = cc.tabComplete(user, "complete", Collections.singletonList("arg")).get();
        assertFalse(list.isEmpty());
        assertEquals("help", list.get(0));
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.CompleteChallengeCommand#tabComplete(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testTabCompleteUserStringListOfStringTwoArgs() {
        List<String> list = cc.tabComplete(user, "complete", Arrays.asList("arg1", "arg2")).get();
        assertFalse(list.isEmpty());
        assertEquals("help", list.get(0));
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.CompleteChallengeCommand#tabComplete(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testTabCompleteUserStringListOfStringThreeArgs() {
        List<String> list = cc.tabComplete(user, "complete", Arrays.asList("arg1", "arg2", "arg3")).get();
        assertFalse(list.isEmpty());
        assertEquals("maker", list.get(0));
        assertEquals("placer", list.get(1));
        assertEquals("breaker", list.get(2));
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.CompleteChallengeCommand#tabComplete(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testTabCompleteUserStringListOfStringFourArgs() {
        List<String> list = cc.tabComplete(user, "complete", Arrays.asList("arg1", "arg2", "arg3", "arg4")).get();
        assertTrue(list.isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.CompleteChallengeCommand#tabComplete(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testTabCompleteUserStringListOfStringFourArgsNumber() {
        List<String> list = cc.tabComplete(user, "complete", Arrays.asList("arg1", "arg2", "arg3", "4")).get();
        assertFalse(list.isEmpty());
        assertEquals("<number>", list.get(0));
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.CompleteChallengeCommand#tabComplete(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testTabCompleteUserStringListOfStringFiveArgs() {
        List<String> list = cc.tabComplete(user, "complete", Arrays.asList("arg1", "arg2", "arg23", "arg4", "arg5")).get();
        assertFalse(list.isEmpty());
        assertEquals("help", list.get(0));
    }

}
