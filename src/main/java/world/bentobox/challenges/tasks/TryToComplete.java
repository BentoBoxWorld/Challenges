package world.bentobox.challenges.tasks;



import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;

import com.google.common.collect.UnmodifiableIterator;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.Challenge.ChallengeType;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.database.object.requirements.CheckPapi;
import world.bentobox.challenges.database.object.requirements.InventoryRequirements;
import world.bentobox.challenges.database.object.requirements.IslandRequirements;
import world.bentobox.challenges.database.object.requirements.OtherRequirements;
import world.bentobox.challenges.database.object.requirements.StatisticRequirements;
import world.bentobox.challenges.database.object.requirements.StatisticRequirements.StatisticRec;
import world.bentobox.challenges.managers.ChallengesManager;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;


/**
 * Run when a user tries to complete a challenge
 * @author tastybento
 *
 */
public class TryToComplete
{
    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * Challenges addon variable.
     */
    private final ChallengesAddon addon;

    /**
     * Challenges manager for addon.
     */
    private ChallengesManager manager;

    /**
     * World where all checks are necessary.
     */
    private World world;

    /**
     * User who is completing challenge.
     */
    private User user;

    /**
     * Permission prefix string.
     */
    private String permissionPrefix;

    /**
     * Challenge that should be completed.
     */
    private Challenge challenge;

    /**
     * Variable that will be used to avoid multiple empty object generation.
     */
    private final ChallengeResult EMPTY_RESULT = new ChallengeResult();

    // ---------------------------------------------------------------------
    // Section: Builder
    // ---------------------------------------------------------------------


    @Deprecated
    public TryToComplete user(User user)
    {
        this.user = user;
        return this;
    }


    @Deprecated
    public TryToComplete(ChallengesAddon addon)
    {
        this.addon = addon;
    }


    // ---------------------------------------------------------------------
    // Section: Constructor
    // ---------------------------------------------------------------------


    /**
     * @param addon - Challenges Addon.
     * @param user - User who performs challenge.
     * @param challenge - Challenge that should be completed.
     * @param world - World where completion may occur.
     * @param topLabel - Label of the top command.
     * @param permissionPrefix - Permission prefix for GameMode addon.
     */
    public TryToComplete(ChallengesAddon addon,
            User user,
            Challenge challenge,
            World world,
            String topLabel,
            String permissionPrefix)
    {
        this.addon = addon;
        this.world = world;
        this.permissionPrefix = permissionPrefix;
        this.user = user;
        this.manager = addon.getChallengesManager();
        // To avoid any modifications that may occur to challenges in current completion
        // just clone it.
        this.challenge = challenge.clone();
    }


    /**
     * This static method allows complete challenge and get result about completion.
     * @param addon - Challenges Addon.
     * @param user - User who performs challenge.
     * @param challenge - Challenge that should be completed.
     * @param world - World where completion may occur.
     * @param topLabel - Label of the top command.
     * @param permissionPrefix - Permission prefix for GameMode addon.
     * @return true, if challenge is completed, otherwise false.
     */
    public static boolean complete(ChallengesAddon addon,
            User user,
            Challenge challenge,
            World world,
            String topLabel,
            String permissionPrefix)
    {
        return TryToComplete.complete(addon, user, challenge, world, topLabel, permissionPrefix, 1);
    }


    /**
     * This static method allows complete challenge and get result about completion.
     * @param addon - Challenges Addon.
     * @param user - User who performs challenge.
     * @param challenge - Challenge that should be completed.
     * @param world - World where completion may occur.
     * @param topLabel - Label of the top command.
     * @param permissionPrefix - Permission prefix for GameMode addon.
     * @param maxTimes - Integer that represents how many times user wants to complete challenges.
     * @return true, if challenge is completed, otherwise false.
     */
    public static boolean complete(ChallengesAddon addon,
            User user,
            Challenge challenge,
            World world,
            String topLabel,
            String permissionPrefix,
            int maxTimes)
    {
        return new TryToComplete(addon, user, challenge, world, topLabel, permissionPrefix).
                build(maxTimes).meetsRequirements;
    }


    // ---------------------------------------------------------------------
    // Section: Methods
    // ---------------------------------------------------------------------


