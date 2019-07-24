package world.bentobox.challenges.database.object;


import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.api.configuration.ConfigComment;
import world.bentobox.bentobox.database.objects.DataObject;


/**
 * Data object for challenges
 * @author tastybento
 *
 */
public class Challenge implements DataObject
{
    /**
     * Empty constructor
     */
    public Challenge()
    {
    }


    /**
     * This enum holds all Challenge Types.
     */
    public enum ChallengeType
    {
        /**
         * The player must have the items on them.
         */
        INVENTORY,

        /**
         * Items or required entities have to be within x blocks of the player.
         */
        ISLAND,

        /**
         * Other type, like required money / experience or island level. This my request
         * other plugins to be setup before it could work.
         */
        OTHER,
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    @ConfigComment("")
    @ConfigComment("Unique name of the challenge")
    @Expose
    private String uniqueId = "";

    @ConfigComment("")
    @ConfigComment("The name of the challenge. May include color codes. Single line.")
    @Expose
    private String friendlyName = "";

    @ConfigComment("")
    @ConfigComment("Whether this challenge is deployed or not.")
    @Expose
    private boolean deployed;

    @ConfigComment("")
    @ConfigComment("Description of the challenge. Will become the lore on the icon. Can ")
    @ConfigComment("include & color codes. String List.")
    @Expose
    private List<String> description = new ArrayList<>();

    @ConfigComment("")
    @ConfigComment("The icon in the GUI for this challenge. ItemStack.")
    @Expose
    private ItemStack icon = new ItemStack(Material.PAPER);

    @ConfigComment("")
    @ConfigComment("Order of this challenge. It allows define order for challenges in")
    @ConfigComment("single level. If order for challenges are equal, it will order by")
    @ConfigComment("challenge unique id.")
    @Expose
    private int order = -1;

    @ConfigComment("")
    @ConfigComment("Challenge type can be INVENTORY, OTHER or ISLAND.")
    @Expose
    private ChallengeType challengeType = ChallengeType.INVENTORY;

    @ConfigComment("")
    @ConfigComment("List of environments where this challenge will occur: NETHER, NORMAL,")
    @ConfigComment("THE_END. Leave blank for all.")
    @Expose
    private Set<World.Environment> environment = new HashSet<>();

    @ConfigComment("")
    @ConfigComment("If true, the challenge will disappear from the GUI when completed")
    @Expose
    private boolean removeWhenCompleted;

    @ConfigComment("")
    @ConfigComment("Unique challenge ID. Empty means that challenge is in free challenge list.")
    @Expose
    private String level = "";

    // ---------------------------------------------------------------------
    // Section: Requirement related
    // ---------------------------------------------------------------------

    @ConfigComment("")
    @ConfigComment("")
    @ConfigComment("The required permissions to see this challenge. String list.")
    @Expose
    private Set<String> requiredPermissions = new HashSet<>();

    @ConfigComment("")
    @ConfigComment("This is a map of the blocks required in a ISLAND challenge. Material,")
    @ConfigComment("Integer")
    @Expose
    private Map<Material, Integer> requiredBlocks = new EnumMap<>(Material.class);

    @ConfigComment("")
    @ConfigComment("Remove the required blocks from the island")
    @Expose
    private boolean removeBlocks;

    @ConfigComment("")
    @ConfigComment("Any entities that must be in the area for ISLAND type challenges. ")
    @ConfigComment("Map EntityType, Number")
    @Expose
    private Map<EntityType, Integer> requiredEntities = new EnumMap<>(EntityType.class);

    @ConfigComment("")
    @ConfigComment("Remove the entities from the island")
    @Expose
    private boolean removeEntities;

    @ConfigComment("")
    @ConfigComment("The items that must be in the inventory to complete the challenge. ")
    @ConfigComment("ItemStack List.")
    @Expose
    private List<ItemStack> requiredItems = new ArrayList<>();

