package world.bentobox.challenges.panel;


import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.banner.Pattern;
import org.bukkit.conversations.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.hooks.LangUtilsHook;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.ChallengesManager;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.database.object.requirements.InventoryRequirements;
import world.bentobox.challenges.database.object.requirements.IslandRequirements;
import world.bentobox.challenges.database.object.requirements.OtherRequirements;
import world.bentobox.challenges.utils.LevelStatus;
import world.bentobox.challenges.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


/**
 * This class contains common methods that will be used over all GUIs. It also allows
 * easier navigation between different GUIs.
 */
public abstract class CommonGUI
{
    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * This variable stores parent gui.
     */
    protected CommonGUI parentGUI;

    /**
     * Variable stores Challenges addon.
     */
    protected ChallengesAddon addon;

    /**
     * Variable stores world in which panel is referred to.
     */
    protected World world;

    /**
     * Variable stores user who created this panel.
     */
    protected User user;

    /**
     * Variable stores top label of command from which panel was called.
     */
    protected String topLabel;

    /**
     * Variable stores permission prefix of command from which panel was called.
     */
    protected String permissionPrefix;

    /**
     * Variable stores any value.
     */
    protected Object valueObject;

    /**
     * This object holds current page index.
     */
    protected int pageIndex;

    /**
     * This object holds PanelItem that allows to return to previous panel.
     */
    protected PanelItem returnButton;


    /**
     * This enum contains buttons that is offten used in multiple GUIs.
     */
    protected enum CommonButtons
    {
        NEXT,
        PREVIOUS,
        RETURN
    }


    // ---------------------------------------------------------------------
    // Section: Constants
    // ---------------------------------------------------------------------


    protected static final String ADMIN = "admin";

    protected static final String CHALLENGES = "challenges";

    protected static final String IMPORT = "import";

    protected static final String DEFAULT = "defaults";

    protected static final String GENERATE = "generate";

    protected static final String SETTINGS = "settings";

    protected static final String DELETE = "delete";

    protected static final String WIPE = "wipe";

    protected static final String EDIT = "edit";

    protected static final String ADD = "add";

    protected static final String RESET = "reset";

    protected static final String COMPLETE = "complete";

    protected static final String DOWNLOAD = "download";

    // ---------------------------------------------------------------------
    // Section: Constructors
    // ---------------------------------------------------------------------


    /**
     * Default constructor that inits panels with minimal requirements, without parent panel.
     *
     * @param addon Addon where panel operates.
     * @param world World from which panel was created.
     * @param user User who created panel.
     * @param topLabel Command top label which creates panel (f.e. island or ai)
     * @param permissionPrefix Command permission prefix (f.e. bskyblock.)
     */
    public CommonGUI(ChallengesAddon addon,
            World world,
            User user,
            String topLabel,
            String permissionPrefix)
    {
        this(addon, world, user, topLabel, permissionPrefix, null);
    }


    /**
     * Default constructor that inits panels with minimal requirements.
     *
     * @param addon Addon where panel operates.
     * @param world World from which panel was created.
     * @param user User who created panel.
     * @param topLabel Command top label which creates panel (f.e. island or ai)
     * @param permissionPrefix Command permission prefix (f.e. bskyblock.)
     * @param parentGUI Parent panel for current panel.
     */
    public CommonGUI(ChallengesAddon addon,
            World world,
            User user,
            String topLabel,
            String permissionPrefix,
            CommonGUI parentGUI)
    {
        this.addon = addon;
        this.world = world;
        this.user = user;

        this.topLabel = topLabel;
        this.permissionPrefix = permissionPrefix;

        this.parentGUI = parentGUI;

        this.pageIndex = 0;

        this.returnButton = new PanelItemBuilder().
                name(this.user.getTranslation("challenges.gui.buttons.return")).
                icon(Material.OAK_DOOR).
                clickHandler((panel, user1, clickType, i) -> {
                    if (this.parentGUI == null)
                    {
                        this.user.closeInventory();
                        return true;
                    }

                    this.parentGUI.build();
                    return true;
                }).build();
    }


