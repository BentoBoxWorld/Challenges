package world.bentobox.challenges.handlers;


import java.util.Collections;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;

import world.bentobox.bentobox.api.addons.request.AddonRequestHandler;
import world.bentobox.challenges.ChallengesAddon;


/**
 * This handler returns all levels that is working in given world.
 */
public class LevelListRequestHandler extends AddonRequestHandler
{

    /**
     * Constructor creates a new CompletedChallengesRequestHandler instance.
     *
     * @param addon of type ChallengesAddon
     */
    public LevelListRequestHandler(ChallengesAddon addon)
    {
        super("level-list");
        this.addon = addon;
    }


    /* (non-Javadoc)
     * @see world.bentobox.bentobox.api.addons.request.AddonRequestHandler#handle(java.util.Map)
     * @param metaData Required meta data.
     * @return List of strings that contains levels in given world
     */
    @Override
    public Object handle(Map<String, Object> metaData)
    {
        /*
            What we need in the metaData:
				0. "world-name" -> String
            What we will return:
				- List of levels in given world.
         */

        if (metaData == null ||
            metaData.isEmpty() ||
            metaData.get("world-name") == null ||
            !(metaData.get("world-name") instanceof String))
        {
            return Collections.emptyList();
        }

        World world = Bukkit.getWorld((String) metaData.get("world-name"));

        if (world == null)
        {
            return Collections.emptyList();
        }

        return this.addon.getChallengesManager().getLevelNames(world);
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * Variable stores challenges addon.
     */
    private final ChallengesAddon addon;
}