    @ConfigComment("")
    @ConfigComment("Take the required items from the player")
    @Expose
    private boolean takeItems = true;

    @ConfigComment("")
    @ConfigComment("Required experience for challenge completion.")
    @Expose
    private int requiredExperience = 0;

    @ConfigComment("")
    @ConfigComment("Take the experience from the player")
    @Expose
    private boolean takeExperience;

    @ConfigComment("")
    @ConfigComment("Required money for challenge completion. Economy plugins or addons")
    @ConfigComment("is required for this option.")
    @Expose
    private int requiredMoney = 0;

    @ConfigComment("")
    @ConfigComment("Take the money from the player")
    @Expose
    private boolean takeMoney;

    @ConfigComment("")
    @ConfigComment("Required island level for challenge completion. Plugin or Addon that")
    @ConfigComment("calculates island level is required for this option.")
    @Expose
    private long requiredIslandLevel;

    @ConfigComment("")
    @ConfigComment("The number of blocks around the player to search for items on an island")
    @Expose
    private int searchRadius = 10;


    // ---------------------------------------------------------------------
    // Section: Rewards
    // ---------------------------------------------------------------------

    @ConfigComment("")
    @ConfigComment("")
    @ConfigComment("If this is blank, the reward text will be auto-generated, otherwise")
    @ConfigComment("this will be used.")
    @Expose
    private String rewardText = "";


    @ConfigComment("")
    @ConfigComment("List of items the player will receive first time. ItemStack List.")
    @Expose
    private List<ItemStack> rewardItems = new ArrayList<>();

    @ConfigComment("")
    @ConfigComment("Experience point reward")
    @Expose
    private int rewardExperience = 0;

    @ConfigComment("")
    @ConfigComment("Money reward. Economy plugin or addon required for this option.")
    @Expose
    private int rewardMoney = 0;

    @ConfigComment("")
    @ConfigComment("Commands to run when the player completes the challenge for the first")
    @ConfigComment("time. String List")
    @Expose
    private List<String> rewardCommands = new ArrayList<>();


    // ---------------------------------------------------------------------
    // Section: Repeat Rewards
    // ---------------------------------------------------------------------


    @ConfigComment("")
    @ConfigComment("")
    @ConfigComment("True if the challenge is repeatable")
    @Expose
    private boolean repeatable;

    @ConfigComment("")
    @ConfigComment("Description of the repeat rewards. If blank, it will be autogenerated.")
    @Expose
    private String repeatRewardText = "";

    @ConfigComment("")
    @ConfigComment("Maximum number of times the challenge can be repeated. 0 or less")
    @ConfigComment("will mean infinite times.")
    @Expose
    private int maxTimes = 1;

    @ConfigComment("")
    @ConfigComment("Repeat experience reward")
    @Expose
    private int repeatExperienceReward = 0;

    @ConfigComment("")
    @ConfigComment("Reward items for repeating the challenge. List of ItemStacks.")
    @Expose
    private List<ItemStack> repeatItemReward = new ArrayList<>();

    @ConfigComment("")
    @ConfigComment("Repeat money reward. Economy plugin or addon required for this option.")
    @Expose
    private int repeatMoneyReward;

    @ConfigComment("")
    @ConfigComment("Commands to run when challenge is repeated. String List.")
    @Expose
    private List<String> repeatRewardCommands = new ArrayList<>();


    // ---------------------------------------------------------------------
    // Section: Getters
    // ---------------------------------------------------------------------


    /**
     * @return the uniqueId
     */
    @Override
    public String getUniqueId()
    {
        return uniqueId;
    }


    /**
     * @return the friendlyName
     */
    public String getFriendlyName()
    {
        return friendlyName.isEmpty() ? uniqueId : friendlyName;
    }


    /**
     * @return the deployed
     */
    public boolean isDeployed()
    {
        return deployed;
    }


    /**
     * @return the description
     */
    public List<String> getDescription()
    {
        return description;
    }


