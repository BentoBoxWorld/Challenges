package bskyblock.addon.challenges;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import bskyblock.addon.challenges.database.object.ChallengeLevels;
import bskyblock.addon.challenges.database.object.Challenges;

public class FreshSqueezedChallenges {

	ChallengesAddon addon;
	YamlConfiguration chal;

	public FreshSqueezedChallenges(ChallengesAddon challengesAddon) {
		this.addon = challengesAddon;
		File challengeFile = new File(addon.getDataFolder(), "challenges.yml");
		if (!challengeFile.exists()) {
			addon.saveResource("challenges.yml",false);
		}
		chal = new YamlConfiguration();
		try {
			chal.load(challengeFile);
		} catch (IOException | InvalidConfigurationException e) {
			addon.getLogger().severe("Could not set up initial challenges");
		}
		makeLevels();
		makeChallenges();
		addon.getChallengesManager().save(true);
	}

	private void makeLevels() {
		// Parse the levels
		String levels = chal.getString("challenges.levels", "");
		if (!levels.isEmpty()) {
			String[] lvs = levels.split(" ");
			int order = 0;
			for (String level : lvs) {
				ChallengeLevels challengeLevel = new ChallengeLevels();
				challengeLevel.setFriendlyName(level);
				challengeLevel.setUniqueId(level);
				challengeLevel.setOrder(order++);
				challengeLevel.setWaiveramount(chal.getInt("challenges.waiveramount"));
				// Check if there is a level reward
				ConfigurationSection unlock = chal.getConfigurationSection("challenges.levelUnlock." + level);
				if (unlock != null) {
					challengeLevel.setUnlockMessage(unlock.getString("message"));
					challengeLevel.setRewardDescription(unlock.getString("rewardDesc",""));
					challengeLevel.setRewardItems(parseItems(unlock.getString("itemReward","")));
					challengeLevel.setMoneyReward(unlock.getInt("moneyReward"));
					challengeLevel.setExpReward(unlock.getInt("expReward"));
					challengeLevel.setRewardCommands(unlock.getStringList("commands"));
				}
				addon.getChallengesManager().storeLevel(challengeLevel);
			}
		}		
	}

	/**
	 * Imports challenges
	 */
	private void makeChallenges() {
		// Parse the challenge file
		ConfigurationSection chals = chal.getConfigurationSection("challenges.challengeList");
		for (String challenge : chals.getKeys(false)) {
			Challenges newChallenge = new Challenges();
			newChallenge.setUniqueId(challenge);
			ConfigurationSection details = chals.getConfigurationSection(challenge);
			newChallenge.setFriendlyName(details.getString("friendlyname", challenge));
			newChallenge.setDescription(addon.getChallengesManager().stringSplit(details.getString("description", "")));
			newChallenge.setIcon(new ParseItem(addon, details.getString("icon") + ":1").getItem());
			newChallenge.setLevel(details.getString("level", ChallengesManager.FREE));
			newChallenge.setChallengeType(Challenges.ChallengeType.valueOf(details.getString("type","INVENTORY").toUpperCase()));
			newChallenge.setTakeItems(details.getBoolean("takeItems",true));
			newChallenge.setRewardText(details.getString("rewardText", ""));
			newChallenge.setRewardCommands(details.getStringList("rewardcommands"));
			newChallenge.setMoneyReward(details.getInt("moneyReward",0));
			newChallenge.setExpReward(details.getInt("expReward"));
			newChallenge.setRepeatable(details.getBoolean("repeatable"));
			newChallenge.setRepeatRewardText(details.getString("repeatRewardText",""));
			newChallenge.setRepeatMoneyReward(details.getInt("repearMoneyReward"));
			newChallenge.setRepeatExpReward(details.getInt("repeatExpReward"));
			newChallenge.setRepeatRewardCommands(details.getStringList("repeatrewardcommands"));
			newChallenge.setMaxTimes(details.getInt("maxtimes"));
			// TODO reset allowed
			newChallenge.setReqMoney(details.getInt("requiredMoney"));
			newChallenge.setReqExp(details.getInt("requiredExp"));
			String reqItems = details.getString("requiredItems","");
			if (newChallenge.getChallengeType().equals(Challenges.ChallengeType.INVENTORY)) {
				newChallenge.setRequiredItems(parseItems(reqItems));
			} else if (newChallenge.getChallengeType().equals(Challenges.ChallengeType.LEVEL)) {
				newChallenge.setReqIslandlevel(Long.parseLong(reqItems));
			} else if (newChallenge.getChallengeType().equals(Challenges.ChallengeType.ISLAND)) {
				parseEntities(newChallenge, reqItems);
			}
			newChallenge.setItemReward(parseItems(details.getString("itemReward")));
			newChallenge.setRepeatItemReward(parseItems(details.getString("repeatItemReward")));
			// Save
			addon.getChallengesManager().storeChallenge(newChallenge);
		}
		addon.getChallengesManager().sortChallenges();
	}

	/**
	 * Run through entity types and materials and try to match to the string given
	 * @param challenge - challenge to be adjusted
	 * @param string - string from YAML file
	 */
	private void parseEntities(Challenges challenge, String string) {
		Map<EntityType, Integer> req = new EnumMap<>(EntityType.class);
		Map<Material, Integer> blocks = new EnumMap<>(Material.class);
		if (!string.isEmpty()) {
			for (String s : string.split(" ")) {
				String[] part = s.split(":");
				try {
					Arrays.asList(EntityType.values()).stream().filter(t -> t.name().equalsIgnoreCase(part[0])).forEach(t -> req.put(t, Integer.valueOf(part[1])));
					Arrays.asList(Material.values()).stream().filter(t -> t.name().equalsIgnoreCase(part[0])).forEach(t -> blocks.put(t, Integer.valueOf(part[1])));
				} catch (Exception e) {
					addon.getLogger().severe("Cannot parse '" + s + "'. Skipping...");
				}
			}
		}
		challenge.setRequiredEntities(req);
		challenge.setRequiredBlocks(blocks);
	}

	private List<ItemStack> parseItems(String reqList) {
		List<ItemStack> result = new ArrayList<>();
		if (!reqList.isEmpty()) {
			for (String s : reqList.split(" ")) {
				ItemStack item = new ParseItem(addon,s).getItem();
				if (item != null) {
					result.add(item);
				}
			}
		}
		return result;
	}

}