    /**
     * This method checks if challenge can be done, and complete it, if it is possible.
     * @return ChallengeResult object, that contains completion status.
     */
    ChallengeResult build(int maxTimes)
    {
        // Check if can complete challenge
        ChallengeResult result = this.checkIfCanCompleteChallenge(maxTimes);

        if (!result.isMeetsRequirements())
        {
            return result;
        }

        if (this.user.getLocation() == null || this.user.getInventory() == null)
        {
            // This is just a cleaning check. There is no situations where location or inventory
            // could be null at this point of code.
            return result;
        }

        this.fullFillRequirements(result);

        // Validation to avoid rewarding if something goes wrong in removing requirements.

        if (!result.isMeetsRequirements())
        {
            if (result.removedItems != null)
            {
                result.removedItems.forEach((item, amount) ->
                {
                    ItemStack returnItem = item.clone();
                    returnItem.setAmount(amount);

                    this.user.getInventory().addItem(returnItem).forEach((k, v) ->
                    this.user.getWorld().dropItem(this.user.getLocation(), v));
                });
            }

            // Entities and blocks will not be restored.

            return result;
        }

        // If challenge was not completed then reward items for completing it first time.
        if (!result.wasCompleted())
        {
            // Item rewards
            for (ItemStack reward : this.challenge.getRewardItems())
            {
                // Clone is necessary because otherwise it will chane reward itemstack
                // amount.
                this.user.getInventory().addItem(reward.clone()).forEach((k, v) ->
                this.user.getWorld().dropItem(this.user.getLocation(), v));
            }

            // Money Reward
            if (this.addon.isEconomyProvided())
            {
                this.addon.getEconomyProvider().deposit(this.user, this.challenge.getRewardMoney());
            }

            // Experience Reward
            this.user.getPlayer().giveExp(this.challenge.getRewardExperience());

            // Run commands
            this.runCommands(this.challenge.getRewardCommands());

            // Send message about first completion only if it is completed only once.
            if (result.getFactor() == 1)
            {
                Utils.sendMessage(this.user, 
                        this.world, Constants.MESSAGES + "you-completed-challenge", Constants.PARAMETER_VALUE,
                        this.challenge.getFriendlyName());
            }

            if (this.addon.getChallengesSettings().isBroadcastMessages())
            {
                Bukkit.getOnlinePlayers().stream().
                map(User::getInstance).forEach(user -> Utils.sendMessage(user,
                        this.world,
                        Constants.MESSAGES + "name-has-completed-challenge",
                        Constants.PARAMETER_NAME, this.user.getName(),
                        Constants.PARAMETER_VALUE, this.challenge.getFriendlyName()));
            }

            // sends title to player on challenge completion
            if (this.addon.getChallengesSettings().isShowCompletionTitle())
            {
                this.user.getPlayer().sendTitle(
                        this.parseChallenge(this.user.getTranslation("challenges.titles.challenge-title"),
                                this.challenge),
                        this.parseChallenge(this.user.getTranslation("challenges.titles.challenge-subtitle"),
                                this.challenge),
                        10, this.addon.getChallengesSettings().getTitleShowtime(), 20);
            }
        }

        if (result.wasCompleted() || result.getFactor() > 1)
        {
            int rewardFactor = result.getFactor() - (result.wasCompleted() ? 0 : 1);

            // Item Repeat Rewards
            for (ItemStack reward : this.challenge.getRepeatItemReward())
            {
                // Clone is necessary because otherwise it will chane reward itemstack
                // amount.

                for (int i = 0; i < rewardFactor; i++)
                {
                    this.user.getInventory().addItem(reward.clone()).forEach((k, v) ->
                    this.user.getWorld().dropItem(this.user.getLocation(), v));
                }
            }

            // Money Repeat Reward
            if (this.addon.isEconomyProvided())
            {
                this.addon.getEconomyProvider().deposit(this.user,
                        this.challenge.getRepeatMoneyReward() * rewardFactor);
            }

            // Experience Repeat Reward
            this.user.getPlayer().giveExp(
                    this.challenge.getRepeatExperienceReward() * rewardFactor);

            // Run commands
            for (int i = 0; i < rewardFactor; i++)
            {
                this.runCommands(this.challenge.getRepeatRewardCommands());
            }

            if (result.getFactor() > 1)
            {
                Utils.sendMessage(this.user,
                        this.world, Constants.MESSAGES + "you-repeated-challenge-multiple", Constants.PARAMETER_VALUE,
                        this.challenge.getFriendlyName(), "[count]", Integer.toString(result.getFactor()));
            }
            else
            {
                Utils.sendMessage(this.user,
                        this.world, Constants.MESSAGES + "you-repeated-challenge", Constants.PARAMETER_VALUE,
                        this.challenge.getFriendlyName());
            }
        }

        // Mark as complete
        this.manager.setChallengeComplete(this.user, this.world, this.challenge, result.getFactor());

        // Check level completion for non-free challenges
        if (!result.wasCompleted() &&
                !this.challenge.getLevel().equals(ChallengesManager.FREE))
        {
            ChallengeLevel level = this.manager.getLevel(this.challenge);

            if (level != null && !this.manager.isLevelCompleted(this.user, this.world, level))
            {
                if (this.manager.validateLevelCompletion(this.user, this.world, level))
                {
                    // Item rewards
                    for (ItemStack reward : level.getRewardItems())
                    {
                        // Clone is necessary because otherwise it will chane reward itemstack
                        // amount.
                        this.user.getInventory().addItem(reward.clone()).forEach((k, v) ->
                        this.user.getWorld().dropItem(this.user.getLocation(), v));
                    }

                    // Money Reward
                    if (this.addon.isEconomyProvided())
                    {
                        this.addon.getEconomyProvider().deposit(this.user, level.getRewardMoney());
                    }

                    // Experience Reward
                    this.user.getPlayer().giveExp(level.getRewardExperience());

                    // Run commands
                    this.runCommands(level.getRewardCommands());

                    Utils.sendMessage(this.user,
                            this.world, Constants.MESSAGES + "you-completed-level", Constants.PARAMETER_VALUE,
                            level.getFriendlyName());

                    if (this.addon.getChallengesSettings().isBroadcastMessages())
                    {
                        Bukkit.getOnlinePlayers().stream().
                        map(User::getInstance).forEach(user -> Utils.sendMessage(user,
                                this.world,
                                Constants.MESSAGES + "name-has-completed-level",
                                Constants.PARAMETER_NAME, this.user.getName(),
                                Constants.PARAMETER_VALUE, level.getFriendlyName()));
                    }

                    this.manager.setLevelComplete(this.user, this.world, level);

                    // sends title to player on level completion
                    if (this.addon.getChallengesSettings().isShowCompletionTitle())
                    {
                        this.user.getPlayer().sendTitle(
                                this.parseLevel(this.user.getTranslation("challenges.titles.level-title"), level),
                                this.parseLevel(this.user.getTranslation("challenges.titles.level-subtitle"), level),
                                10, this.addon.getChallengesSettings().getTitleShowtime(), 20);
                    }
                }
            }
        }

        return result;
    }


    /**
     * This method fulfills all challenge type requirements, that is not fulfilled yet.
     * @param result Challenge Results
     */
    private void fullFillRequirements(ChallengeResult result)
    {
        switch (this.challenge.getChallengeType())
        {
        case ISLAND_TYPE -> {
            IslandRequirements requirements = this.challenge.getRequirements();

            if (result.meetsRequirements &&
                    requirements.isRemoveEntities() &&
                    !requirements.getRequiredEntities().isEmpty())
            {
                this.removeEntities(result.entities, result.getFactor());
            }

            if (result.meetsRequirements &&
                    requirements.isRemoveBlocks() &&
                    !requirements.getRequiredBlocks().isEmpty())
            {
                this.removeBlocks(result.blocks, result.getFactor());
            }
        }
        case INVENTORY_TYPE -> {
            // If remove items, then remove them
            if (this.getInventoryRequirements().isTakeItems()) {
                int sumEverything = result.requiredItems.stream().
                        mapToInt(itemStack -> itemStack.getAmount() * result.getFactor()).
                        sum();

                Map<ItemStack, Integer> removedItems =
                        this.removeItems(result.requiredItems, result.getFactor());

                int removedAmount = removedItems.values().stream().mapToInt(num -> num).sum();

                // Something is not removed.
                if (sumEverything != removedAmount) {
                    Utils.sendMessage(this.user,
                            this.world,
                            Constants.ERRORS + "cannot-remove-items");

                    result.removedItems = removedItems;
                    result.meetsRequirements = false;
                }
            }
        }
        case OTHER_TYPE -> {
            OtherRequirements requirements = this.challenge.getRequirements();

            if (this.addon.isEconomyProvided() && requirements.isTakeMoney()) {
                this.addon.getEconomyProvider().withdraw(this.user, requirements.getRequiredMoney());
            }

            if (requirements.isTakeExperience() && this.user.getPlayer().getGameMode() != GameMode.CREATIVE) {
                // Cannot take anything from creative game mode.
                this.user.getPlayer().setTotalExperience(
                        this.user.getPlayer().getTotalExperience() - requirements.getRequiredExperience());
            }
        }
        case STATISTIC_TYPE -> {
            StatisticRequirements requirements = this.challenge.getRequirements();
            for (StatisticRec s : requirements.getRequiredStatistics()) {
                if (s.reduceStatistic() && s.statistic() != null) {
                    int removeAmount = result.getFactor() * s.amount();

                    // Start to remove from player who called the completion.
                    switch (s.statistic().getType())
                    {
                    case UNTYPED -> {
                        int statistic = this.user.getPlayer().getStatistic(s.statistic());

                        if (removeAmount >= statistic) {
                            this.user.getPlayer().setStatistic(s.statistic(), 0);
                            removeAmount -= statistic;
                        } else {
                            this.user.getPlayer().setStatistic(s.statistic(), statistic - removeAmount);
                            removeAmount = 0;
                        }
                    }
                    case ITEM, BLOCK -> {
                        if (s.material() == null) {
                            // Just a sanity check. Material cannot be null at this point of code.
                            removeAmount = 0;
                        } else {
                            int statistic = this.user.getPlayer().getStatistic(s.statistic(), s.material());

                            if (removeAmount >= statistic) {
                                this.user.getPlayer().setStatistic(s.statistic(), s.material(), 0);
                                removeAmount -= statistic;
                            } else {
                                this.user.getPlayer().setStatistic(s.statistic(), s.material(),
                                        statistic - removeAmount);
                                removeAmount = 0;
                            }
                        }
                    }
                    case ENTITY -> {
                        if (s.entity() == null) {
                            // Just a sanity check. Entity cannot be null at this point of code.
                            removeAmount = 0;
                        } else {
                            int statistic = this.user.getPlayer().getStatistic(s.statistic(), s.entity());

                            if (removeAmount >= statistic) {
                                this.user.getPlayer().setStatistic(s.statistic(), s.entity(), 0);
                                removeAmount -= statistic;
                            } else {
                                this.user.getPlayer().setStatistic(s.statistic(), s.entity(), statistic - removeAmount);
                                removeAmount = 0;
                            }
                        }
                    }
                    }

                    // If challenges are in sync with all island members, then punish others too.
                    if (this.addon.getChallengesSettings().isStoreAsIslandData())
                    {
                        Island island = this.addon.getIslands().getIsland(this.world, this.user);

                        if (island == null) {
                            // hmm
                            return;
                        }

                        for (UnmodifiableIterator<UUID> iterator = island.getMemberSet().iterator(); iterator.hasNext()
                                && removeAmount > 0;) {
                            Player player = Bukkit.getPlayer(iterator.next());

                            if (player == null || player == this.user.getPlayer()) {
                                // cannot punish null or player who already was punished.
                                continue;
                            }

                            switch (Objects.requireNonNull(s.statistic()).getType()) {
                            case UNTYPED -> {
                                int statistic = player.getStatistic(s.statistic());

                                if (removeAmount >= statistic)
                                {
                                    removeAmount -= statistic;
                                    player.setStatistic(s.statistic(), 0);
                                }
                                else
                                {
                                    player.setStatistic(s.statistic(), statistic - removeAmount);
                                    removeAmount = 0;
                                }
                            }
                            case ITEM, BLOCK -> {
                                if (s.material() == null) {
                                    // Just a sanity check. Entity cannot be null at this point of code.
                                    removeAmount = 0;
                                } else {
                                    int statistic = player.getStatistic(s.statistic(), s.material());

                                    if (removeAmount >= statistic) {
                                        removeAmount -= statistic;
                                        player.setStatistic(s.statistic(), s.material(), 0);
                                    } else {
                                        player.setStatistic(s.statistic(), s.material(), statistic - removeAmount);
                                        removeAmount = 0;
                                    }
                                }
                            }
                            case ENTITY -> {
                                if (s.entity() == null)
                                {
                                    // Just a sanity check. Entity cannot be null at this point of code.
                                    removeAmount = 0;
                                }
                                else
                                {
                                    int statistic = player.getStatistic(s.statistic(), s.entity());

                                    if (removeAmount >= statistic) {
                                        removeAmount -= statistic;
                                        player.setStatistic(s.statistic(), s.entity(), 0);
                                    } else {
                                        player.setStatistic(s.statistic(), s.entity(), statistic - removeAmount);
                                        removeAmount = 0;
                                    }
                                }
                            }
                            }
                        }
                    }
                }
            }
        }
        }
    }


