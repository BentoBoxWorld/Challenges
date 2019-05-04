package world.bentobox.challenges;


import org.eclipse.jdt.annotation.NonNull;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;

import world.bentobox.bentobox.api.logs.LogEntry;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.database.object.ChallengesPlayerData;
import world.bentobox.challenges.events.ChallengeCompletedEvent;
import world.bentobox.challenges.events.ChallengeResetAllEvent;
import world.bentobox.challenges.events.ChallengeResetEvent;
import world.bentobox.challenges.events.LevelCompletedEvent;
import world.bentobox.challenges.utils.LevelStatus;


/**
 * This class manges challenges. It allows access to all data that is stored to database.
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


    // ---------------------------------------------------------------------
    // Section: Constants
    // ---------------------------------------------------------------------


    /**
     * String for free Challenge Level.
     */
    public static final String FREE = "";


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
        this.settings = addon.getChallengesSettings();

        // Set up the configs
        this.challengeDatabase = new Database<>(addon, Challenge.class);
        this.levelDatabase = new Database<>(addon, ChallengeLevel.class);
        // Players is where all the player history will be stored
        this.playersDatabase = new Database<>(addon, ChallengesPlayerData.class);

        // Init all cache objects.
        this.challengeCacheData = new HashMap<>();
        this.levelCacheData = new HashMap<>();
        this.playerCacheData = new HashMap<>();

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
        this.challengeCacheData.clear();
        this.levelCacheData.clear();

        if (!this.playerCacheData.isEmpty())
        {
            // store player data before cleaning.
            this.savePlayersData();
        }

        this.playerCacheData.clear();

        this.addon.getLogger().info("Loading challenges...");

        this.challengeDatabase.loadObjects().forEach(this::loadChallenge);
        this.levelDatabase.loadObjects().forEach(this::loadLevel);
        // It is not necessary to load all players in memory.
