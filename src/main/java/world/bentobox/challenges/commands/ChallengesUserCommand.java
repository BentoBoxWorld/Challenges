package world.bentobox.challenges.commands;


import java.util.List;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.Settings;
import world.bentobox.challenges.panel.GameModesGUI;


/**
 * This class provides all necessary thing to implement challenges user command
 */
public class ChallengesUserCommand extends CompositeCommand
{
	/**
	 * Constructor that inits command with given string.
	 * @param addon Challenges Addon
	 * @param commands String that contains main command and its alias separated via whitespace.
	 * @param gameModeAddons List with GameModes where challenges addon operates.
	 */
	public ChallengesUserCommand(ChallengesAddon addon, String commands, List<GameModeAddon> gameModeAddons)
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
		this.setOnlyPlayer(true);
		this.setPermission("challenges");
		this.setParametersHelp("challenges.commands.user.main.parameters");
		this.setDescription("challenges.commands.user.main.description");
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		// It is not necessary to check 0, as in that case addon will not be hooked.

		if (this.gameModeAddons.size() == 1)
		{
			this.gameModeAddons.get(0).getPlayerCommand().ifPresent(compositeCommand ->
				user.performCommand(compositeCommand.getTopLabel() + " challenges"));
			return true;
		}
		else if (this.addon.getChallengesSettings().getUserGuiMode() == Settings.GuiMode.CURRENT_WORLD)
		{
			// Find GameMode and run command
			for (GameModeAddon addon : this.gameModeAddons)
			{
				if (addon.inWorld(user.getWorld()))
				{
					addon.getPlayerCommand().ifPresent(compositeCommand ->
						user.performCommand(compositeCommand.getTopLabel() + " challenges"));

					return true;
				}
			}
		}
		else if (this.addon.getChallengesSettings().getUserGuiMode() == Settings.GuiMode.GAMEMODE_LIST)
		{
			new GameModesGUI(this.addon,
				this.getWorld(),
				user,
				this.getTopLabel(),
				this.getPermissionPrefix(),
				false,
				this.gameModeAddons).build();
			return true;
		}

		return false;
	}


	// ---------------------------------------------------------------------
	// Section: Variables
	// ---------------------------------------------------------------------


	/**
	 * List with hooked GameMode addons.
	 */
	private List<GameModeAddon> gameModeAddons;

	/**
	 * Challenges addon for easier operations.
	 */
	private ChallengesAddon addon;
}