    /**
     * Checks if a challenge can be completed or not
     * It returns ChallengeResult.
     * @param maxTimes - times that user wanted to complete
     */
    private ChallengeResult checkIfCanCompleteChallenge(int maxTimes)
    {
        ChallengeResult result;

        ChallengeType type = this.challenge.getChallengeType();
        // Check the world
        if (!this.challenge.isDeployed())
        {
            Utils.sendMessage(this.user, this.world, Constants.ERRORS + "not-deployed");
            result = EMPTY_RESULT;
        }
        else if (maxTimes < 1)
        {
            Utils.sendMessage(this.user, this.world, Constants.ERRORS + "not-valid-integer");
            result = EMPTY_RESULT;
        }
        else if (Util.getWorld(this.world) != Util.getWorld(this.user.getWorld()) ||
                !this.challenge.matchGameMode(Utils.getGameMode(this.world)))
        {
            Utils.sendMessage(this.user, this.world, "general.errors.wrong-world");
            result = EMPTY_RESULT;
        }
        // Player is not on island
        else if (this.user.getLocation() == null ||
                ChallengesAddon.CHALLENGES_WORLD_PROTECTION.isSetForWorld(this.world) &&
                !this.addon.getIslands().locationIsOnIsland(this.user.getPlayer(), this.user.getLocation()))
        {
            Utils.sendMessage(this.user, this.world, Constants.MESSAGES + "not-on-island");
            result = EMPTY_RESULT;
        }
        // Check player permission
        else if (!this.addon.getIslands().getIslandAt(this.user.getLocation()).
                map(i -> i.isAllowed(this.user, ChallengesAddon.CHALLENGES_ISLAND_PROTECTION)).orElse(false))
        {
            Utils.sendMessage(this.user, this.world, Constants.MESSAGES + "no-rank");
            result = EMPTY_RESULT;
        }
        // Check if user has unlocked challenges level.
        else if (!this.challenge.getLevel().equals(ChallengesManager.FREE) &&
                !this.manager.isLevelUnlocked(this.user, this.world, this.manager.getLevel(this.challenge.getLevel())))
        {
            Utils.sendMessage(this.user, this.world, Constants.ERRORS + "challenge-level-not-available");
            result = EMPTY_RESULT;
        }
        // Check max times
        else if (this.challenge.isRepeatable() && this.challenge.getMaxTimes() > 0 &&
                this.manager.getChallengeTimes(this.user, this.world, this.challenge) >= this.challenge.getMaxTimes())
        {
            Utils.sendMessage(this.user, this.world, Constants.ERRORS + "not-repeatable");
            result = EMPTY_RESULT;
        }
        // Check repeatability
        else if (!this.challenge.isRepeatable() && this.manager.isChallengeComplete(this.user, this.world, this.challenge))
        {
            Utils.sendMessage(this.user, this.world, Constants.ERRORS + "not-repeatable");
            result = EMPTY_RESULT;
        }
        // Check if timeout is not broken
        else if (this.manager.isBreachingTimeOut(this.user, this.world, this.challenge))
        {
            long missing = this.manager.getLastCompletionDate(this.user, this.world, challenge) +
                    this.challenge.getTimeout() - System.currentTimeMillis();

            Utils.sendMessage(this.user, this.world, Constants.ERRORS + "timeout",
                    "[timeout]", Utils.parseDuration(Duration.ofMillis(this.challenge.getTimeout()), this.user),
                    "[wait-time]", Utils.parseDuration(Duration.ofMillis(missing), this.user));
            result = EMPTY_RESULT;
        }
        // Check environment
        else if (!this.challenge.getEnvironment().isEmpty() &&
                !this.challenge.getEnvironment().contains(this.user.getWorld().getEnvironment()))
        {
            Utils.sendMessage(this.user, this.world, Constants.ERRORS + "wrong-environment");
            result = EMPTY_RESULT;
        }
        // Check permission
        else if (!this.checkPermissions())
        {
            Utils.sendMessage(this.user, this.world, Constants.ERRORS + "no-permission");
            result = EMPTY_RESULT;
        }
        else if (type.equals(ChallengeType.INVENTORY_TYPE))
        {
            result = this.checkInventory(this.getAvailableCompletionTimes(maxTimes));
        }
        else if (type.equals(ChallengeType.ISLAND_TYPE))
        {
            result = this.checkSurrounding(this.getAvailableCompletionTimes(maxTimes));
        }
        else if (type.equals(ChallengeType.OTHER_TYPE))
        {
            result = this.checkOthers(this.getAvailableCompletionTimes(maxTimes));
        }
        else if (type.equals(ChallengeType.STATISTIC_TYPE))
        {
            result = this.checkStatistic(this.getAvailableCompletionTimes(maxTimes));
        }
        else
        {
            result = EMPTY_RESULT;
        }

        // Mark if challenge is completed.
        if (result.isMeetsRequirements())
        {
            result.setCompleted(this.manager.isChallengeComplete(this.user, this.world, this.challenge));
        }
        // Everything fails till this point.
        return result;
    }


