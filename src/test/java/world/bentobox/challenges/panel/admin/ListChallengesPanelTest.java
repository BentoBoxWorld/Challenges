package world.bentobox.challenges.panel.admin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

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
import world.bentobox.challenges.panel.PanelTestHelper;

/**
 * Tests for {@link ListChallengesPanel} element button creation.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ListChallengesPanelTest {

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

    private Server previousServer;

    @BeforeEach
    public void setUp() throws Exception {
        when(addon.getChallengesManager()).thenReturn(manager);
        PanelTestHelper.setupUserTranslations(user);

        when(server.getItemFactory()).thenReturn(itemFactory);
        when(itemFactory.getItemMeta(any(Material.class))).thenReturn(meta);
        previousServer = Bukkit.getServer();
        PanelTestHelper.setServer(server);
    }

    @AfterEach
    public void tearDown() throws Exception {
        PanelTestHelper.setServer(previousServer);
    }

    private ListChallengesPanel createPanel(ListChallengesPanel.Mode mode) throws Exception {
        Constructor<ListChallengesPanel> ctor = ListChallengesPanel.class.getDeclaredConstructor(
            ChallengesAddon.class, World.class, User.class,
            ListChallengesPanel.Mode.class, String.class, String.class);
        ctor.setAccessible(true);
        return ctor.newInstance(addon, world, user, mode, "island", "bskyblock.");
    }

    private PanelItem callCreateElementButton(ListChallengesPanel panel, Challenge challenge)
            throws Exception {
        Method method = ListChallengesPanel.class.getDeclaredMethod("createElementButton",
            Challenge.class);
        method.setAccessible(true);
        return (PanelItem) method.invoke(panel, challenge);
    }

    @Test
    public void testCreateElementButtonEditMode() throws Exception {
        ListChallengesPanel panel = createPanel(ListChallengesPanel.Mode.EDIT);
        Challenge challenge = PanelTestHelper.createBasicChallenge("Diamond Challenge", true);
        PanelItem item = callCreateElementButton(panel, challenge);
        assertNotNull(item);
    }

    @Test
    public void testCreateElementButtonDeleteMode() throws Exception {
        ListChallengesPanel panel = createPanel(ListChallengesPanel.Mode.DELETE);
        Challenge challenge = PanelTestHelper.createBasicChallenge("Diamond Challenge", true);
        PanelItem item = callCreateElementButton(panel, challenge);
        assertNotNull(item);
    }

    @Test
    public void testCreateElementButtonUndeployedChallengeGlows() throws Exception {
        ListChallengesPanel panel = createPanel(ListChallengesPanel.Mode.EDIT);
        Challenge challenge = PanelTestHelper.createBasicChallenge("Undeployed", false);
        PanelItem item = callCreateElementButton(panel, challenge);
        assertNotNull(item);
    }

    @Test
    public void testCreateElementButtonDeployedChallengeNoGlow() throws Exception {
        ListChallengesPanel panel = createPanel(ListChallengesPanel.Mode.EDIT);
        Challenge challenge = PanelTestHelper.createBasicChallenge("Deployed", true);
        PanelItem item = callCreateElementButton(panel, challenge);
        assertNotNull(item);
    }

    @Test
    public void testCreateMultipleElementButtons() throws Exception {
        ListChallengesPanel panel = createPanel(ListChallengesPanel.Mode.EDIT);
        for (int i = 0; i < 5; i++) {
            Challenge challenge = PanelTestHelper.createBasicChallenge("Challenge " + i, true);
            PanelItem item = callCreateElementButton(panel, challenge);
            assertNotNull(item, "Button for challenge " + i + " should not be null");
        }
    }

    @Test
    public void testBothModesCreateButtons() {
        for (ListChallengesPanel.Mode mode : ListChallengesPanel.Mode.values()) {
            assertDoesNotThrow(() -> {
                ListChallengesPanel panel = createPanel(mode);
                Challenge challenge = PanelTestHelper.createBasicChallenge("Test", true);
                PanelItem item = callCreateElementButton(panel, challenge);
                assertNotNull(item);
            }, "Mode " + mode + " should create buttons successfully");
        }
    }
}
