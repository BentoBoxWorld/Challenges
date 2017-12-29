package bskyblock.addon.challenges;

import bskyblock.addon.challenges.commands.ChallengesCommand;
import bskyblock.addon.challenges.config.PluginConfig;
import us.tastybento.bskyblock.BSkyBlock;
import us.tastybento.bskyblock.api.addons.Addon;

/**
 * Addin to BSkyBlock that enables challenges
 * @author tastybento
 *
 */
public class Challenges extends Addon {

    // The BSkyBlock plugin instance.
    private BSkyBlock bSkyBlock;

    private ChallengesManager manager;

    @Override
    public void onEnable() {
        // Load the plugin's config
        new PluginConfig(this);
        // Get the BSkyBlock plugin. This will be available because this plugin depends on it in plugin.yml.
        bSkyBlock = BSkyBlock.getInstance();
        // Check if it is enabled - it might be loaded, but not enabled.
        if (bSkyBlock == null || !bSkyBlock.isEnabled()) {
            this.setEnabled(false);
            return;
        }
        
        // Challenges Manager
        manager = new ChallengesManager(this);
        // Register commands
        new ChallengesCommand(this);
        // Done
    }

    @Override
    public void onDisable(){
        if (manager != null)
            manager.save(false);
    }

    public ChallengesManager getManager() {
        return manager;
    }

}
