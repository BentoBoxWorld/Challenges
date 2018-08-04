/**
 * 
 */
package bentobox.addon.challenges;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import bentobox.addon.challenges.database.object.Challenges;
import bentobox.addon.challenges.database.object.Challenges.ChallengeType;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
public class ChallengesAddonTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Server server = mock(Server.class);
        World world = mock(World.class);
        world = mock(World.class);
        Mockito.when(server.getLogger()).thenReturn(Logger.getAnonymousLogger());
        Mockito.when(server.getWorld("world")).thenReturn(world);
        Mockito.when(server.getVersion()).thenReturn("BSB_Mocking");
        
        PluginManager pluginManager = mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(pluginManager);
        
        ItemFactory itemFactory = mock(ItemFactory.class);
        when(server.getItemFactory()).thenReturn(itemFactory);
        
        Bukkit.setServer(server);
        
        PotionMeta potionMeta = mock(PotionMeta.class);
        when(itemFactory.getItemMeta(any())).thenReturn(potionMeta);
        
        OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
        when(Bukkit.getOfflinePlayer(any(UUID.class))).thenReturn(offlinePlayer);
        when(offlinePlayer.getName()).thenReturn("tastybento");

        when(Bukkit.getItemFactory()).thenReturn(itemFactory);
        when(Bukkit.getLogger()).thenReturn(Logger.getAnonymousLogger());

    }

    @Test
    public void test() {
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Challenges challenges = new Challenges();
        challenges.setChallengeType(ChallengeType.ISLAND);
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
        String json = gson.toJson(challenges);

        Logger.getAnonymousLogger().info(json);
        
    }

}
