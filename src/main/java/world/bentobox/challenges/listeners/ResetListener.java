/**
 *
 */
package world.bentobox.challenges.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.bentobox.api.events.island.IslandEvent;
import world.bentobox.bentobox.api.events.island.IslandEvent.Reason;

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

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onIslandReset(IslandEvent e) {
        if (e.getReason().equals(Reason.CREATED) || (addon.getChallengesSettings().isResetChallenges() && e.getReason().equals(Reason.RESETTED))) {
            addon.getChallengesManager().resetAllChallenges(User.getInstance(e.getOwner()), e.getLocation().getWorld());
        }
    }
}
