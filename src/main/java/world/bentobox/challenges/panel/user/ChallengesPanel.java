//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.challenges.panel.user;


import org.bukkit.World;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.TemplatedPanel;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.panels.builders.TemplatedPanelBuilder;
import world.bentobox.bentobox.api.panels.reader.ItemTemplateRecord;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.config.SettingsUtils;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.panel.CommonPanel;
import world.bentobox.challenges.tasks.TryToComplete;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.LevelStatus;
import world.bentobox.challenges.utils.Utils;


/**
 * Main challenges panel builder.
 */
public class ChallengesPanel extends CommonPanel
{
    private ChallengesPanel(ChallengesAddon addon,
        World world,
        User user,
        String topLabel,
        String permissionPrefix)
    {
        super(addon, user, world, topLabel, permissionPrefix);
        this.updateLevelList();
        this.containsChallenges = this.manager.hasAnyChallengeData(this.world);
    }


    /**
     * Open the Challenges GUI.
     *
     * @param addon the addon
     * @param world the world
     * @param user the user
     * @param topLabel the top label
     * @param permissionPrefix the permission prefix
     */
    public static void open(ChallengesAddon addon,
        World world,
        User user,
        String topLabel,
        String permissionPrefix)
    {
        new ChallengesPanel(addon, world, user, topLabel, permissionPrefix).build();
    }


    protected void build()
    {
        // Do not open gui if there is no challenges.
        if (!this.containsChallenges)
        {
            this.addon.logError("There are no challenges set up!");
            Utils.sendMessage(this.user, this.world, Constants.ERRORS + "no-challenges");
            return;
        }

        // Create lists for builder.
        this.updateFreeChallengeList();
        this.updateChallengeList();
        // this.updateLevelList();

        // Start building panel.
        TemplatedPanelBuilder panelBuilder = new TemplatedPanelBuilder();

        // Set main template.
        panelBuilder.template("main_panel", new File(this.addon.getDataFolder(), "panels"));
        panelBuilder.user(this.user);
        panelBuilder.world(this.user.getWorld());

        // Register button builders
        panelBuilder.registerTypeBuilder("CHALLENGE", this::createChallengeButton);
        panelBuilder.registerTypeBuilder("LEVEL", this::createLevelButton);

        panelBuilder.registerTypeBuilder("UNASSIGNED_CHALLENGES", this::createFreeChallengesButton);

        panelBuilder.registerTypeBuilder("NEXT", this::createNextButton);
        panelBuilder.registerTypeBuilder("PREVIOUS", this::createPreviousButton);

        // Register unknown type builder.
        panelBuilder.build();
    }


    private void updateFreeChallengeList()
    {
        this.freeChallengeList = this.manager.getFreeChallenges(this.world);

        if (this.addon.getChallengesSettings().isRemoveCompleteOneTimeChallenges())
        {
            this.freeChallengeList.removeIf(challenge -> !challenge.isRepeatable() &&
                this.manager.isChallengeComplete(this.user, this.world, challenge));
        }

        // Remove all undeployed challenges if VisibilityMode is set to Hidden.
        if (this.addon.getChallengesSettings().getVisibilityMode().equals(SettingsUtils.VisibilityMode.HIDDEN))
        {
            this.freeChallengeList.removeIf(challenge -> !challenge.isDeployed());
        }
    }


    private void updateChallengeList()
    {
        if (this.lastSelectedLevel != null)
        {
            this.challengeList = this.manager.getLevelChallenges(this.lastSelectedLevel.getLevel(), true);

            if (this.addon.getChallengesSettings().isRemoveCompleteOneTimeChallenges())
            {
                this.challengeList.removeIf(challenge -> !challenge.isRepeatable() &&
                    this.manager.isChallengeComplete(this.user, this.world, challenge));
            }

            // Remove all undeployed challenges if VisibilityMode is set to Hidden.
            if (this.addon.getChallengesSettings().getVisibilityMode().equals(SettingsUtils.VisibilityMode.HIDDEN))
            {
                this.challengeList.removeIf(challenge -> !challenge.isDeployed());
            }
        }
        else
        {
            this.challengeList = this.freeChallengeList;
        }
    }


    /**
     * Updates level status list and selects last unlocked level.
     */
    private void updateLevelList()
    {
        this.levelList = this.manager.getAllChallengeLevelStatus(this.user, this.world);

        for (LevelStatus levelStatus : this.levelList)
        {
            if (levelStatus.isUnlocked())
            {
                this.lastSelectedLevel = levelStatus;
            }
            else
            {
                break;
            }
        }
    }


