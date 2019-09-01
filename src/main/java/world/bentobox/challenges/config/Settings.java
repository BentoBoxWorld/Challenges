package world.bentobox.challenges.config;


import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.configuration.ConfigComment;
import world.bentobox.bentobox.api.configuration.ConfigEntry;
import world.bentobox.bentobox.api.configuration.ConfigObject;
import world.bentobox.bentobox.api.configuration.StoreAt;


@StoreAt(filename="config.yml", path="addons/Challenges")
@ConfigComment("Challenges [version] Configuration")
@ConfigComment("This config file is dynamic and saved when the server is shutdown.")
@ConfigComment("You cannot edit it while the server is running because changes will")
@ConfigComment("be lost! Use in-game settings GUI or edit when server is offline.")
@ConfigComment("")
public class Settings implements ConfigObject
{
    @ConfigComment("")
    @ConfigComment("Allows to define common challenges command that will open User GUI")
    @ConfigComment("with all GameMode selection or Challenges from user world.")
    @ConfigComment("This will not affect /{gamemode_user} challenges command.")
    @ConfigEntry(path = "commands.user", needsReset = true)
    private String userCommand = "challenges c";

    @ConfigComment("")
    @ConfigComment("Allows to define common challenges command that will open Admin GUI")
    @ConfigComment("with all GameMode selection.")
    @ConfigComment("This will not affect /{gamemode_admin} challenges command.")
    @ConfigEntry(path = "commands.admin", needsReset = true)
    private String adminCommand = "challengesadmin chadmin";

    @ConfigComment("")
    @ConfigComment("This enables/disables common command that will be independent from")
    @ConfigComment("all GameModes. For admins it will open selection with all GameModes")
    @ConfigComment("(unless there is one), but for users it will open GUI that corresponds")
    @ConfigComment("to their world (unless it is specified other way in Admin GUI).")
    @ConfigEntry(path = "commands.single-gui", needsReset = true)
    private boolean useCommonGUI = false;

    @ConfigComment("")
    @ConfigComment("This allows for admins to define which GUI will be opened for admins")
    @ConfigComment("when users calls single-gui command.")
    @ConfigComment("Acceptable values:")
    @ConfigComment("   - CURRENT_WORLD - will open GUI that corresponds to user location.")
    @ConfigComment("   - GAMEMODE_LIST - will open GUI with all installed game modes.")
    @ConfigEntry(path = "commands.single-gamemode")
    private GuiMode userGuiMode = GuiMode.CURRENT_WORLD;

    @ConfigComment("")
    @ConfigComment("This indicate if player challenges data history will be stored or not.")
    @ConfigEntry(path = "history.store-history-data")
    private boolean storeHistory = false;

    @ConfigComment("")
    @ConfigComment("This allows to specify an amount of time in days when history data will")
    @ConfigComment("be removed. 0 means that data will not be removed.")
    @ConfigEntry(path = "history.lifespan")
    private int lifeSpan = 14;

    @ConfigComment("")
    @ConfigComment("Remove non-repeatable challenges from the challenge GUI when complete.")
    @ConfigEntry(path = "gui-settings.remove-complete-one-time-challenges")
    private boolean removeCompleteOneTimeChallenges = false;

    @ConfigComment("")
    @ConfigComment("Add enchanted glow to completed challenges")
    @ConfigEntry(path = "gui-settings.add-completed-glow")
    private boolean addCompletedGlow = true;

    @ConfigComment("")
    @ConfigComment("This allows to change default locked level icon. This option may be")
    @ConfigComment("overwritten by each challenge level. If challenge level has specified")
    @ConfigComment("their locked level icon, then it will be used, instead of this one.")
    @ConfigEntry(path = "gui-settings.locked-level-icon")
    private ItemStack lockedLevelIcon = new ItemStack(Material.BOOK);

