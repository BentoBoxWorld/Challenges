package world.bentobox.challenges;


import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.database.object.ChallengesPlayerData;
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
	private Map<UUID, ChallengesPlayerData> playerCacheData;

	/**
	 * This variable allows to access ChallengesAddon.
	 */
	private ChallengesAddon addon;


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
		this.playerCacheData.clear();

		this.addon.getLogger().info("Loading challenges...");

		this.challengeDatabase.loadObjects().forEach(this::loadChallenge);
		this.levelDatabase.loadObjects().forEach(this::loadLevel);
		this.playersDatabase.loadObjects().forEach(this::loadPlayerData);
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
					user.sendMessage("challenges.admin.import.skip",
						"[object]", challenge.getFriendlyName());
				}

				return false;
			}
			else
			{
				if (!silent)
				{
					user.sendMessage("challenges.admin.import.overwriting",
						"[object]", challenge.getFriendlyName());
				}
			}
		}
		else
		{
			if (!silent)
			{
				user.sendMessage("challenges.admin.import.add",
					"[object]", challenge.getFriendlyName());
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
			user.sendMessage("challenges.admin.import.error",
				"[object]", level.getFriendlyName());

			return false;
		}

		if (this.levelCacheData.containsKey(level.getUniqueId()))
		{
			if (!overwrite)
			{
				if (!silent)
				{
					user.sendMessage("challenges.admin.import.skip",
						"[object]", level.getFriendlyName());
				}

				return false;
			}
			else
			{
				if (!silent)
				{
					user.sendMessage("challenges.admin.import.overwriting",
						"[object]", level.getFriendlyName());
				}
			}
		}
		else
		{
			if (!silent)
			{
				user.sendMessage("challenges.admin.import.add",
					"[object]", level.getFriendlyName());
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
			UUID uuid = UUID.fromString(playerData.getUniqueId());
			this.playerCacheData.put(uuid, playerData);
		}
		catch (Exception e)
		{
			this.addon.getLogger().severe("UUID for player in challenge data file is invalid!");
		}
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
	 * Load player from database into the cache or create new player data
	 *
	 * @param user - user to add
	 */
	private void addPlayer(User user)
	{
		if (this.playerCacheData.containsKey(user.getUniqueId()))
		{
			return;
		}

		// The player is not in the cache
		// Check if the player exists in the database

		if (this.playersDatabase.objectExists(user.getUniqueId().toString()))
		{
			// Load player from database
			ChallengesPlayerData data = this.playersDatabase.loadObject(user.getUniqueId().toString());
			// Store in cache
			this.playerCacheData.put(user.getUniqueId(), data);
		}
		else
		{
			// Create the player data
			ChallengesPlayerData pd = new ChallengesPlayerData(user.getUniqueId().toString());
			this.playersDatabase.saveObject(pd);
			// Add to cache
			this.playerCacheData.put(user.getUniqueId(), pd);
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
		this.savePlayers();
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
	private void saveChallenge(Challenge challenge)
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
	private void saveLevel(ChallengeLevel level)
	{
		this.levelDatabase.saveObject(level);
	}


	/**
	 * This method saves all players to database.
	 */
	private void savePlayers()
	{
		this.playerCacheData.values().forEach(this.playersDatabase::saveObject);
	}


	/**
	 * This method saves player with given UUID.
	 * @param playerUUID users UUID.
	 */
	private void savePlayer(UUID playerUUID)
	{
		if (this.playerCacheData.containsKey(playerUUID))
		{
			this.playersDatabase.saveObject(this.playerCacheData.get(playerUUID));
		}
	}


// ---------------------------------------------------------------------
// Section: Player Data related methods
// ---------------------------------------------------------------------


	/**
	 * This method returns all players who have done at least one challenge in given world.
	 * @param world World in which must search challenges.
	 * @return List with players who have done at least on challenge.
	 */
	public List<Player> getPlayers(World world)
	{
		List<String> allChallengeList = this.getAllChallengesNames(world);

		// This is using Database, as some users may not be in the cache.

		return this.playersDatabase.loadObjects().stream().filter(playerData ->
			allChallengeList.stream().anyMatch(playerData::isChallengeDone)).
			map(playerData -> Bukkit.getPlayer(UUID.fromString(playerData.getUniqueId()))).
			collect(Collectors.toList());
	}


	/**
	 * This method returns how many times a player has done a challenge before
	 * @param user - user
	 * @param challenge - challenge
	 * @return - number of times
	 */
	public long getChallengeTimes(User user, Challenge challenge)
	{
		this.addPlayer(user);
		return this.playerCacheData.get(user.getUniqueId()).getTimes(challenge.getUniqueId());
	}


	/**
	 * Checks if a challenge is complete or not
	 *
	 * @param user - User who must be checked.
	 * @param challenge - Challenge
	 * @return - true if completed
	 */
	public boolean isChallengeComplete(User user, Challenge challenge)
	{
		this.addPlayer(user);
		return this.playerCacheData.get(user.getUniqueId()).isChallengeDone(challenge.getUniqueId());
	}


	/**
	 * Get the status on every level
	 *
	 * @param user - user
	 * @param world - world
	 * @return Level status - how many challenges still to do on which level
	 */
	public List<LevelStatus> getChallengeLevelStatus(User user, World world)
	{
		this.addPlayer(user);
		ChallengesPlayerData playerData = this.playerCacheData.get(user.getUniqueId());

		List<ChallengeLevel> challengeLevelList = this.getLevels(world);

		List<LevelStatus> result = new ArrayList<>();

		// The first level is always unlocked and previous for it is null.
		ChallengeLevel previousLevel = null;
		int doneChallengeCount = 0;

		// For each challenge level, check how many the user has done
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
	 * Check is user can see given level.
	 *
	 * @param user - user
	 * @param world - world
	 * @param level - level
	 * @return true if level is unlocked
	 */
	public boolean isLevelUnlocked(User user, World world, ChallengeLevel level)
	{
		this.addPlayer(user);

		return this.getChallengeLevelStatus(user, world).stream().
			filter(LevelStatus::isUnlocked).
			anyMatch(lv -> lv.getLevel().equals(level));
	}


	/**
	 * Sets the challenge as complete and increments the number of times it has been
	 * completed
	 *
	 * @param user - user
	 * @param challenge - challenge
	 */
	public void setChallengeComplete(User user, Challenge challenge)
	{
		this.addPlayer(user);
		this.playerCacheData.get(user.getUniqueId()).setChallengeDone(challenge.getUniqueId());
		// Save
		this.savePlayer(user.getUniqueId());
	}


	/**
	 * Reset the challenge to zero time / not done
	 *
	 * @param user - user
	 * @param challenge - challenge
	 */
	public void resetChallenge(User user, Challenge challenge)
	{
		this.addPlayer(user);
		this.playerCacheData.get(user.getUniqueId()).setChallengeTimes(challenge.getUniqueId(), 0);
		// Save
		this.savePlayer(user.getUniqueId());
	}



	/**
	 * Resets all the challenges for user in world
	 *
	 * @param user - island owner's UUID
	 * @param world - world
	 */
	public void resetAllChallenges(User user, World world)
	{
		this.addPlayer(user);
		this.playerCacheData.get(user.getUniqueId()).reset(world);
		// Save
		this.savePlayer(user.getUniqueId());
	}


	/**
	 * This method returns if given user has been already completed given level.
	 * @param level Level that must be checked.
	 * @param user User who need to be checked.
	 * @return true, if level is already completed.
	 */
	public boolean isLevelCompleted(User user, ChallengeLevel level)
	{
		this.addPlayer(user);
		return this.playerCacheData.get(user.getUniqueId()).isLevelDone(level.getUniqueId());
	}


	/**
	 * This method checks all level challenges and checks if all challenges are done.
	 * @param level Level that must be checked.
	 * @param user User who need to be checked.
	 * @return true, if all challenges are done, otherwise false.
	 */
	public boolean validateLevelCompletion(User user, ChallengeLevel level)
	{
		this.addPlayer(user);
		ChallengesPlayerData playerData = this.playerCacheData.get(user.getUniqueId());
		long doneChallengeCount = level.getChallenges().stream().filter(playerData::isChallengeDone).count();

		return level.getChallenges().size() == doneChallengeCount;
	}


	/**
	 * This method sets given level as completed.
	 * @param level Level that must be completed.
	 * @param user User who complete level.
	 */
	public void setLevelComplete(User user, ChallengeLevel level)
	{
		this.addPlayer(user);
		this.playerCacheData.get(user.getUniqueId()).addCompletedLevel(level.getUniqueId());
		// Save
		this.savePlayer(user.getUniqueId());
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
	public List<String> getAllChallengesNames(World world)
	{
		String worldName = Util.getWorld(world).getName();
		// TODO: Probably need to check also database.
		return this.challengeCacheData.values().stream().
			sorted(Comparator.comparing(Challenge::getOrder)).
			filter(challenge -> challenge.getUniqueId().startsWith(worldName)).
			map(Challenge::getUniqueId).
			collect(Collectors.toList());
	}


	/**
	 * Get the list of all challenge for world.
	 *
	 * @param world - the world to check
	 * @return List of challenges
	 */
	public List<Challenge> getAllChallenges(World world)
	{
		String worldName = Util.getWorld(world).getName();
		// TODO: Probably need to check also database.
		return this.challengeCacheData.values().stream().
			sorted(Comparator.comparing(Challenge::getOrder)).
			filter(challenge -> challenge.getUniqueId().startsWith(worldName)).
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
	public List<ChallengeLevel> getLevels(World world)
	{
		String worldName = Util.getWorld(world).getName();
		// TODO: Probably need to check also database.
		return this.levelCacheData.values().stream().
			sorted(ChallengeLevel::compareTo).
			filter(challenge -> challenge.getUniqueId().startsWith(worldName)).
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
	public ChallengeLevel createLevel(String uniqueID)
	{
		if (!this.containsLevel(uniqueID))
		{
			ChallengeLevel level = new ChallengeLevel();
			level.setUniqueId(uniqueID);

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
}