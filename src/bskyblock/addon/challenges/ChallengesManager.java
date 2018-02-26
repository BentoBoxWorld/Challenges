package bskyblock.addon.challenges;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import bskyblock.addon.challenges.commands.admin.SurroundChallengeBuilder;
import bskyblock.addon.challenges.database.object.ChallengeLevels;
import bskyblock.addon.challenges.database.object.Challenges;
import bskyblock.addon.challenges.database.object.Challenges.ChallengeType;
import bskyblock.addon.challenges.panel.ChallengesPanels;
import us.tastybento.bskyblock.BSkyBlock;
import us.tastybento.bskyblock.api.commands.User;
import us.tastybento.bskyblock.api.configuration.BSBConfig;

public class ChallengesManager {

    public static final String FREE = "Free";
    private LinkedHashMap<ChallengeLevels, List<Challenges>> challengeList;
    private BSBConfig<Challenges> chConfig;
    private BSBConfig<ChallengeLevels> lvConfig;
    private ChallengesPanels challengesPanels;

    public ChallengesManager(ChallengesAddon addon) {
        // Set up the configs
        chConfig = new BSBConfig<Challenges>(addon, Challenges.class);
        lvConfig = new BSBConfig<ChallengeLevels>(addon, ChallengeLevels.class);
        challengeList = new LinkedHashMap<>();
        // Start panels
        challengesPanels = new ChallengesPanels(addon, this);
        load();
    }

    public long checkChallengeTimes(User user, Challenges challenge) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Creates an inventory challenge
     * @param user - the user who is making the challenge
     * @param inventory - the inventory that will be used to make the challenge
     */
    public boolean createInvChallenge(User user, Inventory inventory) {
        if (inventory.getContents().length == 0) {
            return false;
        }
        Challenges newChallenge = new Challenges();
        newChallenge.setChallengeType(ChallengeType.INVENTORY);
        newChallenge.setFriendlyName(inventory.getTitle());
        newChallenge.setDeployed(false);
        List<ItemStack> requiredItems = new ArrayList<>();
        inventory.forEach(item -> {
            if (item != null && !item.getType().equals(Material.AIR)) {
                requiredItems.add(item);
            }
        });
        newChallenge.setRequiredItems(requiredItems);
        newChallenge.setTakeItems(true);
        newChallenge.setUniqueId(inventory.getTitle());
        newChallenge.setIcon(new ItemStack(Material.EMPTY_MAP));
        newChallenge.setFreeChallenge(true);
        newChallenge.setLevel(FREE);

        // Move all the items back to the player's inventory
        inventory.forEach(item -> {
            if (item != null) {
                Map<Integer, ItemStack> residual = user.getInventory().addItem(item);
                // Drop any residual items at the foot of the player
                residual.forEach((k, v) -> {
                    user.getWorld().dropItem(user.getLocation(), v);
                });
            }
        });

        // Save the challenge
        if (!chConfig.saveConfigObject(newChallenge)) {
            user.sendRawMessage(ChatColor.RED + "Challenge creation failed!");
            return false;
        }
        user.sendRawMessage("Success");
        return true;
    }
    
    /**
     * Create a surrounding challenge
     * @param challengeInfo - info on the challenge from the builder
     * @return true if successful, false if not
     */
    public boolean createSurroundingChallenge(SurroundChallengeBuilder challengeInfo) {
        if (challengeInfo.getReqBlocks().isEmpty() && challengeInfo.getReqEntities().isEmpty()) {
            challengeInfo.getOwner().sendMessage("challenges.error.no-items-clicked");
            return false;
        }
        Challenges newChallenge = new Challenges();
        newChallenge.setChallengeType(ChallengeType.SURROUNDING);
        newChallenge.setFriendlyName(challengeInfo.getName());
        newChallenge.setDeployed(true);
        newChallenge.setRequiredBlocks(challengeInfo.getReqBlocks());
        newChallenge.setRequiredEntities(challengeInfo.getReqEntities());
        newChallenge.setUniqueId(challengeInfo.getName());
        newChallenge.setIcon(new ItemStack(Material.ARMOR_STAND));
        newChallenge.setFreeChallenge(true);
        newChallenge.setLevel(FREE);

        // Save the challenge
        if (!chConfig.saveConfigObject(newChallenge)) {
            challengeInfo.getOwner().sendMessage("challenges.error.could-not-save");
            return false;
        }
        return true;
    }

