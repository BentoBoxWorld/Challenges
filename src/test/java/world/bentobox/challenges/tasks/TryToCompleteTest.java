package world.bentobox.challenges.tasks;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.bentobox.api.user.User;

/**
 * @author tastybento
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Bukkit.class})
public class TryToCompleteTest {

	private User user;
	ItemStack[] stacks = { new ItemStack(Material.PAPER, 32),
		new ItemStack(Material.ACACIA_BOAT),
		null,
		null,
		new ItemStack(Material.CACTUS, 32),
		new ItemStack(Material.CACTUS, 32),
		new ItemStack(Material.CACTUS, 32),
		new ItemStack(Material.BRICK_STAIRS, 64),
		new ItemStack(Material.BRICK_STAIRS, 64),
		new ItemStack(Material.BRICK_STAIRS, 5),
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

		Server server = mock(Server.class);
		ItemFactory itemFactory = mock(ItemFactory.class);
		when(server.getItemFactory()).thenReturn(itemFactory);

		// Test will not work with items that has meta data.
		when(itemFactory.getItemMeta(any())).thenReturn(null);
		when(itemFactory.equals(null, null)).thenReturn(true);

		PowerMockito.mockStatic(Bukkit.class);
		when(Bukkit.getServer()).thenReturn(server);

		when(Bukkit.getItemFactory()).thenReturn(itemFactory);
		when(Bukkit.getLogger()).thenReturn(Logger.getAnonymousLogger());
	}

	/**
	 * Test method for {@link TryToComplete#removeItems(java.util.List, int)}.
	 */
	@Test
	public void testRemoveItemsSuccess() {
		Material requiredMaterial = Material.PAPER;
		int requiredQuantity = 21;

		this.required.add(new ItemStack(requiredMaterial, requiredQuantity));
		TryToComplete x = new TryToComplete(this.addon);
		x.user(this.user);
		Map<ItemStack, Integer> removed = x.removeItems(this.required, 1);

		assertEquals((int) removed.getOrDefault(new ItemStack(requiredMaterial, 1), 0), requiredQuantity);
	}

	/**
	 * Test method for {@link TryToComplete#removeItems(java.util.List, int)}.
	 */
	@Test
	public void testRemoveItemsMax() {
		Material requiredMaterial = Material.PAPER;
		int requiredQuantity = 50;

		this.required.add(new ItemStack(requiredMaterial, requiredQuantity));
		TryToComplete x = new TryToComplete(this.addon);
		x.user(this.user);
		Map<ItemStack, Integer> removed = x.removeItems(this.required, 1);

		assertNotEquals((int) removed.getOrDefault(new ItemStack(requiredMaterial, 1), 0), requiredQuantity);
	}

	/**
	 * Test method for {@link TryToComplete#removeItems(java.util.List, int)}.
	 */
	@Test
	public void testRemoveItemsZero() {
		Material requiredMaterial = Material.PAPER;
		int requiredQuantity = 0;

		this.required.add(new ItemStack(requiredMaterial, requiredQuantity));
		TryToComplete x = new TryToComplete(this.addon);
		x.user(this.user);
		Map<ItemStack, Integer> removed = x.removeItems(this.required, 1);

		assertTrue(removed.isEmpty());
	}

	/**
	 * Test method for {@link TryToComplete#removeItems(java.util.List, int)}.
	 */
	@Test
	public void testRemoveItemsSuccessMultiple() {
		required.add(new ItemStack(Material.PAPER, 11));
		required.add(new ItemStack(Material.PAPER, 5));
		required.add(new ItemStack(Material.PAPER, 5));
		TryToComplete x = new TryToComplete(addon);
		x.user(user);
		Map<ItemStack, Integer> removed = x.removeItems(required, 1);

		assertEquals((int) removed.getOrDefault(new ItemStack(Material.PAPER, 1), 0), 21);
	}

	/**
	 * Test method for {@link TryToComplete#removeItems(java.util.List, int)}.
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
		Map<ItemStack, Integer> removed = x.removeItems(required, 1);

		assertEquals((int) removed.getOrDefault(new ItemStack(Material.PAPER, 1), 0), 21);
		assertEquals((int) removed.getOrDefault(new ItemStack(Material.CACTUS, 1), 0), 10);
	}

	/**
	 * Test method for {@link TryToComplete#removeItems(java.util.List, int)}.
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
		Map<ItemStack, Integer> removed = x.removeItems(required, 1);
		assertTrue(removed.isEmpty());
	}

	/**
	 * Test method for {@link TryToComplete#removeItems(java.util.List, int)}.
	 */
	@Test
	public void testRemoveItemsFail() {
		ItemStack input = new ItemStack(Material.GOLD_BLOCK, 55);
		required.add(input);
		TryToComplete x = new TryToComplete(addon);
		x.user(user);
		Map<ItemStack, Integer> removed = x.removeItems(required, 1);

		// It will remove 32, but not any more
		assertEquals((int) removed.getOrDefault(new ItemStack(Material.GOLD_BLOCK, 1), 0), 32);

		// An error will be thrown
		Mockito.verify(addon, Mockito.times(1)).logError(Mockito.anyString());
	}



	/**
	 * Test method for {@link TryToComplete#removeItems(java.util.List, int)}.
	 */
	@Test
	public void testRequireTwoStacks() {
		required.add(new ItemStack(Material.BRICK_STAIRS, 64));
		required.add(new ItemStack(Material.BRICK_STAIRS, 64));

		TryToComplete x = new TryToComplete(addon);
		x.user(user);
		Map<ItemStack, Integer> removed = x.removeItems(required, 1);

		// It should remove both stacks
		assertEquals((int) removed.getOrDefault(new ItemStack(Material.BRICK_STAIRS, 1), 0), 128);
	}


	/**
	 * Test method for {@link TryToComplete#removeItems(java.util.List, int)}.
	 */
	@Test
	public void testFactorStacks() {
		required.add(new ItemStack(Material.BRICK_STAIRS, 32));

		TryToComplete x = new TryToComplete(addon);
		x.user(user);
		Map<ItemStack, Integer> removed = x.removeItems(required, 4);

		// It should remove both stacks
		assertEquals((int) removed.getOrDefault(new ItemStack(Material.BRICK_STAIRS, 1), 0), 128);
	}
}

