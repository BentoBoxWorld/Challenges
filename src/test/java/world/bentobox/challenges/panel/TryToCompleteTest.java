/**
 *
 */
package world.bentobox.challenges.panel;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.bentobox.api.user.User;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
public class TryToCompleteTest {

    private User user;
    ItemStack[] stacks = { new ItemStack(Material.PAPER, 32),
            new ItemStack(Material.ACACIA_BOAT),
            null,
            null,
            new ItemStack(Material.CACTUS, 32),
            new ItemStack(Material.CACTUS, 32),
            new ItemStack(Material.CACTUS, 32),
            new ItemStack(Material.GOLD_BLOCK, 32)
    };
    List<ItemStack> required;
    private ChallengesAddon addon;
    private PlayerInventory inv;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        user = mock(User.class);
        inv = mock(PlayerInventory.class);
        when(inv.getContents()).thenReturn(stacks);
        when(user.getInventory()).thenReturn(inv);
        addon = mock(ChallengesAddon.class);
        required = new ArrayList<>();
    }

    /**
     * Test method for {@link world.bentobox.challenges.panel.TryToComplete#removeItems(java.util.List)}.
     */
    @Test
    public void testRemoveItemsSuccess() {
        Material reqMat = Material.PAPER;
        int reqQty = 21;
        required.add(new ItemStack(reqMat, reqQty));
        TryToComplete x = new TryToComplete(addon);
        x.user(user);
        Map<Material, Integer> removed = x.removeItems(required);
        assertTrue(removed.get(reqMat) == reqQty);
    }

    /**
     * Test method for {@link world.bentobox.challenges.panel.TryToComplete#removeItems(java.util.List)}.
     */
    @Test
    public void testRemoveItemsMax() {
        Material reqMat = Material.PAPER;
        int reqQty = 50;
        required.add(new ItemStack(reqMat, reqQty));
        TryToComplete x = new TryToComplete(addon);
        x.user(user);
        Map<Material, Integer> removed = x.removeItems(required);
        assertTrue(removed.get(reqMat) == 32);
    }

    /**
     * Test method for {@link world.bentobox.challenges.panel.TryToComplete#removeItems(java.util.List)}.
     */
    @Test
    public void testRemoveItemsZero() {
        Material reqMat = Material.PAPER;
        int reqQty = 0;
        required.add(new ItemStack(reqMat, reqQty));
        TryToComplete x = new TryToComplete(addon);
        x.user(user);
        Map<Material, Integer> removed = x.removeItems(required);
        assertTrue(removed.get(reqMat) == null);
    }

    /**
     * Test method for {@link world.bentobox.challenges.panel.TryToComplete#removeItems(java.util.List)}.
     */
    @Test
    public void testRemoveItemsSuccessMultiple() {
        required.add(new ItemStack(Material.PAPER, 11));
        required.add(new ItemStack(Material.PAPER, 5));
        required.add(new ItemStack(Material.PAPER, 5));
        TryToComplete x = new TryToComplete(addon);
        x.user(user);
        Map<Material, Integer> removed = x.removeItems(required);
        assertTrue(removed.get(Material.PAPER) == 21);
    }

    /**
     * Test method for {@link world.bentobox.challenges.panel.TryToComplete#removeItems(java.util.List)}.
     */
    @Test
    public void testRemoveItemsSuccessMultipleOther() {
        required.add(new ItemStack(Material.CACTUS, 5));
        required.add(new ItemStack(Material.PAPER, 11));
        required.add(new ItemStack(Material.PAPER, 5));
        required.add(new ItemStack(Material.PAPER, 5));
        required.add(new ItemStack(Material.CACTUS, 5));
        TryToComplete x = new TryToComplete(addon);
        x.user(user);
        Map<Material, Integer> removed = x.removeItems(required);
        assertTrue(removed.get(Material.PAPER) == 21);
        assertTrue(removed.get(Material.CACTUS) == 10);
    }

    /**
     * Test method for {@link world.bentobox.challenges.panel.TryToComplete#removeItems(java.util.List)}.
     */
    @Test
    public void testRemoveItemsMultipleOtherFail() {
        required.add(new ItemStack(Material.ACACIA_FENCE, 5));
        required.add(new ItemStack(Material.ARROW, 11));
        required.add(new ItemStack(Material.STONE, 5));
        required.add(new ItemStack(Material.BAKED_POTATO, 5));
        required.add(new ItemStack(Material.GHAST_SPAWN_EGG, 5));
        TryToComplete x = new TryToComplete(addon);
        x.user(user);
        Map<Material, Integer> removed = x.removeItems(required);
        assertTrue(removed.isEmpty());

    }

    /**
     * Test method for {@link world.bentobox.challenges.panel.TryToComplete#removeItems(java.util.List)}.
     */
    @Test
    public void testRemoveItemsFail() {
        required.add(new ItemStack(Material.GOLD_BLOCK, 55));
        TryToComplete x = new TryToComplete(addon);
        x.user(user);
        Map<Material, Integer> removed = x.removeItems(required);
        // It will remove 32, but not any more
        assertTrue(removed.get(Material.GOLD_BLOCK) == 32);
        // An error will be thrown
        Mockito.verify(addon, Mockito.times(1)).logError(Mockito.anyString());
    }
}
