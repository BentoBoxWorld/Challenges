package world.bentobox.challenges.panel;


import org.bukkit.Material;
import org.bukkit.World;

import java.util.List;
import java.util.Optional;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.utils.GuiUtils;


/**
 * This class process GameModeGui opening.
 */
public class GameModesGUI extends CommonGUI
{
    /**
     * @param adminMode - boolean that indicate if Gui is in admin mode.
     * @param gameModeAddons - List with GameModes where Challenges addon is integrated.
     * @param addon Addon where panel operates.
     * @param world World from which panel was created.
     * @param user User who created panel.
     * @param topLabel Command top label which creates panel (f.e. island or ai)
     * @param permissionPrefix Command permission prefix (f.e. bskyblock.)

     */
    public GameModesGUI(ChallengesAddon addon,
            World world,
            User user,
            String topLabel,
            String permissionPrefix,
            boolean adminMode,
            List<GameModeAddon> gameModeAddons)
    {
        super(addon, world, user, topLabel, permissionPrefix);
        this.adminMode = adminMode;
        this.gameModeAddons = gameModeAddons;
    }


    @Override
    public void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user).
                name("challenges.gui.title.game-modes");

        GuiUtils.fillBorder(panelBuilder, this.adminMode ?
                Material.BLACK_STAINED_GLASS_PANE :
                    Material.BLUE_STAINED_GLASS_PANE);

        int elementIndex;

        if (this.gameModeAddons.size() < 8)
        {
            if (this.gameModeAddons.size() == 7)
            {
                elementIndex = 19;
            }
            else
            {
                elementIndex = 22 - this.gameModeAddons.size() / 2;
            }
        }
        else
        {
            elementIndex = 10;
        }

        for (GameModeAddon gameModeAddon : this.gameModeAddons)
        {
            if (!panelBuilder.slotOccupied(elementIndex))
            {
                panelBuilder.item(elementIndex++, this.createGameModeIcon(gameModeAddon));
            }
            else
            {
                // Find first open slot
                while (panelBuilder.slotOccupied(elementIndex))
                {
                    elementIndex++;
                }
            }
        }

        panelBuilder.build();
    }


    /**
     * This method creates icon that will display given GameMode addon.
     * @param gameModeAddon GameMode addon.
     * @return PanelItem that acts as icon for given GameMode.
     */
    private PanelItem createGameModeIcon(GameModeAddon gameModeAddon)
    {
        return new PanelItemBuilder().
                name(gameModeAddon.getDescription().getName()).
                description(gameModeAddon.getDescription().getDescription()).
                icon(Material.PAPER).
                clickHandler((panel, user, clickType, slot) -> {
                    Optional<CompositeCommand> command;

                    if (this.adminMode)
                    {
                        command = gameModeAddon.getAdminCommand();
                    }
                    else
                    {
                        command = gameModeAddon.getPlayerCommand();
                    }

                    command.ifPresent(compositeCommand ->
                    user.performCommand(compositeCommand.getTopLabel() + " challenges"));

                    return true;
                }).
                build();
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * List with game mode addons which must be showed in current GUI.
     */
    private List<GameModeAddon> gameModeAddons;

    /**
     * Stores if current GUI is in Admin Mode or not.
     */
    private boolean adminMode;
}
