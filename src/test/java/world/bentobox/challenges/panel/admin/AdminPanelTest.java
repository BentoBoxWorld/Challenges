package world.bentobox.challenges.panel.admin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
class AdminPanelTest {

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
    private MockedStatic<Bukkit> mockedBukkit;

    @BeforeEach
    void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);
        ServerMock mbServer = MockBukkit.mock();

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
    void tearDown() throws Exception {
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
    void testCreateCompleteUserChallengesButton() throws Exception {
        assertNotNull(callCreateButton("COMPLETE_USER_CHALLENGES"));
    }

    @Test
    void testCreateResetUserChallengesButton() throws Exception {
        assertNotNull(callCreateButton("RESET_USER_CHALLENGES"));
    }

    @Test
    void testCreateAddChallengeButton() throws Exception {
        assertNotNull(callCreateButton("ADD_CHALLENGE"));
    }

    @Test
    void testCreateAddLevelButton() throws Exception {
        assertNotNull(callCreateButton("ADD_LEVEL"));
    }

    @Test
    void testCreateEditChallengeButton() throws Exception {
        assertNotNull(callCreateButton("EDIT_CHALLENGE"));
    }

    @Test
    void testCreateEditLevelButton() throws Exception {
        assertNotNull(callCreateButton("EDIT_LEVEL"));
    }

    @Test
    void testCreateDeleteChallengeButton() throws Exception {
        assertNotNull(callCreateButton("DELETE_CHALLENGE"));
    }

    @Test
    void testCreateDeleteLevelButton() throws Exception {
        assertNotNull(callCreateButton("DELETE_LEVEL"));
    }

    @Test
    void testCreateEditSettingsButton() throws Exception {
        assertNotNull(callCreateButton("EDIT_SETTINGS"));
    }

    @Test
    void testCreateImportDatabaseButton() throws Exception {
        assertNotNull(callCreateButton("IMPORT_DATABASE"));
    }

    @Test
    void testCreateImportTemplateButton() throws Exception {
        assertNotNull(callCreateButton("IMPORT_TEMPLATE"));
    }

    @Test
    void testCreateExportChallengesButton() throws Exception {
        assertNotNull(callCreateButton("EXPORT_CHALLENGES"));
    }

    @Test
    void testCreateChallengeWipeButton() throws Exception {
        assertNotNull(callCreateButton("CHALLENGE_WIPE"));
    }

    @Test
    void testCreateCompleteWipeButton() throws Exception {
        assertNotNull(callCreateButton("COMPLETE_WIPE"));
    }

    @Test
    void testCreateUserWipeButton() throws Exception {
        assertNotNull(callCreateButton("USER_WIPE"));
    }

    @Test
    void testCreateLibraryButton() throws Exception {
        assertNotNull(callCreateButton("LIBRARY"));
    }

    @Test
    void testAllButtonsCreateSuccessfully() throws Exception {
        Class<?> buttonEnum = Class.forName(
            "world.bentobox.challenges.panel.admin.AdminPanel$Button");

        for (Object constant : buttonEnum.getEnumConstants()) {
            assertDoesNotThrow(() -> callCreateButton(constant.toString()),
                "Failed to create button: " + constant);
        }
    }
}
