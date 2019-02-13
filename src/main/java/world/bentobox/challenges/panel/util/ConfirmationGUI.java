package world.bentobox.challenges.panel.util;


import org.bukkit.Material;

import java.util.function.Consumer;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.GuiUtils;


/**
 * This GUI is used to confirm that user wants to run command, that should be created from
 * command string list.
 */
public class ConfirmationGUI
{
	/**
	 * This constructor inits and opens ConfirmationGUI.
	 *
	 * @param user Gui Caller.
	 */
	public ConfirmationGUI(User user, Consumer<Boolean> consumer)
	{
		this.user = user;
		this.consumer = consumer;

		this.build();
	}


	/**
	 * This method builds confirmation panel with 2 buttons.
	 */
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(this.user.getTranslation("challenges.gui.title.admin.confirm-title"));

		GuiUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

		// Accept buttons
		panelBuilder.item(10, this.getButton(true));
		panelBuilder.item(11, this.getButton(true));
		panelBuilder.item(12, this.getButton(true));

		panelBuilder.item(19, this.getButton(true));
		panelBuilder.item(20, this.getButton(true));
		panelBuilder.item(21, this.getButton(true));

		panelBuilder.item(28, this.getButton(true));
		panelBuilder.item(29, this.getButton(true));
		panelBuilder.item(30, this.getButton(true));

		// Cancel Buttons
		panelBuilder.item(14, this.getButton(false));
		panelBuilder.item(15, this.getButton(false));
		panelBuilder.item(16, this.getButton(false));

		panelBuilder.item(23, this.getButton(false));
		panelBuilder.item(24, this.getButton(false));
		panelBuilder.item(25, this.getButton(false));

		panelBuilder.item(32, this.getButton(false));
		panelBuilder.item(33, this.getButton(false));
		panelBuilder.item(34, this.getButton(false));

		panelBuilder.item(44,
			new PanelItemBuilder().
				icon(Material.OAK_DOOR).
				name(this.user.getTranslation("challenges.gui.buttons.return")).
				clickHandler( (panel, user1, clickType, slot) -> {
					this.consumer.accept(false);
					return true;
				}).build());

		panelBuilder.build();
	}


	/**
	 * This method creates button with requested value.
	 * @param returnValue requested value
	 * @return PanelItem button.
	 */
	private PanelItem getButton(boolean returnValue)
	{
		return new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.buttons.admin." + (returnValue ? "accept" : "cancel"))).
			icon(returnValue ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE).
			clickHandler((panel, user1, clickType, i) -> {
				this.consumer.accept(returnValue);
				return true;
			}).
			build();
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * User who wants to run command.
	 */
	private User user;

	/**
	 * Stores current Consumer
	 */
	private Consumer<Boolean> consumer;
}