    /**
     * Default constructor that inits panels with minimal requirements.
     * @param parentGUI Parent panel for current panel.
     */
    public CommonGUI(CommonGUI parentGUI)
    {
        this.addon = parentGUI.addon;
        this.world = parentGUI.world;
        this.user = parentGUI.user;

        this.topLabel = parentGUI.topLabel;
        this.permissionPrefix = parentGUI.permissionPrefix;

        this.parentGUI = parentGUI;

        this.pageIndex = 0;

        this.returnButton = new PanelItemBuilder().
                name(this.user.getTranslation("challenges.gui.buttons.return")).
                icon(Material.OAK_DOOR).
                clickHandler((panel, user1, clickType, i) -> {

                    if (this.parentGUI == null)
                    {
                        this.user.closeInventory();
                        return true;
                    }

                    this.parentGUI.build();
                    return true;
                }).build();
    }


    // ---------------------------------------------------------------------
    // Section: Common methods
    // ---------------------------------------------------------------------


    /**
     * This method builds all necessary elements in GUI panel.
     */
    public abstract void build();


    /**
     * This method returns PanelItem that represents given Button.
     * @param button Button that must be returned.
     * @return PanelItem with requested functionality.
     */
    protected PanelItem getButton(CommonButtons button)
    {
        ItemStack icon;
        String name;
        List<String> description;
        PanelItem.ClickHandler clickHandler;

        switch (button)
        {
        case NEXT:
        {
            name = this.user.getTranslation("challenges.gui.buttons.next");
            description = Collections.emptyList();
            icon = new ItemStack(Material.OAK_SIGN);
            clickHandler = (panel, user, clickType, slot) -> {
                this.pageIndex++;
                this.build();
                return true;
            };

            break;
        }
        case PREVIOUS:
        {
            name = this.user.getTranslation("challenges.gui.buttons.previous");
            description = Collections.emptyList();
            icon = new ItemStack(Material.OAK_SIGN);
            clickHandler = (panel, user, clickType, slot) -> {
                this.pageIndex--;
                this.build();
                return true;
            };

            break;
        }
        case RETURN:
            return this.returnButton;
        default:
            return null;
        }

        return new PanelItemBuilder().
                icon(icon).
                name(name).
                description(description).
                glow(false).
                clickHandler(clickHandler).
                build();
    }


    /**
     * This method sets new value to ValueObject variable.
     * @param value new Value of valueObject.
     */
    public void setValue(Object value)
    {
        this.valueObject = value;
    }


    // ---------------------------------------------------------------------
    // Section: Generate Challenge Description
    // ---------------------------------------------------------------------


