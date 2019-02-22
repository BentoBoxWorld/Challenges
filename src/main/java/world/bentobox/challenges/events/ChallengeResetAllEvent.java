package world.bentobox.challenges.events;


import java.util.UUID;

import world.bentobox.bentobox.api.events.PremadeEvent;


/**
 * This event is fired when all challenges in given world is reset.
 */
public class ChallengeResetAllEvent extends PremadeEvent
{
	/**
	 * Constructor creates a new ChallengeResetAllEvent instance.
	 *
	 * @param worldName of type String
	 * @param playerUUID of type UUID
	 * @param admin of type boolean
	 * @param reason of type String
	 */
	public ChallengeResetAllEvent(
		String worldName,
		UUID playerUUID,
		boolean admin,
		String reason)
	{
		this.worldName = worldName;
		this.playerUUID = playerUUID;
		this.admin = admin;
		this.reason = reason;
	}


// ---------------------------------------------------------------------
// Section: Getters and setters
// ---------------------------------------------------------------------


	/**
	 * This method returns the worldName value.
	 *
	 * @return the value of worldName.
	 */
	public String getWorldName()
	{
		return worldName;
	}


	/**
	 * This method sets the worldName value.
	 *
	 * @param worldName the worldName new value.
	 */
	public void setWorldName(String worldName)
	{
		this.worldName = worldName;
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
	 * This method returns the reason value.
	 *
	 * @return the value of reason.
	 */
	public String getReason()
	{
		return reason;
	}


	/**
	 * This method sets the reason value.
	 *
	 * @param reason the reason new value.
	 */
	public void setReason(String reason)
	{
		this.reason = reason;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * World where challenges are reset
	 */
	private String worldName;

	/**
	 * User who resets challenges
	 */
	private UUID playerUUID;

	/**
	 * Indicates if admin resets challenges
	 */
	private boolean admin;

	/**
	 * Reset Reason
	 */
	private String reason;
}
