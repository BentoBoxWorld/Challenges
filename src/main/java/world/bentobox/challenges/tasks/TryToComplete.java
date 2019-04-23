/**
 *
 */
package world.bentobox.challenges.tasks;


import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import java.util.*;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
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

            this.user.sendMessage("challenges.messages.you-completed-challenge", "[value]", this.challenge.getFriendlyName());

            if (this.addon.getChallengesSettings().isBroadcastMessages())
            {
                for (Player player : this.addon.getServer().getOnlinePlayers())
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
        }
        else
        {
            // Item Repeat Rewards
            for (ItemStack reward : this.challenge.getRepeatItemReward())
            {
                // Clone is necessary because otherwise it will chane reward itemstack
                // amount.
                this.user.getInventory().addItem(reward.clone()).forEach((k, v) ->
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

            this.user.sendMessage("challenges.messages.you-repeated-challenge", "[value]", this.challenge.getFriendlyName());
        }

        // Mark as complete
        this.manager.setChallengeComplete(this.user, this.world, this.challenge);

        if (!result.repeat)
        {
            ChallengeLevel level = this.manager.getLevel(this.challenge);

            if (!this.manager.isLevelCompleted(this.user, this.world, level))
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
            this.user.sendMessage("challenges.errors.not-deployed");
            result = EMPTY_RESULT;
        }
        else if (Util.getWorld(this.world) != Util.getWorld(this.user.getWorld()) ||
            !this.challenge.getUniqueId().startsWith(Util.getWorld(this.world).getName()))
        {
            this.user.sendMessage("general.errors.wrong-world");
            result = EMPTY_RESULT;
        }
        // Player is not on island
        else if (ChallengesAddon.CHALLENGES_WORLD_PROTECTION.isSetForWorld(this.world) &&
            !this.addon.getIslands().userIsOnIsland(this.user.getWorld(), this.user))
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
        List<ItemStack> requiredItems = new ArrayList<>(this.challenge.getRequiredItems().size());

        // Players in creative game mode has got all items. No point to search for them.
        if (this.user.getPlayer().getGameMode() != GameMode.CREATIVE)
        {
            // Group all equal items in singe stack, as otherwise it will be too complicated to check if all
            // items are in players inventory.
            for (ItemStack item : this.challenge.getRequiredItems())
            {
                boolean isUnique = true;

                int i = 0;
                final int requiredSize = requiredItems.size();

                while (i < requiredSize && isUnique)
                {
                    ItemStack required = requiredItems.get(i);

                    // Merge items which meta can be ignored or is similar to item in required list.
                    if (this.canIgnoreMeta(item.getType()) && item.getType().equals(required.getType()) ||
                        required.isSimilar(item))
                    {
                        required.setAmount(required.getAmount() + item.getAmount());
                        isUnique = false;
                    }

                    i++;
                }

                if (isUnique)
                {
                    // The same issue as in other places. Clone prevents from changing original item.
                    requiredItems.add(item.clone());
                }
            }

            int sumEverything = 0;

            // Check if all required items are in players inventory.
            for (ItemStack required : requiredItems)
            {
                if (this.canIgnoreMeta(required.getType()))
                {
                    int numInInventory =
                        Arrays.stream(this.user.getInventory().getContents()).
                            filter(Objects::nonNull).
                            filter(i -> i.getType().equals(required.getType())).
                            mapToInt(ItemStack::getAmount).
                            sum();

                    if (numInInventory < required.getAmount())
                    {
                        this.user.sendMessage("challenges.errors.not-enough-items",
                            "[items]",
                            Util.prettifyText(required.getType().toString()));
                        return EMPTY_RESULT;
                    }
                }
                else
                {
                    if (!this.user.getInventory().containsAtLeast(required, required.getAmount()))
                    {
                        this.user.sendMessage("challenges.errors.not-enough-items",
                            "[items]",
                            Util.prettifyText(required.getType().toString()));
                        return EMPTY_RESULT;
                    }
                }

                sumEverything += required.getAmount();
            }

            // If remove items, then remove them
            if (this.challenge.isTakeItems())
            {
                Map<Material, Integer> removedItems = this.removeItems(requiredItems);

                int removedAmount = removedItems.values().stream().mapToInt(num -> num).sum();

                // Something is not removed.
                if (sumEverything != removedAmount)
                {
                    this.user.sendMessage("challenges.errors.cannot-remove-items");
                    // TODO: Necessary to implement returning removed items.

                    return EMPTY_RESULT;
                }
            }
        }

        // Return the result
        return new ChallengeResult().setMeetsRequirements().setRepeat(
            this.manager.isChallengeComplete(this.user, this.world, this.challenge));
    }


    /**
     * Removes items from a user's inventory
     * @param requiredItemList - a list of item stacks to be removed
     */
    Map<Material, Integer> removeItems(List<ItemStack> requiredItemList)
    {
        Map<Material, Integer> removed = new HashMap<>();

        for (ItemStack required : requiredItemList)
        {
            int amountToBeRemoved = required.getAmount();

            List<ItemStack> itemsInInventory;

            if (this.canIgnoreMeta(required.getType()))
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

            for (ItemStack i : itemsInInventory)
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
                this.addon.logError("Could not remove " + amountToBeRemoved + " of " + required.getType() +
                    " from player's inventory!");
            }
        }

        return removed;
    }


    /**
     * This method returns if meta data of these items can be ignored. It means, that items will be searched
     * and merged by they type instead of using ItemStack#isSimilar(ItemStack) method.
     *
     * This limits custom Challenges a lot. It comes from ASkyBlock times, and that is the reason why it is
     * still here. It would be a great Challenge that could be completed by collecting 4 books, that cannot
     * be crafted. Unfortunately, this prevents it.
     * The same happens with firework rockets, enchanted books and filled maps.
     * In future it should be able to specify, which items meta should be ignored when adding item in required
     * item list.
     *
     * @param material Material that need to be checked.
     * @return True if material meta can be ignored, otherwise false.
     */
    private boolean canIgnoreMeta(Material material)
    {
        return material.equals(Material.FIREWORK_ROCKET) ||
            material.equals(Material.ENCHANTED_BOOK) ||
            material.equals(Material.WRITTEN_BOOK) ||
            material.equals(Material.FILLED_MAP);
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
        Location playerLocation = this.user.getLocation();

        // Init location in player position.
        BoundingBox boundingBox = new BoundingBox(playerLocation.getBlockX(),
            playerLocation.getBlockY(),
            playerLocation.getBlockZ(),
            playerLocation.getBlockX(),
            playerLocation.getBlockY(),
            playerLocation.getBlockZ());

        // Expand position with search radius.
        boundingBox.expand(this.challenge.getSearchRadius());

        if (ChallengesAddon.CHALLENGES_WORLD_PROTECTION.isSetForWorld(this.world))
        {
            // Players should not be able to complete challenge if they stay near island with required blocks.

            Island island = this.addon.getIslands().getIsland(this.world, this.user);

            if (boundingBox.getMinX() < island.getMinX())
            {
                boundingBox.expand(BlockFace.EAST, Math.abs(island.getMinX() - boundingBox.getMinX()));
            }

            if (boundingBox.getMinZ() < island.getMinZ())
            {
                boundingBox.expand(BlockFace.NORTH, Math.abs(island.getMinZ() - boundingBox.getMinZ()));
            }

            int range = island.getRange();

            int islandMaxX = island.getMinX() + range * 2;
            int islandMaxZ = island.getMinZ() + range * 2;

            if (boundingBox.getMaxX() > islandMaxX)
            {
                boundingBox.expand(BlockFace.WEST, Math.abs(boundingBox.getMaxX() - islandMaxX));
            }

            if (boundingBox.getMaxZ() > islandMaxZ)
            {
                boundingBox.expand(BlockFace.SOUTH, Math.abs(boundingBox.getMaxZ() - islandMaxZ));
            }
        }

        ChallengeResult result = this.searchForEntities(this.challenge.getRequiredEntities(), boundingBox);

        if (result.meetsRequirements && !this.challenge.getRequiredBlocks().isEmpty())
        {
            // Search for items only if entities found
            result = this.searchForBlocks(this.challenge.getRequiredBlocks(), boundingBox);
        }

        if (result.meetsRequirements &&
            this.challenge.isRemoveEntities() &&
            !this.challenge.getRequiredEntities().isEmpty())
        {
            this.removeEntities(boundingBox);
        }

        if (result.meetsRequirements &&
            this.challenge.isRemoveBlocks() &&
            !this.challenge.getRequiredBlocks().isEmpty())
        {
            this.removeBlocks(boundingBox);
        }

        // Check if challenge is repeated.
        result.setRepeat(this.manager.isChallengeComplete(this.user, this.world, this.challenge));

        return result;
    }


    /**
     * This method search required blocks in given challenge boundingBox.
     * @param map RequiredBlock Map.
     * @param boundingBox Bounding box of island challenge
     * @return ChallengeResult
     */
    private ChallengeResult searchForBlocks(Map<Material, Integer> map, BoundingBox boundingBox)
    {
        Map<Material, Integer> blocks = new EnumMap<>(map);

        if (blocks.isEmpty())
        {
            return new ChallengeResult().setMeetsRequirements();
        }

        for (int x = (int) boundingBox.getMinX(); x <= boundingBox.getMaxX(); x++)
        {
            for (int y = (int) boundingBox.getMinY(); y <= boundingBox.getMaxY(); y++)
            {
                for (int z = (int) boundingBox.getMinZ(); z <= boundingBox.getMaxZ(); z++)
                {
                    Material mat = this.user.getWorld().getBlockAt(x, y, z).getType();
                    // Remove one
                    blocks.computeIfPresent(mat, (b, amount) -> amount - 1);
                    // Remove any that have an amount of 0
                    blocks.entrySet().removeIf(en -> en.getValue() <= 0);

                    if (blocks.isEmpty())
                    {
                        // Return as soon as it s empty as no point to search more.
                        return new ChallengeResult().setMeetsRequirements();
                    }
                }
            }
        }

        if (blocks.isEmpty())
        {
            return new ChallengeResult().setMeetsRequirements();
        }

        this.user.sendMessage("challenges.errors.not-close-enough", "[number]", String.valueOf(this.challenge.getSearchRadius()));

        blocks.forEach((k, v) -> user.sendMessage("challenges.errors.you-still-need",
            "[amount]", String.valueOf(v),
            "[item]", Util.prettifyText(k.toString())));

        return EMPTY_RESULT;
    }


    /**
     * This method search required entities in given radius from user position and entity is inside boundingBox.
     * @param map RequiredEntities Map.
     * @param boundingBox Bounding box of island challenge
     * @return ChallengeResult
     */
    private ChallengeResult searchForEntities(Map<EntityType, Integer> map, BoundingBox boundingBox)
    {
        Map<EntityType, Integer> entities = map.isEmpty() ? new EnumMap<>(EntityType.class) : new EnumMap<>(map);

        if (entities.isEmpty())
        {
            return new ChallengeResult().setMeetsRequirements();
        }

        int searchRadius = this.challenge.getSearchRadius();

        this.user.getPlayer().getNearbyEntities(searchRadius, searchRadius, searchRadius).forEach(entity -> {
            // Check if entity is inside challenge bounding box
            if (boundingBox.contains(entity.getBoundingBox()))
            {
                // Look through all the nearby Entities, filtering by type
                entities.computeIfPresent(entity.getType(), (reqEntity, amount) -> amount - 1);
                entities.entrySet().removeIf(e -> e.getValue() == 0);
            }
        });

        if (entities.isEmpty())
        {
            return new ChallengeResult().setMeetsRequirements();
        }

        entities.forEach((reqEnt, amount) -> this.user.sendMessage("challenges.errors.you-still-need",
            "[amount]", String.valueOf(amount),
            "[item]", Util.prettifyText(reqEnt.toString())));

        return EMPTY_RESULT;
    }


    /**
     * This method removes required block and set air instead of it.
     * @param boundingBox Bounding box of island challenge
     */
    private void removeBlocks(BoundingBox boundingBox)
    {
        Map<Material, Integer> blocks = new EnumMap<>(this.challenge.getRequiredBlocks());

        for (int x = (int) boundingBox.getMinX(); x <= boundingBox.getMaxX(); x++)
        {
            for (int y = (int) boundingBox.getMinY(); y <= boundingBox.getMaxY(); y++)
            {
                for (int z = (int) boundingBox.getMinZ(); z <= boundingBox.getMaxZ(); z++)
                {
                    Block block = this.user.getWorld().getBlockAt(new Location(this.user.getWorld(), x, y, z));

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
     * @param boundingBox Bounding box of island challenge
     */
    private void removeEntities(BoundingBox boundingBox)
    {
        Map<EntityType, Integer> entities = this.challenge.getRequiredEntities().isEmpty() ?
            new EnumMap<>(EntityType.class) : new EnumMap<>(this.challenge.getRequiredEntities());

        int searchRadius = this.challenge.getSearchRadius();

        this.user.getPlayer().getNearbyEntities(searchRadius, searchRadius, searchRadius).forEach(entity -> {
            // Look through all the nearby Entities, filtering by type

            if (entities.containsKey(entity.getType()) && boundingBox.contains(entity.getBoundingBox()))
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
    	if (!this.addon.isLevelProvided() && 
			this.challenge.getRequiredIslandLevel() != 0)
		{
			this.user.sendMessage("challenges.errors.missing-addon");
		}
    	else if (!this.addon.isEconomyProvided() && 
			this.challenge.getRequiredMoney() != 0)
		{
			this.user.sendMessage("challenges.errors.missing-addon");
		}
		else if (this.addon.isEconomyProvided() && this.challenge.getRequiredMoney() < 0)
		{
			this.user.sendMessage("challenges.errors.incorrect");
		}
    	else if (this.addon.isEconomyProvided() && 
			!this.addon.getEconomyProvider().has(this.user, this.challenge.getRequiredMoney()))
        {
            this.user.sendMessage("challenges.errors.not-enough-money",
                "[value]",
                Integer.toString(this.challenge.getRequiredMoney()));
        }
		else if (this.challenge.getRequiredExperience() < 0)
		{
			this.user.sendMessage("challenges.errors.incorrect");
		}
        else if (this.user.getPlayer().getTotalExperience() < this.challenge.getRequiredExperience() &&
            this.user.getPlayer().getGameMode() != GameMode.CREATIVE)
        {
            // Players in creative gamemode has infinite amount of EXP.

            this.user.sendMessage("challenges.errors.not-enough-experience",
                "[value]",
                Integer.toString(this.challenge.getRequiredExperience()));
        }
        else if (this.addon.isLevelProvided() && 
			this.addon.getLevelAddon().getIslandLevel(this.world, this.user.getUniqueId()) < this.challenge.getRequiredIslandLevel())
        {
            this.user.sendMessage("challenges.errors.island-level",
                TextVariables.NUMBER,
                String.valueOf(this.challenge.getRequiredIslandLevel()));
        }
        else
        {
            if (this.addon.isEconomyProvided() && this.challenge.isTakeMoney())
            {
                this.addon.getEconomyProvider().withdraw(this.user, this.challenge.getRequiredMoney());
            }

            if (this.challenge.isTakeExperience() &&
                this.user.getPlayer().getGameMode() != GameMode.CREATIVE)
            {
                // Cannot take anything from creative game mode.
                this.user.getPlayer().setTotalExperience(
                    this.user.getPlayer().getTotalExperience() - this.challenge.getRequiredExperience());
            }

            return new ChallengeResult().setMeetsRequirements().
                setRepeat(this.manager.isChallengeComplete(this.user, this.world, this.challenge));
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
