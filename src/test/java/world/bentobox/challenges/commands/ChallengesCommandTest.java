package world.bentobox.challenges.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
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
import world.bentobox.bentobox.api.configuration.WorldSettings;
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
import world.bentobox.challenges.managers.ChallengesManager;

/**
 * @author tastybento
 *
 */
public class ChallengesCommandTest {

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
    private ChallengesPlayerCommand cc;
    @Mock
    private World world;
    @Mock
    private ChallengesManager chm;
    @Mock
    private IslandWorldManager iwm;
    @Mock
    private GameModeAddon gameModeAddon;

    private AutoCloseable closeable;
    private ServerMock server;
    private MockedStatic<Bukkit> mockedBukkit;
    private MockedStatic<Util> mockedUtil;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        server = MockBukkit.mock();
        // Force Paper static fields to initialize against the real MockBukkit server
        // before mockStatic(Bukkit) replaces Bukkit with Mockito stubs.
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

        @NonNull
        WorldSettings ws = new TestWorldSetting();
        when(iwm.getWorldSettings(any())).thenReturn(ws);

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
        when(user.getTranslationOrNothing(anyString())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(0, String.class));
        when(user.getWorld()).thenReturn(world);

        // Bukkit static mock — leave most calls as deep stubs and delegate
        // server/itemFactory to the real MockBukkit instance.
        mockedBukkit = Mockito.mockStatic(Bukkit.class, Mockito.RETURNS_DEEP_STUBS);
        mockedBukkit.when(Bukkit::getServer).thenReturn(server);
        mockedBukkit.when(Bukkit::getItemFactory).thenReturn(server.getItemFactory());

        // Addon
        when(addon.getChallengesManager()).thenReturn(chm);
        when(chm.getAllChallengeLevelStatus(any(), any())).thenReturn(Collections.emptyList());
        // Challenges exist
        when(chm.hasAnyChallengeData(any(World.class))).thenReturn(true);

        // Settings
        Settings settings = new Settings();
        when(addon.getChallengesSettings()).thenReturn(settings);
        settings.setVisibilityMode(VisibilityMode.VISIBLE);

        // Island
        when(plugin.getIslands()).thenReturn(im);
        when(im.getIsland(any(), any(User.class))).thenReturn(island);
        when(im.locationIsOnIsland(any(Player.class), any())).thenReturn(true);

        // Util
        mockedUtil = Mockito.mockStatic(Util.class, Mockito.CALLS_REAL_METHODS);
        mockedUtil.when(() -> Util.sameWorld(any(), any())).thenReturn(true);
        mockedUtil.when(() -> Util.translateColorCodes(anyString()))
                .thenAnswer((Answer<String>) invocation -> invocation.getArgument(0, String.class));

        // Command under test
        cc = new ChallengesPlayerCommand(addon, ic);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (mockedBukkit != null) mockedBukkit.closeOnDemand();
        if (mockedUtil != null) mockedUtil.closeOnDemand();
        if (closeable != null) closeable.close();
        MockBukkit.unmock();
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void testCanExecuteWrongWorld() {
        when(iwm.inWorld(any(World.class))).thenReturn(false);
        assertFalse(cc.canExecute(user, "challenges", Collections.emptyList()));
        verify(user).getTranslation(world, "general.errors.wrong-world");
    }

    @Test
    public void testCanExecuteNoChallenges() {
        when(iwm.inWorld(any(World.class))).thenReturn(true);
        when(chm.hasAnyChallengeData(any(World.class))).thenReturn(false);
        assertFalse(cc.canExecute(user, "challenges", Collections.emptyList()));
        verify(addon).logError("There are no challenges set up in world!");
        verify(user).getTranslation(world, "challenges.errors.no-challenges");
    }

    @Test
    public void testCanExecuteNoChallengesOp() {
        when(user.isOp()).thenReturn(true);
        when(chm.hasAnyChallengeData(any(World.class))).thenReturn(false);
        assertFalse(cc.canExecute(user, "challenges", Collections.emptyList()));
        verify(addon).logError("There are no challenges set up in world!");
        verify(user).getTranslation(world, "challenges.errors.no-challenges-admin", "[command]", "bsb challenges");
        verify(user, never()).getTranslation(world, "challenges.errors.no-challenges");
    }

    @Test
    public void testCanExecuteNoChallengesHasPerm() {
        when(user.hasPermission(anyString())).thenReturn(true);
        when(chm.hasAnyChallengeData(any(World.class))).thenReturn(false);
        assertFalse(cc.canExecute(user, "challenges", Collections.emptyList()));
        verify(addon).logError("There are no challenges set up in world!");
        verify(user).getTranslation(world, "challenges.errors.no-challenges-admin", "[command]", "bsb challenges");
        verify(user, never()).getTranslation(world, "challenges.errors.no-challenges");
    }

    @Test
    public void testCanExecuteNoAdminCommand() {
        when(gameModeAddon.getAdminCommand()).thenReturn(Optional.empty());
        when(user.isOp()).thenReturn(true);
        when(chm.hasAnyChallengeData(any(World.class))).thenReturn(false);
        assertFalse(cc.canExecute(user, "challenges", Collections.emptyList()));
        verify(addon).logError("There are no challenges set up in world!");
        verify(user).getTranslation(world, "challenges.errors.no-challenges-admin", "[command]", "bsb challenges");
        verify(user, never()).getTranslation(world, "challenges.errors.no-challenges");
    }

    @Test
    public void testCanExecuteNoIsland() {
        when(im.getIsland(any(), any(User.class))).thenReturn(null);
        assertFalse(cc.canExecute(user, "challenges", Collections.emptyList()));
        verify(user).getTranslation(world, "general.errors.no-island");
    }

    @Test
    public void testCanExecuteSuccess() {
        assertTrue(cc.canExecute(user, "challenges", Collections.emptyList()));
        verify(user, never()).sendMessage(anyString());
    }

    @Test
    public void testExecuteUserStringListOfStringUser() {
        assertTrue(cc.execute(user, "challenges", Collections.emptyList()));
    }

    @Test
    public void testSetup() {
        assertEquals("bskyblock.challenges", cc.getPermission());
        assertEquals("challenges.commands.user.main.parameters", cc.getParameters());
        assertEquals("challenges.commands.user.main.description", cc.getDescription());
        assertTrue(cc.isOnlyPlayer());
        // CompleteChallengeCommand
        assertEquals(1, cc.getSubCommands(true).size());
    }

}
