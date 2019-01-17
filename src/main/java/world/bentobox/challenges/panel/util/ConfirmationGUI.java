package world.bentobox.challenges.panel.util;




import org.bukkit.Material;

import java.util.*;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.commands.CompositeCommand;
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
	 * @param user Gui Caller.
	 * @param parentGUI Parent GUI.
	 * @param commandLabels Command labels.
	 * @param variables Variables at the end of command.
	 */
	public ConfirmationGUI(BentoBox plugin,
		User user,
		CommonGUI parentGUI,
		List<String> commandLabels,
		String... variables)
	{
		this.plugin = plugin;
		this.user = user;
		this.parentGUI = parentGUI;
		this.commandLabels = commandLabels;
		this.variables = variables;

		if (this.commandLabels.isEmpty())
		{
			this.user.sendMessage("challenges.errors.missing-command");
		}
		else
		{
			this.build();
		}
	}


	/**
	 * This method builds confirmation panel with 2 buttons.
	 */
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().name(this.user.getTranslation("challenges.gui.admin.confirm-title"));

		panelBuilder.item(3, new PanelItemBuilder().
			name(this.user.getTranslation("challenges.gui.admin.buttons.proceed")).
			icon(Material.GREEN_STAINED_GLASS_PANE).
			clickHandler((panel, user1, clickType, index) ->
			{
				Iterator<String> iterator = this.commandLabels.iterator();

				CompositeCommand command = this.plugin.getCommandsManager().getCommand(iterator.next());

				while (iterator.hasNext() && command != null)
				{
					Optional<CompositeCommand> commandOptional = command.getSubCommand(iterator.next());

					if (commandOptional.isPresent())
					{
						command = commandOptional.get();
					}
					else
					{
						this.user.sendMessage("challenges.errors.missing-command");
						command = null;
					}
				}

				if (command != null)
				{
					command.execute(this.user, "CONFIRMATION", Arrays.asList(this.variables));
				}

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
	 * BentoBox plugin.
	 */
	private BentoBox plugin;

	/**
	 * User who wants to run command.
	 */
	private User user;

	/**
	 * Parent GUI where should return on cancel or proceed.
	 */
	private CommonGUI parentGUI;

	/**
	 * List of command labels.
	 */
	private List<String> commandLabels;

	/**
	 * List of variables.
	 */
	private String[] variables;
}
