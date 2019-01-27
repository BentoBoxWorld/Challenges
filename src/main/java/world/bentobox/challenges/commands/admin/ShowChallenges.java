package world.bentobox.challenges.commands.admin;

import java.util.List;

import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;

public class ShowChallenges extends CompositeCommand {


    /**
     * Admin command to show challenges and manage them
     * @param parent
     */
    public ShowChallenges(Addon addon, CompositeCommand parent) {
        super(addon, parent, "show");
    }

    @Override
    public void setup() {
        this.setPermission("admin.challenges");
        this.setParametersHelp("challaneges.admin.show.parameters");
        this.setDescription("challenges.admin.show.description");

    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        ((ChallengesAddon)getAddon()).getChallengesManager().getAllChallengesNames(this.getWorld()).forEach(user::sendRawMessage);
        return true;
    }

}
