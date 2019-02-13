package world.bentobox.challenges;


import java.util.HashSet;
import java.util.Set;

import world.bentobox.bentobox.api.configuration.ConfigComment;
import world.bentobox.bentobox.api.configuration.ConfigEntry;
import world.bentobox.bentobox.api.configuration.StoreAt;
import world.bentobox.bentobox.database.objects.DataObject;


@StoreAt(filename="config.yml", path="addons/Challenges")
@ConfigComment("Challenges [version] Configuration")
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
	@ConfigComment("This string allows to change element order in Challenge description. Each letter represents")
	@ConfigComment("one object from challenge description. If letter is not used, then its represented part")
	@ConfigComment("will not be in description. If use any letter that is not recognized, then it will be")
	@ConfigComment("ignored. Some strings can be customized via lang file under 'challenges.gui.challenge-description'.")
	@ConfigComment("List of letters and their meaning: ")
	@ConfigComment(" - L - Level String: '*.level'")
	@ConfigComment(" - S - Status String: '*.completed'")
	@ConfigComment(" - T - Times String: '*.completed-times', '*.completed-times-of' or '*.maxed-reached'")
	@ConfigComment(" - D - Description String: defined in challenge object - challenge.description")
	@ConfigComment(" - W - Warning String: '*.warning-items-take', '*.objects-close-by', '*.warning-entities-kill', '*.warning-blocks-remove'")
	@ConfigComment(" - E - Environment String: defined in challenge object - challenge.environment")
	@ConfigComment(" - Q - Requirement String: '*.required-level', '*.required-money', '*.required-experience'")
	@ConfigComment(" - R - Reward String: '*.experience-reward', '*.money-reward', '*.not-repeatable'")
	@ConfigComment("By adding 'i' after Q or R (requirements and rewards) will display list of items, blocks")
	@ConfigComment("and entities that are defined in challenge and can be customized under 'challenges.gui.description.*'")
	@ConfigEntry(path = "challenge-lore-message")
	private String challengeLoreMessage = "LSTDEQiWRi";

	@ConfigComment("")
	@ConfigComment("This string allows to change element order in Level description. Each letter represents")
	@ConfigComment("one object from level description. If letter is not used, then its represented part")
	@ConfigComment("will not be in description. If use any letter that is not recognized, then it will be")
	@ConfigComment("ignored. Some strings can be customized via lang file under 'challenges.gui.level-description'.")
	@ConfigComment("List of letters and their meaning: ")
	@ConfigComment(" - S - Status String: '*.completed'")
	@ConfigComment(" - T - Count of completed challenges String: '*.completed-challenges-of'")
	@ConfigComment(" - D - Description String: defined in level object - challengeLevel.unlockMessage")
	@ConfigComment(" - A - WaiverAmount String: '*.waver-amount'")
	@ConfigComment(" - R - Reward String: '*.experience-reward', '*.money-reward', '*.not-repeatable'")
	@ConfigComment("By adding 'i' after R (rewards) will display list of items that are defined in challenge")
	@ConfigComment("and can be customized under 'challenges.gui.description.*'")
	@ConfigEntry(path = "level-lore-message")
	private String levelLoreMessage = "STDARi";

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

	/**
	 * Configuration version
	 */
	@ConfigComment("")
	private String configVersion = "v1.1";

// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * This method returns the challengeLoreMessage object.
	 * @return the challengeLoreMessage object.
	 */
	public String getChallengeLoreMessage()
	{
		return challengeLoreMessage;
	}


	/**
	 * This method returns the configVersion object.
	 * @return the configVersion object.
	 */
	public String getConfigVersion()
	{
		return configVersion;
	}


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


	/**
	 * This method returns the levelLoreMessage object.
	 * @return the levelLoreMessage object.
	 */
	public String getLevelLoreMessage()
	{
		return levelLoreMessage;
	}


	/**
	 * This method sets the configVersion object value.
	 * @param configVersion the configVersion object new value.
	 */
	public void setConfigVersion(String configVersion)
	{
		this.configVersion = configVersion;
	}


	@Override
	public void setUniqueId(String uniqueId)
	{
		this.uniqueId = uniqueId;
	}


	/**
	 * This method sets the challengeLoreMessage object value.
	 * @param challengeLoreMessage the challengeLoreMessage object new value.
	 */
	public void setChallengeLoreMessage(String challengeLoreMessage)
	{
		this.challengeLoreMessage = challengeLoreMessage;
	}


	/**
	 * This method sets the levelLoreMessage object value.
	 * @param levelLoreMessage the levelLoreMessage object new value.
	 */
	public void setLevelLoreMessage(String levelLoreMessage)
	{
		this.levelLoreMessage = levelLoreMessage;
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
