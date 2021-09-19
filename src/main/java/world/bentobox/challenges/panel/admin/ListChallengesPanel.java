package world.bentobox.challenges.panel.admin;


import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.World;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.panel.CommonPagedPanel;
import world.bentobox.challenges.panel.CommonPanel;
import world.bentobox.challenges.panel.ConversationUtils;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.GuiUtils;
import world.bentobox.challenges.utils.Utils;


/**
 * This class contains all necessary elements to create GUI that lists all challenges.
 * It allows to edit them or remove, depending on given input mode.
 */
public class ListChallengesPanel extends CommonPagedPanel
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
    private ListChallengesPanel(ChallengesAddon addon,
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
    private ListChallengesPanel(CommonPanel parentGUI, Mode mode)
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
        new ListChallengesPanel(addon, world, user, mode, topLabel, permissionPrefix).build();
    }


    /**
     * Open the Challenges Admin GUI.
     */
    public static void open(CommonPanel parentGUI, Mode mode)
    {
        new ListChallengesPanel(parentGUI, mode).build();
    }


    // ---------------------------------------------------------------------
    // Section: Methods
    // ---------------------------------------------------------------------


    /**
     * {@inheritDoc}
     */
    @Override
    protected void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
            this.user.getTranslation(Constants.TITLE + "choose-challenge"));

        if (this.currentMode.equals(Mode.DELETE))
        {
            GuiUtils.fillBorder(panelBuilder, Material.RED_STAINED_GLASS_PANE);
        }
        else
        {
            GuiUtils.fillBorder(panelBuilder);
        }

        this.populateElements(panelBuilder,
            this.addon.getChallengesManager().getAllChallenges(this.world),
            o -> this.createChallengeIcon((Challenge) o));

        panelBuilder.item(44, this.returnButton);

        panelBuilder.build();
    }


    /**
     * This method creates button for given challenge.
     * @param challenge Challenge which button must be created.
     * @return Challenge button.
     */
    private PanelItem createChallengeIcon(Challenge challenge)
    {
        PanelItemBuilder itemBuilder = new PanelItemBuilder().
            name(Util.translateColorCodes(challenge.getFriendlyName())).
            description(this.generateChallengeDescription(challenge, null)).
            icon(challenge.getIcon()).
            glow(!challenge.isDeployed());

        if (this.currentMode.equals(Mode.EDIT))
        {
            itemBuilder.description("");
            itemBuilder.description(this.user.getTranslation(Constants.TIPS + "click-to-edit"));

            itemBuilder.clickHandler((panel, user1, clickType, i) -> {
                EditChallengePanel.open(this, challenge);
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
                        this.addon.getChallengesManager().deleteChallenge(challenge);
                    }

                    this.build();
                };

                // Create conversation that gets user acceptance to delete generator data.
                ConversationUtils.createConfirmation(
                    consumer,
                    this.user,
                    this.user.getTranslation(Constants.CONVERSATIONS + "confirm-challenge-deletion",
                        Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world),
                        Constants.PARAMETER_CHALLENGE, challenge.getFriendlyName()),
                    this.user.getTranslation(Constants.CONVERSATIONS + "challenge-removed",
                        Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world),
                        Constants.PARAMETER_CHALLENGE, challenge.getFriendlyName()));
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
