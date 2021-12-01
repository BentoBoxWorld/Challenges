package world.bentobox.challenges.commands.admin;


import java.util.List;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.panel.user.GameModePanel;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;


/**
 * This class provides all necessary thing to implement challenges admin command
 */
public class ChallengesGlobalAdminCommand extends CompositeCommand
{
	/**
	 * Constructor that init command with given string.
	 * @param addon Challenges Addon
	 * @param gameModeAddons List with GameModes where challenges addon operates.
	 */
	public ChallengesGlobalAdminCommand(ChallengesAddon addon, List<GameModeAddon> gameModeAddons)
	{
		super(addon,
			addon.getChallengesSettings().getAdminGlobalCommand().split(" ")[0],
			addon.getChallengesSettings().getAdminGlobalCommand().split(" "));
		this.gameModeAddons = gameModeAddons;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setup()
	{
		this.setPermission("addon.admin.challenges");
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

		if (this.gameModeAddons.isEmpty())
		{
			Utils.sendMessage(user, user.getTranslation(Constants.ERRORS + "not-hooked"));
			return false;
		}
		else if (this.gameModeAddons.size() == 1)
		{
			this.gameModeAddons.get(0).getAdminCommand().ifPresent(compositeCommand ->
				user.performCommand(compositeCommand.getTopLabel() + " " +
					this.<ChallengesAddon>getAddon().getChallengesSettings().getAdminMainCommand().split(" ")[0]));
		}
		else
		{
			GameModePanel.open(this.getAddon(),
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
	 * This variable stores List with game modes where challenges addon are hooked in.
	 */
	private final List<GameModeAddon> gameModeAddons;
}