    /**
     * Updates level status list and returns if any new level has been unlocked.
     * @return {code true} if a new level was unlocked, {@code false} otherwise.
     */
    private boolean updateLevelListSilent()
    {
        Optional<LevelStatus> firstLockedLevel =
            this.levelList.stream().filter(levelStatus -> !levelStatus.isUnlocked()).findFirst();

        if (firstLockedLevel.isPresent())
        {
            // If there still exist any locked level, update level status list.
            this.levelList = this.manager.getAllChallengeLevelStatus(this.user, this.world);

            // Find a new first locked level.
            Optional<LevelStatus> newLockedLevel =
                this.levelList.stream().filter(levelStatus -> !levelStatus.isUnlocked()).findFirst();

            return newLockedLevel.isEmpty() ||
                firstLockedLevel.get().getLevel() != newLockedLevel.get().getLevel();
        }
        else
        {
            // If locked level is not present, return false.
            return false;
        }
    }


    @Nullable
    private PanelItem createChallengeButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        if (this.challengeList.isEmpty())
        {
            // Does not contain any free challenges.
            return null;
        }

        Challenge levelChallenge;

        // Check if that is a specific free challenge
        if (template.dataMap().containsKey("id"))
        {
            String id = (String) template.dataMap().get("id");

            // Find a challenge with given Id;
            levelChallenge = this.challengeList.stream().
                filter(challenge -> challenge.getUniqueId().equals(id)).
                findFirst().
                orElse(null);

            if (levelChallenge == null)
            {
                // There is no challenge in the list with specific id.
                return null;
            }
        }
        else
        {
            int index = this.challengeIndex * slot.amountMap().getOrDefault("CHALLENGE", 1) + slot.slot();

            if (index >= this.challengeList.size())
            {
                // Out of index.
                return null;
            }

            levelChallenge = this.challengeList.get(index);
        }

