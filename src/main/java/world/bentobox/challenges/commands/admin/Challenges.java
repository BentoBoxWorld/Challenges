package world.bentobox.challenges.commands.admin;

import java.util.List;

import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.panel.admin.AdminGUI;


public class Challenges extends CompositeCommand {

    /**
     * Admin command for challenges
     * @param parent
     */
    public Challenges(ChallengesAddon addon, CompositeCommand parent) {
        super(addon, parent, "challenges");
    }

    @Override
    public void setup() {
        this.setPermission("admin.challenges");
        this.setParametersHelp("challeneges.admin.parameters");
        this.setDescription("challenges.admin.description");
        // Register sub commands
        new ImportCommand(getAddon(), this);
        // new CompleteChallenge(getAddon(), this);
        new ReloadChallenges(getAddon(), this);
        new ResetChallenge(getAddon(), this);
        //new ShowChallenges(getAddon(), this);
        //new CreateChallenge(getAddon(), this);

    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        // Open up the admin challenges GUI
        if (user.isPlayer()) {
            new AdminGUI((ChallengesAddon) this.getAddon(),
                this.getWorld(),
                user,
                this.getTopLabel(),
                this.getPermissionPrefix()).build();

            return true;
        }
        return false;
    }

}
