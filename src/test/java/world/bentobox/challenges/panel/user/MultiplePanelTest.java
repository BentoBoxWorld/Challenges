package world.bentobox.challenges.panel.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
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
import world.bentobox.bentobox.api.panels.TemplatedPanel;
import world.bentobox.bentobox.api.panels.reader.ItemTemplateRecord;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.panel.PanelTestHelper;

/**
 * Tests for {@link MultiplePanel} button creation logic.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MultiplePanelTest {

    @Mock
    private ChallengesAddon addon;
    @Mock
    private User user;
    @Mock
    private World world;
    @Mock
    private Server server;
    @Mock
    private ItemFactory itemFactory;
    @Mock
    private ItemMeta meta;

    private Server previousServer;

    @BeforeEach
    public void setUp() throws Exception {
        PanelTestHelper.setupUserTranslations(user);
        when(user.getWorld()).thenReturn(world);

        when(server.getItemFactory()).thenReturn(itemFactory);
        when(itemFactory.getItemMeta(any(Material.class))).thenReturn(meta);
        previousServer = Bukkit.getServer();
        PanelTestHelper.setServer(server);
    }

    @AfterEach
    public void tearDown() throws Exception {
        PanelTestHelper.setServer(previousServer);
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
    public void testDefaultCompletionValueIsOne() throws Exception {
        @SuppressWarnings("unchecked")
        Consumer<Integer> action = mock(Consumer.class);
        MultiplePanel panel = createPanel(action);
        assertEquals(1, getCompletionValue(panel));
    }

    @Test
    public void testCreateIncreaseButton() throws Exception {
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
    public void testCreateReduceButton() throws Exception {
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
    public void testCreateValueButton() throws Exception {
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
    public void testCreateIncreaseButtonWithNullIcon() throws Exception {
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
    public void testCreateReduceButtonWithNullIcon() throws Exception {
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
    public void testCreateIncreaseButtonDefaultValue() throws Exception {
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
    public void testCompletionValueSetAndGet() throws Exception {
        @SuppressWarnings("unchecked")
        Consumer<Integer> action = mock(Consumer.class);
        MultiplePanel panel = createPanel(action);
        setCompletionValue(panel, 10);
        assertEquals(10, getCompletionValue(panel));
    }
}
