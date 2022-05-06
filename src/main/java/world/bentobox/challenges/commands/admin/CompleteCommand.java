package world.bentobox.challenges.commands.admin;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;


/**
 * This command allows completing challenges without a gui.
 */
public class CompleteCommand extends CompositeCommand
{
	/**
	 * Default constructor for Composite Command.
	 * @param addon Challenges addon.
	 * @param cmd Parent Command.
	 */
	public CompleteCommand(Addon addon, CompositeCommand cmd)
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
		this.setPermission("complete");
		this.setParametersHelp("challenges.commands.admin.complete.parameters");
		this.setDescription("challenges.commands.admin.complete.description");
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		if (args.isEmpty())
		{
			if (user.isPlayer())
			{
				Utils.sendMessage(user, user.getTranslation("challenges.errors.no-name"));
			}
			else
			{
				this.addon.logError("Missing parameters");
			}
		}
		else if (args.size() < 2)
		{
			if (user.isPlayer())
			{
				Utils.sendMessage(user, user.getTranslation("challenges.errors.missing-arguments"));
			}
			else
			{
				this.addon.logError("Missing parameters");
			}
		}
		else if (!args.get(1).isEmpty())
		{
			UUID targetUUID = this.getPlayers().getUUID(args.get(0));

			if (targetUUID == null)
			{
				if (user.isPlayer())
				{
					Utils.sendMessage(user, user.getTranslation("general.errors.unknown-player",
						TextVariables.NAME,
						args.get(0)));
				}
				else
				{
					this.addon.logError("Unknown player name " + args.get(0));
				}

				return false;
			}

			// Add world name back at the start
			String challengeName = Utils.getGameMode(this.getWorld()) + "_" + args.get(1);
			Challenge challenge = this.addon.getChallengesManager().getChallenge(challengeName);
			User target = User.getInstance(targetUUID);

			if (challenge != null)
			{
				if (!this.addon.getChallengesManager().isChallengeComplete(targetUUID, this.getWorld(), challenge))
				{
					this.addon.getChallengesManager().setChallengeComplete(
						targetUUID, this.getWorld(), challenge, user.getUniqueId());


					if (user.isPlayer())
					{
						Utils.sendMessage(user, user.getTranslation("challenges.messages.completed",
							Constants.PARAMETER_NAME, challenge.getFriendlyName(),
							Constants.PARAMETER_PLAYER, target.getName()));
					}
					else
					{
						this.addon.log("Challenge " + challenge.getFriendlyName() + " completed for player " +
							target.getName());
					}
				}
				else
				{
					if (user.isPlayer())
					{
						Utils.sendMessage(user, user.getTranslation("challenges.messages.already-completed"));
					}
					else
					{
						this.addon.log("Challenge was already completed");
					}
				}

				return true;
			}
			else
			{
				if (user.isPlayer())
				{
					Utils.sendMessage(user, user.getTranslation("challenges.errors.unknown-challenge"));
				}
				else
				{
					this.addon.logError("Unknown challenge " + args.get(1));
				}

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
			case 3 ->
				// Create suggestions with all challenges that is available for users.
				returnList.addAll(Util.getOnlinePlayerList(user));
			case 4 ->
				// Create suggestions with all challenges that is available for users.
				returnList.addAll(this.addon.getChallengesManager().getAllChallengesNames(this.getWorld()).stream().
						map(challenge -> challenge.substring(Utils.getGameMode(this.getWorld()).length() + 1)).toList());
			default ->
				returnList.add("help");
		}

		return Optional.of(Util.tabLimit(returnList, lastString));
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * Variable that holds challenge addon. Single casting.
	 */
	private final ChallengesAddon addon;
}
