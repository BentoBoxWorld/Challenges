/**
 * 
 */
package bskyblock.addon.challenges.panel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import bskyblock.addon.challenges.ChallengesAddon;
import bskyblock.addon.challenges.ChallengesManager;
import bskyblock.addon.challenges.database.object.Challenges;
import bskyblock.addon.challenges.database.object.Challenges.ChallengeType;
import bskyblock.addon.level.Level;
import us.tastybento.bskyblock.Constants;
import us.tastybento.bskyblock.api.user.User;
import us.tastybento.bskyblock.util.Util;

/**
 * Run when a user tries to complete a challenge
 * @author tastybento
 *
 */
public class TryToComplete {

    private ChallengesAddon addon;

    public TryToComplete(ChallengesAddon addon, User user, ChallengesManager manager, Challenges challenge) {
        this.addon = addon;
        Bukkit.getLogger().info("DEBUG: try to complete");
        // Check if user is in the worlds
        if (!Util.inWorld(user.getLocation())) {
            user.sendMessage("general.errors.wrong-world");
            return;
        }
        Bukkit.getLogger().info("DEBUG: right world");
        // Check if can complete challenge
        ChallengeResult result = checkIfCanCompleteChallenge(user, manager, challenge);
        if (!result.meetsRequirements) {
            Bukkit.getLogger().info("DEBUG: could not complete");
            return;
        }
        Bukkit.getLogger().info("DEBUG: Can complete!");
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
            runCommands(user, challenge.getRewardCommands());
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
            runCommands(user, challenge.getRepeatRewardCommands());
            user.sendMessage("challenges.you-repeated", "[challenge]", challenge.getFriendlyName());
        }
        // Market as complete
        manager.setChallengeComplete(user, challenge.getUniqueId());
    }

    /**
     * Checks if a challenge can be completed or not
     */
    private ChallengeResult checkIfCanCompleteChallenge(User user, ChallengesManager manager, Challenges challenge) {
        // Check if user has the 
        if (!challenge.getLevel().equals(ChallengesManager.FREE) && !manager.isLevelAvailable(user, challenge.getLevel())) {
            user.sendMessage("challenges.errors.challenge-level-not-available");
            return new ChallengeResult();
        }
        Bukkit.getLogger().info("DEBUG: Level is available or challenge is free");
        // Check max times
        if (challenge.isRepeatable() && challenge.getMaxTimes() > 0) {
            Bukkit.getLogger().info("DEBUG: repeatable and max times > 0");
            if (manager.checkChallengeTimes(user, challenge) >= challenge.getMaxTimes()) {
                user.sendMessage("challenges.errors.cannot-repeat");
                return new ChallengeResult();
            }
        }
        // Check repeatability
        if (manager.isChallengeComplete(user, challenge.getUniqueId()) 
                && (!challenge.isRepeatable() || challenge.getChallengeType().equals(ChallengeType.LEVEL)
                        || challenge.getChallengeType().equals(ChallengeType.ISLAND))) {
            user.sendMessage("challenges.errors.cannot-repeat");
            return new ChallengeResult();
        }
        Bukkit.getLogger().info("DEBUG: switch " + challenge.getChallengeType());
        switch (challenge.getChallengeType()) {
        case INVENTORY:
            return checkInventory(user, manager, challenge);
        case LEVEL:
            return checkLevel(user, manager, challenge);
        case ISLAND:
            return checkSurrounding(user, manager, challenge);
        default:
            return new ChallengeResult();
        }
    }

    private ChallengeResult checkInventory(User user, ChallengesManager manager, Challenges challenge) {
        Bukkit.getLogger().info("DEBUG: Checking inventory");
        // Run through inventory
        List<ItemStack> required = new ArrayList<>(challenge.getRequiredItems());
        for (ItemStack req : required) {
            // I wonder how well this works
            if (!user.getInventory().containsAtLeast(req, req.getAmount())) {
                Bukkit.getLogger().info("DEBUG: insufficient items " + req);
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
        Bukkit.getLogger().info("DEBUG: Everything there!");
        return new ChallengeResult().setMeetsRequirements().setRepeat(manager.isChallengeComplete(user, challenge.getUniqueId()));
    }

    private ChallengeResult checkLevel(User user, ChallengesManager manager, Challenges challenge) {
        // Check if the level addon is installed or not
        return addon.getAddonByName("BSkyBlock-Level").map(l -> {
            return ((Level)l).getIslandLevel(user.getUniqueId()) >= challenge.getReqIslandlevel() ?
                    new ChallengeResult().setMeetsRequirements() : new ChallengeResult();
        }).orElse(new ChallengeResult());
    }

    private ChallengeResult checkSurrounding(User user, ChallengesManager manager, Challenges challenge) {
        if (!addon.getIslands().playerIsOnIsland(user)) {
            // Player is not on island
            user.sendMessage("challenges.error.not-on-island");
            return new ChallengeResult();
        }
        // Check for items or entities in the area
        ChallengeResult result = searchForEntities(user, challenge.getRequiredEntities(), challenge.getSearchRadius());
        if (result.meetsRequirements) {
            // Search for items only if entities found
            result = searchForBlocks(user, challenge.getRequiredBlocks(), challenge.getSearchRadius());
        }
        return result;
    }

    private ChallengeResult searchForBlocks(User user, Map<Material, Integer> map, int searchRadius) {
        Map<Material, Integer> blocks = new HashMap<>(map);
        addon.getLogger().info("Size of blocks = " + blocks.size());
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

    private ChallengeResult searchForEntities(User user, Map<EntityType, Integer> map, int searchRadius) {
        Map<EntityType, Integer> entities = new HashMap<>(map);
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

    private void runCommands(User player, List<String> commands) {
        // Ignore commands with this perm
        if (player.hasPermission(Constants.PERMPREFIX + "command.challengeexempt") && !player.isOp()) {
            return;
        }
        for (String cmd : commands) {
            if (cmd.startsWith("[SELF]")) {
                addon.getLogger().info("Running command '" + cmd + "' as " + player.getName());
                cmd = cmd.substring(6,cmd.length()).replace("[player]", player.getName()).trim();
                try {
                    if (!player.performCommand(cmd)) {
                        addon.getLogger().severe("Problem executing island command executed by player - skipping!");
                        addon.getLogger().severe("Command was : " + cmd);
                    }
                } catch (Exception e) {
                    addon.getLogger().severe("Problem executing island command executed by player - skipping!");
                    addon.getLogger().severe("Command was : " + cmd);
                    addon.getLogger().severe("Error was: " + e.getMessage());
                    e.printStackTrace();
                }

                continue;
            }
            // Substitute in any references to player
            try {
                if (!addon.getServer().dispatchCommand(addon.getServer().getConsoleSender(), cmd.replace("[player]", player.getName()))) {
                    addon.getLogger().severe("Problem executing challenge reward commands - skipping!");
                    addon.getLogger().severe("Command was : " + cmd);
                }
            } catch (Exception e) {
                addon.getLogger().severe("Problem executing challenge reward commands - skipping!");
                addon.getLogger().severe("Command was : " + cmd);
                addon.getLogger().severe("Error was: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
