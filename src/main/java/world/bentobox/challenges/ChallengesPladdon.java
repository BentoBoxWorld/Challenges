//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.challenges;


import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;

/**
 * @author tastybento
 */
public class ChallengesPladdon extends Pladdon
{
    @Override
    public Addon getAddon()
    {
        return new ChallengesAddon();
    }
}
