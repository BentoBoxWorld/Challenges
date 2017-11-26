package bskyblock.addin.challenges;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import bskyblock.addin.challenges.database.object.ChallengesDO;
import bskyblock.addin.challenges.database.object.LevelsDO;
import bskyblock.addin.challenges.panel.Panel;
import bskyblock.addin.challenges.panel.Panel.PanelBuilder;
import bskyblock.addin.challenges.panel.Panel.PanelItem;
import us.tastybento.bskyblock.database.flatfile.FlatFileDatabase;
import us.tastybento.bskyblock.database.managers.AbstractDatabaseHandler;
import us.tastybento.bskyblock.util.Util;

public class ChallengesManager implements Listener {

    private static final boolean DEBUG = false;
    private Challenges plugin;
    private HashMap<UUID, Panel> challengePanels;

    private AbstractDatabaseHandler<ChallengesDO> chHandler;
    private HashMap<String,ChallengesDO> challenges;
    private AbstractDatabaseHandler<LevelsDO> lvHandler;
    private HashMap<String,LevelsDO> levels;

    @SuppressWarnings("unchecked")
    public ChallengesManager(Challenges plugin){
        this.plugin = plugin;
        // Set up the database handler to store and retrieve Challenges
        chHandler = (AbstractDatabaseHandler<ChallengesDO>) new FlatFileDatabase().getHandler(plugin, ChallengesDO.class);
        lvHandler = (AbstractDatabaseHandler<LevelsDO>) new FlatFileDatabase().getHandler(plugin, LevelsDO.class);
        challenges = new HashMap<>();
        levels = new HashMap<>();
        challengePanels = new HashMap<>();
        load();
    }

    public AbstractDatabaseHandler<ChallengesDO> getHandler() {
        return chHandler;
    }

    /**
     * Clear and reload all challenges
     */
    public void load() {
        levels.clear();
        try {
            for (LevelsDO level : lvHandler.loadObjects()) {
                levels.put(level.getUniqueId(), level);
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | SecurityException | ClassNotFoundException | IntrospectionException
                | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        challenges.clear();
        try {
            for (ChallengesDO challenge : chHandler.loadObjects()) {
                challenges.put(challenge.getUniqueId(), challenge);
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | SecurityException | ClassNotFoundException | IntrospectionException
                | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Save to the database
     * @param async - if true, saving will be done async
     */
    public void save(boolean async){
        if(async){
            Runnable save = () -> {
                int index = 1;
                for(ChallengesDO challenge : challenges.values()){
                    plugin.getLogger().info("DEBUG: saving challenges async " + index++);
                    try {
                        chHandler.saveObject(challenge);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, save);
        } else {
            int index = 1;
            for(ChallengesDO challenge : challenges.values()){
                plugin.getLogger().info("DEBUG: saving challenges sync " + index++);
                try {
                    chHandler.saveObject(challenge);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Metrics-related methods //

    public void shutdown(){
        save(false);
        challenges.clear();
    }

    /**
     * Create a challenge from the inventory contents
     * @param contents
     */
    public void createChallenge(Player player, String name) {
        // Get the main icon
        ItemStack icon = player.getInventory().getItemInOffHand();
        if (icon == null || icon.getType().equals(Material.AIR)) {
            Util.sendMessage(player, "Icon will be paper");
            icon = new ItemStack(Material.PAPER);
        }
        icon.setAmount(1);
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        lore.add("Required items:");

        List<ItemStack> inv = Arrays.asList(player.getInventory().getStorageContents());
        List<ItemStack> contents = new ArrayList<>();
        for (ItemStack item : inv) {
            if (item != null && !item.getType().equals(Material.AIR)) {
                contents.add(item);
                lore.add(item.getType() + " x " + item.getAmount());
            }
        }
        if (lore.size() == 1) {
            lore.add("No items");
        }
        meta.setDisplayName(name);
        meta.setLore(lore);
        icon.setItemMeta(meta);
        ChallengesDO newChallenge = new ChallengesDO();
        newChallenge.setRequiredItems(contents);
        newChallenge.setUniqueId(name);
        newChallenge.setIcon(icon);
        if (chHandler.objectExits(name)) {
            Util.sendMessage(player, ChatColor.RED + "Challenge already exists! Use /c replace <name>");
            return;
        }
        try {
            chHandler.saveObject(newChallenge);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException
                | InstantiationException | NoSuchMethodException | IntrospectionException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Util.sendMessage(player, ChatColor.RED + "Challenge creation failed! " + e.getMessage());
            return;
        }
        Util.sendMessage(player, "Challenge accepted!");
        challenges.put(newChallenge.getUniqueId(), newChallenge);
    }

    public Inventory getChallenges(Player player) {
        // TODO build the panel that is customized to the player
        // Build panel
        PanelBuilder panelBuilder = Panel.builder(plugin)
                .name(plugin.getLocale(player).get("challenges.guiTitle"));
        for (ChallengesDO challenge: challenges.values()) {
            plugin.getLogger().info("Adding challenge " + challenge.getUniqueId());
            PanelItem item = Panel.panelItemBuilder()
            .setIcon(challenge.getIcon())
            .setName(challenge.getFriendlyName().isEmpty() ? challenge.getUniqueId() : challenge.getFriendlyName())
            .setDescription(challenge.getDescription())
            .setSlot(challenge.getSlot())
            .build();
            plugin.getLogger().info("requested slot" + item.getSlot());
            panelBuilder.addItem(item);
        }
        Panel panel = panelBuilder.build();
        challengePanels.put(player.getUniqueId(), panel);
        plugin.getLogger().info("DEBUG: added inv " + challengePanels.size());
        return panel.getPanel();
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked(); // The player that
        // clicked the item
        UUID playerUUID = player.getUniqueId();
        Inventory inventory = event.getInventory(); // The inventory that was
        // clicked in
        // Check this is the right panel
        if (inventory.getName() == null || !inventory.getName().equals(plugin.getLocale(player).get("challenges.guiTitle"))) {
            return;
        }
        event.setCancelled(true);
        if (!event.getClick().equals(ClickType.LEFT)) {            
            inventory.clear();
            player.closeInventory();
            player.updateInventory();
            return;
        }
        int slot = event.getRawSlot();
        if (slot == -999) {
            inventory.clear();
            player.closeInventory();
            event.setCancelled(true);
            return;
        }
        // TODO: Deal with the clicking
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        challengePanels.remove(event.getPlayer().getUniqueId()); 
        plugin.getLogger().info("DEBUG: removing inv " + challengePanels.size());
    }
    
    /**
     * Clean up the hashmap should the player open up another inventory
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer(); 
        UUID playerUUID = player.getUniqueId();
        Inventory inventory = event.getInventory(); // The inventory that was
        if (inventory.getName() == null || !inventory.getName().equals(plugin.getLocale(player).get("challenges.guiTitle"))) {
            challengePanels.remove(playerUUID);
            plugin.getLogger().info("DEBUG: removing inv " + challengePanels.size());
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onLogOut(PlayerQuitEvent event) {
        challengePanels.remove(event.getPlayer().getUniqueId());
        plugin.getLogger().info("DEBUG: removing inv " + challengePanels.size());
    }
}