    /**
     * This method generates and returns given challenge description. It is used here to avoid multiple
     * duplicates, as it would be nice to have single place where challenge could be generated.
     * @param challenge Challenge which description must be generated.
     * @return List of strings that will be used in challenges description.
     */
    protected List<String> generateChallengeDescription(Challenge challenge, Player user)
    {
        List<String> result = new ArrayList<>();

        // Some values to avoid overchecking.
        ChallengesManager manager = this.addon.getChallengesManager();

        final boolean isCompletedOnce =
                manager.isChallengeComplete(user.getUniqueId(), world, challenge);
        final long doneTimes = challenge.isRepeatable() ?
                manager.getChallengeTimes(this.user, this.world, challenge) : isCompletedOnce ? 0 : 1;

                boolean isCompletedAll = isCompletedOnce && challenge.isRepeatable() &&
                        challenge.getMaxTimes() > 0 &&
                        doneTimes >= challenge.getMaxTimes();

                        this.addon.getChallengesSettings().getChallengeLoreMessage().forEach(messagePart -> {
                            switch (messagePart)
                            {
                            case LEVEL:
                            {
                                ChallengeLevel level = manager.getLevel(challenge);

                                if (level == null)
                                {
                                    result.add(this.user.getTranslation("challenges.errors.missing-level",
                                            "[level]", challenge.getLevel()));
                                }
                                else
                                {
                                    result.add(this.user
                                            .getTranslation("challenges.gui.challenge-description.level",
                                                    "[level]", level.getFriendlyName()));
                                }
                                break;
                            }
                            case STATUS:
                            {
                                if (isCompletedOnce)
                                {
                                    result.add(this.user
                                            .getTranslation("challenges.gui.challenge-description.completed"));
                                }
                                break;
                            }
                            case COUNT:
                            {
                                if (challenge.isRepeatable())
                                {
                                    if (challenge.getMaxTimes() > 0)
                                    {
                                        if (isCompletedAll)
                                        {
                                            result.add(this.user.getTranslation(
                                                    "challenges.gui.challenge-description.maxed-reached",
                                                    "[donetimes]",
                                                    String.valueOf(doneTimes),
                                                    "[maxtimes]",
                                                    String.valueOf(challenge.getMaxTimes())));
                                        }
                                        else
                                        {
                                            result.add(this.user.getTranslation(
                                                    "challenges.gui.challenge-description.completed-times-of",
                                                    "[donetimes]",
                                                    String.valueOf(doneTimes),
                                                    "[maxtimes]",
                                                    String.valueOf(challenge.getMaxTimes())));
                                        }
                                    }
                                    else
                                    {
                                        result.add(this.user.getTranslation(
                                                "challenges.gui.challenge-description.completed-times",
                                                "[donetimes]",
                                                String.valueOf(doneTimes)));
                                    }
                                }
                                break;
                            }
                            case DESCRIPTION:
                            {
                                result.addAll(challenge.getDescription());
                                break;
                            }
                            case WARNINGS:
                            {
                                if (!isCompletedAll)
                                {
                                    if (challenge.getChallengeType().equals(Challenge.ChallengeType.INVENTORY))
                                    {
                                        if (challenge.<InventoryRequirements>getRequirements().isTakeItems())
                                        {
                                            result.add(this.user.getTranslation(
                                                    "challenges.gui.challenge-description.warning-items-take"));
                                        }
                                    }
                                    else if (challenge.getChallengeType().equals(Challenge.ChallengeType.ISLAND))
                                    {
                                        result.add(this.user.getTranslation(
                                                "challenges.gui.challenge-description.objects-close-by"));

                                        IslandRequirements requirements = challenge.getRequirements();

                                        if (requirements.isRemoveEntities() && !requirements.getRequiredEntities().isEmpty())
                                        {
                                            result.add(this.user.getTranslation(
                                                    "challenges.gui.challenge-description.warning-entities-kill"));
                                        }

                                        if (requirements.isRemoveBlocks() && !requirements.getRequiredBlocks().isEmpty())
                                        {
                                            result.add(this.user.getTranslation(
                                                    "challenges.gui.challenge-description.warning-blocks-remove"));
                                        }
                                    }
                                }
                                break;
                            }
                            case ENVIRONMENT:
                            {
                                // Display only if there are limited environments

                                if (!isCompletedAll &&
                                        !challenge.getEnvironment().isEmpty() &&
                                        challenge.getEnvironment().size() != 3)
                                {
                                    result.add(this.user.getTranslation("challenges.gui.challenge-description.environment"));

                                    if (challenge.getEnvironment().contains(World.Environment.NORMAL))
                                    {
                                        result.add(this.user.getTranslation("challenges.gui.descriptions.normal"));
                                    }

                                    if (challenge.getEnvironment().contains(World.Environment.NETHER))
                                    {
                                        result.add(this.user.getTranslation("challenges.gui.descriptions.nether"));
                                    }

                                    if (challenge.getEnvironment().contains(World.Environment.THE_END))
                                    {
                                        result.add(this.user.getTranslation("challenges.gui.descriptions.the-end"));
                                    }
                                }
                                break;
                            }
                            case REQUIREMENTS:
                            {
                                if (!isCompletedAll)
                                {
                                    switch (challenge.getChallengeType())
                                    {
                                    case INVENTORY:
                                        result.addAll(this.getInventoryRequirements(challenge.getRequirements()));
                                        break;
                                    case ISLAND:
                                        result.addAll(this.getIslandRequirements(challenge.getRequirements()));
                                        break;
                                    case OTHER:
                                        result.addAll(this.getOtherRequirements(challenge.getRequirements()));
                                        break;
                                    }
                                }

                                break;
                            }
                            case REWARD_TEXT:
                            {
                                if (isCompletedAll)
                                {
                                    result.add(this.user.getTranslation("challenges.gui.challenge-description.not-repeatable"));
                                }
                                else
                                {
                                    // Show a title to the rewards
                                    // If there is no reward text, do not display title
                                    String rewardText = isCompletedOnce ? challenge.getRepeatRewardText() : challenge.getRewardText();
                                    if (rewardText != null)
                                    {
                                        String testText = ChatColor.translateAlternateColorCodes('&', rewardText);
                                        if (!ChatColor.stripColor(testText.replaceAll("[\\r\\n]", "")).isEmpty())
                                        {
                                            result.add(this.user.getTranslation("challenges.gui.challenge-description.rewards-title"));
                                            result.add(rewardText);
                                        }
                                    }
                                }
                                break;
                            }
                            case REWARD_OTHER:
                            {
                                if (!isCompletedAll)
                                {
                                    result.addAll(this.getChallengeRewardOthers(challenge, isCompletedOnce));
                                }
                                break;
                            }
                            case REWARD_ITEMS:
                            {
                                if (!isCompletedAll)
                                {
                                    result.addAll(this.getChallengeRewardItems(challenge, isCompletedOnce));
                                }
                                break;
                            }
                            case REWARD_COMMANDS:
                            {
                                if (!isCompletedAll)
                                {
                                    result.addAll(this.getChallengeRewardCommands(challenge, isCompletedOnce, user));
                                }
                                break;
                            }
                            }
                        });

                        result.replaceAll(x -> x.replace("[label]", this.topLabel));

                        return result;
    }


