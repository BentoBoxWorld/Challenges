package world.bentobox.challenges.database.object;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.configuration.ConfigComment;
import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;
import world.bentobox.challenges.managers.ChallengesManager;


/**
 * Represent a challenge level
 * @author tastybento
 *
 */
@Table(name = "ChallengeLevel")
public class ChallengeLevel implements DataObject, Comparable<ChallengeLevel>
{
    /**
     * Constructor ChallengeLevel creates a new ChallengeLevel instance.
     */
    public ChallengeLevel()
    {
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    @ConfigComment("")
    @ConfigComment("Level name")
    @Expose
    private String uniqueId = ChallengesManager.FREE;

    @ConfigComment("")
    @ConfigComment("A friendly name for the level. If blank, level name is used.")
    @Expose
    private String friendlyName = "";

    @ConfigComment("")
    @ConfigComment("ItemStack that represents current level. Will be used as icon in GUIs.")
    @Expose
    private ItemStack icon = new ItemStack(Material.BOOK);

    @ConfigComment("")
    @ConfigComment("ItemStack that represents current level when it is locked. Will be used")
    @ConfigComment("as icon in GUIs. Will overwrite icon defined in addon settings.")
    @Expose
    private ItemStack lockedIcon = null;

    @ConfigComment("")
    @ConfigComment("World that this level applies in. String.")
    @Expose
    private String world = "";

    @ConfigComment("")
    @ConfigComment("The ordering of the level, lowest to highest")
    @Expose
    private int order;

    @ConfigComment("")
    @ConfigComment("The number of undone challenges that can be left on this level before")
    @ConfigComment("unlocking next level. Give players more with the permission")
    @ConfigComment("[gamemode].challenges.waiver-add.x where x is a number")
    @Expose
    private int waiverAmount = 1;

    @ConfigComment("")
    @ConfigComment("The message shown when unlocking this level. Single line string.")
    @Expose
    private String unlockMessage = "";

    @ConfigComment("")
    @ConfigComment("")
    @ConfigComment("If this is blank, the reward text will be auto-generated, otherwise")
    @ConfigComment("this will be used.")
    @Expose
    private String rewardText = "";


    @ConfigComment("")
    @ConfigComment("List of items the player will receive on completing level.")
    @ConfigComment("ItemStack List.")
    @Expose
    private List<ItemStack> rewardItems = new ArrayList<>();

    @ConfigComment("")
    @ConfigComment("Experience point reward on completing level.")
    @Expose
    private int rewardExperience = 0;

    @ConfigComment("")
    @ConfigComment("Money reward. Economy plugin or addon required for this option.")
    @Expose
    private double rewardMoney = 0;

    @ConfigComment("")
    @ConfigComment("Commands to run when the player completes all challenges in current")
    @ConfigComment("level. String List")
    @Expose
    private List<String> rewardCommands = new ArrayList<>();

    @ConfigComment("")
    @ConfigComment("Set of all challenges that is linked with current level.")
    @ConfigComment("String Set")
    @Expose
    private Set<String> challenges = new HashSet<>();

    @ConfigComment("")
    @ConfigComment("Set of materials which metadata can be ignored.")
    @Expose
    private Set<Material> ignoreRewardMetaData = new HashSet<>();

// ---------------------------------------------------------------------
// Section: Getters
// ---------------------------------------------------------------------


    /**
     * This method returns the uniqueId value.
     * @return the value of uniqueId.
     * @see DataObject#getUniqueId()
     */
    @Override
    public String getUniqueId()
    {
        return uniqueId;
    }


    /**
     * This method returns the friendlyName value.
     * @return the value of friendlyName.
     */
    public String getFriendlyName()
    {
        return friendlyName;
    }


    /**
     * This method returns the icon value.
     * @return the value of icon.
     */
    public ItemStack getIcon()
    {
        if (icon == null) {
            icon = new ItemStack(Material.PAPER);
        }
        return icon.clone();
    }


    /**
     * This method returns the closedIcon value.
     * @return the value of closedIcon.
     */
    public ItemStack getLockedIcon()
    {
        return lockedIcon;
    }


    /**
     * This method returns the world value.
     * @return the value of world.
     */
    public String getWorld()
    {
        return world;
    }


    /**
     * This method returns the order value.
     * @return the value of order.
     */
    public int getOrder()
    {
        return order;
    }


    /**
     * This method returns the waiverAmount value.
     * @return the value of waiverAmount.
     */
    public int getWaiverAmount()
    {
        return waiverAmount;
    }


    /**
     * This method returns the unlockMessage value.
     * @return the value of unlockMessage.
     */
    public String getUnlockMessage()
    {
        return unlockMessage;
    }


    /**
     * This method returns the rewardText value.
     * @return the value of rewardText.
     */
    public String getRewardText()
    {
        return rewardText;
    }


    /**
     * This method returns the rewardItems value.
     * @return the value of rewardItems.
     */
    public List<ItemStack> getRewardItems()
    {
        return rewardItems;
    }


    /**
     * This method returns the rewardExperience value.
     * @return the value of rewardExperience.
     */
    public int getRewardExperience()
    {
        return rewardExperience;
    }


    /**
     * This method returns the rewardMoney value.
     * @return the value of rewardMoney.
     */
    public double getRewardMoney()
    {
        return rewardMoney;
    }


    /**
     * This method returns the rewardCommands value.
     * @return the value of rewardCommands.
     */
    public List<String> getRewardCommands()
    {
        return rewardCommands;
    }


    /**
     * This method returns the challenges value.
     * @return the value of challenges.
     */
    public Set<String> getChallenges()
    {
        return challenges;
    }


    /**
     * Gets ignore reward meta data.
     *
     * @return the ignore reward meta data
     */
    public Set<Material> getIgnoreRewardMetaData()
    {
        return ignoreRewardMetaData;
    }


// ---------------------------------------------------------------------
// Section: Setters
// ---------------------------------------------------------------------


    /**
     * This method sets the uniqueId value.
     * @param uniqueId the uniqueId new value.
     *
     * @see DataObject#setUniqueId(String)
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
     * This method sets the icon value.
     * @param icon the icon new value.
     *
     */
    public void setIcon(ItemStack icon)
    {
        this.icon = icon;
    }


    /**
     * This method sets the closedIcon value.
     * @param closedIcon the closedIcon new value.
     *
     */
    public void setLockedIcon(ItemStack closedIcon)
    {
        this.lockedIcon = closedIcon;
    }


    /**
     * This method sets the world value.
     * @param world the world new value.
     *
     */
    public void setWorld(String world)
    {
        this.world = world;
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
     * This method sets the waiverAmount value.
     * @param waiverAmount the waiverAmount new value.
     *
     */
    public void setWaiverAmount(int waiverAmount)
    {
        this.waiverAmount = waiverAmount;
    }


    /**
     * This method sets the unlockMessage value.
     * @param unlockMessage the unlockMessage new value.
     *
     */
    public void setUnlockMessage(String unlockMessage)
    {
        this.unlockMessage = unlockMessage;
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
     * This method sets the challenges value.
     * @param challenges the challenges new value.
     *
     */
    public void setChallenges(Set<String> challenges)
    {
        this.challenges = challenges;
    }


    /**
     * Sets ignore reward meta data.
     *
     * @param ignoreRewardMetaData the ignore reward meta data
     */
    public void setIgnoreRewardMetaData(Set<Material> ignoreRewardMetaData)
    {
        this.ignoreRewardMetaData = ignoreRewardMetaData;
    }


// ---------------------------------------------------------------------
// Section: Other methods
// ---------------------------------------------------------------------


    /**
     * This method match if given gameMode relates to current level. It is detected
     * via level uniqueId, as it always must start with gameMode name.
     *
     * This method is created to avoid issues with capital letters in world names in 1.14.
     * It is reused for GameMode storage change.
     * @param gameMode Name that must be checked.
     * @return {@code true} if current level relates to given gameMode name, otherwise
     * {@code false}.
     */
    public boolean matchGameMode(String gameMode)
    {
        return gameMode != null &&
            this.uniqueId.regionMatches(true, 0, gameMode, 0, gameMode.length());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@NotNull ChallengeLevel o)
    {
        if (this.equals(o))
        {
            return 0;
        }
        else
        {
            if (this.getWorld().equals(o.getWorld()))
            {
                if (this.order == o.getOrder())
                {
                    return this.getUniqueId().compareTo(o.getUniqueId());
                }
                else
                {
                    return Integer.compare(this.order, o.getOrder());
                }
            }
            else
            {
                return this.getWorld().compareTo(o.getWorld());
            }
        }
    }


    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (!(obj instanceof ChallengeLevel other))
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
            this.challenges != null &&
            this.icon != null &&
            this.world != null &&
            this.unlockMessage != null &&
            this.rewardText != null &&
            this.rewardItems.stream().noneMatch(Objects::isNull) &&
            this.rewardCommands != null;
    }


    /**
     * Clone method that returns clone of current challengeLevel.
     * @return ChallengeLevel that is cloned from current object.
     */
    public ChallengeLevel copy()
    {
        ChallengeLevel clone = new ChallengeLevel();

        try
        {
            clone.setUniqueId(this.uniqueId);
            clone.setFriendlyName(this.friendlyName);
            clone.setIcon(this.icon.clone());
            clone.setLockedIcon(this.lockedIcon != null ? this.lockedIcon.clone() : null);
            clone.setWorld(this.world);
            clone.setOrder(this.order);
            clone.setWaiverAmount(this.waiverAmount);
            clone.setUnlockMessage(this.unlockMessage);
            clone.setRewardText(this.rewardText);
            clone.setRewardItems(
                this.rewardItems.stream().
                    map(ItemStack::clone).
                    collect(Collectors.toCollection(() -> new ArrayList<>(this.rewardItems.size()))));
            clone.setRewardExperience(this.rewardExperience);
            clone.setRewardMoney(this.rewardMoney);
            clone.setRewardCommands(new ArrayList<>(this.rewardCommands));
            clone.setChallenges(new HashSet<>(this.challenges));
            clone.setIgnoreRewardMetaData(new HashSet<>(this.ignoreRewardMetaData));
        }
        catch (Exception e)
        {
            BentoBox.getInstance().logError("Failed to clone ChallengeLevel " + this.uniqueId);
            BentoBox.getInstance().logStacktrace(e);
            clone = this;
        }

        return clone;
    }
}