    @ConfigComment("")
    @ConfigComment("This indicate if free challenges must be at the start (true) or at the end (false) of list.")
    @ConfigEntry(path = "gui-settings.free-challenges-first")
    private boolean freeChallengesFirst = true;

    @ConfigComment("")
    @ConfigComment("This allows to change lore description line length. By default it is 25, but some server")
    @ConfigComment("owners may like it to be larger.")
    @ConfigEntry(path = "gui-settings.lore-length")
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
    @ConfigEntry(path = "gui-settings.challenge-lore-message")
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
    @ConfigEntry(path = "gui-settings.level-lore-message")
    private String levelLoreMessage = "STDARi";

    @ConfigComment("")
    @ConfigComment("This indicate if challenges data will be stored per island (true) or per player (false).")
    @ConfigEntry(path = "store-island-data")
    private boolean storeAsIslandData = false;

    @ConfigComment("")
    @ConfigComment("Reset Challenges - if this is true, player's challenges will reset when users")
    @ConfigComment("reset an island or if users are kicked or leave a team. Prevents exploiting the")
    @ConfigComment("challenges by doing them repeatedly.")
    @ConfigEntry(path = "reset-challenges")
    private boolean resetChallenges = true;

    @ConfigComment("")
    @ConfigComment("Broadcast 1st time challenge completion messages to all players.")
    @ConfigComment("Change to false if the spam becomes too much.")
    @ConfigEntry(path = "broadcast-messages")
    private boolean broadcastMessages = true;

    @ConfigComment("")
    @ConfigComment("Shows a title screen for player after completion a challenge or level.")
    @ConfigComment("Message can be edited via language settings.")
    @ConfigEntry(path = "title.show-title")
    private boolean showCompletionTitle = true;

    @ConfigComment("")
    @ConfigComment("Integer that represents how long title will be visible for player.")
    @ConfigEntry(path = "title.title-showtime")
    private int titleShowtime = 70;

    @ConfigComment("")
    @ConfigComment("Long that represents how frequently (in minutes) challenges addon will save data to database.")
    @ConfigComment("If this is set to 0, saving will not happen.")
    @ConfigEntry(path = "auto-saver")
    private long autoSaveTimer = 30;

    @ConfigComment("")
    @ConfigComment("This list stores GameModes in which Challenges addon should not work.")
    @ConfigComment("To disable addon it is necessary to write its name in new line that starts with -. Example:")
    @ConfigComment("disabled-gamemodes:")
    @ConfigComment(" - BSkyBlock")
    @ConfigEntry(path = "disabled-gamemodes")
    private Set<String> disabledGameModes = new HashSet<>();

    /**
     * Configuration version
     */
    @ConfigComment("")
    private String configVersion = "v3";


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
     * This method returns the storeAsIslandData object.
     * @return the storeAsIslandData object.
     */
    public boolean isStoreAsIslandData()
    {
        return storeAsIslandData;
    }


    /**
     * This method returns the storeHistory object.
     * @return the storeHistory object.
     */
    public boolean isStoreHistory()
    {
        return storeHistory;
    }


    /**
     * This method returns the userCommand value.
     * @return the value of userCommand.
     */
    public String getUserCommand()
    {
        return userCommand;
    }


    /**
     * This method returns the adminCommand value.
     * @return the value of adminCommand.
     */
    public String getAdminCommand()
    {
        return adminCommand;
    }


    /**
     * This method returns the useCommonGUI value.
     * @return the value of useCommonGUI.
     */
    public boolean isUseCommonGUI()
    {
        return useCommonGUI;
    }


    /**
     * This method returns the userGuiMode value.
     * @return the value of userGuiMode.
     */
    public GuiMode getUserGuiMode()
    {
        return userGuiMode;
    }


    /**
     * This method returns the lifeSpan value.
     * @return the value of lifeSpan.
     */
    public int getLifeSpan()
    {
        return lifeSpan;
    }


    /**
     * This method returns the lockedLevelIcon value.
     * @return the value of lockedLevelIcon.
     */
    public ItemStack getLockedLevelIcon()
    {
        return lockedLevelIcon.clone();
    }