    /**
     * @return the icon
     */
    public ItemStack getIcon()
    {
        return icon !=null ? icon.clone() : new ItemStack(Material.PAPER);
    }


    /**
     * @return the order
     */
    public int getOrder()
    {
        return order;
    }


    /**
     * @return the challengeType
     */
    public ChallengeType getChallengeType()
    {
        return challengeType;
    }


    /**
     * @return the environment
     */
    public Set<World.Environment> getEnvironment()
    {
        return environment;
    }


    /**
     * @return the level
     */
    public String getLevel()
    {
        return level;
    }


    /**
     * @return the removeWhenCompleted
     */
    public boolean isRemoveWhenCompleted()
    {
        return removeWhenCompleted;
    }


    /**
     * @return the requiredPermissions
     */
    public Set<String> getRequiredPermissions()
    {
        return requiredPermissions;
    }


    /**
     * @return the requiredBlocks
     */
    public Map<Material, Integer> getRequiredBlocks()
    {
        return requiredBlocks;
    }


    /**
     * @return the removeBlocks
     */
    public boolean isRemoveBlocks()
    {
        return removeBlocks;
    }


    /**
     * @return the requiredEntities
     */
    public Map<EntityType, Integer> getRequiredEntities()
    {
        return requiredEntities;
    }


    /**
     * @return the removeEntities
     */
    public boolean isRemoveEntities()
    {
        return removeEntities;
    }


    /**
     * @return the requiredItems
     */
    public List<ItemStack> getRequiredItems()
    {
        return requiredItems;
    }


    /**
     * @return the takeItems
     */
    public boolean isTakeItems()
    {
        return takeItems;
    }


    /**
     * @return the requiredExperience
     */
    public int getRequiredExperience()
    {
        return requiredExperience;
    }


    /**
     * @return the takeExperience
     */
    public boolean isTakeExperience()
    {
        return takeExperience;
    }


    /**
     * @return the requiredMoney
     */
    public int getRequiredMoney()
    {
        return requiredMoney;
    }


    /**
     * @return the takeMoney
     */
    public boolean isTakeMoney()
    {
        return takeMoney;
    }


    /**
     * @return the requiredIslandLevel
     */
    public long getRequiredIslandLevel()
    {
        return requiredIslandLevel;
    }


    /**
     * @return the searchRadius
     */
    public int getSearchRadius()
    {
        return searchRadius;
    }


    /**
     * @return the rewardText
     */
    public String getRewardText()
    {
        return rewardText;
    }


    /**
     * @return the rewardItems
     */
    public List<ItemStack> getRewardItems()
    {
        return rewardItems;
    }


    /**
     * @return the rewardExperience
     */
    public int getRewardExperience()
    {
        return rewardExperience;
    }


    /**
     * @return the rewardMoney
     */
    public int getRewardMoney()
    {
        return rewardMoney;
    }


    /**
     * @return the rewardCommands
     */
    public List<String> getRewardCommands()
    {
        return rewardCommands;
    }


    /**
     * @return the repeatable
     */
    public boolean isRepeatable()
    {
        return repeatable;
    }


    /**
     * @return the repeatRewardText
     */
    public String getRepeatRewardText()
    {
        return repeatRewardText;
    }


    /**
     * @return the maxTimes
     */
    public int getMaxTimes()
    {
        return maxTimes;
    }


    /**
     * @return the repeatExperienceReward
     */
    public int getRepeatExperienceReward()
    {
        return repeatExperienceReward;
    }


    /**
     * @return the repeatItemReward
     */
    public List<ItemStack> getRepeatItemReward()
    {
        return repeatItemReward;
    }


    /**
     * @return the repeatMoneyReward
     */
    public int getRepeatMoneyReward()
    {
        return repeatMoneyReward;
    }


    /**
     * @return the repeatRewardCommands
     */
    public List<String> getRepeatRewardCommands()
    {
        return repeatRewardCommands;
    }


