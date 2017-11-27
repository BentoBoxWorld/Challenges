package bskyblock.addin.challenges.database.object;

import java.util.ArrayList;
import java.util.List;

import us.tastybento.bskyblock.database.objects.DataObject;

public class LevelsDO extends DataObject implements Comparable<LevelsDO> {

    /**
     * A friendly name for the level. If blank, level name is used.
     */
    private String friendlyName = "";
    /**
     * Commands to run when this level is completed
     */
    private List<String> rewardCommands = new ArrayList<>();
    /**
     * Level name
     */
    private String uniqueId = "";
    /**
     * The number of undone challenges that can be left on this level before unlocking next level
     */
    private int waiveramount = 1;
    
    /**
     * The ordering of the levels, lowest to highest
     */
    private int order = 0;
    
    public String getFriendlyName() {
        return friendlyName;
    }

    public List<String> getRewardCommands() {
        return rewardCommands;
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

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
    public int compareTo(LevelsDO o) {
        return Integer.compare(this.order, o.order);
    }
    
}
