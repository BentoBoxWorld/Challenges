package world.bentobox.challenges.panel.util;


import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.PanelListener;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.panel.CommonGUI;


/**
 * This class allows to change Input ItemStacks to different ItemStacks.
 */
public class ItemSwitchGUI
{
	public ItemSwitchGUI(CommonGUI parentGUI, User user, List<ItemStack> itemStacks)
	{
		this.parentGUI = parentGUI;
		this.user = user;
		this.itemStacks = itemStacks;
		this.build();
	}


	/**
	 * This method builds panel that allows to change given number value.
	 */
	private void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().name(this.user.getTranslation("challenges.gui.change-items"));

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
				name = this.user.getTranslation("challenges.gui.buttons.save");
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

					this.parentGUI.setValue(returnItems);
					this.user.closeInventory();
					this.parentGUI.build();

					return true;
				};
				break;
			}
			case CANCEL:
			{
				name = this.user.getTranslation("challenges.gui.buttons.cancel");
				description = Collections.emptyList();
				icon = new ItemStack(Material.IRON_DOOR);
				clickHandler = (panel, user, clickType, slot) -> {
					this.parentGUI.build();
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

		return new PanelItem(icon, name, description, false, clickHandler, false);
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
			super(item.clone(), "", Collections.emptyList(), false, null, false);
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
	 * ParentGUI from which current gui is called.
	 */
	private CommonGUI parentGUI;

	/**
	 * User who opens current gui.
	 */
	private User user;

	/**
	 * List with original items.
	 */
	private List<ItemStack> itemStacks;
}
