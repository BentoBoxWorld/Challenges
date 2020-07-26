package world.bentobox.challenges;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.api.logs.LogEntry;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.config.Settings;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.database.object.ChallengesPlayerData;
import world.bentobox.challenges.database.object.requirements.InventoryRequirements;
import world.bentobox.challenges.database.object.requirements.IslandRequirements;
import world.bentobox.challenges.database.object.requirements.OtherRequirements;
import world.bentobox.challenges.database.object.requirements.Requirements;
import world.bentobox.challenges.events.ChallengeCompletedEvent;
import world.bentobox.challenges.events.ChallengeResetAllEvent;
import world.bentobox.challenges.events.ChallengeResetEvent;
import world.bentobox.challenges.events.LevelCompletedEvent;
import world.bentobox.challenges.utils.LevelStatus;
import world.bentobox.challenges.utils.Utils;


/**
 * This class manages challenges. It allows access to all data that is stored to database.
 * It also provides information about challenge level status for each user.
 */
public class ChallengesManager
{
    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * This config object stores structures for challenge objects.
     */
    private Database<Challenge> challengeDatabase;

    /**
     * This config object stores structures for challenge level objects.
     */
    private Database<ChallengeLevel> levelDatabase;

    /**
     * This database allows to access player challenge data.
     */
    private Database<ChallengesPlayerData> playersDatabase;

    /**
     * This is local cache that links challenge unique id with challenge object.
     */
    private Map<String, Challenge> challengeCacheData;

    /**
     * This is local cache that links level unique id with level object.
     */
    private Map<String, ChallengeLevel> levelCacheData;

    /**
     * This is local cache that links UUID with corresponding player challenge data.
     */
    private Map<String, ChallengesPlayerData> playerCacheData;

    /**
     * This variable allows to access ChallengesAddon.
     */
    private ChallengesAddon addon;

    /**
     * This variable allows to access ChallengesAddon settings.
     */
    private Settings settings;

    /**
     * Island world manager allows to detect which world refferes to which gamemode addon.
     */
    private IslandWorldManager islandWorldManager;


    // ---------------------------------------------------------------------
    // Section: Constants
    // ---------------------------------------------------------------------


    /**
     * String for free Challenge Level.
     */
    public static final String FREE = "";
    public static final String VALUE = "[value]";
    public static final String USER_ID = "user-id";
    public static final String CHALLENGE_ID = "challenge-id";
    public static final String ADMIN_ID = "admin-id";
    public static final String RESET = "RESET";


    // ---------------------------------------------------------------------
    // Section: Comparators
    // ---------------------------------------------------------------------


    /**
     * This comparator orders challenges by their level, order and name.
     */
    private final Comparator<Challenge> challengeComparator = (o1, o2) -> {
        if (o1.getLevel().equals(o2.getLevel()))
        {
            if (o1.getOrder() == o2.getOrder())
            {
                // If orders are equal, sort by unique Id
                return o1.getUniqueId().compareToIgnoreCase(o2.getUniqueId());
            }
            else
            {
                // If levels are equal, sort them by order numbers.
                return Integer.compare(o1.getOrder(), o2.getOrder());
            }
        }
        else
        {
            if (o1.getLevel().isEmpty() || o2.getLevel().isEmpty())
            {
                // If exist free level challenge, then it should be at the start.
                return Boolean.compare(o2.getLevel().isEmpty(), o1.getLevel().isEmpty());
            }
            else
            {
                // Sort by challenges level order numbers
                return Integer.compare(this.getLevel(o1.getLevel()).getOrder(),
                        this.getLevel(o2.getLevel()).getOrder());
            }
        }
    };


    // ---------------------------------------------------------------------
    // Section: Constructor
    // ---------------------------------------------------------------------


    /**
     * Initial constructor. Inits and loads all data.
     * @param addon challenges addon.
     */
    public ChallengesManager(ChallengesAddon addon)
    {
        this.addon = addon;
        this.islandWorldManager = addon.getPlugin().getIWM();

        this.settings = addon.getChallengesSettings();

        // Set up the configs
        this.challengeDatabase = new Database<>(addon, Challenge.class);
        this.levelDatabase = new Database<>(addon, ChallengeLevel.class);
        // Players is where all the player history will be stored
        this.playersDatabase = new Database<>(addon, ChallengesPlayerData.class);

        // Init all cache objects.
        this.challengeCacheData = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.levelCacheData = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.playerCacheData = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        this.load();
    }


    // ---------------------------------------------------------------------
    // Section: Loading and storing methods
    // ---------------------------------------------------------------------


    /**
     * Clear and reload all challenges
     */
    public void load()
    {
        this.addon.log("Loading challenges...");
        this.challengeCacheData.clear();
        this.levelCacheData.clear();

        if (!this.playerCacheData.isEmpty())
        {
            // store player data before cleaning.
            this.savePlayersData();
        }

        this.playerCacheData.clear();
        this.loadAndValidate();
    }


    /**
     * This method loads and validates all challenges and levels.
     */
    private void loadAndValidate()
    {
        this.challengeDatabase.loadObjects().forEach(this::loadChallenge);
        this.levelDatabase.loadObjects().forEach(this::loadLevel);
        // this validate challenge levels
        this.validateChallenges();
    }


    /**
     * Reload database. This method keeps cache memory.
     */
    public void reload()
    {
        this.addon.log("Reloading challenges...");
        if (!this.playerCacheData.isEmpty())
        {
            // store player data before cleaning.
            this.savePlayersData();
        }
        //this.challengeDatabase = new Database<>(addon, Challenge.class);
        //this.levelDatabase = new Database<>(addon, ChallengeLevel.class);
        //this.playersDatabase = new Database<>(addon, ChallengesPlayerData.class);

        this.loadAndValidate();
    }


    /**
     * Load challenge silently. Used when loading.
     *
     * @param challenge Challenge that must be loaded.
     * @return true if successful
     */
    private void loadChallenge(@NonNull Challenge challenge)
    {
        this.loadChallenge(challenge, true, null, true);
    }


    /**
     * Load the challenge.
     *
     * @param challenge - challenge
     * @param overwrite - true if previous challenge should be overwritten
     * @param user - user making the request
     * @param silent - if true, no messages are sent to user
     * @return - true if imported
     */
    public boolean loadChallenge(@NonNull Challenge challenge,
            boolean overwrite,
            User user,
            boolean silent)
    {
        // This may happen if database somehow failed to load challenge and return
        // null as input.
        if (challenge == null)
        {
            if (!silent)
            {
                user.sendMessage("load-error", "[value]", "NULL");
            }

            return false;
        }

        if (!challenge.isValid())
        {
            if (!silent)
            {
                user.sendMessage("challenges.errors.invalid-challenge", "[challenge]", challenge.getUniqueId());
            }

            this.addon.logWarning("Data for challenge `" + challenge.getUniqueId() + "` is not valid. It could be NULL element in item-stack!");
            return false;
        }

        if (this.challengeCacheData.containsKey(challenge.getUniqueId()))
        {
            if (!overwrite)
            {
                if (!silent)
                {
                    user.sendMessage("challenges.messages.load-skipping",
                            VALUE, challenge.getFriendlyName());
                }

                return false;
            }
            else
            {
                if (!silent)
                {
                    user.sendMessage("challenges.messages.load-overwriting",
                            VALUE, challenge.getFriendlyName());
                }
            }
        }
        else
        {
            if (!silent)
            {
                user.sendMessage("challenges.messages.load-add",
                        VALUE, challenge.getFriendlyName());
            }
        }

        this.challengeCacheData.put(challenge.getUniqueId(), challenge);
        return true;
    }


