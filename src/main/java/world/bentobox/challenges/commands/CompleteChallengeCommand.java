package world.bentobox.challenges.commands;


import java.util.*;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.tasks.TryToComplete;
import world.bentobox.challenges.utils.Utils;


/**
 * This command allows to complete challenges without a gui.
 */
public class CompleteChallengeCommand extends CompositeCommand
{
	/**
	 * Default constructor for Composite Command.
	 * @param addon Challenges addon.
	 * @param cmd Parent Command.
	 */
	public CompleteChallengeCommand(Addon addon, CompositeCommand cmd)
	{
		super(addon, cmd, "complete");
		this.addon = (ChallengesAddon) addon;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setup()
	{
		this.setOnlyPlayer(true);
		this.setPermission("complete");
		this.setParametersHelp("challenges.commands.user.complete.parameters");
		this.setDescription("challenges.commands.user.complete.description");
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		if (args.isEmpty())
		{
			user.sendMessage("challenges.errors.no-name");
			this.showHelp(this, user);
			return false;
		}
		else if (!args.get(0).isEmpty())
		{
			// Add world name back at the start
			String challengeName = Utils.getGameMode(this.getWorld()) + "_" + args.get(0);
			Challenge challenge = this.addon.getChallengesManager().getChallenge(challengeName);

			int count = args.size() == 2 ? Integer.valueOf(args.get(1)) : 1;

			if (challenge != null)
			{
				return TryToComplete.complete(this.addon,
					user,
					challenge,
					this.getWorld(),
					this.getTopLabel(),
					this.getPermissionPrefix(),
					count);
			}
			else
			{
				user.sendMessage("challenges.errors.unknown-challenge");
				this.showHelp(this, user);
				return false;
			}
		}

		this.showHelp(this, user);
		return false;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<List<String>> tabComplete(User user, String alias, List<String> args)
	{
		String lastString = args.get(args.size() - 1);

		final List<String> returnList = new ArrayList<>();
		final int size = args.size();

		switch (size)
		{
			case 3:
				// Create suggestions with all challenges that is available for users.
				returnList.addAll(this.addon.getChallengesManager().getAllChallengesNames(this.getWorld()).stream().
					map(challenge -> challenge.substring(Utils.getGameMode(this.getWorld()).length() + 1)).
					collect(Collectors.toList()));

				break;
			case 4:
				// Suggest a number of completions.
				if (lastString.isEmpty() || lastString.matches("[0-9]*"))
				{
					returnList.add("<number>");
				}

				break;
			default:
			{
				returnList.add("help");
				break;
			}
		}

		return Optional.of(Util.tabLimit(returnList, lastString));
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * Variable that holds challenge addon. Single casting.
	 */
	private ChallengesAddon addon;
}
