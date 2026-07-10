package world.bentobox.challenges.panel.user;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.config.Settings;
import world.bentobox.challenges.config.SettingsUtils;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.managers.ChallengesManager;
import world.bentobox.challenges.panel.PanelTestHelper;

/**
 * Tests for {@link ChallengesPanel} challenge filtering logic (issue #337).
 */
@DisplayName("ChallengesPanel - Remove when completed flag")
class ChallengesPanelTest {

    @Mock
    private ChallengesAddon addon;
    @Mock
    private User user;
    @Mock
    private World world;
    @Mock
    private ChallengesManager manager;
    @Mock
    private Settings settings;

    private AutoCloseable closeable;
    private MockedStatic<Bukkit> mockedBukkit;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        ServerMock mbServer = MockBukkit.mock();
        PanelTestHelper.primeBukkitRegistry();

        when(addon.getChallengesManager()).thenReturn(manager);
        when(addon.getChallengesSettings()).thenReturn(settings);
        PanelTestHelper.setupUserTranslations(user);
        when(user.getWorld()).thenReturn(world);
        when(user.getUniqueId()).thenReturn(UUID.randomUUID());
        when(manager.hasAnyChallengeData(world)).thenReturn(true);

        mockedBukkit = Mockito.mockStatic(Bukkit.class, Mockito.RETURNS_DEEP_STUBS);
        mockedBukkit.when(Bukkit::getServer).thenReturn(mbServer);
        mockedBukkit.when(Bukkit::getItemFactory).thenReturn(mbServer.getItemFactory());
        mockedBukkit.when(Bukkit::getUnsafe).thenReturn(mbServer.getUnsafe());
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mockedBukkit != null) mockedBukkit.closeOnDemand();
        if (closeable != null) closeable.close();
        MockBukkit.unmock();
        Mockito.framework().clearInlineMocks();
    }

    private ChallengesPanel createPanel() throws Exception {
        // Create panel via reflection since constructor is private
        java.lang.reflect.Constructor<ChallengesPanel> ctor =
            ChallengesPanel.class.getDeclaredConstructor(
                ChallengesAddon.class, World.class, User.class, String.class, String.class);
        ctor.setAccessible(true);
        return ctor.newInstance(addon, world, user, "challenges", "challenges");
    }

    private void callUpdateFreeChallengeList(ChallengesPanel panel) throws Exception {
        Method method = ChallengesPanel.class.getDeclaredMethod("updateFreeChallengeList");
        method.setAccessible(true);
        method.invoke(panel);
    }

    private List<Challenge> getFreeChallengeList(ChallengesPanel panel) throws Exception {
        Field field = ChallengesPanel.class.getDeclaredField("freeChallengeList");
        field.setAccessible(true);
        return (List<Challenge>) field.get(panel);
    }

    @Test
    @DisplayName("Global OFF, per-challenge OFF: completed non-repeatable should NOT be hidden")
    void testGlobalOffPerChallengeOff() throws Exception {
        // Setup
        when(settings.isRemoveCompleteOneTimeChallenges()).thenReturn(false);
        when(settings.getVisibilityMode()).thenReturn(SettingsUtils.VisibilityMode.VISIBLE);

        Challenge completed = PanelTestHelper.createBasicChallenge("TestChallenge", true);
        when(completed.isRepeatable()).thenReturn(false);
        when(completed.isRemoveWhenCompleted()).thenReturn(false);

        List<Challenge> challenges = new ArrayList<>();
        challenges.add(completed);
        when(manager.getFreeChallenges(world)).thenReturn(challenges);
        when(manager.isChallengeComplete(user, world, completed)).thenReturn(true);

        // Execute
        ChallengesPanel panel = createPanel();
        callUpdateFreeChallengeList(panel);

        // Verify: challenge should still be in the list
        assertTrue(getFreeChallengeList(panel).contains(completed),
            "Completed challenge should be visible when both global and per-challenge flags are OFF");
    }

    @Test
    @DisplayName("Global ON, per-challenge OFF: completed non-repeatable should be hidden")
    void testGlobalOnPerChallengeOff() throws Exception {
        // Setup
        when(settings.isRemoveCompleteOneTimeChallenges()).thenReturn(true);
        when(settings.getVisibilityMode()).thenReturn(SettingsUtils.VisibilityMode.VISIBLE);

        Challenge completed = PanelTestHelper.createBasicChallenge("TestChallenge", true);
        when(completed.isRepeatable()).thenReturn(false);
        when(completed.isRemoveWhenCompleted()).thenReturn(false);

        List<Challenge> challenges = new ArrayList<>();
        challenges.add(completed);
        when(manager.getFreeChallenges(world)).thenReturn(challenges);
        when(manager.isChallengeComplete(user, world, completed)).thenReturn(true);

        // Execute
        ChallengesPanel panel = createPanel();
        callUpdateFreeChallengeList(panel);

        // Verify: challenge should be hidden
        assertFalse(getFreeChallengeList(panel).contains(completed),
            "Completed challenge should be hidden when global setting is ON");
    }

    @Test
    @DisplayName("Global OFF, per-challenge ON: completed non-repeatable should be hidden")
    void testGlobalOffPerChallengeOn() throws Exception {
        // Setup
        when(settings.isRemoveCompleteOneTimeChallenges()).thenReturn(false);
        when(settings.getVisibilityMode()).thenReturn(SettingsUtils.VisibilityMode.VISIBLE);

        Challenge completed = PanelTestHelper.createBasicChallenge("TestChallenge", true);
        when(completed.isRepeatable()).thenReturn(false);
        when(completed.isRemoveWhenCompleted()).thenReturn(true);  // Per-challenge flag is ON

        List<Challenge> challenges = new ArrayList<>();
        challenges.add(completed);
        when(manager.getFreeChallenges(world)).thenReturn(challenges);
        when(manager.isChallengeComplete(user, world, completed)).thenReturn(true);

        // Execute
        ChallengesPanel panel = createPanel();
        callUpdateFreeChallengeList(panel);

        // Verify: challenge should be hidden due to per-challenge flag
        assertFalse(getFreeChallengeList(panel).contains(completed),
            "Completed challenge should be hidden when per-challenge flag is ON");
    }

    @Test
    @DisplayName("Global ON, per-challenge ON: completed non-repeatable should be hidden")
    void testGlobalOnPerChallengeOn() throws Exception {
        // Setup
        when(settings.isRemoveCompleteOneTimeChallenges()).thenReturn(true);
        when(settings.getVisibilityMode()).thenReturn(SettingsUtils.VisibilityMode.VISIBLE);

        Challenge completed = PanelTestHelper.createBasicChallenge("TestChallenge", true);
        when(completed.isRepeatable()).thenReturn(false);
        when(completed.isRemoveWhenCompleted()).thenReturn(true);  // Per-challenge flag is ON

        List<Challenge> challenges = new ArrayList<>();
        challenges.add(completed);
        when(manager.getFreeChallenges(world)).thenReturn(challenges);
        when(manager.isChallengeComplete(user, world, completed)).thenReturn(true);

        // Execute
        ChallengesPanel panel = createPanel();
        callUpdateFreeChallengeList(panel);

        // Verify: challenge should be hidden
        assertFalse(getFreeChallengeList(panel).contains(completed),
            "Completed challenge should be hidden when both flags are ON");
    }

    @Test
    @DisplayName("Completed repeatable challenge should never be hidden, regardless of flags")
    void testRepeatableChallengeNeverHidden() throws Exception {
        // Setup
        when(settings.isRemoveCompleteOneTimeChallenges()).thenReturn(true);
        when(settings.getVisibilityMode()).thenReturn(SettingsUtils.VisibilityMode.VISIBLE);

        Challenge completed = PanelTestHelper.createBasicChallenge("TestChallenge", true);
        when(completed.isRepeatable()).thenReturn(true);  // Repeatable
        when(completed.isRemoveWhenCompleted()).thenReturn(true);
        when(completed.getMaxTimes()).thenReturn(5);

        List<Challenge> challenges = new ArrayList<>();
        challenges.add(completed);
        when(manager.getFreeChallenges(world)).thenReturn(challenges);
        when(manager.isChallengeComplete(user, world, completed)).thenReturn(true);

        // Execute
        ChallengesPanel panel = createPanel();
        callUpdateFreeChallengeList(panel);

        // Verify: repeatable challenge should still be in the list
        assertTrue(getFreeChallengeList(panel).contains(completed),
            "Repeatable challenge should always be visible, regardless of completion status or flags");
    }

    @Test
    @DisplayName("Incomplete non-repeatable challenge should never be hidden, regardless of flags")
    void testIncompleteNonRepeatableNeverHidden() throws Exception {
        // Setup
        when(settings.isRemoveCompleteOneTimeChallenges()).thenReturn(true);
        when(settings.getVisibilityMode()).thenReturn(SettingsUtils.VisibilityMode.VISIBLE);

        Challenge incomplete = PanelTestHelper.createBasicChallenge("TestChallenge", true);
        when(incomplete.isRepeatable()).thenReturn(false);
        when(incomplete.isRemoveWhenCompleted()).thenReturn(true);

        List<Challenge> challenges = new ArrayList<>();
        challenges.add(incomplete);
        when(manager.getFreeChallenges(world)).thenReturn(challenges);
        when(manager.isChallengeComplete(user, world, incomplete)).thenReturn(false);

        // Execute
        ChallengesPanel panel = createPanel();
        callUpdateFreeChallengeList(panel);

        // Verify: incomplete challenge should still be in the list
        assertTrue(getFreeChallengeList(panel).contains(incomplete),
            "Incomplete challenge should always be visible");
    }
}
