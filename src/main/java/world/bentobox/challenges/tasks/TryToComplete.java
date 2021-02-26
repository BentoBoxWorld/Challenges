package world.bentobox.challenges.tasks;



import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;

import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.hooks.LangUtilsHook;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.ChallengesManager;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.Challenge.ChallengeType;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.database.object.requirements.InventoryRequirements;
import world.bentobox.challenges.database.object.requirements.IslandRequirements;
import world.bentobox.challenges.database.object.requirements.OtherRequirements;
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
    private ChallengesAddon addon;

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
     * Top command first label.
     */
    private String topLabel;

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
    public TryToComplete label(String label)
    {
        this.topLabel = label;
        return this;
    }


    @Deprecated
    public TryToComplete user(User user)
    {
        this.user = user;
        return this;
    }


    @Deprecated
    public TryToComplete manager(ChallengesManager manager)
    {
        this.manager = manager;
        return this;
    }


    @Deprecated
    public TryToComplete challenge(Challenge challenge)
    {
        this.challenge = challenge;
        return this;
    }


    @Deprecated
    public TryToComplete world(World world)
    {
        this.world = world;
        return this;
    }


    @Deprecated
    public TryToComplete permPrefix(String prefix)
    {
        this.permissionPrefix = prefix;
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
        this.topLabel = topLabel;
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
                this.user.sendMessage("challenges.messages.you-completed-challenge", "[value]", this.challenge.getFriendlyName());
            }

            if (this.addon.getChallengesSettings().isBroadcastMessages())
            {
                for (Player player : Bukkit.getOnlinePlayers())
                {
                    // Only other players should see message.
                    if (!player.getUniqueId().equals(this.user.getUniqueId()))
                    {
                        User.getInstance(player).sendMessage("challenges.messages.name-has-completed-challenge",
                                "[name]", this.user.getName(),
                                "[value]", this.challenge.getFriendlyName());
                    }
                }
            }

            // sends title to player on challenge completion
            if (this.addon.getChallengesSettings().isShowCompletionTitle())
            {
                this.user.getPlayer().sendTitle(
                        this.parseChallenge(this.user.getTranslation("challenges.titles.challenge-title"), this.challenge),
                        this.parseChallenge(this.user.getTranslation("challenges.titles.challenge-subtitle"), this.challenge),
                        10,
                        this.addon.getChallengesSettings().getTitleShowtime(),
                        20);
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
                        (double)this.challenge.getRepeatMoneyReward() * rewardFactor);
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
                this.user.sendMessage("challenges.messages.you-repeated-challenge-multiple",
                        "[value]", this.challenge.getFriendlyName(),
                        "[count]", Integer.toString(result.getFactor()));
            }
            else
            {
                this.user.sendMessage("challenges.messages.you-repeated-challenge", "[value]", this.challenge.getFriendlyName());
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

                    this.user.sendMessage("challenges.messages.you-completed-level", "[value]", level.getFriendlyName());

                    if (this.addon.getChallengesSettings().isBroadcastMessages())
                    {
                        for (Player player : this.addon.getServer().getOnlinePlayers())
                        {
                            // Only other players should see message.
                            if (!player.getUniqueId().equals(this.user.getUniqueId()))
                            {
                                User.getInstance(player).sendMessage("challenges.messages.name-has-completed-level",
                                        "[name]", this.user.getName(), "[value]", level.getFriendlyName());
                            }
                        }
                    }

                    this.manager.setLevelComplete(this.user, this.world, level);

                    // sends title to player on level completion
                    if (this.addon.getChallengesSettings().isShowCompletionTitle())
                    {
                        this.user.getPlayer().sendTitle(
                                this.parseLevel(this.user.getTranslation("challenges.titles.level-title"), level),
                                this.parseLevel(this.user.getTranslation("challenges.titles.level-subtitle"), level),
                                10,
                                this.addon.getChallengesSettings().getTitleShowtime(),
                                20);
                    }
                }
            }
        }

        return result;
    }


    /**
     * This method full fills all challenge type requirements, that is not full filled yet.
     * @param result Challenge Results
     */
    private void fullFillRequirements(ChallengeResult result)
    {
        if (this.challenge.getChallengeType().equals(ChallengeType.ISLAND))
        {
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
        else if (this.challenge.getChallengeType().equals(ChallengeType.INVENTORY))
        {
            // If remove items, then remove them
            if (this.getInventoryRequirements().isTakeItems())
            {
                int sumEverything = result.requiredItems.stream().
                        mapToInt(itemStack -> itemStack.getAmount() * result.getFactor()).
                        sum();

                Map<ItemStack, Integer> removedItems =
                        this.removeItems(result.requiredItems, result.getFactor());

                int removedAmount = removedItems.values().stream().mapToInt(num -> num).sum();

                // Something is not removed.
                if (sumEverything != removedAmount)
                {
                    this.user.sendMessage("challenges.errors.cannot-remove-items");

                    result.removedItems = removedItems;
                    result.meetsRequirements = false;
                }
            }
        }
        else if (this.challenge.getChallengeType().equals(ChallengeType.OTHER))
        {
            OtherRequirements requirements = this.challenge.getRequirements();

            if (this.addon.isEconomyProvided() && requirements.isTakeMoney())
            {
                this.addon.getEconomyProvider().withdraw(this.user, requirements.getRequiredMoney());
            }

            if (requirements.isTakeExperience() &&
                    this.user.getPlayer().getGameMode() != GameMode.CREATIVE)
            {
                // Cannot take anything from creative game mode.
                this.user.getPlayer().setTotalExperience(
                        this.user.getPlayer().getTotalExperience() - requirements.getRequiredExperience());
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
            this.user.sendMessage("challenges.errors.not-deployed");
            result = EMPTY_RESULT;
        }
        else if (maxTimes < 1)
        {
            this.user.sendMessage("challenges.errors.not-valid-integer");
            result = EMPTY_RESULT;
        }
        else if (Util.getWorld(this.world) != Util.getWorld(this.user.getWorld()) ||
                !this.challenge.matchGameMode(Utils.getGameMode(this.world)))
        {
            this.user.sendMessage("general.errors.wrong-world");
            result = EMPTY_RESULT;
        }
        // Player is not on island
        else if (ChallengesAddon.CHALLENGES_WORLD_PROTECTION.isSetForWorld(this.world) &&
                !this.addon.getIslands().locationIsOnIsland(this.user.getPlayer(), this.user.getLocation()))
        {
            this.user.sendMessage("challenges.errors.not-on-island");
            result = EMPTY_RESULT;
        }
        // Check player permission
        else if (!this.addon.getIslands().getIslandAt(this.user.getLocation()).
                map(i -> i.isAllowed(this.user, ChallengesAddon.CHALLENGES_ISLAND_PROTECTION)).
                orElse(false))
        {
            this.user.sendMessage("challenges.errors.no-rank");
            result = EMPTY_RESULT;
        }
        // Check if user has unlocked challenges level.
        else if (!this.challenge.getLevel().equals(ChallengesManager.FREE) &&
                !this.manager.isLevelUnlocked(this.user, this.world, this.manager.getLevel(this.challenge.getLevel())))
        {
            this.user.sendMessage("challenges.errors.challenge-level-not-available");
            result = EMPTY_RESULT;
        }
        // Check max times
        else if (this.challenge.isRepeatable() && this.challenge.getMaxTimes() > 0 &&
                this.manager.getChallengeTimes(this.user, this.world, this.challenge) >= this.challenge.getMaxTimes())
        {
            this.user.sendMessage("challenges.errors.not-repeatable");
            result = EMPTY_RESULT;
        }
        // Check repeatability
        else if (!this.challenge.isRepeatable() && this.manager.isChallengeComplete(this.user, this.world, this.challenge))
        {
            this.user.sendMessage("challenges.errors.not-repeatable");
            result = EMPTY_RESULT;
        }
        // Check environment
        else if (!this.challenge.getEnvironment().isEmpty() &&
                !this.challenge.getEnvironment().contains(this.user.getWorld().getEnvironment()))
        {
            this.user.sendMessage("challenges.errors.wrong-environment");
            result = EMPTY_RESULT;
        }
        // Check permission
        else if (!this.checkPermissions())
        {
            this.user.sendMessage("general.errors.no-permission");
            result = EMPTY_RESULT;
        }
        else if (type.equals(ChallengeType.INVENTORY))
        {
            result = this.checkInventory(this.getAvailableCompletionTimes(maxTimes));
        }
        else if (type.equals(ChallengeType.ISLAND))
        {
            result = this.checkSurrounding(this.getAvailableCompletionTimes(maxTimes));
        }
        else if (type.equals(ChallengeType.OTHER))
        {
            result = this.checkOthers(this.getAvailableCompletionTimes(maxTimes));
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
                this.challenge.getRequirements().getRequiredPermissions().stream().allMatch(s -> this.user.hasPermission(s));
    }


    /**
     * This method checks if it is possible to complete maxTimes current challenge by
     * challenge constraints and already completed times.
     * @param vantedTimes How many times user wants to complete challenge
     * @return how many times user is able complete challenge by its constraints.
     */
    private int getAvailableCompletionTimes(int vantedTimes)
    {
        if (!this.challenge.isRepeatable())
        {
            // Challenge is not repeatable
            vantedTimes = 1;
        }
        else if (this.challenge.getMaxTimes() != 0)
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
        if (user.hasPermission(this.permissionPrefix + "command.challengeexempt") && !user.isOp())
        {
            return;
        }
        for (String cmd : commands)
        {
            if (cmd.startsWith("[SELF]"))
            {
                String alert = "Running command '" + cmd + "' as " + this.user.getName();
                this.addon.getLogger().info(alert);
                cmd = cmd.substring(6, cmd.length()).replace("[player]", this.user.getName()).trim();
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
                if (!this.addon.getServer().dispatchCommand(this.addon.getServer().getConsoleSender(),
                        cmd.replace("[player]", this.user.getName())))
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
        // Run through inventory
        List<ItemStack> requiredItems;

        // Players in creative game mode has got all items. No point to search for them.
        if (this.user.getPlayer().getGameMode() != GameMode.CREATIVE)
        {
            requiredItems = Utils.groupEqualItems(this.getInventoryRequirements().getRequiredItems());

            // Check if all required items are in players inventory.
            for (ItemStack required : requiredItems)
            {
                int numInInventory;

                if (Utils.canIgnoreMeta(required.getType()))
                {
                    numInInventory =
                            Arrays.stream(this.user.getInventory().getContents()).
                            filter(Objects::nonNull).
                            filter(i -> i.getType().equals(required.getType())).
                            mapToInt(ItemStack::getAmount).
                            sum();
                }
                else
                {
                    numInInventory =
                            Arrays.stream(this.user.getInventory().getContents()).
                            filter(Objects::nonNull).
                            filter(i -> i.isSimilar(required)).
                            mapToInt(ItemStack::getAmount).
                            sum();
                }

                if (numInInventory < required.getAmount())
                {
                    this.user.sendMessage("challenges.errors.not-enough-items",
                            "[items]",
                            LangUtilsHook.getItemName(required, user));
                    return EMPTY_RESULT;
                }

                maxTimes = Math.min(maxTimes, numInInventory / required.getAmount());
            }
        }
        else
        {
            requiredItems = Collections.emptyList();
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

            if (Utils.canIgnoreMeta(required.getType()))
            {
                // Use collecting method that ignores item meta.
                itemsInInventory = Arrays.stream(user.getInventory().getContents()).
                        filter(Objects::nonNull).
                        filter(i -> i.getType().equals(required.getType())).
                        collect(Collectors.toList());
            }
            else
            {
                // Use collecting method that compares item meta.
                itemsInInventory = Arrays.stream(user.getInventory().getContents()).
                        filter(Objects::nonNull).
                        filter(i -> i.isSimilar(required)).
                        collect(Collectors.toList());
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
                    boundingBox.getWidthZ() > distance * 2 + 3 ||
                    boundingBox.getHeight() > distance * 2 + 3)
            {
                this.addon.logError("BoundingBox is larger than SearchRadius. " +
                        " | BoundingBox: " + boundingBox.toString() +
                        " | Search Distance: " + requirements.getSearchRadius() +
                        " | Location: " + this.user.getLocation().toString() +
                        " | Center: " + island.getCenter().toString() +
                        " | Range: " + range);

                return EMPTY_RESULT;
            }
        }

        ChallengeResult result = this.searchForEntities(requirements.getRequiredEntities(), factor, boundingBox);

        if (result.isMeetsRequirements() && !requirements.getRequiredBlocks().isEmpty())
        {
            // Search for items only if entities found
            result = this.searchForBlocks(requirements.getRequiredBlocks(), result.getFactor(), boundingBox);
        }

        return result;
    }


    /**
     * This method search required blocks in given challenge boundingBox.
     * @param requiredMap RequiredBlock Map.
     * @param factor - requirement multilayer.
     * @param boundingBox Bounding box of island challenge
     * @return ChallengeResult
     */
    private ChallengeResult searchForBlocks(Map<Material, Integer> requiredMap, int factor, BoundingBox boundingBox)
    {
        if (requiredMap.isEmpty())
        {
            return new ChallengeResult().setMeetsRequirements().setCompleteFactor(factor);
        }

        Map<Material, Integer> blocks = new EnumMap<>(requiredMap);
        Map<Material, Integer> blocksFound = new HashMap<>(requiredMap.size());

        // This queue will contain only blocks whit required type ordered by distance till player.
        Queue<Block> blockFromWorld = new PriorityQueue<>((o1, o2) -> {
            if (o1.getType().equals(o2.getType()))
            {
                return Double.compare(o1.getLocation().distance(this.user.getLocation()),
                        o2.getLocation().distance(this.user.getLocation()));
            }
            else
            {
                return o1.getType().compareTo(o2.getType());
            }
        });

        for (int x = (int) boundingBox.getMinX(); x <= boundingBox.getMaxX(); x++)
        {
            for (int y = (int) boundingBox.getMinY(); y <= boundingBox.getMaxY(); y++)
            {
                for (int z = (int) boundingBox.getMinZ(); z <= boundingBox.getMaxZ(); z++)
                {
                    Block block = this.user.getWorld().getBlockAt(x, y, z);

                    if (requiredMap.containsKey(block.getType()))
                    {
                        blockFromWorld.add(block);

                        blocksFound.putIfAbsent(block.getType(), 1);
                        blocksFound.computeIfPresent(block.getType(), (reqEntity, amount) -> amount + 1);

                        // Remove one
                        blocks.computeIfPresent(block.getType(), (b, amount) -> amount - 1);
                        // Remove any that have an amount of 0
                        blocks.entrySet().removeIf(en -> en.getValue() <= 0);

                        if (blocks.isEmpty() && factor == 1)
                        {
                            // Return as soon as it s empty as no point to search more.
                            return new ChallengeResult().setMeetsRequirements().setCompleteFactor(factor).setBlockQueue(blockFromWorld);
                        }
                    }
                }
            }
        }

        if (blocks.isEmpty())
        {
            if (factor > 1)
            {
                // Calculate minimal completion count.

                for (Map.Entry<Material, Integer> entry : blocksFound.entrySet())
                {
                    factor = Math.min(factor,
                            entry.getValue() / requiredMap.get(entry.getKey()));
                }
            }

            // kick garbage collector
            blocksFound.clear();

            return new ChallengeResult().setMeetsRequirements().setCompleteFactor(factor).setBlockQueue(blockFromWorld);
        }

        this.user.sendMessage("challenges.errors.not-close-enough",
                "[number]",
                String.valueOf(this.getIslandRequirements().getSearchRadius()));

        blocks.forEach((k, v) -> user.sendMessage("challenges.errors.you-still-need",
                "[amount]", String.valueOf(v),
                "[item]", LangUtilsHook.getMaterialName(k, user)));


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
    private ChallengeResult searchForEntities(Map<EntityType, Integer> requiredMap,
            int factor,
            BoundingBox boundingBox)
    {
        if (requiredMap.isEmpty())
        {
            return new ChallengeResult().setMeetsRequirements().setCompleteFactor(factor);
        }

        // Collect all entities that could be removed.
        Map<EntityType, Integer> entitiesFound = new HashMap<>();
        Map<EntityType, Integer> minimalRequirements = new EnumMap<>(requiredMap);

        // Create queue that contains all required entities ordered by distance till player.
        Queue<Entity> entityQueue = new PriorityQueue<>((o1, o2) -> {
            if (o1.getType().equals(o2.getType()))
            {
                return Double.compare(o1.getLocation().distance(this.user.getLocation()),
                        o2.getLocation().distance(this.user.getLocation()));
            }
            else
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

        if (minimalRequirements.isEmpty())
        {
            if (factor > 1)
            {
                // Calculate minimal completion count.

                for (Map.Entry<EntityType, Integer> entry : entitiesFound.entrySet())
                {
                    factor = Math.min(factor,
                            entry.getValue() / requiredMap.get(entry.getKey()));
                }
            }

            // Kick garbage collector
            entitiesFound.clear();

            return new ChallengeResult().setMeetsRequirements().setCompleteFactor(factor).setEntityQueue(entityQueue);
        }

        minimalRequirements.forEach((reqEnt, amount) -> this.user.sendMessage("challenges.errors.you-still-need",
                "[amount]", String.valueOf(amount),
                "[item]", LangUtilsHook.getEntityName(reqEnt, user)));

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
    private void removeBlocks(Queue<Block> blockQueue, int factor)
    {
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
    private void removeEntities(Queue<Entity> entityQueue, int factor)
    {
        Map<EntityType, Integer> entities = this.getIslandRequirements().getRequiredEntities().isEmpty() ?
                new EnumMap<>(EntityType.class) : new EnumMap<>(this.getIslandRequirements().getRequiredEntities());

                // Increase required entities by factor.
                entities.entrySet().forEach(entry -> entry.setValue(entry.getValue() * factor));

                // Go through entity queue and remove entities that are requried.
                entityQueue.forEach(entity -> {
                    if (entities.containsKey(entity.getType()))
                    {
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
    private ChallengeResult checkOthers(int factor)
    {
        OtherRequirements requirements = this.getOtherRequirements();

        if (!this.addon.isLevelProvided() &&
                requirements.getRequiredIslandLevel() != 0)
        {
            this.user.sendMessage("challenges.errors.missing-addon");
        }
        else if (!this.addon.isEconomyProvided() &&
                requirements.getRequiredMoney() != 0)
        {
            this.user.sendMessage("challenges.errors.missing-addon");
        }
        else if (this.addon.isEconomyProvided() && requirements.getRequiredMoney() < 0)
        {
            this.user.sendMessage("challenges.errors.incorrect");
        }
        else if (this.addon.isEconomyProvided() &&
                !this.addon.getEconomyProvider().has(this.user, requirements.getRequiredMoney()))
        {
            this.user.sendMessage("challenges.errors.not-enough-money",
                    "[value]",
                    Double.toString(requirements.getRequiredMoney()));
        }
        else if (requirements.getRequiredExperience() < 0)
        {
            this.user.sendMessage("challenges.errors.incorrect");
        }
        else if (this.user.getPlayer().getTotalExperience() < requirements.getRequiredExperience() &&
                this.user.getPlayer().getGameMode() != GameMode.CREATIVE)
        {
            // Players in creative gamemode has infinite amount of EXP.

            this.user.sendMessage("challenges.errors.not-enough-experience",
                    "[value]",
                    Integer.toString(requirements.getRequiredExperience()));
        }
        else if (this.addon.isLevelProvided() &&
                this.addon.getLevelAddon().getIslandLevel(this.world, this.user.getUniqueId()) < requirements.getRequiredIslandLevel())
        {
            this.user.sendMessage("challenges.errors.island-level",
                    TextVariables.NUMBER,
                    String.valueOf(requirements.getRequiredIslandLevel()));
        }
        else
        {
            // calculate factor

            if (this.addon.isEconomyProvided() && requirements.isTakeMoney())
            {
                factor = Math.min(factor, (int) (this.addon.getEconomyProvider().getBalance(this.user) / requirements.getRequiredMoney()));
            }

            if (requirements.getRequiredExperience() > 0 && requirements.isTakeExperience())
            {
                factor = Math.min(factor, this.user.getPlayer().getTotalExperience() / requirements.getRequiredExperience());
            }

            return new ChallengeResult().setMeetsRequirements().setCompleteFactor(factor);
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
    private String parseChallenge(String inputMessage, Challenge challenge)
    {
        String outputMessage = inputMessage;

        if (inputMessage.contains("[") && inputMessage.contains("]"))
        {
            outputMessage = outputMessage.replace("[friendlyName]", challenge.getFriendlyName());
            outputMessage = outputMessage.replace("[level]", challenge.getLevel().isEmpty() ? "" : this.manager.getLevel(challenge.getLevel()).getFriendlyName());
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
    private String parseLevel(String inputMessage, ChallengeLevel level)
    {
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
    private IslandRequirements getIslandRequirements()
    {
        return this.challenge.getRequirements();
    }


    /**
     * This is simple cast method. Easier access to InventoryRequirements.
     * @return Inventory Requirements
     */
    private InventoryRequirements getInventoryRequirements()
    {
        return this.challenge.getRequirements();
    }


    /**
     * This is simple cast method. Easier access to OtherRequirements.
     * @return Other Requirements
     */
    private OtherRequirements getOtherRequirements()
    {
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
    class ChallengeResult
    {
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
        ChallengeResult setCompleted(boolean completed)
        {
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
        int getFactor()
        {
            return this.factor;
        }


        /**
         * This method returns if challenge requirements has been met at least once.
         * @return value of meets requirements variable.
         */
        boolean isMeetsRequirements()
        {
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
