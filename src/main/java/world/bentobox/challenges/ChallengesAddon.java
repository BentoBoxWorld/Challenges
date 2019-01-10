package world.bentobox.challenges;

import org.bukkit.Bukkit;

import world.bentobox.acidisland.AcidIsland;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.challenges.commands.ChallengesCommand;
import world.bentobox.challenges.commands.admin.Challenges;
import world.bentobox.challenges.listeners.ResetListener;
import world.bentobox.challenges.listeners.SaveListener;
import world.bentobox.bentobox.api.addons.Addon;

/**
 * Add-on to BSkyBlock that enables challenges
 * @author tastybento
 *
 */
public class ChallengesAddon extends Addon {

// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    private ChallengesManager challengesManager;

    private ChallengesImportManager importManager;

    private Settings settings;

    private boolean hooked;


// ---------------------------------------------------------------------
// Section: Constants
// ---------------------------------------------------------------------


    /**
     * Permission prefix for addon.
     */
    private static final String PERMISSION_PREFIX = "addon";


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoad() {
        // Save default config.yml
        this.saveDefaultConfig();
        // Load the plugin's config
        this.loadSettings();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onEnable() {
        // Check if it is enabled - it might be loaded, but not enabled.
        if (this.getPlugin() == null || !this.getPlugin().isEnabled()) {
            Bukkit.getLogger().severe("BentoBox is not available or disabled!");
            this.setState(State.DISABLED);
            return;
        }

        // Challenges Manager
        this.challengesManager = new ChallengesManager(this);
        // Challenge import setup
        this.importManager = new ChallengesImportManager(this);


        // Integrate into AcidIsland.
        if (this.settings.getDisabledGameModes().isEmpty() ||
            !this.settings.getDisabledGameModes().contains("AcidIsland"))
        {
            this.getPlugin().getAddonsManager().getAddonByName("AcidIsland").ifPresent(
                addon -> {
                    AcidIsland acidIsland = (AcidIsland) addon;

                    new Challenges(this,
                        this.getPlugin().getCommandsManager().getCommand(
                            acidIsland.getSettings().getAdminCommand()));

                    new ChallengesCommand(this,
                        this.getPlugin().getCommandsManager().getCommand(
                            acidIsland.getSettings().getIslandCommand()));

                    this.hooked = true;
                });
        }

        // Integrate into BSkyBlock.
        if (this.settings.getDisabledGameModes().isEmpty() ||
            !this.settings.getDisabledGameModes().contains("BSkyBlock"))
        {
            this.getPlugin().getAddonsManager().getAddonByName("BSkyBlock").ifPresent(
                addon -> {
//                  BSkyBlock skyBlock = (BSkyBlock) addon;
//                  SkyBlock addon cannot change commands ;(

                    new Challenges(this,
                        this.getPlugin().getCommandsManager().getCommand("bsbadmin"));

                    new ChallengesCommand(this,
                        this.getPlugin().getCommandsManager().getCommand("island"));

                    this.hooked = true;
                });
        }

        if (this.hooked) {
            // Try to find Level addon and if it does not exist, display a warning
            if (!this.getAddonByName("Level").isPresent()) {
                this.logWarning("Level add-on not found so level challenges will not work!");
            }

            // Register the reset listener
            this.registerListener(new ResetListener(this));
            // Register the autosave listener.
            this.registerListener(new SaveListener(this));
        } else {
            this.logError("Challenges could not hook into AcidIsland or BSkyBlock so will not do anything!");
            this.setState(State.DISABLED);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onReload()
    {
        if (this.hooked) {
            this.challengesManager.save();

            this.loadSettings();
            this.getLogger().info("Challenges addon reloaded.");
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisable() {
        if (this.hooked) {
            this.challengesManager.save();
        }
    }


    /**
     * This method loads addon configuration settings in memory.
     */
    private void loadSettings() {
        this.settings = new Config<>(this, Settings.class).loadConfigObject();

        if (this.settings == null) {
            // Disable
            this.logError("Challenges settings could not load! Addon disabled.");
            this.setState(State.DISABLED);
        }
    }


// ---------------------------------------------------------------------
// Section: Getters
// ---------------------------------------------------------------------


    /**
     * @return challengesManager
     */
    public ChallengesManager getChallengesManager() {
        return this.challengesManager;
    }


    /**
     * @return Permission Prefix.
     */
    @Override
    public String getPermissionPrefix() {
        return PERMISSION_PREFIX;
    }


    /**
     * @return the importManager
     */
    public ChallengesImportManager getImportManager() {
        return this.importManager;
    }


    /**
     * @return the challenge settings.
     */
    public Settings getChallengesSettings()
    {
        return this.settings;
    }
}
