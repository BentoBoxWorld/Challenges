package world.bentobox.challenges.handlers;


import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;

import world.bentobox.bentobox.api.addons.request.AddonRequestHandler;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.managers.ChallengesManager;


/**
 * This Request Handler returns completed challenges for user in given world.
 */
public class CompletedChallengesRequestHandler extends AddonRequestHandler
{

    /**
     * Constructor creates a new CompletedChallengesRequestHandler instance.
     *
     * @param addon of type ChallengesAddon
     */
    public CompletedChallengesRequestHandler(ChallengesAddon addon)
    {
        super("completed-challenges");
        this.addon = addon;
    }


    /* (non-Javadoc)
     * @see world.bentobox.bentobox.api.addons.request.AddonRequestHandler#handle(java.util.Map)
     * @param metaData Required meta data.
     * @return Set of strings that contains completed challenges.
     */
    @Override
    public Object handle(Map<String, Object> metaData)
    {
        /*
            What we need in the metaData:
            	0. "player" -> UUID
				1. "world-name" -> String
            What we will return:
				- Empty Set if player or given world is not valid.
            	- Set of completed challenges in given world (or empty list if user haven't completed any challenge)
         */

        if (metaData == null ||
            metaData.isEmpty() ||
            metaData.get("world-name") == null ||
            !(metaData.get("world-name") instanceof String) ||
            metaData.get("player") == null ||
            !(metaData.get("player") instanceof UUID player))
        {
            return Collections.emptySet();
        }

        World world = Bukkit.getWorld((String) metaData.get("world-name"));

        if (world == null)
        {
            return Collections.emptySet();
        }

        ChallengesManager manager = this.addon.getChallengesManager();

        return manager.getAllChallengesNames(world).stream().
            filter(challenge -> manager.isChallengeComplete(player, world, challenge)).
            collect(Collectors.toSet());
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * Variable stores challenges addon.
     */
    private final ChallengesAddon addon;
}
