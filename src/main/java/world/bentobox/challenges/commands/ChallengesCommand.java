package world.bentobox.challenges.commands;

import java.util.List;

import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.panel.user.ChallengesGUI;


public class ChallengesCommand extends CompositeCommand {
    public static final String CHALLENGE_COMMAND = "challenges";

    public ChallengesCommand(ChallengesAddon addon, CompositeCommand cmd) {
        super(addon, cmd, CHALLENGE_COMMAND);
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        // Open up the challenges GUI
        if (user.isPlayer()) {
            new ChallengesGUI((ChallengesAddon) this.getAddon(),
                this.getWorld(),
                user,
                this.getTopLabel(),
                this.getPermissionPrefix()).build();
            return true;
        }
        // Show help
        showHelp(this, user);
        return false;
    }

    @Override
    public void setup() {
        this.setPermission(CHALLENGE_COMMAND);
        this.setParametersHelp(CHALLENGE_COMMAND + ".parameters");
        this.setDescription(CHALLENGE_COMMAND + ".description");
    }


}
