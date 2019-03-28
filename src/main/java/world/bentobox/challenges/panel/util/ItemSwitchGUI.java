package world.bentobox.challenges.panel.util;


import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.PanelListener;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.GuiUtils;


/**
 * This class allows to change Input ItemStacks to different ItemStacks.
 */
public class ItemSwitchGUI
{
	public ItemSwitchGUI(User user, List<ItemStack> itemStacks, int lineLength, BiConsumer<Boolean, List<ItemStack>> consumer)
	{
		this.consumer = consumer;
		this.user = user;
		this.itemStacks = itemStacks;
		this.lineLength = lineLength;
		this.build();
	}


	/**
	 * This method builds panel that allows to change given number value.
	 */
	private void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(this.user.getTranslation("challenges.gui.title.admin.manage-items"));

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
	 * @param button Button functionality.
	 * @return PanelItem.
	 */
	private PanelItem getButton(Button button)
	{
		ItemStack icon;
		String name;
		List<String> description;
		PanelItem.ClickHandler clickHandler;

		switch (button)
		{
			case SAVE:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.save");
				description = Collections.emptyList();
				icon = new ItemStack(Material.COMMAND_BLOCK);
				clickHandler = (panel, user, clickType, slot) -> {
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
				break;
			}
			case CANCEL:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.cancel");
				description = Collections.emptyList();
				icon = new ItemStack(Material.IRON_DOOR);
				clickHandler = (panel, user, clickType, slot) -> {
					this.consumer.accept(false, Collections.emptyList());
					return true;
				};
				break;
			}
			case EMPTY:
			{
				name = "";
				description = Collections.emptyList();
				icon = new ItemStack(Material.BARRIER);
				clickHandler = (panel, user, clickType, slot) -> true;
				break;
			}
			default:
				return null;
		}

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(GuiUtils.stringSplit(description, this.lineLength)).
			glow(false).
			clickHandler(clickHandler).
			build();
	}


// ---------------------------------------------------------------------
// Section: Private classes
// ---------------------------------------------------------------------


	/**
	 * This CustomPanelItem does no lose Item original MetaData. After PanelItem has been
	 * created it restores original meta data. It also does not allow to change anything that
	 * could destroy meta data.
	 */
	private class CustomPanelItem extends PanelItem
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
	private class CustomPanelListener implements PanelListener
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


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * User who opens current gui.
	 */
	private User user;

	/**
	 * List with original items.
	 */
	private List<ItemStack> itemStacks;

	/**
	 * Consumer that returns item stacks on save action.
	 */
	private BiConsumer<Boolean, List<ItemStack>> consumer;

	/**
	 * This variable stores how large line can be, before warp it.
	 */
	private int lineLength;
}
