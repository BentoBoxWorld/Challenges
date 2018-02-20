package bskyblock.addon.challenges.panel;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import bskyblock.addon.challenges.Challenges;
import bskyblock.addon.challenges.ChallengesManager;
import bskyblock.addon.challenges.ChallengesManager.LevelStatus;
import bskyblock.addon.challenges.database.object.ChallengesData;
import us.tastybento.bskyblock.api.commands.User;
import us.tastybento.bskyblock.api.panels.ClickType;
import us.tastybento.bskyblock.api.panels.Panel;
import us.tastybento.bskyblock.api.panels.PanelItem;
import us.tastybento.bskyblock.api.panels.builders.PanelBuilder;
import us.tastybento.bskyblock.api.panels.builders.PanelItemBuilder;


public class ChallengesPanels {
    private static final boolean DEBUG = false;
    //private static final String CHALLENGE_COMMAND = "challenges";
    private Challenges plugin;
    private ChallengesManager manager;

    public ChallengesPanels(Challenges plugin, ChallengesManager manager){
        this.plugin = plugin;
        this.manager = manager;
    }

    /**
     * @param user
     * @return
     */
    public void getChallenges(User user) {
        getChallenges(user, "");
    }

    /**
     * Dynamically creates an inventory of challenges for the player showing the
     * level
     * 
     * @param user
     * @return inventory
     */
    public void getChallenges(User user, String level) {
        plugin.getLogger().info("DEBUG: level requested = " + level);
        PanelBuilder panelBuilder = new PanelBuilder()
                .setName(user.getTranslation("challenges.guiTitle"));
        List<ChallengesData> levelChallenges = manager.getChallenges(level);
        // Do some checking
        if (DEBUG)
            plugin.getLogger().info("DEBUG: Opening level " + level);
        // Only show a control panel for the level requested.
        for (ChallengesData challenge : levelChallenges) {
            plugin.getLogger().info("Adding challenge " + challenge.getUniqueId());
            boolean completed = manager.isChallengeComplete(user.getUniqueId(), challenge.getUniqueId());
            if (completed && challenge.isRemoveWhenCompleted())
                continue;
            PanelItem item = new PanelItemBuilder()
                    .icon(challenge.getIcon())
                    .name(challenge.getFriendlyName().isEmpty() ? challenge.getUniqueId() : challenge.getFriendlyName())
                    .description(challenge.getDescription())
                    .glow(completed)
                    .clickHandler(new PanelItem.ClickHandler() {
                        @Override
                        public boolean onClick(User user, ClickType click) {
                            user.sendMessage("Hi!");
                            return false;
                        }
                    })
                    //.setCommand(CHALLENGE_COMMAND + " c " + challenge.getUniqueId())
                    .build();
            plugin.getLogger().info("requested slot" + challenge.getSlot());
            panelBuilder.addItem(challenge.getSlot(),item);
        }
        // Add navigation to other levels
        for (LevelStatus status: manager.getChallengeLevelStatus(user)) {
            String name = ChatColor.GOLD + (status.getLevel().getFriendlyName().isEmpty() ? status.getLevel().getUniqueId() : status.getLevel().getFriendlyName());
            if (status.isComplete() || status.getPreviousLevel() == null) {
                // Clicking on this icon will open up this level's challenges
                PanelItem item = new PanelItemBuilder()
                        .icon(new ItemStack(Material.BOOK_AND_QUILL))
                        .name(name)
                        .description(Arrays.asList(user.getTranslation("challenges.navigation","[level]",name)))
                        .clickHandler(new PanelItem.ClickHandler() {
                           
                            @Override
                            public boolean onClick(User user, ClickType click) {
                                // TODO Auto-generated method stub
                                return false;
                            }
                        })
                        //.setCommand(CHALLENGE_COMMAND + " c " + status.getLevel().getUniqueId())
                        .build();
                panelBuilder.addItem(item);
            } else {
                // Clicking on this icon will do nothing because the challenge is not unlocked yet
                String previousLevelName = ChatColor.GOLD + (status.getPreviousLevel().getFriendlyName().isEmpty() ? status.getPreviousLevel().getUniqueId() : status.getPreviousLevel().getFriendlyName());
                PanelItem item = new PanelItemBuilder()
                        .icon(new ItemStack(Material.BOOK))
                        .name(name)
                        .description(Arrays.asList(user.getTranslation("challenges.toComplete", "[challengesToDo]",String.valueOf(status.getNumberOfChallengesStillToDo()), "[thisLevel]", previousLevelName)))
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
        panel.open(user);
    }

    /*
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
    /*
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
*/
}
