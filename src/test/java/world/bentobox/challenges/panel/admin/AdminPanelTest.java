package world.bentobox.challenges.panel.admin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Optional;

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

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.managers.WebManager;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.WhiteBox;
import world.bentobox.challenges.managers.ChallengesManager;
import world.bentobox.challenges.panel.PanelTestHelper;

/**
 * Tests for {@link AdminPanel} button creation logic.
 */
public class AdminPanelTest {

    @Mock
    private ChallengesAddon addon;
    @Mock
    private User user;
    @Mock
    private World world;
    @Mock
    private ChallengesManager manager;
    @Mock
    private BentoBox plugin;
    @Mock
    private WebManager webManager;

    private Object adminPanel;
    private AutoCloseable closeable;
    private ServerMock mbServer;
    private MockedStatic<Bukkit> mockedBukkit;

    @BeforeEach
    public void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);
        mbServer = MockBukkit.mock();

        when(addon.getChallengesManager()).thenReturn(manager);
        PanelTestHelper.setupUserTranslations(user);

        // BentoBox instance for WebManager.isEnabled()
        when(plugin.getWebManager()).thenReturn(webManager);
        when(webManager.getGitHub()).thenReturn(Optional.empty());
        WhiteBox.setInternalState(BentoBox.class, "instance", plugin);

        mockedBukkit = Mockito.mockStatic(Bukkit.class, Mockito.RETURNS_DEEP_STUBS);
        mockedBukkit.when(Bukkit::getServer).thenReturn(mbServer);
        mockedBukkit.when(Bukkit::getItemFactory).thenReturn(mbServer.getItemFactory());
        mockedBukkit.when(Bukkit::getUnsafe).thenReturn(mbServer.getUnsafe());

        var ctor = AdminPanel.class.getDeclaredConstructor(
            ChallengesAddon.class, World.class, User.class, String.class, String.class);
        ctor.setAccessible(true);
        adminPanel = ctor.newInstance(addon, world, user, "island", "bskyblock.");
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (mockedBukkit != null) mockedBukkit.closeOnDemand();
        if (closeable != null) closeable.close();
        MockBukkit.unmock();
        Mockito.framework().clearInlineMocks();
    }

    private PanelItem callCreateButton(String buttonName) throws Exception {
        Class<?> buttonEnum = Class.forName(
            "world.bentobox.challenges.panel.admin.AdminPanel$Button");
        Object enumValue = null;
        for (Object c : buttonEnum.getEnumConstants()) {
            if (c.toString().equals(buttonName)) {
                enumValue = c;
                break;
            }
        }
        Method method = AdminPanel.class.getDeclaredMethod("createButton", buttonEnum);
        method.setAccessible(true);
        return (PanelItem) method.invoke(adminPanel, enumValue);
    }

    @Test
    public void testCreateCompleteUserChallengesButton() throws Exception {
        assertNotNull(callCreateButton("COMPLETE_USER_CHALLENGES"));
    }

    @Test
    public void testCreateResetUserChallengesButton() throws Exception {
        assertNotNull(callCreateButton("RESET_USER_CHALLENGES"));
    }

    @Test
    public void testCreateAddChallengeButton() throws Exception {
        assertNotNull(callCreateButton("ADD_CHALLENGE"));
    }

    @Test
    public void testCreateAddLevelButton() throws Exception {
        assertNotNull(callCreateButton("ADD_LEVEL"));
    }

    @Test
    public void testCreateEditChallengeButton() throws Exception {
        assertNotNull(callCreateButton("EDIT_CHALLENGE"));
    }

    @Test
    public void testCreateEditLevelButton() throws Exception {
        assertNotNull(callCreateButton("EDIT_LEVEL"));
    }

    @Test
    public void testCreateDeleteChallengeButton() throws Exception {
        assertNotNull(callCreateButton("DELETE_CHALLENGE"));
    }

    @Test
    public void testCreateDeleteLevelButton() throws Exception {
        assertNotNull(callCreateButton("DELETE_LEVEL"));
    }

    @Test
    public void testCreateEditSettingsButton() throws Exception {
        assertNotNull(callCreateButton("EDIT_SETTINGS"));
    }

    @Test
    public void testCreateImportDatabaseButton() throws Exception {
        assertNotNull(callCreateButton("IMPORT_DATABASE"));
    }

    @Test
    public void testCreateImportTemplateButton() throws Exception {
        assertNotNull(callCreateButton("IMPORT_TEMPLATE"));
    }

    @Test
    public void testCreateExportChallengesButton() throws Exception {
        assertNotNull(callCreateButton("EXPORT_CHALLENGES"));
    }

    @Test
    public void testCreateChallengeWipeButton() throws Exception {
        assertNotNull(callCreateButton("CHALLENGE_WIPE"));
    }

    @Test
    public void testCreateCompleteWipeButton() throws Exception {
        assertNotNull(callCreateButton("COMPLETE_WIPE"));
    }

    @Test
    public void testCreateUserWipeButton() throws Exception {
        assertNotNull(callCreateButton("USER_WIPE"));
    }

    @Test
    public void testCreateLibraryButton() throws Exception {
        assertNotNull(callCreateButton("LIBRARY"));
    }

    @Test
    public void testAllButtonsCreateSuccessfully() throws Exception {
        Class<?> buttonEnum = Class.forName(
            "world.bentobox.challenges.panel.admin.AdminPanel$Button");

        for (Object constant : buttonEnum.getEnumConstants()) {
            assertDoesNotThrow(() -> callCreateButton(constant.toString()),
                "Failed to create button: " + constant);
        }
    }
}
