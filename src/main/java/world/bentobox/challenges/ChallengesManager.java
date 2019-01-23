package world.bentobox.challenges;


import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
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
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.Challenge.ChallengeType;
import world.bentobox.challenges.database.object.ChallengesPlayerData;
import world.bentobox.challenges.panel.ChallengesPanels;

public class ChallengesManager {

    public static final String FREE = "Free";
    private Map<ChallengeLevel, Set<Challenge>> challengeMap;
    private Config<Challenge> chConfig;
    private Config<ChallengeLevel> lvConfig;
    private Database<ChallengesPlayerData> players;
    private ChallengesPanels challengesPanels;
    private Map<UUID,ChallengesPlayerData> playerData;
    private ChallengesAddon addon;

    public ChallengesManager(ChallengesAddon addon) {
        this.addon = addon;
        // Set up the configs
        chConfig = new Config<>(addon, Challenge.class);
        lvConfig = new Config<>(addon, ChallengeLevel.class);
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
    public long checkChallengeTimes(User user, Challenge challenge, World world) {
        addPlayer(user);
        return playerData.get(user.getUniqueId()).getTimes(challenge.getUniqueId());
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
        Challenge newChallenge = new Challenge();
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
        Challenge newChallenge = new Challenge();
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
        challengeMap.values().forEach(ch -> ch.stream().filter(c -> c.getUniqueId().startsWith(Util.getWorld(world).getName())).forEach(c -> result.add(c.getUniqueId())));
        return result;
    }

    /**
     * Get challenge by name
     * @param name - unique name of challenge
     * @param world - world to check
     * @return - challenge or null if it does not exist
     */
    public Challenge getChallenge(String name, World world) {
        String worldName = Util.getWorld(world).getName();
        for (Set<Challenge> ch : challengeMap.values())  {
            Optional<Challenge> challenge = ch.stream().filter(c -> c.getUniqueId().equalsIgnoreCase(worldName + name)).findFirst();
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
    public List<LevelStatus> getChallengeLevelStatus(User user, World world)
    {
        this.addPlayer(user);
        ChallengesPlayerData playerData = this.playerData.get(user.getUniqueId());
        List<LevelStatus> result = new ArrayList<>();

        // The first level is always unlocked
        ChallengeLevel previousLevel = null;
        int doneChallengeCount = Integer.MAX_VALUE;

        // For each challenge level, check how many the user has done
        for (Entry<ChallengeLevel, Set<Challenge>> entry : this.challengeMap.entrySet())
        {
            // Check how much challenges must be done in previous level.
            int challengesToDo = Math.max(0, entry.getKey().getWaiverAmount() - doneChallengeCount);

            doneChallengeCount = (int) entry.getValue().stream().filter(
                ch -> playerData.isChallengeDone(ch.getUniqueId())).count();

            // Create result class with the data
            result.add(new LevelStatus(
                entry.getKey(),
                previousLevel,
                challengesToDo,
                entry.getValue().size() == doneChallengeCount,
                challengesToDo <= 0));

            // Set up the next level for the next loop
            previousLevel = entry.getKey();
        }

        return result;
    }

    /**
     * Get the challenge list
     * @return the challengeList
     */
    public Map<ChallengeLevel, Set<Challenge>> getChallengeList() {
        // TODO return the challenges for world
        return challengeMap;
    }

    /**
     * Get the set of challenges for this level for this world
     * @param level - the level required
     * @param world
     * @return the set of challenges for this level, or the first set of challenges if level is blank, or a blank list if there are no challenges
     */
    public Set<Challenge> getChallenges(String level, World world) {
        String worldName = Util.getWorld(world).getName();
        Optional<ChallengeLevel> lv = challengeMap.keySet().stream().filter(l -> l.getUniqueId().equalsIgnoreCase(level)).findFirst();
        // Get the challenges applicable to this world
        return lv.isPresent() ? challengeMap.get(lv.get()).stream()
                .filter(c -> c.getUniqueId().startsWith(worldName)).collect(Collectors.toSet())
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
    public ChallengeLevel getPreviousLevel(ChallengeLevel currentLevel) {
        ChallengeLevel result = null;
        for (ChallengeLevel level : challengeMap.keySet()) {
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
        for (Set<Challenge> ch : challengeMap.values())  {
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
        for (Set<Challenge> ch : challengeMap.values())  {
            if (ch.stream().filter(c -> c.getUniqueId().startsWith(Util.getWorld(world).getName())).anyMatch(c -> c.getUniqueId().equalsIgnoreCase(name))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a challenge is complete or not
     * @param challengeName - Challenge uniqueId
     * @return - true if completed
     */
    public boolean isChallengeComplete(User user, String challengeName, World world) {
        addPlayer(user);
        return playerData.get(user.getUniqueId()).isChallengeDone(challengeName);
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
        playerData.get(user.getUniqueId()).setChallengeDone(challengeUniqueId);
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
        playerData.get(user.getUniqueId()).setChallengeTimes(challengeUniqueId, 0);
        // Save
        savePlayer(user.getUniqueId());
    }

    /**
     * @param challengeList the challengeList to set
     */
    public void setChallengeList(Map<ChallengeLevel, Set<Challenge>> challengeList) {
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
    private boolean storeChallenge(Challenge challenge) {
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
    public boolean storeChallenge(Challenge challenge, boolean overwrite, User user, boolean silent) {
        // See if we have this level already
        ChallengeLevel level;
        if (lvConfig.configObjectExists(challenge.getLevel())) {
            // Get it from the database
            level = lvConfig.loadConfigObject(challenge.getLevel());
        } else {
            // Make it
            level = new ChallengeLevel();
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
    public void storeLevel(ChallengeLevel level) {
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


    public Challenge createChallenge()
    {
        return new Challenge();
    }


    public List<Challenge> getChallenges(ChallengeLevel challengeLevel)
    {
        return new ArrayList<>(this.challengeMap.get(challengeLevel));
    }


    public List<ChallengeLevel> getChallengeLevelList()
    {
        return new ArrayList<>(this.challengeMap.keySet());
    }


    public List<Challenge> getChallengesList()
    {
        return new ArrayList<>();
    }


    public void deleteChallenge(Challenge selectedChallenge)
    {

    }


    public void deleteChallengeLevel(ChallengeLevel valueObject)
    {

    }

    public void resetAllChallenges(User uuid, World world)
    {

    }


	public Challenge createChallenge(String reply)
	{
		return new Challenge();
	}


	public boolean validateChallengeUniqueID(World world, String reply)
	{
		return true;
	}


	public boolean validateLevelUniqueID(World world, String reply)
	{
		return false;
	}


	public ChallengeLevel createLevel(String reply)
	{
		return new ChallengeLevel();
	}


	public void unlinkChallenge(ChallengeLevel challengeLevel, Challenge value)
	{

	}


	public void linkChallenge(ChallengeLevel challengeLevel, Challenge value)
	{

	}


    public void resetChallenge(UUID uniqueId, Challenge value)
    {

    }


    public void completeChallenge(UUID uniqueId, Challenge value)
    {

    }


    public List<Challenge> getFreeChallenges(User user, World world)
    {
        return Collections.emptyList();
    }


    public String getChallengesLevel(Challenge challenge)
    {
        return "HERE NEED LEVEL NAME";
    }


    public boolean isChallengeComplete(User user, Challenge challenge)
    {
        return this.isChallengeComplete(user, challenge.getUniqueId(), user.getWorld());
    }


    public long checkChallengeTimes(User user, Challenge challenge)
    {
       return this.checkChallengeTimes(user, challenge, user.getWorld());
    }


    public List<Player> getPlayers(World world)
    {
        List<Player> playerList = new ArrayList<>();


        return playerList;
    }



	public ChallengeLevel getLevel(String level)
	{
		return null;
	}


	public void addChallengeToLevel(Challenge newChallenge, ChallengeLevel level)
	{

	}
}
