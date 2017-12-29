package bskyblock.addon.challenges.commands;

import java.util.List;
import java.util.UUID;

import bskyblock.addon.challenges.Challenges;
import us.tastybento.bskyblock.api.commands.CompositeCommand;
import us.tastybento.bskyblock.api.commands.User;
import us.tastybento.bskyblock.config.Settings;

public class CreateChallenge extends CompositeCommand {


    private Challenges plugin;

    public CreateChallenge(Challenges plugin, ChallengesCommand parent) {
        super(parent, "create");
        this.plugin = plugin;
    }

    @Override
    public void setup() {
        this.setOnlyPlayer(true);
        this.setPermission(Settings.PERMPREFIX + "challenges");
        this.setDescription("addon.challenges.create.description");
        this.setUsage("addon.challenges.create.usage");
    }

    @Override
    public boolean execute(User user, List<String> args) {
        // Create a copy of items in the player's main inventory
        String name = UUID.randomUUID().toString();
        if (args.size() > 0) {
            name = args.get(0);
        }
        plugin.getManager().createChallenge(user, name);
        return false;
    }

}
