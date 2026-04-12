package world.bentobox.challenges.commands.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.PlayersManager;
import world.bentobox.bentobox.managers.WebManager;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.WhiteBox;
import world.bentobox.challenges.config.Settings;
import world.bentobox.challenges.managers.ChallengesManager;
import world.bentobox.challenges.utils.Utils;

/**
 * Base class for admin command tests. Provides the common mock setup
 * (BentoBox, Bukkit, parent CompositeCommand, etc.) shared by all admin
 * command test classes.
 */
public abstract class AdminCommandTestBase {

    @Mock
    protected CompositeCommand parentCmd;
    @Mock
    protected User user;
    @Mock
    protected IslandsManager im;
    @Mock
    protected Island island;
    @Mock
    protected ChallengesAddon addon;
    @Mock
    protected World world;
    @Mock
    protected ChallengesManager chm;
    @Mock
    protected IslandWorldManager iwm;
    @Mock
    protected GameModeAddon gameModeAddon;
    @Mock
    protected PlayersManager pm;

    protected BentoBox plugin;
    protected Settings settings;
    protected UUID userUUID;
    protected AutoCloseable closeable;
    protected ServerMock server;
    protected MockedStatic<Bukkit> mockedBukkit;
    protected MockedStatic<Utils> mockedUtils;
    protected MockedStatic<Util> mockedUtil;

    @BeforeEach
    public void setUpBase() {
        closeable = MockitoAnnotations.openMocks(this);
        server = MockBukkit.mock();
        @SuppressWarnings("unused")
        var unusedTagRef = org.bukkit.Tag.LEAVES;

        // BentoBox singleton
        plugin = mock(BentoBox.class);
        WhiteBox.setInternalState(BentoBox.class, "instance", plugin);
        User.setPlugin(plugin);

        // Command manager
        CommandsManager cm = mock(CommandsManager.class);
        when(plugin.getCommandsManager()).thenReturn(cm);

        // Parent command
        when(parentCmd.getAddon()).thenReturn(addon);
        when(parentCmd.getPermissionPrefix()).thenReturn("bskyblock.");
        when(parentCmd.getLabel()).thenReturn("challenges");
        when(parentCmd.getTopLabel()).thenReturn("bsb");
        when(parentCmd.getWorld()).thenReturn(world);

        // IWM
        when(iwm.getFriendlyName(any())).thenReturn("BSkyBlock");
        when(iwm.inWorld(any(World.class))).thenReturn(true);
        when(iwm.getAddon(any())).thenReturn(Optional.of(gameModeAddon));
        when(plugin.getIWM()).thenReturn(iwm);

        // Game Mode Addon
        when(gameModeAddon.getAdminCommand()).thenReturn(Optional.of(parentCmd));

        // World
        when(world.toString()).thenReturn("world");
        when(world.getName()).thenReturn("world");

        // User / Player
        Player p = mock(Player.class);
        userUUID = UUID.randomUUID();
        when(user.isOp()).thenReturn(false);
        when(user.getUniqueId()).thenReturn(userUUID);
        when(user.getPlayer()).thenReturn(p);
        when(user.getName()).thenReturn("tastybento");
        when(user.isPlayer()).thenReturn(true);
        when(user.getWorld()).thenReturn(world);
        when(user.getTranslation(anyString()))
                .thenAnswer((Answer<String>) inv -> inv.getArgument(0, String.class));
        when(user.getTranslationOrNothing(anyString()))
                .thenAnswer((Answer<String>) inv -> inv.getArgument(0, String.class));

        // Bukkit static mock
        mockedBukkit = Mockito.mockStatic(Bukkit.class, Mockito.RETURNS_DEEP_STUBS);
        mockedBukkit.when(Bukkit::getServer).thenReturn(server);
        mockedBukkit.when(Bukkit::getItemFactory).thenReturn(server.getItemFactory());

        // Addon & ChallengesManager
        when(addon.getChallengesManager()).thenReturn(chm);
        when(chm.hasAnyChallengeData(any(World.class))).thenReturn(true);

        // Settings
        settings = new Settings();
        when(addon.getChallengesSettings()).thenReturn(settings);

        // Island Manager
        when(plugin.getIslands()).thenReturn(im);
        when(im.getIsland(any(), any(User.class))).thenReturn(island);

        // Players Manager
        when(plugin.getPlayers()).thenReturn(pm);

        // Web Manager (needed by AdminPanel.open)
        WebManager wm = mock(WebManager.class);
        when(plugin.getWebManager()).thenReturn(wm);

        // Utils static mock
        mockedUtils = Mockito.mockStatic(Utils.class);
        mockedUtils.when(() -> Utils.getGameMode(any())).thenReturn("BSkyBlock");

        // Util static mock
        mockedUtil = Mockito.mockStatic(Util.class, Mockito.CALLS_REAL_METHODS);
        mockedUtil.when(() -> Util.translateColorCodes(anyString()))
                .thenAnswer((Answer<String>) inv -> inv.getArgument(0, String.class));
    }

    @AfterEach
    public void tearDownBase() throws Exception {
        if (mockedBukkit != null) mockedBukkit.closeOnDemand();
        if (mockedUtils != null) mockedUtils.closeOnDemand();
        if (mockedUtil != null) mockedUtil.closeOnDemand();
        if (closeable != null) closeable.close();
        MockBukkit.unmock();
        User.clearUsers();
        Mockito.framework().clearInlineMocks();
    }
}
