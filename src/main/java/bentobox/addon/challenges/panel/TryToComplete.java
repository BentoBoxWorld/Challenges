/**
 *
 */
package bentobox.addon.challenges.panel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import bentobox.addon.challenges.ChallengesAddon;
import bentobox.addon.challenges.ChallengesManager;
import bentobox.addon.challenges.commands.ChallengesCommand;
import bentobox.addon.challenges.database.object.Challenges;
import bentobox.addon.challenges.database.object.Challenges.ChallengeType;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.bentobox.util.Util;
import world.bentobox.level.Level;

/**
 * Run when a user tries to complete a challenge
 * @author tastybento
 *
 */
public class TryToComplete {

    private ChallengesAddon addon;
    private World world;
    private String permPrefix;
    private User user;
    private ChallengesManager manager;
    private Challenges challenge;
    private String label;

    public TryToComplete label(String label) {
        this.label = label;
        return this;
    }

    public TryToComplete user(User user) {
        this.user = user;
        return this;
    }

    public TryToComplete manager(ChallengesManager manager) {
        this.manager = manager;
        return this;
    }

    public TryToComplete challenge(Challenges challenge) {
        this.challenge = challenge;
        return this;
    }

    public TryToComplete world(World world) {
        this.world = world;
        return this;
    }

    public TryToComplete permPrefix(String prefix) {
        this.permPrefix = prefix;
        return this;
    }

    public TryToComplete(ChallengesAddon addon) {
        this.addon = addon;
    }

    public ChallengeResult build() {
        // Check if can complete challenge
        ChallengeResult result = checkIfCanCompleteChallenge();
        if (!result.meetsRequirements) {
            return result;
        }
        if (!result.repeat) {
            // Give rewards
            for (ItemStack reward : challenge.getRewardItems()) {
                user.getInventory().addItem(reward).forEach((k,v) -> user.getWorld().dropItem(user.getLocation(), v));
            }

            // Give money
            this.addon.getPlugin().getVault().ifPresent(
                vaultHook -> vaultHook.deposit(this.user, this.challenge.getRewardMoney()));

            // Give exp
            user.getPlayer().giveExp(challenge.getRewardExp());
            // Run commands
            runCommands(challenge.getRewardCommands());
            user.sendMessage("challenges.you-completed", "[challenge]", challenge.getFriendlyName());
            if (addon.getConfig().getBoolean("broadcastmessages", false)) {
                for (Player p : addon.getServer().getOnlinePlayers()) {
                    User.getInstance(p).sendMessage("challenges.name-has-completed",
                            "[name]", user.getName(), "[challenge]", challenge.getFriendlyName());
                }
            }
        } else {
            // Give rewards
            for (ItemStack reward : challenge.getRepeatItemReward()) {
                user.getInventory().addItem(reward).forEach((k,v) -> user.getWorld().dropItem(user.getLocation(), v));
            }

            // Give money
            this.addon.getPlugin().getVault().ifPresent(
                vaultHook -> vaultHook.deposit(this.user, this.challenge.getRepeatMoneyReward()));

            // Give exp
            user.getPlayer().giveExp(challenge.getRepeatExpReward());
            // Run commands
            runCommands(challenge.getRepeatRewardCommands());
            user.sendMessage("challenges.you-repeated", "[challenge]", challenge.getFriendlyName());
        }
        // Mark as complete
        manager.setChallengeComplete(user, challenge.getUniqueId(), world);
        user.closeInventory();
        user.getPlayer().performCommand(label + " " + ChallengesCommand.CHALLENGE_COMMAND + " " + challenge.getLevel());
        return result;
    }

