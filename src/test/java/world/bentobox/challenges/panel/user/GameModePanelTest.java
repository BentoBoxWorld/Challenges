package world.bentobox.challenges.panel.user;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.TemplatedPanel;
import world.bentobox.bentobox.api.panels.reader.ItemTemplateRecord;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.managers.ChallengesManager;
import world.bentobox.challenges.panel.PanelTestHelper;

/**
 * Tests for {@link GameModePanel} button creation logic.
 */
class GameModePanelTest {

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
        when(user.getWorld()).thenReturn(world);

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

    private GameModePanel createPanel(List<GameModeAddon> addons, boolean adminMode) throws Exception {
        Constructor<GameModePanel> ctor = GameModePanel.class.getDeclaredConstructor(
            ChallengesAddon.class, World.class, User.class, List.class, boolean.class);
        ctor.setAccessible(true);
        return ctor.newInstance(addon, world, user, addons, adminMode);
    }

    private GameModeAddon createMockGameMode(String name) {
        GameModeAddon gm = mock(GameModeAddon.class);
        AddonDescription desc = new AddonDescription.Builder("bentobox", name, "1.0")
            .description("test").authors("tester").build();
        when(gm.getDescription()).thenReturn(desc);
        return gm;
    }

    private PanelItem callCreateGameModeButtonWithSlot(GameModePanel panel,
            ItemTemplateRecord template, TemplatedPanel.ItemSlot slot) throws Exception {
        Method method = GameModePanel.class.getDeclaredMethod("createGameModeButton",
            ItemTemplateRecord.class, TemplatedPanel.ItemSlot.class);
        method.setAccessible(true);
        return (PanelItem) method.invoke(panel, template, slot);
    }

    private PanelItem callCreateNextButton(GameModePanel panel,
            ItemTemplateRecord template, TemplatedPanel.ItemSlot slot) throws Exception {
        Method method = GameModePanel.class.getDeclaredMethod("createNextButton",
            ItemTemplateRecord.class, TemplatedPanel.ItemSlot.class);
        method.setAccessible(true);
        return (PanelItem) method.invoke(panel, template, slot);
    }

    private PanelItem callCreatePreviousButton(GameModePanel panel,
            ItemTemplateRecord template, TemplatedPanel.ItemSlot slot) throws Exception {
        Method method = GameModePanel.class.getDeclaredMethod("createPreviousButton",
            ItemTemplateRecord.class, TemplatedPanel.ItemSlot.class);
        method.setAccessible(true);
        return (PanelItem) method.invoke(panel, template, slot);
    }

    @Test
    void testCreateGameModeButtonReturnsNullForEmptyList() throws Exception {
        GameModePanel panel = createPanel(Collections.emptyList(), false);

        ItemTemplateRecord template = PanelTestHelper.createSimpleTemplate(new HashMap<>());
        TemplatedPanel.ItemSlot slot = PanelTestHelper.createItemSlot(0,
            Map.of("GAMEMODE", 1));

        PanelItem item = callCreateGameModeButtonWithSlot(panel, template, slot);
        assertNull(item);
    }

    @Test
    void testCreateGameModeButtonWithAddon() throws Exception {
        GameModeAddon gm = createMockGameMode("BSkyBlock");
        when(gm.inWorld(any(World.class))).thenReturn(false);
        GameModePanel panel = createPanel(List.of(gm), false);

        ItemTemplateRecord template = PanelTestHelper.createEmptyTemplate(new HashMap<>());
        TemplatedPanel.ItemSlot slot = PanelTestHelper.createItemSlot(0,
            Map.of("GAMEMODE", 1));

        PanelItem item = callCreateGameModeButtonWithSlot(panel, template, slot);
        assertNotNull(item);
    }

