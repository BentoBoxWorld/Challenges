package bskyblock.addon.challenges.commands;

import java.util.List;

import bskyblock.addon.challenges.ChallengesAddon;
import us.tastybento.bskyblock.api.commands.CompositeCommand;
import us.tastybento.bskyblock.api.user.User;

public class ChallengesCommand extends CompositeCommand {

	public static final String CHALLENGE_COMMAND = "challenges";

	private ChallengesAddon addon;

	public ChallengesCommand(ChallengesAddon addon) {
		super(CHALLENGE_COMMAND, "c", "challenge");
		this.addon = addon;
		// Set up commands
	}

	@Override
	public boolean execute(User user, List<String> args) {
		// Open up the challenges GUI
		if (user.isPlayer()) {
			addon.getChallengesManager().getChallengesPanels().getChallenges(user, args.isEmpty() ? "" : args.get(0));
			return true;
		} 
		return false;
	}

	@Override
	public void setup() {
		this.setOnlyPlayer(true);
		this.setPermission("bskyblock." + CHALLENGE_COMMAND);
		this.setParameters(CHALLENGE_COMMAND + ".parameters");
		this.setDescription(CHALLENGE_COMMAND + ".description");
		this.setOnlyPlayer(true);  
	}

}