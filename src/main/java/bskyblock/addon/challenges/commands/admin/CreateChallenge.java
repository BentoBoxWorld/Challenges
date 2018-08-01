package bskyblock.addon.challenges.commands.admin;

import java.util.List;

import bskyblock.addon.challenges.ChallengesAddon;
import bskyblock.addon.challenges.panel.CreateChallengeListener;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.user.User;

public class CreateChallenge extends CompositeCommand {


    private ChallengesAddon addon;

    /**
     * Admin command to make challenges
     * @param parent
     */
    public CreateChallenge(ChallengesAddon addon, CompositeCommand parent) {
        super(parent, "create");
        this.addon = addon;
        new CreateSurrounding(addon, this);

    }

    @Override
    public void setup() {
        this.setOnlyPlayer(true);
        this.setPermission("admin.challenges");
        this.setParameters("challaneges.admin.create.parameters");
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
        .listener(new CreateChallengeListener(addon, user))
        .user(user)
        .build();
        return true;
    }

}
