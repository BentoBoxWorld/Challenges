/**
 *
 */
package world.bentobox.challenges.panel;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import java.util.*;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.ChallengesManager;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.Challenge.ChallengeType;
import world.bentobox.challenges.database.object.ChallengeLevel;


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
        this.challenge = challenge;
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
        return new TryToComplete(addon, user, challenge, world, topLabel, permissionPrefix).
            build().meetsRequirements;
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    /**
     * This method checks if challenge can be done, and complete it, if it is possible.
     * @return ChallengeResult object, that contains completion status.
     */
    public ChallengeResult build()
    {
        // Check if can complete challenge
        ChallengeResult result = this.checkIfCanCompleteChallenge();

        if (!result.meetsRequirements)
        {
            return result;
        }

        if (!result.repeat)
        {
            // Item rewards
            for (ItemStack reward : this.challenge.getRewardItems())
            {
                this.user.getInventory().addItem(reward).forEach((k, v) ->
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

            this.user.sendMessage("challenges.you-completed", "[challenge]", this.challenge.getFriendlyName());

            if (this.addon.getChallengesSettings().isBroadcastMessages())
            {
                for (Player player : this.addon.getServer().getOnlinePlayers())
                {
                    // Only other players should see message.
                    if (!player.getUniqueId().equals(this.user.getUniqueId()))
                    {
                        User.getInstance(player).sendMessage("challenges.name-has-completed",
                            "[name]", this.user.getName(), "[challenge]", this.challenge.getFriendlyName());
                    }
                }
            }
        }
        else
        {
            // Item Repeat Rewards
            for (ItemStack reward : this.challenge.getRepeatItemReward())
            {
                this.user.getInventory().addItem(reward).forEach((k, v) ->
                    this.user.getWorld().dropItem(this.user.getLocation(), v));
            }

            // Money Repeat Reward
            if (this.addon.isEconomyProvided())
            {
                this.addon.getEconomyProvider().deposit(this.user, this.challenge.getRepeatMoneyReward());
            }

            // Experience Repeat Reward
            this.user.getPlayer().giveExp(this.challenge.getRepeatExperienceReward());

            // Run commands
            this.runCommands(this.challenge.getRepeatRewardCommands());

            this.user.sendMessage("challenges.you-repeated", "[challenge]", this.challenge.getFriendlyName());
        }

        // Mark as complete
        this.manager.setChallengeComplete(this.user, this.challenge);

        if (!result.repeat)
        {
            ChallengeLevel level = this.manager.getLevel(this.challenge);

            if (!this.manager.isLevelCompleted(this.user, level))
            {
                if (this.manager.validateLevelCompletion(this.user, level))
                {
                    // Item rewards
                    for (ItemStack reward : level.getRewardItems())
                    {
                        this.user.getInventory().addItem(reward).forEach((k, v) ->
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

                    this.user.sendMessage("challenges.you-completed-level", "[level]", level.getFriendlyName());

                    if (this.addon.getChallengesSettings().isBroadcastMessages())
                    {
                        for (Player player : this.addon.getServer().getOnlinePlayers())
                        {
                            // Only other players should see message.
                            if (!player.getUniqueId().equals(this.user.getUniqueId()))
                            {
                                User.getInstance(player).sendMessage("challenges.name-has-completed-level",
                                    "[name]", this.user.getName(), "[level]", level.getFriendlyName());
                            }
                        }
                    }

                    this.manager.setLevelComplete(this.user, level);
                }
            }
        }

        return result;
    }


    /**
     * Checks if a challenge can be completed or not
     * It returns ChallengeResult.
     */
    private ChallengeResult checkIfCanCompleteChallenge()
    {
        ChallengeResult result;

        ChallengeType type = this.challenge.getChallengeType();

        // Check the world
        if (!this.challenge.isDeployed())
        {
            this.user.sendMessage("challenges.error.not-deployed");
            result = EMPTY_RESULT;
        }
        else if (Util.getWorld(this.world) != Util.getWorld(this.user.getWorld()) ||
            !this.challenge.getUniqueId().startsWith(Util.getWorld(this.world).getName()))
        {
            this.user.sendMessage("general.errors.wrong-world");
            result = EMPTY_RESULT;
        }
        // Player is not on island
        else if (!this.addon.getIslands().userIsOnIsland(this.user.getWorld(), this.user))
        {
            this.user.sendMessage("challenges.error.not-on-island");
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
            this.manager.getChallengeTimes(this.user, this.challenge) >= this.challenge.getMaxTimes())
        {
            this.user.sendMessage("challenges.not-repeatable");
            result = EMPTY_RESULT;
        }
        // Check repeatability
        else if (!this.challenge.isRepeatable() && this.manager.isChallengeComplete(this.user, this.challenge))
        {
            this.user.sendMessage("challenges.not-repeatable");
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
            result = this.checkInventory();
        }
        else if (type.equals(ChallengeType.ISLAND))
        {
            result = this.checkSurrounding();
        }
        else if (type.equals(ChallengeType.OTHER))
        {
            result = this.checkOthers();
        }
        else
        {
            result = EMPTY_RESULT;
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
        return this.challenge.getRequiredPermissions().isEmpty() ||
            this.challenge.getRequiredPermissions().stream().allMatch(s -> this.user.hasPermission(s));
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
     */
    private ChallengeResult checkInventory()
    {
        // Run through inventory
        List<ItemStack> required = new ArrayList<>(this.challenge.getRequiredItems());
        for (ItemStack req : required)
        {
            // Check for FIREWORK_ROCKET, ENCHANTED_BOOK, WRITTEN_BOOK, POTION and FILLED_MAP because these have unique meta when created
            switch (req.getType())
            {
                case FIREWORK_ROCKET:
                case ENCHANTED_BOOK:
                case WRITTEN_BOOK:
                case FILLED_MAP:
                    // Get how many items are in the inventory. Item stacks amounts need to be summed
                    int numInInventory =
                        Arrays.stream(this.user.getInventory().getContents()).filter(Objects::nonNull).
                            filter(i -> i.getType().equals(req.getType())).
                            mapToInt(ItemStack::getAmount).
                            sum();

                    if (numInInventory < req.getAmount())
                    {
                        this.user.sendMessage("challenges.error.not-enough-items",
                            "[items]",
                            Util.prettifyText(req.getType().toString()));
                        return EMPTY_RESULT;
                    }
                    break;
                default:
                    // General checking
                    if (!this.user.getInventory().containsAtLeast(req, req.getAmount()))
                    {
                        this.user.sendMessage("challenges.error.not-enough-items",
                            "[items]",
                            Util.prettifyText(req.getType().toString()));
                        return EMPTY_RESULT;
                    }
            }
        }

        // If remove items, then remove them

        if (this.challenge.isTakeItems())
        {
            this.removeItems(required);
        }


        // Return the result
        return new ChallengeResult().setMeetsRequirements().setRepeat(
            this.manager.isChallengeComplete(this.user, this.challenge));
    }


    /**
     * Removes items from a user's inventory
     * @param required - a list of item stacks to be removed
     */
    Map<Material, Integer> removeItems(List<ItemStack> required)
    {
        Map<Material, Integer> removed = new HashMap<>();

        for (ItemStack req : required)
        {
            int amountToBeRemoved = req.getAmount();
            List<ItemStack> itemsInInv = Arrays.stream(user.getInventory().getContents()).
                filter(Objects::nonNull).
                filter(i -> i.getType().equals(req.getType())).
                collect(Collectors.toList());

            for (ItemStack i : itemsInInv)
            {
                if (amountToBeRemoved > 0)
                {
                    // Remove either the full amount or the remaining amount
                    if (i.getAmount() >= amountToBeRemoved)
                    {
                        i.setAmount(i.getAmount() - amountToBeRemoved);
                        removed.merge(i.getType(), amountToBeRemoved, Integer::sum);
                        amountToBeRemoved = 0;
                    }
                    else
                    {
                        removed.merge(i.getType(), i.getAmount(), Integer::sum);
                        amountToBeRemoved -= i.getAmount();
                        i.setAmount(0);
                    }
                }
            }

            if (amountToBeRemoved > 0)
            {
                this.addon.logError("Could not remove " + amountToBeRemoved + " of " + req.getType() +
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
     */
    private ChallengeResult checkSurrounding()
    {
        ChallengeResult result;

        if (!this.addon.getIslands().userIsOnIsland(this.user.getWorld(), this.user))
        {
            // Player is not on island
            this.user.sendMessage("challenges.error.not-on-island");
            result = EMPTY_RESULT;
        }
        else
        {
            // Check for items or entities in the area

            result = this.searchForEntities(this.challenge.getRequiredEntities(), this.challenge.getSearchRadius());

            if (result.meetsRequirements && !this.challenge.getRequiredBlocks().isEmpty())
            {
                // Search for items only if entities found
                result = this.searchForBlocks(this.challenge.getRequiredBlocks(), this.challenge.getSearchRadius());
            }

            if (result.meetsRequirements &&
                this.challenge.isRemoveEntities() &&
                !this.challenge.getRequiredEntities().isEmpty())
            {
                this.removeEntities();
            }

            if (result.meetsRequirements &&
                this.challenge.isRemoveBlocks() &&
                !this.challenge.getRequiredBlocks().isEmpty())
            {
                this.removeBlocks();
            }
        }

        return result;
    }


    /**
     * This method search required blocks in given radius from user position.
     * @param map RequiredBlock Map.
     * @param searchRadius Search distance
     * @return ChallengeResult
     */
    private ChallengeResult searchForBlocks(Map<Material, Integer> map, int searchRadius)
    {
        Map<Material, Integer> blocks = new EnumMap<>(map);

        for (int x = -searchRadius; x <= searchRadius; x++)
        {
            for (int y = -searchRadius; y <= searchRadius; y++)
            {
                for (int z = -searchRadius; z <= searchRadius; z++)
                {
                    Material mat = this.user.getWorld().getBlockAt(this.user.getLocation().add(new Vector(x, y, z))).getType();
                    // Remove one
                    blocks.computeIfPresent(mat, (b, amount) -> amount - 1);
                    // Remove any that have an amount of 0
                    blocks.entrySet().removeIf(en -> en.getValue() <= 0);
                }
            }
        }

        if (blocks.isEmpty())
        {
            return new ChallengeResult().setMeetsRequirements();
        }

        this.user.sendMessage("challenges.error.not-close-enough", "[number]", String.valueOf(searchRadius));

        blocks.forEach((k, v) -> user.sendMessage("challenges.error.you-still-need",
            "[amount]", String.valueOf(v),
            "[item]", Util.prettifyText(k.toString())));

        return EMPTY_RESULT;
    }


    /**
     * This method search required entities in given radius from user position.
     * @param map RequiredEntities Map.
     * @param searchRadius Search distance
     * @return ChallengeResult
     */
    private ChallengeResult searchForEntities(Map<EntityType, Integer> map, int searchRadius)
    {
        Map<EntityType, Integer> entities = map.isEmpty() ? new EnumMap<>(EntityType.class) : new EnumMap<>(map);

        this.user.getPlayer().getNearbyEntities(searchRadius, searchRadius, searchRadius).forEach(entity -> {
            // Look through all the nearby Entities, filtering by type
            entities.computeIfPresent(entity.getType(), (reqEntity, amount) -> amount - 1);
            entities.entrySet().removeIf(e -> e.getValue() == 0);
        });

        if (entities.isEmpty())
        {
            return new ChallengeResult().setMeetsRequirements();
        }

        entities.forEach((reqEnt, amount) -> this.user.sendMessage("challenges.error.you-still-need",
            "[amount]", String.valueOf(amount),
            "[item]", Util.prettifyText(reqEnt.toString())));

        return EMPTY_RESULT;
    }


    /**
     * This method removes required block and set air instead of it.
     */
    private void removeBlocks()
    {
        Map<Material, Integer> blocks = new EnumMap<>(this.challenge.getRequiredBlocks());
        int searchRadius = this.challenge.getSearchRadius();

        for (int x = -searchRadius; x <= searchRadius; x++)
        {
            for (int y = -searchRadius; y <= searchRadius; y++)
            {
                for (int z = -searchRadius; z <= searchRadius; z++)
                {
                    Block block = this.user.getWorld().getBlockAt(this.user.getLocation().add(new Vector(x, y, z)));

                    if (blocks.containsKey(block.getType()))
                    {
                        blocks.computeIfPresent(block.getType(), (b, amount) -> amount - 1);
                        blocks.entrySet().removeIf(en -> en.getValue() <= 0);

                        block.setType(Material.AIR);
                    }
                }
            }
        }
    }


    /**
     * This method removes required entities.
     */
    private void removeEntities()
    {
        Map<EntityType, Integer> entities = this.challenge.getRequiredEntities().isEmpty() ?
            new EnumMap<>(EntityType.class) : new EnumMap<>(this.challenge.getRequiredEntities());

        int searchRadius = this.challenge.getSearchRadius();

        this.user.getPlayer().getNearbyEntities(searchRadius, searchRadius, searchRadius).forEach(entity -> {
            // Look through all the nearby Entities, filtering by type

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
     */
    private ChallengeResult checkOthers()
    {
        if (!this.addon.isEconomyProvided() ||
            this.challenge.getRequiredMoney() <= 0 ||
            !this.addon.getEconomyProvider().has(this.user, this.challenge.getRequiredMoney()))
        {
            this.user.sendMessage("challenges.not-enough-money",
                "[money]",
                Integer.toString(this.challenge.getRequiredMoney()));
        }
        else if (this.challenge.getRequiredExperience() <= 0 ||
            this.user.getPlayer().getTotalExperience() < this.challenge.getRequiredExperience())
        {
            this.user.sendMessage("challenges.not-enough-exp",
                "[xp]",
                Integer.toString(this.challenge.getRequiredExperience()));
        }
        else if (!this.addon.isLevelProvided() ||
            this.addon.getLevelAddon().getIslandLevel(this.world, this.user.getUniqueId()) < this.challenge.getRequiredIslandLevel())
        {
            this.user.sendMessage("challenges.error.island-level",
                TextVariables.NUMBER,
                String.valueOf(this.challenge.getRequiredIslandLevel()));
        }
        else
        {
            if (this.addon.isEconomyProvided() && this.challenge.isTakeMoney())
            {
                this.addon.getEconomyProvider().withdraw(this.user, this.challenge.getRequiredMoney());
            }

            if (this.challenge.isTakeExperience())
            {
                this.user.getPlayer().setTotalExperience(
                    this.user.getPlayer().getTotalExperience() - this.challenge.getRequiredExperience());
            }

            return new ChallengeResult().setMeetsRequirements();
        }

        return EMPTY_RESULT;
    }


// ---------------------------------------------------------------------
// Section: Private classes
// ---------------------------------------------------------------------


    /**
     * Contains flags on completion of challenge
     *
     * @author tastybento
     */
    private class ChallengeResult
    {
        private boolean meetsRequirements;

        private boolean repeat;


        /**
         */
        ChallengeResult setMeetsRequirements()
        {
            this.meetsRequirements = true;
            return this;
        }


        /**
         * @param repeat the repeat to set
         */
        ChallengeResult setRepeat(boolean repeat)
        {
            this.repeat = repeat;
            return this;
        }
    }
}
