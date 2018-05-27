package bskyblock.addon.challenges.commands.admin;

import java.util.List;

import bskyblock.addon.challenges.ChallengesAddon;
import us.tastybento.bskyblock.api.commands.CompositeCommand;
import us.tastybento.bskyblock.api.user.User;

public class ChallengesAdminCommand extends CompositeCommand {

	private static final String CHALLENGE_ADMIN_COMMAND = "cadmin";

	private ChallengesAddon addon;

	public ChallengesAdminCommand(ChallengesAddon addon) {
		super(CHALLENGE_ADMIN_COMMAND);
		this.addon = addon;
		// Set up create command
		new CreateChallenge(addon, this);
		new SetIcon(addon, this);
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
		this.setPermission("bskyblock.challenges.admin");
		this.setParameters("challenges.admin.parameters");
		this.setDescription("challenges.admin.description");
		this.setOnlyPlayer(true);  
	}

}