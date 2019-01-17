package world.bentobox.challenges.panel.util;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.panel.CommonGUI;


/**
 * This GUI allows to edit List of strings. AnvilGUI has limited text space, so splitting
 * text in multiple rows allows to edit each row separately.
 */
public class StringListGUI
{
	public StringListGUI(CommonGUI parentGUI,
		User user,
		List<String> value,
		CompositeCommand command,
		String... parameters)
	{
		this.parentGUI = parentGUI;
		this.user = user;
		this.value = value;
		this.command = command;
		this.parameters = parameters;

		if (this.value.size() > 18)
		{
			// TODO: throw error that so large list cannot be edited.
			this.parentGUI.build();
		}
		else
		{
			this.build();
		}
	}


	/**
	 * This method builds panel that allows to change given string value.
	 */
	private void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().name(this.user.getTranslation("challenges.gui.text-edit-title"));

		panelBuilder.item(0, this.getButton(Button.SAVE));
		panelBuilder.item(1, this.getButton(Button.VALUE));

		panelBuilder.item(3, this.getButton(Button.ADD));
		panelBuilder.item(4, this.getButton(Button.REMOVE));
		panelBuilder.item(4, this.getButton(Button.CLEAR));

		panelBuilder.item(8, this.getButton(Button.CANCEL));

		for (String element : this.value)
		{
			panelBuilder.item(this.createStringElement(element));
		}

		panelBuilder.build();
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
					List<String> values = Arrays.asList(this.parameters);
					values.addAll(this.value);

					if (this.command.execute(this.user, "STRING_LIST_GUI", values))
					{
						this.user.closeInventory();
						this.parentGUI.build();
					}
					else
					{
						this.build();
					}

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
			case VALUE:
			{
				name = this.user.getTranslation("challenges.gui.buttons.value");
				description = this.value;
				icon = new ItemStack(Material.PAPER);
				clickHandler = (panel, user, clickType, slot) -> true;
				break;
			}
			case ADD:
			{
				name = this.user.getTranslation("challenges.gui.buttons.add");
				description = Collections.emptyList();
				icon = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
				clickHandler = (panel, user, clickType, slot) -> {

					// TODO: Open Anvil GUI.

					this.build();
					return true;
				};
				break;
			}
			case CLEAR:
			{
				name = this.user.getTranslation("challenges.gui.buttons.clear");
				description = Collections.emptyList();
				icon = new ItemStack(Material.RED_STAINED_GLASS_PANE);
				clickHandler = (panel, user, clickType, slot) -> {
					this.value.clear();
					this.build();
					return true;
				};
				break;
			}
			case REMOVE:
			{
				name = this.user.getTranslation("challenges.gui.buttons.remove");
				description = Collections.emptyList();
				icon = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
				clickHandler = (panel, user, clickType, slot) -> {
					this.value.removeIf(String::isEmpty);

					this.build();
					return true;
				};
				break;
			}
			default:
				return null;
		}

		return new PanelItem(icon, name, description, false, clickHandler, false);
	}


	/**
	 * This method creates paper icon that represents single line from list.
	 * @param element Paper Icon name
	 * @return PanelItem.
	 */
	private PanelItem createStringElement(String element)
	{
		return new PanelItemBuilder().
			name(element).
			icon(Material.PAPER).
			clickHandler((panel, user1, clickType, i) -> {
			// TODO: open anvil gui.
			return true;
		}).build();
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * This enum holds all button values in current gui.
	 */
	private enum Button
	{
		VALUE,
		ADD,
		REMOVE,
		CANCEL,
		CLEAR,
		SAVE
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * This variable stores return GUI.
	 */
	private CommonGUI parentGUI;

	/**
	 * User who runs GUI.
	 */
	private User user;

	/**
	 * Current value.
	 */
	private List<String> value;

	/**
	 * Command that must be processed on save.
	 */
	private CompositeCommand command;

	/**
	 * Command input parameters before number.
	 */
	private String[] parameters;
}
