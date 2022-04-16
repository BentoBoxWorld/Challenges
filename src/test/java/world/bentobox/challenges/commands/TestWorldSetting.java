package world.bentobox.challenges.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.bentobox.api.configuration.WorldSettings;
import world.bentobox.bentobox.api.flags.Flag;

public class TestWorldSetting implements WorldSettings {

    private Map<String, Boolean> flags = new HashMap<>();

    @Override
    public GameMode getDefaultGameMode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Flag, Integer> getDefaultIslandFlags() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Flag, Integer> getDefaultIslandSettings() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Difficulty getDifficulty() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDifficulty(Difficulty difficulty) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getFriendlyName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getIslandDistance() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getIslandHeight() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getIslandProtectionRange() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getIslandStartX() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getIslandStartZ() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getIslandXOffset() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getIslandZOffset() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<String> getIvSettings() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getMaxHomes() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getMaxIslands() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getMaxTeamSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getNetherSpawnRadius() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getPermissionPrefix() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<EntityType> getRemoveMobsWhitelist() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getSeaHeight() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<String> getHiddenFlags() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getVisitorBannedCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Boolean> getWorldFlags() {
        // TODO Auto-generated method stub
        return flags ;
    }

    @Override
    public String getWorldName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isDragonSpawn() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEndGenerate() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEndIslands() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isNetherGenerate() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isNetherIslands() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOnJoinResetEnderChest() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOnJoinResetInventory() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOnJoinResetMoney() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOnJoinResetHealth() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOnJoinResetHunger() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOnJoinResetXP() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public @NonNull List<String> getOnJoinCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isOnLeaveResetEnderChest() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOnLeaveResetInventory() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOnLeaveResetMoney() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOnLeaveResetHealth() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOnLeaveResetHunger() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOnLeaveResetXP() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public @NonNull List<String> getOnLeaveCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isUseOwnGenerator() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isWaterUnsafe() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<String> getGeoLimitSettings() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getResetLimit() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getResetEpoch() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setResetEpoch(long timestamp) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isTeamJoinDeathReset() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getDeathsMax() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isDeathsCounted() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDeathsResetOnNewIsland() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAllowSetHomeInNether() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAllowSetHomeInTheEnd() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRequireConfirmationToSetHomeInNether() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRequireConfirmationToSetHomeInTheEnd() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getBanLimit() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isLeaversLoseReset() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isKickedKeepInventory() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCreateIslandOnFirstLoginEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getCreateIslandOnFirstLoginDelay() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isCreateIslandOnFirstLoginAbortOnLogout() {
        // TODO Auto-generated method stub
        return false;
    }

}
