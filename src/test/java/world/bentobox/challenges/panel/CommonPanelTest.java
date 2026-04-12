package world.bentobox.challenges.panel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.managers.ChallengesManager;

/**
 * Tests for {@link CommonPanel} including description generation.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CommonPanelTest {

    @Mock
    private ChallengesAddon addon;
    @Mock
    private User user;
    @Mock
    private World world;
    @Mock
    private ChallengesManager manager;
    @Mock
    private Server server;
    @Mock
    private ItemFactory itemFactory;
    @Mock
    private ItemMeta meta;

    private TestableCommonPanel panel;
    private Server previousServer;

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
    public void setUp() throws Exception {
        when(addon.getChallengesManager()).thenReturn(manager);
        PanelTestHelper.setupUserTranslations(user);

        when(server.getItemFactory()).thenReturn(itemFactory);
        when(itemFactory.getItemMeta(any(Material.class))).thenReturn(meta);
        previousServer = Bukkit.getServer();
        PanelTestHelper.setServer(server);

        panel = new TestableCommonPanel(addon, user, world, "island", "bskyblock.");
    }

    @AfterEach
    public void tearDown() throws Exception {
        PanelTestHelper.setServer(previousServer);
    }

    @Test
    public void testMainConstructorSetsFields() {
        assertEquals(addon, panel.addon);
        assertEquals(manager, panel.manager);
        assertEquals(user, panel.user);
        assertEquals(world, panel.world);
        assertEquals("island", panel.topLabel);
        assertEquals("bskyblock.", panel.permissionPrefix);
    }

    @Test
    public void testMainConstructorCreatesReturnButton() {
        assertNotNull(panel.getReturnButton());
    }

    @Test
    public void testChildConstructorInheritsFields() {
        TestableCommonPanel child = new TestableCommonPanel(panel);
        assertEquals(addon, child.addon);
        assertEquals(manager, child.manager);
        assertEquals(user, child.user);
        assertEquals(world, child.world);
        assertEquals("island", child.topLabel);
        assertEquals("bskyblock.", child.permissionPrefix);
    }

    @Test
    public void testChildConstructorCreatesReturnButton() {
        TestableCommonPanel child = new TestableCommonPanel(panel);
        assertNotNull(child.getReturnButton());
    }

    @Test
    public void testReopenCallsBuild() {
        CommonPanel.reopen(panel);
        assertEquals(1, panel.getBuildCount());
    }

    @Test
    public void testReopenMultipleTimes() {
        CommonPanel.reopen(panel);
        CommonPanel.reopen(panel);
        CommonPanel.reopen(panel);
        assertEquals(3, panel.getBuildCount());
    }

    @Test
    public void testGenerateChallengeDescriptionNullTarget() {
        Challenge challenge = PanelTestHelper.createBasicChallenge("Test", true);
        List<String> description = panel.callGenerateChallengeDescription(challenge, null);
        assertNotNull(description);
    }

    @Test
    public void testGenerateChallengeDescriptionCompletedChallenge() {
        Challenge challenge = PanelTestHelper.createBasicChallenge("Test", true);
        UUID uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);
        when(manager.isChallengeComplete(uuid, world, challenge)).thenReturn(true);

        List<String> description = panel.callGenerateChallengeDescription(challenge, user);
        assertNotNull(description);
    }

    @Test
    public void testGenerateChallengeDescriptionRepeatableChallenge() {
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
    public void testGenerateChallengeDescriptionFullyCompletedRepeatable() {
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
    public void testGenerateChallengeDescriptionWithCustomDescription() {
        Challenge challenge = PanelTestHelper.createBasicChallenge("Test", true);
        when(challenge.getDescription()).thenReturn(List.of("Custom line 1", "Custom line 2"));

        List<String> description = panel.callGenerateChallengeDescription(challenge, null);
        assertNotNull(description);
    }

    @Test
    public void testGenerateChallengeDescriptionEmptyDescription() {
        Challenge challenge = PanelTestHelper.createBasicChallenge("Test", true);
        when(challenge.getDescription()).thenReturn(Collections.emptyList());

        List<String> description = panel.callGenerateChallengeDescription(challenge, null);
        assertNotNull(description);
    }
}
