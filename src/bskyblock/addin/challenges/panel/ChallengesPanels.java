package bskyblock.addin.challenges.panel;

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

import bskyblock.addin.challenges.Challenges;
import bskyblock.addin.challenges.ChallengesManager;
import bskyblock.addin.challenges.ChallengesManager.LevelStatus;
import bskyblock.addin.challenges.database.object.ChallengesDO;
import bskyblock.addin.challenges.panel.Panel.PanelBuilder;
import bskyblock.addin.challenges.panel.Panel.PanelItem;

public class ChallengesPanels implements Listener {
    private static final boolean DEBUG = false;
    private static final String CHALLENGE_COMMAND = "challenges";
    private Challenges plugin;
    private HashMap<UUID, Panel> challengePanels;
    private ChallengesManager manager;

    public ChallengesPanels(Challenges plugin, ChallengesManager manager){
        this.plugin = plugin;
        this.manager = manager;
        challengePanels = new HashMap<>();
    }

    /**
     * @param player
     * @return
     */
    public Inventory getChallenges(Player player) {
        return getChallenges(player, "");
    }

    /**
     * Dynamically creates an inventory of challenges for the player showing the
     * level
     * 
     * @param player
     * @param level
     * @return inventory
     */
    public Inventory getChallenges(Player player, String level) {
        plugin.getLogger().info("DEBUG: level requested = " + level);
        PanelBuilder panelBuilder = Panel.builder(plugin)
                .name(plugin.getLocale(player).get("challenges.guiTitle"));
        List<ChallengesDO> levelChallenges = manager.getChallenges(level);
        // Do some checking
        if (DEBUG)
            plugin.getLogger().info("DEBUG: Opening level " + level);
        // Only show a control panel for the level requested.
        for (ChallengesDO challenge : levelChallenges) {
            plugin.getLogger().info("Adding challenge " + challenge.getUniqueId());
            boolean completed = manager.isChallengeComplete(player.getUniqueId(), challenge.getUniqueId());
            if (completed && challenge.isRemoveWhenCompleted())
                continue;
            PanelItem item = Panel.panelItemBuilder()
                    .setIcon(challenge.getIcon())
                    .setName(challenge.getFriendlyName().isEmpty() ? challenge.getUniqueId() : challenge.getFriendlyName())
                    .setDescription(challenge.getDescription())
                    .setSlot(challenge.getSlot())
                    .setGlow(completed)
                    .setCommand(CHALLENGE_COMMAND + " c " + challenge.getUniqueId())
                    .build();
            plugin.getLogger().info("requested slot" + item.getSlot());
            panelBuilder.addItem(item);
        }
        // Add navigation to other levels
        for (LevelStatus status: manager.getChallengeLevelStatus(player)) {
            String name = ChatColor.GOLD + (status.getLevel().getFriendlyName().isEmpty() ? status.getLevel().getUniqueId() : status.getLevel().getFriendlyName());
            if (status.isComplete() || status.getPreviousLevel() == null) {
                // Clicking on this icon will open up this level's challenges
                PanelItem item = Panel.panelItemBuilder()
                        .setIcon(new ItemStack(Material.BOOK_AND_QUILL))
                        .setName(name)
                        .setDescription(plugin.getLocale(player).get("challenges.navigation").replace("[level]",name))
                        .setCommand(CHALLENGE_COMMAND + " c " + status.getLevel().getUniqueId())
                        .build();
                panelBuilder.addItem(item);  
            } else {
                // Clicking on this icon will do nothing because the challenge is not unlocked yet
                String previousLevelName = ChatColor.GOLD + (status.getPreviousLevel().getFriendlyName().isEmpty() ? status.getPreviousLevel().getUniqueId() : status.getPreviousLevel().getFriendlyName());
                PanelItem item = Panel.panelItemBuilder()
                        .setIcon(new ItemStack(Material.BOOK))
                        .setName(name)
                        .setDescription((plugin.getLocale(player).get("challenges.toComplete").replace("[challengesToDo]",String.valueOf(status.getNumberOfChallengesStillToDo()))).replace("[thisLevel]", previousLevelName))
                        .build();
                panelBuilder.addItem(item);
            }
        }
        /*
        // Add the free challenges if not already shown (which can happen if all of the challenges are done!)
        if (!level.equals("") && challengeList.containsKey("")) {
            for (String freeChallenges: challengeList.get("")) {
                CPItem item = createItem(freeChallenges, player);
                if (item != null) {
                    cp.add(item);
                } 
            }
        }*/
        // Create the panel
        Panel panel = panelBuilder.build();
        challengePanels.put(player.getUniqueId(), panel);
        return panel.getPanel();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked(); // The player that
        // clicked the item
        //UUID playerUUID = player.getUniqueId();
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
