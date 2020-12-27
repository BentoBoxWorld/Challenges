package world.bentobox.challenges.events;



import org.bukkit.event.HandlerList;
import java.util.UUID;

import world.bentobox.bentobox.api.events.BentoBoxEvent;


/**
 * This Event is fired when challenge is completed.
 */
public class ChallengeCompletedEvent extends BentoBoxEvent
{

	/**
	 * Constructor creates a new ChallengeCompletedEvent instance.
	 *
	 * @param challengeID of type String
	 * @param playerUUID of type UUID
	 * @param admin of type boolean
	 * @param completionCount of type int
	 */
	public ChallengeCompletedEvent(
		String challengeID,
		UUID playerUUID,
		boolean admin,
		int completionCount)
	{
		this.challengeID = challengeID;
		this.playerUUID = playerUUID;
		this.admin = admin;
		this.completionCount = completionCount;
	}


// ---------------------------------------------------------------------
// Section: Getters and setters
// ---------------------------------------------------------------------


	/**
	 * This method returns the challengeID value.
	 *
	 * @return the value of challengeID.
	 */
	public String getChallengeID()
	{
		return challengeID;
	}


	/**
	 * This method sets the challengeID value.
	 *
	 * @param challengeID the challengeID new value.
	 */
	public void setChallengeID(String challengeID)
	{
		this.challengeID = challengeID;
	}


	/**
	 * This method returns the playerUUID value.
	 *
	 * @return the value of playerUUID.
	 */
	public UUID getPlayerUUID()
	{
		return playerUUID;
	}


	/**
	 * This method sets the playerUUID value.
	 *
	 * @param playerUUID the playerUUID new value.
	 */
	public void setPlayerUUID(UUID playerUUID)
	{
		this.playerUUID = playerUUID;
	}


	/**
	 * This method returns the admin value.
	 *
	 * @return the value of admin.
	 */
	public boolean isAdmin()
	{
		return admin;
	}


	/**
	 * This method sets the admin value.
	 *
	 * @param admin the admin new value.
	 */
	public void setAdmin(boolean admin)
	{
		this.admin = admin;
	}


	/**
	 * This method returns the completionCount value.
	 *
	 * @return the value of completionCount.
	 */
	public int getCompletionCount()
	{
		return completionCount;
	}


	/**
	 * This method sets the completionCount value.
	 *
	 * @param completionCount the completionCount new value.
	 */
	public void setCompletionCount(int completionCount)
	{
		this.completionCount = completionCount;
	}


// ---------------------------------------------------------------------
// Section: Handler methods
// ---------------------------------------------------------------------


	/**
	 * Gets handlers.
	 *
	 * @return the handlers
	 */
	@Override
	public HandlerList getHandlers()
	{
		return ChallengeCompletedEvent.handlers;
	}


	/**
	 * Gets handlers.
	 *
	 * @return the handlers
	 */
	public static HandlerList getHandlerList()
	{
		return ChallengeCompletedEvent.handlers;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * Completed challenge ID
	 */
	private String challengeID;

	/**
	 * User who completes challenge
	 */
	private UUID playerUUID;

	/**
	 * Indicates if admin completes challenge
	 */
	private boolean admin;

	/**
	 * Count of completions
	 */
	private int completionCount;

	/**
	 * Event listener list for current
	 */
	private static final HandlerList handlers = new HandlerList();
}