    /**
     * Store a challenge level
     *
     * @param level the challenge level
     */
    private void loadLevel(@NonNull ChallengeLevel level)
    {
        this.loadLevel(level, true, null, true);
    }


    /**
     * This method loads given level into local cache. It provides functionality to overwrite local
     * value with new one, and send message to given user.
     *
     * @param level of type ChallengeLevel that must be loaded in local cache.
     * @param overwrite of type boolean that indicate if local element must be overwritten.
     * @param user of type User who will receive messages.
     * @param silent of type boolean that indicate if message to user must be sent.
     * @return boolean that indicate about load status.
     */
    public boolean loadLevel(@NonNull ChallengeLevel level,
            boolean overwrite,
            User user,
            boolean silent)
    {
        // This may happen if database somehow failed to load challenge and return
        // null as input.
        if (level == null)
        {
            if (!silent)
            {
                user.sendMessage("load-error", "[value]", "NULL");
            }

            return false;
        }

        if (!level.isValid())
        {
            if (!silent)
            {
                user.sendMessage("challenges.errors.invalid-level", "[level]", level.getUniqueId());
            }

            this.addon.logWarning("Data for level `" + level.getUniqueId() + "` is not valid. It could be NULL element in item-stack!");
            return false;
        }

        if (!this.isValidLevel(level))
        {
            if (user != null)
            {
                user.sendMessage("challenges.errors.load-error",
                        VALUE, level.getFriendlyName());
            }
            else
            {
                this.addon.logError(
                        "Challenge Level '" + level.getUniqueId() + "' is not valid and skipped");
            }

            return false;
        }

        if (this.levelCacheData.containsKey(level.getUniqueId()))
        {
            if (!overwrite)
            {
                if (!silent)
                {
                    user.sendMessage("challenges.messages.load-skipping",
                            VALUE, level.getFriendlyName());
                }

                return false;
            }
            else
            {
                if (!silent)
                {
                    user.sendMessage("challenges.messages.load-overwriting",
                            VALUE, level.getFriendlyName());
                }
            }
        }
        else
        {
            if (!silent)
            {
                user.sendMessage("challenges.messages.load-add",
                        VALUE, level.getFriendlyName());
            }
        }

        this.levelCacheData.put(level.getUniqueId(), level);
        return true;
    }


    /**
     * This method stores PlayerData into local cache.
     *
     * @param playerData ChallengesPlayerData that must be loaded.
     *
     * TODO: Remove this unused method?
     */
    private void loadPlayerData(@NonNull ChallengesPlayerData playerData)
    {
        try
        {
            this.playerCacheData.put(playerData.getUniqueId(), playerData);
        }
        catch (Exception e)
        {
            this.addon.getLogger().severe("UUID for player in challenge data file is invalid!");
        }
    }


    /**
     * This method removes given player from cache data.
     *
     * @param playerID player ID which cache data must be removed.
     */
    public void removeFromCache(UUID playerID)
    {
// Remove due possible issues with saving... (#246)
//        if (!this.settings.isStoreAsIslandData() && this.playerCacheData.containsKey(playerID.toString()))
//        {
//            // save before remove
//            this.savePlayerData(playerID.toString());
//            this.playerCacheData.remove(playerID.toString());
//        }

        this.savePlayerData(playerID.toString());

        // TODO: It would be necessary to remove also data, if they stores islands.
        // Unfortunately, I do not know all worlds. Checking everything would be bad. Probably, I could
        // add extra map that links players with their cached island data?
    }


    // ---------------------------------------------------------------------
    // Section: Other storing related methods
    // ---------------------------------------------------------------------


    /**
     * This method iterates through all challenges that is loaded from database and removes levels
     * that are not found.
     */
    private void validateChallenges()
    {
        this.challengeCacheData.values().forEach(challenge -> {
            if (!this.isValidChallenge(challenge))
            {
                // If challenge's level is not found, then set it as free challenge.
                challenge.setLevel(FREE);
                this.addon.logWarning("Challenge's " + challenge.getUniqueId() + " level was not found in the database. " +
                        "To avoid any errors with missing level, challenge was added to the FREE level!");
            }
        });
    }


    /**
     * This method checks if given challenge's level exists in local cache or database.
     * @param challenge that must be validated
     * @return true ir challenge's level exists, otherwise false.
     */
    private boolean isValidChallenge(@NonNull Challenge challenge)
    {
        if (challenge.getLevel().equals(FREE) || this.getLevel(challenge.getLevel()) != null)
        {
            return true;
        }
        else
        {
            this.addon.logError("Cannot find " + challenge.getUniqueId() + " challenge's level " + challenge.getLevel() + " in database.");
            return false;
        }
    }


    /**
     * This method checks if given level all challenges exists in local cache or database.
     * It also checks if world where level must operate exists.
     * @param level that must be validated
     * @return true ir level is valid, otherwise false.
     */
    private boolean isValidLevel(@NonNull ChallengeLevel level)
    {
        if (!this.islandWorldManager.inWorld(Bukkit.getWorld(level.getWorld())))
        {
            return false;
        }

        for (String uniqueID : level.getChallenges())
        {
            if (!this.challengeCacheData.containsKey(uniqueID))
            {
                if (!this.challengeDatabase.objectExists(uniqueID) ||
                    !this.loadChallenge(this.challengeDatabase.loadObject(uniqueID), false, null, true))
                {
                    this.addon.logError("Cannot find " + uniqueID + " challenge for " + level.getUniqueId());
                    return false;
                }
            }
        }

        return true;
    }


    /**
     * Load player/island from database into the cache or create new player/island data
     *
     * @param uniqueID - uniqueID to add
     */
    private void addPlayerData(@NonNull String uniqueID)
    {
        if (this.playerCacheData.containsKey(uniqueID))
        {
            return;
        }

        // The player is not in the cache
        // Check if the player exists in the database

        if (this.playersDatabase.objectExists(uniqueID))
        {
            // Load player from database
            ChallengesPlayerData data = this.playersDatabase.loadObject(uniqueID);
            // Store in cache

            if (data != null)
            {
                this.playerCacheData.put(uniqueID, data);
            }
            else
            {
                this.addon.logError("Could not load NULL player data object.");
            }
        }
        else
        {
            // Create the player data
            ChallengesPlayerData pd = new ChallengesPlayerData(uniqueID);
            this.playersDatabase.saveObjectAsync(pd);
            // Add to cache
            this.playerCacheData.put(uniqueID, pd);
        }
    }

    // ---------------------------------------------------------------------
    // Section: Wipe data
    // ---------------------------------------------------------------------


    /**
     * This method removes all challenges addon data from Database.
     * @param complete Remove also user data.
     */
    public void wipeDatabase(boolean complete)
    {
        this.wipeLevels();
        this.wipeChallenges();

        if (complete)
        {
            this.wipePlayers();
        }
    }


    /**
     * This method collects all data from levels database and removes them.
     * Also clears levels cache data.
     */
    private void wipeLevels()
    {
        List<ChallengeLevel> levelList = this.levelDatabase.loadObjects();

        levelList.forEach(level -> this.levelDatabase.deleteID(level.getUniqueId()));
        this.levelCacheData.clear();
    }


