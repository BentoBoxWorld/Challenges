package world.bentobox.challenges;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.challenges.commands.ChallengesCommand;
import world.bentobox.challenges.commands.ChallengesUserCommand;
import world.bentobox.challenges.commands.admin.Challenges;
import world.bentobox.challenges.commands.admin.ChallengesAdminCommand;
import world.bentobox.challenges.handlers.*;
import world.bentobox.challenges.listeners.ResetListener;
import world.bentobox.challenges.listeners.SaveListener;
import world.bentobox.level.Level;


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

    /**
     * This boolean indicate if economy is enabled.
     */
    private boolean economyProvided;

    /**
     * VaultHook that process economy.
     */
    private VaultHook vaultHook;

    /**
     * Level addon.
     */
    private Level levelAddon;

    /**
     * This indicate if level addon exists.
     */
    private boolean levelProvided;

// ---------------------------------------------------------------------
// Section: Constants
// ---------------------------------------------------------------------


    /**
     * Permission prefix for addon.
     */
    private static final String PERMISSION_PREFIX = "addon.";

	/**
	 * This flag allows to complete challenges in any part of the world. It will not limit
	 * player to their island. Useful for skygrid without protection flags.
	 */
	public static Flag CHALLENGES_WORLD_PROTECTION =
		new Flag.Builder("CHALLENGES_WORLD_PROTECTION", Material.GRASS_BLOCK).type(Flag.Type.WORLD_SETTING).defaultSetting(true).build();

	/**
	 * This flag allows to define which users can complete challenge. F.e. it can be set
	 * that only Island owner can complete challenge.
	 * By default it is set to Visitor.
	 */
	public static Flag CHALLENGES_ISLAND_PROTECTION =
		new Flag.Builder("CHALLENGES_ISLAND_PROTECTION", Material.COMMAND_BLOCK).defaultRank(RanksManager.VISITOR_RANK).build();


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

        // Check if addon is not disabled before.
        if (this.getState().equals(State.DISABLED))
        {
            Bukkit.getLogger().severe("Challenges Addon is not available or disabled!");
            return;
        }

        // Challenges Manager
        this.challengesManager = new ChallengesManager(this);
        // Challenge import setup
        this.importManager = new ChallengesImportManager(this);

        List<GameModeAddon> hookedGameModes = new ArrayList<>();

        this.getPlugin().getAddonsManager().getGameModeAddons().forEach(gameModeAddon -> {
        	if (!this.settings.getDisabledGameModes().contains(gameModeAddon.getDescription().getName()))
			{
				if (gameModeAddon.getPlayerCommand().isPresent())
				{
					new ChallengesCommand(this, gameModeAddon.getPlayerCommand().get());
                    this.hooked = true;

                    hookedGameModes.add(gameModeAddon);
                }

				if (gameModeAddon.getAdminCommand().isPresent())
				{
					new Challenges(this, gameModeAddon.getAdminCommand().get());
					this.hooked = true;
				}

				CHALLENGES_WORLD_PROTECTION.addGameModeAddon(gameModeAddon);
				CHALLENGES_ISLAND_PROTECTION.addGameModeAddon(gameModeAddon);
			}
		});

        if (this.hooked) {

            // Create general challenge commands

            if (this.settings.isUseCommonGUI())
            {
                new ChallengesUserCommand(this,
                    this.settings.getUserCommand(),
                    hookedGameModes);
                new ChallengesAdminCommand(this,
                    this.settings.getAdminCommand(),
                    hookedGameModes);
            }

            // Try to find Level addon and if it does not exist, display a warning

            Optional<Addon> level = this.getAddonByName("Level");

            if (!level.isPresent())
            {
                this.logWarning("Level add-on not found so level challenges will not work!");
                this.levelAddon = null;
            }
            else
            {
                this.levelProvided = true;
                this.levelAddon = (Level) level.get();
            }

            Optional<VaultHook> vault = this.getPlugin().getVault();

            if (!vault.isPresent() || !vault.get().hook())
            {
                this.vaultHook = null;
                this.logWarning("Economy plugin not found so money options will not work!");
            }
            else
            {
                this.economyProvided = true;
                this.vaultHook = vault.get();
            }

            // Register the reset listener
            this.registerListener(new ResetListener(this));
            // Register the autosave listener.
            this.registerListener(new SaveListener(this));

            // Register Flags
			this.getPlugin().getFlagsManager().registerFlag(CHALLENGES_ISLAND_PROTECTION);
			this.getPlugin().getFlagsManager().registerFlag(CHALLENGES_WORLD_PROTECTION);

            // Register Request Handlers
            this.registerRequestHandler(new ChallengeListRequestHandler(this));
            this.registerRequestHandler(new LevelListRequestHandler(this));

            this.registerRequestHandler(new ChallengeDataRequestHandler(this));
            this.registerRequestHandler(new LevelDataRequestHandler(this));

            this.registerRequestHandler(new CompletedChallengesRequestHandler(this));
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
        super.onReload();

        if (this.hooked)
        {
            this.loadSettings();
            this.challengesManager.reload();
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

        if (this.settings != null)
        {
            new Config<>(this, Settings.class).saveConfigObject(this.settings);
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


    /**
     *
     * @return economyProvided variable.
     */
    public boolean isEconomyProvided()
    {
        return this.economyProvided;
    }


    /**
     * Returns VaultHook. Used to get easier access to Economy. NEVER USE WITHOUT isEconomyProvided or null
     * check.
     * @return VaultHook or null.
     */
    public VaultHook getEconomyProvider()
    {
        return vaultHook;
    }


    /**
     *
     * @return levelProvided variable.
     */
    public boolean isLevelProvided()
    {
        return levelProvided;
    }


    /**
     * This method returns Level addon. Used to easier access to Level. NEVER USE WITHOUT isLevelProvided or null
     * @return LevelAddon or null.
     */
    public Level getLevelAddon()
    {
        return levelAddon;
    }
}
