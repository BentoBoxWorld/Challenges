package bentobox.addon.challenges;

import org.bukkit.Bukkit;

import bentobox.addon.challenges.commands.ChallengesCommand;
import bentobox.addon.challenges.commands.admin.Challenges;
import bentobox.addon.challenges.listeners.ResetListener;
import bentobox.addon.challenges.listeners.SaveListener;
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

        // Register commands - run one tick later to allow all addons to load
        // AcidIsland hook in
        getPlugin().getAddonsManager().getAddonByName("AcidIsland").ifPresent(a -> {
            CompositeCommand acidIslandCmd = getPlugin().getCommandsManager().getCommand("ai");
            if (acidIslandCmd != null) {
                new ChallengesCommand(this, acidIslandCmd);
                CompositeCommand acidCmd = getPlugin().getCommandsManager().getCommand("acid");
                new Challenges(this, acidCmd);
                hooked = true;
            }
        });
        getPlugin().getAddonsManager().getAddonByName("BSkyBlock").ifPresent(a -> {
            // BSkyBlock hook in
            CompositeCommand bsbIslandCmd = getPlugin().getCommandsManager().getCommand("island");
            if (bsbIslandCmd != null) {
                new ChallengesCommand(this, bsbIslandCmd);
                CompositeCommand bsbAdminCmd = getPlugin().getCommandsManager().getCommand("bsbadmin");
                new Challenges(this, bsbAdminCmd);
                hooked = true;
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
            challengesManager.save(false);
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
