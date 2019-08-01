package world.bentobox.challenges.database.object;


import com.google.gson.annotations.Expose;
import org.eclipse.jdt.annotation.NonNull;
import java.util.*;

import world.bentobox.bentobox.api.logs.LogEntry;
import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.adapters.Adapter;
import world.bentobox.bentobox.database.objects.adapters.LogEntryListAdapter;


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
	private Map<String, Integer> challengeStatus = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	/**
	 * Map of challenges completion time where key is challenges unique id and value is
	 * timestamp when challenge was completed last time.
	 */
	@Expose
	private Map<String, Long> challengesTimestamp = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	/**
	 * Set of Strings that contains all challenge levels that are completed.
	 */
	@Expose
	private Set<String> levelsDone = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

	/**
	 * Stores history about challenge completion.
	 */
	@Adapter(LogEntryListAdapter.class)
	@Expose
	private List<LogEntry> history = new LinkedList<>();


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


	/**
	 * This method returns the history object.
	 * @return the history object.
	 */
	public List<LogEntry> getHistory()
	{
		return history;
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


	/**
	 * This method sets the history object value.
	 * @param history the history object new value.
	 */
	public void setHistory(List<LogEntry> history)
	{
		this.history = history;
	}


// ---------------------------------------------------------------------
// Section: Other Methods
// ---------------------------------------------------------------------


	/**
	 * Resets all challenges and levels in GameMode for this player
	 *
	 * @param gameMode GameMode which challenges must be reset.
	 */
	public void reset(@NonNull String gameMode)
	{
		challengeStatus.keySet().removeIf(n -> n.regionMatches(true, 0, gameMode, 0, gameMode.length()));
		challengesTimestamp.keySet().removeIf(n -> n.regionMatches(true, 0, gameMode, 0, gameMode.length()));
		levelsDone.removeIf(n -> n.regionMatches(true, 0, gameMode, 0, gameMode.length()));
	}


	/**
	 * Mark a challenge as having been completed. Will increment the number of times and
	 * timestamp
	 *
	 * @param challengeName - unique challenge name
	 */
	public void setChallengeDone(@NonNull String challengeName)
	{
		this.addChallengeDone(challengeName, 1);
	}


	/**
	 * Mark a challenge as having been completed. Will increment the number of times and
	 * timestamp
	 *
	 * @param challengeName - unique challenge name
	 * @param times - how many new times should be added
	 */
	public void addChallengeDone(@NonNull String challengeName, int times)
	{
		int newTimes = challengeStatus.getOrDefault(challengeName, 0) + times;
		challengeStatus.put(challengeName, newTimes);
		challengesTimestamp.put(challengeName, System.currentTimeMillis());
	}


	/**
	 * Set the number of times a challenge has been done
	 *
	 * @param challengeName - unique challenge name
	 * @param times - the number of times to set
	 */
	public void setChallengeTimes(@NonNull String challengeName, @NonNull int times)
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
	public boolean isChallengeDone(@NonNull String challengeName)
	{
		return this.getTimes(challengeName) > 0;
	}


	/**
	 * Check how many times a challenge has been done
	 *
	 * @param challengeName - unique challenge name
	 * @return - number of times
	 */
	public int getTimes(@NonNull String challengeName)
	{
		return challengeStatus.getOrDefault(challengeName, 0);
	}


	/**
	 * This method adds given level id to completed level set.
	 * @param uniqueId from ChallengeLevel object.
	 */
	public void addCompletedLevel(@NonNull String uniqueId)
	{
		this.levelsDone.add(uniqueId);
	}


	/**
	 * This method returns if given level is done.
	 * @param uniqueId  of ChallengeLevel object.
	 * @return <code>true</code> if level is completed, otherwise <code>false</code>
	 */
	public boolean isLevelDone(@NonNull String uniqueId)
	{
		return !this.levelsDone.isEmpty() && this.levelsDone.contains(uniqueId);
	}


	/**
	 * This method adds given LogEntry to history.
	 *
	 * @param entry of type LogEntry
	 */
	public void addHistoryRecord(@NonNull LogEntry entry)
	{
		this.history.add(entry);
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
			return uniqueId.equalsIgnoreCase(other.uniqueId);
		}
	}
}