    // ---------------------------------------------------------------------
    // Section: Setters
    // ---------------------------------------------------------------------


    /**
     * @param uniqueId the uniqueId to set
     */
    @Override
    public void setUniqueId(String uniqueId)
    {
        this.uniqueId = uniqueId;
    }


    /**
     * This method sets the friendlyName value.
     * @param friendlyName the friendlyName new value.
     *
     */
    public void setFriendlyName(String friendlyName)
    {
        this.friendlyName = friendlyName;
    }


    /**
     * This method sets the deployed value.
     * @param deployed the deployed new value.
     *
     */
    public void setDeployed(boolean deployed)
    {
        this.deployed = deployed;
    }


    /**
     * This method sets the description value.
     * @param description the description new value.
     *
     */
    public void setDescription(List<String> description)
    {
        this.description = description;
    }


    /**
     * This method sets the icon value.
     * @param icon the icon new value.
     *
     */
    public void setIcon(ItemStack icon)
    {
        this.icon = icon;
    }


    /**
     * This method sets the order value.
     * @param order the order new value.
     *
     */
    public void setOrder(int order)
    {
        this.order = order;
    }


    /**
     * This method sets the challengeType value.
     * @param challengeType the challengeType new value.
     *
     */
    public void setChallengeType(ChallengeType challengeType)
    {
        this.challengeType = challengeType;
    }


    /**
     * This method sets the environment value.
     * @param environment the environment new value.
     *
     */
    public void setEnvironment(Set<World.Environment> environment)
    {
        this.environment = environment;
    }


    /**
     * This method sets the level value.
     * @param level the level new value.
     */
    public void setLevel(String level)
    {
        this.level = level;
    }


    /**
     * This method sets the removeWhenCompleted value.
     * @param removeWhenCompleted the removeWhenCompleted new value.
     *
     */
    public void setRemoveWhenCompleted(boolean removeWhenCompleted)
    {
        this.removeWhenCompleted = removeWhenCompleted;
    }


    /**
     * This method sets the requiredPermissions value.
     * @param requiredPermissions the requiredPermissions new value.
     *
     */
    public void setRequiredPermissions(Set<String> requiredPermissions)
    {
        this.requiredPermissions = requiredPermissions;
    }


    /**
     * This method sets the requiredBlocks value.
     * @param requiredBlocks the requiredBlocks new value.
     *
     */
    public void setRequiredBlocks(Map<Material, Integer> requiredBlocks)
    {
        this.requiredBlocks = requiredBlocks;
    }


    /**
     * This method sets the removeBlocks value.
     * @param removeBlocks the removeBlocks new value.
     *
     */
    public void setRemoveBlocks(boolean removeBlocks)
    {
        this.removeBlocks = removeBlocks;
    }


    /**
     * This method sets the requiredEntities value.
     * @param requiredEntities the requiredEntities new value.
     *
     */
    public void setRequiredEntities(Map<EntityType, Integer> requiredEntities)
    {
        this.requiredEntities = requiredEntities;
    }


    /**
     * This method sets the removeEntities value.
     * @param removeEntities the removeEntities new value.
     *
     */
    public void setRemoveEntities(boolean removeEntities)
    {
        this.removeEntities = removeEntities;
    }


    /**
     * This method sets the requiredItems value.
     * @param requiredItems the requiredItems new value.
     *
     */
    public void setRequiredItems(List<ItemStack> requiredItems)
    {
        this.requiredItems = requiredItems;
    }


    /**
     * This method sets the takeItems value.
     * @param takeItems the takeItems new value.
     *
     */
    public void setTakeItems(boolean takeItems)
    {
        this.takeItems = takeItems;
    }


    /**
     * This method sets the requiredExperience value.
     * @param requiredExperience the requiredExperience new value.
     *
     */
    public void setRequiredExperience(int requiredExperience)
    {
        this.requiredExperience = requiredExperience;
    }


