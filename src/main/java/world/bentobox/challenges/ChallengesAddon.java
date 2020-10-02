package world.bentobox.challenges;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.database.DatabaseSetup.DatabaseType;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.challenges.commands.ChallengesCommand;
import world.bentobox.challenges.commands.ChallengesUserCommand;
import world.bentobox.challenges.commands.admin.Challenges;
import world.bentobox.challenges.commands.admin.ChallengesAdminCommand;
import world.bentobox.challenges.config.Settings;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.handlers.ChallengeDataRequestHandler;
import world.bentobox.challenges.handlers.ChallengeListRequestHandler;
import world.bentobox.challenges.handlers.CompletedChallengesRequestHandler;
import world.bentobox.challenges.handlers.LevelDataRequestHandler;
import world.bentobox.challenges.handlers.LevelListRequestHandler;
import world.bentobox.challenges.listeners.ResetListener;
import world.bentobox.challenges.listeners.SaveListener;
import world.bentobox.challenges.web.WebManager;
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

    /**
     * This class manages web content loading.
     */
    private WebManager webManager;

    private Settings settings;

    private boolean hooked;

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
	public static final Flag CHALLENGES_WORLD_PROTECTION =
		new Flag.Builder("CHALLENGES_WORLD_PROTECTION", Material.GRASS_BLOCK).type(Flag.Type.WORLD_SETTING).defaultSetting(true).build();

	/**
	 * This flag allows to define which users can complete challenge. F.e. it can be set
	 * that only Island owner can complete challenge.
	 * By default it is set to Visitor.
	 */
	public static final Flag CHALLENGES_ISLAND_PROTECTION =
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
            this.logError("BentoBox is not available or disabled!");
            this.setState(State.DISABLED);
            return;
        }

        // Check if addon is not disabled before.
        if (this.getState().equals(State.DISABLED))
        {
            this.logError("Challenges Addon is not available or disabled!");
            return;
        }

        if (this.isInCompatibleDatabase())
        {
            this.logError("BentoBox database is not compatible with Challenges Addon.");
            this.logError("Please use JSON based database type.");
            this.setState(State.INCOMPATIBLE);
            return;
        }

        // Challenges Manager
        this.challengesManager = new ChallengesManager(this);
        // Challenge import setup
        this.importManager = new ChallengesImportManager(this);

        // Web content loading
        this.webManager = new WebManager(this);

        List<GameModeAddon> hookedGameModes = new ArrayList<>();

        this.getPlugin().getAddonsManager().getGameModeAddons().forEach(gameModeAddon -> {
        	if (!this.settings.getDisabledGameModes().contains(
        	    gameModeAddon.getDescription().getName()))
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

				this.registerPlaceholders(gameModeAddon);

				// TODO: this is old placeholders. Remove when backward compatibility ends.
                this.registerPlaceholdersOld(gameModeAddon);
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
                this.logWarning("Vault plugin not found. Economy will not work!");
            }
            else
            {
                this.vaultHook = vault.get();
            }

            // Register the reset listener
            this.registerListener(new ResetListener(this));
            // Register the autosave listener.
            this.registerListener(new SaveListener(this));

            // Register Flags
			this.registerFlag(CHALLENGES_ISLAND_PROTECTION);
			this.registerFlag(CHALLENGES_WORLD_PROTECTION);

            // Register Request Handlers
            this.registerRequestHandler(new ChallengeListRequestHandler(this));
            this.registerRequestHandler(new LevelListRequestHandler(this));

            this.registerRequestHandler(new ChallengeDataRequestHandler(this));
            this.registerRequestHandler(new LevelDataRequestHandler(this));

            this.registerRequestHandler(new CompletedChallengesRequestHandler(this));

            if (this.settings.getAutoSaveTimer() > 0)
            {
                Bukkit.getScheduler().runTaskTimerAsynchronously(
                    this.getPlugin(),
                    bukkitTask -> ChallengesAddon.this.challengesManager.save(),
                    this.settings.getAutoSaveTimer() * 60 * 20,
                    this.settings.getAutoSaveTimer() * 60 * 20
                );
            }
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
            this.log("Challenges addon reloaded.");
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
     * This method saves addon settings into file.
     */
    public void saveSettings()
    {
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


    /**
     * This method checks if database is compatible with Challenges addon.
     * @return {@code true} if database type is YAML, {@code false} - otherwise.
     */
    private boolean isInCompatibleDatabase()
    {
        return this.getPlugin().getSettings().getDatabaseType().equals(DatabaseType.YAML);
    }


    /**
     * This method registers placeholders into GameMode addon.
     * @param gameModeAddon GameMode addon where placeholders must be hooked in.
     */
    private void registerPlaceholders(GameModeAddon gameModeAddon)
    {
        final String addonName = this.getDescription().getName().toLowerCase();
        final World world = gameModeAddon.getOverWorld();

        // Number of completions for all challenges placeholder
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(gameModeAddon,
            addonName + "_total_completion_count",
            user -> String.valueOf(this.challengesManager.getTotalChallengeCompletionCount(user, world)));

        // Completed challenge count placeholder
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(gameModeAddon,
            addonName + "_completed_count",
            user -> String.valueOf(this.challengesManager.getCompletedChallengeCount(user, world)));

        // Uncompleted challenge count placeholder
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(gameModeAddon,
            addonName + "_uncompleted_count",
            user -> String.valueOf(this.challengesManager.getChallengeCount(world) -
                this.challengesManager.getCompletedChallengeCount(user, world)));

        // Completed challenge level count placeholder
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(gameModeAddon,
            addonName + "_completed_level_count",
            user -> String.valueOf(this.challengesManager.getCompletedLevelCount(user, world)));

        // Uncompleted challenge level count placeholder
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(gameModeAddon,
            addonName + "_uncompleted_level_count",
            user -> String.valueOf(this.challengesManager.getLevelCount(world) -
                this.challengesManager.getCompletedLevelCount(user, world)));

        // Unlocked challenge level count placeholder
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(gameModeAddon,
            addonName + "_unlocked_level_count",
            user -> String.valueOf(this.challengesManager.getLevelCount(world) -
                this.challengesManager.getUnlockedLevelCount(user, world)));

        // Locked challenge level count placeholder
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(gameModeAddon,
            addonName + "_locked_level_count",
            user -> String.valueOf(this.challengesManager.getLevelCount(world) -
                this.challengesManager.getUnlockedLevelCount(user, world)));

        // Latest challenge level name placeholder
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(gameModeAddon,
            addonName + "_latest_level_name",
            user -> {
                ChallengeLevel level = this.challengesManager.getLatestUnlockedLevel(user, world);
                return level != null ? level.getFriendlyName() : "";
            });

        // Latest challenge level id placeholder
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(gameModeAddon,
            addonName + "_latest_level_id",
            user -> {
                ChallengeLevel level = this.challengesManager.getLatestUnlockedLevel(user, world);
                return level != null ? level.getUniqueId() : "";
            });

        // Completed challenge count in latest level
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(gameModeAddon,
            addonName + "_latest_level_completed_count",
            user -> {
                ChallengeLevel level = this.challengesManager.getLatestUnlockedLevel(user, world);
                return String.valueOf(level != null ?
                    this.challengesManager.getLevelCompletedChallengeCount(user, world, level) : 0);
            });

        // Uncompleted challenge count in latest level
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(gameModeAddon,
            addonName + "_latest_level_uncompleted_count",
            user -> {
                ChallengeLevel level = this.challengesManager.getLatestUnlockedLevel(user, world);
                return String.valueOf(level != null ?
                    level.getChallenges().size() - this.challengesManager.getLevelCompletedChallengeCount(user, world, level) : 0);
            });
    }


    /**
     * This method registers placeholders into GameMode addon.
     * @param gameModeAddon GameMode addon where placeholders must be hooked in.
     * @since 0.8.1
     * @deprecated remove after 0.9.0
     */
    @Deprecated
    private void registerPlaceholdersOld(GameModeAddon gameModeAddon)
    {
        final String gameMode = gameModeAddon.getDescription().getName().toLowerCase();
        final World world = gameModeAddon.getOverWorld();

        // Number of completions for all challenges placeholder
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(this,
            gameMode + "_challenge_total_completion_count",
            user -> String.valueOf(this.challengesManager.getTotalChallengeCompletionCount(user, world)));

        // Completed challenge count placeholder
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(this,
            gameMode + "_challenge_completed_count",
            user -> String.valueOf(this.challengesManager.getCompletedChallengeCount(user, world)));

        // Uncompleted challenge count placeholder
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(this,
            gameMode + "_challenge_uncompleted_count",
            user -> String.valueOf(this.challengesManager.getChallengeCount(world) -
                this.challengesManager.getCompletedChallengeCount(user, world)));

        // Completed challenge level count placeholder
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(this,
            gameMode + "_challenge_completed_level_count",
            user -> String.valueOf(this.challengesManager.getCompletedLevelCount(user, world)));

        // Uncompleted challenge level count placeholder
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(this,
            gameMode + "_challenge_uncompleted_level_count",
            user -> String.valueOf(this.challengesManager.getLevelCount(world) -
                this.challengesManager.getCompletedLevelCount(user, world)));

        // Unlocked challenge level count placeholder
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(this,
            gameMode + "_challenge_unlocked_level_count",
            user -> String.valueOf(this.challengesManager.getLevelCount(world) -
                this.challengesManager.getUnlockedLevelCount(user, world)));

        // Locked challenge level count placeholder
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(this,
            gameMode + "_challenge_locked_level_count",
            user -> String.valueOf(this.challengesManager.getLevelCount(world) -
                this.challengesManager.getUnlockedLevelCount(user, world)));

        // Latest challenge level name placeholder
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(this,
            gameMode + "_challenge_latest_level_name",
            user -> {
                ChallengeLevel level = this.challengesManager.getLatestUnlockedLevel(user, world);
                return level != null ? level.getFriendlyName() : "";
            });

        // Latest challenge level id placeholder
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(this,
            gameMode + "_challenge_latest_level_id",
            user -> {
                ChallengeLevel level = this.challengesManager.getLatestUnlockedLevel(user, world);
                return level != null ? level.getUniqueId() : "";
            });
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
     * @return the webManager
     */
    public WebManager getWebManager() {
        return this.webManager;
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
        return this.vaultHook != null && this.vaultHook.hook();
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
