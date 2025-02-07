//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.challenges.panel.user;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.TemplatedPanel;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.panels.builders.TemplatedPanelBuilder;
import world.bentobox.bentobox.api.panels.reader.ItemTemplateRecord;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.panel.ConversationUtils;
import world.bentobox.challenges.utils.Constants;


public class MultiplePanel
{
    private MultiplePanel(ChallengesAddon addon, User user, Consumer<Integer> action)
    {
        this.addon = addon;
        this.user = user;
        this.action = action;
    }


    public static void open(ChallengesAddon addon, User user, Consumer<Integer> action)
    {
        new MultiplePanel(addon, user, action).build();
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    private void build()
    {
        // Start building panel.
        TemplatedPanelBuilder panelBuilder = new TemplatedPanelBuilder();

        // Set main template.
        panelBuilder.template("multiple_panel", new File(this.addon.getDataFolder(), "panels"));
        panelBuilder.user(this.user);
        panelBuilder.world(this.user.getWorld());

        // Register button builders
        panelBuilder.registerTypeBuilder("INCREASE", this::createIncreaseButton);
        panelBuilder.registerTypeBuilder("REDUCE", this::createReduceButton);
        panelBuilder.registerTypeBuilder("ACCEPT", this::createValueButton);

        // Register unknown type builder.
        panelBuilder.build();
    }


    @NonNull
    private PanelItem createIncreaseButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot itemSlot)
    {
        int increaseValue = (int) template.dataMap().getOrDefault("value", 1);

        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            ItemStack clone = template.icon().clone();
            clone.setAmount(increaseValue);
            builder.icon(clone);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(template.description(),
                Constants.PARAMETER_NUMBER, String.valueOf(increaseValue)));
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            this.completionValue += increaseValue;
            this.build();

            // Always return true.
            return true;
        });

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(action.tooltip())).
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


    @NonNull
    private PanelItem createReduceButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot itemSlot)
    {
        int decreaseValue = (int) template.dataMap().getOrDefault("value", 1);

        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            ItemStack clone = template.icon().clone();
            clone.setAmount(decreaseValue);
            builder.icon(clone);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(template.description(),
                Constants.PARAMETER_NUMBER, String.valueOf(decreaseValue)));
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            this.completionValue = Math.max(this.completionValue - decreaseValue, 1);
            this.build();

            // Always return true.
            return true;
        });

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(action.tooltip())).
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


    @NonNull
    private PanelItem createValueButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot itemSlot)
    {
        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            ItemStack clone = template.icon().clone();
            clone.setAmount(this.completionValue);
            builder.icon(clone);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(template.description(),
                Constants.PARAMETER_NUMBER, String.valueOf(completionValue)));
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            for (ItemTemplateRecord.ActionRecords actionRecords : template.actions())
            {
                if (clickType == actionRecords.clickType())
                {
                    if (actionRecords.actionType().equalsIgnoreCase("input"))
                    {
                        // Input consumer.
                        Consumer<Number> numberConsumer = number ->
                        {
                            if (number != null)
                            {
                                this.completionValue = number.intValue();
                            }

                            // reopen panel
                            this.build();
                        };

                        ConversationUtils.createNumericInput(numberConsumer,
                            this.user,
                            this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                            1,
                            2000);
                    }
                    else if (actionRecords.actionType().equalsIgnoreCase("accept"))
                    {
                        this.action.accept(this.completionValue);
                    }
                }
            }

            // Always return true.
            return true;
        });

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(action.tooltip())).
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
     * Variable stores user who created this panel.
     */
    private final User user;

    /**
     * This variable holds action that will be performed on accept.
     */
    private final Consumer<Integer> action;

    /**
     * Variable stores Challenges addon.
     */
    protected final ChallengesAddon addon;

    /**
     * Local storing of selected value.
     */
    private int completionValue = 1;
}
