package world.bentobox.challenges.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.challenges.WhiteBox;
import world.bentobox.challenges.config.SettingsUtils.VisibilityMode;

/**
 * @author tastybento
 */
public class UtilsTest {

    @Mock
    private IslandWorldManager iwm;
    @Mock
    private GameModeAddon gameModeAddon;

    private AutoCloseable closeable;
    private MockedStatic<Bukkit> mockedBukkit;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        // Set up plugin
        BentoBox plugin = mock(BentoBox.class);
        WhiteBox.setInternalState(BentoBox.class, "instance", plugin);

        // Mock item factory (for itemstacks)
        mockedBukkit = Mockito.mockStatic(Bukkit.class);
        ItemFactory itemFactory = mock(ItemFactory.class);
        mockedBukkit.when(Bukkit::getItemFactory).thenReturn(itemFactory);

        // IWM getAddon
        AddonDescription desc = new AddonDescription.Builder("main", "name", "1.0").build();
        when(gameModeAddon.getDescription()).thenReturn(desc);
        Optional<GameModeAddon> optionalAddon = Optional.of(gameModeAddon);
        when(iwm.getAddon(any())).thenReturn(optionalAddon);
        when(plugin.getIWM()).thenReturn(iwm);
    }

    @AfterEach
    public void tearDown() throws Exception {
        mockedBukkit.closeOnDemand();
        closeable.close();
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void testGroupEqualItemsEmpty() {
        assertTrue(Utils.groupEqualItems(Collections.emptyList(), Collections.emptySet()).isEmpty());
    }

    @Test
    public void testGroupEqualItems() {
        List<ItemStack> requiredItems = new ArrayList<>();
        Set<Material> ignoreMeta = Collections.singleton(Material.ACACIA_FENCE);
        // First item
        ItemStack is = mock(ItemStack.class);
        when(is.getAmount()).thenReturn(1);
        when(is.getType()).thenReturn(Material.ACACIA_FENCE);
        when(is.getMaxStackSize()).thenReturn(64);
        when(is.isSimilar(any())).thenReturn(true);
        when(is.clone()).thenReturn(is);
        requiredItems.add(is);
        for (int i = 0; i < 9; i++) {
            ItemStack is2 = mock(ItemStack.class);
            when(is2.getAmount()).thenReturn(1);
            when(is2.getType()).thenReturn(Material.ACACIA_FENCE);
            when(is2.getMaxStackSize()).thenReturn(64);
            when(is2.isSimilar(any())).thenReturn(true);
            when(is2.clone()).thenReturn(is);
            requiredItems.add(is2);
        }
        List<ItemStack> list = Utils.groupEqualItems(requiredItems, ignoreMeta);
        assertEquals(1, list.size());
        verify(is, times(9)).setAmount(2);
    }

    @Test
    public void testGroupEqualItemsUnique() {
        List<ItemStack> requiredItems = new ArrayList<>();
        // First item
        ItemStack is = mock(ItemStack.class);
        when(is.getAmount()).thenReturn(1);
        when(is.getType()).thenReturn(Material.ACACIA_FENCE);
        when(is.getMaxStackSize()).thenReturn(64);
        when(is.isSimilar(any())).thenReturn(false);
        when(is.clone()).thenReturn(is);
        requiredItems.add(is);
        for (int i = 0; i < 9; i++) {
            ItemStack is2 = mock(ItemStack.class);
            when(is2.getAmount()).thenReturn(1);
            when(is2.getType()).thenReturn(Material.values()[i + 20]);
            when(is2.getMaxStackSize()).thenReturn(64);
            when(is2.isSimilar(any())).thenReturn(false);
            when(is2.clone()).thenReturn(is);
            requiredItems.add(is2);
        }
        List<ItemStack> list = Utils.groupEqualItems(requiredItems, Collections.emptySet());
        assertEquals(10, list.size());
        verify(is, never()).setAmount(2);
    }

    @Test
    public void testGetGameModeNoGameMode() {
        when(iwm.getAddon(any())).thenReturn(Optional.empty());
        assertNull(Utils.getGameMode(mock(World.class)));
    }

    @Test
    public void testGetGameMode() {
        assertEquals("name", Utils.getGameMode(mock(World.class)));
    }

    @Test
    public void testGetNextValue() {
        assertEquals(VisibilityMode.HIDDEN, Utils.getNextValue(VisibilityMode.values(), VisibilityMode.VISIBLE));
        assertEquals(VisibilityMode.TOGGLEABLE, Utils.getNextValue(VisibilityMode.values(), VisibilityMode.HIDDEN));
        assertEquals(VisibilityMode.VISIBLE, Utils.getNextValue(VisibilityMode.values(), VisibilityMode.TOGGLEABLE));
    }

    @Test
    public void testGetPreviousValue() {
        assertEquals(VisibilityMode.TOGGLEABLE, Utils.getPreviousValue(VisibilityMode.values(), VisibilityMode.VISIBLE));
        assertEquals(VisibilityMode.VISIBLE, Utils.getPreviousValue(VisibilityMode.values(), VisibilityMode.HIDDEN));
        assertEquals(VisibilityMode.HIDDEN, Utils.getPreviousValue(VisibilityMode.values(), VisibilityMode.TOGGLEABLE));
    }
}
