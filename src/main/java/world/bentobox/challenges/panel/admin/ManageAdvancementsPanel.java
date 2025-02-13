package world.bentobox.challenges.panel.admin;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.advancement.Advancement;
import org.bukkit.inventory.ItemStack;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.challenges.panel.CommonPagedPanel;
import world.bentobox.challenges.panel.CommonPanel;
import world.bentobox.challenges.panel.util.SingleAdvancementSelector;
import world.bentobox.challenges.utils.Constants;


/**
 * This class allows to edit material that are in required material map.
 */
public class ManageAdvancementsPanel extends CommonPagedPanel<Advancement>
{

    // ---------------------------------------------------------------------
    // Section: Enums
    // ---------------------------------------------------------------------

    /**
     * Functional buttons in current GUI.
     */
    private enum Button {
        ADD_ADVANCEMENT, REMOVE_ADVANCEMENT
    }

    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * Contains selected advancements.
     */
    private final Set<Advancement> selectedAdvancements;

    /**
     * List of required advancements
     */
    private final List<Advancement> advancementsList;

    /**
     * Stores filtered items.
     */
    private List<Advancement> filterElements;

    private ManageAdvancementsPanel(CommonPanel parentGUI, List<Advancement> advancementsList)
	{
		super(parentGUI);
        this.advancementsList = advancementsList;

        // Sort tags by their ordinal value.
        this.advancementsList.sort(Comparator.comparing(advancement -> advancement.getDisplay().getTitle()));

        this.selectedAdvancements = new HashSet<>();

		// Init without filters applied.
        this.filterElements = this.advancementsList;
	}


	/**
	 * Open the Challenges Admin GUI.
	 */
    public static void open(CommonPanel parentGUI, List<Advancement> advancementsList) {
        new ManageAdvancementsPanel(parentGUI, advancementsList).build();
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
		if (this.searchString == null || this.searchString.isBlank())
		{
            this.filterElements = this.advancementsList;
		}
		else
		{
            this.filterElements = this.advancementsList.stream().
				filter(element -> {
					// If element name is set and name contains search field, then do not filter out.
                        return element.getDisplay().getTitle().toLowerCase(Locale.ENGLISH)
                                .contains(this.searchString.toLowerCase(Locale.ENGLISH));
				}).
				distinct().
				collect(Collectors.toList());
		}
	}


	/**
	 * This method builds all necessary elements in GUI panel.
	 */
	@Override
	protected void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).
                name(this.user.getTranslation(Constants.TITLE + "manage-advancements"));

		// Create nice border.
		PanelUtils.fillBorder(panelBuilder);

        panelBuilder.item(3, this.createButton(Button.ADD_ADVANCEMENT));
        panelBuilder.item(5, this.createButton(Button.REMOVE_ADVANCEMENT));
        // Fill the box with what is selected
		this.populateElements(panelBuilder, this.filterElements);

		// Add return button.
		panelBuilder.item(44, this.returnButton);

		panelBuilder.build();
	}


	/**
	 * This method creates PanelItem button of requested type.
	 * @param button Button which must be created.
	 * @return new PanelItem with requested functionality.
	 */
	private PanelItem createButton(Button button)
	{
		final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";

        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);
		description.add(this.user.getTranslation(reference + "description"));

		ItemStack icon;
		PanelItem.ClickHandler clickHandler;
		boolean glow;

		switch (button)
		{
        case ADD_ADVANCEMENT -> {
				icon = new ItemStack(Material.BUCKET);
				clickHandler = (panel, user1, clickType, slot) ->
				{
                    SingleAdvancementSelector.open(this.user, (status, advancement) ->
						{
							if (status)
							{
                            this.advancementsList.add(advancement);
							}

							this.build();
						});
					return true;
				};
				glow = false;

				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-add"));
			}
            case REMOVE_ADVANCEMENT -> {

                if (!this.selectedAdvancements.isEmpty())
				{
                    this.selectedAdvancements.forEach(adv -> description.add(adv.getDisplay().getTitle()));
				}

				icon = new ItemStack(Material.LAVA_BUCKET);

				clickHandler = (panel, user1, clickType, slot) ->
				{
                    if (!this.selectedAdvancements.isEmpty())
					{
                        this.advancementsList.removeAll(this.selectedAdvancements);
                        this.selectedAdvancements.clear();
						this.build();
					}

					return true;
				};

                glow = !this.selectedAdvancements.isEmpty();

				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-remove"));
			}
			default -> {
				icon = new ItemStack(Material.PAPER);
				clickHandler = null;
				glow = false;
			}
		}

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(description).
			clickHandler(clickHandler).
			glow(glow).
			build();
	}


	/**
     * This method creates button for given stat.
     * @param rec material which button must be created.
     * @return new Button for material.
     */
	@Override
    protected PanelItem createElementButton(Advancement rec)
	{
        final String reference = Constants.BUTTON + "advancement_element.";

		List<String> description = new ArrayList<>();

        // Show everything about this advancement
        description
                .add(this.user.getTranslation(reference + "description", "[description]",
                        rec.getDisplay().getDescription()));

        if (this.selectedAdvancements.contains(rec))
		{
			description.add(this.user.getTranslation(reference + "selected"));
		}

		description.add("");

        if (this.selectedAdvancements.contains(rec))
		{
			description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-deselect"));
		}
		else
		{
			description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-select"));
		}

		return new PanelItemBuilder().
                name(this.user.getTranslation(reference + "name", "[name]", rec.getDisplay().getTitle()))
                .icon(rec.getDisplay().getIcon()).
			description(description).
			clickHandler((panel, user1, clickType, slot) -> {
				// On right click change which entities are selected for deletion.
				if (clickType.isRightClick())
				{
                        if (!this.selectedAdvancements.add(rec))
					{
						// Remove material if it is already selected
                            this.selectedAdvancements.remove(rec);
					}

					this.build();
				}
				return true;
			}).
                glow(this.selectedAdvancements.contains(rec)).
			build();
	}

}
