package world.bentobox.challenges.handlers;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import world.bentobox.bentobox.api.addons.request.AddonRequestHandler;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.ChallengeLevel;


/**
 * This handler returns Data map of requested level.
 */
public class LevelDataRequestHandler extends AddonRequestHandler
{
	/**
	 * Constructor creates a new LevelDataRequestHandler instance.
	 *
	 * @param addon of type ChallengesAddon
	 */
	public LevelDataRequestHandler(ChallengesAddon addon)
	{
		super("level-data");
		this.addon = addon;
	}


	/**
	 * @param metaData Required meta data.
	 * @return Map that returns information about level
	 * @see AddonRequestHandler#handle(Map <String, Object>)
	 */
	@Override
	public Object handle(Map<String, Object> metaData)
	{
		/*
            What we need in the metaData:
				0. "level-name" -> String
            What we will return:
				- Empty Map if level is not given or not found
				- Map that contains information about given level:
					- uniqueId: the same id that was passed to this handler.
			 		- name: String object of display name for level.
			 		- icon: ItemStack object that represents level.
			 		- order: Integer object of order number for given level.
			 		- message: String object that represents level unlock message.
			 		- world: String object that represents world name where level operates.

			 		- waiveramount: Integer object of waiver amount for given level.
			 		- challenges: List of strings that represents challenges that is owned by given level.
         */

		if (metaData == null ||
			metaData.isEmpty() ||
			metaData.get("level-name") == null ||
			!(metaData.get("level-name") instanceof String))
		{
			return Collections.emptyMap();
		}

		ChallengeLevel level = this.addon.getChallengesManager().getLevel((String) metaData.get("level-name"));

		Map<String, Object> levelDataMap;

		if (level == null)
		{
			levelDataMap = Collections.emptyMap();
		}
		else
		{
			levelDataMap = new HashMap<>();

			levelDataMap.put("uniqueId", level.getUniqueId());
			levelDataMap.put("name", level.getFriendlyName());
			levelDataMap.put("icon", level.getIcon());
			levelDataMap.put("order", level.getOrder());
			levelDataMap.put("message", level.getUnlockMessage());
			levelDataMap.put("world", level.getWorld());
			levelDataMap.put("challenges", level.getChallenges());
			levelDataMap.put("waiveramount", level.getWaiverAmount());
		}

		return levelDataMap;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * Variable stores challenges addon.
	 */
	private ChallengesAddon addon;
}