    /**
     * This method sets the takeExperience value.
     * @param takeExperience the takeExperience new value.
     *
     */
    public void setTakeExperience(boolean takeExperience)
    {
        this.takeExperience = takeExperience;
    }


    /**
     * This method sets the requiredMoney value.
     * @param requiredMoney the requiredMoney new value.
     *
     */
    public void setRequiredMoney(int requiredMoney)
    {
        this.requiredMoney = requiredMoney;
    }


    /**
     * This method sets the takeMoney value.
     * @param takeMoney the takeMoney new value.
     *
     */
    public void setTakeMoney(boolean takeMoney)
    {
        this.takeMoney = takeMoney;
    }


    /**
     * This method sets the requiredIslandLevel value.
     * @param requiredIslandLevel the requiredIslandLevel new value.
     *
     */
    public void setRequiredIslandLevel(long requiredIslandLevel)
    {
        this.requiredIslandLevel = requiredIslandLevel;
    }


    /**
     * This method sets the searchRadius value.
     * @param searchRadius the searchRadius new value.
     *
     */
    public void setSearchRadius(int searchRadius)
    {
        this.searchRadius = searchRadius;
    }


    /**
     * This method sets the rewardText value.
     * @param rewardText the rewardText new value.
     *
     */
    public void setRewardText(String rewardText)
    {
        this.rewardText = rewardText;
    }


    /**
     * This method sets the rewardItems value.
     * @param rewardItems the rewardItems new value.
     *
     */
    public void setRewardItems(List<ItemStack> rewardItems)
    {
        this.rewardItems = rewardItems;
    }


    /**
     * This method sets the rewardExperience value.
     * @param rewardExperience the rewardExperience new value.
     *
     */
    public void setRewardExperience(int rewardExperience)
    {
        this.rewardExperience = rewardExperience;
    }


    /**
     * This method sets the rewardMoney value.
     * @param rewardMoney the rewardMoney new value.
     *
     */
    public void setRewardMoney(int rewardMoney)
    {
        this.rewardMoney = rewardMoney;
    }


    /**
     * This method sets the rewardCommands value.
     * @param rewardCommands the rewardCommands new value.
     *
     */
    public void setRewardCommands(List<String> rewardCommands)
    {
        this.rewardCommands = rewardCommands;
    }


    /**
     * This method sets the repeatable value.
     * @param repeatable the repeatable new value.
     *
     */
    public void setRepeatable(boolean repeatable)
    {
        this.repeatable = repeatable;
    }


    /**
     * This method sets the repeatRewardText value.
     * @param repeatRewardText the repeatRewardText new value.
     *
     */
    public void setRepeatRewardText(String repeatRewardText)
    {
        this.repeatRewardText = repeatRewardText;
    }


    /**
     * This method sets the maxTimes value.
     * @param maxTimes the maxTimes new value.
     *
     */
    public void setMaxTimes(int maxTimes)
    {
        this.maxTimes = maxTimes;
    }


    /**
     * This method sets the repeatExperienceReward value.
     * @param repeatExperienceReward the repeatExperienceReward new value.
     *
     */
    public void setRepeatExperienceReward(int repeatExperienceReward)
    {
        this.repeatExperienceReward = repeatExperienceReward;
    }


    /**
     * This method sets the repeatItemReward value.
     * @param repeatItemReward the repeatItemReward new value.
     *
     */
    public void setRepeatItemReward(List<ItemStack> repeatItemReward)
    {
        this.repeatItemReward = repeatItemReward;
    }


    /**
     * This method sets the repeatMoneyReward value.
     * @param repeatMoneyReward the repeatMoneyReward new value.
     *
     */
    public void setRepeatMoneyReward(int repeatMoneyReward)
    {
        this.repeatMoneyReward = repeatMoneyReward;
    }


    /**
     * This method sets the repeatRewardCommands value.
     * @param repeatRewardCommands the repeatRewardCommands new value.
     *
     */
    public void setRepeatRewardCommands(List<String> repeatRewardCommands)
    {
        this.repeatRewardCommands = repeatRewardCommands;
    }


