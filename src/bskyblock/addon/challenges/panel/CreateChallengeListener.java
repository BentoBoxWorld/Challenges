package bskyblock.addon.challenges.panel;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import bskyblock.addon.challenges.Challenges;
import us.tastybento.bskyblock.api.commands.User;
import us.tastybento.bskyblock.api.panels.PanelListener;

public class CreateChallengeListener implements PanelListener {

    private Challenges addon;
    private User user;

    public CreateChallengeListener(Challenges addon, User user) {
        this.addon = addon;
        this.user = user;
    }

    @Override
    public void setup() {}

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        Bukkit.getLogger().info("DEBUG: event = " + event);
        Bukkit.getLogger().info("DEBUG: addon = " + addon);
        Bukkit.getLogger().info("DEBUG: challenge manager = " + addon.getChallengesManager());
        addon.getChallengesManager().createInvChallenge(user, event.getInventory());    
    }

    @Override
    public void onInventoryClick(User user, InventoryClickEvent event) {
        // Allow drag and drop
        Bukkit.getLogger().info("DEBUG: setting cancelled to false");
        event.setCancelled(false);   
    }
}
