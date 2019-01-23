package world.bentobox.challenges.database.object;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.World;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.util.Util;

/**
 * Stores the player's challenge situation
 * @author tastybento
 *
 */
public class ChallengesPlayerData implements DataObject
{
	/**
	 * Constructor ChallengesPlayerData creates a new ChallengesPlayerData instance.
	 */
	public ChallengesPlayerData()
	{
	}


	/**
	 * Creates a player data entry
	 *
	 * @param uniqueId - the player's UUID in string format
	 */
	public ChallengesPlayerData(String uniqueId)
	{
		this.uniqueId = uniqueId;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * This variable stores each player UUID as string.
	 */
	@Expose
	private String uniqueId = "";

	/**
	 * Challenge map, where key = unique challenge name and Value = number of times
	 * completed
	 */
	@Expose
	private Map<String, Integer> challengeStatus = new HashMap<>();

	/**
	 * Map of challenges completion time where key is challenges unique id and value is
	 * timestamp when challenge was completed last time.
	 */
	@Expose
	private Map<String, Long> challengesTimestamp = new HashMap<>();

	/**
	 * Set of Strings that contains all challenge levels that are completed.
	 */
	@Expose
	private Set<String> levelsDone = new HashSet<>();


// ---------------------------------------------------------------------
// Section: Getters
// ---------------------------------------------------------------------


	/**
	 * @return uniqueID
	 * @see DataObject#getUniqueId()
	 */
	@Override
	public String getUniqueId()
	{
		return uniqueId;
	}


	/**
	 * This method returns the challengeStatus value.
	 * @return the value of challengeStatus.
	 */
	public Map<String, Integer> getChallengeStatus()
	{
		return challengeStatus;
	}


	/**
	 * This method returns the challengesTimestamp value.
	 * @return the value of challengesTimestamp.
	 */
	public Map<String, Long> getChallengesTimestamp()
	{
		return challengesTimestamp;
	}


	/**
	 * This method returns the levelsDone value.
	 * @return the value of levelsDone.
	 */
	public Set<String> getLevelsDone()
	{
		return levelsDone;
	}


// ---------------------------------------------------------------------
// Section: Setters
// ---------------------------------------------------------------------


	/**
	 * @param uniqueId - unique ID the uniqueId to set
	 * @see DataObject#setUniqueId(String)
	 */
	@Override
	public void setUniqueId(String uniqueId)
	{
		this.uniqueId = uniqueId;
	}


	/**
	 * This method sets the challengeStatus value.
	 * @param challengeStatus the challengeStatus new value.
	 *
	 */
	public void setChallengeStatus(Map<String, Integer> challengeStatus)
	{
		this.challengeStatus = challengeStatus;
	}


	/**
	 * This method sets the challengesTimestamp value.
	 * @param challengesTimestamp the challengesTimestamp new value.
	 *
	 */
	public void setChallengesTimestamp(Map<String, Long> challengesTimestamp)
	{
		this.challengesTimestamp = challengesTimestamp;
	}


	/**
	 * This method sets the levelsDone value.
	 * @param levelsDone the levelsDone new value.
	 *
	 */
	public void setLevelsDone(Set<String> levelsDone)
	{
		this.levelsDone = levelsDone;
	}


// ---------------------------------------------------------------------
// Section: Other Methods
// ---------------------------------------------------------------------


	/**
	 * Resets all challenges and levels in world for this player
	 *
	 * @param world world which challenges must be reset.
	 */
	public void reset(World world)
	{
		String worldName = Util.getWorld(world).getName();
		challengeStatus.keySet().removeIf(n -> n.startsWith(worldName));
		challengesTimestamp.keySet().removeIf(n -> n.startsWith(worldName));
		levelsDone.removeIf(n -> n.startsWith(worldName));
	}


	/**
	 * Mark a challenge as having been completed. Will increment the number of times and
	 * timestamp
	 *
	 * @param challengeName - unique challenge name
	 */
	public void setChallengeDone(String challengeName)
	{
		int times = challengeStatus.getOrDefault(challengeName, 0) + 1;
		challengeStatus.put(challengeName, times);
		challengesTimestamp.put(challengeName, System.currentTimeMillis());
	}


	/**
	 * Set the number of times a challenge has been done
	 *
	 * @param challengeName - unique challenge name
	 * @param times - the number of times to set
	 */
	public void setChallengeTimes(String challengeName, int times)
	{
		challengeStatus.put(challengeName, times);
		challengesTimestamp.put(challengeName, System.currentTimeMillis());
	}


	/**
	 * Check if a challenge has been done
	 *
	 * @param challengeName - unique challenge name
	 * @return true if done at least once
	 */
	public boolean isChallengeDone(String challengeName)
	{
		return this.getTimes(challengeName) > 0;
	}


	/**
	 * Check how many times a challenge has been done
	 *
	 * @param challengeName - unique challenge name
	 * @return - number of times
	 */
	public int getTimes(String challengeName)
	{
		return challengeStatus.getOrDefault(challengeName, 0);
	}


	/**
	 * @see Object#hashCode()
	 * @return object hashCode value.
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
		return result;
	}


	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @param obj Other object.
	 * @return boolean that indicate if objects are equals.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof ChallengesPlayerData))
		{
			return false;
		}

		ChallengesPlayerData other = (ChallengesPlayerData) obj;

		if (uniqueId == null)
		{
			return other.uniqueId == null;
		}
		else
		{
			return uniqueId.equals(other.uniqueId);
		}
	}
}