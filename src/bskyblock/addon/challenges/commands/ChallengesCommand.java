package bskyblock.addon.challenges.commands;

import java.util.List;

import bskyblock.addon.challenges.Challenges;
import us.tastybento.bskyblock.Constants;
import us.tastybento.bskyblock.api.commands.CompositeCommand;
import us.tastybento.bskyblock.api.commands.User;

public class ChallengesCommand extends CompositeCommand {
    private static final String CHALLENGE_COMMAND = "challenges";
    private Challenges addon;

    public ChallengesCommand(Challenges addon) {
        super(CHALLENGE_COMMAND, "c", "challenge");
        this.addon = addon;
        // Set up create command
        new CreateChallenge(addon, this);
    }

    @Override
    public boolean execute(User user, List<String> args) {
        // Open up the challenges GUI
        if (user.isPlayer()) {
            addon.getChallengesManager().getChallengesPanels().getChallenges(user);
            return true;
        } 
        return false;
    }

    @Override
    public void setup() {
        this.setOnlyPlayer(true);
        this.setPermission(Constants.PERMPREFIX + "challenges");
        this.setParameters("challaneges.parameters");
        this.setDescription("challenges.description");
        this.setOnlyPlayer(true);  
    }


}
