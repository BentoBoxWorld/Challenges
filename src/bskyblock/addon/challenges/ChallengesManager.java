package bskyblock.addon.challenges;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import bskyblock.addon.challenges.database.object.ChallengesData;
import bskyblock.addon.challenges.database.object.ChallengesData.ChallengeType;
import bskyblock.addon.challenges.database.object.LevelsDO;
import bskyblock.addon.challenges.panel.ChallengesPanels;
import us.tastybento.bskyblock.BSkyBlock;
import us.tastybento.bskyblock.api.commands.User;
import us.tastybento.bskyblock.database.flatfile.FlatFileDatabase;
import us.tastybento.bskyblock.database.managers.AbstractDatabaseHandler;

public class ChallengesManager {

    //private static final boolean DEBUG = false;
    private Challenges addon;
    private LinkedHashMap<LevelsDO, List<ChallengesData>> challengeList;

    private AbstractDatabaseHandler<ChallengesData> chHandler;
    private AbstractDatabaseHandler<LevelsDO> lvHandler;
    
    private ChallengesPanels challengesPanels;

    @SuppressWarnings("unchecked")
    public ChallengesManager(Challenges plugin) {
        this.addon = plugin;
        // Set up the database handler to store and retrieve Challenges
        chHandler = (AbstractDatabaseHandler<ChallengesData>) new FlatFileDatabase().getHandler(ChallengesData.class);
        lvHandler = (AbstractDatabaseHandler<LevelsDO>) new FlatFileDatabase().getHandler(LevelsDO.class);
        challengeList = new LinkedHashMap<>();
        // Start panels
        challengesPanels = new ChallengesPanels(plugin, this);
        load();
    }

    /**
     * @return the challengesPanels
     */
    public ChallengesPanels getChallengesPanels() {
        return challengesPanels;
    }

    public AbstractDatabaseHandler<ChallengesData> getHandler() {
        return chHandler;
    }