    /**
     * @param addon
     * @param user
     * @param manager
     * @param challenge
     * @param world
     * @param permPrefix
     */
    public TryToComplete(ChallengesAddon addon, User user, ChallengesManager manager, Challenges challenge, World world, String permPrefix, String label) {
        this.addon = addon;
        this.world = world;
        this.permPrefix = permPrefix;
        this.user = user;
        this.manager = manager;
        this.challenge = challenge;

        // Check if can complete challenge
        ChallengeResult result = checkIfCanCompleteChallenge();
        if (!result.meetsRequirements) {
            return;
        }
        if (!result.repeat) {
            // Give rewards
            for (ItemStack reward : challenge.getRewardItems()) {
                user.getInventory().addItem(reward).forEach((k,v) -> user.getWorld().dropItem(user.getLocation(), v));
            }

            // Give money
            this.addon.getPlugin().getVault().ifPresent(
                vaultHook -> vaultHook.deposit(this.user, this.challenge.getRewardMoney()));

            // Give exp
            user.getPlayer().giveExp(challenge.getRewardExp());
            // Run commands
            runCommands(challenge.getRewardCommands());
            user.sendMessage("challenges.you-completed", "[challenge]", challenge.getFriendlyName());
            if (addon.getConfig().getBoolean("broadcastmessages", false)) {
                for (Player p : addon.getServer().getOnlinePlayers()) {
                    User.getInstance(p).sendMessage("challenges.name-has-completed",
                            "[name]", user.getName(), "[challenge]", challenge.getFriendlyName());
                }
            }
        } else {
            // Give rewards
            for (ItemStack reward : challenge.getRepeatItemReward()) {
                user.getInventory().addItem(reward).forEach((k,v) -> user.getWorld().dropItem(user.getLocation(), v));
            }

            // Give money
            this.addon.getPlugin().getVault().ifPresent(
                vaultHook -> vaultHook.deposit(this.user, this.challenge.getRepeatMoneyReward()));

            // Give exp
            user.getPlayer().giveExp(challenge.getRepeatExpReward());
            // Run commands
            runCommands(challenge.getRepeatRewardCommands());
            user.sendMessage("challenges.you-repeated", "[challenge]", challenge.getFriendlyName());
        }
        // Mark as complete
        manager.setChallengeComplete(user, challenge.getUniqueId(), world);
        user.closeInventory();
        user.getPlayer().performCommand(label + " " + ChallengesCommand.CHALLENGE_COMMAND + " " + challenge.getLevel());
    }

    /**
     * Checks if a challenge can be completed or not
     */
    private ChallengeResult checkIfCanCompleteChallenge() {
        // Check the world
        if (!Util.getWorld(user.getWorld()).getName().equalsIgnoreCase(challenge.getWorld())) {
            user.sendMessage("general.errors.wrong-world");
            return new ChallengeResult();
        }
        // Check if user has the
        if (!challenge.getLevel().equals(ChallengesManager.FREE) && !manager.isLevelUnlocked(user, challenge.getLevel(), world)) {
            user.sendMessage("challenges.errors.challenge-level-not-available");
            return new ChallengeResult();
        }
        // Check max times
        if (challenge.isRepeatable() && challenge.getMaxTimes() > 0 && manager.checkChallengeTimes(user, challenge, world) >= challenge.getMaxTimes()) {
            user.sendMessage("challenges.not-repeatable");
            return new ChallengeResult();
        }
        // Check repeatability
        if (manager.isChallengeComplete(user, challenge.getUniqueId(), world)
                && (!challenge.isRepeatable() || challenge.getChallengeType().equals(ChallengeType.LEVEL)
                        || challenge.getChallengeType().equals(ChallengeType.ISLAND))) {
            user.sendMessage("challenges.not-repeatable");
            return new ChallengeResult();
        }

        // Check money
        Optional<VaultHook> vaultHook = this.addon.getPlugin().getVault();

        if (vaultHook.isPresent())
        {
            if (!vaultHook.get().has(this.user, this.challenge.getReqMoney()))
            {
                this.user.sendMessage("challenges.not-enough-money", "[money]", Integer.toString(this.challenge.getReqMoney()));
                return new ChallengeResult();
            }
        }

        // Check exp
        if (this.user.getPlayer().getTotalExperience() < this.challenge.getReqExp())
        {
            this.user.sendMessage("challenges.not-enough-exp", "[xp]", Integer.toString(this.challenge.getReqExp()));
            return new ChallengeResult();
        }

        switch (challenge.getChallengeType()) {
        case INVENTORY:
            return checkInventory();
        case LEVEL:
            return checkLevel();
        case ISLAND:
            return checkSurrounding();
        default:
            return new ChallengeResult();
        }
    }

