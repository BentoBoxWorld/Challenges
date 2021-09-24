package world.bentobox.challenges.database.object;


import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;
import world.bentobox.challenges.database.object.adapters.EntityCompatibilityAdapter;
import world.bentobox.challenges.database.object.adapters.RequirementsAdapter;
import world.bentobox.challenges.database.object.adapters.TypeMigrationAdapter;
import world.bentobox.challenges.database.object.requirements.Requirements;


/**
 * Data object for challenges
 * @author tastybento
 *
 */
@Table(name = "Challenge")
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
        INVENTORY_TYPE,

        /**
         * Items or required entities have to be within x blocks of the player.
         */
        ISLAND_TYPE,

        /**
         * Other type, like required money / experience or island level. This my request
         * other plugins to be setup before it could work.
         */
        OTHER_TYPE,

        /**
         * Challenge based on player statistic data.
         */
        STATISTIC_TYPE
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * Unique name for challenge object.
     */
    @Expose
    private String uniqueId = "";

    /**
     * The name of the challenge. May include color codes. Single line.
     */
    @Expose
    private String friendlyName = "";

    /**
     * Whether this challenge is deployed or not.
     */
    @Expose
    private boolean deployed;

    /**
     * Description of the challenge. Will become the lore on the icon. Can include & color codes. String List.
     */
    @Expose
    private List<String> description = new ArrayList<>();

    /**
     * The icon in the GUI for this challenge. ItemStack.
     */
    @Expose
    private ItemStack icon = new ItemStack(Material.PAPER);

    /**
     * Order of this challenge. It allows define order for challenges in
     * single level. If order for challenges are equal, it will order by
     * challenge unique id.
     */
    @Expose
    private int order = -1;

    /**
     * Challenge type can be INVENTORY, OTHER or ISLAND.
     */
    @Expose
    @JsonAdapter(TypeMigrationAdapter.class)
    private ChallengeType challengeType = ChallengeType.INVENTORY_TYPE;

    /**
     * List of environments where this challenge will occur: NETHER, NORMAL, THE_END. Leave blank for all.
     */
    @Expose
    private Set<World.Environment> environment = new HashSet<>();

    /**
     * If true, the challenge will disappear from the GUI when completed
     */
    @Expose
    private boolean removeWhenCompleted;

    /**
     * Unique level ID. Empty means that challenge is in free challenge list.
     */
    @Expose
    private String level = "";

    // ---------------------------------------------------------------------
    // Section: Requirement related
    // ---------------------------------------------------------------------

    /**
     * Requirements for current challenge.
     */
    @Expose
    @JsonAdapter(RequirementsAdapter.class)
    private Requirements requirements;


    // ---------------------------------------------------------------------
    // Section: Rewards
    // ---------------------------------------------------------------------

    /**
     * If this is blank, the reward text will be auto-generated, otherwise this will be used.
     */
    @Expose
    private String rewardText = "";

    /**
     * List of items the player will receive first time. ItemStack List.
     */
    @Expose
    private List<ItemStack> rewardItems = new ArrayList<>();

    /**
     * Experience point reward
     */
    @Expose
    private int rewardExperience = 0;

    /**
     * Money reward. Economy plugin or addon required for this option.
     */
    @Expose
    private double rewardMoney = 0;

    /**
     * Commands to run when the player completes the challenge for the first time. String List
     */
    @Expose
    private List<String> rewardCommands = new ArrayList<>();


    // ---------------------------------------------------------------------
    // Section: Repeat Rewards
    // ---------------------------------------------------------------------

    /**
     * True if the challenge is repeatable
     */
    @Expose
    private boolean repeatable;

    /**
     * Timeout for repeatable challenge before it can be completed again.
     */
    @Expose
    private long timeout;

    /**
     * Description of the repeat rewards. If blank, it will be autogenerated
     */
    @Expose
    private String repeatRewardText = "";

    /**
     * Maximum number of times the challenge can be repeated. 0 or less will mean infinite times.
     */
    @Expose
    private int maxTimes = 1;

    /**
     * Repeat experience reward
     */
    @Expose
    private int repeatExperienceReward = 0;

    /**
     * Reward items for repeating the challenge. List of ItemStacks.
     */
    @Expose
    private List<ItemStack> repeatItemReward = new ArrayList<>();

    /**
     * Repeat money reward. Economy plugin or addon required for this option.
     */
    @Expose
    private double repeatMoneyReward;

    /**
     * Commands to run when challenge is repeated. String List.
     */
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
     * Method Challenge#getRequirements returns the requirements of this object.
     * @return the requirements (type Requirements) of this object.
     */
    @SuppressWarnings("unchecked")
    public <T extends Requirements> T getRequirements()
    {
        return (T) this.requirements;
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
    public double getRewardMoney()
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
    public double getRepeatMoneyReward()
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


    /**
     * Gets timeout.
     *
     * @return the timeout
     */
    public long getTimeout()
    {
        return timeout;
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
    public void setLevel(@NonNull String level)
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
    public void setRewardMoney(double rewardMoney)
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
    public void setRepeatMoneyReward(double repeatMoneyReward)
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


    /**
     * Method Challenge#setRequirements sets new value for the requirements of this object.
     * @param requirements new value for this object.
     *
     */
    public void setRequirements(Requirements requirements)
    {
        this.requirements = requirements;
    }


    /**
     * Sets timeout.
     *
     * @param timeout the timeout
     */
    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }


    // ---------------------------------------------------------------------
    // Section: Other methods
    // ---------------------------------------------------------------------


    /**
     * This method match if given gameMode relates to current challenge. It is detected
     * via challenge uniqueId, as it always must start with gameMode name.
     * This method is created to avoid issues with capital letters in world names in 1.14
     * and readjust to store GameMode name instead of world name.
     * @param gameMode Name that must be checked.
     * @return {@code true} if current challenge relates to given gameMode name, otherwise
     * {@code false}.
     */
    public boolean matchGameMode(String gameMode)
    {
        return gameMode != null &&
            this.uniqueId.regionMatches(true, 0, gameMode, 0, gameMode.length());
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

        if (!(obj instanceof Challenge other))
        {
            return false;
        }

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
     * This method checks if variable values are valid for current level.
     * @return {@code true} if all object values are valid, {@code false} otherwise.
     */
    public boolean isValid()
    {
        return this.uniqueId != null &&
            !this.uniqueId.isEmpty() &&
            this.friendlyName != null &&
            this.description != null &&
            this.icon != null &&
            this.challengeType != null &&
            this.environment != null &&
            this.level != null &&

            this.requirements.isValid() &&

            this.rewardText != null &&
            this.rewardItems.stream().noneMatch(Objects::isNull) &&
            this.rewardCommands != null &&

            this.repeatRewardText != null &&
            this.repeatItemReward.stream().noneMatch(Objects::isNull) &&
            this.repeatRewardCommands != null;
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
            clone.setRequirements(this.requirements.clone());
            clone.setRewardText(this.rewardText);
            clone.setRewardItems(
                this.rewardItems.stream().
                    map(ItemStack::clone).
                    collect(Collectors.toCollection(() -> new ArrayList<>(this.rewardItems.size()))));
            clone.setRewardExperience(this.rewardExperience);
            clone.setRewardMoney(this.rewardMoney);
            clone.setRewardCommands(new ArrayList<>(this.rewardCommands));
            clone.setRepeatable(this.repeatable);
            clone.setRepeatRewardText(this.repeatRewardText);
            clone.setMaxTimes(this.maxTimes);
            clone.setRepeatExperienceReward(this.repeatExperienceReward);
            clone.setRepeatItemReward(
                this.repeatItemReward.stream().
                    map(ItemStack::clone).
                    collect(Collectors.toCollection(() -> new ArrayList<>(this.repeatItemReward.size()))));
            clone.setRepeatMoneyReward(this.repeatMoneyReward);
            clone.setRepeatRewardCommands(new ArrayList<>(this.repeatRewardCommands));
            clone.setTimeout(this.timeout);
        }
        catch (Exception e)
        {
            BentoBox.getInstance().logError("Failed to clone Challenge " + this.uniqueId);
            BentoBox.getInstance().logStacktrace(e);
            clone = this;
            this.deployed = false;
        }

        return clone;
    }
}