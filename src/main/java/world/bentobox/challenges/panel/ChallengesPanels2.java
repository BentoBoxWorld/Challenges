package world.bentobox.challenges.panel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.ChallengesManager;
import world.bentobox.challenges.LevelStatus;
import world.bentobox.challenges.commands.ChallengesCommand;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.Challenge.ChallengeType;
import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;


/**
 * @deprecated All panels are reworked.
 */
@Deprecated
public class ChallengesPanels2 {

    public enum Mode {
        ADMIN,
        EDIT,
        PLAYER
    }
    private ChallengesAddon addon;
    private ChallengesManager manager;
    private User requester;
    private String level;
    private World world;
    private String permPrefix;
    private String label;
    private Mode mode;
    private User target;

    public ChallengesPanels2(ChallengesAddon addon, User requester, User target, String level, World world, String permPrefix, String label, Mode mode) {
        this.addon = addon;
        this.manager = addon.getChallengesManager();
        this.requester = requester;
        this.target = target;
        this.world = world;
        this.permPrefix = permPrefix;
        this.label = label;
        this.mode = mode;

        if (manager.getChallengeList().isEmpty()) {
            addon.getLogger().severe("There are no challenges set up!");
            requester.sendMessage("general.errors.general");
            return;
        }
        if (level.isEmpty()) {
            // TODO: open the farthest challenge panel
            level = manager.getChallengeList().keySet().iterator().next().getUniqueId();
        }
        this.level = level;
        // Check if level is valid
        if (mode.equals(Mode.PLAYER) && !manager.isLevelUnlocked(requester, level, world)) {
            return;
        }
        PanelBuilder panelBuilder = new PanelBuilder();
        switch (mode) {
        case ADMIN:
            panelBuilder.name(requester.getTranslation("challenges.admin.gui-title"));
            break;
        case EDIT:
            panelBuilder.name(requester.getTranslation("challenges.admin.edit-gui-title"));
            break;
        case PLAYER:
            panelBuilder.name(requester.getTranslation("challenges.gui-title"));
            break;
        default:
            break;

        }

        addChallengeItems(panelBuilder);
        addNavigation(panelBuilder);
        addFreeChallanges(panelBuilder);

        // Create the panel
        Panel panel = panelBuilder.build();
        panel.open(requester);
    }

