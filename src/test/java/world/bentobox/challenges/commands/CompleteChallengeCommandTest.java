package world.bentobox.challenges.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

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
import world.bentobox.challenges.WhiteBox;
import world.bentobox.challenges.config.Settings;
import world.bentobox.challenges.config.SettingsUtils.VisibilityMode;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.managers.ChallengesManager;
import world.bentobox.challenges.tasks.TryToComplete;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;

/**
 * @author tastybento
 *
 */
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

    private AutoCloseable closeable;
    private MockedStatic<Bukkit> mockedBukkit;
    private MockedStatic<Utils> mockedUtils;
    private MockedStatic<TryToComplete> mockedTtc;
    private MockedStatic<Util> mockedUtil;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        ServerMock server = MockBukkit.mock();
        @SuppressWarnings("unused")
        var unusedTagRef = org.bukkit.Tag.LEAVES;
        // Set up plugin
        BentoBox plugin = mock(BentoBox.class);
        WhiteBox.setInternalState(BentoBox.class, "instance", plugin);
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
        when(user.isOp()).thenReturn(false);
        UUID uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(p);
        when(user.getName()).thenReturn("tastybento");
        when(user.getPermissionValue(anyString(), anyInt())).thenReturn(-1);
        when(user.isPlayer()).thenReturn(true);

        // Bukkit static mock — delegate item factory to MockBukkit's real one.
        mockedBukkit = Mockito.mockStatic(Bukkit.class, Mockito.RETURNS_DEEP_STUBS);
        mockedBukkit.when(Bukkit::getServer).thenReturn(server);
        mockedBukkit.when(Bukkit::getItemFactory).thenReturn(server.getItemFactory());

        // Addon & Challenge Manager
        when(addon.getChallengesManager()).thenReturn(chm);
        when(chm.getAllChallengeLevelStatus(any(), any())).thenReturn(Collections.emptyList());
        when(chm.hasAnyChallengeData(any(World.class))).thenReturn(true);
        when(chm.getChallenge(anyString())).thenReturn(challenge);
        List<String> nameList = Arrays.asList("world_maker", "world_placer", "bad_challenge_name", "world_breaker");
        when(chm.getAllChallengesNames(any())).thenReturn(nameList);

        // Settings
        Settings settings = new Settings();
        when(addon.getChallengesSettings()).thenReturn(settings);
        settings.setVisibilityMode(VisibilityMode.VISIBLE);

        // Island
        when(plugin.getIslands()).thenReturn(im);
        when(im.getIsland(any(), any(User.class))).thenReturn(island);

        // Utils
        mockedUtils = Mockito.mockStatic(Utils.class);
        mockedUtils.when(() -> Utils.getGameMode(any())).thenReturn("world");

        // Try to complete
        mockedTtc = Mockito.mockStatic(TryToComplete.class);
        mockedTtc.when(() -> TryToComplete.complete(any(), any(), any(), any(), anyString(), anyString(), anyInt()))
                .thenReturn(true);

        // Util
        mockedUtil = Mockito.mockStatic(Util.class, Mockito.CALLS_REAL_METHODS);
        mockedUtil.when(() -> Util.tabLimit(any(), any()))
                .thenAnswer((Answer<List<String>>) invocation -> (List<String>) invocation.getArgument(0, List.class));
        mockedUtil.when(() -> Util.translateColorCodes(anyString()))
                .thenAnswer((Answer<String>) invocation -> invocation.getArgument(0, String.class));

        // Command under test
        cc = new CompleteChallengeCommand(addon, ic);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (mockedBukkit != null) mockedBukkit.closeOnDemand();
        if (mockedUtils != null) mockedUtils.closeOnDemand();
        if (mockedTtc != null) mockedTtc.closeOnDemand();
        if (mockedUtil != null) mockedUtil.closeOnDemand();
        if (closeable != null) closeable.close();
        MockBukkit.unmock();
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void testCompleteChallengeCommand() {
        assertEquals("complete", cc.getLabel());
    }

    @Test
    public void testSetup() {
        assertEquals("bskyblock.challenges", cc.getPermission());
        assertEquals("challenges.commands.user.complete.parameters", cc.getParameters());
        assertEquals("challenges.commands.user.complete.description", cc.getDescription());
        assertTrue(cc.isOnlyPlayer());
        assertEquals(0, cc.getSubCommands(true).size());
    }

    @Test
    public void testExecuteUserStringListOfStringNoArgs() {
        assertFalse(cc.execute(user, "complete", Collections.emptyList()));
        mockedUtils.verify(() -> Utils.sendMessage(user, world, Constants.ERRORS + "no-name"));
        verify(user).sendMessage(eq("commands.help.header"), eq(TextVariables.LABEL), eq("BSkyBlock"));
    }

    @Test
    public void testExecuteUserStringListOfStringUnknownChallenge() {
        when(chm.getChallenge(anyString())).thenReturn(null);
        assertFalse(cc.execute(user, "complete", Collections.singletonList("mychal")));
        mockedUtils.verify(() -> Utils.sendMessage(user, world, Constants.ERRORS + "unknown-challenge"));
        verify(user).sendMessage(eq("commands.help.header"), eq(TextVariables.LABEL), eq("BSkyBlock"));
    }

    @Test
    public void testExecuteUserStringListOfStringKnownChallengeFail() {
        mockedTtc.when(() -> TryToComplete.complete(any(), any(), any(), any(), anyString(), anyString(), anyInt()))
                .thenReturn(false);
        assertFalse(cc.execute(user, "complete", Collections.singletonList("mychal")));
        verify(user, never()).sendMessage(any());
    }

    @Test
    public void testExecuteUserStringListOfStringKnownChallengeSuccess() {
        assertTrue(cc.execute(user, "complete", Collections.singletonList("mychal")));
        verify(user, never()).sendMessage(any());
    }

    @Test
    public void testExecuteUserStringListOfStringKnownChallengeSuccessMultipleTimesNoPerm() {
        assertTrue(cc.execute(user, "complete", Arrays.asList("mychal", "5")));
        mockedUtils.verify(() -> Utils.sendMessage(user, world, Constants.ERRORS + "no-multiple-permission"));
    }

    @Test
    public void testExecuteUserStringListOfStringKnownChallengeSuccessMultipleTimesHasPerm() {
        when(user.hasPermission(anyString())).thenReturn(true);
        assertTrue(cc.execute(user, "complete", Arrays.asList("mychal", "5")));
        verify(user, never()).sendMessage(any());
    }

    @Test
    public void testTabCompleteUserStringListOfStringNoArgs() {
        Optional<List<String>> result = cc.tabComplete(user, "complete", Collections.emptyList());
        assertTrue(result.isEmpty() || result.get().isEmpty());
    }

    @Test
    public void testTabCompleteUserStringListOfStringOneArg() {
        List<String> list = cc.tabComplete(user, "complete", Collections.singletonList("arg")).get();
        assertFalse(list.isEmpty());
        assertEquals("help", list.getFirst());
    }

    @Test
    public void testTabCompleteUserStringListOfStringTwoArgs() {
        List<String> list = cc.tabComplete(user, "complete", Arrays.asList("arg1", "arg2")).get();
        assertFalse(list.isEmpty());
        assertEquals("help", list.getFirst());
    }

    @Test
    public void testTabCompleteUserStringListOfStringThreeArgs() {
        List<String> list = cc.tabComplete(user, "complete", Arrays.asList("arg1", "arg2", "arg3")).get();
        assertFalse(list.isEmpty());
        assertEquals("maker", list.get(0));
        assertEquals("placer", list.get(1));
        assertEquals("breaker", list.get(2));
    }

    @Test
    public void testTabCompleteUserStringListOfStringFourArgs() {
        List<String> list = cc.tabComplete(user, "complete", Arrays.asList("arg1", "arg2", "arg3", "arg4")).get();
        assertTrue(list.isEmpty());
    }

    @Test
    public void testTabCompleteUserStringListOfStringFourArgsNumber() {
        List<String> list = cc.tabComplete(user, "complete", Arrays.asList("arg1", "arg2", "arg3", "4")).get();
        assertFalse(list.isEmpty());
        assertEquals("<number>", list.getFirst());
    }

    @Test
    public void testTabCompleteUserStringListOfStringFiveArgs() {
        List<String> list = cc.tabComplete(user, "complete", Arrays.asList("arg1", "arg2", "arg23", "arg4", "arg5")).get();
        assertFalse(list.isEmpty());
        assertEquals("help", list.getFirst());
    }

}