    /**
     * This method checks if user has all required permissions.
     * @return true if user has all required permissions, otherwise false.
     */
    private boolean checkPermissions()
    {
        return this.challenge.getRequirements().getRequiredPermissions().isEmpty() ||
                this.challenge.getRequirements().getRequiredPermissions().stream()
                .allMatch(s -> this.user.hasPermission(s));
    }


    /**
     * This method checks if it is possible to complete maxTimes current challenge by
     * challenge constraints and already completed times.
     * @param vantedTimes How many times user wants to complete challenge
     * @return how many times user is able complete challenge by its constraints.
     */
    private int getAvailableCompletionTimes(int vantedTimes)
    {
        if (!this.challenge.isRepeatable() || this.challenge.getTimeout() > 0)
        {
            // Challenge is not repeatable
            vantedTimes = 1;
        }
        else if (this.challenge.getMaxTimes() > 0)
        {
            // Challenge has limitations
            long availableTimes = this.challenge.getMaxTimes() - this.manager.getChallengeTimes(this.user, this.world, this.challenge);

            if (availableTimes < vantedTimes)
            {
                vantedTimes = (int) availableTimes;
            }
        }

        return vantedTimes;
    }


    /**
     * This method runs all commands from command list.
     * @param commands List of commands that must be performed.
     */
    private void runCommands(List<String> commands)
    {
        // Ignore commands with this perm
        if (this.user.hasPermission(this.permissionPrefix + "command.challengeexempt") && !this.user.isOp())
        {
            return;
        }

        final Island island = this.addon.getIslandsManager().getIsland(this.world, this.user);
        final String owner = island == null ? "" : this.addon.getPlayers().getName(island.getOwner());

        for (String cmd : commands)
        {
            if (cmd.startsWith("[SELF]"))
            {
                String alert = "Running command '" + cmd + "' as " + this.user.getName();
                this.addon.getLogger().info(alert);
                cmd = cmd.substring(6).
                        replaceAll(Constants.ESC + Constants.PARAMETER_PLAYER, this.user.getName())
                        .replaceAll(Constants.ESC + Constants.PARAMETER_OWNER, owner)
                        .replaceAll(Constants.ESC + Constants.PARAMETER_NAME,
                                island == null || island.getName() == null ? "" : island.getName())
                        .trim();
                try
                {
                    if (!user.performCommand(cmd))
                    {
                        this.showError(cmd);
                    }
                }
                catch (Exception e)
                {
                    this.showError(cmd);
                }

                continue;
            }

            // Substitute in any references to player

            try
            {
                cmd = cmd.replaceAll(Constants.ESC + Constants.PARAMETER_PLAYER, this.user.getName()).
                        replaceAll(Constants.ESC + Constants.PARAMETER_OWNER, owner)
                        .replaceAll(Constants.ESC + Constants.PARAMETER_NAME,
                                island == null || island.getName() == null ? "" : island.getName())
                        .trim();

                if (!this.addon.getServer().dispatchCommand(this.addon.getServer().getConsoleSender(), cmd))
                {
                    this.showError(cmd);
                }
            }
            catch (Exception e)
            {
                this.showError(cmd);
            }
        }
    }


    /**
     * Throws error message.
     * @param cmd Error message that appear after failing some command.
     */
    private void showError(final String cmd)
    {
        this.addon.getLogger().severe("Problem executing command executed by player - skipping!");
        this.addon.getLogger().severe(() -> "Command was : " + cmd);
    }


    // ---------------------------------------------------------------------
    // Section: Inventory Challenge
    // ---------------------------------------------------------------------


    /**
     * Checks if a inventory challenge can be completed or not
     * It returns ChallengeResult.
     * @param maxTimes - times that user wanted to complete
     */
    private ChallengeResult checkInventory(int maxTimes)
    {
        if (maxTimes <= 0)
        {
            return EMPTY_RESULT;
        }

        // Run through inventory
        List<ItemStack> requiredItems;

        // Players in creative game mode has got all items. No point to search for them.
        if (this.user.getPlayer().getGameMode() != GameMode.CREATIVE)
        {
            requiredItems = Utils.groupEqualItems(this.getInventoryRequirements().getRequiredItems(),
                    this.getInventoryRequirements().getIgnoreMetaData());

            // Check if all required items are in players inventory.
            for (ItemStack required : requiredItems)
            {
                int numInInventory;

                if (this.getInventoryRequirements().getIgnoreMetaData().contains(required.getType()))
                {
                    numInInventory = Arrays.stream(this.user.getInventory().getContents()).
                            filter(Objects::nonNull).filter(i -> i.getType().equals(required.getType()))
                            .mapToInt(ItemStack::getAmount).sum();
                }
                else
                {
                    numInInventory = Arrays.stream(this.user.getInventory().getContents()).
                            filter(Objects::nonNull).filter(i -> i.isSimilar(required)).mapToInt(ItemStack::getAmount)
                            .sum();
                }

                if (numInInventory < required.getAmount())
                {
                    Utils.sendMessage(this.user, this.world, Constants.ERRORS + "not-enough-items",
                            "[items]", Utils.prettifyObject(required, this.user));
                    return EMPTY_RESULT;
                }

                maxTimes = Math.min(maxTimes, numInInventory / required.getAmount());
            }
        }
        else
        {
            requiredItems = Collections.emptyList();
            // Set maxTime to 2, to not crash client when completing 2147483647 times.
            maxTimes = 2;
        }

        // Return the result
        return new ChallengeResult().
                setMeetsRequirements().
                setCompleteFactor(maxTimes).
                setRequiredItems(requiredItems);
    }


    /**
     * Removes items from a user's inventory
     * @param requiredItemList - a list of item stacks to be removed
     * @param factor - factor for required items.
     */
    Map<ItemStack, Integer> removeItems(List<ItemStack> requiredItemList, int factor)
    {
        Map<ItemStack, Integer> removed = new HashMap<>();

        for (ItemStack required : requiredItemList)
        {
            int amountToBeRemoved = required.getAmount() * factor;
            List<ItemStack> itemsInInventory;

            if (this.user.getInventory() == null)
            {
                // Sanity check. User always has inventory at this point of code.
                itemsInInventory = Collections.emptyList();
            }
            else if (this.getInventoryRequirements().getIgnoreMetaData().contains(required.getType()))
            {
                // Use collecting method that ignores item meta.
                itemsInInventory = Arrays.stream(user.getInventory().getContents()).
                        filter(Objects::nonNull).filter(i -> i.getType().equals(required.getType()))
                        .collect(Collectors.toList());
            }
            else
            {
                // Use collecting method that compares item meta.
                itemsInInventory = Arrays.stream(user.getInventory().getContents()).
                        filter(Objects::nonNull).filter(i -> i.isSimilar(required)).collect(Collectors.toList());
            }

            for (ItemStack itemStack : itemsInInventory)
            {
                if (amountToBeRemoved > 0)
                {
                    ItemStack dummy = itemStack.clone();
                    dummy.setAmount(1);

                    // Remove either the full amount or the remaining amount
                    if (itemStack.getAmount() >= amountToBeRemoved)
                    {
                        itemStack.setAmount(itemStack.getAmount() - amountToBeRemoved);
                        removed.merge(dummy, amountToBeRemoved, Integer::sum);
                        amountToBeRemoved = 0;
                    }
                    else
                    {
                        removed.merge(dummy, itemStack.getAmount(), Integer::sum);
                        amountToBeRemoved -= itemStack.getAmount();
                        itemStack.setAmount(0);
                    }
                }
            }

            if (amountToBeRemoved > 0)
            {
                this.addon.logError("Could not remove " + amountToBeRemoved + " of " + required.getType() +
                        " from player's inventory!");
            }
        }

        return removed;
    }


