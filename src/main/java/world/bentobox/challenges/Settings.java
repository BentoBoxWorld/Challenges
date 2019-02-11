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
	@ConfigEntry(path = "reset-challenges")
	private boolean resetChallenges = true;

	@ConfigComment("")
	@ConfigComment("Broadcast 1st time challenge completion messages to all players.")
	@ConfigComment("Change to false if the spam becomes too much.")
	@ConfigEntry(path = "broadcast-messages")
	private boolean broadcastMessages = true;

	@ConfigComment("")
	@ConfigComment("Remove non-repeatable challenges from the challenge GUI when complete.")
	@ConfigEntry(path = "remove-complete-one-time-challenges")
	private boolean removeCompleteOneTimeChallenges = false;

	@ConfigComment("")
	@ConfigComment("Add enchanted glow to completed challenges")
	@ConfigEntry(path = "add-completed-glow")
	private boolean addCompletedGlow = true;

	@ConfigComment("")
	@ConfigComment("This indicate if free challenges must be at the start (true) or at the end (false) of list.")
	@ConfigEntry(path = "free-challenges-first")
	private boolean freeChallengesFirst = true;

	@ConfigComment("")
	@ConfigComment("This allows to change lore description line length. By default it is 25, but some server")
	@ConfigComment("owners may like it to be larger.")
	@ConfigEntry(path = "lore-length")
	private int loreLineLength = 25;

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


	/**
	 * @return freeChallengesFirst value.
	 */
	public boolean isFreeChallengesFirst()
	{
		return this.freeChallengesFirst;
	}


	/**
	 * This method returns the loreLineLength object.
	 * @return the loreLineLength object.
	 */
	public int getLoreLineLength()
	{
		return loreLineLength;
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


	/**
	 * @param freeChallengesFirst new freeChallengesFirst value.
	 */
	public void setFreeChallengesFirst(boolean freeChallengesFirst)
	{
		this.freeChallengesFirst = freeChallengesFirst;
	}


	/**
	 * This method sets the loreLineLength object value.
	 * @param loreLineLength the loreLineLength object new value.
	 */
	public void setLoreLineLength(int loreLineLength)
	{
		this.loreLineLength = loreLineLength;
	}
}