    /**
     * Clear and reload all challenges
     */
    public void load() {
        // Load the challenges
        challengeList.clear();
        try {
            for (ChallengesData challenge : chHandler.loadObjects()) {
                // See if we have this level already
                LevelsDO level;
                if (lvHandler.objectExists(challenge.getLevel())) {
                    // Get it from the database
                    level = lvHandler.loadObject(challenge.getLevel());
                } else {
                    // Make it
                    level = new LevelsDO();
                    level.setUniqueId(challenge.getLevel());
                    lvHandler.saveObject(level);
                }
                if (challengeList.containsKey(level)) {
                    challengeList.get(level).add(challenge);                    
                } else {
                    // First challenge of this level type
                    List<ChallengesData> challenges = new ArrayList<>();
                    challenges.add(challenge);
                    challengeList.put(level, challenges);
                }
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | SecurityException | ClassNotFoundException | IntrospectionException
                | SQLException | NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Sort the challenge list into level order
        challengeList = challengeList.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    /**
     * Save to the database
     * @param async - if true, saving will be done async
     */
    public void save(boolean async){
        if(async){
            Runnable save = () -> {
                for (Entry<LevelsDO, List<ChallengesData>> en : challengeList.entrySet()) {
                    try {
                        lvHandler.saveObject(en.getKey());
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                            | SecurityException | InstantiationException | NoSuchMethodException
                            | IntrospectionException | SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    for (ChallengesData challenge : en.getValue()) {
                        try {
                            chHandler.saveObject(challenge);
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                                | SecurityException | InstantiationException | NoSuchMethodException
                                | IntrospectionException | SQLException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            };
            BSkyBlock.getInstance().getServer().getScheduler().runTaskAsynchronously(BSkyBlock.getInstance(), save);
        } else {
            for (Entry<LevelsDO, List<ChallengesData>> en : challengeList.entrySet()) {
                try {
                    lvHandler.saveObject(en.getKey());
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | SecurityException | InstantiationException | NoSuchMethodException | IntrospectionException
                        | SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                for (ChallengesData challenge : en.getValue()) {
                    try {
                        chHandler.saveObject(challenge);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                            | SecurityException | InstantiationException | NoSuchMethodException
                            | IntrospectionException | SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void shutdown(){
        save(false);
        challengeList.clear();
    }

    /**
     * Create a challenge from the inventory contents
     * @param contents
     */
    public void createChallenge(User user, String name) {
        // Get the main icon
        ItemStack icon = user.getInventory().getItemInOffHand();
        if (icon == null || icon.getType().equals(Material.AIR)) {
            user.sendRawMessage("Hold something in your off-hand to make it the icon. Icon will be paper be default.");
            icon = new ItemStack(Material.PAPER);
        }
        icon.setAmount(1);
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        lore.add("Required items:");

        List<ItemStack> inv = Arrays.asList(user.getInventory().getStorageContents());
        List<ItemStack> contents = new ArrayList<>();
        for (ItemStack item : inv) {
            if (item != null && !item.getType().equals(Material.AIR)) {
                contents.add(item);
                lore.add(item.getType() + " x " + item.getAmount());
            }
        }
        if (lore.size() == 1) {
            lore.add("No items");
        }
        meta.setDisplayName(name);
        meta.setLore(lore);
        icon.setItemMeta(meta);
        ChallengesData newChallenge = new ChallengesData();
        newChallenge.setRequiredItems(contents);
        newChallenge.setUniqueId(name);
        newChallenge.setIcon(icon);
        if (chHandler.objectExists(name)) {
            user.sendRawMessage(ChatColor.RED + "Challenge already exists! Use /c replace <name>");
            return;
        }
        try {
            chHandler.saveObject(newChallenge);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException
                | InstantiationException | NoSuchMethodException | IntrospectionException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            user.sendRawMessage(ChatColor.RED + "Challenge creation failed! " + e.getMessage());
            return;
        }
        user.sendRawMessage("Challenge accepted!");
        // TODO ADD CHALLENGE
        //challenges.put(newChallenge.getUniqueId(), newChallenge);
    }

    /**
     * Get the list of challenges for this level
     * @param level - the level required
     * @return the list of challenges for this level, or the first set of challenges if level is blank, or a blank list if there are no challenges
     */
    public List<ChallengesData> getChallenges(String level) {
        return challengeList.getOrDefault(level, challengeList.isEmpty() ? new ArrayList<ChallengesData>() : challengeList.values().iterator().next());
    }

    /**
     * Checks if a challenge is complete or not
     * @param uniqueId - unique ID - player's UUID
     * @param uniqueId2 - Challenge id
     * @return - true if completed
     */
    public boolean isChallengeComplete(UUID uniqueId, String uniqueId2) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isLevelComplete(UUID uniqueId, LevelsDO otherLevel) {
        // TODO Auto-generated method stub
        return false;
    }

    public LevelsDO getPreviousLevel(LevelsDO otherLevel) {
        LevelsDO result = null;

        for (LevelsDO level : challengeList.keySet()) {
            if (level.equals(otherLevel)) {
                return result;
            }
            result = level;
        }
        return result;
    }
    
    /**
     * Get the status on every level
     * @param user
     * @return Level name, how many challenges still to do on which level
     */
    public List<LevelStatus> getChallengeLevelStatus(User user) {
        List<LevelStatus> result = new ArrayList<>();
        LevelsDO previousLevel = null;
        for (Entry<LevelsDO, List<ChallengesData>> en : challengeList.entrySet()) {
            int challsToDo = 0; // TODO - calculate how many challenges still to do for this player
            boolean complete = false; // TODO
            result.add(new LevelStatus(en.getKey(), previousLevel, challsToDo, complete));
        }
        return result;
    }
    
    public class LevelStatus {
        private final LevelsDO level;
        private final LevelsDO previousLevel;
        private final int numberOfChallengesStillToDo;
        private final boolean complete;
        
        public LevelStatus(LevelsDO level, LevelsDO previousLevel, int numberOfChallengesStillToDo, boolean complete) {
            super();
            this.level = level;
            this.previousLevel = previousLevel;
            this.numberOfChallengesStillToDo = numberOfChallengesStillToDo;
            this.complete = complete;
        }
        /**
         * @return the level
         */
        public LevelsDO getLevel() {
            return level;
        }
        /**
         * @return the previousLevel
         */
        public LevelsDO getPreviousLevel() {
            return previousLevel;
        }
        /**
         * @return the numberOfChallengesStillToDo
         */
        public int getNumberOfChallengesStillToDo() {
            return numberOfChallengesStillToDo;
        }
        /**
         * @return the complete
         */
        public boolean isComplete() {
            return complete;
        }

        
    }

    /**
     * Creates an inventory challenge
     * @param user
     * @param inventory
     */
    public void createInvChallenge(User user, Inventory inventory) {
        if (inventory.getContents().length == 0) {
            return;
        }
        ChallengesData newChallenge = new ChallengesData();
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
        try {
            chHandler.saveObject(newChallenge);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException
                | InstantiationException | NoSuchMethodException | IntrospectionException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            user.sendRawMessage(ChatColor.RED + "Challenge creation failed! " + e.getMessage());
            return;
        }
        
        user.sendRawMessage("Success");
    }
    
}
