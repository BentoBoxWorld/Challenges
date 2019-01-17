package world.bentobox.challenges.panel.util;


import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import java.util.Collections;
import java.util.List;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.PanelListener;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.user.User;


/**
 * This class allows to change Input ItemStacks to different ItemStacks.
 */
public class ItemGUI
{
	public ItemGUI(User user, List<ItemStack> itemStacks)
	{
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
		panelBuilder.listener(new CustomPanelListener());

		for (ItemStack itemStack : this.itemStacks)
		{
			panelBuilder.item(new CustomPanelItem(itemStack));
		}

		panelBuilder.build().open(this.user);
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
			event.setCancelled(false);
		}
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
}
