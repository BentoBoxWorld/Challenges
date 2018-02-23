package bskyblock.addon.challenges.commands.admin;

import java.util.List;

import bskyblock.addon.challenges.Challenges;
import us.tastybento.bskyblock.Constants;
import us.tastybento.bskyblock.api.commands.CompositeCommand;
import us.tastybento.bskyblock.api.commands.User;

public class ChallengesAdminCommand extends CompositeCommand {
    private static final String CHALLENGE_ADMIN_COMMAND = "cadmin";
    private Challenges addon;

    public ChallengesAdminCommand(Challenges addon) {
        super(CHALLENGE_ADMIN_COMMAND);
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
        this.setPermission(Constants.PERMPREFIX + "challenges.admin");
        this.setParameters("challaneges.admin.parameters");
        this.setDescription("challenges.admin.description");
        this.setOnlyPlayer(true);  
    }


}
