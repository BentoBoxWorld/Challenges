package world.bentobox.challenges;


import java.util.HashSet;
import java.util.Set;

import world.bentobox.bentobox.api.configuration.ConfigComment;
import world.bentobox.bentobox.api.configuration.ConfigEntry;
import world.bentobox.bentobox.api.configuration.StoreAt;
import world.bentobox.bentobox.database.objects.DataObject;


@StoreAt(filename="config.yml", path="addons/Challenges")
@ConfigComment("Challenges Configuration [version]")
@ConfigComment("This config file is dynamic and saved when the server is shutdown.")
@ConfigComment("You cannot edit it while the server is running because changes will")
@ConfigComment("be lost! Use in-game settings GUI or edit when server is offline.")
@ConfigComment("")
public class Settings implements DataObject
{
	@ConfigComment("")
	@ConfigComment("Reset Challenges - if this is true, player's challenges will reset when they")
	@ConfigComment("reset an island or if they are kicked or leave a team. Prevents exploiting the")
	@ConfigComment("challenges by doing them repeatedly.")
	private boolean resetChallenges = true;

	@ConfigComment("")
	@ConfigComment("Broadcast 1st time challenge completion messages to all players.")
	@ConfigComment("Change to false if the spam becomes too much.")
	private boolean broadcastMessages = true;

	@ConfigComment("")
	@ConfigComment("Remove non-repeatable challenges from the challenge GUI when complete.")
	private boolean removeCompleteOneTimeChallenges = false;

	@ConfigComment("")
	@ConfigComment("Add enchanted glow to completed challenges")
	private boolean addCompletedGlow = true;

	@ConfigComment("")
	@ConfigComment("This list stores GameModes in which Challenges addon should not work.")
	@ConfigComment("To disable addon it is necessary to write its name in new line that starts with -. Example:")
	@ConfigComment("disabled-gamemodes:")
	@ConfigComment(" - BSkyBlock")
	@ConfigEntry(path = "disabled-gamemodes")
	private Set<String> disabledGameModes = new HashSet<>();

	/**
	 * Default variable.
	 */
	@ConfigComment("")
	private String uniqueId = "config";


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	@Override
	public String getUniqueId()
	{
		return this.uniqueId;
	}


	/**
	 * @return resetChallenges value.
	 */
	public boolean isResetChallenges()
	{
		return this.resetChallenges;
	}


	/**
	 * @return broadcastMessages value.
	 */
	public boolean isBroadcastMessages()
	{
		return this.broadcastMessages;
	}


	/**
	 * @return removeCompleteOneTimeChallenges value.
	 */
	public boolean isRemoveCompleteOneTimeChallenges()
	{
		return this.removeCompleteOneTimeChallenges;
	}


	/**
	 * @return addCompletedGlow value.
	 */
	public boolean isAddCompletedGlow()
	{
		return this.addCompletedGlow;
	}


	/**
	 * @return disabledGameModes value.
	 */
	public Set<String> getDisabledGameModes()
	{
		return this.disabledGameModes;
	}


	@Override
	public void setUniqueId(String uniqueId)
	{
		this.uniqueId = uniqueId;
	}


	/**
	 * @param resetChallenges new resetChallenges value.
	 */
	public void setResetChallenges(boolean resetChallenges)
	{
		this.resetChallenges = resetChallenges;
	}


	/**
	 * @param broadcastMessages new broadcastMessages value.
	 */
	public void setBroadcastMessages(boolean broadcastMessages)
	{
		this.broadcastMessages = broadcastMessages;
	}


	/**
	 * @param removeCompleteOneTimeChallenges new removeCompleteOneTimeChallenges value.
	 */
	public void setRemoveCompleteOneTimeChallenges(boolean removeCompleteOneTimeChallenges)
	{
		this.removeCompleteOneTimeChallenges = removeCompleteOneTimeChallenges;
	}


	/**
	 * @param addCompletedGlow new addCompletedGlow value.
	 */
	public void setAddCompletedGlow(boolean addCompletedGlow)
	{
		this.addCompletedGlow = addCompletedGlow;
	}


	/**
	 * @param disabledGameModes new disabledGameModes value.
	 */
	public void setDisabledGameModes(Set<String> disabledGameModes)
	{
		this.disabledGameModes = disabledGameModes;
	}
}
