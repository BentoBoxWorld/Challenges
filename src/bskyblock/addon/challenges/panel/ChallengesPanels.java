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
import bskyblock.addon.challenges.database.object.ChallengesData.ChallengeType;
import us.tastybento.bskyblock.api.commands.User;
import us.tastybento.bskyblock.api.panels.ClickType;
import us.tastybento.bskyblock.api.panels.Panel;
import us.tastybento.bskyblock.api.panels.PanelItem;
import us.tastybento.bskyblock.api.panels.builders.PanelBuilder;
import us.tastybento.bskyblock.api.panels.builders.PanelItemBuilder;


public class ChallengesPanels {
    private static final boolean DEBUG = true;
    private Challenges addon;
    private ChallengesManager manager;

    public ChallengesPanels(Challenges plugin, ChallengesManager manager){
        this.addon = plugin;
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
     */
    public void getChallenges(User user, String level) {
        addon.getLogger().info("DEBUG: level requested = " + level);
        PanelBuilder panelBuilder = new PanelBuilder()
                .setName(user.getTranslation("challenges.guiTitle"));

        addChallengeItems(panelBuilder, user, level);
        addFreeChallanges(panelBuilder);
        addNavigation(panelBuilder, user);

        // Create the panel
        addon.getLogger().info("DEBUG: panel created");
        Panel panel = panelBuilder.build();
        panel.open(user);
    }

    private void addFreeChallanges(PanelBuilder panelBuilder) {
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

    }

    private void addChallengeItems(PanelBuilder panelBuilder, User user, String level) {

        List<ChallengesData> levelChallenges = manager.getChallenges(level);
        // Do some checking
        if (DEBUG)
            addon.getLogger().info("DEBUG: Opening level " + level + " with " + levelChallenges.size() + " challenges");

        // Only show a control panel for the level requested.
        for (ChallengesData challenge : levelChallenges) {
            addon.getLogger().info("DEBUG: Adding challenge " + challenge.getUniqueId());
            // Check completion
            boolean completed = manager.isChallengeComplete(user, challenge.getUniqueId());
            addon.getLogger().info("DEBUG: challenge completed = " + completed);
            // If challenge is removed after completion, remove it
            if (completed && challenge.isRemoveWhenCompleted()) {
                addon.getLogger().info("DEBUG: ignored completed");
                continue;
            }
            PanelItem item = new PanelItemBuilder()
                    .icon(challenge.getIcon())
                    .name(challenge.getFriendlyName().isEmpty() ? challenge.getUniqueId() : challenge.getFriendlyName())
                    .description(challenge.getDescription())
                    .glow(completed)
                    .clickHandler(new PanelItem.ClickHandler() {
                        @Override
                        public boolean onClick(User user, ClickType click) {
                            if (!challenge.getChallengeType().equals(ChallengeType.ICON)) {
                                new TryToComplete(addon, user, manager, challenge);
                            }
                            return true;
                        }
                    })
                    .build();
            addon.getLogger().info("requested slot" + challenge.getSlot());
            panelBuilder.addItem(challenge.getSlot(),item);
        }


    }

    private void addNavigation(PanelBuilder panelBuilder, User user) {
        // TODO Auto-generated method stub
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