    /**
     * Get the status on every level
     * @param user
     * @return Level name, how many challenges still to do on which level
     */
    public List<LevelStatus> getChallengeLevelStatus(User user) {
        List<LevelStatus> result = new ArrayList<>();
        ChallengeLevels previousLevel = null;
        for (Entry<ChallengeLevels, List<Challenges>> en : challengeList.entrySet()) {
            int challsToDo = 0; // TODO - calculate how many challenges still to do for this player
            boolean complete = false; // TODO
            result.add(new LevelStatus(en.getKey(), previousLevel, challsToDo, complete));
        }
        return result;
    }

    /**
     * Get the list of challenges for this level
     * @param level - the level required
     * @return the list of challenges for this level, or the first set of challenges if level is blank, or a blank list if there are no challenges
     */
    public List<Challenges> getChallenges(String level) {
        return challengeList.getOrDefault(level, challengeList.isEmpty() ? new ArrayList<Challenges>() : challengeList.values().iterator().next());
    }

    /**
     * @return the challengesPanels
     */
    public ChallengesPanels getChallengesPanels() {
        return challengesPanels;
    }

    /**
     * Get the previous level to the one supplied
     * @param currentLevel - the current level
     * @return the previous level, or null if there is none
     */
    public ChallengeLevels getPreviousLevel(ChallengeLevels currentLevel) {
        ChallengeLevels result = null;
        for (ChallengeLevels level : challengeList.keySet()) {
            if (level.equals(currentLevel)) {
                return result;
            }
            result = level;
        }
        return result;
    }

    /**
     * Checks if a challenge is complete or not
     * @param uniqueId - unique ID - player's UUID
     * @param uniqueId2 - Challenge id
     * @return - true if completed
     */
    public boolean isChallengeComplete(User user, String uniqueId2) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isLevelAvailable(User user, String level) {
        // TODO
        return false;
    }

    public boolean isLevelComplete(User user, ChallengeLevels otherLevel) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * Clear and reload all challenges
     */
    public void load() {
        // Load the challenges
        challengeList.clear();
        for (Challenges challenge : chConfig.loadConfigObjects()) {
            Bukkit.getLogger().info("Loading challenge " + challenge.getFriendlyName() + " level " + challenge.getLevel());
            // See if we have this level already
            ChallengeLevels level;
            if (lvConfig.configObjectExists(challenge.getLevel())) {
                //Bukkit.getLogger().info("DEBUG: Level contains level " + challenge.getLevel());
                // Get it from the database
                level = lvConfig.loadConfigObject(challenge.getLevel());
            } else {
                //Bukkit.getLogger().info("DEBUG: Level does not contains level " + challenge.getLevel());
                // Make it
                level = new ChallengeLevels();
                level.setUniqueId(challenge.getLevel());
                //Bukkit.getLogger().info("DEBUG: Level unique Id set to " + level.getUniqueId());
                lvConfig.saveConfigObject(level);
            }
            if (challengeList.containsKey(level)) {
                //Bukkit.getLogger().info("DEBUG: Challenge contains level " + level.getUniqueId());
                challengeList.get(level).add(challenge);                    
            } else {
                //Bukkit.getLogger().info("DEBUG: No key found");
                // First challenge of this level type
                List<Challenges> challenges = new ArrayList<>();
                challenges.add(challenge);
                challengeList.put(level, challenges);
            }
        }
        //Bukkit.getLogger().info("DEBUG: " + challengeList.size());
        // Sort the challenge list into level order
        challengeList = challengeList.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        //Bukkit.getLogger().info("DEBUG: " + challengeList.size());
    }

    private void save() {
        challengeList.entrySet().forEach(en -> {
            lvConfig.saveConfigObject(en.getKey());
            en.getValue().forEach(chConfig::saveConfigObject);
        });
    }

    /**
     * Save to the database
     * @param async - if true, saving will be done async
     */
    public void save(boolean async) {
        if (async) {
            BSkyBlock.getInstance().getServer().getScheduler().runTaskAsynchronously(BSkyBlock.getInstance(), () -> save());
        } else {
            save();
        }
    }

    /**
     * Sets the challenge as complete and increments the number of times it has been completed
     * @param user
     * @param uniqueId
     */
    public void setChallengeComplete(User user, String uniqueId) {
        // TODO Auto-generated method stub

    }

}
