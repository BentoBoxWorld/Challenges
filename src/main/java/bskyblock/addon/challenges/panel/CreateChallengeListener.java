package bskyblock.addon.challenges.panel;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import bskyblock.addon.challenges.ChallengesAddon;
import us.tastybento.bskyblock.api.panels.PanelListener;
import us.tastybento.bskyblock.api.user.User;

public class CreateChallengeListener implements PanelListener {
	
	private ChallengesAddon addon;

	private User user;

	public CreateChallengeListener(ChallengesAddon addon, User user) {
		this.addon = addon;
		this.user = user;
	}

	@Override
	public void setup() {
		// Nothing to setup
	}

	@Override
	public void onInventoryClose(InventoryCloseEvent event) {
		addon.getChallengesManager().createInvChallenge(user, event.getInventory());	
	}

	@Override
	public void onInventoryClick(User user, InventoryClickEvent event) {
		// Allow drag and drop
		event.setCancelled(false);   
	}

}