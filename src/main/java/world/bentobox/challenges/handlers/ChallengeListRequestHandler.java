package world.bentobox.challenges.handlers;


import org.bukkit.Bukkit;
import java.util.Collections;
import java.util.Map;

import world.bentobox.bentobox.api.addons.request.AddonRequestHandler;
import world.bentobox.challenges.ChallengesAddon;


/**
 * This handler returns all challenges that is operating in given world.
 */
public class ChallengeListRequestHandler extends AddonRequestHandler
{
	/**
	 * Constructor creates a new CompletedChallengesRequestHandler instance.
	 *
	 * @param addon of type ChallengesAddon
	 */
	public ChallengeListRequestHandler(ChallengesAddon addon)
	{
		super("challenge-list");
		this.addon = addon;
	}


	/**
	 * @param metaData Required meta data.
	 * @return Set of strings that contains completed challenges.
	 * @see AddonRequestHandler#handle(Map <String, Object>)
	 */
	@Override
	public Object handle(Map<String, Object> metaData)
	{
		/*
            What we need in the metaData:
				0. "world-name" -> String
            What we will return:
				- List of challenges in given world.
         */

		if (metaData == null ||
			metaData.isEmpty() ||
			metaData.get("world-name") == null ||
			!(metaData.get("world-name") instanceof String) ||
			Bukkit.getWorld((String) metaData.get("world-name")) == null)
		{
			return Collections.emptyList();
		}

		return this.addon.getChallengesManager().getAllChallengesNames(Bukkit.getWorld((String) metaData.get("world-name")));
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * Variable stores challenges addon.
	 */
	private ChallengesAddon addon;
}
