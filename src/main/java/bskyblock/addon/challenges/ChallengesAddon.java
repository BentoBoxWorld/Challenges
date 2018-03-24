package bskyblock.addon.challenges;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import com.google.gson.Gson;

import bskyblock.addon.challenges.commands.ChallengesCommand;
import bskyblock.addon.challenges.commands.admin.ChallengesAdminCommand;
import bskyblock.addon.challenges.config.PluginConfig;
import bskyblock.addon.challenges.database.object.Challenges;
import bskyblock.addon.challenges.database.object.Challenges.ChallengeType;
import us.tastybento.bskyblock.BSkyBlock;
import us.tastybento.bskyblock.api.addons.Addon;
import us.tastybento.bskyblock.database.DatabaseConnectionSettingsImpl;
import us.tastybento.bskyblock.database.mysql.MySQLDatabaseConnecter;

/**
 * Add-on to BSkyBlock that enables challenges
 * @author tastybento
 *
 */
public class ChallengesAddon extends Addon {

    private ChallengesManager challengesManager;

    @Override
    public void onEnable() {
        // Load the plugin's config
        new PluginConfig(this);
        // Check if it is enabled - it might be loaded, but not enabled.
        if (getBSkyBlock() == null || !getBSkyBlock().isEnabled()) {
            Bukkit.getLogger().severe("BSkyBlock is not available or disabled!");
            this.setEnabled(false);
            return;
        }

        // Challenges Manager
        challengesManager = new ChallengesManager(this);
        // Register commands
        new ChallengesCommand(this);
        new ChallengesAdminCommand(this);
        // Done
        /*
        Gson gson = new Gson();
        Challenges challenges = new Challenges();
        challenges.setChallengeType(ChallengeType.SURROUNDING);
        Map<Material, Integer> map = new HashMap<>();
        map.put(Material.DIRT, 5);
        map.put(Material.ACACIA_FENCE_GATE, 3);
        challenges.setRequiredBlocks(map);
        challenges.setIcon(new ItemStack(Material.ACACIA_FENCE_GATE));
        List<ItemStack> requiredItems = new ArrayList<>();
        ItemStack result = new ItemStack(Material.POTION, 55);
        ItemStack result2 = new ItemStack(Material.SPLASH_POTION, 22);
        ItemStack result3 = new ItemStack(Material.LINGERING_POTION, 11);

        PotionMeta potionMeta = (PotionMeta) result.getItemMeta();
        PotionData potionData = new PotionData(PotionType.FIRE_RESISTANCE, true, false);
        potionMeta.setBasePotionData(potionData); 
        result.setItemMeta(potionMeta);

        PotionMeta potionMeta2 = (PotionMeta) result2.getItemMeta();
        PotionData potionData2 = new PotionData(PotionType.SPEED, true, false);
        potionMeta2.setBasePotionData(potionData2); 
        potionMeta2.addEnchant(Enchantment.BINDING_CURSE, 1, true);
        result2.setItemMeta(potionMeta2);

        requiredItems.add(result);
        requiredItems.add(result2);
        requiredItems.add(result3);
        challenges.setRequiredItems(requiredItems);
        challenges.setUniqueId(UUID.randomUUID().toString());
        String json = gson.toJson(challenges);

        Logger.getAnonymousLogger().info(json);

        BSkyBlock plugin = BSkyBlock.getInstance();
        MySQLDatabaseConnecter conn = new MySQLDatabaseConnecter(new DatabaseConnectionSettingsImpl(
                plugin.getSettings().getDbHost(),
                plugin.getSettings().getDbPort(),
                plugin.getSettings().getDbName(),
                plugin.getSettings().getDbUsername(),
                plugin.getSettings().getDbPassword()
                ));
        try (Connection connection = conn.createConnection()) {
            StringBuilder sql = new StringBuilder();
            sql.append("create table if not exists test (json JSON, uniqueId VARCHAR(255) GENERATED ALWAYS AS (json->\"$.uniqueId\"), INDEX i (uniqueId) );");
            // Prepare and execute the database statements
            try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe(() -> "Problem trying to create schema for data object ");
            }
            sql = new StringBuilder();
            sql.append("INSERT INTO `TEST` (`json`) VALUES (?)");
            try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
                pstmt.setString(1, json);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe(() -> "Problem trying to create data object ");
                e.printStackTrace();
            }
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        */
    }

    @Override
    public void onDisable(){
    }

    public ChallengesManager getChallengesManager() {
        return challengesManager;
    }

}
