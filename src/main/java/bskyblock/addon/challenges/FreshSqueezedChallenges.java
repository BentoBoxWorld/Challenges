package bskyblock.addon.challenges;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import bskyblock.addon.challenges.database.object.ChallengeLevels;
import bskyblock.addon.challenges.database.object.Challenges;

public class FreshSqueezedChallenges {

    private static final boolean DEBUG = false;
    ChallengesAddon addon;
    YamlConfiguration chal;

    public FreshSqueezedChallenges(ChallengesAddon challengesAddon) {
        this.addon = challengesAddon;
        File challengeFile = new File(addon.getDataFolder(), "challenges.yml");
        if (!challengeFile.exists()) {
            addon.saveResource("challenges.yml",false);
        }
        chal = new YamlConfiguration();
        try {
            chal.load(challengeFile);
        } catch (IOException | InvalidConfigurationException e) {
            addon.getLogger().severe("Could not set up initial challenges");
        }
        makeLevels();
        makeChallenges();
        addon.getChallengesManager().save(true);
    }

    private void makeLevels() {
        // Parse the levels
        String levels = chal.getString("challenges.levels", "");
        if (!levels.isEmpty()) {
            String[] lvs = levels.split(" ");
            int order = 0;
            for (String level : lvs) {
                ChallengeLevels challengeLevel = new ChallengeLevels();
                challengeLevel.setFriendlyName(level);
                challengeLevel.setUniqueId(level);
                challengeLevel.setOrder(order++);
                challengeLevel.setWaiveramount(chal.getInt("challenges.waiveramount"));
                // Check if there is a level reward
                ConfigurationSection unlock = chal.getConfigurationSection("challenges.levelUnlock." + level);
                if (unlock != null) {
                    challengeLevel.setUnlockMessage(unlock.getString("message"));
                    challengeLevel.setRewardDescription(unlock.getString("rewardDesc",""));
                    challengeLevel.setRewardItems(parseItems(unlock.getString("itemReward","")));
                    challengeLevel.setMoneyReward(unlock.getInt("moneyReward"));
                    challengeLevel.setExpReward(unlock.getInt("expReward"));
                    challengeLevel.setRewardCommands(unlock.getStringList("commands"));
                }
                addon.getChallengesManager().storeLevel(challengeLevel);
            }
        }        
    }

    /**
     * Imports challenges
     */
    private void makeChallenges() {
        // Parse the challenge file
        ConfigurationSection chals = chal.getConfigurationSection("challenges.challengeList");
        for (String challenge : chals.getKeys(false)) {
            Challenges newChallenge = new Challenges();
            newChallenge.setUniqueId(challenge);
            ConfigurationSection details = chals.getConfigurationSection(challenge);
            newChallenge.setFriendlyName(details.getString("friendlyname", challenge));
            newChallenge.setDescription(details.getString("description", ""));
            newChallenge.setIcon(parseItem(details.getString("icon") + ":1"));
            newChallenge.setLevel(details.getString("level", ChallengesManager.FREE));
            newChallenge.setChallengeType(Challenges.ChallengeType.valueOf(details.getString("type","INVENTORY").toUpperCase()));
            newChallenge.setTakeItems(details.getBoolean("takeItems",true));
            newChallenge.setRewardText(details.getString("rewardText", ""));
            newChallenge.setRewardCommands(details.getStringList("rewardcommands"));
            newChallenge.setMoneyReward(details.getInt("moneyReward",0));
            newChallenge.setExpReward(details.getInt("expReward"));
            newChallenge.setRepeatable(details.getBoolean("repeatable"));
            newChallenge.setRepeatRewardText(details.getString("repeatRewardText",""));
            newChallenge.setRepeatMoneyReward(details.getInt("repearMoneyReward"));
            newChallenge.setRepeatExpReward(details.getInt("repeatExpReward"));
            newChallenge.setRepeatRewardCommands(details.getStringList("repeatrewardcommands"));
            newChallenge.setMaxTimes(details.getInt("maxtimes"));
            // TODO reset allowed
            newChallenge.setReqMoney(details.getInt("requiredMoney"));
            newChallenge.setReqExp(details.getInt("requiredExp"));
            if (newChallenge.getChallengeType().equals(Challenges.ChallengeType.INVENTORY)) {
                newChallenge.setRequiredItems(parseItems(details.getString("requiredItems","")));
            } else if (newChallenge.getChallengeType().equals(Challenges.ChallengeType.LEVEL)) {
                newChallenge.setReqIslandlevel(Long.parseLong(details.getString("requiredItems","")));
            } else if (newChallenge.getChallengeType().equals(Challenges.ChallengeType.ISLAND)) {
                parseEntities(newChallenge, details.getString("requiredItems",""));
            }
            newChallenge.setItemReward(parseItems(details.getString("itemReward")));
            newChallenge.setRepeatItemReward(parseItems(details.getString("repeatItemReward")));
            // Save
            addon.getChallengesManager().storeChallenge(newChallenge);
        }
        addon.getChallengesManager().sortChallenges();
    }

