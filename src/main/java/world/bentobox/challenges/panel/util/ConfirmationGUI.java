package world.bentobox.challenges.panel.util;


import org.bukkit.Material;

import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.panel.CommonGUI;


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
	 * @param parentGUI Parent GUI.
	 */
	public ConfirmationGUI(CommonGUI parentGUI, User user)
	{
		this.user = user;
		this.parentGUI = parentGUI;

		this.build();
	}


	/**
	 * This method builds confirmation panel with 2 buttons.
	 */
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder()
			.name(this.user.getTranslation("challenges.gui.admin.confirm-title"));

		panelBuilder.item(3, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.admin.buttons.proceed")).
			icon(Material.GREEN_STAINED_GLASS_PANE).
			clickHandler((panel, user1, clickType, index) -> {
				this.parentGUI.setValue(true);
				this.user.closeInventory();
				this.parentGUI.build();
				return true;
			}).
			build());

		panelBuilder.item(5, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.admin.buttons.cancel")).
			icon(Material.RED_STAINED_GLASS_PANE).
			clickHandler((panel, user1, clickType, i) ->
			{
				this.parentGUI.setValue(null);
				this.parentGUI.build();
				return true;
			}).
			build());

		panelBuilder.build();
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * User who wants to run command.
	 */
	private User user;

	/**
	 * Parent GUI where should return on cancel or proceed.
	 */
	private CommonGUI parentGUI;
}
