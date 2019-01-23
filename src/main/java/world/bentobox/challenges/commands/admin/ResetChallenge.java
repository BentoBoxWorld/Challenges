package world.bentobox.challenges.commands.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.ChallengesManager;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;


/**
 * @deprecated Challenges can be reset via GUI.
 */
@Deprecated
public class ResetChallenge extends CompositeCommand {

    private ChallengesManager manager;

    /**
     * Admin command to complete user challenges
     * @param parent
     */
    public ResetChallenge(Addon addon, CompositeCommand parent) {
        super(addon, parent, "reset");
    }

    @Override
    public void setup() {
        this.setPermission("admin.challenges");
        this.setParametersHelp("challenges.admin.reset.parameters");
        this.setDescription("challenges.admin.reset.description");
        manager = ((ChallengesAddon)getAddon()).getChallengesManager();
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        if (args.size() != 2) {
            // Show help
            showHelp(this, user);
            return false;
        }
        // Get target player
        UUID targetUUID = getPlayers().getUUID(args.get(0));
        if (targetUUID == null) {
            user.sendMessage("general.errors.unknown-player", TextVariables.NAME, args.get(0));
            return false;
        }
        if (!getPlugin().getIslands().hasIsland(getWorld(), targetUUID)) {
            user.sendMessage("general.errors.player-has-no-island");
            return false;
        }
        // Check for valid challenge name
        if (!manager.isChallenge(getWorld(), args.get(1))) {
            user.sendMessage("challenges.admin.complete.unknown-challenge");
            return false;
        }
        // Complete challenge
        User target = User.getInstance(targetUUID);
        manager.setResetChallenge(target, args.get(1), getWorld());
        user.sendMessage("general.success");
        return true;
    }

    @Override
    public Optional<List<String>> tabComplete(User user, String alias, List<String> args) {
        String lastArg = !args.isEmpty() ? args.get(args.size()-1) : "";
        if (args.size() == 3) {
            // Online players
            return Optional.of(Util.tabLimit(new ArrayList<>(Util.getOnlinePlayerList(user)), lastArg));
        } else if (args.size() == 4) {
            // Challenges in this world
            return Optional.of(Util.tabLimit(manager.getAllChallengesList(getWorld()), lastArg));
        }
        return Optional.empty();
    }
}
