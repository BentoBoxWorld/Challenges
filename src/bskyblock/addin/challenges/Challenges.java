package bskyblock.addin.challenges;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import bskyblock.addin.challenges.commands.ChallengesCommand;
import bskyblock.addin.challenges.config.LocaleManager;
import bskyblock.addin.challenges.config.PluginConfig;
import us.tastybento.bskyblock.BSkyBlock;
import us.tastybento.bskyblock.config.BSBLocale;
import us.tastybento.bskyblock.database.BSBDatabase;
import us.tastybento.bskyblock.database.flatfile.FlatFileDatabase;

/**
 * Addin to BSkyBlock that enables challenges
 * @author tastybento
 *
 */
public class Challenges extends JavaPlugin {


    private static final boolean DEBUG = true;

    // The BSkyBlock plugin instance.
    private BSkyBlock bSkyBlock;

    // Locale manager for this plugin
    private LocaleManager localeManager;

    // The BSkyBlock database object
    private BSBDatabase database;

    private ChallengesManager manager;

    private FlatFileDatabase flatFile;


    @SuppressWarnings("unchecked")
    @Override
    public void onEnable() {
        // Load the plugin's config
        new PluginConfig(this);
        // Get the BSkyBlock plugin. This will be available because this plugin depends on it in plugin.yml.
        bSkyBlock = BSkyBlock.getPlugin();
        // Check if it is enabled - it might be loaded, but not enabled.
        if (bSkyBlock == null || !bSkyBlock.isEnabled()) {
            this.setEnabled(false);
            return;
        }

        // Local locales
        localeManager = new LocaleManager(this);
        
        // Challenges Manager
        manager = new ChallengesManager(this);
        // Register commands
        new ChallengesCommand(this);
        // Register Listener
        getServer().getPluginManager().registerEvents(manager, this);
        // Done
    }

    @Override
    public void onDisable(){
        if (manager != null)
            manager.save(false);
    }


    /**
     * Get the locale for this player
     * @param sender
     * @return Locale object for sender
     */
    public BSBLocale getLocale(CommandSender sender) {
        return localeManager.getLocale(sender);
    }

    /**
     * Get the locale for this UUID
     * @param uuid
     * @return Locale object for UUID
     */
    public BSBLocale getLocale(UUID uuid) {
        return localeManager.getLocale(uuid);
    }

    public ChallengesManager getManager() {
        return manager;
    }

}
