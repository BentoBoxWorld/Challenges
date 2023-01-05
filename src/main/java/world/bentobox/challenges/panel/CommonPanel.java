//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.challenges.panel;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.database.object.requirements.*;
import world.bentobox.challenges.managers.ChallengesManager;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.LevelStatus;
import world.bentobox.challenges.utils.Utils;


/**
 * This class contains common methods for all panels.
 */
public abstract class CommonPanel
{
    /**
     * This is default constructor for all classes that extends CommonPanel.
     *
     * @param addon ChallengesAddon instance.
     * @param user User who opens panel.
     */
    protected CommonPanel(ChallengesAddon addon, User user, World world, String topLabel, String permissionPrefix)
    {
        this.addon = addon;
        this.world = world;
        this.manager = addon.getChallengesManager();
        this.user = user;

        this.topLabel = topLabel;
        this.permissionPrefix = permissionPrefix;

        this.parentPanel = null;

        this.returnButton = new PanelItemBuilder().
            name(this.user.getTranslation(Constants.BUTTON + "quit.name")).
            description(this.user.getTranslationOrNothing(Constants.BUTTON + "quit.description")).
            description("").
            description(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-quit")).
            icon(Material.OAK_DOOR).
            clickHandler((panel, user1, clickType, i) -> {
                this.user.closeInventory();
                return true;
            }).build();
    }


    /**
     * This is default constructor for all classes that extends CommonPanel.
     *
     * @param parentPanel Parent panel of current panel.
     */
    protected CommonPanel(@NonNull CommonPanel parentPanel)
    {
        this.addon = parentPanel.addon;
        this.manager = parentPanel.manager;
        this.user = parentPanel.user;
        this.world = parentPanel.world;

        this.topLabel = parentPanel.topLabel;
        this.permissionPrefix = parentPanel.permissionPrefix;

        this.parentPanel = parentPanel;

        this.returnButton = new PanelItemBuilder().
            name(this.user.getTranslation(Constants.BUTTON + "return.name")).
            description(this.user.getTranslationOrNothing(Constants.BUTTON + "return.description")).
            description("").
            description(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-return")).
            icon(Material.OAK_DOOR).
            clickHandler((panel, user1, clickType, i) -> {
                this.parentPanel.build();
                return true;
            }).build();
    }


    /**
     * This method allows building panel.
     */
    protected abstract void build();


    /**
     * This method reopens given panel.
     * @param panel Panel that must be reopened.
     */
    public static void reopen(CommonPanel panel)
    {
        panel.build();
    }


// ---------------------------------------------------------------------
// Section: Common methods
// ---------------------------------------------------------------------


    /**
     * This method generates and returns given challenge description. It is used here to avoid multiple
     * duplicates, as it would be nice to have single place where challenge could be generated.
     * @param challenge Challenge which description must be generated.
     * @param target target player.
     * @return List of strings that will be used in challenges description.
     */
    protected List<String> generateChallengeDescription(Challenge challenge, @Nullable User target)
    {
        // Some values to avoid over checking.
        final boolean isCompletedOnce = target != null &&
            this.manager.isChallengeComplete(target.getUniqueId(), this.world, challenge);

        final long doneTimes = target != null && challenge.isRepeatable() ?
            this.manager.getChallengeTimes(target, this.world, challenge) : (isCompletedOnce ? 0 : 1);

        boolean isCompletedAll = isCompletedOnce &&
            (!challenge.isRepeatable() ||
                challenge.getMaxTimes() > 0 && doneTimes >= challenge.getMaxTimes());

        final String reference = Constants.DESCRIPTIONS + "challenge.";

        // Get description from custom translations
        String description = this.user.getTranslationOrNothing(
            "challenges.challenges."  + challenge.getUniqueId() + ".description");

        if (description.isEmpty())
        {
            // Get data from object in single string.
            description = Util.translateColorCodes(String.join("\n", challenge.getDescription()));
        }

        // Non-memory optimal code used for easier debugging and nicer code layout for my eye :)
        // Get status in single string
        String status = this.generateChallengeStatus(isCompletedOnce,
            isCompletedAll,
            doneTimes,
            challenge.getMaxTimes());
        // Get requirements in single string
        String requirements = isCompletedAll ? "" : this.generateRequirements(challenge, target);
        // Get rewards in single string
        String rewards = isCompletedAll ? "" : this.generateRewards(challenge, isCompletedOnce);
        // Get coolDown in singe string
        String coolDown = isCompletedAll || challenge.getTimeout() <= 0 ? "" :
            this.generateCoolDown(challenge, target);

        if (!description.replaceAll("(?m)^[ \\t]*\\r?\\n", "").isEmpty())
        {
            String returnString = this.user.getTranslationOrNothing(reference + "lore",
                "[requirements]", requirements,
                "[rewards]", rewards,
                "[status]", status,
                "[cooldown]", coolDown);

            // remove empty lines from the generated text.
            List<String> collect =
                Arrays.stream(returnString.replaceAll("(?m)^[ \\t]*\\r?\\n", "").
                        split("\n")).
                    collect(Collectors.toList());

            // find and replace description from collected blocks.

            for (int i = 0; i < collect.size(); i++)
            {
                if (collect.get(i).contains(Constants.PARAMETER_DESCRIPTION))
                {
                    collect.set(i, collect.get(i).replace(Constants.PARAMETER_DESCRIPTION, description));
                }
            }

            return collect;
        }
        else
        {
            String returnString = this.user.getTranslationOrNothing(reference + "lore",
                Constants.PARAMETER_DESCRIPTION, description,
                "[requirements]", requirements,
                "[rewards]", rewards,
                "[status]", status,
                "[cooldown]", coolDown);

            // Remove empty lines and returns as a list.

            return Arrays.stream(returnString.replaceAll("(?m)^[ \\t]*\\r?\\n", "").
                    split("\n")).
                collect(Collectors.toList());
        }
    }


    /**
     * Generate cool down string.
     *
     * @param challenge the challenge
     * @param target the target
     * @return the string
     */
    private String generateCoolDown(Challenge challenge, @Nullable User target)
    {
        final String reference = Constants.DESCRIPTIONS + "challenge.cooldown.";

        String coolDown;

        if (target != null && this.manager.isBreachingTimeOut(target, this.world, challenge))
        {
            long missing = this.manager.getLastCompletionDate(this.user, this.world, challenge) +
                challenge.getTimeout() - System.currentTimeMillis();

            coolDown = this.user.getTranslation(reference + "wait-time",
                "[time]",
                Utils.parseDuration(Duration.ofMillis(missing), this.user));
        }
        else
        {
            coolDown = "";
        }

        String timeout = this.user.getTranslation(reference + "timeout",
            "[time]",
            Utils.parseDuration(Duration.ofMillis(challenge.getTimeout()), this.user));

        return this.user.getTranslation(reference + "lore",
            "[timeout]", timeout,
            "[wait-time]", coolDown);
    }


    /**
     * This method generate requirements description for given challenge.
     * @param challenge Challenge which requirements must be generated.
     * @return Lore message with requirements.
     */
    private String generateRequirements(Challenge challenge, @Nullable User target)
    {
        final String reference = Constants.DESCRIPTIONS + "challenge.requirements.";

        String environment;

        if (challenge.getEnvironment().isEmpty() || challenge.getEnvironment().size() == 3)
        {
            // If challenge can be completed everywhere, do not display requirement.
            environment = "";
        }
        else if (challenge.getEnvironment().size() == 1)
        {
            environment = this.user.getTranslationOrNothing(reference + "environment-single",
                Constants.PARAMETER_ENVIRONMENT,
                Utils.prettifyObject(challenge.getEnvironment().iterator().next(), this.user));
        }
        else
        {
            StringBuilder builder = new StringBuilder();
            builder.append(this.user.getTranslationOrNothing(reference + "environment-title"));
            challenge.getEnvironment().stream().sorted().forEach(en ->
            {
                builder.append("\n");
                builder.append(this.user.getTranslationOrNothing(reference + "environment-single",
                    Constants.PARAMETER_ENVIRONMENT,
                    Utils.prettifyObject(en, this.user)));
            });

            environment = builder.toString();
        }

        String permissions;

        if (!challenge.getRequirements().getRequiredPermissions().isEmpty())
        {
            // Yes list duplication for complete menu.
            List<String> missingPermissions = challenge.getRequirements().getRequiredPermissions().stream().
                    filter(permission -> target == null || !target.hasPermission(permission)).
                    sorted().toList();

            StringBuilder permissionBuilder = new StringBuilder();

            if (missingPermissions.size() == 1)
            {
                permissionBuilder.append(this.user.getTranslationOrNothing(reference + "permission-single",
                    Constants.PARAMETER_PERMISSION, missingPermissions.get(0)));
            }
            else if (!missingPermissions.isEmpty())
            {
                permissionBuilder.append(this.user.getTranslationOrNothing(reference + "permissions-title"));
                missingPermissions.forEach(permission ->
                {
                    permissionBuilder.append("\n");
                    permissionBuilder.append(this.user.getTranslationOrNothing(reference + "permissions-list",
                        Constants.PARAMETER_PERMISSION, permission));
                });
            }

            permissions = permissionBuilder.toString();
        }
        else
        {
            permissions = "";
        }

        String typeRequirement = switch (challenge.getChallengeType()) {
            case INVENTORY_TYPE -> this.generateInventoryChallenge(challenge.getRequirements());
            case ISLAND_TYPE -> this.generateIslandChallenge(challenge.getRequirements());
            case OTHER_TYPE -> this.generateOtherChallenge(challenge.getRequirements());
            case STATISTIC_TYPE -> this.generateStatisticChallenge(challenge.getRequirements());
        };

        return this.user.getTranslationOrNothing(reference + "lore",
            Constants.PARAMETER_ENVIRONMENT, environment,
            "[type-requirement]", typeRequirement,
            "[permissions]", permissions);
    }


    /**
     * This method generates lore message for island requirement.
     * @param requirement Island Requirement.
     * @return Requirement lore message.
     */
    private String generateIslandChallenge(IslandRequirements requirement)
    {
        final String reference = Constants.DESCRIPTIONS + "challenge.requirements.island.";

        String blocks;

        if (!requirement.getRequiredBlocks().isEmpty())
        {
            StringBuilder builder = new StringBuilder();
            builder.append(this.user.getTranslationOrNothing(reference + "blocks-title"));
            requirement.getRequiredBlocks().entrySet().stream().
                sorted(Map.Entry.comparingByKey()).
                forEach(entry ->
                {
                    builder.append("\n");

                    if (entry.getValue() > 1)
                    {
                        builder.append(this.user.getTranslationOrNothing(reference + "blocks-value",
                            Constants.PARAMETER_NUMBER, String.valueOf(entry.getValue()),
                            Constants.PARAMETER_MATERIAL, Utils.prettifyObject(entry.getKey(), this.user)));
                    }
                    else
                    {
                        builder.append(this.user.getTranslationOrNothing(reference + "block-value",
                            Constants.PARAMETER_MATERIAL, Utils.prettifyObject(entry.getKey(), this.user)));
                    }
                });

            blocks = builder.toString();
        }
        else
        {
            blocks = "";
        }

        String entities;

        if (!requirement.getRequiredEntities().isEmpty())
        {
            StringBuilder builder = new StringBuilder();
            builder.append(this.user.getTranslationOrNothing(reference + "entities-title"));
            requirement.getRequiredEntities().entrySet().stream().
                sorted(Map.Entry.comparingByKey()).
                forEach(entry ->
                {
                    builder.append("\n");

                    if (entry.getValue() > 1)
                    {
                        builder.append(this.user.getTranslationOrNothing(reference + "entities-value",
                            Constants.PARAMETER_NUMBER, String.valueOf(entry.getValue()),
                            Constants.PARAMETER_ENTITY, Utils.prettifyObject(entry.getKey(), this.user)));
                    }
                    else
                    {
                        builder.append(this.user.getTranslationOrNothing(reference + "entity-value",
                            Constants.PARAMETER_ENTITY, Utils.prettifyObject(entry.getKey(), this.user)));
                    }
                });

            entities = builder.toString();
        }
        else
        {
            entities = "";
        }

        String searchRadius = this.user.getTranslationOrNothing(reference + "search-radius",
            Constants.PARAMETER_NUMBER, String.valueOf(requirement.getSearchRadius()));

        String warningBlocks = requirement.isRemoveBlocks() ?
            this.user.getTranslationOrNothing(reference + "warning-block") : "";
        String warningEntities = requirement.isRemoveEntities() ?
            this.user.getTranslationOrNothing(reference + "warning-entity") : "";

        return this.user.getTranslationOrNothing(reference + "lore",
            "[blocks]", blocks,
            "[entities]", entities,
            "[warning-block]", warningBlocks,
            "[warning-entity]", warningEntities,
            "[search-radius]", searchRadius);
    }


    /**
     * This method generates lore message for inventory requirement.
     * @param requirement Inventory Requirement.
     * @return Requirement lore message.
     */
    private String generateInventoryChallenge(InventoryRequirements requirement)
    {
        final String reference = Constants.DESCRIPTIONS + "challenge.requirements.inventory.";

        String items;

        if (!requirement.getRequiredItems().isEmpty())
        {
            StringBuilder builder = new StringBuilder();
            builder.append(this.user.getTranslationOrNothing(reference + "item-title"));
            Utils.groupEqualItems(requirement.getRequiredItems(), requirement.getIgnoreMetaData()).stream().
                sorted(Comparator.comparing(ItemStack::getType)).
                forEach(itemStack ->
                {
                    builder.append("\n");

                    if (itemStack.getAmount() > 1)
                    {
                        builder.append(this.user.getTranslationOrNothing(reference + "items-value",
                            "[number]", String.valueOf(itemStack.getAmount()),
                            "[item]", Utils.prettifyObject(itemStack, this.user)));
                    }
                    else
                    {
                        builder.append(this.user.getTranslationOrNothing(reference + "item-value",
                            "[item]", Utils.prettifyObject(itemStack, this.user)));
                    }
                });

            items = builder.toString();
        }
        else
        {
            items = "";
        }

        String warning = requirement.isTakeItems() ?
            this.user.getTranslationOrNothing(reference + "warning") : "";

        return this.user.getTranslationOrNothing(reference + "lore",
            "[items]", items,
            "[warning]", warning);
    }


    /**
     * This method generates lore message for other requirement.
     * @param requirement Other Requirement.
     * @return Requirement lore message.
     */
    private String generateOtherChallenge(OtherRequirements requirement)
    {
        final String reference = Constants.DESCRIPTIONS + "challenge.requirements.other.";

        String experience = requirement.getRequiredExperience() <= 0 ? "" :
            this.user.getTranslationOrNothing(reference + "experience",
                "[number]", String.valueOf(requirement.getRequiredExperience()));

        String experienceWarning = requirement.getRequiredExperience() > 0 && requirement.isTakeExperience() ?
            this.user.getTranslationOrNothing(reference + "experience-warning") : "";

        String money = !this.addon.isEconomyProvided() || requirement.getRequiredMoney() <= 0 ? "" :
            this.user.getTranslationOrNothing(reference + "money",
                "[number]", String.valueOf(requirement.getRequiredMoney()));

        String moneyWarning = this.addon.isEconomyProvided() &&
            requirement.getRequiredMoney() > 0 &&
            requirement.isTakeMoney() ?
            this.user.getTranslationOrNothing(reference + "money-warning") : "";

        String level = !this.addon.isLevelProvided() || requirement.getRequiredIslandLevel() <= 0 ? "" :
            this.user.getTranslationOrNothing(reference + "level",
                "[number]", String.valueOf(requirement.getRequiredIslandLevel()));

        return this.user.getTranslationOrNothing(reference + "lore",
            "[experience]", experience,
            "[experience-warning]", experienceWarning,
            "[money]", money,
            "[money-warning]", moneyWarning,
            "[level]", level);
    }


    /**
     * This method generates lore message for Statistic requirement.
     * @param requirement Statistic Requirement.
     * @return Requirement lore message.
     */
    private String generateStatisticChallenge(StatisticRequirements requirement)
    {
        final String reference = Constants.DESCRIPTIONS + "challenge.requirements.statistic.";

        String statistic;

        if (requirement.getStatistic() == null)
        {
            // Challenges by default comes with empty statistic field.
            return "";
        }

        switch (requirement.getStatistic().getType())
        {
            case UNTYPED -> statistic = this.user.getTranslationOrNothing(reference + "statistic",
                "[statistic]", Utils.prettifyObject(requirement.getStatistic(), this.user),
                "[number]", String.valueOf(requirement.getAmount()));
            case ITEM, BLOCK -> {
                if (requirement.getAmount() > 1)
                {
                    statistic = this.user.getTranslationOrNothing(reference + "multiple-target",
                        "[statistic]", Utils.prettifyObject(requirement.getStatistic(), this.user),
                        "[number]", String.valueOf(requirement.getAmount()),
                        "[target]", Utils.prettifyObject(requirement.getMaterial(), this.user));
                }
                else
                {
                    statistic = this.user.getTranslationOrNothing(reference + "single-target",
                        "[statistic]", Utils.prettifyObject(requirement.getStatistic(), this.user),
                        "[target]", Utils.prettifyObject(requirement.getMaterial(), this.user));
                }
            }
            case ENTITY -> {
                if (requirement.getAmount() > 1)
                {
                    statistic = this.user.getTranslationOrNothing(reference + "multiple-target",
                        "[statistic]", Utils.prettifyObject(requirement.getStatistic(), this.user),
                        "[number]", String.valueOf(requirement.getAmount()),
                        "[target]", Utils.prettifyObject(requirement.getEntity(), this.user));
                }
                else
                {
                    statistic = this.user.getTranslationOrNothing(reference + "single-target",
                        "[statistic]", Utils.prettifyObject(requirement.getStatistic(), this.user),
                        "[target]", Utils.prettifyObject(requirement.getEntity(), this.user));
                }
            }
            default -> statistic = "";
        }

        String warning = requirement.isReduceStatistic() ?
            this.user.getTranslationOrNothing(reference + "warning") : "";

        return this.user.getTranslationOrNothing(reference + "lore",
            "[statistic]", statistic,
            "[warning]", warning);
    }


    /**
     * This message generates challenge status description.
     * @param completedOnce Indicate that challenge is completed at least one time.
     * @param completedAll Indicate that challenge is not repeatable anymore.
     * @param completionCount Number of completion count.
     * @param maxCompletions Number of max completion count.
     * @return String with a text that will be generated for status.
     */
    private String generateChallengeStatus(boolean completedOnce,
        boolean completedAll,
        long completionCount,
        int maxCompletions)
    {
        final String reference = Constants.DESCRIPTIONS + "challenge.status.";

        if (completedAll)
        {
            if (maxCompletions > 1)
            {
                return this.user.getTranslationOrNothing(reference + "completed-times-reached",
                    Constants.PARAMETER_MAX, String.valueOf(maxCompletions));
            }
            else
            {
                return this.user.getTranslationOrNothing(reference + "completed");
            }
        }
        else if (completedOnce)
        {
            if (maxCompletions > 0)
            {
                return this.user.getTranslationOrNothing(reference + "completed-times-of",
                    Constants.PARAMETER_MAX, String.valueOf(maxCompletions),
                    Constants.PARAMETER_NUMBER, String.valueOf(completionCount));
            }
            else
            {
                return this.user.getTranslationOrNothing(reference + "completed-times",
                    Constants.PARAMETER_NUMBER, String.valueOf(completionCount));
            }
        }
        else
        {
            return "";
        }
    }


    /**
     * This method creates reward lore text.
     * @param challenge Challenge which reward lore must be generated.
     * @param isRepeating Boolean that indicate if it is repeating reward or first time.
     * @return Reward text.
     */
    private String generateRewards(Challenge challenge, boolean isRepeating)
    {
        if (isRepeating)
        {
            return this.generateRepeatReward(challenge);
        }
        else
        {
            return this.generateReward(challenge);
        }
    }


    /**
     * This method creates repeat reward lore text.
     * @param challenge Challenge which reward lore must be generated.
     * @return Reward text.
     */
    private String generateRepeatReward(Challenge challenge)
    {
        final String reference = Constants.DESCRIPTIONS + "challenge.rewards.";

        String items;

        if (!challenge.getRepeatItemReward().isEmpty())
        {
            StringBuilder builder = new StringBuilder();
            builder.append(this.user.getTranslationOrNothing(reference + "item-title"));
            Utils.groupEqualItems(challenge.getRepeatItemReward(), challenge.getIgnoreRewardMetaData()).stream().
                sorted(Comparator.comparing(ItemStack::getType)).
                forEach(itemStack ->
                {
                    builder.append("\n");

                    if (itemStack.getAmount() > 1)
                    {
                        builder.append(this.user.getTranslationOrNothing(reference + "items-value",
                            "[number]", String.valueOf(itemStack.getAmount()),
                            "[item]", Utils.prettifyObject(itemStack, this.user)));
                    }
                    else
                    {
                        builder.append(this.user.getTranslationOrNothing(reference + "item-value",
                            "[item]", Utils.prettifyObject(itemStack, this.user)));
                    }
                });

            items = builder.toString();
        }
        else
        {
            items = "";
        }

        String experience = challenge.getRepeatExperienceReward() <= 0 ? "" :
            this.user.getTranslationOrNothing(reference + "experience",
                "[number]", String.valueOf(challenge.getRepeatExperienceReward()));

        String money = !this.addon.isEconomyProvided() || challenge.getRepeatMoneyReward() <= 0 ? "" :
            this.user.getTranslationOrNothing(reference + "money",
                "[number]", String.valueOf(challenge.getRepeatMoneyReward()));

        String commands;

        if (!challenge.getRepeatRewardCommands().isEmpty())
        {
            StringBuilder permissionBuilder = new StringBuilder();

            if (!challenge.getRepeatRewardCommands().isEmpty())
            {
                permissionBuilder.append(this.user.getTranslationOrNothing(reference + "commands-title"));

                challenge.getRepeatRewardCommands().forEach(command ->
                {
                    permissionBuilder.append("\n");
                    permissionBuilder.append(this.user.getTranslationOrNothing(reference + "command",
                        "[command]", command));
                });
            }

            commands = permissionBuilder.toString();
        }
        else
        {
            commands = "";
        }

        if (challenge.getRepeatRewardText().isEmpty() &&
            items.isEmpty() &&
            experience.isEmpty() &&
            money.isEmpty() &&
            commands.isEmpty())
        {
            // If everything is empty, do not return anything.
            return "";
        }

        String rewardText = this.user.getTranslationOrNothing(
            "challenges.challenges."  + challenge.getUniqueId() + ".repeat-reward-text");

        if (rewardText.isEmpty())
        {
            rewardText = Util.translateColorCodes(String.join("\n", challenge.getRepeatRewardText()));
        }

        return this.user.getTranslationOrNothing(reference + "lore",
            "[text]", rewardText,
            "[items]", items,
            "[experience]", experience,
            "[money]", money,
            "[commands]", commands);
    }


    /**
     * This method creates reward lore text.
     * @param challenge Challenge which reward lore must be generated.
     * @return Reward text.
     */
    private String generateReward(Challenge challenge)
    {
        final String reference = Constants.DESCRIPTIONS + "challenge.rewards.";

        String items;

        if (!challenge.getRewardItems().isEmpty())
        {
            StringBuilder builder = new StringBuilder();
            builder.append(this.user.getTranslationOrNothing(reference + "item-title"));
            Utils.groupEqualItems(challenge.getRewardItems(), challenge.getIgnoreRewardMetaData()).stream().
                sorted(Comparator.comparing(ItemStack::getType)).
                forEach(itemStack ->
                {
                    builder.append("\n");

                    if (itemStack.getAmount() > 1)
                    {
                        builder.append(this.user.getTranslationOrNothing(reference + "items-value",
                            "[number]", String.valueOf(itemStack.getAmount()),
                            "[item]", Utils.prettifyObject(itemStack, this.user)));
                    }
                    else
                    {
                        builder.append(this.user.getTranslationOrNothing(reference + "item-value",
                            "[item]", Utils.prettifyObject(itemStack, this.user)));
                    }
                });

            items = builder.toString();
        }
        else
        {
            items = "";
        }

        String experience = challenge.getRewardExperience() <= 0 ? "" :
            this.user.getTranslationOrNothing(reference + "experience",
                "[number]", String.valueOf(challenge.getRewardExperience()));

        String money = !this.addon.isEconomyProvided() || challenge.getRewardMoney() <= 0 ? "" :
            this.user.getTranslationOrNothing(reference + "money",
                "[number]", String.valueOf(challenge.getRewardMoney()));

        String commands;

        if (!challenge.getRewardCommands().isEmpty())
        {
            StringBuilder permissionBuilder = new StringBuilder();

            if (!challenge.getRewardCommands().isEmpty())
            {
                permissionBuilder.append(this.user.getTranslationOrNothing(reference + "commands-title"));

                challenge.getRewardCommands().forEach(command ->
                {
                    permissionBuilder.append("\n");
                    permissionBuilder.append(this.user.getTranslationOrNothing(reference + "command",
                        "[command]", command));
                });
            }

            commands = permissionBuilder.toString();
        }
        else
        {
            commands = "";
        }

        if (challenge.getRewardText().isEmpty() &&
            items.isEmpty() &&
            experience.isEmpty() &&
            money.isEmpty() &&
            commands.isEmpty())
        {
            // If everything is empty, do not return anything.
            return "";
        }

        String rewardText = this.user.getTranslationOrNothing(
            "challenges.challenges."  + challenge.getUniqueId() + ".reward-text");

        if (rewardText.isEmpty())
        {
            rewardText = Util.translateColorCodes(String.join("\n", challenge.getRewardText()));
        }

        return this.user.getTranslationOrNothing(reference + "lore",
            "[text]", rewardText,
            "[items]", items,
            "[experience]", experience,
            "[money]", money,
            "[commands]", commands);
    }


    /**
     * This method generates level description string.
     * @param level Level which string must be generated.
     * @return List with generated description.
     */
    protected List<String> generateLevelDescription(ChallengeLevel level)
    {
        final String reference = Constants.DESCRIPTIONS + "level.";

        // Non-memory optimal code used for easier debugging and nicer code layout for my eye :)
        // Get status in single string
        String status = "";
        // Get requirements in single string
        String waiver = this.manager.isLastLevel(level, this.world) ? "" :
            this.user.getTranslationOrNothing(reference + "waiver",
                "[number]", String.valueOf(level.getWaiverAmount()));
        // Get rewards in single string
        String rewards = this.generateReward(level);

        String returnString = this.user.getTranslation(reference + "lore",
            "[text]", Util.translateColorCodes(level.getUnlockMessage()),
            "[waiver]", waiver,
            "[rewards]", rewards,
            "[status]", status);

        // Remove empty lines and returns as a list.

        return Arrays.stream(returnString.replaceAll("(?m)^[ \\t]*\\r?\\n", "").
                split("\n")).
            collect(Collectors.toList());
    }


    /**
     * This method generates level description string.
     * @param levelStatus Level which string must be generated.
     * @param user User who calls generation.
     * @return List with generated description.
     */
    protected List<String> generateLevelDescription(LevelStatus levelStatus, User user)
    {
        ChallengeLevel level = levelStatus.getLevel();

        final String reference = Constants.DESCRIPTIONS + "level.";

        // Non-memory optimal code used for easier debugging and nicer code layout for my eye :)
        // Get status in single string
        String status = this.generateLevelStatus(levelStatus);
        // Get requirements in single string
        String waiver = this.manager.isLastLevel(level, this.world) ||
            !levelStatus.isUnlocked() ||
            levelStatus.isComplete() ?
            "" : this.user.getTranslationOrNothing(reference + "waiver",
            "[number]", String.valueOf(level.getWaiverAmount()));
        // Get rewards in single string
        String rewards = !levelStatus.isUnlocked() ? "" : this.generateReward(level);

        String description = this.user.getTranslationOrNothing(
            "challenges.levels."  + level.getUniqueId() + ".description");

        if (description.isEmpty())
        {
            description = Util.translateColorCodes(String.join("\n", level.getUnlockMessage()));
        }

        String returnString = this.user.getTranslation(reference + "lore",
            "[text]", description,
            "[waiver]", waiver,
            "[rewards]", rewards,
           "[status]", status);

        // Remove empty lines and returns as a list.

        return Arrays.stream(returnString.replaceAll("(?m)^[ \\t]*\\r?\\n", "").
                split("\n")).
            collect(Collectors.toList());
    }


    /**
     * This method generates level status description.
     * @param levelStatus Level status which description must be generated.
     * @return Level status text.
     */
    private String generateLevelStatus(LevelStatus levelStatus)
    {
        final String reference = Constants.DESCRIPTIONS + "level.status.";

        if (!levelStatus.isUnlocked())
        {
            return this.user.getTranslationOrNothing(reference + "locked") + "\n" +
                this.user.getTranslationOrNothing(reference + "missing-challenges",
                    "[number]", String.valueOf(levelStatus.getNumberOfChallengesStillToDo()));
        }
        else if (levelStatus.isComplete())
        {
            return this.user.getTranslationOrNothing(reference + "completed");
        }
        else
        {
            ChallengeLevel level = levelStatus.getLevel();
            // Check if unlock message should appear.
            int doneChallenges = (int) level.getChallenges().stream().
                filter(challenge -> this.addon.getChallengesManager().isChallengeComplete(user.getUniqueId(), world, challenge)).
                count();

            return this.user.getTranslation(reference + "completed-challenges-of",
                "[number]", String.valueOf(doneChallenges),
                "[max]", String.valueOf(level.getChallenges().size()));
        }
    }


    /**
     * This method creates reward lore text.
     * @param level ChallengeLevel which reward lore must be generated.
     * @return Reward text.
     */
    private String generateReward(ChallengeLevel level)
    {
        final String reference = Constants.DESCRIPTIONS + "level.rewards.";

        String items;

        if (!level.getRewardItems().isEmpty())
        {
            StringBuilder builder = new StringBuilder();
            builder.append(this.user.getTranslationOrNothing(reference + "item-title"));
            Utils.groupEqualItems(level.getRewardItems(), level.getIgnoreRewardMetaData()).stream().
                sorted(Comparator.comparing(ItemStack::getType)).
                forEach(itemStack ->
                {
                    builder.append("\n");

                    if (itemStack.getAmount() > 1)
                    {
                        builder.append(this.user.getTranslationOrNothing(reference + "items-value",
                            "[number]", String.valueOf(itemStack.getAmount()),
                            "[item]", Utils.prettifyObject(itemStack, this.user)));
                    }
                    else
                    {
                        builder.append(this.user.getTranslationOrNothing(reference + "item-value",
                            "[item]", Utils.prettifyObject(itemStack, this.user)));
                    }
                });

            items = builder.toString();
        }
        else
        {
            items = "";
        }

        String experience = level.getRewardExperience() <= 0 ? "" :
            this.user.getTranslationOrNothing(reference + "experience",
                "[number]", String.valueOf(level.getRewardExperience()));

        String money = !this.addon.isEconomyProvided() || level.getRewardMoney() <= 0 ? "" :
            this.user.getTranslationOrNothing(reference + "money",
                "[number]", String.valueOf(level.getRewardMoney()));

        String commands;

        if (!level.getRewardCommands().isEmpty())
        {
            StringBuilder permissionBuilder = new StringBuilder();

            if (!level.getRewardCommands().isEmpty())
            {
                permissionBuilder.append(this.user.getTranslationOrNothing(reference + "commands-title"));

                level.getRewardCommands().forEach(command ->
                {
                    permissionBuilder.append("\n");
                    permissionBuilder.append(this.user.getTranslationOrNothing(reference + "command",
                        "[command]", command));
                });
            }

            commands = permissionBuilder.toString();
        }
        else
        {
            commands = "";
        }

        if (level.getRewardText().isEmpty() &&
            items.isEmpty() &&
            experience.isEmpty() &&
            money.isEmpty() &&
            commands.isEmpty())
        {
            // If everything is empty, do not return anything.
            return "";
        }

        String rewardText = this.user.getTranslationOrNothing(
            "challenges.levels."  + level.getUniqueId() + ".reward-text");

        if (rewardText.isEmpty())
        {
            rewardText = Util.translateColorCodes(String.join("\n", level.getRewardText()));
        }

        return this.user.getTranslationOrNothing(reference + "lore",
            "[text]", rewardText,
            "[items]", items,
            "[experience]", experience,
            "[money]", money,
            "[commands]", commands);
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * This variable stores parent gui.
     */
    @Nullable
    protected final CommonPanel parentPanel;

    /**
     * Variable stores Challenges addon.
     */
    protected final ChallengesAddon addon;

    /**
     * Variable stores Challenges addon manager.
     */
    protected final ChallengesManager manager;

    /**
     * Variable stores world in which panel is referred to.
     */
    protected final World world;

    /**
     * Variable stores user who created this panel.
     */
    protected final User user;

    /**
     * Variable stores top label of command from which panel was called.
     */
    protected final String topLabel;

    /**
     * Variable stores permission prefix of command from which panel was called.
     */
    protected final String permissionPrefix;

    /**
     * This object holds PanelItem that allows to return to previous panel.
     */
    protected PanelItem returnButton;
}