    // ---------------------------------------------------------------------
    // Section: Island Challenge
    // ---------------------------------------------------------------------


    /**
     * Checks if a island challenge can be completed or not
     * It returns ChallengeResult.
     * @param factor - times that user wanted to complete
     */
    private ChallengeResult checkSurrounding(int factor)
    {
        if (factor <= 0)
        {
            return EMPTY_RESULT;
        }

        // Init location in player position.
        BoundingBox boundingBox = this.user.getPlayer().getBoundingBox().clone();

        // Expand position with search radius. Unless someone sets search radius larger than island
        // range. In this situation use island range.
        int distance = this.addon.getPlugin().getIWM().getIslandDistance(this.world);

        IslandRequirements requirements = this.challenge.getRequirements();

        if (requirements.getSearchRadius() < distance + 1)
        {
            distance = requirements.getSearchRadius();
        }

        boundingBox.expand(distance);

        if (ChallengesAddon.CHALLENGES_WORLD_PROTECTION.isSetForWorld(this.world))
        {
            // Players should not be able to complete challenge if they stay near island with required blocks.

            Island island = this.addon.getIslands().getIsland(this.world, this.user);

            if (island == null) {
                // Just in case. Should never hit because there is a check if the player is on this island further up
                return EMPTY_RESULT;
            }

            if (boundingBox.getMinX() < island.getMinX())
            {
                boundingBox.expand(BlockFace.EAST, Math.abs(island.getMinX() - boundingBox.getMinX()));
            }

            if (boundingBox.getMinZ() < island.getMinZ())
            {
                boundingBox.expand(BlockFace.NORTH, Math.abs(island.getMinZ() - boundingBox.getMinZ()));
            }

            int range = island.getRange();

            if (boundingBox.getMaxX() > island.getMaxX())
            {
                boundingBox.expand(BlockFace.WEST, Math.abs(boundingBox.getMaxX() - island.getMaxX()));
            }

            if (boundingBox.getMaxZ() > island.getMaxZ())
            {
                boundingBox.expand(BlockFace.SOUTH, Math.abs(boundingBox.getMaxZ() - island.getMaxZ()));
            }

            // Protection code. Do not allow to select too large region for completing challenge.
            if (boundingBox.getWidthX() > distance * 2 + 3 ||
                    boundingBox.getWidthZ() > distance * 2 + 3 || boundingBox.getHeight() > distance * 2 + 3)
            {
                this.addon.logError("BoundingBox is larger than SearchRadius. " +
                        " | BoundingBox: " + boundingBox +
                        " | Search Distance: " + requirements.getSearchRadius() +
                        " | Location: " + this.user.getLocation() +
                        " | Center: " + island.getCenter() +
                        " | Range: " + range);

                return EMPTY_RESULT;
            }
        }

        ChallengeResult result = this.searchForEntities(requirements.getRequiredEntities(), factor, boundingBox);

        // For Material tags
        if (result.isMeetsRequirements() && !requirements.getRequiredMaterialTags().isEmpty()) {
            result = searchForTags(requirements.getRequiredMaterialTags(), factor, boundingBox, Tag::isTagged,
                    (world, x, y, z) -> world.getBlockAt(x, y, z).getType());
        }

        // For EntityType tags
        if (result.isMeetsRequirements() && !requirements.getRequiredEntityTypeTags().isEmpty()) {
            result = searchForTags(requirements.getRequiredEntityTypeTags(), factor, boundingBox, Tag::isTagged,
                    (world, x, y, z) -> {
                        Collection<Entity> entities = world.getNearbyEntities(new Location(world, x, y, z), 1, 1, 1);
                        return entities.isEmpty() ? null : entities.iterator().next().getType();
                    });
        }
        if (result.isMeetsRequirements() && !requirements.getRequiredBlocks().isEmpty())
        {
            // Search for items only if entities found
            result = this.searchForBlocks(requirements.getRequiredBlocks(), result.getFactor(), boundingBox);
        }


        return result;
    }

    /**
     * Generic method to search for required tags in given challenge boundingBox.
     * @param <T> The type parameter for the Tag (must extend Keyed)
     * @param requiredMap Required Tag Map
     * @param factor Requirement multiplier
     * @param boundingBox Bounding box of island challenge
     * @param typeChecker Function to check if an element matches the tag
     * @param elementGetter Function to get the element at a specific location
     * @return ChallengeResult
     */
    private <T extends Keyed> ChallengeResult searchForTags(Map<Tag<T>, Integer> requiredMap, int factor,
            BoundingBox boundingBox, BiPredicate<Tag<T>, T> typeChecker, LocationElementGetter<T> elementGetter) {

        if (requiredMap.isEmpty()) {
            return new ChallengeResult().setMeetsRequirements().setCompleteFactor(factor);
        }

        Map<Tag<T>, Integer> tags = new HashMap<>(requiredMap);
        Map<Tag<T>, Integer> tagsFound = new HashMap<>(requiredMap.size());

        Queue<Block> blockQueue = new PriorityQueue<>((o1, o2) -> {
            if (this.user.getLocation() != null) {
                return Double.compare(o1.getLocation().distance(this.user.getLocation()),
                        o2.getLocation().distance(this.user.getLocation()));
            } else {
                return 0;
            }
        });

        for (int x = (int) boundingBox.getMinX(); x <= boundingBox.getMaxX(); x++) {
            for (int y = (int) boundingBox.getMinY(); y <= boundingBox.getMaxY(); y++) {
                for (int z = (int) boundingBox.getMinZ(); z <= boundingBox.getMaxZ(); z++) {
                    T element = elementGetter.getElement(this.user.getWorld(), x, y, z);
                    if (element == null)
                        continue;

                    for (Entry<Tag<T>, Integer> en : requiredMap.entrySet()) {
                        if (typeChecker.test(en.getKey(), element)) {
                            Block block = this.user.getWorld().getBlockAt(x, y, z);
                            blockQueue.add(block);
                            tagsFound.putIfAbsent(en.getKey(), 1);
                            tagsFound.computeIfPresent(en.getKey(), (k, v) -> v + 1);
                            // Remove one
                            tags.computeIfPresent(en.getKey(), (k, v) -> v - 1);
                            // Remove any that have an amount of 0
                            tags.entrySet().removeIf(e -> e.getValue() <= 0);

                            if (tags.isEmpty() && factor == 1) {
                                return new ChallengeResult().setMeetsRequirements().setCompleteFactor(factor)
                                        .setBlockQueue(blockQueue);
                            }
                        }
                    }
                }
            }
        }

        if (tags.isEmpty()) {
            if (factor > 1) {
                // Calculate minimal completion count
                for (Map.Entry<Tag<T>, Integer> entry : tagsFound.entrySet()) {
                    factor = Math.min(factor, entry.getValue() / requiredMap.get(entry.getKey()));
                }
            }

            tagsFound.clear();
            return new ChallengeResult().setMeetsRequirements().setCompleteFactor(factor).setBlockQueue(blockQueue);
        }

        Utils.sendMessage(this.user, this.world, Constants.ERRORS + "not-close-enough", Constants.PARAMETER_NUMBER,
                String.valueOf(this.getIslandRequirements().getSearchRadius()));

        tags.forEach((k, v) -> Utils.sendMessage(this.user, this.world, Constants.ERRORS + "you-still-need", "[amount]",
                String.valueOf(v), "[item]", Utils.prettifyObject(k, this.user)));

        // kick garbage collector
        tags.clear();
        tagsFound.clear();
        blockQueue.clear();

        return EMPTY_RESULT;
    }

