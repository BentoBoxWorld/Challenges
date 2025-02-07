package world.bentobox.challenges.panel.admin;


import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.World;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.panel.CommonPagedPanel;
import world.bentobox.challenges.panel.CommonPanel;
import world.bentobox.challenges.panel.ConversationUtils;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;


/**
 * This class creates GUI that lists all Levels. Clicking on Level icon will be processed
 * by input mode.
 */
public class ListLevelsPanel extends CommonPagedPanel<ChallengeLevel>
{
    // ---------------------------------------------------------------------
    // Section: Constructor
    // ---------------------------------------------------------------------


    /**
     * @param addon Addon where panel operates.
     * @param world World from which panel was created.
     * @param user User who created panel.
     * @param topLabel Command top label which creates panel (f.e. island or ai)
     * @param permissionPrefix Command permission prefix (f.e. bskyblock.)
     * @param mode - mode that indicate what should do icon clicking.
     */
    private ListLevelsPanel(ChallengesAddon addon,
            World world,
            User user,
            Mode mode,
            String topLabel,
            String permissionPrefix)
    {
        super(addon, user, world, topLabel, permissionPrefix);
        this.currentMode = mode;
    }


    /**
     * @param mode - mode that indicate what should do icon clicking.
     */
    private ListLevelsPanel(CommonPanel parentGUI, Mode mode)
    {
        super(parentGUI);
        this.currentMode = mode;
    }


    /**
     * Open the Challenges Admin GUI.
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
        String permissionPrefix,
        Mode mode)
    {
        new ListLevelsPanel(addon, world, user, mode, topLabel, permissionPrefix).build();
    }


    /**
     * Open the Challenges Admin GUI.
     */
    public static void open(CommonPanel parentGUI, Mode mode)
    {
        new ListLevelsPanel(parentGUI, mode).build();
    }


    // ---------------------------------------------------------------------
    // Section: Methods
    // ---------------------------------------------------------------------


    /**
     * This method is called when filter value is updated.
     */
    @Override
    protected void updateFilters()
    {
        // Do nothing here.
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
            this.user.getTranslation(Constants.TITLE + "choose-level"));

        if (this.currentMode.equals(Mode.DELETE))
        {
            PanelUtils.fillBorder(panelBuilder, Material.RED_STAINED_GLASS_PANE);
        }
        else
        {
            PanelUtils.fillBorder(panelBuilder);
        }

        List<ChallengeLevel> levelList = this.addon.getChallengesManager().getLevels(this.world).
            stream().
            filter(challenge -> this.searchString.isBlank() ||
                challenge.getFriendlyName().toLowerCase().contains(this.searchString.toLowerCase()) ||
                challenge.getUniqueId().toLowerCase().contains(this.searchString.toLowerCase())).
            collect(Collectors.toList());

        this.populateElements(panelBuilder, levelList);

        panelBuilder.item(44, this.returnButton);

        panelBuilder.build();
    }


    /**
     * This method creates button for given level
     * @param challengeLevel Level which button must be created.
     * @return Level button.
     */
    @Override
    protected PanelItem createElementButton(ChallengeLevel challengeLevel)
    {
        PanelItemBuilder itemBuilder = new PanelItemBuilder().
            name(Util.translateColorCodes(challengeLevel.getFriendlyName())).
            description(this.generateLevelDescription(challengeLevel)).
            icon(challengeLevel.getIcon());

        if (this.currentMode.equals(Mode.EDIT))
        {
            itemBuilder.description("");
            itemBuilder.description(this.user.getTranslation(Constants.TIPS + "click-to-edit"));

            itemBuilder.clickHandler((panel, user1, clickType, i) -> {
                EditLevelPanel.open(this, challengeLevel);
                return true;
            });
        }
        else if (this.currentMode.equals(Mode.DELETE))
        {
            itemBuilder.description("");
            itemBuilder.description(this.user.getTranslation(Constants.TIPS + "click-to-remove"));

            itemBuilder.clickHandler((panel, user1, clickType, i) -> {
                Consumer<Boolean> consumer = value -> {
                    if (value)
                    {
                        this.addon.getChallengesManager().deleteChallengeLevel(challengeLevel);
                    }

                    this.build();
                };

                // Create conversation that gets user acceptance to delete generator data.
                ConversationUtils.createConfirmation(
                    consumer,
                    this.user,
                    this.user.getTranslation(Constants.CONVERSATIONS + "confirm-level-deletion",
                        Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world),
                        Constants.PARAMETER_LEVEL, challengeLevel.getFriendlyName()),
                    this.user.getTranslation(Constants.CONVERSATIONS + "level-removed",
                        Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world),
                        Constants.PARAMETER_LEVEL, challengeLevel.getFriendlyName()));
                return true;
            });
        }

        return itemBuilder.build();
    }


    // ---------------------------------------------------------------------
    // Section: Enums
    // ---------------------------------------------------------------------


    /**
     * Mode in which gui icons should processed.
     */
    public enum Mode
    {
        EDIT,
        DELETE
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * Current mode in which icons will act.
     */
    private final Mode currentMode;
}
