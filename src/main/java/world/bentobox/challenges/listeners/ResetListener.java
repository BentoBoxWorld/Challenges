/**
 *
 */
package world.bentobox.challenges.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import world.bentobox.bentobox.api.events.island.IslandCreatedEvent;
import world.bentobox.bentobox.api.events.island.IslandRegisteredEvent;
import world.bentobox.bentobox.api.events.island.IslandResettedEvent;
import world.bentobox.challenges.ChallengesAddon;

/**
 * Resets challenges when the island is reset
 * @author tastybento
 *
 */
public class ResetListener implements Listener {

    private ChallengesAddon addon;

    public ResetListener(ChallengesAddon addon) {
        this.addon = addon;
    }


    /**
     * This method handles Island Created event.
     *
     * @param e Event that must be handled.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandCreated(IslandCreatedEvent e)
    {
        addon.getChallengesManager().resetAllChallenges(e.getOwner(), e.getLocation().getWorld(), e.getOwner());
    }


    /**
     * This method handles Island Resetted event.
     *
     * @param e Event that must be handled.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandCreated(IslandResettedEvent e)
    {
        addon.getChallengesManager().resetAllChallenges(e.getOwner(), e.getLocation().getWorld(), e.getOwner());
    }


    /**
     * This method handles Island Registered event.
     *
     * @param e Event that must be handled.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandCreated(IslandRegisteredEvent e)
    {
        addon.getChallengesManager().resetAllChallenges(e.getOwner(), e.getLocation().getWorld(), e.getOwner());
    }
}