    /**
     * This method returns list of strings that contains basic information about challenge rewards.
     * @param challenge which reward message must be created.
     * @param isCompletedOnce indicate if must use repeat rewards
     * @return list of strings that contains rewards message.
     */
    private List<String> getChallengeRewardOthers(Challenge challenge, boolean isCompletedOnce)
    {
        double rewardMoney;
        int rewardExperience;


        if (!isCompletedOnce)
        {
            rewardMoney = challenge.getRewardMoney();
            rewardExperience = challenge.getRewardExperience();
        }
        else
        {
            rewardMoney = challenge.getRepeatMoneyReward();
            rewardExperience = challenge.getRepeatExperienceReward();
        }

        List<String> result = new ArrayList<>();

        // Add message about reward XP
        if (rewardExperience > 0)
        {
            result.add(this.user.getTranslation("challenges.gui.challenge-description.experience-reward",
                    "[value]", Integer.toString(rewardExperience)));
        }

        // Add message about reward money
        if (this.addon.getPlugin().getSettings().isUseEconomy() && rewardMoney > 0)
        {
            result.add(this.user.getTranslation("challenges.gui.challenge-description.money-reward",
                    "[value]", Double.toString(rewardMoney)));
        }

        return result;
    }


    /**
     * This method returns list of strings that contains reward items from given challenge.
     * @param challenge Challenge which reward items and commands must be returned.
     * @param isCompletedOnce Boolean that indicate if must use repeat rewards.
     * @return List of strings that contains message from challenges.
     */
    private List<String> getChallengeRewardItems(Challenge challenge, boolean isCompletedOnce)
    {
        List<String> result = new ArrayList<>();

        List<ItemStack> rewardItems;

        if (isCompletedOnce)
        {
            rewardItems = challenge.getRepeatItemReward();
        }
        else
        {
            rewardItems = challenge.getRewardItems();
        }

        // Add message about reward items
        if (!rewardItems.isEmpty())
        {
            result.add(this.user.getTranslation("challenges.gui.challenge-description.reward-items"));

            Utils.groupEqualItems(rewardItems).forEach(itemStack ->
            result.addAll(this.generateItemStackDescription(itemStack)));
        }

        return result;
    }


    /**
     * This method returns list of strings that contains reward commands from given challenge.
     * @param challenge Challenge which reward items and commands must be returned.
     * @param isCompletedOnce Boolean that indicate if must use repeat rewards.
     * @param user Target user for command string.
     * @return List of strings that contains message from challenges.
     */
    private List<String> getChallengeRewardCommands(Challenge challenge, boolean isCompletedOnce, Player user)
    {
        List<String> result = new ArrayList<>();

        List<String> rewardCommands;

        if (isCompletedOnce)
        {
            rewardCommands = challenge.getRepeatRewardCommands();
        }
        else
        {
            rewardCommands = challenge.getRewardCommands();
        }

        // Add message about reward commands
        if (!rewardCommands.isEmpty())
        {
            result.add(this.user.getTranslation("challenges.gui.challenge-description.reward-commands"));

            for (String command : rewardCommands)
            {
                result.add(this.user.getTranslation("challenges.gui.descriptions.command",
                        "[command]",  command.replace("[player]", user.getName()).replace("[SELF]", "")));
            }
        }

        return result;
    }


