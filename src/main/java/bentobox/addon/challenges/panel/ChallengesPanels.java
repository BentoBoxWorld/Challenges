package bentobox.addon.challenges.panel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import bentobox.addon.challenges.ChallengesAddon;
import bentobox.addon.challenges.ChallengesManager;
import bentobox.addon.challenges.LevelStatus;
import bentobox.addon.challenges.commands.ChallengesCommand;
import bentobox.addon.challenges.database.object.Challenges;
import bentobox.addon.challenges.database.object.Challenges.ChallengeType;
import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;


public class ChallengesPanels {
    private ChallengesAddon addon;
    private ChallengesManager manager;
    private User user;
    private String level;
    private World world;
    private String permPrefix;
    private String label;

    public ChallengesPanels(ChallengesAddon addon, User user, String level, World world, String permPrefix, String label) {
        this.addon = addon;
        this.manager = addon.getChallengesManager();
        this.user = user;
        this.world = world;
        this.permPrefix = permPrefix;
        this.label = label;

        if (manager.getChallengeList().isEmpty()) {
            addon.getLogger().severe("There are no challenges set up!");
            user.sendMessage("general.errors.general");
            return;
        }
        if (level.isEmpty()) {
            level = manager.getChallengeList().keySet().iterator().next().getUniqueId();
        }
        this.level = level;
        // Check if level is valid
        if (!manager.isLevelUnlocked(user, level, world)) {
            return;
        }
        PanelBuilder panelBuilder = new PanelBuilder()
                .name(user.getTranslation("challenges.gui-title"));

        addChallengeItems(panelBuilder);
        addNavigation(panelBuilder);
        addFreeChallanges(panelBuilder);

        // Create the panel
        Panel panel = panelBuilder.build();
        panel.open(user);
    }

    private void addChallengeItems(PanelBuilder panelBuilder) {
        Set<Challenges> levelChallenges = manager.getChallenges(level, world);
        // Only show a control panel for the level requested.
        for (Challenges challenge : levelChallenges) {
            createItem(panelBuilder, challenge);
        }
    }

    private void addFreeChallanges(PanelBuilder panelBuilder) {
        manager.getChallenges(ChallengesManager.FREE, world).forEach(challenge -> createItem(panelBuilder, challenge));
    }


