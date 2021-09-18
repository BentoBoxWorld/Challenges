package world.bentobox.challenges.commands.admin;


import java.util.List;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.panel.user.GameModePanel;


/**
 * This class provides all necessary thing to implement challenges admin command
 */
public class ChallengesAdminCommand extends CompositeCommand
{
	/**
	 * Constructor that inits command with given string.
	 * @param addon Challenges Addon
	 * @param commands String that contains main command and its alias separated via whitespace.
	 * @param gameModeAddons List with GameModes where challenges addon operates.
	 */
	public ChallengesAdminCommand(ChallengesAddon addon, String commands, List<GameModeAddon> gameModeAddons)
	{
		super(addon,
			commands.split(" ")[0],
			commands.split(" "));
		this.gameModeAddons = gameModeAddons;
		this.addon = addon;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setup()
	{
		this.setPermission("admin.challenges");
		this.setParametersHelp("challenges.commands.admin.main.parameters");
		this.setDescription("challenges.commands.admin.main.description");
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		// For single game mode just open correct gui.

		if (this.gameModeAddons.size() == 1)
		{
			this.gameModeAddons.get(0).getAdminCommand().ifPresent(compositeCommand ->
				user.performCommand(compositeCommand.getTopLabel() + " challenges"));
		}
		else
		{
			GameModePanel.open(this.addon,
				this.getWorld(),
				user,
				this.gameModeAddons,
				true);
		}

		return true;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * This variable stores challenges addon.
	 */
	private final ChallengesAddon addon;

	/**
	 * This variable stores List with game modes where challenges addon are hooked in.
	 */
	private final List<GameModeAddon> gameModeAddons;
}
