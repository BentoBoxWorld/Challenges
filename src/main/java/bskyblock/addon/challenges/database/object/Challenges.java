package bskyblock.addon.challenges.database.object;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import bskyblock.addon.challenges.ChallengesManager;
import us.tastybento.bskyblock.api.configuration.ConfigComment;
import us.tastybento.bskyblock.database.objects.DataObject;

public class Challenges implements DataObject {

    public enum ChallengeType {
        /**
         * This challenge only shows and icon in the GUI and doesn't do anything.
         */
        ICON,
        /**
         * The player must have the items on them.
         */
        INVENTORY,
        /**
         * The island level has to be equal or over this amount. Only works if there's an island level plugin installed.
         */
        LEVEL,
        /**
         * Items or required entities have to be within x blocks of the player.
         */
        ISLAND
    }
    
    // The order of the fields is the order shown in the YML files
    @ConfigComment("Whether this challenge is deployed or not")
    private boolean deployed;
    
    @ConfigComment("Worlds that this challenge will run in. String list. List only overworld. Nether and end are automatically covered.")
    private List<String> worlds = new ArrayList<>();
    
    // Description
    @ConfigComment("Name of the icon and challenge. May include color codes. Single line.")
    private String friendlyName = "";
    @ConfigComment("Description of the challenge. Will become the lore on the icon. Can include & color codes. String List.")
    private List<String> description = new ArrayList<>();    
    @ConfigComment("The icon in the GUI for this challenge. ItemStack.")
    private ItemStack icon = new ItemStack(Material.PAPER);
    @ConfigComment("Icon slot where this challenge should be placed. 0 to 49. A negative value means any slot")
    private int slot = -1;   

    // Definition
    @ConfigComment("Challenge level. Default is Free")
    private String level = ChallengesManager.FREE;
    @ConfigComment("Challenge type can be ICON, INVENTORY, LEVEL or SURROUNDING.")
    private ChallengeType challengeType = ChallengeType.INVENTORY;
    @ConfigComment("List of environments where this challenge will occur: NETHER, NORMAL, THE_END. Leave blank for all.")
    private List<World.Environment> environment = new ArrayList<>();    
    @ConfigComment("The required permissions to see this challenge. String Set.")
    private Set<String> reqPerms = new HashSet<>();
    @ConfigComment("The number of blocks around the player to search for items on an island")
    private int searchRadius = 10;
    @ConfigComment("If true, the challenge will disappear from the GUI when completed")
    private boolean removeWhenCompleted;
    @ConfigComment("Take the required items from the player")
    private boolean takeItems = true;
    @ConfigComment("Take the money from the player")
    private boolean takeMoney = false;

    // Requirements
    @ConfigComment("This is a map of the blocks required in a SURROUNDING challenge. Material, Integer")
    private Map<Material, Integer> requiredBlocks = new EnumMap<>(Material.class);
    @ConfigComment("The items that must be in the inventory to complete the challenge. ItemStack List.")
    private List<ItemStack> requiredItems = new ArrayList<>();  
    @ConfigComment("Any entities that must be in the area for SURROUNDING type challenges. Map EntityType, Number")
    private Map<EntityType, Integer> requiredEntities = new EnumMap<>(EntityType.class);
    @ConfigComment("Required experience")
    private int reqExp;
    @ConfigComment("Required island level for this challenge. Only works if Level Addon is being used.")
    private long reqIslandlevel;
    @ConfigComment("Required money")
    private int reqMoney;

    // Rewards
    @ConfigComment("List of items the player will receive first time. ItemStack List.")
    private List<ItemStack> itemReward = new ArrayList<>();
    @ConfigComment("If this is blank, the reward text will be auto-generated, otherwise this will be used.")
    private String rewardText = "";
    @ConfigComment("Experience point reward")
    private int expReward;
    @ConfigComment("Money reward")
    private int moneyReward;
    @ConfigComment("Commands to run when the player completes the challenge for the first time. String List")
    private List<String> rewardCommands = new ArrayList<>();

    // Repeatable
    @ConfigComment("True if the challenge is repeatable")
    private boolean repeatable;
    @ConfigComment("Maximum number of times the challenge can be repeated")
    private int maxTimes = 1;
    @ConfigComment("Repeat exp award")
    private int repeatExpReward;
    @ConfigComment("Reward items for repeating the challenge. List of ItemStacks.")
    private List<ItemStack> repeatItemReward = new ArrayList<>();
    @ConfigComment("Repeat money award")
    private int repeatMoneyReward;
    @ConfigComment("Commands to run when challenge is repeated. String List.")
    private List<String> repeatRewardCommands = new ArrayList<>();
    @ConfigComment("Description of the repeat rewards. If blank, it will be autogenerated.")
    private String repeatRewardText = "";