    @Test
    void testCreateGameModeButtonWithIdMatch() throws Exception {
        GameModeAddon gm = createMockGameMode("BSkyBlock");
        when(gm.inWorld(any(World.class))).thenReturn(false);
        GameModePanel panel = createPanel(List.of(gm), false);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", "BSkyBlock");
        ItemTemplateRecord template = PanelTestHelper.createEmptyTemplate(dataMap);
        TemplatedPanel.ItemSlot slot = PanelTestHelper.createItemSlot(0, new HashMap<>());

        PanelItem item = callCreateGameModeButtonWithSlot(panel, template, slot);
        assertNotNull(item);
    }

    @Test
    void testCreateGameModeButtonWithIdNoMatch() throws Exception {
        GameModeAddon gm = createMockGameMode("BSkyBlock");
        GameModePanel panel = createPanel(List.of(gm), false);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", "AcidIsland");
        ItemTemplateRecord template = PanelTestHelper.createEmptyTemplate(dataMap);
        TemplatedPanel.ItemSlot slot = PanelTestHelper.createItemSlot(0, new HashMap<>());

        PanelItem item = callCreateGameModeButtonWithSlot(panel, template, slot);
        assertNull(item);
    }

    @Test
    void testCreateGameModeButtonOutOfRange() throws Exception {
        GameModeAddon gm = createMockGameMode("BSkyBlock");
        GameModePanel panel = createPanel(List.of(gm), false);

        ItemTemplateRecord template = PanelTestHelper.createSimpleTemplate(new HashMap<>());
        TemplatedPanel.ItemSlot slot = PanelTestHelper.createItemSlot(5,
            Map.of("GAMEMODE", 1));

        PanelItem item = callCreateGameModeButtonWithSlot(panel, template, slot);
        assertNull(item);
    }

    @Test
    void testCreateNextButtonNoTarget() throws Exception {
        GameModePanel panel = createPanel(Collections.emptyList(), false);

        ItemTemplateRecord template = PanelTestHelper.createSimpleTemplate(new HashMap<>());
        TemplatedPanel.ItemSlot slot = PanelTestHelper.createItemSlot(0, new HashMap<>());

        PanelItem item = callCreateNextButton(panel, template, slot);
        assertNull(item);
    }

    @Test
    void testCreatePreviousButtonAtStart() throws Exception {
        GameModePanel panel = createPanel(Collections.emptyList(), false);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("target", "GAMEMODE");
        ItemTemplateRecord template = PanelTestHelper.createSimpleTemplate(dataMap);
        TemplatedPanel.ItemSlot slot = PanelTestHelper.createItemSlot(0, new HashMap<>());

        PanelItem item = callCreatePreviousButton(panel, template, slot);
        assertNull(item);
    }

    @Test
    void testCreateNextButtonNotEnoughAddons() throws Exception {
        GameModeAddon gm = createMockGameMode("BSkyBlock");
        GameModePanel panel = createPanel(List.of(gm), false);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("target", "GAMEMODE");
        ItemTemplateRecord template = PanelTestHelper.createSimpleTemplate(dataMap);
        TemplatedPanel.ItemSlot slot = PanelTestHelper.createItemSlot(0,
            Map.of("GAMEMODE", 5));

        PanelItem item = callCreateNextButton(panel, template, slot);
        assertNull(item);
    }

    @Test
    void testCreateGameModeButtonWithCustomTemplate() throws Exception {
        GameModeAddon gm = createMockGameMode("BSkyBlock");
        when(gm.inWorld(any(World.class))).thenReturn(true);
        GameModePanel panel = createPanel(List.of(gm), false);

        ItemTemplateRecord template = PanelTestHelper.createTemplate(
            new ItemStack(Material.DIAMOND), "custom.title", "custom.desc",
            new HashMap<>(), Collections.emptyList());
        TemplatedPanel.ItemSlot slot = PanelTestHelper.createItemSlot(0,
            Map.of("GAMEMODE", 1));

        PanelItem item = callCreateGameModeButtonWithSlot(panel, template, slot);
        assertNotNull(item);
    }
}
