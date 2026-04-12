package world.bentobox.challenges.panel.admin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.managers.WebManager;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.managers.ChallengesManager;
import world.bentobox.challenges.panel.PanelTestHelper;

/**
 * Tests for {@link AdminPanel} button creation logic.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
    private Server server;
    @Mock
    private ItemFactory itemFactory;
    @Mock
    private ItemMeta meta;

    @Mock
    private BentoBox plugin;
    @Mock
    private WebManager webManager;

    private Object adminPanel;
    private Server previousServer;
    private BentoBox previousInstance;

    @BeforeEach
    public void setUp() throws Exception {
        when(addon.getChallengesManager()).thenReturn(manager);
        PanelTestHelper.setupUserTranslations(user);

        when(server.getItemFactory()).thenReturn(itemFactory);
        when(itemFactory.getItemMeta(any(Material.class))).thenReturn(meta);
        previousServer = Bukkit.getServer();
        PanelTestHelper.setServer(server);

        // Set up BentoBox instance for WebManager.isEnabled()
        when(plugin.getWebManager()).thenReturn(webManager);
        when(webManager.getGitHub()).thenReturn(java.util.Optional.empty());
        previousInstance = BentoBox.getInstance();
        PanelTestHelper.setBentoBoxInstance(plugin);

        var ctor = AdminPanel.class.getDeclaredConstructor(
            ChallengesAddon.class, World.class, User.class, String.class, String.class);
        ctor.setAccessible(true);
        adminPanel = ctor.newInstance(addon, world, user, "island", "bskyblock.");
    }

    @AfterEach
    public void tearDown() throws Exception {
        PanelTestHelper.setServer(previousServer);
        PanelTestHelper.setBentoBoxInstance(previousInstance);
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
        // WebManager.isEnabled() is static - may return false by default
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
