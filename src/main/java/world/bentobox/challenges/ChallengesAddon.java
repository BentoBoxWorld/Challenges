package world.bentobox.challenges;

import org.bukkit.Bukkit;

import world.bentobox.challenges.commands.ChallengesCommand;
import world.bentobox.challenges.commands.admin.Challenges;
import world.bentobox.challenges.listeners.ResetListener;
import world.bentobox.challenges.listeners.SaveListener;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;

/**
 * Add-on to BSkyBlock that enables challenges
 * @author tastybento
 *
 */
public class ChallengesAddon extends Addon {

    private ChallengesManager challengesManager;
    private String permissionPrefix = "addon";
    private FreshSqueezedChallenges importManager;
    private boolean hooked;

    @Override
    public void onLoad() {
        // Save default config.yml
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        // Check if it is enabled - it might be loaded, but not enabled.
        if (getPlugin() == null || !getPlugin().isEnabled()) {
            Bukkit.getLogger().severe("BentoBox is not available or disabled!");
            this.setState(State.DISABLED);
            return;
        }

        // Challenges Manager
        challengesManager = new ChallengesManager(this);
        // Challenge import setup
        importManager = new FreshSqueezedChallenges(this);

        this.getPlugin().getAddonsManager().getGameModeAddons().forEach(gameModeAddon -> {
            if (gameModeAddon.getPlayerCommand().isPresent())
            {
                new ChallengesCommand(this, gameModeAddon.getPlayerCommand().get());
                this.hooked = true;
            }

            if (gameModeAddon.getAdminCommand().isPresent())
            {
                new Challenges(this, gameModeAddon.getAdminCommand().get());
                this.hooked = true;
            }
        });
        
        // If the add-on never hooks in, then it is useless
        if (!hooked) {
            logError("Challenges could not hook into AcidIsland or BSkyBlock so will not do anything!");
            this.setState(State.DISABLED);
            return;
        }
        // Try to find Level addon and if it does not exist, display a warning
        if (!getAddonByName("Level").isPresent()) {
            logWarning("Level add-on not found so level challenges will not work!");
        }
        // Register the reset listener
        this.registerListener(new ResetListener(this));
        // Register the autosave listener.
        this.registerListener(new SaveListener(this));
        // Done
    }

    @Override
    public void onDisable(){
        if (challengesManager != null) {
            challengesManager.save();
        }
    }

    public ChallengesManager getChallengesManager() {
        return challengesManager;
    }

    @Override
    public String getPermissionPrefix() {
        return permissionPrefix ;
    }

    /**
     * @return the importManager
     */
    public FreshSqueezedChallenges getImportManager() {
        return importManager;
    }

}
