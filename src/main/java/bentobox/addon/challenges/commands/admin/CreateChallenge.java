package bentobox.addon.challenges.commands.admin;

import java.util.List;

import bentobox.addon.challenges.ChallengesAddon;
import bentobox.addon.challenges.panel.CreateChallengeListener;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.user.User;

public class CreateChallenge extends CompositeCommand {

    /**
     * Admin command to make challenges
     * @param parent
     */
    public CreateChallenge(Addon addon, CompositeCommand parent) {
        super(addon, parent, "create");
        new CreateSurrounding(addon, this);
    }

    @Override
    public void setup() {
        this.setOnlyPlayer(true);
        this.setPermission("admin.challenges");
        this.setParametersHelp("challaneges.admin.create.parameters");
        this.setDescription("challenges.admin.create.description");

    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        if (args.isEmpty()) {
            user.sendRawMessage("not enough args");
            return false;
        }
        new PanelBuilder()
        .name(args.get(0))
        .size(49)
        .listener(new CreateChallengeListener((ChallengesAddon) getAddon(), user))
        .user(user)
        .build();
        return true;
    }

}