    @ConfigComment("Unique name of the challenge")
    private String uniqueId = "";


    /**
     * @return the challengeType
     */
    public ChallengeType getChallengeType() {
        return challengeType;
    }

    /**
     * @param challengeType the challengeType to set
     */
    public void setChallengeType(ChallengeType challengeType) {
        this.challengeType = challengeType;
    }

    /**
     * @return the deployed
     */
    public boolean isDeployed() {
        return deployed;
    }

    /**
     * @param deployed the deployed to set
     */
    public void setDeployed(boolean deployed) {
        this.deployed = deployed;
    }

    /**
     * @return the description
     */
    public List<String> getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(List<String> description) {
        this.description = description;
    }

    /**
     * @return the expReward
     */
    public int getExpReward() {
        return expReward;
    }

    /**
     * @param expReward the expReward to set
     */
    public void setExpReward(int expReward) {
        this.expReward = expReward;
    }

    /**
     * @return the friendlyName
     */
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * @param friendlyName the friendlyName to set
     */
    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    /**
     * @return the icon
     */
    public ItemStack getIcon() {
        return icon.clone();
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }

    /**
     * @return the level
     */
    public String getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(String level) {
        if (level.isEmpty()) {
            level = ChallengesManager.FREE;
        }
        this.level = level;
    }

    /**
     * @return the maxTimes
     */
    public int getMaxTimes() {
        return maxTimes;
    }

    /**
     * @param maxTimes the maxTimes to set
     */
    public void setMaxTimes(int maxTimes) {
        this.maxTimes = maxTimes;
    }

    /**
     * @return the moneyReward
     */
    public int getMoneyReward() {
        return moneyReward;
    }

    /**
     * @param moneyReward the moneyReward to set
     */
    public void setMoneyReward(int moneyReward) {
        this.moneyReward = moneyReward;
    }

    /**
     * @return the removeWhenCompleted
     */
    public boolean isRemoveWhenCompleted() {
        return removeWhenCompleted;
    }

    /**
     * @param removeWhenCompleted the removeWhenCompleted to set
     */
    public void setRemoveWhenCompleted(boolean removeWhenCompleted) {
        this.removeWhenCompleted = removeWhenCompleted;
    }

    /**
     * @return the repeatable
     */
    public boolean isRepeatable() {
        return repeatable;
    }

    /**
     * @param repeatable the repeatable to set
     */
    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    /**
     * @return the repeatExpReward
     */
    public int getRepeatExpReward() {
        return repeatExpReward;
    }

    /**
     * @param repeatExpReward the repeatExpReward to set
     */
    public void setRepeatExpReward(int repeatExpReward) {
        this.repeatExpReward = repeatExpReward;
    }

    /**
     * @return the repeatItemReward
     */
    public List<ItemStack> getRepeatItemReward() {
        return repeatItemReward;
    }

    /**
     * @param repeatItemReward the repeatItemReward to set
     */
    public void setRepeatItemReward(List<ItemStack> repeatItemReward) {
        this.repeatItemReward = repeatItemReward;
    }

    /**
     * @return the repeatMoneyReward
     */
    public int getRepeatMoneyReward() {
        return repeatMoneyReward;
    }

    /**
     * @param repeatMoneyReward the repeatMoneyReward to set
     */
    public void setRepeatMoneyReward(int repeatMoneyReward) {
        this.repeatMoneyReward = repeatMoneyReward;
    }

    /**
     * @return the repeatRewardCommands
     */
    public List<String> getRepeatRewardCommands() {
        return repeatRewardCommands;
    }

    /**
     * @param repeatRewardCommands the repeatRewardCommands to set
     */
    public void setRepeatRewardCommands(List<String> repeatRewardCommands) {
        this.repeatRewardCommands = repeatRewardCommands;
    }

    /**
     * @return the repeatRewardText
     */
    public String getRepeatRewardText() {
        return repeatRewardText;
    }

    /**
     * @param repeatRewardText the repeatRewardText to set
     */
    public void setRepeatRewardText(String repeatRewardText) {
        this.repeatRewardText = repeatRewardText;
    }

    /**
     * @return the reqExp
     */
    public int getReqExp() {
        return reqExp;
    }

    /**
     * @param reqExp the reqExp to set
     */
    public void setReqExp(int reqExp) {
        this.reqExp = reqExp;
    }

