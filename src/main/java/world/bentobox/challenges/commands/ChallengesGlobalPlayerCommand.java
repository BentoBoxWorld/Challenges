package world.bentobox.challenges.commands;


import java.util.List;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.config.SettingsUtils.GuiMode;
import world.bentobox.challenges.panel.user.GameModePanel;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;


/**
 * This class provides all necessary thing to implement challenges user command
 */
public class ChallengesGlobalPlayerCommand extends CompositeCommand
{
	/**
	 * Constructor that init command with given string.
	 * @param addon Challenges Addon
	 * @param gameModeAddons List with GameModes where challenges addon operates.
	 */
	public ChallengesGlobalPlayerCommand(ChallengesAddon addon, List<GameModeAddon> gameModeAddons)
	{
		super(addon,
			addon.getChallengesSettings().getPlayerGlobalCommand().split(" ")[0],
			addon.getChallengesSettings().getPlayerGlobalCommand().split(" "));
		this.gameModeAddons = gameModeAddons;
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

		if (this.gameModeAddons.isEmpty())
		{
			Utils.sendMessage(user, user.getWorld(), Constants.ERRORS + "not-hooked");
			return false;
		}
		else if (this.gameModeAddons.size() == 1)
		{
			this.gameModeAddons.get(0).getPlayerCommand().ifPresent(compositeCommand ->
				user.performCommand(compositeCommand.getTopLabel() + " " +
					this.<ChallengesAddon>getAddon().getChallengesSettings().getPlayerMainCommand().split(" ")[0]));
			return true;
		}
		else if (this.<ChallengesAddon>getAddon().getChallengesSettings().getUserGuiMode() == GuiMode.CURRENT_WORLD)
		{
			// Find GameMode and run command
			for (GameModeAddon addon : this.gameModeAddons)
			{
				if (addon.inWorld(user.getWorld()))
				{
					addon.getPlayerCommand().ifPresent(compositeCommand ->
						user.performCommand(compositeCommand.getTopLabel() + " " +
							this.<ChallengesAddon>getAddon().getChallengesSettings().getPlayerMainCommand().split(" ")[0]));

					return true;
				}
			}

			Utils.sendMessage(user, user.getWorld(), "general.errors.wrong-world");
		}
		else if (this.<ChallengesAddon>getAddon().getChallengesSettings().getUserGuiMode() == GuiMode.GAMEMODE_LIST)
		{
			GameModePanel.open(this.getAddon(),
				this.getWorld(),
				user,
				this.gameModeAddons,
				false);
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
	private final List<GameModeAddon> gameModeAddons;
}