    /**
     * This method collects all data from challenges database and removes them.
     * Also clears challenges cache data.
     */
    private void wipeChallenges()
    {
        List<Challenge> challengeList = this.challengeDatabase.loadObjects();

        challengeList.forEach(challenge -> this.challengeDatabase.deleteID(challenge.getUniqueId()));
        this.challengeCacheData.clear();
    }


    /**
     * This method collects all data from players database and removes them.
     * Also clears players cache data.
     */
    public void wipePlayers()
    {
        List<ChallengesPlayerData> playerDataList = this.playersDatabase.loadObjects();

        playerDataList.forEach(playerData -> this.playersDatabase.deleteID(playerData.getUniqueId()));
        this.playerCacheData.clear();
    }


    // ---------------------------------------------------------------------
    // Section: Wipe data
    // ---------------------------------------------------------------------


    /**
     * This method migrated all challenges addon data from worldName to addonID formant.
     */
    public void migrateDatabase(User user, World world)
    {
        world = Util.getWorld(world);

        if (user.isPlayer())
        {
            user.sendMessage("challenges.messages.admin.migrate-start");
        }
        else
        {
            this.addon.log("Starting migration to new data format.");
        }

        boolean challenges = this.migrateChallenges(world);
        boolean levels = this.migrateLevels(world);

        if (challenges || levels)
        {
            this.migratePlayers(world);

            if (user.isPlayer())
            {
                user.sendMessage("challenges.messages.admin.migrate-end");
            }
            else
            {
                this.addon.log("Migration to new data format completed.");
            }
        }
        else
        {
            if (user.isPlayer())
            {
                user.sendMessage("challenges.messages.admin.migrate-not");
            }
            else
            {
                this.addon.log("All data is valid. Migration is not necessary.");
            }
        }
    }


    /**
     * This method collects all data from levels database and migrates them.
     */
    private boolean migrateLevels(World world)
    {
        String addonName = Utils.getGameMode(world);

        if (addonName == null || addonName.equalsIgnoreCase(world.getName()))
        {
            return false;
        }

        boolean updated = false;
        List<ChallengeLevel> levelList = this.levelDatabase.loadObjects();
        for (ChallengeLevel level : levelList)
        {
            if (level.getUniqueId().regionMatches(true, 0, world.getName() + "_", 0, world.getName().length() + 1))
            {
                this.levelDatabase.deleteID(level.getUniqueId());
                this.levelCacheData.remove(level.getUniqueId());

                level.setUniqueId(
                        addonName + level.getUniqueId().substring(world.getName().length()));

                Set<String> challengesID = new HashSet<>(level.getChallenges());
                level.getChallenges().clear();

                challengesID.forEach(challenge ->
                level.getChallenges().add(addonName + challenge.substring(world.getName().length())));

                this.levelDatabase.saveObjectAsync(level);
                this.levelCacheData.put(level.getUniqueId(), level);

                updated = true;
            }
        }

        return updated;
    }


    /**
     * This method collects all data from challenges database and migrates them.
     */
    @SuppressWarnings("deprecation")
    private boolean migrateChallenges(World world)
    {
        String addonName = Utils.getGameMode(world);

        if (addonName == null || addonName.equalsIgnoreCase(world.getName()))
        {
            return false;
        }

        boolean updated = false;

        List<Challenge> challengeList = this.challengeDatabase.loadObjects();

        for (Challenge challenge : challengeList)
        {
            if (challenge.getUniqueId().regionMatches(true, 0, world.getName() + "_", 0, world.getName().length() + 1))
            {
                this.challengeDatabase.deleteID(challenge.getUniqueId());
                this.challengeCacheData.remove(challenge.getUniqueId());

                challenge.setUniqueId(addonName + challenge.getUniqueId().substring(world.getName().length()));

                if (!challenge.getLevel().equals(FREE))
                {
                    challenge.setLevel(addonName + challenge.getLevel().substring(world.getName().length()));
                }

                updated = true;

                this.challengeDatabase.saveObjectAsync(challenge);
                this.challengeCacheData.put(challenge.getUniqueId(), challenge);
            }

            // Migrate Requirements.
            if (challenge.getRequirements() == null)
            {
                switch (challenge.getChallengeType())
                {
                case INVENTORY:
                    InventoryRequirements inventoryRequirements = new InventoryRequirements();
                    inventoryRequirements.setRequiredItems(challenge.getRequiredItems());
                    inventoryRequirements.setTakeItems(challenge.isTakeItems());

                    inventoryRequirements.setRequiredPermissions(challenge.getRequiredPermissions());
                    challenge.setRequirements(inventoryRequirements);
                    break;
                case ISLAND:
                    IslandRequirements islandRequirements = new IslandRequirements();
                    islandRequirements.setRemoveBlocks(challenge.isRemoveBlocks());
                    islandRequirements.setRemoveEntities(challenge.isRemoveEntities());
                    islandRequirements.setRequiredBlocks(challenge.getRequiredBlocks());
                    islandRequirements.setRequiredEntities(challenge.getRequiredEntities());
                    islandRequirements.setSearchRadius(challenge.getSearchRadius());

                    islandRequirements.setRequiredPermissions(challenge.getRequiredPermissions());
                    challenge.setRequirements(islandRequirements);
                    break;
                case OTHER:
                    OtherRequirements otherRequirements = new OtherRequirements();
                    otherRequirements.setRequiredExperience(challenge.getRequiredExperience());
                    otherRequirements.setRequiredIslandLevel(challenge.getRequiredIslandLevel());
                    otherRequirements.setRequiredMoney(challenge.getRequiredMoney());
                    otherRequirements.setTakeExperience(challenge.isTakeExperience());
                    otherRequirements.setTakeMoney(challenge.isTakeMoney());

                    otherRequirements.setRequiredPermissions(challenge.getRequiredPermissions());
                    challenge.setRequirements(otherRequirements);
                    break;
                }

                // This save should not involve any upgrades in other parts.

                this.challengeDatabase.saveObjectAsync(challenge);
                this.challengeCacheData.put(challenge.getUniqueId(), challenge);
            }
        }

        return updated;
    }


    /**
     * This method collects all data from players database and migrates them.
     */
    private void migratePlayers(World world)
    {
        String addonName = Utils.getGameMode(world);

        if (addonName == null || addonName.equalsIgnoreCase(world.getName()))
        {
            return;
        }

        List<ChallengesPlayerData> playerDataList = this.playersDatabase.loadObjects();

        playerDataList.forEach(playerData -> {
            Set<String> levelsDone = new TreeSet<>(playerData.getLevelsDone());
            levelsDone.forEach(level -> {
                if (level.regionMatches(true, 0, world.getName() + "_", 0, world.getName().length() + 1))
                {
                    playerData.getLevelsDone().remove(level);
                    playerData.getLevelsDone().add(addonName + level.substring(world.getName().length()));
                }
            });

            Map<String, Integer> challengeStatus = new TreeMap<>(playerData.getChallengeStatus());
            challengeStatus.forEach((challenge, count) -> {
                if (challenge.regionMatches(true, 0, world.getName() + "_", 0, world.getName().length() + 1))
                {
                    playerData.getChallengeStatus().remove(challenge);
                    playerData.getChallengeStatus().put(addonName + challenge.substring(world.getName().length()), count);
                }
            });

            Map<String, Long> challengeTimestamp = new TreeMap<>(playerData.getChallengesTimestamp());
            challengeTimestamp.forEach((challenge, timestamp) -> {
                if (challenge.regionMatches(true, 0, world.getName() + "_", 0, world.getName().length() + 1))
                {
                    playerData.getChallengesTimestamp().remove(challenge);
                    playerData.getChallengesTimestamp().put(addonName + challenge.substring(world.getName().length()), timestamp);
                }
            });

            this.playersDatabase.saveObjectAsync(playerData);
        });
    }