        return this.createChallengeButton(template, levelChallenge);
    }


    @NonNull
    private PanelItem createChallengeButton(ItemTemplateRecord template, @NonNull Challenge challenge)
    {
        PanelItemBuilder builder = new PanelItemBuilder();

        // Template specification are always more important than dynamic content.
        builder.icon(template.icon() != null ? template.icon().clone() : challenge.getIcon());

        // Template specific title is always more important than challenge name.
        if (template.title() != null && !template.title().isBlank())
        {
            builder.name(this.user.getTranslation(this.world, template.title(),
                Constants.PARAMETER_CHALLENGE, challenge.getFriendlyName()));
        }
        else
        {
            builder.name(Util.translateColorCodes(challenge.getFriendlyName()));
        }

        if (template.description() != null && !template.description().isBlank())
        {
            // TODO: adding parameters could be useful.
            builder.description(this.user.getTranslation(this.world, template.description()));
        }
        else
        {
            builder.description(this.generateChallengeDescription(challenge, this.user));
        }

        // If challenge is not repeatable, remove all other actions beside "COMPLETE".
        // If challenge is completed all possible times, remove action.

        List<ItemTemplateRecord.ActionRecords> actions = template.actions().stream().
            filter(action -> challenge.isRepeatable() || "COMPLETE".equalsIgnoreCase(action.actionType())).
            filter(action ->
            {
                boolean isCompletedOnce =
                    this.manager.isChallengeComplete(this.user.getUniqueId(), this.world, challenge);

                if (!isCompletedOnce)
                {
                    // Is not completed once, then it must appear.
                    return true;
                }
                else if (challenge.isRepeatable() && challenge.getMaxTimes() <= 0)
                {
                    // Challenge is unlimited. Must appear in the list.
                    return true;
                }
                else
                {
                    // Challenge still have some opened slots.

                    long doneTimes = challenge.isRepeatable() ?
                        this.manager.getChallengeTimes(this.user, this.world, challenge) : 1;

                    return challenge.isRepeatable() && doneTimes < challenge.getMaxTimes();
                }
            }).
            toList();

        // Add Click handler
        builder.clickHandler((panel, user, clickType, i) -> {
            for (ItemTemplateRecord.ActionRecords action : actions)
            {
                if (clickType == action.clickType() || clickType.equals(ClickType.UNKNOWN))
                {
                    switch (action.actionType().toUpperCase())
                    {
                        case "COMPLETE":
                            if (TryToComplete.complete(this.addon,
                                this.user,
                                challenge,
                                this.world,
                                this.topLabel,
                                this.permissionPrefix))
                            {
                                if (this.updateLevelListSilent())
                                {
                                    // Need to rebuild all because completing a challenge
                                    // may unlock a new level. #187
                                    this.build();
                                }
                                else
                                {
                                    // There was no unlocked levels.
                                    panel.getInventory().setItem(i,
                                        this.createChallengeButton(template, challenge).getItem());
                                }
                            }
                            else if (challenge.isRepeatable() && challenge.getTimeout() > 0)
                            {
                                // Update timeout after clicking.
                                panel.getInventory().setItem(i,
                                    this.createChallengeButton(template, challenge).getItem());
                            }
                            break;
                        case "COMPLETE_MAX":
                            if (challenge.isRepeatable())
                            {
                                if (TryToComplete.complete(this.addon,
                                    this.user,
                                    challenge,
                                    this.world,
                                    this.topLabel,
                                    this.permissionPrefix,
                                    Integer.MAX_VALUE))
                                {
                                    if (this.updateLevelListSilent())
                                    {
                                        // Need to rebuild all because completing a challenge
                                        // may unlock a new level. #187
                                        this.build();
                                    }
                                    else
                                    {
                                        // There was no unlocked levels.
                                        panel.getInventory().setItem(i,
                                            this.createChallengeButton(template, challenge).getItem());
                                    }
                                }
                                else if (challenge.getTimeout() > 0)
                                {
                                    // Update timeout after clicking.
                                    panel.getInventory().setItem(i,
                                        this.createChallengeButton(template, challenge).getItem());
                                }
                            }
                            break;
                        case "MULTIPLE_PANEL":
                            if (challenge.isRepeatable())
                            {
                                MultiplePanel.open(this.addon, this.user, value ->
                                {
                                    TryToComplete.complete(this.addon,
                                        this.user,
                                        challenge,
                                        this.world,
                                        this.topLabel,
                                        this.permissionPrefix,
                                        value);

                                    this.updateLevelListSilent();
                                    this.build();
                                });
                            }
                            break;
                    }
                }
            }

            return true;
        });

        // Collect tooltips.
        List<String> tooltips = actions.stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(this.world, action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }

        // Glow the icon.
        builder.glow(this.addon.getChallengesSettings().isAddCompletedGlow() &&
            this.manager.isChallengeComplete(this.user, this.world, challenge));

        // Click Handlers are managed by custom addon buttons.
        return builder.build();
    }


    @Nullable
    private PanelItem createLevelButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        if (this.levelList.isEmpty())
        {
            // Does not contain any levels.
            return null;
        }

        LevelStatus level;

        // Check if that is a specific level
        if (template.dataMap().containsKey("id"))
        {
            String id = (String) template.dataMap().get("id");

            // Find a challenge with given Id;
            level = this.levelList.stream().
                filter(levelStatus -> levelStatus.getLevel().getUniqueId().equals(id)).
                findFirst().
                orElse(null);

            if (level == null)
            {
                // There is no challenge in the list with specific id.
                return null;
            }
        }
        else
        {
            int index = this.levelIndex * slot.amountMap().getOrDefault("LEVEL", 1) + slot.slot();

            if (index >= this.levelList.size())
            {
                // Out of index.
                return null;
            }

            level = this.levelList.get(index);
        }

        return this.createLevelButton(template, level);
    }


    @NonNull
    private PanelItem createLevelButton(ItemTemplateRecord template, @NonNull LevelStatus level)
    {
        PanelItemBuilder builder = new PanelItemBuilder();

        // Template specification are always more important than dynamic content.
        if (template.icon() != null)
        {
            builder.icon(template.icon().clone());
        }
        else
        {
            if (level.isUnlocked())
            {
                builder.icon(level.getLevel().getIcon());
            }
            else if (level.getLevel().getLockedIcon() != null)
            {
                // Clone will prevent issues with description storing.
                // It can be done only here as it can be null.
                builder.icon(level.getLevel().getLockedIcon().clone());
            }
            else
            {
                builder.icon(this.addon.getChallengesSettings().getLockedLevelIcon());
            }
        }

        if (template.title() != null && !template.title().isBlank())
        {
            builder.name(this.user.getTranslation(this.world, template.title(),
                Constants.PARAMETER_LEVEL, level.getLevel().getFriendlyName()));
        }
        else
        {
            builder.name(Util.translateColorCodes(level.getLevel().getFriendlyName()));
        }

        if (template.description() != null && !template.description().isBlank())
        {
            // TODO: adding parameters could be useful.
            builder.description(this.user.getTranslation(this.world, template.description()));
        }
        else
        {
            // TODO: Complete description generate.
            builder.description(this.generateLevelDescription(level, this.user));
        }

        // Add click handler
        builder.clickHandler((panel, user, clickType, i) -> {
            if (level != this.lastSelectedLevel && level.isUnlocked())
            {
                this.lastSelectedLevel = level;
                this.challengeIndex = 0;

                this.build();
            }

            return true;
        });

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            filter(action -> level != this.lastSelectedLevel && level.isUnlocked()).
            map(action -> this.user.getTranslation(this.world, action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }

        // Glow the icon.
        builder.glow(level == this.lastSelectedLevel ||
            level.isUnlocked() &&
                this.addon.getChallengesSettings().isAddCompletedGlow() &&
                this.manager.isLevelCompleted(this.user, this.world, level.getLevel()));

        // Click Handlers are managed by custom addon buttons.
        return builder.build();
    }


    @Nullable
    private PanelItem createFreeChallengesButton(@NonNull ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        if (this.freeChallengeList.isEmpty())
        {
            // There are no free challenges for selection.
            return null;
        }

        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            builder.icon(template.icon().clone());
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(this.world, template.description()));
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            if (this.lastSelectedLevel != null)
            {
                this.lastSelectedLevel = null;
                this.build();
            }

            // Always return true.
            return true;
        });

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            filter(action -> this.lastSelectedLevel == null).
            map(action -> this.user.getTranslation(this.world, action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }

        return builder.build();
    }


    @Nullable
    private PanelItem createNextButton(@NonNull ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        String target = template.dataMap().getOrDefault("target", "").toString().toUpperCase();

        int nextPageIndex;

        switch (target)
        {
            case "CHALLENGE" -> {
                int size = this.challengeList.size();

                if (size <= slot.amountMap().getOrDefault("CHALLENGE", 1) ||
                    1.0 * size / slot.amountMap().getOrDefault("CHALLENGE", 1) <= this.challengeIndex + 1)
                {
                    // There are no next elements
                    return null;
                }

                nextPageIndex = this.challengeIndex + 2;
            }
            case "LEVEL" -> {
                int size = this.levelList.size();

                if (size <= slot.amountMap().getOrDefault("LEVEL", 1) ||
                    1.0 * size / slot.amountMap().getOrDefault("LEVEL", 1) <= this.levelIndex + 1)
                {
                    // There are no next elements
                    return null;
                }

                nextPageIndex = this.levelIndex + 2;
            }
            default -> {
                // If not assigned to any type, return null.
                return null;
            }
        }

        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            ItemStack clone = template.icon().clone();

            if ((Boolean) template.dataMap().getOrDefault("indexing", false))
            {
                clone.setAmount(nextPageIndex);
            }

            builder.icon(clone);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(this.world, template.description(),
                Constants.PARAMETER_NUMBER, String.valueOf(nextPageIndex)));
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            // Next button ignores click type currently.
            switch (target)
            {
                case "CHALLENGE" -> this.challengeIndex++;
                case "LEVEL" -> this.levelIndex++;
            }

            this.build();

            // Always return true.
            return true;
        });

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(this.world, action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }

        return builder.build();
    }


    @Nullable
    private PanelItem createPreviousButton(@NonNull ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        String target = template.dataMap().getOrDefault("target", "").toString().toUpperCase();

        int previousPageIndex;

        if ("CHALLENGE".equals(target))
        {
            if (this.challengeIndex == 0)
            {
                // There are no next elements
                return null;
            }

            previousPageIndex = this.challengeIndex;
        }
        else if ("LEVEL".equals(target))
        {
            if (this.levelIndex == 0)
            {
                // There are no next elements
                return null;
            }

            previousPageIndex = this.levelIndex;
        }
        else
        {
            // If not assigned to any type, return null.
            return null;
        }

        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            ItemStack clone = template.icon().clone();

            if ((Boolean) template.dataMap().getOrDefault("indexing", false))
            {
                clone.setAmount(previousPageIndex);
            }

            builder.icon(clone);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(this.world, template.description(),
                Constants.PARAMETER_NUMBER, String.valueOf(previousPageIndex)));
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            // Next button ignores click type currently.
            switch (target)
            {
                case "CHALLENGE" -> this.challengeIndex--;
                case "LEVEL" -> this.levelIndex--;
            }

            this.build();

            // Always return true.
            return true;
        });

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(this.world, action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }

        return builder.build();
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * This boolean indicates if in the world there exist challenges for displaying in GUI.
     */
    private final boolean containsChallenges;

    /**
     * This list contains free challenges in current Panel.
     */
    private List<Challenge> freeChallengeList;

    /**
     * This will be used if levels are more than 18.
     */
    private int levelIndex;

    /**
     * This list contains all information about level completion in current world.
     */
    private List<LevelStatus> levelList;

    /**
     * This will be used if free challenges are more than 18.
     */
    private int challengeIndex;

    /**
     * This list contains challenges in current Panel.
     */
    private List<Challenge> challengeList;

    /**
     * This indicates last selected level.
     */
    private LevelStatus lastSelectedLevel;
}