    // Interface to get elements at a specific location
    @FunctionalInterface
    private interface LocationElementGetter<T extends Keyed> {
        T getElement(World world, int x, int y, int z);
    }

    /**
     * This method search required blocks in given challenge boundingBox.
     * @param requiredMap RequiredBlock Map.
     * @param factor - requirement multilayer.
     * @param boundingBox Bounding box of island challenge
     * @return ChallengeResult
     */
    private ChallengeResult searchForBlocks(Map<Material, Integer> requiredMap, int factor, BoundingBox boundingBox) {
        if (requiredMap.isEmpty()) {
            return new ChallengeResult().setMeetsRequirements().setCompleteFactor(factor);
        }
        Map<Material, Integer> blocks = new EnumMap<>(requiredMap);
        Map<Material, Integer> blocksFound = new HashMap<>(requiredMap.size());

        // This queue will contain only blocks with the required type ordered by distance till player.
        Queue<Block> blockFromWorld = new PriorityQueue<>((o1, o2) -> {
            if (o1.getType().equals(o2.getType()) && this.user.getLocation() != null) {
                return Double.compare(o1.getLocation().distance(this.user.getLocation()),
                        o2.getLocation().distance(this.user.getLocation()));
            } else {
                return o1.getType().compareTo(o2.getType());
            }
        });

        for (int x = (int) boundingBox.getMinX(); x <= boundingBox.getMaxX(); x++) {
            for (int y = (int) boundingBox.getMinY(); y <= boundingBox.getMaxY(); y++) {
                for (int z = (int) boundingBox.getMinZ(); z <= boundingBox.getMaxZ(); z++) {
                    Block block = this.user.getWorld().getBlockAt(x, y, z);

                    if (requiredMap.containsKey(block.getType())) {
                        blockFromWorld.add(block);

                        blocksFound.putIfAbsent(block.getType(), 1);
                        blocksFound.computeIfPresent(block.getType(), (reqEntity, amount) -> amount + 1);

                        // Remove one
                        blocks.computeIfPresent(block.getType(), (b, amount) -> amount - 1);
                        // Remove any that have an amount of 0
                        blocks.entrySet().removeIf(en -> en.getValue() <= 0);

                        if (blocks.isEmpty() && factor == 1) {
                            // Return as soon as it s empty as no point to search more.
                            return new ChallengeResult().setMeetsRequirements().setCompleteFactor(factor)
                                    .setBlockQueue(blockFromWorld);
                        }
                    }
                }
            }
        }

        if (blocks.isEmpty()) {
            if (factor > 1) {
                // Calculate minimal completion count.

                for (Map.Entry<Material, Integer> entry : blocksFound.entrySet()) {
                    factor = Math.min(factor, entry.getValue() / requiredMap.get(entry.getKey()));
                }
            }

            // kick garbage collector
            blocksFound.clear();

            return new ChallengeResult().setMeetsRequirements().setCompleteFactor(factor).setBlockQueue(blockFromWorld);
        }

        Utils.sendMessage(this.user, this.world, Constants.ERRORS + "not-close-enough", Constants.PARAMETER_NUMBER,
                String.valueOf(this.getIslandRequirements().getSearchRadius()));

        blocks.forEach((k, v) -> Utils.sendMessage(this.user, this.world, Constants.ERRORS + "you-still-need",
                "[amount]", String.valueOf(v), "[item]", Utils.prettifyObject(k, this.user)));


        // kick garbage collector
        blocks.clear();
        blocksFound.clear();
        blockFromWorld.clear();

        return EMPTY_RESULT;
    }


    /**
     * This method search required entities in given radius from user position and entity is inside boundingBox.
     * @param requiredMap RequiredEntities Map.
     * @param factor - requirements multiplier.
     * @param boundingBox Bounding box of island challenge
     * @return ChallengeResult
     */
    private ChallengeResult searchForEntities(Map<EntityType, Integer> requiredMap, int factor,
            BoundingBox boundingBox) {
        if (requiredMap.isEmpty()) {
            return new ChallengeResult().setMeetsRequirements().setCompleteFactor(factor);
        }

        // Collect all entities that could be removed.
        Map<EntityType, Integer> entitiesFound = new HashMap<>();
        Map<EntityType, Integer> minimalRequirements = new EnumMap<>(requiredMap);

        // Create queue that contains all required entities ordered by distance till player.
        Queue<Entity> entityQueue = new PriorityQueue<>((o1, o2) -> {
            if (o1.getType().equals(o2.getType()) && this.user.getLocation() != null) {
                return Double.compare(o1.getLocation().distance(this.user.getLocation()),
                        o2.getLocation().distance(this.user.getLocation()));
            } else
            {
                return o1.getType().compareTo(o2.getType());
            }
        });

        user.getWorld().getNearbyEntities(boundingBox).forEach(entity -> {
            // Check if entity is inside challenge bounding box
            if (requiredMap.containsKey(entity.getType()))
            {
                entityQueue.add(entity);

                entitiesFound.putIfAbsent(entity.getType(), 1);
                entitiesFound.computeIfPresent(entity.getType(), (reqEntity, amount) -> amount + 1);

                // Look through all the nearby Entities, filtering by type
                minimalRequirements.computeIfPresent(entity.getType(), (reqEntity, amount) -> amount - 1);
                minimalRequirements.entrySet().removeIf(e -> e.getValue() == 0);
            }
        });

        if (minimalRequirements.isEmpty()) {
            if (factor > 1)
            {
                // Calculate minimal completion count.

                for (Map.Entry<EntityType, Integer> entry : entitiesFound.entrySet())
                {
                    factor = Math.min(factor, entry.getValue() / requiredMap.get(entry.getKey()));
                }
            }

            // Kick garbage collector
            entitiesFound.clear();

            return new ChallengeResult().setMeetsRequirements().setCompleteFactor(factor).setEntityQueue(entityQueue);
        }

        minimalRequirements.forEach(
                (reqEnt, amount) -> Utils.sendMessage(this.user, this.world, Constants.ERRORS + "you-still-need",
                        "[amount]", String.valueOf(amount), "[item]", Utils.prettifyObject(reqEnt, this.user)));

        // Kick garbage collector
        entitiesFound.clear();
        minimalRequirements.clear();
        entityQueue.clear();

        return EMPTY_RESULT;
    }