    /**
     * This method returns list of strings that contains basic information about requirements.
     * @param requirements which requirements message must be created.
     * @return list of strings that contains requirements message.
     */
    private List<String> getOtherRequirements(OtherRequirements requirements)
    {
        List<String> result = new ArrayList<>();

        // Add message about required exp
        if (requirements.getRequiredExperience() > 0)
        {
            result.add(this.user.getTranslation("challenges.gui.challenge-description.required-experience",
                    "[value]", Integer.toString(requirements.getRequiredExperience())));
        }

        // Add message about required money
        if (this.addon.isEconomyProvided() && requirements.getRequiredMoney() > 0)
        {
            result.add(this.user.getTranslation("challenges.gui.challenge-description.required-money",
                    "[value]", Double.toString(requirements.getRequiredMoney())));
        }

        // Add message about required island level
        if (this.addon.isLevelProvided() && requirements.getRequiredIslandLevel() > 0)
        {
            result.add(this.user.getTranslation("challenges.gui.challenge-description.required-island-level",
                    "[value]", Long.toString(requirements.getRequiredIslandLevel())));
        }

        return result;
    }


    /**
     * This method returns list of strings that contains basic information about requirements.
     * @param requirements which requirements message must be created.
     * @return list of strings that contains requirements message.
     */
    private List<String> getInventoryRequirements(InventoryRequirements requirements)
    {
        List<String> result = new ArrayList<>();

        // Add message about required items
        if (!requirements.getRequiredItems().isEmpty())
        {
            result.add(this.user.getTranslation("challenges.gui.challenge-description.required-items"));

            Utils.groupEqualItems(requirements.getRequiredItems()).forEach(itemStack ->
            result.addAll(this.generateItemStackDescription(itemStack)));
        }

        return result;
    }


    /**
     * This method returns list of strings that contains required items, entities and blocks from given challenge.
     * @param challenge Challenge which requirement items, entities and blocks must be returned.
     * @return List of strings that contains message from challenges.
     */
    private List<String> getIslandRequirements(IslandRequirements challenge)
    {
        List<String> result = new ArrayList<>();

        if (!challenge.getRequiredBlocks().isEmpty() || !challenge.getRequiredEntities().isEmpty())
        {
            // Add required blocks
            if (!challenge.getRequiredBlocks().isEmpty())
            {
                result.add(this.user.getTranslation("challenges.gui.challenge-description.required-blocks"));

                for (Map.Entry<Material, Integer> entry : challenge.getRequiredBlocks().entrySet())
                {
                    result.add(this.user.getTranslation("challenges.gui.descriptions.block",
                            "[block]", LangUtilsHook.getMaterialName(entry.getKey(), user),
                            "[count]", Integer.toString(entry.getValue())));
                }
            }

            // Add required entities
            if (!challenge.getRequiredEntities().isEmpty())
            {
                result.add(this.user.getTranslation("challenges.gui.challenge-description.required-entities"));

                for (Map.Entry<EntityType, Integer> entry : challenge.getRequiredEntities().entrySet())
                {
                    result.add(this.user.getTranslation("challenges.gui.descriptions.entity",
                            "[entity]", LangUtilsHook.getEntityName(entry.getKey(), user),
                            "[count]", Integer.toString(entry.getValue())));
                }
            }
        }

        return result;
    }


    // ---------------------------------------------------------------------
    // Section: Generate Level Description
    // ---------------------------------------------------------------------


