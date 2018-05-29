package bskyblock.addon.challenges.database.object;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import bskyblock.addon.challenges.ChallengesManager;
import us.tastybento.bskyblock.api.configuration.ConfigComment;
import us.tastybento.bskyblock.database.objects.DataObject;

/**
 * Represent a challenge level
 * @author tastybento
 *
 */
public class ChallengeLevels implements DataObject, Comparable<ChallengeLevels> {

    public ChallengeLevels() {}

    @ConfigComment("A friendly name for the level. If blank, level name is used.")
    private String friendlyName = "";
    
    @ConfigComment("Worlds that this level applies in. String list.")
    private List<String> worlds = new ArrayList<>();

    @ConfigComment("Commands to run when this level is completed")
    private List<String> rewardCommands = new ArrayList<>();
    
    @ConfigComment("Level name")
    private String uniqueId = ChallengesManager.FREE;
    
    @ConfigComment("The number of undone challenges that can be left on this level before unlocking next level")
    private int waiveramount = 1;
    
    @ConfigComment("The ordering of the levels, lowest to highest")
    private int order = 0;
    
    @ConfigComment("The message shown when unlocking this level")
    private String unlockMessage = "";
    
    @ConfigComment("Unlock reward description")
    private String rewardDescription = "";
    
    @ConfigComment("List of reward itemstacks")
    private List<ItemStack> rewardItems;
    
    @ConfigComment("Unlock experience reward")
    private int expReward;
    
    @ConfigComment("Unlock money reward")
    private int moneyReward;
    
    public String getFriendlyName() {
        return friendlyName;
    }

    public List<String> getRewardCommands() {
        return rewardCommands = new ArrayList<>();
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * Get the number of undone tasks that can be left on a level before unlocking next level
     * @return
     */
    public int getWaiveramount() {
        return waiveramount;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public void setRewardCommands(List<String> rewardCommands) {
        this.rewardCommands = rewardCommands;
    }

    @Override
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setWaiveramount(int waiveramount) {
        this.waiveramount = waiveramount;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int compareTo(ChallengeLevels o) {
        return Integer.compare(this.order, o.order);
    }
    
    /**
     * @return the rewardDescription
     */
    public String getRewardDescription() {
        return rewardDescription;
    }

    /**
     * @param rewardDescription the rewardDescription to set
     */
    public void setRewardDescription(String rewardDescription) {
        this.rewardDescription = rewardDescription;
    }

    /**
     * @return the rewardItems
     */
    public List<ItemStack> getRewardItems() {
        return rewardItems;
    }

    /**
     * @param rewardItems the rewardItems to set
     */
    public void setRewardItems(List<ItemStack> rewardItems) {
        this.rewardItems = rewardItems;
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
     * @return the unlockMessage
     */
    public String getUnlockMessage() {
        return unlockMessage;
    }

    /**
     * @param unlockMessage the unlockMessage to set
     */
    public void setUnlockMessage(String unlockMessage) {
        this.unlockMessage = unlockMessage;
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
        if (!(obj instanceof ChallengeLevels)) {
            return false;
        }
        ChallengeLevels other = (ChallengeLevels) obj;
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
