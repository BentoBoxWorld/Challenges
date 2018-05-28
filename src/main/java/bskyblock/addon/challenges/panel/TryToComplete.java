/**
 * 
 */
package bskyblock.addon.challenges.panel;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import bskyblock.addon.challenges.ChallengesAddon;
import bskyblock.addon.challenges.ChallengesManager;
import bskyblock.addon.challenges.commands.ChallengesCommand;
import bskyblock.addon.challenges.database.object.Challenges;
import bskyblock.addon.challenges.database.object.Challenges.ChallengeType;
import bskyblock.addon.level.Level;
import us.tastybento.bskyblock.api.user.User;
import us.tastybento.bskyblock.util.Util;

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
            for (ItemStack reward : challenge.getItemReward()) {
                user.getInventory().addItem(reward).forEach((k,v) -> user.getWorld().dropItem(user.getLocation(), v));
            }
            // Give money
            challenge.getMoneyReward();
            // Give exp
            user.getPlayer().giveExp(challenge.getExpReward());
            // Run commands
            runCommands(challenge.getRewardCommands());
            user.sendMessage("challenges.you-completed", "[challenge]", challenge.getFriendlyName());
        } else {
            // Give rewards
            for (ItemStack reward : challenge.getRepeatItemReward()) {
                user.getInventory().addItem(reward).forEach((k,v) -> user.getWorld().dropItem(user.getLocation(), v));
            }
            // Give money
            challenge.getRepeatMoneyReward();
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
            // I wonder how well this works
            if (!user.getInventory().containsAtLeast(req, req.getAmount())) {
                user.sendMessage("challenges.error.not-enough-items", "[items]", Util.prettifyText(req.getType().toString()));
                return new ChallengeResult();
            }
        }
        // If remove items, then remove them
        if (challenge.isTakeItems()) {
            for (ItemStack items : required) {
                user.getInventory().removeItem(items);
            }
        }
        return new ChallengeResult().setMeetsRequirements().setRepeat(manager.isChallengeComplete(user, challenge.getUniqueId(), world));
    }

    private ChallengeResult checkLevel() {
        // Check if the level addon is installed or not
        return addon.getAddonByName("BSkyBlock-Level")
                .map(l -> ((Level)l).getIslandLevel(world, user.getUniqueId()) >= challenge.getReqIslandlevel() ? new ChallengeResult().setMeetsRequirements() : new ChallengeResult()
                        ).orElse(new ChallengeResult());
    }

    private ChallengeResult checkSurrounding() {
        if (!addon.getIslands().userIsOnIsland(world, user)) {
            // Player is not on island
            user.sendMessage("challenges.error.not-on-island");
            return new ChallengeResult();
        }
        // Check for items or entities in the area
        ChallengeResult result = searchForEntities(challenge.getRequiredEntities(), challenge.getSearchRadius());
        if (result.meetsRequirements) {
            // Search for items only if entities found
            result = searchForBlocks(challenge.getRequiredBlocks(), challenge.getSearchRadius());
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
        Map<EntityType, Integer> entities = new EnumMap<>(map);
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