    /**
     * This method returns the showCompletionTitle object.
     * @return the showCompletionTitle object.
     */
    public boolean isShowCompletionTitle()
    {
        return this.showCompletionTitle;
    }


    /**
     * This method returns the titleShowtime object.
     * @return the titleShowtime object.
     */
    public int getTitleShowtime()
    {
        return this.titleShowtime;
    }


    /**
     * This method returns the autoSaveTimer object.
     * @return the autoSaveTimer object.
     */
    public long getAutoSaveTimer()
    {
        return autoSaveTimer;
    }


    /**
     * This method sets the autoSaveTimer object value.
     * @param autoSaveTimer the autoSaveTimer object new value.
     *
     */
    public void setAutoSaveTimer(long autoSaveTimer)
    {
        this.autoSaveTimer = autoSaveTimer;
    }


    /**
     * This method sets the titleShowtime object value.
     * @param titleShowtime the titleShowtime object new value.
     *
     */
    public void setTitleShowtime(int titleShowtime)
    {
        this.titleShowtime = titleShowtime;
    }


    /**
     * This method sets the showCompletionTitle object value.
     * @param showCompletionTitle the showCompletionTitle object new value.
     *
     */
    public void setShowCompletionTitle(boolean showCompletionTitle)
    {
        this.showCompletionTitle = showCompletionTitle;
    }


    /**
     * This method sets the lockedLevelIcon value.
     * @param lockedLevelIcon the lockedLevelIcon new value.
     *
     */
    public void setLockedLevelIcon(ItemStack lockedLevelIcon)
    {
        this.lockedLevelIcon = lockedLevelIcon;
    }


    /**
     * This method sets the userGuiMode value.
     * @param userGuiMode the userGuiMode new value.
     */
    public void setUserGuiMode(GuiMode userGuiMode)
    {
        this.userGuiMode = userGuiMode;
    }


    /**
     * This method sets the configVersion object value.
     * @param configVersion the configVersion object new value.
     */
    public void setConfigVersion(String configVersion)
    {
        this.configVersion = configVersion;
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


    /**
     * This method sets the storeAsIslandData object value.
     * @param storeAsIslandData the storeAsIslandData object new value.
     */
    public void setStoreAsIslandData(boolean storeAsIslandData)
    {
        this.storeAsIslandData = storeAsIslandData;
    }


    /**
     * This method sets the storeHistory object value.
     * @param storeHistory the storeHistory object new value.
     */
    public void setStoreHistory(boolean storeHistory)
    {
        this.storeHistory = storeHistory;
    }


    /**
     * This method sets the userCommand value.
     * @param userCommand the userCommand new value.
     */
    public void setUserCommand(String userCommand)
    {
        this.userCommand = userCommand;
    }


    /**
     * This method sets the adminCommand value.
     * @param adminCommand the adminCommand new value.
     */
    public void setAdminCommand(String adminCommand)
    {
        this.adminCommand = adminCommand;
    }


    /**
     * This method sets the useCommonGUI value.
     * @param useCommonGUI the useCommonGUI new value.
     */
    public void setUseCommonGUI(boolean useCommonGUI)
    {
        this.useCommonGUI = useCommonGUI;
    }


    /**
     * This method sets the lifeSpan value.
     * @param lifeSpan the lifeSpan new value.
     *
     */
    public void setLifeSpan(int lifeSpan)
    {
        this.lifeSpan = lifeSpan;
    }


    // ---------------------------------------------------------------------
    // Section: Enums
    // ---------------------------------------------------------------------


    /**
     * This enum holds all possible values for Gui Opening for users.
     */
    public enum GuiMode
    {
        /**
         * Opens user GUI with list of all GameModes.
         */
        GAMEMODE_LIST,
        /**
         * Opens user GUI with challenges in given world.
         */
        CURRENT_WORLD
    }
}