    /**
     * This method removes required block and set air instead of it.
     * @param blockQueue Queue with blocks that could be removed
     * @param factor requirement factor for each block type.
     */
    private void removeBlocks(Queue<Block> blockQueue, int factor) {
        Map<Material, Integer> blocks = new EnumMap<>(this.getIslandRequirements().getRequiredBlocks());

        // Increase required blocks by factor.
        blocks.entrySet().forEach(entry -> entry.setValue(entry.getValue() * factor));

        blockQueue.forEach(block -> {
            if (blocks.containsKey(block.getType()))
            {
                blocks.computeIfPresent(block.getType(), (b, amount) -> amount - 1);
                blocks.entrySet().removeIf(en -> en.getValue() <= 0);

                block.setType(Material.AIR);
            }
        });
    }


    /**
     * This method removes required entities.
     * @param entityQueue Queue with entities that could be removed
     * @param factor requirement factor for each entity type.
     */
    private void removeEntities(Queue<Entity> entityQueue, int factor) {
        Map<EntityType, Integer> entities = this.getIslandRequirements().getRequiredEntities().isEmpty()
                ? new EnumMap<>(EntityType.class)
                : new EnumMap<>(this.getIslandRequirements().getRequiredEntities());

        // Increase required entities by factor.
        entities.entrySet().forEach(entry -> entry.setValue(entry.getValue() * factor));

        // Go through entity queue and remove entities that are requried.
        entityQueue.forEach(entity -> {
            if (entities.containsKey(entity.getType())) {
                entities.computeIfPresent(entity.getType(), (reqEntity, amount) -> amount - 1);
                entities.entrySet().removeIf(e -> e.getValue() == 0);
                entity.remove();
            }
        });
    }


    // ---------------------------------------------------------------------
    // Section: Other challenge
    // ---------------------------------------------------------------------


    /**
     * Checks if a other challenge can be completed or not
     * It returns ChallengeResult.
     * @param factor - times that user wanted to complete
     */
    private ChallengeResult checkOthers(int factor) {
        if (factor <= 0) {
            return EMPTY_RESULT;
        }

        OtherRequirements requirements = this.getOtherRequirements();

        if (!this.addon.isLevelProvided() && requirements.getRequiredIslandLevel() != 0) {
            Utils.sendMessage(this.user, this.world, Constants.ERRORS + "missing-addon");
        } else if (!this.addon.isEconomyProvided() && requirements.getRequiredMoney() != 0) {
            Utils.sendMessage(this.user, this.world, Constants.ERRORS + "missing-addon");
        } else if (this.addon.isEconomyProvided() && requirements.getRequiredMoney() < 0) {
            Utils.sendMessage(this.user, this.world, Constants.ERRORS + "incorrect");
        } else if (this.addon.isEconomyProvided()
                && !this.addon.getEconomyProvider().has(this.user, requirements.getRequiredMoney())) {
            Utils.sendMessage(this.user, this.world, Constants.ERRORS + "not-enough-money", Constants.PARAMETER_VALUE,
                    Double.toString(requirements.getRequiredMoney()));
        } else if (requirements.getRequiredExperience() < 0) {
            Utils.sendMessage(this.user, this.world, Constants.ERRORS + "incorrect");
        } else if (this.user.getPlayer().getTotalExperience() < requirements.getRequiredExperience()
                && this.user.getPlayer().getGameMode() != GameMode.CREATIVE) {
            // Players in creative gamemode has infinite amount of EXP.
            Utils.sendMessage(this.user, this.world, Constants.ERRORS + "not-enough-experience",
                    Constants.PARAMETER_VALUE, Integer.toString(requirements.getRequiredExperience()));
        } else if (this.addon.isLevelProvided() && this.addon.getLevelAddon().getIslandLevel(this.world,
                this.user.getUniqueId()) < requirements.getRequiredIslandLevel()) {
            Utils.sendMessage(this.user,
                    this.world, Constants.ERRORS + "island-level", TextVariables.NUMBER,
                    String.valueOf(requirements.getRequiredIslandLevel()));
        } else if (this.addon.getPlugin().getHooks().getHook("PlaceholderAPI").isPresent()
                && !requirements.getPapiString().isEmpty()
                && !CheckPapi.evaluate(user.getPlayer(), requirements.getPapiString())) {
            Utils.sendMessage(this.user, this.world, Constants.ERRORS + "incorrect");
            if (!requirements.getPapiString().isEmpty()) {
                addon.log("FYI:.Challenge failed for " + user.getName() + ". PAPI formula: "
                        + requirements.getPapiString() + " = "
                        + CheckPapi.evaluate(user.getPlayer(), requirements.getPapiString()));
            }
        } else if (!requirements.getAdvancements().stream().map(user.getPlayer()::getAdvancementProgress)
                .allMatch(AdvancementProgress::isDone)) {
            Utils.sendMessage(this.user, this.world, Constants.ERRORS + "incorrect");
            user.sendMessage("challenges.gui.buttons.required_advancements.title");
            requirements.getAdvancements().stream().filter(ad -> !user.getPlayer().getAdvancementProgress(ad).isDone())
                    .forEach(ad -> Utils.sendMessage(this.user, this.world,
                            "challenges.gui.buttons.advancement_element.name", "[name]",
                            ad.getDisplay().getTitle()));
        }
        else
        {
            // calculate factor

            if (this.addon.isEconomyProvided() && requirements.isTakeMoney()) {
                factor = Math.min(factor, (int) (this.addon.getEconomyProvider().getBalance(this.user)
                        / requirements.getRequiredMoney()));
            }

            if (requirements.getRequiredExperience() > 0 && requirements.isTakeExperience()) {
                factor = Math.min(factor,
                        this.user.getPlayer().getTotalExperience() / requirements.getRequiredExperience());
            }

            return new ChallengeResult().setMeetsRequirements().setCompleteFactor(factor);
        }

        return EMPTY_RESULT;
    }

    // ---------------------------------------------------------------------
    // Section: Statistic Challenge
    // ---------------------------------------------------------------------

    /**
     * Checks if a statistic challenge can be completed or not
     * It returns ChallengeResult.
     * @param factor - times that user wanted to complete
     */
    private ChallengeResult checkStatistic(int factor) {
        if (factor <= 0) {
            return EMPTY_RESULT;
        }

        StatisticRequirements requirements = this.challenge.getRequirements();

        int currentValue;

        if (requirements.getRequiredStatistics().isEmpty()) {
            // Sanity check.
            return EMPTY_RESULT;
        }
        List<ChallengeResult> cr = new ArrayList<>();
        // Check all requirements
        for (StatisticRec s : requirements.getRequiredStatistics()) {

            switch (Objects.requireNonNull(s.statistic()).getType())
            {
            case UNTYPED -> currentValue = this.manager.getStatisticData(this.user, this.world, s.statistic());
            case ITEM, BLOCK ->
                currentValue = this.manager.getStatisticData(this.user, this.world, s.statistic(), s.material());
            case ENTITY ->
                currentValue = this.manager.getStatisticData(this.user, this.world, s.statistic(), s.entity());
            default -> currentValue = 0;
            }

            if (currentValue < s.amount()) {
                switch (Objects.requireNonNull(s.statistic()).getType()) {
                case ITEM, BLOCK -> {
                    Utils.sendMessage(this.user, this.world, Constants.ERRORS + "requirement-not-met-material",
                            TextVariables.NUMBER, String.valueOf(s.amount()), "[statistic]",
                            Utils.prettifyObject(s.statistic(), this.user), "[material]",
                            Utils.prettifyObject(s.material(), this.user), Constants.PARAMETER_VALUE,
                            String.valueOf(currentValue));
                }
                case ENTITY -> {
                    Utils.sendMessage(this.user, this.world, Constants.ERRORS + "requirement-not-met-entity",
                            TextVariables.NUMBER, String.valueOf(s.amount()), "[statistic]",
                            Utils.prettifyObject(s.statistic(), this.user), "[entity]",
                            Utils.prettifyObject(s.entity(), this.user), Constants.PARAMETER_VALUE,
                            String.valueOf(currentValue));
                }
                default -> {
                    Utils.sendMessage(this.user, this.world, Constants.ERRORS + "requirement-not-met",
                            TextVariables.NUMBER, String.valueOf(s.amount()), "[statistic]",
                            Utils.prettifyObject(s.statistic(), this.user), Constants.PARAMETER_VALUE,
                            String.valueOf(currentValue));
                }
                }
            } else {
                factor = s.amount() == 0 ? factor : Math.min(factor, currentValue / s.amount());
                // Store result
                cr.add(new ChallengeResult().setMeetsRequirements().setCompleteFactor(factor));
            }
        }
        // Check results -- there must be some and all must pass
        if (!cr.isEmpty() && cr.stream().allMatch(result -> result.meetsRequirements)) {
            // Return any of them, because they pass
            return cr.getFirst();
        }
        return EMPTY_RESULT;
    }

