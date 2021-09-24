package world.bentobox.challenges.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import world.bentobox.bentobox.api.events.island.IslandCreatedEvent;
import world.bentobox.bentobox.api.events.island.IslandRegisteredEvent;
import world.bentobox.bentobox.api.events.island.IslandResettedEvent;
import world.bentobox.bentobox.api.events.team.TeamKickEvent;
import world.bentobox.bentobox.api.events.team.TeamLeaveEvent;
import world.bentobox.challenges.ChallengesAddon;


/**
 * Resets challenges when the island is reset
 *
 * @author tastybento
 */
public record ResetListener(ChallengesAddon addon) implements Listener
{
    /**
     * This method handles Island Created event.
     *
     * @param e Event that must be handled.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandCreated(IslandCreatedEvent e)
    {
        // Reset any challenges that can be assigned to the island or its owner.
        if (this.addon.getChallengesSettings().isResetChallenges())
        {
            this.addon.getChallengesManager().resetAllChallenges(e.getOwner(),
                e.getLocation().getWorld(),
                e.getOwner());
        }
    }


    /**
     * This method handles Island Resetted event.
     *
     * @param e Event that must be handled.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandResetted(IslandResettedEvent e)
    {
        // Reset owner challenges only if data is stored per player.
        if (this.addon.getChallengesSettings().isResetChallenges() &&
            !this.addon.getChallengesSettings().isStoreAsIslandData())
        {
            this.addon.getChallengesManager().resetAllChallenges(e.getOwner(),
                e.getLocation().getWorld(),
                e.getOwner());
        }
    }


    /**
     * This method handles Island Registered event.
     *
     * @param e Event that must be handled.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandRegistered(IslandRegisteredEvent e)
    {
        // Reset owner challenges only if data is stored per player.
        if (this.addon.getChallengesSettings().isResetChallenges() &&
            !this.addon.getChallengesSettings().isStoreAsIslandData())
        {
            this.addon.getChallengesManager().resetAllChallenges(e.getOwner(),
                e.getLocation().getWorld(),
                e.getOwner());
        }
    }


    /**
     * This method handles Island Registered event.
     *
     * @param e Event that must be handled.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeamLeave(TeamLeaveEvent e)
    {
        // Reset player challenges only if data is stored per player.
        if (this.addon.getChallengesSettings().isResetChallenges() &&
            !this.addon.getChallengesSettings().isStoreAsIslandData())
        {
            this.addon.getChallengesManager().resetAllChallenges(e.getPlayerUUID(),
                e.getLocation().getWorld(),
                e.getOwner());
        }
    }


    /**
     * This method handles Island Registered event.
     *
     * @param e Event that must be handled.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeamKick(TeamKickEvent e)
    {
        // Reset player challenges only if data is stored per player.
        if (this.addon.getChallengesSettings().isResetChallenges() &&
            !this.addon.getChallengesSettings().isStoreAsIslandData())
        {
            this.addon.getChallengesManager().resetAllChallenges(e.getPlayerUUID(),
                e.getLocation().getWorld(),
                e.getOwner());
        }
    }
}
