package world.bentobox.challenges;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Player.Spigot;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.BoundingBox;
import org.eclipse.jdt.annotation.Nullable;
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
import world.bentobox.challenges.config.Settings;
import world.bentobox.challenges.managers.ChallengesManager;

/**
 * Abstract base class for Challenges addon tests.
 * Provides common BentoBox/MockBukkit/Mockito setup and teardown.
 */
public abstract class AbstractChallengesTest {

    // Constants
    protected static final String GAME_MODE_NAME = "BSkyBlock";

    // Core BentoBox
    @Mock
    protected BentoBox plugin;
    @Mock
    protected ChallengesAddon addon;
    @Mock
    protected User user;
    @Mock
    protected World world;
    @Mock
    protected Player player;
    @Mock
    protected Island island;

    // Managers
    @Mock
    protected ChallengesManager cm;
    @Mock
    protected IslandsManager im;
    @Mock
    protected IslandWorldManager iwm;
    @Mock
    protected AddonsManager am;
    @Mock
    protected GameModeAddon gameMode;
    @Mock
    protected Settings settings;
    @Mock
    protected WorldSettings mySettings;

    // Player peripherals
    @Mock
    protected @Nullable PlayerInventory inv;
    @Mock
    protected BoundingBox bb;
    @Mock
    protected Spigot spigot;

    // Infrastructure
    protected ServerMock mbServer;
    protected AutoCloseable closeable;
    protected MockedStatic<Bukkit> mockedBukkit;
    protected MockedStatic<Util> mockedUtil;

    // Shared state
    protected UUID playerUUID;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        mbServer = MockBukkit.mock();
        // Force tag registry initialization
        @SuppressWarnings("unused")
        var unusedTagRef = org.bukkit.Tag.LEAVES;

        // BentoBox singleton
        WhiteBox.setInternalState(BentoBox.class, "instance", plugin);
        when(addon.getPlugin()).thenReturn(plugin);

        // World
        when(user.getWorld()).thenReturn(world);
        when(world.getName()).thenReturn("world");
        when(world.getEnvironment()).thenReturn(Environment.NORMAL);

        // Addons manager — one game mode
        when(plugin.getAddonsManager()).thenReturn(am);
        when(am.getGameModeAddons()).thenReturn(Collections.singletonList(gameMode));
        AddonDescription desc = new AddonDescription.Builder("bentobox", GAME_MODE_NAME, "1.3")
                .description("test").authors("tasty").build();
        when(gameMode.getDescription()).thenReturn(desc);

        // Util static mock
        mockedUtil = Mockito.mockStatic(Util.class, Mockito.CALLS_REAL_METHODS);
        mockedUtil.when(() -> Util.getWorld(any())).thenReturn(world);
        mockedUtil.when(() -> Util.prettifyText(anyString())).thenCallRealMethod();
        mockedUtil.when(() -> Util.stripSpaceAfterColorCodes(anyString())).thenCallRealMethod();
        mockedUtil.when(() -> Util.translateColorCodes(anyString()))
                .thenAnswer((Answer<String>) invocation -> invocation.getArgument(0, String.class));

        // Island World Manager
        when(plugin.getIWM()).thenReturn(iwm);
        when(iwm.getAddon(any())).thenReturn(Optional.of(gameMode));
        when(iwm.getIslandDistance(any())).thenReturn(400);
        when(iwm.inWorld(any(World.class))).thenReturn(true);
        when(iwm.getFriendlyName(any())).thenReturn("BSkyBlock");

        // Island Manager
        when(addon.getIslands()).thenReturn(im);
        when(im.getIslandAt(any())).thenReturn(Optional.of(island));
        when(im.getIsland(any(), any(User.class))).thenReturn(island);
        when(im.locationIsOnIsland(any(), any())).thenReturn(true);
        // Island flags — everything allowed by default
        when(island.isAllowed(any(), any())).thenReturn(true);
        @Nullable
        Location islandCenter = mock(Location.class);
        when(islandCenter.toString()).thenReturn("center");
        when(island.getCenter()).thenReturn(islandCenter);

        // Challenges Manager
        when(addon.getChallengesManager()).thenReturn(cm);
        when(cm.isLevelUnlocked(any(), any(), any())).thenReturn(true);
        when(cm.getChallengeTimes(any(), any(), any(world.bentobox.challenges.database.object.Challenge.class)))
                .thenReturn(3L);

        // User + Player
        playerUUID = UUID.randomUUID();
        when(user.hasPermission(anyString())).thenReturn(true);
        when(user.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(playerUUID);
        when(player.getWorld()).thenReturn(world);
        when(player.spigot()).thenReturn(spigot);
        when(user.getUniqueId()).thenReturn(playerUUID);
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

        // Inventory
        when(user.getInventory()).thenReturn(inv);
        when(inv.getContents()).thenReturn(new org.bukkit.inventory.ItemStack[0]);

        // BoundingBox
        when(player.getBoundingBox()).thenReturn(bb);
        when(bb.clone()).thenReturn(bb);
        when(bb.toString()).thenReturn("BoundingBox");

        // Locales
        User.setPlugin(plugin);
        LocalesManager lm = mock(LocalesManager.class);
        when(plugin.getLocalesManager()).thenReturn(lm);
        when(lm.get(any(), any()))
                .thenAnswer((Answer<String>) invocation -> invocation.getArgument(1, String.class));
        PlaceholdersManager phm = mock(PlaceholdersManager.class);
        when(plugin.getPlaceholdersManager()).thenReturn(phm);
        when(phm.replacePlaceholders(any(), any()))
                .thenAnswer((Answer<String>) invocation -> invocation.getArgument(1, String.class));

        // Player defaults
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);

        // Addon settings
        when(addon.getChallengesSettings()).thenReturn(settings);
        when(settings.isBroadcastMessages()).thenReturn(true);
        when(settings.isShowCompletionTitle()).thenReturn(false);

        // Bukkit static mock
        mockedBukkit = Mockito.mockStatic(Bukkit.class, Mockito.RETURNS_DEEP_STUBS);
        mockedBukkit.when(Bukkit::getServer).thenReturn(mbServer);
        mockedBukkit.when(Bukkit::getOnlinePlayers)
                .then((Answer<Set<Player>>) invocation -> Set.of(player));
        mockedBukkit.when(Bukkit::getItemFactory).thenReturn(mbServer.getItemFactory());

        // World settings + flag
        Map<String, Boolean> worldFlags = new HashMap<>();
        when(mySettings.getWorldFlags()).thenReturn(worldFlags);
        when(iwm.getWorldSettings(any())).thenReturn(mySettings);
        ChallengesAddon.CHALLENGES_WORLD_PROTECTION.setSetting(world, true);
    }

    @AfterEach
    public void tearDown() throws Exception {
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
}
