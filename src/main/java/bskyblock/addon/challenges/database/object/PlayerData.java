package bskyblock.addon.challenges.database.object;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.Expose;

import us.tastybento.bskyblock.database.objects.DataObject;

/**
 * Stores the player's challenge situation
 * @author tastybento
 *
 */
public class PlayerData implements DataObject {

	@Expose
	private String uniqueId = "";
	/**
	 * Challenge map, where key = unique challenge name and Value = number of times completed
	 */
	@Expose
	private Map<String, Integer> challengeStatus = new HashMap<>();
	@Expose
	private Map<String, Long> challengesTimestamp = new HashMap<>();
	@Expose
	private Set<String> levelsDone = new HashSet<>();

	// Required for bean instantiation
	public PlayerData() {}

	/**
	 * Mark a challenge as having been completed. Will increment the number of times and timestamp
	 * @param challengeName - unique challenge name
	 */
	public void setChallengeDone(String challengeName) {
		int times = challengeStatus.getOrDefault(challengeName, 0) + 1;
		challengeStatus.put(challengeName, times);
		challengesTimestamp.put(challengeName, System.currentTimeMillis());
	}

	/**
	 * Check if a challenge has been done
	 * @param challengeName - unique challenge name
	 * @return true if done at least once
	 */
	public boolean isChallengeDone(String challengeName) {
		return getTimes(challengeName) > 0 ? true : false;
	}

	/**
	 * Check how many times a challenge has been done
	 * @param challengeName - unique challenge name
	 * @return - number of times
	 */
	public int getTimes(String challengeName) {
		return challengeStatus.getOrDefault(challengeName, 0);
	}

	/**
	 * Creates a player data entry
	 * @param uniqueId - the player's UUID in string format
	 */
	public PlayerData(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	/* (non-Javadoc)
	 * @see us.tastybento.bskyblock.database.objects.DataObject#getUniqueId()
	 */
	@Override
	public String getUniqueId() {
		// TODO Auto-generated method stub
		return uniqueId;
	}

	/* (non-Javadoc)
	 * @see us.tastybento.bskyblock.database.objects.DataObject#setUniqueId(java.lang.String)
	 */
	@Override
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;

	}

	/**
	 * @return the challengeStatus
	 */
	public Map<String, Integer> getChallengeStatus() {
		return challengeStatus;
	}

	/**
	 * @param challengeStatus the challengeStatus to set
	 */
	public void setChallengeStatus(Map<String, Integer> challengeStatus) {
		this.challengeStatus = challengeStatus;
	}

	/**
	 * @return the challengesTimestamp
	 */
	public Map<String, Long> getChallengesTimestamp() {
		return challengesTimestamp;
	}

	/**
	 * @param challengesTimestamp the challengesTimestamp to set
	 */
	public void setChallengesTimestamp(Map<String, Long> challengesTimestamp) {
		this.challengesTimestamp = challengesTimestamp;
	}

	/**
	 * @return the levelsDone
	 */
	public Set<String> getLevelsDone() {
		return levelsDone;
	}

	/**
	 * @param levelsDone the levelsDone to set
	 */
	public void setLevelsDone(Set<String> levelsDone) {
		this.levelsDone = levelsDone;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PlayerData)) {
			return false;
		}
		PlayerData other = (PlayerData) obj;
		if (uniqueId == null) {
			if (other.uniqueId != null) {
				return false;
			}
		} else if (!uniqueId.equals(other.uniqueId)) {
			return false;
		}
		return true;
	}

}