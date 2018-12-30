package world.bentobox.challenges.commands.admin;

import java.util.List;

import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.ChallengesManager;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;

public class ReloadChallenges extends CompositeCommand {

    private ChallengesManager manager;

    /**
     * Admin command to complete user challenges
     * @param parent
     */
    public ReloadChallenges(Addon addon, CompositeCommand parent) {
        super(addon, parent, "reload");
    }

    @Override
    public void setup() {
        this.setPermission("admin.challenges");
        this.setParametersHelp("challenges.admin.reload.parameters");
        this.setDescription("challenges.admin.reload.description");
        manager = ((ChallengesAddon)getAddon()).getChallengesManager();
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        if (!args.isEmpty()) {
            // Show help
            showHelp(this, user);
            return false;
        }
        manager.load();
        user.sendMessage("general.success");
        return true;
    }

}
