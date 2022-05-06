package world.bentobox.challenges.panel.util;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.PanelListener;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.Constants;


/**
 * This class allows to change Input ItemStacks to different ItemStacks.
 */
public record ItemSelector(User user, List<ItemStack> itemStacks, BiConsumer<Boolean, List<ItemStack>> consumer)
{
	/**
	 * This method opens GUI that allows to select challenge type.
	 *
	 * @param user User who opens GUI.
	 * @param consumer Consumer that allows to get clicked type.
	 */
	public static void open(User user, List<ItemStack> itemStacks, BiConsumer<Boolean, List<ItemStack>> consumer)
	{
		new ItemSelector(user, itemStacks, consumer).build();
	}


	/**
	 * This method builds panel that allows to change given number value.
	 */
	private void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().
			user(this.user).
			name(this.user.getTranslation(Constants.TITLE + "item-selector"));

		// Size of inventory that user can set via GUI.
		panelBuilder.size(45);

		panelBuilder.listener(new CustomPanelListener());

		panelBuilder.item(0, this.getButton(Button.SAVE));

		for (int i = 1; i < 8; i++)
		{
			panelBuilder.item(i, this.getButton(Button.EMPTY));
		}

		panelBuilder.item(8, this.getButton(Button.CANCEL));

		for (ItemStack itemStack : this.itemStacks)
		{
			panelBuilder.item(new CustomPanelItem(itemStack));
		}

		panelBuilder.build().open(this.user);
	}


	/**
	 * This method create button that does some functionality in current gui.
	 *
	 * @param button Button functionality.
	 * @return PanelItem.
	 */
	private PanelItem getButton(Button button)
	{
		final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";

		String name = this.user.getTranslation(reference + "name");
		final List<String> description = new ArrayList<>(3);
		description.add(this.user.getTranslation(reference + "description"));

		ItemStack icon;
		PanelItem.ClickHandler clickHandler;

		switch (button)
		{
			case SAVE -> {
				icon = new ItemStack(Material.COMMAND_BLOCK);
				clickHandler = (panel, user, clickType, slot) ->
				{
					// Magic number 9 - second row. First row is for custom buttons.
					// Magic number 45 - This GUI is initialed with 45 elements.
					List<ItemStack> returnItems = new ArrayList<>(36);

					for (int i = 9; i < 45; i++)
					{
						ItemStack itemStack = panel.getInventory().getItem(i);

						if (itemStack != null)
						{
							returnItems.add(itemStack);
						}
					}

					this.consumer.accept(true, returnItems);

					return true;
				};

				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-save"));
			}
			case CANCEL -> {
				icon = new ItemStack(Material.IRON_DOOR);
				clickHandler = (panel, user, clickType, slot) ->
				{
					this.consumer.accept(false, Collections.emptyList());
					return true;
				};

				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-cancel"));
			}
			case EMPTY -> {
				description.clear();
				name = "&r";
				icon = new ItemStack(Material.BARRIER);
				clickHandler = null;
			}
			default -> {
				icon = new ItemStack(Material.PAPER);
				clickHandler = null;
			}
		}

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(description).
			clickHandler(clickHandler).
			build();
	}


// ---------------------------------------------------------------------
// Section: Private classes
// ---------------------------------------------------------------------


	/**
	 * This CustomPanelItem does no lose Item original MetaData. After PanelItem has been created it
	 * restores original meta data. It also does not allow to change anything that could destroy meta data.
	 */
	private static class CustomPanelItem extends PanelItem
	{
		CustomPanelItem(ItemStack item)
		{
			super(new PanelItemBuilder().
				icon(item.clone()).
				name("").
				description(Collections.emptyList()).
				glow(false).
				clickHandler(null));

			this.getItem().setItemMeta(item.getItemMeta());
		}


		@Override
		public void setGlow(boolean glow)
		{
		}


		@Override
		public void setDescription(List<String> description)
		{
		}


		@Override
		public void setName(String name)
		{
		}


		@Override
		public void setHead(ItemStack itemStack)
		{
		}
	}


	/**
	 * This CustomPanelListener allows to move items in current panel.
	 */
	private static class CustomPanelListener implements PanelListener
	{
		@Override
		public void setup()
		{
		}


		@Override
		public void onInventoryClose(InventoryCloseEvent inventoryCloseEvent)
		{
		}


		@Override
		public void onInventoryClick(User user, InventoryClickEvent event)
		{
			// First row of elements should be ignored, as it contains buttons and blocked slots.
			event.setCancelled(event.getRawSlot() < 9);
		}
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * This enum holds all button values in current gui.
	 */
	private enum Button
	{
		CANCEL,
		SAVE,
		EMPTY
	}
}
