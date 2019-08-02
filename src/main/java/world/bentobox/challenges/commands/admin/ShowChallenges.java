package world.bentobox.challenges.commands.admin;

import java.util.List;
import java.util.logging.Level;

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
        this.setParametersHelp("challenges.commands.admin.show.parameters");
        this.setDescription("challenges.commands.admin.show.description");

    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        if (user.isPlayer())
        {
            ((ChallengesAddon) getAddon()).getChallengesManager().
                getAllChallengesNames(this.getWorld()).forEach(user::sendRawMessage);
        }
        else
        {
            ((ChallengesAddon) getAddon()).getChallengesManager().
                getAllChallengesNames(this.getWorld()).forEach(c -> this.getAddon().log(c));
        }

        return true;

    }

}
