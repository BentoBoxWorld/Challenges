package bentobox.addon.challenges.commands;

import java.util.List;

import bentobox.addon.challenges.ChallengesAddon;
import bentobox.addon.challenges.panel.ChallengesPanels2;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;

public class ChallengesCommand extends CompositeCommand {
    public static final String CHALLENGE_COMMAND = "challenges";

    public ChallengesCommand(ChallengesAddon addon, CompositeCommand cmd) {
        super(addon, cmd, CHALLENGE_COMMAND);
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        // Open up the challenges GUI
        if (user.isPlayer()) {
            new ChallengesPanels2((ChallengesAddon) getAddon(), user, args.isEmpty() ? "" : args.get(0), getWorld(), getPermissionPrefix(), getTopLabel(), false);
            return true;
        }
        return false;
    }

    @Override
    public void setup() {
        this.setOnlyPlayer(true);
        this.setPermission(CHALLENGE_COMMAND);
        this.setParametersHelp(CHALLENGE_COMMAND + ".parameters");
        this.setDescription(CHALLENGE_COMMAND + ".description");
        this.setOnlyPlayer(true);
    }


}
