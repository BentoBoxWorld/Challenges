package world.bentobox.challenges;


import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.commands.admin.SurroundChallengeBuilder;
import world.bentobox.challenges.database.object.ChallengeLevels;
import world.bentobox.challenges.database.object.Challenges;
import world.bentobox.challenges.database.object.Challenges.ChallengeType;
import world.bentobox.challenges.database.object.ChallengesPlayerData;
import world.bentobox.challenges.panel.ChallengesPanels;

public class ChallengesManager {

    public static final String FREE = "Free";
    private Map<ChallengeLevels, Set<Challenges>> challengeMap;
    private Config<Challenges> chConfig;
    private Config<ChallengeLevels> lvConfig;
    private Database<ChallengesPlayerData> players;
    private ChallengesPanels challengesPanels;
    private Map<UUID,ChallengesPlayerData> playerData;
    private ChallengesAddon addon;

    public ChallengesManager(ChallengesAddon addon) {
        this.addon = addon;
        // Set up the configs
        chConfig = new Config<>(addon, Challenges.class);
        lvConfig = new Config<>(addon, ChallengeLevels.class);
        // Players is where all the player history will be stored
        players = new Database<>(addon, ChallengesPlayerData.class);
        // Cache of challenges
        challengeMap = new LinkedHashMap<>();
        // Cache of player data
        playerData = new HashMap<>();
        load();
    }

    /**
     * Load player from database into the cache or create new player data
     * @param user - user to add
     */
    private void addPlayer(User user) {
        if (playerData.containsKey(user.getUniqueId())) {
            return;
        }
        // The player is not in the cache
        // Check if the player exists in the database
        if (players.objectExists(user.getUniqueId().toString())) {
            // Load player from database
            ChallengesPlayerData data = players.loadObject(user.getUniqueId().toString());
            // Store in cache
            playerData.put(user.getUniqueId(), data);
        } else {
            // Create the player data
            ChallengesPlayerData pd = new ChallengesPlayerData(user.getUniqueId().toString());
            players.saveObject(pd);
            // Add to cache
            playerData.put(user.getUniqueId(), pd);
        }
    }

    /**
     * Check how many times a player has done a challenge before
     * @param user - user
     * @param challenge - challenge
     * @return - number of times
     */
    public long checkChallengeTimes(User user, Challenges challenge, World world) {
        addPlayer(user);
        return playerData.get(user.getUniqueId()).getTimes(world, challenge.getUniqueId());
    }

    /**
     * Creates a simple example description of the requirements
     * @param user - user of this command
     * @param requiredItems - list of items
     * @return Description list
     */
    private List<String> createDescription(User user, List<ItemStack> requiredItems) {
        addPlayer(user);
        List<String> result = new ArrayList<>();
        result.add(user.getTranslation("challenges.admin.create.description"));
        for (ItemStack item : requiredItems) {
            result.add(user.getTranslation("challenges.admin.create.description-item-color") + item.getAmount() + " x " + Util.prettifyText(item.getType().toString()));
        }
        return result;
    }