    private ChallengeResult checkInventory() {
        // Run through inventory
        List<ItemStack> required = new ArrayList<>(challenge.getRequiredItems());
        for (ItemStack req : required) {
            // Check for FIREWORK_ROCKET, ENCHANTED_BOOK, WRITTEN_BOOK, POTION and FILLED_MAP because these have unique meta when created
            switch (req.getType()) {
            case FIREWORK_ROCKET:
            case ENCHANTED_BOOK:
            case WRITTEN_BOOK:
            case FILLED_MAP:
                // Get how many items are in the inventory. Item stacks amounts need to be summed
                int numInInventory = Arrays.stream(user.getInventory().getContents()).filter(Objects::nonNull).filter(i -> i.getType().equals(req.getType())).mapToInt(i -> i.getAmount()).sum();
                if (numInInventory < req.getAmount()) {
                    user.sendMessage("challenges.error.not-enough-items", "[items]", Util.prettifyText(req.getType().toString()));
                    return new ChallengeResult();
                }
                break;
            default:
                // General checking
                if (!user.getInventory().containsAtLeast(req, req.getAmount())) {
                    user.sendMessage("challenges.error.not-enough-items", "[items]", Util.prettifyText(req.getType().toString()));
                    return new ChallengeResult();
                }
            }

        }
        // If remove items, then remove them
        if (challenge.isTakeItems()) {
            removeItems(required);

        }

        // process money removal
        this.removeMoney();

        // Return the result
        return new ChallengeResult().setMeetsRequirements().setRepeat(manager.isChallengeComplete(user, challenge.getUniqueId(), world));
    }


    /**
     * This method withdraw user money, if challenge Required Money is larger then 0, and
     * it is set to removal.
     * This works only if vaultHook is enabled.
      */
    private void removeMoney()
    {
        Optional<VaultHook> vaultHook = this.addon.getPlugin().getVault();

        if (vaultHook.isPresent() &&
            this.challenge.isTakeMoney() &&
            this.challenge.getReqMoney() > 0)
        {
            vaultHook.get().withdraw(this.user, this.challenge.getReqMoney());
        }
    }


    /**
     * Removes items from a user's inventory
     * @param required - a list of item stacks to be removed
     * @return Map of item type and quantity that were successfully removed from the user's inventory
     */
    public Map<Material, Integer> removeItems(List<ItemStack> required) {
        Map<Material, Integer> removed = new HashMap<>();
        for (ItemStack req : required) {
            int amountToBeRemoved = req.getAmount();
            List<ItemStack> itemsInInv = Arrays.stream(user.getInventory().getContents()).filter(Objects::nonNull).filter(i -> i.getType().equals(req.getType())).collect(Collectors.toList());
            for (ItemStack i : itemsInInv) {
                if (amountToBeRemoved > 0) {
                    // Remove either the full amount or the remaining amount
                    if (i.getAmount() >= amountToBeRemoved) {
                        i.setAmount(i.getAmount() - amountToBeRemoved);
                        removed.merge(i.getType(), amountToBeRemoved, Integer::sum);
                        amountToBeRemoved = 0;
                    } else {
                        removed.merge(i.getType(), i.getAmount(), Integer::sum);
                        amountToBeRemoved -= i.getAmount();
                        i.setAmount(0);

                    }
                }
            }
            if (amountToBeRemoved > 0) {
                addon.logError("Could not remove " + amountToBeRemoved + " of " + req.getType() + " from player's inventory!");
            }
        }
        return removed;
    }

    private ChallengeResult checkLevel() {
        // Check if the level addon is installed or not
        long level = addon.getAddonByName("Level")
                .map(l -> ((Level)l).getIslandLevel(world, user.getUniqueId())).orElse(0L);
        if (level >= challenge.getReqIslandlevel()) {
            // process money removal
            this.removeMoney();
            return new ChallengeResult().setMeetsRequirements();
        } else {
            user.sendMessage("challenges.error.island-level", TextVariables.NUMBER, String.valueOf(challenge.getReqIslandlevel()));
            return new ChallengeResult();
        }
    }

