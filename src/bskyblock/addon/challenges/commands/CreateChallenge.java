package bskyblock.addon.challenges.commands;

import java.util.List;

import bskyblock.addon.challenges.Challenges;
import bskyblock.addon.challenges.panel.CreateChallengeListener;
import us.tastybento.bskyblock.Constants;
import us.tastybento.bskyblock.api.commands.CompositeCommand;
import us.tastybento.bskyblock.api.commands.User;
import us.tastybento.bskyblock.api.panels.builders.PanelBuilder;

public class CreateChallenge extends CompositeCommand {


    private Challenges addon;

    /**
     * Admin command to make challenges
     * @param parent
     */
    public CreateChallenge(Challenges addon, ChallengesCommand parent) {
        super(parent, "create");
        this.addon = addon;
    }

    @Override
    public void setup() {
        this.setOnlyPlayer(true);
        this.setPermission(Constants.PERMPREFIX + "admin.challenges");
        this.setParameters("challaneges.admin.create.parameters");
        this.setDescription("challenges.admin.create.description");
    }

    @Override
    public boolean execute(User user, List<String> args) {
        if (args.isEmpty()) {
            user.sendRawMessage("not enough args");
            return false;
        }
        new PanelBuilder()
        .setName(args.get(0))
        .setSize(49)
        .setListener(new CreateChallengeListener(addon, user))
        .setUser(user)
        .build();
        return true;
    }

}
