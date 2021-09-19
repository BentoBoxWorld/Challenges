//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.challenges.panel.user;


import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.TemplatedPanel;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.panels.builders.TemplatedPanelBuilder;
import world.bentobox.bentobox.api.panels.reader.ItemTemplateRecord;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.panel.CommonPanel;
import world.bentobox.challenges.utils.Constants;


/**
 * Main challenges panel builder.
 */
public class GameModePanel extends CommonPanel
{
    private GameModePanel(ChallengesAddon addon,
        World world,
        User user,
        List<GameModeAddon> addonList,
        boolean adminMode)
    {
        super(addon, user, world, null, null);
        this.addonList = addonList;
        this.adminMode = adminMode;
    }


    /**
     * Open the Challenges GUI.
     *
     * @param addon the addon
     * @param world the world
     * @param user the user
     * @param addonList List of gamemode addons
     * @param adminMode Indicate if admin mode.
     */
    public static void open(ChallengesAddon addon,
        World world,
        User user,
        List<GameModeAddon> addonList,
        boolean adminMode)
    {
        new GameModePanel(addon, world, user, addonList, adminMode).build();
    }


    protected void build()
    {
        // Start building panel.
        TemplatedPanelBuilder panelBuilder = new TemplatedPanelBuilder();

        // Set main template.
        panelBuilder.template("gamemode_panel", new File(this.addon.getDataFolder(), "panels"));
        panelBuilder.user(this.user);
        panelBuilder.world(this.user.getWorld());

        // Register button builders
        panelBuilder.registerTypeBuilder("GAMEMODE", this::createGameModeButton);

        panelBuilder.registerTypeBuilder("NEXT", this::createNextButton);
        panelBuilder.registerTypeBuilder("PREVIOUS", this::createPreviousButton);

        // Register unknown type builder.
        panelBuilder.build();
    }


    @Nullable
    private PanelItem createGameModeButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        if (this.addonList.isEmpty())
        {
            // Does not contain any free challenges.
            return null;
        }

        GameModeAddon gameModeAddon;

        // Check if that is a specific free challenge
        if (template.dataMap().containsKey("id"))
        {
            String id = (String) template.dataMap().get("id");

            // Find a challenge with given Id;
            gameModeAddon = this.addonList.stream().
                filter(gamemode -> gamemode.getDescription().getName().equals(id)).
                findFirst().
                orElse(null);

            if (gameModeAddon == null)
            {
                // There is no gamemode in the list with specific id.
                return null;
            }
        }
        else
        {
            int index = this.addonIndex * slot.amountMap().getOrDefault("GAMEMODE", 1) + slot.slot();

            if (index >= this.addonList.size())
            {
                // Out of index.
                return null;
            }

            gameModeAddon = this.addonList.get(index);
        }

        return this.createGameModeButton(template, gameModeAddon);
    }


    @NonNull
    private PanelItem createGameModeButton(ItemTemplateRecord template, @NonNull GameModeAddon gameModeAddon)
    {
        PanelItemBuilder builder = new PanelItemBuilder();

        // Template specification are always more important than dynamic content.
        builder.icon(template.icon() != null ?
            template.icon().clone() :
            new ItemStack(gameModeAddon.getDescription().getIcon()));

        // Template specific title is always more important than challenge name.
        if (template.title() != null && !template.title().isBlank())
        {
            builder.name(this.user.getTranslation(this.world, template.title(),
                Constants.GAMEMODE, gameModeAddon.getDescription().getName()));
        }
        else
        {
            builder.name(Util.translateColorCodes(gameModeAddon.getDescription().getName()));
        }

        if (template.description() != null && !template.description().isBlank())
        {
            // TODO: adding parameters could be useful.
            builder.description(this.user.getTranslation(this.world, template.description()));
        }
        else
        {
            builder.description(gameModeAddon.getDescription().getDescription());
        }

        // Add Click handler
        builder.clickHandler((panel, user, clickType, i) -> {
            for (ItemTemplateRecord.ActionRecords action : template.actions())
            {
                if (clickType == action.clickType())
                {
                    if (this.adminMode)
                    {
                        gameModeAddon.getAdminCommand().ifPresent(compositeCommand ->
                            user.performCommand(compositeCommand.getTopLabel() + " " +
                                this.addon.getChallengesSettings().getAdminMainCommand().split(" ")[0]));
                    }
                    else
                    {
                        gameModeAddon.getPlayerCommand().ifPresent(compositeCommand ->
                            user.performCommand(compositeCommand.getTopLabel() + " " +
                                this.addon.getChallengesSettings().getPlayerMainCommand().split(" ")[0]));
                    }
                }
            }

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

        // Glow the icon.
        builder.glow(gameModeAddon.inWorld(this.user.getWorld()));

        // Click Handlers are managed by custom addon buttons.
        return builder.build();
    }


    @Nullable
    private PanelItem createNextButton(@NonNull ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        String target = template.dataMap().getOrDefault("target", "").toString().toUpperCase();

        int nextPageIndex;

        if ("GAMEMODE".equals(target))
        {
            int size = this.addonList.size();

            if (size <= slot.amountMap().getOrDefault("GAMEMODE", 1) ||
                1.0 * size / slot.amountMap().getOrDefault("GAMEMODE", 1) <= this.addonIndex + 1)
            {
                // There are no next elements
                return null;
            }

            nextPageIndex = this.addonIndex + 2;
        }
        else
        {// If not assigned to any type, return null.
            return null;
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
            builder.description(this.user.getTranslation(this.world, template.description()),
                Constants.NUMBER, String.valueOf(nextPageIndex));
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            this.addonIndex++;
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

        if ("GAMEMODE".equals(target))
        {
            if (this.addonIndex == 0)
            {
                // There are no next elements
                return null;
            }

            previousPageIndex = this.addonIndex;
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
            builder.description(this.user.getTranslation(this.world, template.description()),
                Constants.NUMBER, String.valueOf(previousPageIndex));
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            // Next button ignores click type currently.
            this.addonIndex--;
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
     * This will be used if free challenges are more than 18.
     */
    private int addonIndex;

    /**
     * This list contains challenges in current Panel.
     */
    private final List<GameModeAddon> addonList;

    /**
     * Indicate if gui is for players or admins.
     */
    private final boolean adminMode;
}