//        this.playersDatabase.loadObjects().forEach(this::loadPlayerData);
    }


    /**
     * Reload database. This method keeps cache memory.
     */
    public void reload()
    {
        if (!this.playerCacheData.isEmpty())
        {
            // store player data before cleaning.
            this.savePlayersData();
        }

        this.addon.getLogger().info("Reloading challenges...");

        this.challengeDatabase = new Database<>(addon, Challenge.class);
        this.levelDatabase = new Database<>(addon, ChallengeLevel.class);
        this.playersDatabase = new Database<>(addon, ChallengesPlayerData.class);

        this.challengeDatabase.loadObjects().forEach(this::loadChallenge);
        this.levelDatabase.loadObjects().forEach(this::loadLevel);
        // It is not necessary to load all players in memory.
//        this.playersDatabase.loadObjects().forEach(this::loadPlayerData);
    }


    /**
     * Load challenge silently. Used when loading.
     *
     * @param challenge Challenge that must be loaded.
     * @return true if successful
     */
    private void loadChallenge(Challenge challenge)
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
    public boolean loadChallenge(Challenge challenge,
            boolean overwrite,
            User user,
            boolean silent)
    {
        if (this.challengeCacheData.containsKey(challenge.getUniqueId()))
        {
            if (!overwrite)
            {
                if (!silent)
                {
                    user.sendMessage("challenges.messages.load-skipping",
                            "[value]", challenge.getFriendlyName());
                }

                return false;
            }
            else
            {
                if (!silent)
                {
                    user.sendMessage("challenges.messages.load-overwriting",
                            "[value]", challenge.getFriendlyName());
                }
            }
        }
        else
        {
            if (!silent)
            {
                user.sendMessage("challenges.messages.load-add",
                        "[value]", challenge.getFriendlyName());
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
    private void loadLevel(ChallengeLevel level)
    {
        this.loadLevel(level, true, null, true);
    }


    /**
     * This method loads given level into local cache. It provides functionality to
     * overwrite local value with new one, and send message to given user.
     * @param level of type ChallengeLevel that must be loaded in local cache.
     * @param overwrite of type boolean that indicate if local element must be overwritten.
     * @param user of type User who will receive messages.
     * @param silent of type boolean that indicate if message to user must be sent.
     * @return boolean that indicate about load status.
     */
    public boolean loadLevel(ChallengeLevel level, boolean overwrite, User user, boolean silent)
    {
        if (!this.isValidLevel(level))
        {
            if (user != null)
            {
                user.sendMessage("challenges.errors.load-error",
                    "[value]", level.getFriendlyName());
            }
            else
            {
                this.addon.logError("Challenge Level '" + level.getUniqueId() + "' is not valid and skipped");
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
                            "[value]", level.getFriendlyName());
                }

                return false;
            }
            else
            {
                if (!silent)
                {
                    user.sendMessage("challenges.messages.load-overwriting",
                            "[value]", level.getFriendlyName());
                }
            }
        }
        else
        {
            if (!silent)
            {
                user.sendMessage("challenges.messages.load-add",
                        "[value]", level.getFriendlyName());
            }
        }

        this.levelCacheData.put(level.getUniqueId(), level);
        return true;
    }


    /**
     * This method stores PlayerData into local cache.
     * @param playerData ChallengesPlayerData that must be loaded.
     */
    private void loadPlayerData(ChallengesPlayerData playerData)
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
     * @param playerID player ID which cache data must be removed.
     */
    public void removeFromCache(UUID playerID)
    {
        if (!this.settings.isStoreAsIslandData())
        {
            if (this.playerCacheData.containsKey(playerID.toString()))
            {
                // save before remove
                this.savePlayerData(playerID.toString());
                this.playerCacheData.remove(playerID.toString());
            }
        }

        // TODO: It would be necessary to remove also data, if they stores islands.
        // Unfortunately, I do not know all worlds. Checking everything would be bad. Probably, I could
        // add extra map that links players with their cached island data?
    }


    // ---------------------------------------------------------------------
    // Section: Other storing related methods
    // ---------------------------------------------------------------------


    /**
     * This method checks if given level all challenges exists in local cache or database.
     * It also checks if world where level must operate exists.
     * @param level that must be validated
     * @return true ir level is valid, otherwise false.
     */
    private boolean isValidLevel(ChallengeLevel level)
    {
        if (!this.addon.getPlugin().getIWM().inWorld(Bukkit.getWorld(level.getWorld())))
        {
            return false;
        }

        for (String uniqueID : level.getChallenges())
        {
            if (!this.challengeCacheData.containsKey(uniqueID))
            {
                if (this.challengeDatabase.objectExists(uniqueID))
                {
                    this.loadChallenge(this.challengeDatabase.loadObject(uniqueID));
                }
                else
                {
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
            this.playerCacheData.put(uniqueID, data);
        }
        else
        {
            // Create the player data
            ChallengesPlayerData pd = new ChallengesPlayerData(uniqueID);
            this.playersDatabase.saveObject(pd);
            // Add to cache
            this.playerCacheData.put(uniqueID, pd);
        }
    }


    // ---------------------------------------------------------------------
    // Section: Saving methods
    // ---------------------------------------------------------------------


    /**
     * This method init all cached object saving to database.
     */
    public void save()
    {
        this.saveChallenges();
        this.saveLevels();
        this.savePlayersData();
    }


    /**
     * This method saves all challenges to database.
     */
    private void saveChallenges()
    {
        this.challengeCacheData.values().forEach(this.challengeDatabase::saveObject);
    }


    /**
     * This method saves given challenge object to database.
     * @param challenge object that must be saved
     */
    public void saveChallenge(Challenge challenge)
    {
        this.challengeDatabase.saveObject(challenge);
    }


    /**
     * This method saves all levels to database.
     */
    private void saveLevels()
    {
        this.levelCacheData.values().forEach(this.levelDatabase::saveObject);
    }


    /**
     * This method saves given level into database.
     * @param level object that must be saved
     */
    public void saveLevel(ChallengeLevel level)
    {
        this.levelDatabase.saveObject(level);
    }


    /**
     * This method saves all players/islands to database.
     */
    private void savePlayersData()
    {
        this.playerCacheData.values().forEach(this.playersDatabase::saveObject);
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

            this.playersDatabase.saveObject(cachedData);
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
        this.addPlayerData(storageDataID);
        this.playerCacheData.get(storageDataID).setChallengeDone(challengeID);
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
     * Resets all the challenges for user in world
     *
     * @param storageDataID - island owner's UUID
     * @param worldName - world
     */
    private void resetAllChallenges(@NonNull String storageDataID, @NonNull String worldName)
    {
        this.addPlayerData(storageDataID);
        this.playerCacheData.get(storageDataID).reset(worldName);
        // Save
        this.savePlayerData(storageDataID);
    }


    /**
     * Get the status on every level for required world and playerData
     *
     * @param storageDataID - playerData ID
     * @param worldName - World Name where levels should be searched.
     * @return Level status - how many challenges still to do on which level
     */
    private List<LevelStatus> getAllChallengeLevelStatus(String storageDataID, String worldName)
    {
        this.addPlayerData(storageDataID);
        ChallengesPlayerData playerData = this.playerCacheData.get(storageDataID);

        List<ChallengeLevel> challengeLevelList = this.getLevels(worldName);

        List<LevelStatus> result = new ArrayList<>();

        // The first level is always unlocked and previous for it is null.
        ChallengeLevel previousLevel = null;
        int doneChallengeCount = 0;

        // For each challenge level, check how many the storageDataID has done
        for (ChallengeLevel level : challengeLevelList)
        {
            // To find how many challenges user still must do in previous level, we must
            // know how many challenges there were and how many has been done. Then
            // remove waiver amount to get count of challenges that still necessary to do.

            int challengesToDo = previousLevel == null ? 0 :
                (previousLevel.getChallenges().size() - doneChallengeCount - level.getWaiverAmount());

            // As level already contains unique ids of challenges, just iterate through them.
            doneChallengeCount = (int) level.getChallenges().stream().filter(playerData::isChallengeDone).count();

            result.add(new LevelStatus(
                    level,
                    previousLevel,
                    challengesToDo,
                    level.getChallenges().size() == doneChallengeCount,
                    challengesToDo <= 0));

            previousLevel = level;
        }

        return result;
    }


    /**
     * This method returns LevelStatus object for given challenge level.
     * @param storageDataID User which level status must be acquired.
     * @param level Level which status must be calculated.
     * @return LevelStatus of given level.
     */
    private LevelStatus getChallengeLevelStatus(@NonNull String storageDataID, @NonNull ChallengeLevel level)
    {
        List<LevelStatus> statusList = this.getAllChallengeLevelStatus(storageDataID, level.getWorld());

        for (LevelStatus status : statusList)
        {
            if (status.getLevel().equals(level))
            {
                return status;
            }
        }

        return null;
    }


    /**
     * Check is playerData can see given level.
     * TODO: not an optimal way. Faster would be to check previous level challenges.
     * @param storageDataID - playerData ID
     * @param level - level
     * @return true if level is unlocked
     */
    private boolean isLevelUnlocked(@NonNull String storageDataID, ChallengeLevel level)
    {
        this.addPlayerData(storageDataID);

        return this.getAllChallengeLevelStatus(storageDataID, level.getWorld()).stream().
            filter(LevelStatus::isUnlocked).
            anyMatch(lv -> lv.getLevel().equals(level));
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
    public void setChallengeComplete(User user, World world, Challenge challenge)
    {
        this.setChallengeComplete(user.getUniqueId(), world, challenge);
    }


    /**
     * This method sets given challenge as completed.
     * @param userID - Targeted user.
     * @param world - World where completion must be called.
     * @param challenge - That must be completed.
     */
    public void setChallengeComplete(UUID userID, World world, Challenge challenge)
    {
        String storageID = this.getDataUniqueID(userID, Util.getWorld(world));
        this.setChallengeComplete(storageID, challenge.getUniqueId());
        this.addLogEntry(storageID, new LogEntry.Builder("COMPLETE").
            data("user-id", userID.toString()).
            data("challenge-id", challenge.getUniqueId()).
            build());

        // Fire event that user completes challenge
        Bukkit.getServer().getPluginManager().callEvent(
            new ChallengeCompletedEvent(challenge.getUniqueId(),
                userID,
                false,
                1));
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
            data("user-id", userID.toString()).
            data("challenge-id", challenge.getUniqueId()).
            data("admin-id", adminID == null ? "OP" : adminID.toString()).
            build());

        // Fire event that admin completes user challenge
        Bukkit.getServer().getPluginManager().callEvent(
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
        this.addLogEntry(storageID, new LogEntry.Builder("RESET").
            data("user-id", userID.toString()).
            data("challenge-id", challenge.getUniqueId()).
            data("admin-id", adminID == null ? "RESET" : adminID.toString()).
            build());

        // Fire event that admin resets user challenge
        Bukkit.getServer().getPluginManager().callEvent(
            new ChallengeResetEvent(challenge.getUniqueId(),
                userID,
                true,
                "RESET"));
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
    public void resetAllChallenges(UUID userID, World world, UUID adminID)
    {
        world = Util.getWorld(world);
        String storageID = this.getDataUniqueID(userID, world);

        this.resetAllChallenges(storageID, world.getName());
        this.addLogEntry(storageID, new LogEntry.Builder("RESET_ALL").
            data("user-id", userID.toString()).
            data("admin-id", adminID == null ? "ISLAND_RESET" : adminID.toString()).
            build());

        // Fire event that admin resets user challenge
        Bukkit.getServer().getPluginManager().callEvent(
            new ChallengeResetAllEvent(world.getName(),
                userID,
                adminID != null,
                adminID == null ? "ISLAND_RESET" : "RESET_ALL"));
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
        world = Util.getWorld(world);
        return this.getChallengeTimes(this.getDataUniqueID(user, world), challenge.getUniqueId());
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
     * @return true, if level is already completed.
     */
    public boolean isLevelUnlocked(User user, World world, ChallengeLevel level)
    {
        return this.isLevelUnlocked(this.getDataUniqueID(user, Util.getWorld(world)), level);
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
            data("user-id", user.getUniqueId().toString()).
            data("level", level.getUniqueId()).build());

        // Fire event that user completes level
        Bukkit.getServer().getPluginManager().callEvent(
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
     * @param world World where level must be validated.
     * @param level Level that must be validated.
     * @param user User who need to be validated.
     * @return LevelStatus of given level.
     */
    public LevelStatus getChallengeLevelStatus(User user, World world, ChallengeLevel level)
    {
        return this.getChallengeLevelStatus(this.getDataUniqueID(user, Util.getWorld(world)), level);
    }


    /**
     * This method returns LevelStatus object for given challenge level.
     * @param world World where level must be validated.
     * @param level Level that must be validated.
     * @param user User who need to be validated.
     * @return LevelStatus of given level.
     */
    public LevelStatus getChallengeLevelStatus(UUID user, World world, ChallengeLevel level)
    {
        return this.getChallengeLevelStatus(this.getDataUniqueID(user, Util.getWorld(world)), level);
    }


    /**
     * Get the status on every level for required world and user
     *
     * @param user - user which levels should be checked
     * @param world - World where levels should be searched.
     * @return Level status - how many challenges still to do on which level
     */
    public List<LevelStatus> getAllChallengeLevelStatus(User user, World world)
    {
        world = Util.getWorld(world);
        return this.getAllChallengeLevelStatus(this.getDataUniqueID(user, world), world.getName());
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
        World gameWorld = Util.getWorld(world);

        if (gameWorld == null)
        {
            return Collections.emptyList();
        }

        // TODO: Probably need to check also database.
        return this.challengeCacheData.values().stream().
                sorted(Comparator.comparing(Challenge::getOrder)).
                filter(challenge -> challenge.getUniqueId().startsWith(gameWorld.getName())).
                map(Challenge::getUniqueId).
                collect(Collectors.toList());
    }


    /**
     * Get the list of all challenge for world.
     *
     * @param world - the world to check
     * @return List of challenges
     */
    public List<Challenge> getAllChallenges(@NonNull World world)
    {
        World gameWorld = Util.getWorld(world);

        if (gameWorld == null)
        {
            return Collections.emptyList();
        }

        // TODO: Probably need to check also database.
        return this.challengeCacheData.values().stream().
                filter(challenge -> challenge.getUniqueId().startsWith(gameWorld.getName())).
                sorted(Comparator.comparing(Challenge::getOrder)).
                collect(Collectors.toList());
    }


    /**
     * Free challenges... Challenges without a level.
     * @param world World in which challenges must be searched.
     * @return List with free challenges in given world.
     */
    public List<Challenge> getFreeChallenges(World world)
    {
        // Free Challenges hides under FREE level.
        return this.getAllChallenges(world).stream().
                filter(challenge -> challenge.getLevel().equals(FREE)).
                sorted(Comparator.comparing(Challenge::getOrder)).
                collect(Collectors.toList());
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
                this.challengeCacheData.put(name, challenge);
                return challenge;
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
        if (this.challengeCacheData.containsKey(name))
        {
            return true;
        }
        else
        {
            // check database.
            if (this.challengeDatabase.objectExists(name))
            {
                Challenge challenge = this.challengeDatabase.loadObject(name);
                this.challengeCacheData.put(name, challenge);
                return true;
            }
        }

        return false;
    }


    /**
     * This method creates and returns new challenge with given uniqueID.
     * @param uniqueID - new ID for challenge.
     * @return Challenge that is currently created.
     */
    public Challenge createChallenge(String uniqueID)
    {
        if (!this.containsChallenge(uniqueID))
        {
            Challenge challenge = new Challenge();
            challenge.setUniqueId(uniqueID);

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
            this.challengeCacheData.remove(challenge.getUniqueId());
            this.challengeDatabase.deleteObject(challenge);
        }
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
        world = Util.getWorld(world);

        if (world == null)
        {
            return Collections.emptyList();
        }

        return this.getLevels(world.getName());
    }


    /**
     * This method returns list of challenge levels in given world.
     * @param world for which levels must be searched.
     * @return List with challenges in given world.
     */
    public List<ChallengeLevel> getLevels(String world)
    {
        // TODO: Probably need to check also database.
        return this.levelCacheData.values().stream().
                sorted(ChallengeLevel::compareTo).
                filter(challenge -> challenge.getUniqueId().startsWith(world)).
                collect(Collectors.toList());
    }


    /**
     * Get challenge level by its challenge.
     *
     * @param challenge - challenge which level must be returned.
     * @return - challenge level or null if it does not exist
     */
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
                this.levelCacheData.put(name, level);
                return level;
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
                this.levelCacheData.put(name, level);
                return true;
            }
        }

        return false;
    }


    /**
     * This method adds given challenge to given challenge level.
     * @param newChallenge Challenge who must change owner.
     * @param newLevel Level who must add new challenge
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

            if (!oldLevel.equals(newLevel))
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
            this.levelDatabase.deleteObject(challengeLevel);
        }
    }


    /**
     * This method returns if in given world has any stored challenge or level.
     * @param world World that needs to be checked
     * @return <code>true</code> if world has any challenge or level, otherwise <code>false</code>
     */
    public boolean hasAnyChallengeData(@NonNull World world)
    {
        world = Util.getWorld(world);

        if (world == null)
        {
            return false;
        }

        return this.hasAnyChallengeData(world.getName());
    }


    /**
     * This method returns if in given world has any stored challenge or level.
     * @param worldName World name that needs to be checked
     * @return <code>true</code> if world has any challenge or level, otherwise <code>false</code>
     */
    public boolean hasAnyChallengeData(@NonNull String worldName)
    {
        return this.challengeDatabase.loadObjects().stream().anyMatch(
            challenge -> challenge.getUniqueId().startsWith(worldName)) ||
            this.levelDatabase.loadObjects().stream().anyMatch(
                level -> level.getUniqueId().startsWith(worldName));
    }
}