    // ---------------------------------------------------------------------
    // Section: Saving methods
    // ---------------------------------------------------------------------


    /**
     * This method init all cached object saving to database.
     */
    public void save()
    {
        // Challenges and Levels are saved on modifications only to avoid issues with
        // NULL's in data after interrupting server while in saving stage.
        // this.saveChallenges();
        // this.saveLevels();

        this.savePlayersData();
    }


    /**
     * This method saves all challenges to database.
     */
    public void saveChallenges()
    {
        this.challengeCacheData.values().forEach(this::saveChallenge);
    }


    /**
     * This method saves given challenge object to database.
     * @param challenge object that must be saved
     */
    public void saveChallenge(Challenge challenge)
    {
        this.challengeDatabase.saveObjectAsync(challenge);
    }


    /**
     * This method saves all levels to database.
     */
    public void saveLevels()
    {
        this.levelCacheData.values().forEach(this::saveLevel);
    }


    /**
     * This method saves given level into database.
     * @param level object that must be saved
     */
    public void saveLevel(ChallengeLevel level)
    {
        this.levelDatabase.saveObjectAsync(level);
    }


    /**
     * This method saves all players/islands to database.
     */
    private void savePlayersData()
    {
        this.playerCacheData.values().forEach(this.playersDatabase::saveObjectAsync);
    }


