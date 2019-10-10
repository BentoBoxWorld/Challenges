
package world.bentobox.challenges.panel.user;

import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.GuiUtils;


/**
 * This GUI will pop out when user uses right click on challenge. It is meant to choose
 * how many times player will complete challenges.
 */
public class MultipleGUI
{
	/**
	 * Default constructor.
	 * @param user User who opens gui.
	 * @param lineLength Length of lore message.
	 * @param action Action that will be performed on value clicking.
	 */
	public MultipleGUI(User user, int lineLength, Consumer<Integer> action)
	{
		this.user = user;
		this.lineLength = lineLength;
		this.action = action;

		this.build();
	}

	/**
	 * This method builds panel that allows to change given number value.
	 */
	private void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().
			user(this.user).
			type(Panel.Type.HOPPER).
			name(this.user.getTranslation("challenges.gui.title.multiple-complete"));

		panelBuilder.item(2, this.getButton(Button.VALUE));

		// Reduce
		panelBuilder.item(0, this.getButton(Button.REDUCE_LOT));
		panelBuilder.item(1, this.getButton(Button.REDUCE));

		// Increase
		panelBuilder.item(3, this.getButton(Button.INCREASE));
		panelBuilder.item(4, this.getButton(Button.INCREASE_LOT));

		panelBuilder.build();
	}


	/**
	 * This method creates PanelItem with required functionality.
	 * @param button Functionality requirement.
	 * @return PanelItem with functionality.
	 */
	private PanelItem getButton(Button button)
	{
		ItemStack icon;
		String name;
		String description;
		PanelItem.ClickHandler clickHandler;
		boolean glow;

		switch (button)
		{
			case VALUE:
			{
				name = this.user.getTranslation("challenges.gui.buttons.value");
				description = this.user.getTranslation("challenges.gui.descriptions.current-value", "[value]", Integer.toString(this.value));
				icon = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
				clickHandler = (panel, user, clickType, slot) -> {
					this.action.accept(this.value);
					return true;
				};
				glow = false;
				break;
			}
			case INCREASE:
			{
				name = this.user.getTranslation("challenges.gui.buttons.increase");
				description = this.user.getTranslation("challenges.gui.descriptions.increase-by", "[value]", "1");
				icon = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
				clickHandler = (panel, user, clickType, slot) -> {
					this.value++;
					// Necessary just to update second item
					panel.getInventory().setItem(2, this.getButton(Button.VALUE).getItem());
					return true;
				};
				glow = false;
				break;
			}
			case INCREASE_LOT:
			{
				name = this.user.getTranslation("challenges.gui.buttons.increase");
				description = this.user.getTranslation("challenges.gui.descriptions.increase-by", "[value]", "5");
				icon = new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE);
				clickHandler = (panel, user, clickType, slot) -> {
					this.value += 5;
					// Necessary just to update second item
					panel.getInventory().setItem(2, this.getButton(Button.VALUE).getItem());
					return true;
				};
				glow = false;
				break;
			}
			case REDUCE:
			{
				name = this.user.getTranslation("challenges.gui.buttons.reduce");
				description = this.user.getTranslation("challenges.gui.descriptions.reduce-by", "[value]", "1");
				icon = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
				clickHandler = (panel, user, clickType, slot) -> {
					this.value--;

					if (this.value < 1)
					{
						this.value = 1;
					}

					// Necessary just to update second item
					panel.getInventory().setItem(2, this.getButton(Button.VALUE).getItem());

					return true;
				};
				glow = false;
				break;
			}
			case REDUCE_LOT:
			{
				name = this.user.getTranslation("challenges.gui.buttons.reduce");
				description = this.user.getTranslation("challenges.gui.descriptions.reduce-by", "[value]", "5");
				icon = new ItemStack(Material.RED_STAINED_GLASS_PANE);
				clickHandler = (panel, user, clickType, slot) -> {
					this.value -= 5;

					if (this.value < 1)
					{
						this.value = 1;
					}

					// Necessary just to update second item
					panel.getInventory().setItem(2, this.getButton(Button.VALUE).getItem());

					return true;
				};
				glow = false;
				break;
			}
			default:
				return null;
		}

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(GuiUtils.stringSplit(description, this.lineLength)).
			glow(glow).
			clickHandler(clickHandler).
			build();
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * This enum allows to easier define available buttons.
	 */
	enum Button
	{
		VALUE,
		REDUCE,
		REDUCE_LOT,
		INCREASE,
		INCREASE_LOT
	}


// ---------------------------------------------------------------------
// Section: Instance variables
// ---------------------------------------------------------------------


	/**
	 * This variable allows to access to user object.
	 */
	private User user;

	/**
	 * This variable holds action that will be performed on accept.
	 */
	private Consumer<Integer> action;

	/**
	 * This variable holds a number of characters in single line for lore message.
	 */
	private int lineLength;

	/**
	 * This integer holds current value of completion count.
	 */
	private int value = 1;
}