    // ---------------------------------------------------------------------
    // Section: Title parsings
    // ---------------------------------------------------------------------

    /**
     * This method pareses input message by replacing all challenge variables in [] with their values.
     * @param inputMessage inputMessage string
     * @param challenge Challenge from which these values should be taken
     * @return new String that replaces [VALUE] with correct value from challenge.
     */
    private String parseChallenge(String inputMessage, Challenge challenge) {
        String outputMessage = inputMessage;

        if (inputMessage.contains("[") && inputMessage.contains("]")) {
            outputMessage = outputMessage.replace("[friendlyName]", challenge.getFriendlyName());

            ChallengeLevel level = challenge.getLevel().isEmpty() ? null : this.manager.getLevel(challenge.getLevel());
            outputMessage = outputMessage.replace("[level]", level == null ? "" : level.getFriendlyName());

            outputMessage = outputMessage.replace("[rewardText]", challenge.getRewardText());
        }

        return ChatColor.translateAlternateColorCodes('&', outputMessage);
    }


    /**
     * This method pareses input message by replacing all level variables in [] with their values.
     * @param inputMessage inputMessage string
     * @param level level from which these values should be taken
     * @return new String that replaces [VALUE] with correct value from level.
     */
    private String parseLevel(String inputMessage, ChallengeLevel level) {
        String outputMessage = inputMessage;

        if (inputMessage.contains("[") && inputMessage.contains("]"))
        {
            outputMessage = outputMessage.replace("[friendlyName]", level.getFriendlyName());
            outputMessage = outputMessage.replace("[rewardText]", level.getRewardText());
        }

        return ChatColor.translateAlternateColorCodes('&', outputMessage);
    }

    // ---------------------------------------------------------------------
    // Section: Simple getter methods
    // ---------------------------------------------------------------------


    /**
     * This is simple cast method. Easier access to IslandRequirements.
     * @return Island Requirements
     */
    private IslandRequirements getIslandRequirements() {
        return this.challenge.getRequirements();
    }


    /**
     * This is simple cast method. Easier access to InventoryRequirements.
     * @return Inventory Requirements
     */
    private InventoryRequirements getInventoryRequirements() {
        return this.challenge.getRequirements();
    }


    /**
     * This is simple cast method. Easier access to OtherRequirements.
     * @return Other Requirements
     */
    private OtherRequirements getOtherRequirements() {
        return this.challenge.getRequirements();
    }


    // ---------------------------------------------------------------------
    // Section: Result classes
    // ---------------------------------------------------------------------


    /**
     * Contains flags on completion of challenge
     *
     * @author tastybento
     */
    static class ChallengeResult {
        /**
         * This method sets that challenge meets all requirements at least once.
         * @return Current object.
         */
        ChallengeResult setMeetsRequirements()
        {
            this.meetsRequirements = true;
            return this;
        }


        /**
         * Method sets that challenge is completed once already
         * @param completed boolean that indicate that challenge has been already completed.
         * @return Current object.
         */
        ChallengeResult setCompleted(boolean completed) {
            this.completed = completed;
            return this;
        }


        /**
         * Method sets how many times challenge can be completed.
         * @param factor Integer that represents completion count.
         * @return Current object.
         */
        ChallengeResult setCompleteFactor(int factor)
        {
            this.factor = factor;
            return this;
        }


        // ---------------------------------------------------------------------
        // Section: Requirement memory
        // ---------------------------------------------------------------------


        /**
         * Method sets requiredItems for inventory challenge.
         * @param requiredItems items that are required by inventory challenge.
         * @return Current object.
         */
        ChallengeResult setRequiredItems(List<ItemStack> requiredItems)
        {
            this.requiredItems = requiredItems;
            return this;
        }


        /**
         * Method sets queue that contains all blocks with required material type.
         * @param blocks queue that contains required materials from world.
         * @return Current object.
         */
        ChallengeResult setBlockQueue(Queue<Block> blocks)
        {
            this.blocks = blocks;
            return this;
        }

        /**
         * Method sets queue that contains all entities with required entity type.
         * @param entities queue that contains required entities from world.
         * @return Current object.
         */
        ChallengeResult setEntityQueue(Queue<Entity> entities)
        {
            this.entities = entities;
            return this;
        }


        // ---------------------------------------------------------------------
        // Section: Getters
        // ---------------------------------------------------------------------


        /**
         * Returns value of was completed variable.
         * @return value of completed variable
         */
        boolean wasCompleted()
        {
            return this.completed;
        }


        /**
         * This method returns how many times challenge can be completed.
         * @return completion count.
         */
        int getFactor() {
            return this.factor;
        }


        /**
         * This method returns if challenge requirements has been met at least once.
         * @return value of meets requirements variable.
         */
        boolean isMeetsRequirements() {
            return this.meetsRequirements;
        }


        // ---------------------------------------------------------------------
        // Section: Variables
        // ---------------------------------------------------------------------


        /**
         * Boolean that indicate that challenge has already bean completed once before.
         */
        private boolean completed;

        /**
         * Indicates that challenge can be completed.
         */
        private boolean meetsRequirements;

        /**
         * Integer that represents how many times challenge is completed
         */
        private int factor;

        /**
         * List that contains required items for Inventory Challenge
         * Necessary as it contains grouped items by type or similarity, not by limit 64.
         */
        private List<ItemStack> requiredItems;

        /**
         * Map that contains removed items and their removed count.
         */
        private Map<ItemStack, Integer> removedItems = null;

        /**
         * Queue of blocks that contains all blocks with the same type as requiredBlock from
         * challenge requirements.
         */
        private Queue<Block> blocks;

        /**
         * Queue of entities that contains all entities with the same type as requiredEntities from
         * challenge requirements.
         */
        private Queue<Entity> entities;

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "ChallengeResult [completed=" + completed + ", meetsRequirements=" + meetsRequirements + ", factor="
                    + factor + ", requiredItems=" + requiredItems + ", removedItems=" + removedItems + ", blocks="
                    + blocks + ", entities=" + entities + "]";
        }
    }
}
