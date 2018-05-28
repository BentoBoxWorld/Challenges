package bskyblock.addon.challenges.commands.admin;

import java.util.List;

import bskyblock.addon.challenges.ChallengesAddon;
import us.tastybento.bskyblock.api.commands.CompositeCommand;
import us.tastybento.bskyblock.api.user.User;

public class ChallengesAdminCommand extends CompositeCommand {
    private static final String CHALLENGE_ADMIN_COMMAND = "cadmin";

    public ChallengesAdminCommand(ChallengesAddon addon, CompositeCommand cmd) {
        super(cmd, CHALLENGE_ADMIN_COMMAND);
        // Set up create command
        new CreateChallenge(addon, this);
        new SetIcon(addon, this);
    }

    @Override
    public boolean execute(User user, List<String> args) {
        return false;
    }

    @Override
    public void setup() {
        this.setOnlyPlayer(true);
        this.setPermission(getPermissionPrefix() + "challenges.admin");
        this.setParameters("challaneges.admin.parameters");
        this.setDescription("challenges.admin.description");
        this.setOnlyPlayer(true);  
    }

}
