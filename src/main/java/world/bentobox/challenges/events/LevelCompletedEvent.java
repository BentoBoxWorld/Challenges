package world.bentobox.challenges.events;


import java.util.UUID;

import world.bentobox.bentobox.api.events.PremadeEvent;


/**
 * This event is fired when challenge level is completed.
 */
public class LevelCompletedEvent extends PremadeEvent
{

	/**
	 * Constructor creates a new LevelCompletedEvent instance.
	 *
	 * @param levelID of type String
	 * @param playerUUID of type UUID
	 * @param admin of type boolean
	 */
	public LevelCompletedEvent(
		String levelID,
		UUID playerUUID,
		boolean admin)
	{
		this.levelID = levelID;
		this.playerUUID = playerUUID;
		this.admin = admin;
	}


// ---------------------------------------------------------------------
// Section: Getters and setters
// ---------------------------------------------------------------------


	/**
	 * This method returns the levelID value.
	 *
	 * @return the value of levelID.
	 */
	public String getLevelID()
	{
		return levelID;
	}


	/**
	 * This method sets the levelID value.
	 *
	 * @param levelID the levelID new value.
	 */
	public void setLevelID(String levelID)
	{
		this.levelID = levelID;
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


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * Completed level ID
	 */
	private String levelID;

	/**
	 * User who completes challenge
	 */
	private UUID playerUUID;

	/**
	 * Indicates if admin completes challenge
	 */
	private boolean admin;
}
