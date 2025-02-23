package world.bentobox.challenges.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.challenges.config.SettingsUtils.VisibilityMode;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class})
public class UtilsTest {
    
    @Mock
    private IslandWorldManager iwm;
    @Mock
    private GameModeAddon gameModeAddon;


    /**
     */
    @Before
    public void setUp() {
        // Set up plugin
        BentoBox plugin = mock(BentoBox.class);
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);
        
        // Mock item factory (for itemstacks)
        PowerMockito.mockStatic(Bukkit.class);
        ItemFactory itemFactory = mock(ItemFactory.class);
        when(Bukkit.getItemFactory()).thenReturn(itemFactory);

        // IWM getAddon        
        AddonDescription desc = new AddonDescription.Builder("main", "name", "1.0").build();
        when(gameModeAddon.getDescription()).thenReturn(desc);
        Optional<GameModeAddon> optionalAddon = Optional.of(gameModeAddon);
        when(iwm.getAddon(any())).thenReturn(optionalAddon);
        when(plugin.getIWM()).thenReturn(iwm);

    }

    /**
     * Test method for {@link world.bentobox.challenges.utils.Utils#groupEqualItems(java.util.List, java.util.Set)}.
     */
    @Test
    public void testGroupEqualItemsEmpty() {
        assertTrue(Utils.groupEqualItems(Collections.emptyList(), Collections.emptySet()).isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.challenges.utils.Utils#groupEqualItems(java.util.List, java.util.Set)}.
     */
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
        // Result should be two stacks stack of 64 doors and 36 doors
        assertEquals(1, list.size());
        verify(is, times(9)).setAmount(2);
    }

    /**
     * Test method for {@link world.bentobox.challenges.utils.Utils#groupEqualItems(java.util.List, java.util.Set)}.
     */
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
            when(is2.getType()).thenReturn(Material.values()[i+20]);
            when(is2.getMaxStackSize()).thenReturn(64);
            when(is2.isSimilar(any())).thenReturn(false);
            when(is2.clone()).thenReturn(is);
            requiredItems.add(is2);
        }
        List<ItemStack> list = Utils.groupEqualItems(requiredItems, Collections.emptySet());
        // Result should be two stacks stack of 64 doors and 36 doors
        assertEquals(10, list.size());
        verify(is, never()).setAmount(2);
    }

    /**
     * Test method for {@link world.bentobox.challenges.utils.Utils#getGameMode(org.bukkit.World)}.
     */
    @Test
    public void testGetGameModeNoGameMode() {
        when(iwm.getAddon(any())).thenReturn(Optional.empty());
        assertNull(Utils.getGameMode(mock(World.class)));
    }
    
    /**
     * Test method for {@link world.bentobox.challenges.utils.Utils#getGameMode(org.bukkit.World)}.
     */
    @Test
    public void testGetGameMode() {
        assertEquals("name", Utils.getGameMode(mock(World.class)));
    }

    /**
     * Test method for {@link world.bentobox.challenges.utils.Utils#getNextValue(T[], java.lang.Object)}.
     */
    @Test
    public void testGetNextValue() {
        assertEquals(VisibilityMode.HIDDEN, Utils.getNextValue(VisibilityMode.values(), VisibilityMode.VISIBLE));
        assertEquals(VisibilityMode.TOGGLEABLE, Utils.getNextValue(VisibilityMode.values(), VisibilityMode.HIDDEN));
        assertEquals(VisibilityMode.VISIBLE, Utils.getNextValue(VisibilityMode.values(), VisibilityMode.TOGGLEABLE));
    }

    /**
     * Test method for {@link world.bentobox.challenges.utils.Utils#getPreviousValue(T[], java.lang.Object)}.
     */
    @Test
    public void testGetPreviousValue() {
        assertEquals(VisibilityMode.TOGGLEABLE, Utils.getPreviousValue(VisibilityMode.values(), VisibilityMode.VISIBLE));
        assertEquals(VisibilityMode.VISIBLE, Utils.getPreviousValue(VisibilityMode.values(), VisibilityMode.HIDDEN));
        assertEquals(VisibilityMode.HIDDEN, Utils.getPreviousValue(VisibilityMode.values(), VisibilityMode.TOGGLEABLE));
    }
}
