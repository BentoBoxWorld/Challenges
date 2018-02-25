package bskyblock.addon.challenges;

import org.bukkit.Bukkit;

import bskyblock.addon.challenges.commands.ChallengesCommand;
import bskyblock.addon.challenges.commands.admin.ChallengesAdminCommand;
import bskyblock.addon.challenges.config.PluginConfig;
import us.tastybento.bskyblock.api.addons.Addon;

/**
 * Add-on to BSkyBlock that enables challenges
 * @author tastybento
 *
 */
public class ChallengesAddon extends Addon {

    private ChallengesManager challengesManager;

    @Override
    public void onEnable() {
        // Load the plugin's config
        new PluginConfig(this);
        // Check if it is enabled - it might be loaded, but not enabled.
        if (getBSkyBlock() == null || !getBSkyBlock().isEnabled()) {
            Bukkit.getLogger().severe("BSkyBlock is not available or disabled!");
            this.setEnabled(false);
            return;
        }
        
        // Challenges Manager
        challengesManager = new ChallengesManager(this);
        // Register commands
        new ChallengesCommand(this);
        new ChallengesAdminCommand(this);
        // Done
    }

    @Override
    public void onDisable(){
        if (challengesManager != null)
            challengesManager.save(false);
    }

    public ChallengesManager getChallengesManager() {
        return challengesManager;
    }

}
