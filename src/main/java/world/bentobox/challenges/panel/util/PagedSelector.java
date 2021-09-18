//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.challenges.panel.util;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.Constants;


/**
 * This single abstract class will manage paged selectors similar to CommonPagedPanel.
 */
public abstract class PagedSelector<T>
{
    /**
     * Instantiates a new Paged selector.
     *
     * @param user the user
     */
    protected PagedSelector(User user)
    {
        this.user = user;
    }


    /**
     * Build.
     */
    protected abstract void build();


    /**
     * Create element button panel item.
     *
     * @param object the object
     * @return the panel item
     */
    protected abstract PanelItem createElementButton(T object);


    /**
     * Populate elements.
     *
     * @param panelBuilder the panel builder
     * @param objectList the object list
     */
    protected void populateElements(PanelBuilder panelBuilder, List<T> objectList)
    {
        final int MAX_ELEMENTS = 21;
        final int size = objectList.size();

        if (this.pageIndex < 0)
        {
            this.pageIndex = size / MAX_ELEMENTS;
        }
        else if (this.pageIndex > (size / MAX_ELEMENTS))
        {
            this.pageIndex = 0;
        }

        int objectIndex = MAX_ELEMENTS * this.pageIndex;

        // I want first row to be only for navigation and return button.
        int index = 10;

        while (objectIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
            objectIndex < size &&
            index < 36)
        {
            if (!panelBuilder.slotOccupied(index))
            {
                panelBuilder.item(index, this.createElementButton(objectList.get(objectIndex++)));
            }

            index++;
        }

        if (size > MAX_ELEMENTS && !(1.0 * size / MAX_ELEMENTS <= this.pageIndex + 1))
        {
            panelBuilder.item(26, this.getButton(CommonButtons.NEXT));

        }

        if (this.pageIndex > 0)
        {
            panelBuilder.item(18, this.getButton(CommonButtons.PREVIOUS));
        }
    }


    /**
     * This method returns PanelItem that represents given Button.
     * @param button Button that must be returned.
     * @return PanelItem with requested functionality.
     */
    protected PanelItem getButton(CommonButtons button)
    {
        final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";

        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);

        ItemStack icon;
        PanelItem.ClickHandler clickHandler;

        if (button == CommonButtons.NEXT)
        {
            description.add(this.user.getTranslation(reference + "description",
                Constants.NUMBER, String.valueOf(this.pageIndex + 2)));

            icon = new ItemStack(Material.OAK_SIGN, this.pageIndex + 2);
            clickHandler = (panel, user, clickType, slot) ->
            {
                this.pageIndex++;
                this.build();
                return true;
            };
        }
        else if (button == CommonButtons.PREVIOUS)
        {
            description.add(this.user.getTranslation(reference + "description",
                Constants.NUMBER, String.valueOf(this.pageIndex)));

            icon = new ItemStack(Material.OAK_SIGN, Math.max(1, this.pageIndex));
            clickHandler = (panel, user, clickType, slot) ->
            {
                this.pageIndex--;
                this.build();
                return true;
            };
        }
        else
        {
            icon = new ItemStack(Material.PAPER);
            clickHandler = null;
        }

        return new PanelItemBuilder().
            icon(icon).
            name(name).
            description(description).
            clickHandler(clickHandler).
            build();
    }


    /**
     * Next and Previous Buttons.
     */
    private enum CommonButtons
    {
        NEXT,
        PREVIOUS
    }


    /**
     * Current page index.
     */
    private int pageIndex;

    /**
     * User who opens gui.
     */
    protected final User user;
}