    /**
     * This method generates level description string.
     * @param level Level which string must be generated.
     * @param user User who calls generation.
     * @return List with generated description.
     */
    protected List<String> generateLevelDescription(ChallengeLevel level, Player user)
    {
        List<String> result = new ArrayList<>();

        ChallengesManager manager = this.addon.getChallengesManager();
        LevelStatus status = manager.getChallengeLevelStatus(user.getUniqueId(), this.world, level);

        // Check if unlock message should appear.
        boolean hasCompletedOne = status.isComplete() || status.isUnlocked() &&
                level.getChallenges().stream().anyMatch(challenge ->
                this.addon.getChallengesManager().isChallengeComplete(user.getUniqueId(), world, challenge));

        this.addon.getChallengesSettings().getLevelLoreMessage().forEach(messagePart -> {
            switch (messagePart)
            {
            case LEVEL_STATUS:
            {
                if (status.isComplete())
                {
                    result.add(this.user.getTranslation("challenges.gui.level-description.completed"));
                }
                break;
            }
            case CHALLENGE_COUNT:
            {
                if (!status.isComplete() && status.isUnlocked())
                {
                    int doneChallengeCount = (int) level.getChallenges().stream().
                            filter(challenge -> this.addon.getChallengesManager().isChallengeComplete(user.getUniqueId(), world, challenge)).
                            count();

                    result.add(this.user.getTranslation("challenges.gui.level-description.completed-challenges-of",
                            "[number]", Integer.toString(doneChallengeCount),
                            "[max]", Integer.toString(level.getChallenges().size())));
                }

                break;
            }
            case UNLOCK_MESSAGE:
            {
                if (!hasCompletedOne)
                {
                    result.add(level.getUnlockMessage());
                }

                break;
            }
            case WAIVER_AMOUNT:
            {
                if (status.isUnlocked() && !status.isComplete())
                {
                    result.add(this.user.getTranslation("challenges.gui.level-description.waver-amount",
                            "[value]", Integer.toString(level.getWaiverAmount())));
                }

                break;
            }
            case LEVEL_REWARD_TEXT:
            {
                if (status.isUnlocked() && !status.isComplete())
                {
                    result.add(level.getRewardText());
                }
                break;
            }
            case LEVEL_REWARD_OTHER:
            {
                if (status.isUnlocked() && !status.isComplete())
                {
                    if (level.getRewardExperience() > 0)
                    {
                        result.add(this.user.getTranslation("challenges.gui.level-description.experience-reward",
                                "[value]", Integer.toString(level.getRewardExperience())));
                    }

                    if (this.addon.isEconomyProvided() && level.getRewardMoney() > 0)
                    {
                        result.add(this.user.getTranslation("challenges.gui.level-description.money-reward",
                                "[value]", Integer.toString(level.getRewardMoney())));
                    }
                }
                break;
            }
            case LEVEL_REWARD_ITEMS:
            {
                if (status.isUnlocked() && !status.isComplete() && !level.getRewardItems().isEmpty())
                {
                    result.add(this.user.getTranslation("challenges.gui.level-description.reward-items"));

                    Utils.groupEqualItems(level.getRewardItems()).forEach(itemStack ->
                    result.addAll(this.generateItemStackDescription(itemStack)));
                }
                break;
            }
            case LEVEL_REWARD_COMMANDS:
            {
                if (status.isUnlocked() && !status.isComplete() && !level.getRewardCommands().isEmpty())
                {
                    result.add(this.user.getTranslation("challenges.gui.level-description.reward-commands"));

                    for (String command : level.getRewardCommands())
                    {
                        result.add(this.user.getTranslation("challenges.gui.descriptions.command",
                                "[command]",  command.replace("[player]", user.getName()).replace("[SELF]", "")));
                    }
                }
                break;
            }
            }
        });

        result.replaceAll(x -> x.replace("[label]", this.topLabel));

        return result;
    }


    // ---------------------------------------------------------------------
    // Section: ItemStack Description
    // ---------------------------------------------------------------------


