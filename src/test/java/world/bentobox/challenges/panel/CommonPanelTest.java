package world.bentobox.challenges.panel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.managers.ChallengesManager;

/**
 * Tests for {@link CommonPanel} including description generation.
 */
class CommonPanelTest {

    @Mock
    private ChallengesAddon addon;
    @Mock
    private User user;
    @Mock
    private World world;
    @Mock
    private ChallengesManager manager;

    private TestableCommonPanel panel;
    private AutoCloseable closeable;
    private MockedStatic<Bukkit> mockedBukkit;

    private static class TestableCommonPanel extends CommonPanel {
        private int buildCount = 0;

        protected TestableCommonPanel(ChallengesAddon addon, User user, World world,
                String topLabel, String permissionPrefix) {
            super(addon, user, world, topLabel, permissionPrefix);
        }

        protected TestableCommonPanel(CommonPanel parentPanel) {
            super(parentPanel);
        }

        @Override
        protected void build() {
            buildCount++;
        }

        public int getBuildCount() {
            return buildCount;
        }

        public List<String> callGenerateChallengeDescription(Challenge challenge, User target) {
            return this.generateChallengeDescription(challenge, target);
        }

        public PanelItem getReturnButton() {
            return this.returnButton;
        }
    }

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        ServerMock mbServer = MockBukkit.mock();

        when(addon.getChallengesManager()).thenReturn(manager);
        PanelTestHelper.setupUserTranslations(user);

        mockedBukkit = Mockito.mockStatic(Bukkit.class, Mockito.RETURNS_DEEP_STUBS);
        mockedBukkit.when(Bukkit::getServer).thenReturn(mbServer);
        mockedBukkit.when(Bukkit::getItemFactory).thenReturn(mbServer.getItemFactory());
        mockedBukkit.when(Bukkit::getUnsafe).thenReturn(mbServer.getUnsafe());

        panel = new TestableCommonPanel(addon, user, world, "island", "bskyblock.");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mockedBukkit != null) mockedBukkit.closeOnDemand();
        if (closeable != null) closeable.close();
        MockBukkit.unmock();
        Mockito.framework().clearInlineMocks();
    }

    @Test
    void testMainConstructorSetsFields() {
        assertEquals(addon, panel.addon);
        assertEquals(manager, panel.manager);
        assertEquals(user, panel.user);
        assertEquals(world, panel.world);
        assertEquals("island", panel.topLabel);
        assertEquals("bskyblock.", panel.permissionPrefix);
    }

    @Test
    void testMainConstructorCreatesReturnButton() {
        assertNotNull(panel.getReturnButton());
    }

    @Test
    void testChildConstructorInheritsFields() {
        TestableCommonPanel child = new TestableCommonPanel(panel);
        assertEquals(addon, child.addon);
        assertEquals(manager, child.manager);
        assertEquals(user, child.user);
        assertEquals(world, child.world);
        assertEquals("island", child.topLabel);
        assertEquals("bskyblock.", child.permissionPrefix);
    }

    @Test
    void testChildConstructorCreatesReturnButton() {
        TestableCommonPanel child = new TestableCommonPanel(panel);
        assertNotNull(child.getReturnButton());
    }

    @Test
    void testReopenCallsBuild() {
        CommonPanel.reopen(panel);
        assertEquals(1, panel.getBuildCount());
    }

    @Test
    void testReopenMultipleTimes() {
        CommonPanel.reopen(panel);
        CommonPanel.reopen(panel);
        CommonPanel.reopen(panel);
        assertEquals(3, panel.getBuildCount());
    }

    @Test
    void testGenerateChallengeDescriptionNullTarget() {
        Challenge challenge = PanelTestHelper.createBasicChallenge("Test", true);
        List<String> description = panel.callGenerateChallengeDescription(challenge, null);
        assertNotNull(description);
    }

    @Test
    void testGenerateChallengeDescriptionCompletedChallenge() {
        Challenge challenge = PanelTestHelper.createBasicChallenge("Test", true);
        UUID uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);
        when(manager.isChallengeComplete(uuid, world, challenge)).thenReturn(true);

        List<String> description = panel.callGenerateChallengeDescription(challenge, user);
        assertNotNull(description);
    }

    @Test
    void testGenerateChallengeDescriptionRepeatableChallenge() {
        Challenge challenge = PanelTestHelper.createBasicChallenge("Test", true);
        when(challenge.isRepeatable()).thenReturn(true);
        when(challenge.getMaxTimes()).thenReturn(5);
        UUID uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);
        when(manager.isChallengeComplete(uuid, world, challenge)).thenReturn(true);
        when(manager.getChallengeTimes(user, world, challenge)).thenReturn(3L);

        List<String> description = panel.callGenerateChallengeDescription(challenge, user);
        assertNotNull(description);
    }

    @Test
    void testGenerateChallengeDescriptionFullyCompletedRepeatable() {
        Challenge challenge = PanelTestHelper.createBasicChallenge("Test", true);
        when(challenge.isRepeatable()).thenReturn(true);
        when(challenge.getMaxTimes()).thenReturn(5);
        UUID uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);
        when(manager.isChallengeComplete(uuid, world, challenge)).thenReturn(true);
        when(manager.getChallengeTimes(user, world, challenge)).thenReturn(5L);

        List<String> description = panel.callGenerateChallengeDescription(challenge, user);
        assertNotNull(description);
    }

    @Test
    void testGenerateChallengeDescriptionWithCustomDescription() {
        Challenge challenge = PanelTestHelper.createBasicChallenge("Test", true);
        when(challenge.getDescription()).thenReturn(List.of("Custom line 1", "Custom line 2"));

        List<String> description = panel.callGenerateChallengeDescription(challenge, null);
        assertNotNull(description);
    }

    @Test
    void testGenerateChallengeDescriptionEmptyDescription() {
        Challenge challenge = PanelTestHelper.createBasicChallenge("Test", true);
        when(challenge.getDescription()).thenReturn(Collections.emptyList());

        List<String> description = panel.callGenerateChallengeDescription(challenge, null);
        assertNotNull(description);
    }
}
