package bskyblock.addon.challenges.panel;

import java.util.Arrays;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import bskyblock.addon.challenges.ChallengesAddon;
import bskyblock.addon.challenges.ChallengesManager;
import bskyblock.addon.challenges.LevelStatus;
import bskyblock.addon.challenges.database.object.Challenges;
import bskyblock.addon.challenges.database.object.Challenges.ChallengeType;
import us.tastybento.bskyblock.api.panels.ClickType;
import us.tastybento.bskyblock.api.panels.Panel;
import us.tastybento.bskyblock.api.panels.PanelItem;
import us.tastybento.bskyblock.api.panels.builders.PanelBuilder;
import us.tastybento.bskyblock.api.panels.builders.PanelItemBuilder;
import us.tastybento.bskyblock.api.user.User;


public class ChallengesPanels {
    private static final boolean DEBUG = true;
    private ChallengesAddon addon;
    private ChallengesManager manager;

    public ChallengesPanels(ChallengesAddon plugin, ChallengesManager manager){
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
                .name(user.getTranslation("challenges.guiTitle"));

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

        Set<Challenges> levelChallenges = manager.getChallenges(level);
        // Do some checking
        if (DEBUG)
            addon.getLogger().info("DEBUG: Opening level " + level + " with " + levelChallenges.size() + " challenges");

        // Only show a control panel for the level requested.
        for (Challenges challenge : levelChallenges) {
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
            if (challenge.getSlot() >= 0) {
                panelBuilder.item(challenge.getSlot(),item);
            } else {
                panelBuilder.item(item);
            }
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
                panelBuilder.item(item);
            } else {
                // Clicking on this icon will do nothing because the challenge is not unlocked yet
                String previousLevelName = ChatColor.GOLD + (status.getPreviousLevel().getFriendlyName().isEmpty() ? status.getPreviousLevel().getUniqueId() : status.getPreviousLevel().getFriendlyName());
                PanelItem item = new PanelItemBuilder()
                        .icon(new ItemStack(Material.BOOK))
                        .name(name)
                        .description(Arrays.asList(user.getTranslation("challenges.toComplete", "[challengesToDo]",String.valueOf(status.getNumberOfChallengesStillToDo()), "[thisLevel]", previousLevelName)))
                        .build();
                panelBuilder.item(item);
            }
        }
    }

}