    /**
     * This method generates description for given item stack object.
     * @param itemStack Object which lore must be generated
     * @return List with generated description
     */
    @SuppressWarnings("deprecation")
    protected List<String> generateItemStackDescription(ItemStack itemStack)
    {
        List<String> result = new ArrayList<>();

        result.add(this.user.getTranslation("challenges.gui.item-description.item",
                "[item]", LangUtilsHook.getItemName(itemStack, user),
                "[count]", Integer.toString(itemStack.getAmount())));

        String cdDesc = LangUtilsHook.getMusicDiskDesc(itemStack.getType(), user);
        if (cdDesc != null)
        {
            result.add(this.user.getTranslation("challenges.gui.item-description.music-disk-desc",
                    "[desc]", cdDesc));
        }

        if (itemStack.hasItemMeta())
        {
            ItemMeta meta = itemStack.getItemMeta();

            if (meta.hasDisplayName())
            {
                result.add(this.user.getTranslation("challenges.gui.item-description.item-name",
                        "[name]", meta.getDisplayName()));
            }

            if (meta.hasLore())
            {
                result.add(this.user.getTranslation("challenges.gui.item-description.item-lore"));
                result.addAll(meta.getLore());
            }

            if (meta instanceof BookMeta)
            {
                result.add(this.user.getTranslation("challenges.gui.item-description.book-meta",
                        "[author]", ((BookMeta) meta).getAuthor(),
                        "[title]", ((BookMeta) meta).getTitle()));
            }
            else if (meta instanceof EnchantmentStorageMeta)
            {
                ((EnchantmentStorageMeta) meta).getStoredEnchants().forEach(((enchantment, level) -> {
                    result.add(this.user.getTranslation("challenges.gui.item-description.item-enchant",
                            "[enchant]", LangUtilsHook.getEnchantName(enchantment, user),
                            "[level]", LangUtilsHook.getEnchantLevelName(level, user)));
                }));
            }
            else if (meta instanceof KnowledgeBookMeta)
            {
                result.add(this.user.getTranslation("challenges.gui.item-description.recipe-count",
                        "[count]", Integer.toString(((KnowledgeBookMeta) meta).getRecipes().size())));
            }
            else if (meta instanceof LeatherArmorMeta)
            {
                // The color value should be displayed like "#2B50E1"
                Color color = ((LeatherArmorMeta) meta).getColor();
                String cols = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
                result.add(this.user.getTranslation("challenges.gui.item-description.armor-color", "[color]", cols));
            }
            else if (meta instanceof PotionMeta)
            {
                PotionData data = ((PotionMeta) meta).getBasePotionData();
                PotionType type = data.getType();

                if (data.isExtended() && data.isUpgraded())
                {
                    result.add(this.user.getTranslation("challenges.gui.item-description.potion-type-extended-upgraded",
                            "[name]", LangUtilsHook.getPotionBaseEffectName(type, user)));
                }
                else if (data.isUpgraded())
                {
                    result.add(this.user.getTranslation("challenges.gui.item-description.potion-type-upgraded",
                            "[name]", LangUtilsHook.getPotionBaseEffectName(type, user)));
                }
                else if (data.isExtended())
                {
                    result.add(this.user.getTranslation("challenges.gui.item-description.potion-type-extended",
                            "[name]", LangUtilsHook.getPotionBaseEffectName(type, user)));
                }
                else
                {
                    result.add(this.user.getTranslation("challenges.gui.item-description.potion-type",
                            "[name]", LangUtilsHook.getPotionBaseEffectName(type, user)));
                }

                if (((PotionMeta) meta).hasCustomEffects())
                {
                    result.add(this.user.getTranslation("challenges.gui.item-description.custom-effects"));

                    ((PotionMeta) meta).getCustomEffects().forEach(potionEffect ->
                    {
                        int duration = potionEffect.getDuration();
                        int m = duration / 20 / 60;
                        int s = duration / 20 % 60;
                        result.add(this.user.getTranslation("challenges.gui.item-description.potion-effect",
                                "[effect]", LangUtilsHook.getPotionEffectName(potionEffect.getType(), user),
                                "[duration]", String.format("%d:%02d", m, s),
                                "[amplifier]", LangUtilsHook.getEffectAmplifierName(potionEffect.getAmplifier(), user)));
                    });
                }
            }
            else if (meta instanceof SkullMeta)
            {
                OfflinePlayer ofp = ((SkullMeta) meta).getOwningPlayer();
                if (ofp != null)
                {
                    String ownerName = ofp.getName();
                    if (ownerName != null && !ownerName.isEmpty())
                    {
                        result.add(this.user.getTranslation("challenges.gui.item-description.skull-owner",
                                "[owner]", ownerName));
                    }
                }
            }
            else if (meta instanceof SpawnEggMeta)
            {
                result.add(this.user.getTranslation("challenges.gui.item-description.egg-meta",
                        "[mob]", LangUtilsHook.getEntityName(((SpawnEggMeta) meta).getSpawnedType(), user)));
            }
            else if (meta instanceof TropicalFishBucketMeta)
            {
                TropicalFishBucketMeta fishMeta = (TropicalFishBucketMeta) meta;

                // First try to use LangUtilsHook to get the predefined tropical
                // fish names so that the description looks like vanilla names.
                String predefined = LangUtilsHook.getPredefinedTropicalFishName(fishMeta, user);
                if (predefined != null)
                {
                    result.add(this.user.getTranslation("challenges.gui.item-description.predefined-fish",
                        "[fish-name]", predefined));
                }
                else if ((fishMeta).hasVariant())
                {
                    result.add(this.user.getTranslation("challenges.gui.item-description.fish-meta",
                        "[pattern]", LangUtilsHook.getTropicalFishTypeName(fishMeta.getPattern(), user),
                        "[pattern-color]", LangUtilsHook.getDyeColorName(fishMeta.getPatternColor(), user),
                        "[body-color]", LangUtilsHook.getDyeColorName(fishMeta.getBodyColor(), user)));
                }
            }
            else if (meta instanceof BannerMeta)
            {
                for (Pattern pattern : ((BannerMeta) meta).getPatterns())
                {
                    result.add(this.user.getTranslation("challenges.gui.item-description.banner-pattern",
                        "[pattern]", LangUtilsHook.getBannerPatternName(pattern, user)));
                }
            }

            if (meta.hasEnchants())
            {
                itemStack.getEnchantments().forEach((enchantment, level) -> {
                    result.add(this.user.getTranslation("challenges.gui.item-description.item-enchant",
                            "[enchant]",
                            LangUtilsHook.getEnchantName(enchantment, user),
                            "[level]",
                            LangUtilsHook.getEnchantLevelName(level, user)));
                });
            }
        }

        return result;
    }


