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
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import bskyblock.addon.challenges.database.object.ChallengeLevels;
import bskyblock.addon.challenges.database.object.Challenges;
import bskyblock.addon.challenges.database.object.Challenges.ChallengeType;
import bskyblock.addon.challenges.panel.ChallengesPanels;
import us.tastybento.bskyblock.BSkyBlock;
import us.tastybento.bskyblock.api.commands.User;
import us.tastybento.bskyblock.database.flatfile.FlatFileDatabase;
import us.tastybento.bskyblock.database.managers.AbstractDatabaseHandler;

public class ChallengesManager {

    //private static final boolean DEBUG = false;
    public static final String FREE = "Free";

    private ChallengesAddon addon;
    private LinkedHashMap<ChallengeLevels, List<Challenges>> challengeList;

    private AbstractDatabaseHandler<Challenges> chHandler;
    private AbstractDatabaseHandler<ChallengeLevels> lvHandler;
    
    
    private ChallengesPanels challengesPanels;

    @SuppressWarnings("unchecked")
    public ChallengesManager(ChallengesAddon plugin) {
        this.addon = plugin;
        // Set up the database handler to store and retrieve Challenges
        chHandler = (AbstractDatabaseHandler<Challenges>) new FlatFileDatabase().getHandler(Challenges.class);
        lvHandler = (AbstractDatabaseHandler<ChallengeLevels>) new FlatFileDatabase().getHandler(ChallengeLevels.class);
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

    public AbstractDatabaseHandler<Challenges> getHandler() {
        return chHandler;
    }

    /**
     * Clear and reload all challenges
     */
    public void load() {
        // Load the challenges
        challengeList.clear();
        try {
            for (Challenges challenge : chHandler.loadObjects()) {
                Bukkit.getLogger().info("DEBUG: Loading challenge " + challenge.getFriendlyName() + " level " + challenge.getLevel());
                // See if we have this level already
                ChallengeLevels level;
                if (lvHandler.objectExists(challenge.getLevel())) {
                    Bukkit.getLogger().info("DEBUG: Level contains level " + challenge.getLevel());
                    // Get it from the database
                    level = lvHandler.loadObject(challenge.getLevel());
                } else {
                    Bukkit.getLogger().info("DEBUG: Level does not contains level " + challenge.getLevel());
                    // Make it
                    level = new ChallengeLevels();
                    level.setUniqueId(challenge.getLevel());
                    Bukkit.getLogger().info("DEBUG: Level unique Id set to " + level.getUniqueId());
                    lvHandler.saveObject(level);
                }
                if (challengeList.containsKey(level)) {
                    Bukkit.getLogger().info("DEBUG: Challenge contains level " + level.getUniqueId());
                    challengeList.get(level).add(challenge);                    
                } else {
                    Bukkit.getLogger().info("DEBUG: No key found");
                    // First challenge of this level type
                    List<Challenges> challenges = new ArrayList<>();
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
        Bukkit.getLogger().info("DEBUG: " + challengeList.size());
        // Sort the challenge list into level order
        challengeList = challengeList.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        Bukkit.getLogger().info("DEBUG: " + challengeList.size());
    }

    /**
     * Save to the database
     * @param async - if true, saving will be done async
     */
    public void save(boolean async){
        if(async){
            Runnable save = () -> {
                for (Entry<ChallengeLevels, List<Challenges>> en : challengeList.entrySet()) {
                    try {
                        lvHandler.saveObject(en.getKey());
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                            | SecurityException | InstantiationException | NoSuchMethodException
                            | IntrospectionException | SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    for (Challenges challenge : en.getValue()) {
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
            for (Entry<ChallengeLevels, List<Challenges>> en : challengeList.entrySet()) {
                try {
                    lvHandler.saveObject(en.getKey());
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | SecurityException | InstantiationException | NoSuchMethodException | IntrospectionException
                        | SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                for (Challenges challenge : en.getValue()) {
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
        Challenges newChallenge = new Challenges();
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
    public List<Challenges> getChallenges(String level) {
        return challengeList.getOrDefault(level, challengeList.isEmpty() ? new ArrayList<Challenges>() : challengeList.values().iterator().next());
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

    public boolean isLevelComplete(User user, ChallengeLevels otherLevel) {
        // TODO Auto-generated method stub
        return false;
    }

    public ChallengeLevels getPreviousLevel(ChallengeLevels otherLevel) {
        ChallengeLevels result = null;

        for (ChallengeLevels level : challengeList.keySet()) {
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
        ChallengeLevels previousLevel = null;
        for (Entry<ChallengeLevels, List<Challenges>> en : challengeList.entrySet()) {
            int challsToDo = 0; // TODO - calculate how many challenges still to do for this player
            boolean complete = false; // TODO
            result.add(new LevelStatus(en.getKey(), previousLevel, challsToDo, complete));
        }
        return result;
    }
    
    public class LevelStatus {
        private final ChallengeLevels level;
        private final ChallengeLevels previousLevel;
        private final int numberOfChallengesStillToDo;
        private final boolean complete;
        
        public LevelStatus(ChallengeLevels level, ChallengeLevels previousLevel, int numberOfChallengesStillToDo, boolean complete) {
            super();
            this.level = level;
            this.previousLevel = previousLevel;
            this.numberOfChallengesStillToDo = numberOfChallengesStillToDo;
            this.complete = complete;
        }
        /**
         * @return the level
         */
        public ChallengeLevels getLevel() {
            return level;
        }
        /**
         * @return the previousLevel
         */
        public ChallengeLevels getPreviousLevel() {
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

    public boolean isLevelAvailable(User user, String level) {
        // TODO
        return false;
    }

    public long checkChallengeTimes(User user, Challenges challenge) {
        // TODO Auto-generated method stub
        return 0;
    }

    public ChallengesAddon getAddon() {
        return addon;      
    }

    /**
     * Sets the challenge as complete and increments the number of times it has been completed
     * @param user
     * @param uniqueId
     */
    public void setChallengeComplete(User user, String uniqueId) {
        // TODO Auto-generated method stub
        
    }

    public void createSurroundingChallenge(String string, Map<Material, Integer> map) {
        if (map.isEmpty()) {
            return;
        }
        Challenges newChallenge = new Challenges();
        newChallenge.setChallengeType(ChallengeType.SURROUNDING);
        newChallenge.setFriendlyName(string);
        newChallenge.setDeployed(true);
        newChallenge.setRequiredBlocks(map);
        newChallenge.setUniqueId(string);
        newChallenge.setIcon(new ItemStack(Material.ARMOR_STAND));
        newChallenge.setFreeChallenge(true);
        newChallenge.setLevel(FREE);

        // Save the challenge
        try {
            chHandler.saveObject(newChallenge);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException
                | InstantiationException | NoSuchMethodException | IntrospectionException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
    }
    
}
