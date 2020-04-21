package world.bentobox.challenges.handlers;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import world.bentobox.bentobox.api.addons.request.AddonRequestHandler;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.Challenge;


/**
 * This handler returns data for requested challenge.
 */
public class ChallengeDataRequestHandler extends AddonRequestHandler
{

    /**
     * Constructor creates a new ChallengesDataRequestHandler instance.
     *
     * @param addon of type ChallengesAddon
     */
    public ChallengeDataRequestHandler(ChallengesAddon addon)
    {
        super("challenge-data");
        this.addon = addon;
    }


    /* (non-Javadoc)
     * @param metaData Required meta data.
     * @return Map that returns information about challenges
     * @see world.bentobox.bentobox.api.addons.request.AddonRequestHandler#handle(java.util.Map)
     */
    @Override
    public Object handle(Map<String, Object> metaData)
    {
        /*
            What we need in the metaData:
				0. "challenge-name" -> String
            What we will return:
				- Empty Map if challenge is not given or not found
				- Map that contains information about given challenge:
					- uniqueId: the same id that was passed to this handler.
			 		- name: String object of display name for challenge.
			 		- icon: ItemStack object that represents challenge.
			 		- levelId: String object of levelId that this challenge is linked.
			 		- order: Integer object of order number for given challenge.
			 		- deployed: boolean object of deployment status.
			 		- description: List of strings that represents challenges description.
			 		- type: String object that represents challenges type.

			 		- repeatable: boolean object of repeatable option.
			 		- maxTimes: Integer object that represents how many times challenge can be completed.
         */

        if (metaData == null ||
                metaData.isEmpty() ||
                metaData.get("challenge-name") == null ||
                !(metaData.get("challenge-name") instanceof String))
        {
            return Collections.emptyMap();
        }

        Challenge challenge = this.addon.getChallengesManager().getChallenge((String) metaData.get("challenge-name"));

        Map<String, Object> challengeDataMap;

        if (challenge == null)
        {
            challengeDataMap = Collections.emptyMap();
        }
        else
        {
            challengeDataMap = new HashMap<>();

            challengeDataMap.put("uniqueId", challenge.getUniqueId());
            challengeDataMap.put("name", challenge.getFriendlyName());
            challengeDataMap.put("icon", challenge.getIcon());
            challengeDataMap.put("levelId", challenge.getLevel());
            challengeDataMap.put("order", challenge.getOrder());
            challengeDataMap.put("deployed", challenge.isDeployed());
            challengeDataMap.put("description", challenge.getDescription());
            challengeDataMap.put("type", challenge.getChallengeType().toString());

            challengeDataMap.put("repeatable", challenge.isRepeatable());
            challengeDataMap.put("maxTimes", challenge.isRepeatable() ? challenge.getMaxTimes() : 1);

        }

        return challengeDataMap;
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * Variable stores challenges addon.
     */
    private ChallengesAddon addon;
}
