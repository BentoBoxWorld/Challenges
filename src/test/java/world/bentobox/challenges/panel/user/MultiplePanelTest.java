package world.bentobox.challenges.panel.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

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

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.TemplatedPanel;
import world.bentobox.bentobox.api.panels.reader.ItemTemplateRecord;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.panel.PanelTestHelper;

/**
 * Tests for {@link MultiplePanel} button creation logic.
 */
class MultiplePanelTest {

    @Mock
    private ChallengesAddon addon;
    @Mock
    private User user;
    @Mock
    private World world;

    private AutoCloseable closeable;
    private ServerMock mbServer;
    private MockedStatic<Bukkit> mockedBukkit;

    @BeforeEach
    void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);
        mbServer = MockBukkit.mock();

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

    @SuppressWarnings("unchecked")
    private MultiplePanel createPanel(Consumer<Integer> action) throws Exception {
        Constructor<MultiplePanel> ctor = MultiplePanel.class.getDeclaredConstructor(
            ChallengesAddon.class, User.class, Consumer.class);
        ctor.setAccessible(true);
        return ctor.newInstance(addon, user, action);
    }

    private int getCompletionValue(MultiplePanel panel) throws Exception {
        Field field = MultiplePanel.class.getDeclaredField("completionValue");
        field.setAccessible(true);
        return field.getInt(panel);
    }

    private void setCompletionValue(MultiplePanel panel, int value) throws Exception {
        Field field = MultiplePanel.class.getDeclaredField("completionValue");
        field.setAccessible(true);
        field.setInt(panel, value);
    }

    private PanelItem callCreateIncreaseButton(MultiplePanel panel,
            ItemTemplateRecord template, TemplatedPanel.ItemSlot slot) throws Exception {
        Method method = MultiplePanel.class.getDeclaredMethod("createIncreaseButton",
            ItemTemplateRecord.class, TemplatedPanel.ItemSlot.class);
        method.setAccessible(true);
        return (PanelItem) method.invoke(panel, template, slot);
    }

    private PanelItem callCreateReduceButton(MultiplePanel panel,
            ItemTemplateRecord template, TemplatedPanel.ItemSlot slot) throws Exception {
        Method method = MultiplePanel.class.getDeclaredMethod("createReduceButton",
            ItemTemplateRecord.class, TemplatedPanel.ItemSlot.class);
        method.setAccessible(true);
        return (PanelItem) method.invoke(panel, template, slot);
    }

    private PanelItem callCreateValueButton(MultiplePanel panel,
            ItemTemplateRecord template, TemplatedPanel.ItemSlot slot) throws Exception {
        Method method = MultiplePanel.class.getDeclaredMethod("createValueButton",
            ItemTemplateRecord.class, TemplatedPanel.ItemSlot.class);
        method.setAccessible(true);
        return (PanelItem) method.invoke(panel, template, slot);
    }

    @Test
    void testDefaultCompletionValueIsOne() throws Exception {
        @SuppressWarnings("unchecked")
        Consumer<Integer> action = mock(Consumer.class);
        MultiplePanel panel = createPanel(action);
        assertEquals(1, getCompletionValue(panel));
    }

    @Test
    void testCreateIncreaseButton() throws Exception {
        @SuppressWarnings("unchecked")
        Consumer<Integer> action = mock(Consumer.class);
        MultiplePanel panel = createPanel(action);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("value", 5);
        ItemTemplateRecord template = PanelTestHelper.createTemplate(
            new ItemStack(Material.PAPER), "Title", "Desc [number]",
            dataMap, Collections.emptyList());
        TemplatedPanel.ItemSlot slot = PanelTestHelper.createItemSlot(0, new HashMap<>());

        PanelItem item = callCreateIncreaseButton(panel, template, slot);
        assertNotNull(item);
    }

    @Test
    void testCreateReduceButton() throws Exception {
        @SuppressWarnings("unchecked")
        Consumer<Integer> action = mock(Consumer.class);
        MultiplePanel panel = createPanel(action);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("value", 5);
        ItemTemplateRecord template = PanelTestHelper.createTemplate(
            new ItemStack(Material.PAPER), "Title", "Desc [number]",
            dataMap, Collections.emptyList());
        TemplatedPanel.ItemSlot slot = PanelTestHelper.createItemSlot(0, new HashMap<>());

        PanelItem item = callCreateReduceButton(panel, template, slot);
        assertNotNull(item);
    }

    @Test
    void testCreateValueButton() throws Exception {
        @SuppressWarnings("unchecked")
        Consumer<Integer> action = mock(Consumer.class);
        MultiplePanel panel = createPanel(action);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("value", 1);
        ItemTemplateRecord template = PanelTestHelper.createTemplate(
            new ItemStack(Material.PAPER), "Title", "Desc [number]",
            dataMap, Collections.emptyList());
        TemplatedPanel.ItemSlot slot = PanelTestHelper.createItemSlot(0, new HashMap<>());

        PanelItem item = callCreateValueButton(panel, template, slot);
        assertNotNull(item);
    }

    @Test
    void testCreateIncreaseButtonWithNullIcon() throws Exception {
        @SuppressWarnings("unchecked")
        Consumer<Integer> action = mock(Consumer.class);
        MultiplePanel panel = createPanel(action);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("value", 1);
        ItemTemplateRecord template = PanelTestHelper.createEmptyTemplate(dataMap);
        TemplatedPanel.ItemSlot slot = PanelTestHelper.createItemSlot(0, new HashMap<>());

        PanelItem item = callCreateIncreaseButton(panel, template, slot);
        assertNotNull(item);
    }

    @Test
    void testCreateReduceButtonWithNullIcon() throws Exception {
        @SuppressWarnings("unchecked")
        Consumer<Integer> action = mock(Consumer.class);
        MultiplePanel panel = createPanel(action);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("value", 1);
        ItemTemplateRecord template = PanelTestHelper.createEmptyTemplate(dataMap);
        TemplatedPanel.ItemSlot slot = PanelTestHelper.createItemSlot(0, new HashMap<>());

        PanelItem item = callCreateReduceButton(panel, template, slot);
        assertNotNull(item);
    }

    @Test
    void testCreateIncreaseButtonDefaultValue() throws Exception {
        @SuppressWarnings("unchecked")
        Consumer<Integer> action = mock(Consumer.class);
        MultiplePanel panel = createPanel(action);

        ItemTemplateRecord template = PanelTestHelper.createTemplate(
            new ItemStack(Material.PAPER), "Title", "Desc",
            new HashMap<>(), Collections.emptyList());
        TemplatedPanel.ItemSlot slot = PanelTestHelper.createItemSlot(0, new HashMap<>());

        PanelItem item = callCreateIncreaseButton(panel, template, slot);
        assertNotNull(item);
    }

    @Test
    void testCompletionValueSetAndGet() throws Exception {
        @SuppressWarnings("unchecked")
        Consumer<Integer> action = mock(Consumer.class);
        MultiplePanel panel = createPanel(action);
        setCompletionValue(panel, 10);
        assertEquals(10, getCompletionValue(panel));
    }
}