    // ---------------------------------------------------------------------
    // Section: Chat Input Methods
    // ---------------------------------------------------------------------


    /**
     * This method will close opened gui and writes inputText in chat. After players answers on inputText in
     * chat, message will trigger consumer and gui will reopen.
     * @param consumer Consumer that accepts player output text.
     * @param question Message that will be displayed in chat when player triggers conversion.
     * @param message Message that will be set in player text field when clicked on question.
     */
    protected void getFriendlyName(Consumer<String> consumer, @NonNull String question, @Nullable String message)
    {
        final User user = this.user;

        Conversation conversation =
                new ConversationFactory(BentoBox.getInstance()).withFirstPrompt(
                        new StringPrompt()
                        {
                            /**
                             * @see Prompt#getPromptText(ConversationContext)
                             */
                            @Override
                            public String getPromptText(ConversationContext conversationContext)
                            {
                                // Close input GUI.
                                user.closeInventory();

                                if (message != null)
                                {
                                    // Create Edit Text message.
                                    TextComponent component = new TextComponent(user.getTranslation("challenges.gui.descriptions.admin.click-to-edit"));
                                    component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, message));
                                    // Send question and message to player.
                                    user.getPlayer().spigot().sendMessage(component);
                                }

                                // There are no editable message. Just return question.
                                return question;
                            }


                            /**
                             * @see Prompt#acceptInput(ConversationContext, String)
                             */
                            @Override
                            public Prompt acceptInput(ConversationContext conversationContext, String answer)
                            {
                                // Add answer to consumer.
                                consumer.accept(answer);
                                // End conversation
                                return Prompt.END_OF_CONVERSATION;
                            }
                        }).
                withLocalEcho(false).
                // On cancel conversation will be closed.
                withEscapeSequence("cancel").
                // Use null value in consumer to detect if user has abandoned conversation.
                addConversationAbandonedListener(abandonedEvent ->
                {
                    if (!abandonedEvent.gracefulExit())
                    {
                        consumer.accept(null);
                    }
                }).
                withPrefix(context -> user.getTranslation("challenges.gui.questions.prefix")).
                buildConversation(user.getPlayer());

        conversation.begin();
    }
}

