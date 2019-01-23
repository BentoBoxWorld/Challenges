package world.bentobox.challenges.panel;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.bentobox.api.panels.PanelListener;
import world.bentobox.bentobox.api.user.User;


/**
 * @deprecated All panels are reworked.
 */
@Deprecated
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
