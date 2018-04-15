package bskyblock.addon.challenges.panel;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import bskyblock.addon.challenges.ChallengesAddon;
import bskyblock.addon.challenges.ChallengesManager;
import bskyblock.addon.challenges.LevelStatus;
import bskyblock.addon.challenges.commands.ChallengesCommand;
import bskyblock.addon.challenges.database.object.Challenges;
import bskyblock.addon.challenges.database.object.Challenges.ChallengeType;
import us.tastybento.bskyblock.api.panels.Panel;
import us.tastybento.bskyblock.api.panels.PanelItem;
import us.tastybento.bskyblock.api.panels.builders.PanelBuilder;
import us.tastybento.bskyblock.api.panels.builders.PanelItemBuilder;
import us.tastybento.bskyblock.api.user.User;


public class ChallengesPanels {
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
        // Get the challenge level this player is on
        getChallenges(user, "");
    }

    /**
     * Dynamically creates an inventory of challenges for the player showing the
     * level
     */
    public void getChallenges(User user, String level) {
        if (manager.getChallengeList().isEmpty()) {
            addon.getLogger().severe("There are no challenges set up!");
            user.sendMessage("general.errors.general");
            return;
        }
        if (level.isEmpty()) {
            level = manager.getChallengeList().keySet().iterator().next().getUniqueId(); 
        }
        // Check if level is valid
        if (!manager.isLevelUnlocked(user, level)) {
            return;
        }
        PanelBuilder panelBuilder = new PanelBuilder()
                .name(user.getTranslation("challenges.gui-title"));

        addChallengeItems(panelBuilder, user, level);
        addNavigation(panelBuilder, user, level);
        addFreeChallanges(panelBuilder, user);

        // Create the panel
        Panel panel = panelBuilder.build();
        panel.open(user);
    }

    private void addFreeChallanges(PanelBuilder panelBuilder, User user) {
        manager.getChallenges(ChallengesManager.FREE).forEach(challenge -> createItem(panelBuilder, challenge, user));
    }


    /**
     * Creates a panel item for challenge if appropriate and adds it to panelBuilder
     * @param panelBuilder
     * @param challenge
     * @param user
     */
    private void createItem(PanelBuilder panelBuilder, Challenges challenge, User user) {
        // Check completion
        boolean completed = manager.isChallengeComplete(user, challenge.getUniqueId());
        // If challenge is removed after completion, remove it
        if (completed && challenge.isRemoveWhenCompleted()) {
            return;
        }
        PanelItem item = new PanelItemBuilder()
                .icon(challenge.getIcon())
                .name(challenge.getFriendlyName().isEmpty() ? challenge.getUniqueId() : challenge.getFriendlyName())
                .description(challenge.getDescription())
                .glow(completed)
                .clickHandler((player,c) -> {
                    if (!challenge.getChallengeType().equals(ChallengeType.ICON)) {
                        new TryToComplete(addon, player, manager, challenge);
                    }
                    return true;
                })
                .build();
        if (challenge.getSlot() >= 0) {
            panelBuilder.item(challenge.getSlot(),item);
        } else {
            panelBuilder.item(item);
        }
    }

    private void addChallengeItems(PanelBuilder panelBuilder, User user, String level) {
        Set<Challenges> levelChallenges = manager.getChallenges(level);
        // Only show a control panel for the level requested.
        for (Challenges challenge : levelChallenges) {
            createItem(panelBuilder, challenge, user);
        }
    }

    private void addNavigation(PanelBuilder panelBuilder, User user, String level) {
        // Add navigation to other levels
        for (LevelStatus status: manager.getChallengeLevelStatus(user)) {
            if (status.getLevel().getUniqueId().equals(level)) {
                // Skip if this is the current level
                continue;
            }
            // Create a nice name for the level
            String name = status.getLevel().getFriendlyName().isEmpty() ? status.getLevel().getUniqueId() : status.getLevel().getFriendlyName();

            if (status.isUnlocked()) {
                // Clicking on this icon will open up this level's challenges
                PanelItem item = new PanelItemBuilder()
                        .icon(new ItemStack(Material.BOOK_AND_QUILL))
                        .name(name)
                        .description(manager.stringSplit(user.getTranslation("challenges.navigation","[level]",name)))
                        .clickHandler((u, c) -> {
                            u.closeInventory();
                            u.performCommand(ChallengesCommand.CHALLENGE_COMMAND + " " + status.getLevel().getUniqueId());
                            return true;
                        })
                        .build();
                panelBuilder.item(item);
            } else {
                // Clicking on this icon will do nothing because the challenge is not unlocked yet
                String previousLevelName = status.getPreviousLevel().getFriendlyName().isEmpty() ? status.getPreviousLevel().getUniqueId() : status.getPreviousLevel().getFriendlyName();
                PanelItem item = new PanelItemBuilder()
                        .icon(new ItemStack(Material.BOOK))
                        .name(name)
                        .description(manager.stringSplit(user.getTranslation("challenges.to-complete", "[challengesToDo]",String.valueOf(status.getNumberOfChallengesStillToDo()), "[thisLevel]", previousLevelName)))
                        .build();
                panelBuilder.item(item);
            }
        }
    }

}