    private void parseEntities(Challenges challenge, String string) {
        Map<EntityType, Integer> req = new HashMap<>();
        Map<Material, Integer> blocks = new HashMap<>();
        if (!string.isEmpty()) {
            for (String s : string.split(" ")) {
                String[] part = s.split(":");
                try {
                    for (EntityType type : EntityType.values()) {
                        if (type.toString().equalsIgnoreCase(part[0])) {
                            req.put(type, Integer.valueOf(part[1]));
                            break;
                        }
                    }
                    for (Material type : Material.values()) {
                        if (type.toString().equalsIgnoreCase(part[0])) {
                           blocks.put(type, Integer.valueOf(part[1]));
                           break; 
                        }
                    }
                    
                } catch (Exception e) {
                    addon.getLogger().severe("Cannot parse '" + s + "'. Skipping...");
                }
            }
        }
        challenge.setRequiredEntities(req);
        challenge.setRequiredBlocks(blocks);
    }

    private List<ItemStack> parseItems(String reqList) {
        List<ItemStack> result = new ArrayList<>();
        if (!reqList.isEmpty()) {
            for (String s : reqList.split(" ")) {
                ItemStack item = parseItem(s);
                if (item != null) {
                    result.add(item);
                }
            }
        }
        return result;
    }

    @SuppressWarnings("deprecation")
    private ItemStack parseItem(String s) {
        Material reqItem = null;
        int reqAmount = 0;
        String[] part = s.split(":");
        // Correct some common mistakes
        if (part[0].equalsIgnoreCase("potato")) {
            part[0] = "POTATO_ITEM";
        } else if (part[0].equalsIgnoreCase("brewing_stand")) {
            part[0] = "BREWING_STAND_ITEM";
        } else if (part[0].equalsIgnoreCase("carrot")) {
            part[0] = "CARROT_ITEM";
        } else if (part[0].equalsIgnoreCase("cauldron")) {
            part[0] = "CAULDRON_ITEM";
        } else if (part[0].equalsIgnoreCase("skull")) {
            part[0] = "SKULL_ITEM";
        }
        // TODO: add netherwart vs. netherstalk?
        // Material:Qty
        if (part.length == 2) {
            try {
                if (StringUtils.isNumeric(part[0])) {
                    reqItem = Material.getMaterial(Integer.parseInt(part[0]));
                } else {
                    reqItem = Material.getMaterial(part[0].toUpperCase());
                }
                reqAmount = Integer.parseInt(part[1]);
                ItemStack item = new ItemStack(reqItem);
                if (DEBUG) {
                    addon.getLogger().info("DEBUG: required item = " + reqItem.toString());
                    addon.getLogger().info("DEBUG: item amount = " + reqAmount);
                }
                return item;

            } catch (Exception e) {
                addon.getLogger().severe("Problem with " + s + " in challenges.yml!");
            }
        } else if (part.length == 3) {
            if (DEBUG)
                addon.getLogger().info("DEBUG: Item with durability");
            if (StringUtils.isNumeric(part[0])) {
                reqItem = Material.getMaterial(Integer.parseInt(part[0]));
            } else {
                reqItem = Material.getMaterial(part[0].toUpperCase());
            }
            reqAmount = Integer.parseInt(part[2]);
            ItemStack item = new ItemStack(reqItem);
            int reqDurability = 0;
            if (StringUtils.isNumeric(part[1])) {
                reqDurability = Integer.parseInt(part[1]);
                item.setDurability((short) reqDurability);
            } else if (reqItem.equals(Material.MONSTER_EGG)) {
                reqDurability = -1; // non existent
                // Check if this is a string
                EntityType entityType = EntityType.valueOf(part[1]);
                item = new ItemStack(Material.MONSTER_EGG);
                SpawnEggMeta meta = ((SpawnEggMeta)item.getItemMeta());
                meta.setSpawnedType(entityType);
                item.setItemMeta(meta);
            }
            return item;
        } else if (part.length == 6 && part[0].contains("POTION")) {
            try {
                reqAmount = Integer.parseInt(part[5]);
                if (DEBUG)
                    addon.getLogger().info("DEBUG: required amount is " + reqAmount);
            } catch (Exception e) {
                addon.getLogger().severe("Could not parse the quantity of the potion item " + s);
                return null;
            }
            /*
             * # Format POTION:NAME:<LEVEL>:<EXTENDED>:<SPLASH/LINGER>:QTY
                # LEVEL, EXTENDED, SPLASH, LINGER are optional.
                # LEVEL is a number, 1 or 2
                # LINGER is for V1.9 servers and later
                # Examples:
                # POTION:STRENGTH:1:EXTENDED:SPLASH:1
                # POTION:INSTANT_DAMAGE:2::LINGER:2
                # POTION:JUMP:2:NOTEXTENDED:NOSPLASH:1
                # POTION:WEAKNESS::::1   -  any weakness potion
             */
            ItemStack item = part[4].isEmpty() ? new ItemStack(Material.POTION) : part[4].equalsIgnoreCase("SPLASH") 
                    ? new ItemStack(Material.SPLASH_POTION) : new ItemStack(Material.LINGERING_POTION);
                    PotionMeta potionMeta = (PotionMeta)(item.getItemMeta());
                    PotionType type = PotionType.valueOf(part[1].toUpperCase());
                    boolean isExtended = part[3].equalsIgnoreCase("EXTENDED") ? true : false;
                    boolean isUpgraded = (part[4].isEmpty() || part[4].equalsIgnoreCase("1")) ? false: true;
                    PotionData data = new PotionData(type, isExtended, isUpgraded);
                    potionMeta.setBasePotionData(data);

                    item.setAmount(reqAmount);
                    return item;

        } else {
            addon.getLogger().severe("Problem with " + s + " in challenges.yml!");
        }                
    
        return null;
        
    }

}
