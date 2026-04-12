package world.bentobox.challenges.panel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.managers.ChallengesManager;

/**
 * Tests for {@link CommonPagedPanel} pagination and button creation logic.
 */
public class CommonPagedPanelTest {

    @Mock
    private ChallengesAddon addon;
    @Mock
    private User user;
    @Mock
    private World world;
    @Mock
    private ChallengesManager manager;
    @Mock
    private PanelBuilder panelBuilder;

    private TestablePagedPanel panel;
    private AutoCloseable closeable;
    private ServerMock mbServer;
    private MockedStatic<Bukkit> mockedBukkit;

    private static class TestablePagedPanel extends CommonPagedPanel<String> {
        private boolean filterUpdated = false;
        private final List<PanelItem> createdButtons = new ArrayList<>();

        protected TestablePagedPanel(ChallengesAddon addon, User user, World world,
                String topLabel, String permissionPrefix) {
            super(addon, user, world, topLabel, permissionPrefix);
        }

        @Override
        protected void build() { }

        @Override
        protected void updateFilters() {
            this.filterUpdated = true;
        }

        @Override
        protected PanelItem createElementButton(String object) {
            PanelItem item = mock(PanelItem.class);
            createdButtons.add(item);
            return item;
        }

        public void callPopulateElements(PanelBuilder builder, List<String> objects) {
            this.populateElements(builder, objects);
        }

        @SuppressWarnings("unchecked")
        public PanelItem callGetButton(String buttonName) throws Exception {
            Class<?> enumClass = Class.forName(
                "world.bentobox.challenges.panel.CommonPagedPanel$CommonButtons");
            Object enumValue = null;
            for (Object c : enumClass.getEnumConstants()) {
                if (c.toString().equals(buttonName)) {
                    enumValue = c;
                    break;
                }
            }
            var method = CommonPagedPanel.class.getDeclaredMethod("getButton", enumClass);
            method.setAccessible(true);
            return (PanelItem) method.invoke(this, enumValue);
        }

        public void setPageIndex(int index) throws Exception {
            var field = CommonPagedPanel.class.getDeclaredField("pageIndex");
            field.setAccessible(true);
            field.setInt(this, index);
        }

        public int getPageIndex() throws Exception {
            var field = CommonPagedPanel.class.getDeclaredField("pageIndex");
            field.setAccessible(true);
            return field.getInt(this);
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);
        mbServer = MockBukkit.mock();

        when(addon.getChallengesManager()).thenReturn(manager);
        PanelTestHelper.setupUserTranslations(user);
        when(panelBuilder.slotOccupied(anyInt())).thenReturn(false);

        mockedBukkit = Mockito.mockStatic(Bukkit.class, Mockito.RETURNS_DEEP_STUBS);
        mockedBukkit.when(Bukkit::getServer).thenReturn(mbServer);
        mockedBukkit.when(Bukkit::getItemFactory).thenReturn(mbServer.getItemFactory());
        mockedBukkit.when(Bukkit::getUnsafe).thenReturn(mbServer.getUnsafe());

        panel = new TestablePagedPanel(addon, user, world, "island", "bskyblock.");
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (mockedBukkit != null) mockedBukkit.closeOnDemand();
        if (closeable != null) closeable.close();
        MockBukkit.unmock();
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void testPopulateEmptyList() {
        panel.callPopulateElements(panelBuilder, List.of());
        verify(panelBuilder, never()).item(anyInt(), any(PanelItem.class));
    }

    @Test
    public void testPopulateSingleElement() {
        panel.callPopulateElements(panelBuilder, List.of("one"));
        verify(panelBuilder).item(eq(10), any(PanelItem.class));
        assertEquals(1, panel.createdButtons.size());
    }

    @Test
    public void testPopulateExactlyMaxElements() {
        List<String> elements = IntStream.range(0, 21).mapToObj(i -> "item" + i).toList();
        panel.callPopulateElements(panelBuilder, elements);
        assertEquals(21, panel.createdButtons.size());
    }

    @Test
    public void testPopulateMoreThanMaxShowsNextButton() {
        List<String> elements = IntStream.range(0, 22).mapToObj(i -> "item" + i).toList();
        panel.callPopulateElements(panelBuilder, elements);
        ArgumentCaptor<Integer> slotCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(panelBuilder, times(23)).item(slotCaptor.capture(), any(PanelItem.class));
        assertTrue(slotCaptor.getAllValues().contains(26));
    }