    /**
     * Creates an inventory challenge
     * @param user - the user who is making the challenge
     * @param inventory - the inventory that will be used to make the challenge
     */
    public boolean createInvChallenge(User user, Inventory inventory) {
        addPlayer(user);
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
        newChallenge.setIcon(new ItemStack(Material.MAP));
        newChallenge.setLevel(FREE);
        newChallenge.setDescription(createDescription(user, requiredItems));

        // Move all the items back to the player's inventory
        inventory.forEach(item -> {
            if (item != null) {
                Map<Integer, ItemStack> residual = user.getInventory().addItem(item);
                // Drop any residual items at the foot of the player
                residual.forEach((k, v) -> user.getWorld().dropItem(user.getLocation(), v));
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
        newChallenge.setChallengeType(ChallengeType.ISLAND);
        newChallenge.setFriendlyName(challengeInfo.getName());
        newChallenge.setDeployed(true);
        newChallenge.setRequiredBlocks(challengeInfo.getReqBlocks());
        newChallenge.setRequiredEntities(challengeInfo.getReqEntities());
        newChallenge.setUniqueId(challengeInfo.getName());
        newChallenge.setIcon(new ItemStack(Material.ARMOR_STAND));
        newChallenge.setLevel(FREE);

        // Save the challenge
        if (!chConfig.saveConfigObject(newChallenge)) {
            challengeInfo.getOwner().sendMessage("challenges.error.could-not-save");
            return false;
        }
        return true;
    }

    /**
     * Get the list of all challenge unique names.
     * @return List of challenge names
     */
    public List<String> getAllChallengesList() {
        List<String> result = new ArrayList<>();
        challengeMap.values().forEach(ch -> ch.forEach(c -> result.add(c.getUniqueId())));
        return result;
    }

    /**
     * Get the list of all challenge unique names for world.
     * @param world - the world to check
     * @return List of challenge names
     */
    public List<String> getAllChallengesList(World world) {
        List<String> result = new ArrayList<>();
        challengeMap.values().forEach(ch -> ch.stream().filter(c -> c.getWorld().equals(Util.getWorld(world).getName())).forEach(c -> result.add(c.getUniqueId())));
        return result;
    }

    /**
     * Get challenge by name
     * @param name - unique name of challenge
     * @param world - world to check
     * @return - challenge or null if it does not exist
     */
    public Challenges getChallenge(String name, World world) {
        String worldName = Util.getWorld(world).getName();
        for (Set<Challenges> ch : challengeMap.values())  {
            Optional<Challenges> challenge = ch.stream().filter(c -> c.getUniqueId().equalsIgnoreCase(worldName + name)).findFirst();
            if (challenge.isPresent()) {
                return challenge.get();
            }
        }
        return null;
    }

    /**
     * Get the status on every level
     * @param user - user
     * @param world - world to check
     * @return Level status - how many challenges still to do on which level
     */
    public List<LevelStatus> getChallengeLevelStatus(User user, World world) {
        addPlayer(user);
        ChallengesPlayerData pd = playerData.get(user.getUniqueId());
        List<LevelStatus> result = new ArrayList<>();
        ChallengeLevels previousLevel = null;
        // The first level is always unlocked
        boolean isUnlocked = true;
        // For each challenge level, check how many the user has done
        for (Entry<ChallengeLevels, Set<Challenges>> en : challengeMap.entrySet()) {
            int total = challengeMap.values().size();
            int waiverAmount = en.getKey().getWaiveramount();
            int challengesDone = (int) en.getValue().stream().filter(ch -> pd.isChallengeDone(world, ch.getUniqueId())).count();
            int challsToDo =  Math.max(0,total - challengesDone - waiverAmount);
            boolean complete = challsToDo > 0 ? false : true;
            // Create result class with the data
            result.add(new LevelStatus(en.getKey(), previousLevel, challsToDo, complete, isUnlocked));
            // Set up the next level for the next loop
            previousLevel = en.getKey();
            isUnlocked = complete;
        }
        return result;
    }

    /**
     * Get the challenge list
     * @return the challengeList
     */
    public Map<ChallengeLevels, Set<Challenges>> getChallengeList() {
        // TODO return the challenges for world
        return challengeMap;
    }

    /**
     * Get the set of challenges for this level for this world
     * @param level - the level required
     * @param world
     * @return the set of challenges for this level, or the first set of challenges if level is blank, or a blank list if there are no challenges
     */
    public Set<Challenges> getChallenges(String level, World world) {
        String worldName = Util.getWorld(world).getName();
        Optional<ChallengeLevels> lv = challengeMap.keySet().stream().filter(l -> l.getUniqueId().equalsIgnoreCase(level)).findFirst();
        // Get the challenges applicable to this world
        return lv.isPresent() ? challengeMap.get(lv.get()).stream()
                .filter(c -> c.getWorld().equalsIgnoreCase(worldName) || c.getWorld().isEmpty()).collect(Collectors.toSet())
                : new HashSet<>();
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
        for (ChallengeLevels level : challengeMap.keySet()) {
            if (level.equals(currentLevel)) {
                return result;
            }
            result = level;
        }
        return result;
    }

    /**
     * Check if a challenge exists - case insensitive
     * @param name - name of challenge
     * @return true if it exists, otherwise false
     */
    public boolean isChallenge(String name) {
        for (Set<Challenges> ch : challengeMap.values())  {
            if (ch.stream().anyMatch(c -> c.getUniqueId().equalsIgnoreCase(name))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a challenge exists in world - case insensitive
     * @param world - world to check
     * @param name - name of challenge
     * @return true if it exists, otherwise false
     */
    public boolean isChallenge(World world, String name) {
        for (Set<Challenges> ch : challengeMap.values())  {
            if (ch.stream().filter(c -> c.getWorld().equals(Util.getWorld(world).getName())).anyMatch(c -> c.getUniqueId().equalsIgnoreCase(name))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a challenge is complete or not
     * @param uniqueId - unique ID - player's UUID
     * @param challengeName - Challenge uniqueId
     * @return - true if completed
     */
    public boolean isChallengeComplete(User user, String challengeName, World world) {
        addPlayer(user);
        return playerData.get(user.getUniqueId()).isChallengeDone(world, challengeName);
    }

    /**
     * Check is user can see level
     * @param user - user
     * @param level - level unique id
     * @return true if level is unlocked
     */
    public boolean isLevelUnlocked(User user, String level, World world) {
        addPlayer(user);
        return getChallengeLevelStatus(user, world).stream().filter(LevelStatus::isUnlocked).anyMatch(lv -> lv.getLevel().getUniqueId().equalsIgnoreCase(level));
    }

    /**
     * Clear and reload all challenges
     */
    public void load() {
        // Load the challenges
        challengeMap.clear();
        addon.getLogger().info("Loading challenges...");
        chConfig.loadConfigObjects().forEach(this::storeChallenge);
        sortChallenges();
        players.loadObjects().forEach(pd -> {
            try {
                UUID uuid = UUID.fromString(pd.getUniqueId());
                playerData.put(uuid,pd);
            } catch (Exception e) {
                addon.getLogger().severe("UUID for player in challenge data file is invalid!");
            }
        });
    }

    /**
     * Save configs and player data
     */
    public void save() {
        challengeMap.entrySet().forEach(en -> {
            lvConfig.saveConfigObject(en.getKey());
            en.getValue().forEach(chConfig::saveConfigObject);
        });
        savePlayers();
    }

    private void savePlayers() {
        playerData.values().forEach(players :: saveObject);
    }

    private void savePlayer(UUID playerUUID) {
        if (playerData.containsKey(playerUUID)) {
            players.saveObject(playerData.get(playerUUID));
        }
    }

    /**
     * Sets the challenge as complete and increments the number of times it has been completed
     * @param user - user
     * @param challengeUniqueId - unique challenge id
     * @param world - world to set
     */
    public void setChallengeComplete(User user, String challengeUniqueId, World world) {
        addPlayer(user);
        playerData.get(user.getUniqueId()).setChallengeDone(world, challengeUniqueId);
        // Save
        savePlayer(user.getUniqueId());
    }

    /**
     * Reset the challenge to zero time / not done
     * @param user - user
     * @param challengeUniqueId - unique challenge id
     * @param world - world to set
     */
    public void setResetChallenge(User user, String challengeUniqueId, World world) {
        addPlayer(user);
        playerData.get(user.getUniqueId()).setChallengeTimes(world, challengeUniqueId, 0);
        // Save
        savePlayer(user.getUniqueId());
    }

    /**
     * @param challengeList the challengeList to set
     */
    public void setChallengeList(Map<ChallengeLevels, Set<Challenges>> challengeList) {
        this.challengeMap = challengeList;
    }

    public void sortChallenges() {
        // Sort the challenge list into level order
        challengeMap = challengeMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }


    /**
     * Store challenge silently. Used when loading.
     * @param challenge
     * @return true if successful
     */
    private boolean storeChallenge(Challenges challenge) {
        return storeChallenge(challenge, true, null, true);
    }

    /**
     * Stores the challenge.
     * @param challenge - challenge
     * @param overwrite - true if previous challenge should be overwritten
     * @param user - user making the request
     * @param silent - if true, no messages are sent to user
     * @return - true if imported
     */
    public boolean storeChallenge(Challenges challenge, boolean overwrite, User user, boolean silent) {
        // See if we have this level already
        ChallengeLevels level;
        if (lvConfig.configObjectExists(challenge.getLevel())) {
            // Get it from the database
            level = lvConfig.loadConfigObject(challenge.getLevel());
        } else {
            // Make it
            level = new ChallengeLevels();
            level.setUniqueId(challenge.getLevel());
            lvConfig.saveConfigObject(level);
        }
        challengeMap.putIfAbsent(level, new HashSet<>());
        if (challengeMap.get(level).contains(challenge)) {
            if (!overwrite) {
                if (!silent) {
                    user.sendMessage("challenges.admin.import.skipping", "[challenge]", challenge.getFriendlyName());
                }
                return false;
            } else {
                if (!silent) {
                    user.sendMessage("challenges.admin.import.overwriting", "[challenge]", challenge.getFriendlyName());
                }
                challengeMap.get(level).add(challenge);
                return true;
            }
        }
        if (!silent) {
            user.sendMessage("challenges.admin.import.imported", "[challenge]", challenge.getFriendlyName());
        }
        challengeMap.get(level).add(challenge);
        return true;
    }

    /**
     * Store a challenge level
     * @param level the challenge level
     */
    public void storeLevel(ChallengeLevels level) {
        lvConfig.saveConfigObject(level);
    }

    /**
     * Simple splitter
     * @param string - string to be split
     * @return list of split strings
     */
    public List<String> stringSplit(String string) {
        string = ChatColor.translateAlternateColorCodes('&', string);
        // Check length of lines
        List<String> result = new ArrayList<>();
        Arrays.asList(string.split("\\|")).forEach(line -> result.addAll(Arrays.asList(WordUtils.wrap(line,25).split("\\n"))));
        return result;
    }

    /**
     * Resets all the challenges for user in world
     * @param uuid - island owner's UUID
     * @param world - world
     */
    public void resetAllChallenges(UUID uuid, World world) {
        User user = User.getInstance(uuid);
        addPlayer(user);
        playerData.get(user.getUniqueId()).reset(world);
        // Save
        savePlayer(user.getUniqueId());

    }


    public Challenges createChallenge()
    {
        return new Challenges();
    }


    public List<Challenges> getChallenges(ChallengeLevels challengeLevel)
    {
        return new ArrayList<>(this.challengeMap.get(challengeLevel));
    }
}