    /**
     * @return the reqIslandlevel
     */
    public long getReqIslandlevel() {
        return reqIslandlevel;
    }

    /**
     * @param reqIslandlevel the reqIslandlevel to set
     */
    public void setReqIslandlevel(long reqIslandlevel) {
        this.reqIslandlevel = reqIslandlevel;
    }

    /**
     * @return the reqMoney
     */
    public int getReqMoney() {
        return reqMoney;
    }

    /**
     * @param reqMoney the reqMoney to set
     */
    public void setReqMoney(int reqMoney) {
        this.reqMoney = reqMoney;
    }

    /**
     * @return the reqPerms
     */
    public Set<String> getReqPerms() {
        return reqPerms;
    }

    /**
     * @param reqPerms the reqPerms to set
     */
    public void setReqPerms(Set<String> reqPerms) {
        this.reqPerms = reqPerms;
    }

    /**
     * @return the requiredItems
     */
    public List<ItemStack> getRequiredItems() {
        return requiredItems;
    }

    /**
     * @param requiredItems the requiredItems to set
     */
    public void setRequiredItems(List<ItemStack> requiredItems) {
        this.requiredItems = requiredItems;
    }

    /**
     * @return requiredEntities
     */
    public Map<EntityType, Integer> getRequiredEntities() {
        return requiredEntities;
    }

    /**
     * @param requiredEntities the requiredEntities to set
     */
    public void setRequiredEntities(Map<EntityType, Integer> requiredEntities) {
        this.requiredEntities = requiredEntities;
    }

    /**
     * @return the requiredBlocks
     */
    public Map<Material, Integer> getRequiredBlocks() {
        return requiredBlocks;
    }

    /**
     * @param map the requiredBlocks to set
     */
    public void setRequiredBlocks(Map<Material, Integer> map) {
        this.requiredBlocks = map;
    }

    /**
     * @return the rewardCommands
     */
    public List<String> getRewardCommands() {
        return rewardCommands;
    }

    /**
     * @param rewardCommands the rewardCommands to set
     */
    public void setRewardCommands(List<String> rewardCommands) {
        this.rewardCommands = rewardCommands;
    }

    /**
     * @return the itemReward
     */
    public List<ItemStack> getItemReward() {
        return itemReward;
    }

    /**
     * @param itemReward the itemReward to set
     */
    public void setItemReward(List<ItemStack> itemReward) {
        this.itemReward = itemReward;
    }

    /**
     * @return the rewardText
     */
    public String getRewardText() {
        return rewardText;
    }

    /**
     * @param rewardText the rewardText to set
     */
    public void setRewardText(String rewardText) {
        this.rewardText = rewardText;
    }

    /**
     * @return the searchRadius
     */
    public int getSearchRadius() {
        return searchRadius;
    }

    /**
     * @param searchRadius the searchRadius to set
     */
    public void setSearchRadius(int searchRadius) {
        this.searchRadius = searchRadius;
    }

    /**
     * @return the slot
     */
    public int getSlot() {
        return slot;
    }

    /**
     * @param slot the slot to set
     */
    public void setSlot(int slot) {
        this.slot = slot;
    }

    /**
     * @return the takeItems
     */
    public boolean isTakeItems() {
        return takeItems;
    }

    /**
     * @param takeItems the takeItems to set
     */
    public void setTakeItems(boolean takeItems) {
        this.takeItems = takeItems;
    }

    /**
     * @return the takeMoney
     */
    public boolean isTakeMoney() {
        return takeMoney;
    }

    /**
     * @param takeMoney the takeMoney to set
     */
    public void setTakeMoney(boolean takeMoney) {
        this.takeMoney = takeMoney;
    }

    /**
     * @return the environment
     */
    public List<World.Environment> getEnvironment() {
        return environment;
    }

    /**
     * @param environment the environment to set
     */
    public void setEnvironment(List<World.Environment> environment) {
        this.environment = environment;
    }

    /**
     * @return the worlds
     */
    public List<String> getWorlds() {
        return worlds;
    }

    /**
     * @param worlds the worlds to set
     */
    public void setWorlds(List<String> worlds) {
        this.worlds = worlds;
    }

    /**
     * @return the uniqueId
     */
    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * @param uniqueId the uniqueId to set
     */
    @Override
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Challenges)) {
            return false;
        }
        Challenges other = (Challenges) obj;
        if (uniqueId == null) {
            if (other.uniqueId != null) {
                return false;
            }
        } else if (!uniqueId.equals(other.uniqueId)) {
            return false;
        }
        return true;
    }
}