    // ---------------------------------------------------------------------
    // Section: Other methods
    // ---------------------------------------------------------------------


    /**
     * This method match if given worldName relates to current challenge. It is detected
     * via challenge uniqueId, as it always must start with world name.
     * This method is created to avoid issues with capital letters in world names in 1.14
     * @param worldName Name that must be checked.
     * @return {@code true} if current challenge relates to given world name, otherwise
     * {@code false}.
     */
    public boolean matchWorld(String worldName)
    {
        return this.uniqueId.regionMatches(true, 0, worldName, 0, worldName.length());
    }


    /**
     * @see java.lang.Object#hashCode()
     * @return int
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
        return result;
    }


    /**
     * @see java.lang.Object#equals(Object) ()
     * @param obj of type Object
     * @return boolean
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (!(obj instanceof Challenge))
        {
            return false;
        }

        Challenge other = (Challenge) obj;

        if (uniqueId == null)
        {
            return other.uniqueId == null;
        }
        else
        {
            return uniqueId.equalsIgnoreCase(other.uniqueId);
        }
    }


    /**
     * Clone method that returns clone of current challenge.
     * @return Challenge that is cloned from current object.
     */
    @Override
    public Challenge clone()
    {
        Challenge clone;

        try
        {
            clone = (Challenge) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            clone = new Challenge();

            clone.setUniqueId(this.uniqueId);
            clone.setFriendlyName(this.friendlyName);
            clone.setDeployed(this.deployed);
            clone.setDescription(new ArrayList<>(this.description));
            clone.setIcon(this.icon.clone());
            clone.setOrder(this.order);
            clone.setChallengeType(ChallengeType.valueOf(this.challengeType.name()));
            clone.setEnvironment(new HashSet<>(this.environment));
            clone.setLevel(this.level);
            clone.setRemoveWhenCompleted(this.removeWhenCompleted);
            clone.setRequiredPermissions(new HashSet<>(this.requiredPermissions));
            clone.setRequiredBlocks(new HashMap<>(this.requiredBlocks));
            clone.setRemoveBlocks(this.removeBlocks);
            clone.setRequiredEntities(new HashMap<>(this.requiredEntities));
            clone.setRemoveEntities(this.removeEntities);
            clone.setRequiredItems(this.requiredItems.stream().map(ItemStack::clone).collect(Collectors.toCollection(() -> new ArrayList<>(this.requiredItems.size()))));
            clone.setTakeItems(this.takeItems);
            clone.setRequiredExperience(this.requiredExperience);
            clone.setTakeExperience(this.takeExperience);
            clone.setRequiredMoney(this.requiredMoney);
            clone.setTakeMoney(this.takeMoney);
            clone.setRequiredIslandLevel(this.requiredIslandLevel);
            clone.setSearchRadius(this.searchRadius);
            clone.setRewardText(this.rewardText);
            clone.setRewardItems(this.rewardItems.stream().map(ItemStack::clone).collect(Collectors.toCollection(() -> new ArrayList<>(this.rewardItems.size()))));
            clone.setRewardExperience(this.rewardExperience);
            clone.setRewardMoney(this.rewardMoney);
            clone.setRewardCommands(new ArrayList<>(this.rewardCommands));
            clone.setRepeatable(this.repeatable);
            clone.setRepeatRewardText(this.repeatRewardText);
            clone.setMaxTimes(this.maxTimes);
            clone.setRepeatExperienceReward(this.repeatExperienceReward);
            clone.setRepeatItemReward(this.repeatItemReward.stream().map(ItemStack::clone).collect(Collectors.toCollection(() -> new ArrayList<>(this.repeatItemReward.size()))));
            clone.setRepeatMoneyReward(this.repeatMoneyReward);
            clone.setRepeatRewardCommands(new ArrayList<>(this.repeatRewardCommands));
        }

        return clone;
    }
}