    /**
     * This method saves player/island with given UUID.
     * @param uniqueID user/island UUID.
     */
    private void savePlayerData(@NonNull String uniqueID)
    {
        if (this.playerCacheData.containsKey(uniqueID))
        {
            // Clean History Data
            ChallengesPlayerData cachedData = this.playerCacheData.get(uniqueID);

            if (this.settings.getLifeSpan() > 0)
            {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, -this.settings.getLifeSpan());
                long survivalTime = calendar.getTimeInMillis();

                Iterator<LogEntry> entryIterator = cachedData.getHistory().iterator();

                while (entryIterator.hasNext() && this.shouldBeRemoved(entryIterator.next(), survivalTime))
                {
                    entryIterator.remove();
                }
            }

            this.playersDatabase.saveObjectAsync(cachedData);
        }
    }


    // ---------------------------------------------------------------------
    // Section: Private methods that is used to process player/island data.
    // ---------------------------------------------------------------------


    /**
     * This method returns if given log entry stored time stamp is older then survivalTime.
     * @param entry Entry that must be checed.
     * @param survivalTime TimeStamp value.
     * @return true, if log entry is too old for database.
     */
    private boolean shouldBeRemoved(LogEntry entry, long survivalTime)
    {
        return entry.getTimestamp() < survivalTime;
    }



    /**
     * This method returns UUID that corresponds to player or player's island in given world.
     *
     * @param user of type User
     * @param world of type World
     * @return UUID
     */
    private String getDataUniqueID(User user, World world)
    {
        return this.getDataUniqueID(user.getUniqueId(), world);
    }


    /**
     * This method returns UUID that corresponds to player or player's island in given world.
     *
     * @param userID of type User
     * @param world of type World
     * @return UUID
     */
    private String getDataUniqueID(UUID userID, World world)
    {
        if (this.settings.isStoreAsIslandData())
        {
            Island island = this.addon.getIslands().getIsland(world, userID);

            if (island == null)
            {
                // If storage is in island mode and user does not have island, then it can happen.
                // This should never happen ...
                // Just return random UUID and hope that it will not be necessary.
                return "";
            }
            else
            {
                // Unfortunately, island does not store UUID, just a string.
                return island.getUniqueId();
            }
        }
        else
        {
            return userID.toString();
        }
    }


    /**
     * Checks if a challengeID is complete or not
     *
     * @param storageDataID - PlayerData ID object who must be checked.
     * @param challengeID - Challenge uniqueID
     * @return - true if completed
     */
    private long getChallengeTimes(String storageDataID, String challengeID)
    {
        this.addPlayerData(storageDataID);
        return this.playerCacheData.get(storageDataID).getTimes(challengeID);
    }


    /**
     * Checks if a challenge with given ID is complete or not
     *
     * @param storageDataID - PlayerData ID object who must be checked.
     * @param challengeID - Challenge uniqueID
     * @return - true if completed
     */
    private boolean isChallengeComplete(String storageDataID, String challengeID)
    {
        this.addPlayerData(storageDataID);
        return this.playerCacheData.get(storageDataID).isChallengeDone(challengeID);
    }


    /**
     * Sets the challenge with given ID as complete and increments the number of times it has been
     * completed
     *
     * @param storageDataID - playerData ID
     * @param challengeID - challengeID
     */
    private void setChallengeComplete(@NonNull String storageDataID, @NonNull String challengeID)
    {
        this.setChallengeComplete(storageDataID, challengeID, 1);
    }


    /**
     * Sets the challenge with given ID as complete and increments the number of times it has been
     * completed
     *
     * @param storageDataID - playerData ID
     * @param challengeID - challengeID
     * @param count - how many times challenge is completed
     */
    private void setChallengeComplete(@NonNull String storageDataID, @NonNull String challengeID, int count)
    {
        this.addPlayerData(storageDataID);
        this.playerCacheData.get(storageDataID).addChallengeDone(challengeID, count);
        // Save
        this.savePlayerData(storageDataID);
    }


    /**
     * Reset the challenge with given ID to zero time / not done
     *
     * @param storageDataID - playerData ID
     * @param challengeID - challenge ID
     */
    private void resetChallenge(@NonNull String storageDataID, @NonNull String challengeID)
    {
        this.addPlayerData(storageDataID);
        this.playerCacheData.get(storageDataID).setChallengeTimes(challengeID, 0);
        // Save
        this.savePlayerData(storageDataID);
    }


    /**
     * Resets all the challenges for user in given GameMode.
     *
     * @param storageDataID - island owner's UUID
     * @param gameMode - GameMode name.
     */
    private void resetAllChallenges(@NonNull String storageDataID, @NonNull String gameMode)
    {
        this.addPlayerData(storageDataID);

        if (this.playerCacheData.containsKey(storageDataID))
        {
            // There may be a rare situation when player data cannot be loaded. Just avoid
            // error.
            this.playerCacheData.get(storageDataID).reset(gameMode);
        }
        else
        {
            // If object cannot be loaded remove it completely.
            this.playersDatabase.deleteID(storageDataID);
            this.addon.logError("Database object was not loaded. It is removed completely. Object Id: " + storageDataID);
        }

        // Save
        this.savePlayerData(storageDataID);
    }


    /**
     * Get the status on every level for required world and playerData
     *
     * @param storageDataID - playerData ID
     * @param gameMode - World Name where levels should be searched.
     * @return Level status - how many challenges still to do on which level
     */
    @NonNull
    private List<LevelStatus> getAllChallengeLevelStatus(String storageDataID, String gameMode)
    {
        this.addPlayerData(storageDataID);
        ChallengesPlayerData playerData = this.playerCacheData.get(storageDataID);

        List<ChallengeLevel> challengeLevelList = this.getLevels(gameMode);

        List<LevelStatus> result = new ArrayList<>();

        // The first level is always unlocked and previous for it is null.
        ChallengeLevel previousLevel = null;
        int doneChallengeCount = 0;
        boolean previousUnlocked = true;

        // For each challenge level, check how many the storageDataID has done
        for (ChallengeLevel level : challengeLevelList)
        {
            // To find how many challenges user still must do in previous level, we must
            // know how many challenges there were and how many has been done. Then
            // remove waiver amount to get count of challenges that still necessary to do.

            int challengesToDo = previousLevel == null ? 0 :
                (previousLevel.getChallenges().size() - doneChallengeCount - previousLevel.getWaiverAmount());

            // As level already contains unique ids of challenges, just iterate through them.
            doneChallengeCount = (int) level.getChallenges().stream().filter(playerData::isChallengeDone).count();

            // Mark if level is unlocked
            boolean unlocked = previousUnlocked && challengesToDo <= 0;

            result.add(new LevelStatus(
                    level,
                    previousLevel,
                    challengesToDo,
                    level.getChallenges().size() == doneChallengeCount,
                    unlocked));

            previousLevel = level;
            previousUnlocked = unlocked;
        }

        return result;
    }


    /**
     * This method returns LevelStatus object for given challenge level.
     * @param storageDataID User which level status must be acquired.
     * @param world World where level is living.
     * @param level Level which status must be calculated.
     * @return LevelStatus of given level.
     */
    @Nullable
    private LevelStatus getChallengeLevelStatus(@NonNull String storageDataID, World world, @NonNull ChallengeLevel level)
    {
        this.addPlayerData(storageDataID);
        ChallengesPlayerData playerData = this.playerCacheData.get(storageDataID);

        List<ChallengeLevel> challengeLevelList = this.getLevels(world);

        int levelIndex = challengeLevelList.indexOf(level);

        if (levelIndex == -1)
        {
            return null;
        }
        else
        {
            ChallengeLevel previousLevel = levelIndex < 1 ? null : challengeLevelList.get(levelIndex - 1);

            int challengesToDo = previousLevel == null ? 0 :
                (previousLevel.getChallenges().size() - previousLevel.getWaiverAmount()) -
                (int) previousLevel.getChallenges().stream().filter(playerData::isChallengeDone).count();

            // As level already contains unique ids of challenges, just iterate through them.
            int doneChallengeCount = (int) level.getChallenges().stream().filter(playerData::isChallengeDone).count();

            return new LevelStatus(
                    level,
                    previousLevel,
                    challengesToDo,
                    level.getChallenges().size() == doneChallengeCount,
                    challengesToDo <= 0);
        }
    }

    /**
     * This method returns if given user has been already completed given level.
     * @param levelID Level that must be checked.
     * @param storageDataID User who need to be checked.
     * @return true, if level is already completed.
     */
    private boolean isLevelCompleted(@NonNull String storageDataID, @NonNull String levelID)
    {
        this.addPlayerData(storageDataID);
        return this.playerCacheData.get(storageDataID).isLevelDone(levelID);
    }


    /**
     * This method checks all level challenges and checks if all challenges are done.
     * @param level Level that must be checked.
     * @param storageDataID User who need to be checked.
     * @return true, if all challenges are done, otherwise false.
     */
    private boolean validateLevelCompletion(@NonNull String storageDataID, @NonNull ChallengeLevel level)
    {
        this.addPlayerData(storageDataID);
        ChallengesPlayerData playerData = this.playerCacheData.get(storageDataID);
        long doneChallengeCount = level.getChallenges().stream().filter(playerData::isChallengeDone).count();

        return level.getChallenges().size() == doneChallengeCount;
    }


    /**
     * This method sets given level as completed.
     * @param levelID Level that must be completed.
     * @param storageDataID User who complete level.
     */
    private void setLevelComplete(@NonNull String storageDataID, @NonNull String levelID)
    {
        this.addPlayerData(storageDataID);
        this.playerCacheData.get(storageDataID).addCompletedLevel(levelID);
        // Save
        this.savePlayerData(storageDataID);
    }


    /**
     * This methods adds given log entry to database.
     *
     * @param storageDataID of type UUID
     * @param entry of type LogEntry
     */
    private void addLogEntry(@NonNull String storageDataID, @NonNull LogEntry entry)
    {
        // Store data only if it is enabled.

        if (this.settings.isStoreHistory())
        {
            this.addPlayerData(storageDataID);
            this.playerCacheData.get(storageDataID).addHistoryRecord(entry);
            // Save
            this.savePlayerData(storageDataID);
        }
    }


    // ---------------------------------------------------------------------
    // Section: Public methods for processing player/island data.
    // ---------------------------------------------------------------------


    /**
     * This method returns if given user has completed given challenge in world.
     * @param user - User that must be checked.
     * @param world - World where challenge operates.
     * @param challenge - Challenge that must be checked.
     * @return True, if challenge is completed, otherwise - false.
     */
    public boolean isChallengeComplete(User user, World world, Challenge challenge)
    {
        return this.isChallengeComplete(user.getUniqueId(), world, challenge.getUniqueId());
    }


    /**
     * This method returns if given user has completed given challenge in world.
     * @param user - User that must be checked.
     * @param world - World where challenge operates.
     * @param challenge - Challenge that must be checked.
     * @return True, if challenge is completed, otherwise - false.
     */
    public boolean isChallengeComplete(UUID user, World world, Challenge challenge)
    {
        return this.isChallengeComplete(user, world, challenge.getUniqueId());
    }


    /**
     * This method returns if given user has completed given challenge in world.
     * @param user - User that must be checked.
     * @param world - World where challenge operates.
     * @param challengeID - Challenge that must be checked.
     * @return True, if challenge is completed, otherwise - false.
     */
    public boolean isChallengeComplete(UUID user, World world, String challengeID)
    {
        world = Util.getWorld(world);
        return this.isChallengeComplete(this.getDataUniqueID(user, world), challengeID);
    }


    /**
     * This method sets given challenge as completed.
     * @param user - Targeted user.
     * @param world - World where completion must be called.
     * @param challenge - That must be completed.
     */
    public void setChallengeComplete(User user, World world, Challenge challenge, int completionCount)
    {
        this.setChallengeComplete(user.getUniqueId(), world, challenge, completionCount);
    }


    /**
     * This method sets given challenge as completed.
     * @param userID - Targeted user.
     * @param world - World where completion must be called.
     * @param challenge - That must be completed.
     */
    public void setChallengeComplete(UUID userID, World world, Challenge challenge, int completionCount)
    {
        String storageID = this.getDataUniqueID(userID, Util.getWorld(world));
        this.setChallengeComplete(storageID, challenge.getUniqueId(), completionCount);
        this.addLogEntry(storageID, new LogEntry.Builder("COMPLETE").
                data(USER_ID, userID.toString()).
                data(CHALLENGE_ID, challenge.getUniqueId()).
                data("completion-count", Integer.toString(completionCount)).
                build());

        // Fire event that user completes challenge
        Bukkit.getPluginManager().callEvent(
                new ChallengeCompletedEvent(challenge.getUniqueId(),
                        userID,
                        false,
                        completionCount));
    }


    /**
     * This method sets given challenge as completed.
     * @param userID - Targeted user.
     * @param world - World where completion must be called.
     * @param challenge - That must be completed.
     * @param adminID - admin who sets challenge as completed.
     */
    public void setChallengeComplete(UUID userID, World world, Challenge challenge, UUID adminID)
    {
        String storageID = this.getDataUniqueID(userID, Util.getWorld(world));

        this.setChallengeComplete(storageID, challenge.getUniqueId());
        this.addLogEntry(storageID, new LogEntry.Builder("COMPLETE").
                data(USER_ID, userID.toString()).
                data(CHALLENGE_ID, challenge.getUniqueId()).
                data(ADMIN_ID, adminID == null ? "OP" : adminID.toString()).
                build());

        // Fire event that admin completes user challenge
        Bukkit.getPluginManager().callEvent(
                new ChallengeCompletedEvent(challenge.getUniqueId(),
                        userID,
                        true,
                        1));
    }


    /**
     * This method resets given challenge.
     * @param userID - Targeted user.
     * @param world - World where reset must be called.
     * @param challenge - That must be reset.
     */
    public void resetChallenge(UUID userID, World world, Challenge challenge, UUID adminID)
    {
        String storageID = this.getDataUniqueID(userID, Util.getWorld(world));

        this.resetChallenge(storageID, challenge.getUniqueId());
        this.addLogEntry(storageID, new LogEntry.Builder(RESET).
                data(USER_ID, userID.toString()).
                data(CHALLENGE_ID, challenge.getUniqueId()).
                data(ADMIN_ID, adminID == null ? RESET : adminID.toString()).
                build());

        // Fire event that admin resets user challenge
        Bukkit.getPluginManager().callEvent(
                new ChallengeResetEvent(challenge.getUniqueId(),
                        userID,
                        true,
                        RESET));
    }


    /**
     * This method resets all challenges in given world.
     * @param user - Targeted user.
     * @param world - World where challenges must be reset.
     */
    public void resetAllChallenges(User user, World world)
    {
        this.resetAllChallenges(user.getUniqueId(), world, null);
    }


    /**
     * This method resets all challenges in given world.
     * @param userID - Targeted user.
     * @param world - World where challenges must be reset.
     * @param adminID - admin iD
     */
    public void resetAllChallenges(@NonNull UUID userID, World world, @Nullable UUID adminID)
    {
        String storageID = this.getDataUniqueID(userID, Util.getWorld(world));

        this.islandWorldManager.getAddon(world).ifPresent(gameMode -> {
            this.resetAllChallenges(storageID, gameMode.getDescription().getName());
            this.addLogEntry(storageID, new LogEntry.Builder("RESET_ALL").
                    data(USER_ID, userID.toString()).
                    data(ADMIN_ID, adminID == null ? "ISLAND_RESET" : adminID.toString()).
                    build());

            // Fire event that admin resets user challenge
            Bukkit.getPluginManager().callEvent(
                    new ChallengeResetAllEvent(gameMode.getDescription().getName(),
                            userID,
                            adminID != null,
                            adminID == null ? "ISLAND_RESET" : "RESET_ALL"));
        });
    }


    /**
     * Checks if a challenge is complete or not
     *
     * @param user - User that must be checked.
     * @param world - World where challenge operates.
     * @param challenge - Challenge that must be checked.
     * @return - true if completed
     */
    public long getChallengeTimes(User user, World world, Challenge challenge)
    {
        return this.getChallengeTimes(user, world, challenge.getUniqueId());
    }


    /**
     * Checks if a challenge is complete or not
     *
     * @param user - User that must be checked.
     * @param world - World where challenge operates.
     * @param challenge - Challenge that must be checked.
     * @return - true if completed
     */
    public long getChallengeTimes(User user, World world, String challenge)
    {
        world = Util.getWorld(world);
        return this.getChallengeTimes(this.getDataUniqueID(user, world), challenge);
    }


    /**
     * This method returns if given user has been already completed given level.
     * @param world World where level must be checked.
     * @param level Level that must be checked.
     * @param user User who need to be checked.
     * @return true, if level is already completed.
     */
    public boolean isLevelCompleted(User user, World world, ChallengeLevel level)
    {
        return this.isLevelCompleted(this.getDataUniqueID(user, Util.getWorld(world)), level.getUniqueId());
    }


    /**
     * This method returns if given user has unlocked given level.
     * @param world World where level must be checked.
     * @param level Level that must be checked.
     * @param user User who need to be checked.
     * @return true, if level is unlocked.
     */
    public boolean isLevelUnlocked(User user, World world, ChallengeLevel level)
    {
        String storageDataID = this.getDataUniqueID(user, Util.getWorld(world));
        this.addPlayerData(storageDataID);

        return this.islandWorldManager.getAddon(world).filter(gameMode -> this.getAllChallengeLevelStatus(storageDataID, gameMode.getDescription().getName()).
                stream().
                filter(LevelStatus::isUnlocked).
                anyMatch(lv -> lv.getLevel().equals(level))).
                isPresent();

    }


    /**
     * This method sets given level as completed.
     * @param world World where level must be completed.
     * @param level Level that must be completed.
     * @param user User who need to be updated.
     */
    public void setLevelComplete(User user, World world, ChallengeLevel level)
    {
        String storageID = this.getDataUniqueID(user, Util.getWorld(world));

        this.setLevelComplete(storageID, level.getUniqueId());
        this.addLogEntry(storageID, new LogEntry.Builder("COMPLETE_LEVEL").
                data(USER_ID, user.getUniqueId().toString()).
                data("level", level.getUniqueId()).build());

        // Fire event that user completes level
        Bukkit.getPluginManager().callEvent(
                new LevelCompletedEvent(level.getUniqueId(),
                        user.getUniqueId(),
                        false));
    }


    /**
     * This method checks all level challenges and checks if all challenges are done.
     * @param world World where level must be validated.
     * @param level Level that must be validated.
     * @param user User who need to be validated.
     * @return true, if all challenges are done, otherwise false.
     */
    public boolean validateLevelCompletion(User user, World world, ChallengeLevel level)
    {
        return this.validateLevelCompletion(this.getDataUniqueID(user, Util.getWorld(world)), level);
    }


    /**
     * This method returns LevelStatus object for given challenge level.
     * @param uniqueId UUID of user who need to be validated.
     * @param world World where level must be validated.
     * @param level Level that must be validated.
     * @return LevelStatus of given level.
     */
    @Nullable
    public LevelStatus getChallengeLevelStatus(UUID uniqueId, World world, ChallengeLevel level)
    {
        return this.getChallengeLevelStatus(this.getDataUniqueID(uniqueId, Util.getWorld(world)), world, level);
    }


    /**
     * Get the status on every level for required world and user
     *
     * @param user - user which levels should be checked
     * @param world - World where levels should be searched.
     * @return Level status - how many challenges still to do on which level
     */
    @NonNull
    public List<LevelStatus> getAllChallengeLevelStatus(User user, World world)
    {
        return this.islandWorldManager.getAddon(world).map(gameMode ->
            this.getAllChallengeLevelStatus(
                this.getDataUniqueID(user, Util.getWorld(world)),
                gameMode.getDescription().getName())).
                orElse(Collections.emptyList());
    }


    /**
     * This method returns latest ChallengeLevel object that is unlocked.
     * @param user user who latest unlocked level must be returned.
     * @param world World where level operates.
     * @return ChallengeLevel for latest unlocked level.
     */
    @Nullable
    public ChallengeLevel getLatestUnlockedLevel(User user, World world)
    {
        LevelStatus lastStatus = null;

        for (Iterator<LevelStatus> statusIterator = this.getAllChallengeLevelStatus(user, world).iterator();
            statusIterator.hasNext() && (lastStatus == null || lastStatus.isUnlocked());)
        {
            lastStatus = statusIterator.next();
        }

        return lastStatus != null ? lastStatus.getPreviousLevel() : null;
    }


    // ---------------------------------------------------------------------
    // Section: Challenges related methods
    // ---------------------------------------------------------------------


    /**
     * Get the list of all challenge unique names for world.
     *
     * @param world - the world to check
     * @return List of challenge names
     */
    public List<String> getAllChallengesNames(@NonNull World world)
    {
        return this.islandWorldManager.getAddon(world).map(gameMode ->
            this.challengeCacheData.values().stream().
                filter(challenge -> challenge.matchGameMode(gameMode.getDescription().getName())).
                sorted(this.challengeComparator).
                map(Challenge::getUniqueId).
                collect(Collectors.toList())).
            orElse(Collections.emptyList());
    }


    /**
     * Get the list of all challenge for world.
     *
     * @param world - the world to check
     * @return List of challenges
     */
    public List<Challenge> getAllChallenges(@NonNull World world)
    {
        return this.islandWorldManager.getAddon(world).map(gameMode ->
            this.challengeCacheData.values().stream().
                filter(challenge -> challenge.matchGameMode(gameMode.getDescription().getName())).
                sorted(this.challengeComparator).
                collect(Collectors.toList())).
            orElse(Collections.emptyList());
    }


    /**
     * Free challenges... Challenges without a level.
     * @param world World in which challenges must be searched.
     * @return List with free challenges in given world.
     */
    public List<Challenge> getFreeChallenges(@NonNull World world)
    {
        // Free Challenges hides under FREE level.
        return this.islandWorldManager.getAddon(world).map(gameMode ->
        this.challengeCacheData.values().stream().
        filter(challenge -> challenge.getLevel().equals(FREE) &&
                challenge.matchGameMode(gameMode.getDescription().getName())).
        sorted(Comparator.comparing(Challenge::getOrder)).
        collect(Collectors.toList())).
                orElse(Collections.emptyList());
    }


    /**
     * Level which challenges must be received
     * @param level Challenge level.
     * @return List with challenges in given level.
     */
    public List<Challenge> getLevelChallenges(ChallengeLevel level)
    {
        return level.getChallenges().stream().
                map(this::getChallenge).
                filter(Objects::nonNull).
                sorted(Comparator.comparing(Challenge::getOrder)).
                collect(Collectors.toList());
    }


    /**
     * Get challenge by name. Case sensitive
     *
     * @param name - unique name of challenge
     * @return - challenge or null if it does not exist
     */
    @Nullable
    public Challenge getChallenge(String name)
    {
        if (this.challengeCacheData.containsKey(name))
        {
            return this.challengeCacheData.get(name);
        }
        else
        {
            // check database.
            if (this.challengeDatabase.objectExists(name))
            {
                Challenge challenge = this.challengeDatabase.loadObject(name);

                if (challenge != null)
                {
                    this.challengeCacheData.put(name, challenge);
                    return challenge;
                }
                else
                {
                    this.addon.logError("Tried to load NULL challenge object!");
                }
            }
        }

        return null;
    }


    /**
     * Check if a challenge exists - case insensitive
     *
     * @param name - name of challenge
     * @return true if it exists, otherwise false
     */
    public boolean containsChallenge(String name)
    {
        return getChallenge(name) != null;
    }


    /**
     * This method creates and returns new challenge with given uniqueID.
     * @param uniqueID - new ID for challenge.
     * @param requirements - requirements object, as it is not changeable anymore.
     * @return Challenge that is currently created.
     */
    @Nullable
    public Challenge createChallenge(String uniqueID, Challenge.ChallengeType type, Requirements requirements)
    {
        if (!this.containsChallenge(uniqueID))
        {
            Challenge challenge = new Challenge();
            challenge.setUniqueId(uniqueID);
            challenge.setRequirements(requirements);
            challenge.setChallengeType(type);

            this.saveChallenge(challenge);
            this.loadChallenge(challenge);

            return challenge;
        }
        else
        {
            return null;
        }
    }


    /**
     * This method removes challenge from cache and memory.
     * TODO: This will not remove challenge from user data. Probably should do it.
     * @param challenge that must be removed.
     */
    public void deleteChallenge(Challenge challenge)
    {
        if (this.challengeCacheData.containsKey(challenge.getUniqueId()))
        {
            // First remove challenge from its owner level.

            if (!challenge.getLevel().equals(FREE))
            {
                ChallengeLevel level = this.getLevel(challenge.getLevel());

                if (level != null)
                {
                    this.removeChallengeFromLevel(challenge, level);
                }
            }

            // Afterwards remove challenge from the database.

            this.challengeCacheData.remove(challenge.getUniqueId());
            this.challengeDatabase.deleteObject(challenge);
        }
    }


    /**
     * This method returns number of challenges in given world.
     * @param world World where challenge count must be returned.
     * @return Number of challenges in given world.
     */
    public int getChallengeCount(World world)
    {
        return this.getAllChallenges(world).size();
    }


    /**
     * This method returns number of completed challenges in given world.
     * @param user User which completed challenge count must be returned.
     * @param world World where challenge count must be returned.
     * @return Number of completed challenges by given user in given world.
     */
    public long getCompletedChallengeCount(User user, World world)
    {
        return this.getAllChallenges(world).stream().
            filter(challenge -> this.getChallengeTimes(user, world, challenge) > 0).
            count();
    }


    /**
     * This method returns total number of all completion times for all challenges in given world.
     * @param user User which total completion count must be returned.
     * @param world World where challenge count must be returned.
     * @return Sum of completion count for all challenges by given user in given world.
     */
    public long getTotalChallengeCompletionCount(User user, World world)
    {
        return this.getAllChallenges(world).stream().
            mapToLong(challenge -> this.getChallengeTimes(user, world, challenge)).
            sum();
    }


    /**
     * This method returns completed challenge count in given level.
     * @param user User which should be checked
     * @param world World where challenges are operating
     * @param level Level which challenges must be checked.
     * @return Number of completed challenges in given level.
     */
    public long getLevelCompletedChallengeCount(User user, World world, ChallengeLevel level)
    {
        return this.getLevelChallenges(level).stream().
            filter(challenge -> this.getChallengeTimes(user, world, challenge) > 0).
            count();
    }


    // ---------------------------------------------------------------------
    // Section: Level related methods
    // ---------------------------------------------------------------------


    /**
     * This method returns list of challenge levels in given world.
     * @param world for which levels must be searched.
     * @return List with challenges in given world.
     */
    public List<ChallengeLevel> getLevels(@NonNull World world)
    {
        return this.islandWorldManager.getAddon(world).map(gameMode ->
            this.getLevels(gameMode.getDescription().getName())).
            orElse(Collections.emptyList());
    }


    /**
     * This method returns list of challenge levels in given gameMode.
     * @param gameMode for which levels must be searched.
     * @return List with challengeLevel in given gameMode.
     */
    private List<ChallengeLevel> getLevels(String gameMode)
    {
        // TODO: Probably need to check also database.
        return this.levelCacheData.values().stream().
            sorted(ChallengeLevel::compareTo).
            filter(level -> level.matchGameMode(gameMode)).
            collect(Collectors.toList());
    }


    /**
     * This method returns list of challenge levels in given gameMode.
     * @param world for which levels must be searched.
     * @return List with challengeLevel uniqueIds in given world.
     */
    public List<String> getLevelNames(@NonNull World world)
    {
        return this.islandWorldManager.getAddon(world).map(gameMode ->
            this.levelCacheData.values().stream().
                sorted(ChallengeLevel::compareTo).
                filter(level -> level.matchGameMode(gameMode.getDescription().getName())).
                map(ChallengeLevel::getUniqueId).
                collect(Collectors.toList())).
            orElse(Collections.emptyList());
    }


    /**
     * Get challenge level by its challenge.
     *
     * @param challenge - challenge which level must be returned.
     * @return - challenge level or null if it does not exist
     */
    @Nullable
    public ChallengeLevel getLevel(Challenge challenge)
    {
        if (!challenge.getLevel().equals(FREE))
        {
            return this.getLevel(challenge.getLevel());
        }

        return new ChallengeLevel();
    }


    /**
     * Get challenge level by name. Case sensitive
     *
     * @param name - unique name of challenge level
     * @return - challenge level or null if it does not exist
     */
    @Nullable
    public ChallengeLevel getLevel(String name)
    {
        if (this.levelCacheData.containsKey(name))
        {
            return this.levelCacheData.get(name);
        }
        else
        {
            // check database.
            if (this.levelDatabase.objectExists(name))
            {
                ChallengeLevel level = this.levelDatabase.loadObject(name);

                if (level != null)
                {
                    this.levelCacheData.put(name, level);
                    return level;
                }
                else
                {
                    this.addon.logError("Tried to load NULL level.");
                }
            }
        }

        return null;
    }


    /**
     * Check if a challenge level exists - case insensitive
     *
     * @param name - name of challenge level
     * @return true if it exists, otherwise false
     */
    public boolean containsLevel(String name)
    {
        if (this.levelCacheData.containsKey(name))
        {
            return true;
        }
        else
        {
            // check database.
            if (this.levelDatabase.objectExists(name))
            {
                ChallengeLevel level = this.levelDatabase.loadObject(name);

                if (level != null)
                {
                    this.levelCacheData.put(name, level);
                    return true;
                }
                else
                {
                    this.addon.logError("Tried to load NULL level.");
                }
            }
        }

        return false;
    }


    /**
     * This method adds given challenge to given challenge level.
     * @param newChallenge Challenge who must change owner.
     * @param newLevel Level to add to - must exist already
     */
    public void addChallengeToLevel(Challenge newChallenge, ChallengeLevel newLevel)
    {
        if (newChallenge.getLevel().equals(FREE))
        {
            newLevel.getChallenges().add(newChallenge.getUniqueId());
            newChallenge.setLevel(newLevel.getUniqueId());

            this.saveLevel(newLevel);
            this.saveChallenge(newChallenge);
        }
        else
        {
            ChallengeLevel oldLevel = this.getLevel(newChallenge.getLevel());

            if (oldLevel == null || !oldLevel.equals(newLevel))
            {
                this.removeChallengeFromLevel(newChallenge, newLevel);
                newLevel.getChallenges().add(newChallenge.getUniqueId());
                newChallenge.setLevel(newLevel.getUniqueId());

                this.saveLevel(newLevel);
                this.saveChallenge(newChallenge);
            }
        }
    }


    /**
     * This method removes given challenge from given challenge level.
     * @param challenge Challenge which must leave level.
     * @param level level which lost challenge
     */
    public void removeChallengeFromLevel(Challenge challenge, ChallengeLevel level)
    {
        if (level.getChallenges().contains(challenge.getUniqueId()))
        {
            level.getChallenges().remove(challenge.getUniqueId());
            challenge.setLevel(FREE);
            this.saveLevel(level);
            this.saveChallenge(challenge);
        }
    }


    /**
     * This method creates and returns new challenges level with given uniqueID.
     * @param uniqueID - new ID for challenge level.
     * @return ChallengeLevel that is currently created.
     */
    @Nullable
    public ChallengeLevel createLevel(String uniqueID, World world)
    {
        if (!this.containsLevel(uniqueID))
        {
            ChallengeLevel level = new ChallengeLevel();
            level.setUniqueId(uniqueID);
            level.setWorld(world.getName());

            this.saveLevel(level);
            this.loadLevel(level);

            return level;
        }
        else
        {
            return null;
        }
    }


    /**
     * This method removes challenge level from cache and memory.
     * TODO: This will not remove level from user data. Probably should do it.
     * @param challengeLevel Level that must be removed.
     */
    public void deleteChallengeLevel(ChallengeLevel challengeLevel)
    {
        if (this.levelCacheData.containsKey(challengeLevel.getUniqueId()))
        {
            this.levelCacheData.remove(challengeLevel.getUniqueId());

            // Remove challenge level from challenges object.
            if (!challengeLevel.getChallenges().isEmpty())
            {
                challengeLevel.getChallenges().forEach(challengeID -> {
                    Challenge challenge = this.getChallenge(challengeID);

                    if (challenge != null)
                    {
                        challenge.setLevel(ChallengesManager.FREE);
                    }
                });
            }

            this.levelDatabase.deleteObject(challengeLevel);

            this.addon.getPlugin().getPlaceholdersManager().
            unregisterPlaceholder("challenges_completed_challenge_count_per_level_" + challengeLevel.getUniqueId());
        }
    }


    /**
     * This method returns if in given world has any stored challenge or level.
     * @param world World that needs to be checked
     * @return <code>true</code> if world has any challenge or level, otherwise <code>false</code>
     */
    public boolean hasAnyChallengeData(@NonNull World world)
    {
        return this.islandWorldManager.getAddon(world).filter(gameMode ->
        this.hasAnyChallengeData(gameMode.getDescription().getName())).isPresent();
    }


    /**
     * This method returns if in given gameMode has any stored challenge or level.
     * @param gameMode GameMode addon name that needs to be checked
     * @return <code>true</code> if gameMode has any challenge or level, otherwise <code>false</code>
     */
    public boolean hasAnyChallengeData(@NonNull String gameMode)
    {
        return this.challengeDatabase.loadObjects().stream().anyMatch(
                challenge -> challenge.matchGameMode(gameMode)) ||
                this.levelDatabase.loadObjects().stream().anyMatch(
                        level -> level.matchGameMode(gameMode));
    }


    /**
     * This method returns number of levels in given world.
     * @param world World where level count must be returned.
     * @return Number of levels in given world.
     */
    public int getLevelCount(World world)
    {
        return this.getLevels(world).size();
    }


    /**
     * This method returns number of completed levels in given world.
     * @param user User which completed level count must be returned.
     * @param world World where level count must be returned.
     * @return Number of completed levels by given user in given world.
     */
    public long getCompletedLevelCount(User user, World world)
    {
        return this.getAllChallengeLevelStatus(user, world).stream().
            filter(LevelStatus::isComplete).
            count();
    }


    /**
     * This method returns number of unlocked levels in given world.
     * @param user User which unlocked level count must be returned.
     * @param world World where level count must be returned.
     * @return Number of unlocked levels by given user in given world.
     */
    public long getUnlockedLevelCount(User user, World world)
    {
        return this.getAllChallengeLevelStatus(user, world).stream().
            filter(LevelStatus::isUnlocked).
            count();
    }
}