    /**
     * Creates a panel item for challenge if appropriate and adds it to panelBuilder
     * @param panelBuilder
     * @param challenge
     * @param user
     */
    private void createItem(PanelBuilder panelBuilder, Challenges challenge) {
        // Check completion
        boolean completed = manager.isChallengeComplete(user, challenge.getUniqueId(), world);
        // If challenge is removed after completion, remove it
        if (completed && challenge.isRemoveWhenCompleted()) {
            return;
        }
        PanelItem item = new PanelItemBuilder()
                .icon(challenge.getIcon())
                .name(challenge.getFriendlyName().isEmpty() ? challenge.getUniqueId() : challenge.getFriendlyName())
                .description(challengeDescription(challenge))
                .glow(completed)
                .clickHandler((panel, player, c, s) -> {
                    if (!challenge.getChallengeType().equals(ChallengeType.ICON)) {
                        new TryToComplete(addon, player, manager, challenge, world, permPrefix, label);
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

    private void addNavigation(PanelBuilder panelBuilder) {
        // Add navigation to other levels
        for (LevelStatus status: manager.getChallengeLevelStatus(user, world)) {
            if (status.getLevel().getUniqueId().equals(level)) {
                // Skip if this is the current level
                continue;
            }
            // Create a nice name for the level
            String name = status.getLevel().getFriendlyName().isEmpty() ? status.getLevel().getUniqueId() : status.getLevel().getFriendlyName();

            if (status.isUnlocked()) {
                // Clicking on this icon will open up this level's challenges
                PanelItem item = new PanelItemBuilder()
                        .icon(new ItemStack(Material.ENCHANTED_BOOK))
                        .name(name)
                        .description(manager.stringSplit(user.getTranslation("challenges.navigation","[level]",name)))
                        .clickHandler((p, u, c, s) -> {
                            u.closeInventory();
                            u.performCommand(label + " " + ChallengesCommand.CHALLENGE_COMMAND + " " + status.getLevel().getUniqueId());
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

    /**
     * Creates the challenge description for the "item" in the inventory
     *
     * @param challenge
     * @param player
     * @return List of strings splitting challenge string into 25 chars long
     */
    private List<String> challengeDescription(Challenges challenge) {
        List<String> result = new ArrayList<String>();
        String level = challenge.getLevel();
        if (!level.isEmpty()) {
            result.addAll(splitTrans(user, "challenges.level", "[level]", level));
        }
        // Check if completed or not

        boolean complete = addon.getChallengesManager().isChallengeComplete(user, challenge.getUniqueId(), world);
        int maxTimes = challenge.getMaxTimes();
        long doneTimes = addon.getChallengesManager().checkChallengeTimes(user, challenge, world);
        if (complete) {
            result.add(user.getTranslation("challenges.complete"));
        }
        if (challenge.isRepeatable()) {
            if (maxTimes == 0) {

                // Check if the player has maxed out the challenge
                if (doneTimes < maxTimes) {
                    result.addAll(splitTrans(user, "challenges.completed-times","[donetimes]", String.valueOf(doneTimes),"[maxtimes]", String.valueOf(maxTimes)));
                } else {
                    result.addAll(splitTrans(user, "challenges.maxed-reached","[donetimes]", String.valueOf(doneTimes),"[maxtimes]", String.valueOf(maxTimes)));
                }
            }
        }
        if (!complete || (complete && challenge.isRepeatable())) {
            result.addAll(challenge.getDescription());
            if (challenge.getChallengeType().equals(ChallengeType.INVENTORY)) {
                if (challenge.isTakeItems()) {
                    result.addAll(splitTrans(user, "challenges.item-take-warning"));
                }
            } else if (challenge.getChallengeType().equals(ChallengeType.ISLAND)) {
                result.addAll(splitTrans(user, "challenges.items-closeby"));
            }
        }
        if (complete && (!challenge.getChallengeType().equals(ChallengeType.INVENTORY) || !challenge.isRepeatable())) {
            result.addAll(splitTrans(user, "challenges.not-repeatable"));
            return result;
        }
        double moneyReward = 0;
        int expReward = 0;
        String rewardText = "";
        if (!complete) {
            // First time
            moneyReward = challenge.getMoneyReward();
            rewardText = challenge.getRewardText();
            expReward = challenge.getExpReward();
            if (!rewardText.isEmpty()) {
                result.addAll(splitTrans(user, "challenges.first-time-rewards"));
            }
        } else {
            // Repeat challenge
            moneyReward = challenge.getRepeatMoneyReward();
            rewardText = challenge.getRepeatRewardText();
            expReward = challenge.getRepeatExpReward();
            if (!rewardText.isEmpty()) {
                result.addAll(splitTrans(user, "challenges.repeat-rewards"));
            }

        }
        if (!rewardText.isEmpty()) {
            result.addAll(splitTrans(user,rewardText));
        }
        if (expReward > 0) {
            result.addAll(splitTrans(user,"challenges.exp-reward", "[reward]", String.valueOf(expReward)));
        }
        if (addon.getPlugin().getSettings().isUseEconomy() && moneyReward > 0) {
            result.addAll(splitTrans(user,"challenges.money-reward", "[reward]", String.valueOf(moneyReward)));
        }
        // Final placeholder change for [label]
        result.replaceAll(x -> x.replace("[label]", label));
        return result;
    }

    private Collection<? extends String> splitTrans(User user, String string, String...strings) {
        return addon.getChallengesManager().stringSplit(user.getTranslation(string, strings));
    }
}
