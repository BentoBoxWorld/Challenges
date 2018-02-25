package bskyblock.addon.challenges.config;

import bskyblock.addon.challenges.ChallengesAddon;

public class PluginConfig {

    /**
     * Loads the various settings from the config.yml file into the plugin
     */
    public PluginConfig(ChallengesAddon plugin) {
        plugin.saveDefaultConfig();

        // Settings
        Settings.resetChallenges = plugin.getConfig().getBoolean("resetchallenges");
        // Challenge completion broadcast
        Settings.broadcastMessages = plugin.getConfig().getBoolean("broadcastmessages", true);
        // Challenges - show or remove completed one-time challenges
        Settings.removeCompleteOnetimeChallenges = plugin.getConfig().getBoolean("removecompleteonetimechallenges");
        // Add glow to completed challenge icons or not
        Settings.addCompletedGlow = plugin.getConfig().getBoolean("addcompletedglow", true);

        // All done
    }
}