    @Test
    public void testPopulateSecondPageShowsPreviousButton() throws Exception {
        panel.setPageIndex(1);
        List<String> elements = IntStream.range(0, 42).mapToObj(i -> "item" + i).toList();
        panel.callPopulateElements(panelBuilder, elements);
        ArgumentCaptor<Integer> slotCaptor = ArgumentCaptor.forClass(Integer.class);
        // 21 items + previous button + search button = 23
        verify(panelBuilder, times(23)).item(slotCaptor.capture(), any(PanelItem.class));
        assertTrue(slotCaptor.getAllValues().contains(18));
    }

    @Test
    public void testPopulateSkipsOccupiedSlots() {
        when(panelBuilder.slotOccupied(10)).thenReturn(true);
        panel.callPopulateElements(panelBuilder, List.of("one", "two", "three"));
        verify(panelBuilder, never()).item(eq(10), any(PanelItem.class));
        assertEquals(3, panel.createdButtons.size());
    }

    @Test
    public void testNegativePageIndexWrapsToLastPage() throws Exception {
        panel.setPageIndex(-1);
        List<String> elements = IntStream.range(0, 42).mapToObj(i -> "item" + i).toList();
        panel.callPopulateElements(panelBuilder, elements);
        assertEquals(2, panel.getPageIndex());
    }

    @Test
    public void testPageIndexBeyondMaxWrapsToZero() throws Exception {
        panel.setPageIndex(5);
        List<String> elements = IntStream.range(0, 21).mapToObj(i -> "item" + i).toList();
        panel.callPopulateElements(panelBuilder, elements);
        assertEquals(0, panel.getPageIndex());
    }

    @Test
    public void testSearchButtonAppearsWhenMoreThanMaxElements() {
        List<String> elements = IntStream.range(0, 22).mapToObj(i -> "item" + i).toList();
        panel.callPopulateElements(panelBuilder, elements);
        ArgumentCaptor<Integer> slotCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(panelBuilder, times(23)).item(slotCaptor.capture(), any(PanelItem.class));
        assertTrue(slotCaptor.getAllValues().contains(40));
    }

    @Test
    public void testSearchButtonAppearsWhenSearchStringSet() {
        panel.searchString = "test";
        panel.callPopulateElements(panelBuilder, List.of("one"));
        ArgumentCaptor<Integer> slotCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(panelBuilder, times(2)).item(slotCaptor.capture(), any(PanelItem.class));
        assertTrue(slotCaptor.getAllValues().contains(40));
    }

    @Test
    public void testNoSearchButtonWhenFewElementsAndNoSearch() {
        panel.callPopulateElements(panelBuilder, List.of("one", "two"));
        ArgumentCaptor<Integer> slotCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(panelBuilder, times(2)).item(slotCaptor.capture(), any(PanelItem.class));
        assertFalse(slotCaptor.getAllValues().contains(40));
    }

    @Test
    public void testGetNextButton() throws Exception {
        PanelItem nextButton = panel.callGetButton("NEXT");
        assertNotNull(nextButton);
    }

    @Test
    public void testGetPreviousButton() throws Exception {
        PanelItem prevButton = panel.callGetButton("PREVIOUS");
        assertNotNull(prevButton);
    }

    @Test
    public void testGetSearchButton() throws Exception {
        PanelItem searchButton = panel.callGetButton("SEARCH");
        assertNotNull(searchButton);
    }

    @Test
    public void testGetSearchButtonWithExistingSearchString() throws Exception {
        panel.searchString = "diamond";
        PanelItem searchButton = panel.callGetButton("SEARCH");
        assertNotNull(searchButton);
    }

    @Test
    public void testPopulateLastPageNoNextButton() throws Exception {
        panel.setPageIndex(1);
        List<String> elements = IntStream.range(0, 42).mapToObj(i -> "item" + i).toList();
        panel.callPopulateElements(panelBuilder, elements);
        ArgumentCaptor<Integer> slotCaptor = ArgumentCaptor.forClass(Integer.class);
        // 21 items + previous button + search button = 23
        verify(panelBuilder, times(23)).item(slotCaptor.capture(), any(PanelItem.class));
        assertTrue(slotCaptor.getAllValues().contains(18));
        assertEquals(1, panel.getPageIndex());
    }

    @Test
    public void testPopulateFirstPageNoPreviousButton() throws Exception {
        List<String> elements = IntStream.range(0, 42).mapToObj(i -> "item" + i).toList();
        panel.callPopulateElements(panelBuilder, elements);
        // First page: 21 items + next button + search button = 23
        ArgumentCaptor<Integer> slotCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(panelBuilder, times(23)).item(slotCaptor.capture(), any(PanelItem.class));
        assertTrue(slotCaptor.getAllValues().contains(26));
        assertEquals(0, panel.getPageIndex());
    }
}
