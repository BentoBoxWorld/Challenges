package world.bentobox.challenges.events;


import java.util.UUID;

import org.bukkit.event.HandlerList;

import world.bentobox.bentobox.api.events.BentoBoxEvent;


/**
 * This event is fired when single challenge is reset by admin.
 */
public class ChallengeResetEvent extends BentoBoxEvent
{
	/**
	 * Constructor creates a new ChallengeResetEvent instance.
	 *
	 * @param challengeID of type String
	 * @param playerUUID of type UUID
	 * @param admin of type boolean
	 * @param reason of type String
	 */
	public ChallengeResetEvent(
		String challengeID,
		UUID playerUUID,
		boolean admin,
		String reason)
	{
		this.challengeID = challengeID;
		this.playerUUID = playerUUID;
		this.admin = admin;
		this.reason = reason;
	}



// ---------------------------------------------------------------------
// Section: Getters and setters
// ---------------------------------------------------------------------


	/**
	 * This method returns the challengeID value.
	 * @return the value of challengeID.
	 */
	public String getChallengeID()
	{
		return challengeID;
	}


	/**
	 * This method sets the challengeID value.
	 * @param challengeID the challengeID new value.
	 *
	 */
	public void setChallengeID(String challengeID)
	{
		this.challengeID = challengeID;
	}


	/**
	 * This method returns the playerUUID value.
	 * @return the value of playerUUID.
	 */
	public UUID getPlayerUUID()
	{
		return playerUUID;
	}


	/**
	 * This method sets the playerUUID value.
	 * @param playerUUID the playerUUID new value.
	 *
	 */
	public void setPlayerUUID(UUID playerUUID)
	{
		this.playerUUID = playerUUID;
	}


	/**
	 * This method returns the admin value.
	 * @return the value of admin.
	 */
	public boolean isAdmin()
	{
		return admin;
	}


	/**
	 * This method sets the admin value.
	 * @param admin the admin new value.
	 *
	 */
	public void setAdmin(boolean admin)
	{
		this.admin = admin;
	}


	/**
	 * This method returns the reason value.
	 * @return the value of reason.
	 */
	public String getReason()
	{
		return reason;
	}


	/**
	 * This method sets the reason value.
	 * @param reason the reason new value.
	 *
	 */
	public void setReason(String reason)
	{
		this.reason = reason;
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
		return ChallengeResetEvent.handlers;
	}


	/**
	 * Gets handlers.
	 *
	 * @return the handlers
	 */
	public static HandlerList getHandlerList()
	{
		return ChallengeResetEvent.handlers;
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
	 * Reset Reason
	 */
	private String reason;

	/**
	 * Event listener list for current
	 */
	private static final HandlerList handlers = new HandlerList();
}
