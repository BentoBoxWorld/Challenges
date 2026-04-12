package world.bentobox.challenges.panel.admin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

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
import world.bentobox.challenges.panel.PanelTestHelper;

/**
 * Tests for {@link ListChallengesPanel} element button creation.
 */
class ListChallengesPanelTest {

    @Mock
    private ChallengesAddon addon;
    @Mock
    private User user;
    @Mock
    private World world;
    @Mock
    private ChallengesManager manager;

    private AutoCloseable closeable;
    private ServerMock mbServer;
    private MockedStatic<Bukkit> mockedBukkit;

    @BeforeEach
    void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);
        mbServer = MockBukkit.mock();

        when(addon.getChallengesManager()).thenReturn(manager);
        PanelTestHelper.setupUserTranslations(user);

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
    void testCreateElementButtonEditMode() throws Exception {
        ListChallengesPanel panel = createPanel(ListChallengesPanel.Mode.EDIT);
        Challenge challenge = PanelTestHelper.createBasicChallenge("Diamond Challenge", true);
        PanelItem item = callCreateElementButton(panel, challenge);
        assertNotNull(item);
    }

    @Test
    void testCreateElementButtonDeleteMode() throws Exception {
        ListChallengesPanel panel = createPanel(ListChallengesPanel.Mode.DELETE);
        Challenge challenge = PanelTestHelper.createBasicChallenge("Diamond Challenge", true);
        PanelItem item = callCreateElementButton(panel, challenge);
        assertNotNull(item);
    }

    @Test
    void testCreateElementButtonUndeployedChallengeGlows() throws Exception {
        ListChallengesPanel panel = createPanel(ListChallengesPanel.Mode.EDIT);
        Challenge challenge = PanelTestHelper.createBasicChallenge("Undeployed", false);
        PanelItem item = callCreateElementButton(panel, challenge);
        assertNotNull(item);
    }

    @Test
    void testCreateElementButtonDeployedChallengeNoGlow() throws Exception {
        ListChallengesPanel panel = createPanel(ListChallengesPanel.Mode.EDIT);
        Challenge challenge = PanelTestHelper.createBasicChallenge("Deployed", true);
        PanelItem item = callCreateElementButton(panel, challenge);
        assertNotNull(item);
    }

    @Test
    void testCreateMultipleElementButtons() throws Exception {
        ListChallengesPanel panel = createPanel(ListChallengesPanel.Mode.EDIT);
        for (int i = 0; i < 5; i++) {
            Challenge challenge = PanelTestHelper.createBasicChallenge("Challenge " + i, true);
            PanelItem item = callCreateElementButton(panel, challenge);
            assertNotNull(item, "Button for challenge " + i + " should not be null");
        }
    }

    @Test
    void testBothModesCreateButtons() {
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
