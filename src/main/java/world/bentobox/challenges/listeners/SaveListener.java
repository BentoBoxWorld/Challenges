package world.bentobox.challenges.listeners;


import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldSaveEvent;

import world.bentobox.challenges.ChallengesAddon;


/**
 * This is Simple World Save event listener. On each world save, this method
 * asks challenge manager to save its data.
 */
public class SaveListener implements Listener
{
    public SaveListener(ChallengesAddon addon) {
        this.addon = addon;
    }


    /**
     * This event listener handles world save event.
     * @param e World Save event.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onWorldSave(WorldSaveEvent e)
    {
        // Save only for worlds where exist any challenge addon data.
        if (this.addon.getChallengesManager().hasAnyChallengeData(e.getWorld()))
        {
            this.addon.getChallengesManager().save();
        }
    }


    /**
     * This event listener handles player kick event.
     * If player is kicked, then remove it from player cache data.
     * @param e PlayerKickEvent
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerKickEvent(PlayerKickEvent e)
    {
        this.addon.getChallengesManager().removeFromCache(e.getPlayer().getUniqueId());
    }


    /**
     * This event listener handles player quit event.
     * If player quits server, then remove it from player cache data.
     * @param e PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerQuitEvent(PlayerQuitEvent e)
    {
       this.addon.getChallengesManager().removeFromCache(e.getPlayer().getUniqueId());
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    private ChallengesAddon addon;
}
