package bskyblock.addon.challenges.commands;

import java.util.List;

import bskyblock.addon.challenges.ChallengesAddon;
import bskyblock.addon.challenges.panel.ChallengesPanels;
import us.tastybento.bskyblock.api.commands.CompositeCommand;
import us.tastybento.bskyblock.api.user.User;

public class ChallengesCommand extends CompositeCommand {
    public static final String CHALLENGE_COMMAND = "challenges";
    private ChallengesAddon addon;

    public ChallengesCommand(ChallengesAddon addon, CompositeCommand cmd) {
        super(cmd, CHALLENGE_COMMAND);
        this.addon = addon;
    }

    @Override
    public boolean execute(User user, List<String> args) {
        // Open up the challenges GUI
        if (user.isPlayer()) {
            new ChallengesPanels(addon, user, args.isEmpty() ? "" : args.get(0), getWorld(), getPermissionPrefix(), getTopLabel());
            return true;
        } 
        return false;
    }

    @Override
    public void setup() {
        this.setOnlyPlayer(true);
        this.setPermission(getPermissionPrefix() + CHALLENGE_COMMAND);
        this.setParameters(CHALLENGE_COMMAND + ".parameters");
        this.setDescription(CHALLENGE_COMMAND + ".description");
        this.setOnlyPlayer(true);  
    }


}
