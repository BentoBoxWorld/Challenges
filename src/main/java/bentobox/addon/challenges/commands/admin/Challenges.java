package bentobox.addon.challenges.commands.admin;

import java.util.List;

import bentobox.addon.challenges.ChallengesAddon;
import bentobox.addon.challenges.panel.ChallengesPanels2;
import bentobox.addon.challenges.panel.ChallengesPanels2.Mode;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;

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
        new CompleteChallenge(getAddon(), this);
        new ReloadChallenges(getAddon(), this);
        //new ShowChallenges(getAddon(), this);
        //new CreateChallenge(getAddon(), this);

    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        // Open up the admin challenges GUI
        if (user.isPlayer()) {
            new ChallengesPanels2((ChallengesAddon) getAddon(), user, user, args.isEmpty() ? "" : args.get(0), getWorld(), getPermissionPrefix(), getTopLabel(), Mode.ADMIN);
            return true;
        }
        return false;
    }

}
