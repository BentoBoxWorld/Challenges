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

import java.util.Collections;
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
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.ChallengesManager;
import world.bentobox.challenges.config.Settings;
import world.bentobox.challenges.config.SettingsUtils.VisibilityMode;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class, ChatColor.class})
public class ChallengesCommandTest {

    @Mock
    private CompositeCommand ic;
    private UUID uuid;
    @Mock
    private User user;
    @Mock
    private IslandsManager im;
    @Mock
    private Island island;
    @Mock
    private ChallengesAddon addon;
    private ChallengesCommand cc;
    @Mock
    private World world;
    @Mock
    private ChallengesManager chm;
    @Mock
    private IslandWorldManager iwm;
    @Mock
    private GameModeAddon gameModeAddon;
    @Mock
    private Settings settings;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
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
        uuid = UUID.randomUUID();
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

        // Addon
        when(addon.getChallengesManager()).thenReturn(chm);
        when(chm.getAllChallengeLevelStatus(any(), any())).thenReturn(Collections.emptyList());
        // Challenges exist
        when(chm.hasAnyChallengeData(any(World.class))).thenReturn(true);

        // ChatColor
        PowerMockito.mockStatic(ChatColor.class);
        when(ChatColor.translateAlternateColorCodes(any(char.class), anyString())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(1, String.class));

        // Settings
        when(addon.getChallengesSettings()).thenReturn(settings);
        when(settings.getVisibilityMode()).thenReturn(VisibilityMode.VISIBLE);

        // Island
        when(plugin.getIslands()).thenReturn(im);
        when(im.getIsland(any(), any(User.class))).thenReturn(island);

        // Command under test
        cc = new ChallengesCommand(addon, ic);
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.ChallengesCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteWrongWorld() {
        when(iwm.inWorld(any(World.class))).thenReturn(false);
        assertFalse(cc.canExecute(user, "challenges", Collections.emptyList()));
        verify(user).sendMessage("general.errors.wrong-world");
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.ChallengesCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteNoChallenges() {
        when(chm.hasAnyChallengeData(any(World.class))).thenReturn(false);
        assertFalse(cc.canExecute(user, "challenges", Collections.emptyList()));
        verify(addon).logError("There are no challenges set up in world!");
        verify(user).sendMessage("challenges.errors.no-challenges");
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.ChallengesCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteNoChallengesOp() {
        when(user.isOp()).thenReturn(true);
        when(chm.hasAnyChallengeData(any(World.class))).thenReturn(false);
        assertFalse(cc.canExecute(user, "challenges", Collections.emptyList()));
        verify(addon).logError("There are no challenges set up in world!");
        verify(user).sendMessage("challenges.errors.no-challenges-admin", "[command]", "bsb challenges");
        verify(user, never()).sendMessage("challenges.errors.no-challenges");
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.ChallengesCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteNoChallengesHasPerm() {
        when(user.hasPermission(anyString())).thenReturn(true);
        when(chm.hasAnyChallengeData(any(World.class))).thenReturn(false);
        assertFalse(cc.canExecute(user, "challenges", Collections.emptyList()));
        verify(addon).logError("There are no challenges set up in world!");
        verify(user).sendMessage("challenges.errors.no-challenges-admin", "[command]", "bsb challenges");
        verify(user, never()).sendMessage("challenges.errors.no-challenges");
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.ChallengesCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteNoAdminCommand() {
        when(gameModeAddon.getAdminCommand()).thenReturn(Optional.empty());
        when(user.isOp()).thenReturn(true);
        when(chm.hasAnyChallengeData(any(World.class))).thenReturn(false);
        assertFalse(cc.canExecute(user, "challenges", Collections.emptyList()));
        verify(addon).logError("There are no challenges set up in world!");
        verify(user).sendMessage("challenges.errors.no-challenges-admin", "[command]", "bsb challenges");
        verify(user, never()).sendMessage("challenges.errors.no-challenges");
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.ChallengesCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteNoIsland() {
        when(im.getIsland(any(), any(User.class))).thenReturn(null);
        assertFalse(cc.canExecute(user, "challenges", Collections.emptyList()));
        verify(user).sendMessage("general.errors.no-island");       
    }
    
    /**
     * Test method for {@link world.bentobox.challenges.commands.ChallengesCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteSuccess() {
        assertTrue(cc.canExecute(user, "challenges", Collections.emptyList()));
        verify(user, never()).sendMessage(anyString());       
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.ChallengesCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringConsole() {
        User console = mock(User.class);
        assertFalse(cc.execute(console, "challenges", Collections.emptyList()));
        verify(console).sendMessage(eq("commands.help.header"), eq(TextVariables.LABEL), eq("BSkyBlock"));
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.ChallengesCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringUser() {        
        assertTrue(cc.execute(user, "challenges", Collections.emptyList()));
    }

    /**
     * Test method for {@link world.bentobox.challenges.commands.ChallengesCommand#setup()}.
     */
    @Test
    public void testSetup() {
        assertEquals("bskyblock." + ChallengesCommand.CHALLENGE_COMMAND, cc.getPermission());
        assertEquals("challenges.commands.user.main.parameters", cc.getParameters());
        assertEquals("challenges.commands.user.main.description", cc.getDescription());
        assertTrue(cc.isOnlyPlayer());
        // CompleteChallengeCommand
        assertEquals(1, cc.getSubCommands(true).size());
    }

}
