package bskyblock.addon.challenges;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.plugin.PluginManager;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class ParseItemTest {

    private static ChallengesAddon addon;
    private static ItemFactory itemFactory;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        addon = mock(ChallengesAddon.class);
        when(addon.getLogger()).thenReturn(Logger.getAnonymousLogger());
        Server server = mock(Server.class);
        World world = mock(World.class);
        when(server.getLogger()).thenReturn(Logger.getAnonymousLogger());
        when(server.getWorld("world")).thenReturn(world);
        when(server.getVersion()).thenReturn("BSB_Mocking");

        PluginManager pluginManager = mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(pluginManager);

        itemFactory = mock(ItemFactory.class);
        when(server.getItemFactory()).thenReturn(itemFactory);

        Bukkit.setServer(server);

        SpawnEggMeta itemMeta = mock(SpawnEggMeta.class);
        when(itemFactory.getItemMeta(any())).thenReturn(itemMeta);
        when(Bukkit.getItemFactory()).thenReturn(itemFactory);

    }


    @Test
    public void parseItemTest() {
        // Nothing test
        assertNull(new ParseItem(addon, "").getItem());
        // other
        assertNull(new ParseItem(addon, "::::::::::::::").getItem());
        // other
        assertNull(new ParseItem(addon, "anything:anything").getItem());
        // Bad material
        assertNull(new ParseItem(addon, "nosuchmaterial:2").getItem());

        // Material
        for (Material mat : Material.values()) {
            ItemStack test = new ParseItem(addon, mat.name() + ":5").getItem();
            if (test.getType().toString().endsWith("_ITEM") && !mat.toString().endsWith("_ITEM")) {
                assertEquals(mat.toString() + "_ITEM", test.getType().toString());
            } else {
                assertEquals(mat, test.getType());
            }
        }

        // Nothing amount
        ItemStack test = new ParseItem(addon, "STONE:").getItem();
        assertNull(test);


        // Test 3
        // Bad material
        assertNull(new ParseItem(addon, "nosuchmaterial:2:2").getItem());

        // Bad amount
        assertNull(new ParseItem(addon, "STONE:1:sdfgsd").getItem());

        // Missing amount = 1
        test = new ParseItem(addon, "STONE:1:").getItem();
        assertNotNull(test); // This is okay, it's just a 2

        // Test Potions
        PotionMeta itemMeta = mock(PotionMeta.class);
        when(itemFactory.getItemMeta(any())).thenReturn(itemMeta);
        when(Bukkit.getItemFactory()).thenReturn(itemFactory);

        // Bad material
        assertNull(new ParseItem(addon, "nosuchmaterial:JUMP:2:NOTEXTENDED:NOSPLASH:2").getItem());
        // Bad amount
        assertNull(new ParseItem(addon, "POTION:JUMP:2:NOTEXTENDED:NOSPLASH:asfdas").getItem());
        test = new ParseItem(addon, "POTION:JUMP:2:NOTEXTENDED:NOSPLASH:").getItem();
        assertNull(test);

        test = new ParseItem(addon, "POTION:JUMP:2:NOTEXTENDED:NOSPLASH:1").getItem();
        assertEquals(Material.POTION, test.getType());
        test = new ParseItem(addon, "POTION:STRENGTH:1:EXTENDED:SPLASH:1").getItem();
        assertEquals(Material.SPLASH_POTION, test.getType());
        test = new ParseItem(addon, "POTION:INSTANT_DAMAGE:2::LINGER:2").getItem();
        assertEquals(Material.LINGERING_POTION, test.getType());
        test = new ParseItem(addon, "TIPPED_ARROW:STRENGTH:1:::1").getItem();
        assertEquals(Material.TIPPED_ARROW, test.getType());
    }

}
