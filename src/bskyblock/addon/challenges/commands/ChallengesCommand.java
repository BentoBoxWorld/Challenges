package bskyblock.addon.challenges.commands;

import java.util.List;

import bskyblock.addon.challenges.Challenges;
import us.tastybento.bskyblock.api.commands.CompositeCommand;
import us.tastybento.bskyblock.api.commands.User;
import us.tastybento.bskyblock.config.Settings;

public class ChallengesCommand extends CompositeCommand {
    private static final String CHALLENGE_COMMAND = "challenges";
    private Challenges plugin;

    public ChallengesCommand(Challenges plugin) {
        super(CHALLENGE_COMMAND, "c", "challenge");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(User user, List<String> args) {
        // Open up the challenges GUI
        if (user.isPlayer()) {
            plugin.getManager().getChallengesPanels().getChallenges(user);
            return true;
        } 
        return false;
    }

    @Override
    public void setup() {
        this.setOnlyPlayer(true);
        this.setPermission(Settings.PERMPREFIX + "challenges");
        this.setDescription("addon.challenges.help");
        this.setUsage("addon.challenges.usage");
        
        // Set up create command
        new CreateChallenge(plugin, this);
    }


}