    private ChallengeResult checkSurrounding() {
        if (!addon.getIslands().userIsOnIsland(world, user)) {
            // Player is not on island
            user.sendMessage("challenges.error.not-on-island");
            return new ChallengeResult();
        }
        // Check for items or entities in the area
        ChallengeResult result = searchForEntities(challenge.getRequiredEntities(), challenge.getSearchRadius());
        if (result.meetsRequirements && !challenge.getRequiredBlocks().isEmpty()) {
            // Search for items only if entities found
            result = searchForBlocks(challenge.getRequiredBlocks(), challenge.getSearchRadius());
        }

        if (result.meetsRequirements && this.challenge.isTakeMoney())
        {
            // process money removal
            this.removeMoney();
        }

        return result;
    }

    private ChallengeResult searchForBlocks(Map<Material, Integer> map, int searchRadius) {
        Map<Material, Integer> blocks = new EnumMap<>(map);
        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    Material mat = user.getWorld().getBlockAt(user.getLocation().add(new Vector(x,y,z))).getType();
                    // Remove one
                    blocks.computeIfPresent(mat, (b, amount) -> amount - 1);
                    // Remove any that have an amount of 0
                    blocks.entrySet().removeIf(en -> en.getValue() <= 0);
                }
            }
        }
        if (blocks.isEmpty()) {
            return new ChallengeResult().setMeetsRequirements();
        }
        user.sendMessage("challenges.error.not-close-enough", "[number]", String.valueOf(searchRadius));
        blocks.forEach((k,v) -> user.sendMessage("challenges.error.you-still-need",
                "[amount]", String.valueOf(v),
                "[item]", Util.prettifyText(k.toString())));

        return new ChallengeResult();
    }

    private ChallengeResult searchForEntities(Map<EntityType, Integer> map, int searchRadius) {
        Map<EntityType, Integer> entities = map.isEmpty() ? new EnumMap<>(EntityType.class) : new EnumMap<>(map);
        user.getPlayer().getNearbyEntities(searchRadius, searchRadius, searchRadius).forEach(entity -> {
            // Look through all the nearby Entities, filtering by type
            entities.computeIfPresent(entity.getType(), (reqEntity, amount) -> amount - 1);
            entities.entrySet().removeIf(e -> e.getValue() == 0);
        });
        if (entities.isEmpty()) {
            return new ChallengeResult().setMeetsRequirements();
        }
        entities.forEach((reqEnt, amount) -> user.sendMessage("challenges.error.you-still-need",
                "[amount]", String.valueOf(amount),
                "[item]", Util.prettifyText(reqEnt.toString())));
        return new ChallengeResult();
    }


    /**
     * Contains flags on completion of challenge
     * @author tastybento
     *
     */
    public class ChallengeResult {
        private boolean meetsRequirements;
        private boolean repeat;
        /**
         * @param meetsRequirements the meetsRequirements to set
         */
        public ChallengeResult setMeetsRequirements() {
            this.meetsRequirements = true;
            return this;
        }
        /**
         * @param repeat the repeat to set
         */
        public ChallengeResult setRepeat(boolean repeat) {
            this.repeat = repeat;
            return this;
        }

    }

    private void runCommands(List<String> commands) {
        // Ignore commands with this perm
        if (user.hasPermission(permPrefix + "command.challengeexempt") && !user.isOp()) {
            return;
        }
        for (String cmd : commands) {
            if (cmd.startsWith("[SELF]")) {
                String alert = "Running command '" + cmd + "' as " + user.getName();
                addon.getLogger().info(alert);
                cmd = cmd.substring(6,cmd.length()).replace("[player]", user.getName()).trim();
                try {
                    if (!user.performCommand(cmd)) {
                        showError(cmd);
                    }
                } catch (Exception e) {
                    showError(cmd);
                }

                continue;
            }
            // Substitute in any references to player
            try {
                if (!addon.getServer().dispatchCommand(addon.getServer().getConsoleSender(), cmd.replace("[player]", user.getName()))) {
                    showError(cmd);
                }
            } catch (Exception e) {
                showError(cmd);
            }
        }
    }

    private void showError(final String cmd) {
        addon.getLogger().severe("Problem executing command executed by player - skipping!");
        addon.getLogger().severe(() -> "Command was : " + cmd);

    }
}