    private void addChallengeItems(PanelBuilder panelBuilder) {
        // Only show a control panel for the level requested.
        for (Challenge challenge : manager.getChallenges(level, world)) {
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
     * @param requester
     */
    private void createItem(PanelBuilder panelBuilder, Challenge challenge) {
        // For admin, glow means activated. For user, glow means done
        boolean glow = false;
        switch (mode) {
        case ADMIN:
            glow = challenge.isDeployed();
            break;
        case EDIT:
            glow = manager.isChallengeComplete(requester, challenge.getUniqueId(), world);
            break;
        case PLAYER:
            glow = manager.isChallengeComplete(requester, challenge.getUniqueId(), world);
            break;
        default:
            break;

        }
        // If not admin and challenge is removed after completion, remove it
        if (mode.equals(Mode.PLAYER) && glow && challenge.isRemoveWhenCompleted()) {
            return;
        }
        PanelItemBuilder itemBuilder = new PanelItemBuilder()
                .icon(challenge.getIcon())
                .name(challenge.getFriendlyName().isEmpty() ? challenge.getUniqueId() : challenge.getFriendlyName())
                .description(challengeDescription(challenge))
                .glow(glow);
        if (mode.equals(Mode.ADMIN)) {
            // Admin click
            itemBuilder.clickHandler((panel, player, c, s) -> {
                if (!challenge.getChallengeType().equals(ChallengeType.ICON)) {
                    new AdminGUI(addon, player, challenge, world, permPrefix, label);
                }
                return true;
            });

        } else if (mode.equals(Mode.EDIT)) {
            // Admin edit click
            itemBuilder.clickHandler((panel, player, c, s) -> {
                if (!challenge.getChallengeType().equals(ChallengeType.ICON)) {
                    new AdminEditGUI(addon, player, target, challenge, world, permPrefix, label);
                }
                return true;
            });
        } else {
            // Player click
            itemBuilder.clickHandler((panel, player, c, s) -> {
                if (!challenge.getChallengeType().equals(ChallengeType.ICON)) {
                    new TryToComplete(addon, player, manager, challenge, world, permPrefix, label);
                }
                return true;
            });
        }

        // If the challenge has a specific slot allocated, use it
        if (challenge.getSlot() >= 0) {
            panelBuilder.item(challenge.getSlot(),itemBuilder.build());
        } else {
            panelBuilder.item(itemBuilder.build());
        }
    }

    private void addNavigation(PanelBuilder panelBuilder) {
        // TODO: This if fix for wrong getNumberOfChallengesStillToDo() issue. #23
        LevelStatus previousStatus = null;

        // Add navigation to other levels
        for (LevelStatus status: manager.getChallengeLevelStatus(requester, world)) {
            if (status.getLevel().getUniqueId().equalsIgnoreCase(level)) {
                // Skip if this is the current level
                previousStatus = status;
                continue;
            }
            // Create a nice name for the level
            String name = status.getLevel().getFriendlyName().isEmpty() ? status.getLevel().getUniqueId() : status.getLevel().getFriendlyName();

            if (mode.equals(Mode.ADMIN) || mode.equals(Mode.EDIT) || status.isUnlocked()) {
                // Clicking on this icon will open up this level's challenges
                PanelItem item = new PanelItemBuilder()
                        .icon(new ItemStack(Material.ENCHANTED_BOOK))
                        .name(name)
                        .description(manager.stringSplit(requester.getTranslation("challenges.navigation","[level]",name)))
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
                        .description(manager.stringSplit(requester.getTranslation("challenges.to-complete", "[challengesToDo]",String.valueOf(previousStatus != null ? previousStatus.getNumberOfChallengesStillToDo() : ""), "[thisLevel]", previousLevelName)))
                        .build();
                panelBuilder.item(item);
            }

            previousStatus = status;
        }
    }

    /**
     * Creates the challenge description for the "item" in the inventory
     *
     * @param challenge
     * @param player
     * @return List of strings splitting challenge string into 25 chars long
     */
    private List<String> challengeDescription(Challenge challenge) {
        List<String> result = new ArrayList<String>();
        String level = challenge.getLevel();
        if (!level.isEmpty()) {
            result.addAll(splitTrans(requester, "challenges.level", "[level]", level));
        }

        if (mode.equals(Mode.ADMIN)) {
            if ((!challenge.getChallengeType().equals(ChallengeType.INVENTORY) || !challenge.isRepeatable())) {
                result.addAll(splitTrans(requester, "challenges.not-repeatable"));
            } else {
                result.addAll(splitTrans(requester, "challenges.repeatable", "[maxtimes]", String.valueOf(challenge.getMaxTimes())));
            }
            if (challenge.getChallengeType().equals(ChallengeType.INVENTORY) && challenge.isTakeItems()) {
                result.addAll(splitTrans(requester, "challenges.item-take-warning"));
            }
            result.addAll(addRewards(challenge, true, true));
        } else {
            // Check if completed or not
            boolean complete = addon.getChallengesManager().isChallengeComplete(requester, challenge.getUniqueId(), world);
            int maxTimes = challenge.getMaxTimes();
            long doneTimes = addon.getChallengesManager().checkChallengeTimes(requester, challenge, world);
            if (complete) {
                result.add(requester.getTranslation("challenges.complete"));
            }

            if (challenge.isRepeatable()) {
                if (maxTimes == 0) {

                    // Check if the player has maxed out the challenge
                    if (doneTimes < maxTimes) {
                        result.addAll(splitTrans(requester, "challenges.completed-times","[donetimes]", String.valueOf(doneTimes),"[maxtimes]", String.valueOf(maxTimes)));
                    } else {
                        result.addAll(splitTrans(requester, "challenges.maxed-reached","[donetimes]", String.valueOf(doneTimes),"[maxtimes]", String.valueOf(maxTimes)));
                    }
                }
            }
            if (!complete || (complete && challenge.isRepeatable())) {
                result.addAll(challenge.getDescription());
                if (challenge.getChallengeType().equals(ChallengeType.INVENTORY)) {
                    if (challenge.isTakeItems()) {
                        result.addAll(splitTrans(requester, "challenges.item-take-warning"));
                    }
                } else if (challenge.getChallengeType().equals(ChallengeType.ISLAND)) {
                    result.addAll(splitTrans(requester, "challenges.items-closeby"));
                }
            }
            if (complete && (!challenge.getChallengeType().equals(ChallengeType.INVENTORY) || !challenge.isRepeatable())) {
                result.addAll(splitTrans(requester, "challenges.not-repeatable"));
                result.replaceAll(x -> x.replace("[label]", label));
                return result;
            }
            result.addAll(addRewards(challenge, complete, false));
        }
        // Final placeholder change for [label]
        result.replaceAll(x -> x.replace("[label]", label));
        return result;
    }

    private List<String> addRewards(Challenge challenge, boolean complete, boolean admin) {
        List<String> result = new ArrayList<>();
        double moneyReward = 0;
        int expReward = 0;
        String rewardText = "";
        if (admin || !complete) {
            // First time
            moneyReward = challenge.getRewardMoney();
            rewardText = challenge.getRewardText();
            expReward = challenge.getRewardExp();
            if (!rewardText.isEmpty()) {
                result.addAll(splitTrans(requester, "challenges.first-time-rewards"));
            }
        }
        if (admin || complete){
            // Repeat challenge
            moneyReward = challenge.getRepeatMoneyReward();
            rewardText = challenge.getRepeatRewardText();
            expReward = challenge.getRepeatExpReward();
            if (!rewardText.isEmpty()) {
                result.addAll(splitTrans(requester, "challenges.repeat-rewards"));
            }
        }

        if (!rewardText.isEmpty()) {
            result.addAll(splitTrans(requester,rewardText));
        }
        if (expReward > 0) {
            result.addAll(splitTrans(requester,"challenges.exp-reward", "[reward]", String.valueOf(expReward)));
        }
        if (addon.getPlugin().getSettings().isUseEconomy() && moneyReward > 0) {
            result.addAll(splitTrans(requester,"challenges.money-reward", "[reward]", String.valueOf(moneyReward)));
        }
        return result;
    }

    private Collection<? extends String> splitTrans(User user, String string, String...strings) {
        return addon.getChallengesManager().stringSplit(user.getTranslation(string, strings));
    }
}
