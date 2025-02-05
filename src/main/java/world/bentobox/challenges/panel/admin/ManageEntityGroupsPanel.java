package world.bentobox.challenges.panel.admin;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.challenges.panel.CommonPagedPanel;
import world.bentobox.challenges.panel.CommonPanel;
import world.bentobox.challenges.panel.ConversationUtils;
import world.bentobox.challenges.panel.util.MultiEntityTypeTagsSelector;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;


/**
 * This class allows to edit material that are in required material map.
 */
public class ManageEntityGroupsPanel extends CommonPagedPanel<Tag<EntityType>>
{

    // ---------------------------------------------------------------------
    // Section: Enums
    // ---------------------------------------------------------------------

    /**
     * Functional buttons in current GUI.
     */
    private enum Button {
        ADD_ENTITY_GROUP, REMOVE_ENTITY_GROUP
    }

    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * Contains selected materials.
     */
    private final Set<Tag<EntityType>> selectedTags;

    /**
     * List of materials to avoid order issues.
     */
    private final List<Tag<EntityType>> materialList;

    /**
     * List of required materials.
     */
    private final Map<Tag<EntityType>, Integer> tagMap;

    /**
     * Stores filtered items.
     */
    private List<Tag<EntityType>> filterElements;

    private ManageEntityGroupsPanel(CommonPanel parentGUI, Map<Tag<EntityType>, Integer> map)
	{
		super(parentGUI);
        this.tagMap = map;
		this.materialList = new ArrayList<>(this.tagMap.keySet());

        // Sort tags by their ordinal value.
        this.materialList.sort(Comparator.comparing(tag -> tag.getKey().getKey()));

		this.selectedTags = new HashSet<>();

		// Init without filters applied.
		this.filterElements = this.materialList;
	}


	/**
	 * Open the Challenges Admin GUI.
	 */
    public static void open(CommonPanel parentGUI, Map<Tag<EntityType>, Integer> map)
	{
        new ManageEntityGroupsPanel(parentGUI, map).build();
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
			this.filterElements = this.materialList;
		}
		else
		{
			this.filterElements = this.materialList.stream().
				filter(element -> {
					// If element name is set and name contains search field, then do not filter out.
                        return element.getKey().getKey().toLowerCase(Locale.ENGLISH)
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
                name(this.user.getTranslation(Constants.TITLE + "manage-entity-groups"));

		// Create nice border.
		PanelUtils.fillBorder(panelBuilder);

		panelBuilder.item(3, this.createButton(Button.ADD_ENTITY_GROUP));
		panelBuilder.item(5, this.createButton(Button.REMOVE_ENTITY_GROUP));
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
			case ADD_ENTITY_GROUP -> {
				icon = new ItemStack(Material.BUCKET);
				clickHandler = (panel, user1, clickType, slot) ->
				{
                    MultiEntityTypeTagsSelector.open(this.user, MultiEntityTypeTagsSelector.Mode.ENTITY_TYPE,
						new HashSet<>(this.materialList),
						(status, materials) ->
						{
							if (status)
							{
								materials.forEach(material ->
								{
									this.tagMap.put(material, 1);
									this.materialList.add(material);
								});
							}

							this.build();
						});
					return true;
				};
				glow = false;

				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-add"));
			}
			case REMOVE_ENTITY_GROUP -> {

				if (!this.selectedTags.isEmpty())
				{
					description.add(this.user.getTranslation(reference + "title"));
					this.selectedTags.forEach(material ->
						description.add(this.user.getTranslation(reference + "material",
							"[material]", Utils.prettifyObject(material, this.user))));
				}

				icon = new ItemStack(Material.LAVA_BUCKET);

				clickHandler = (panel, user1, clickType, slot) ->
				{
					if (!this.selectedTags.isEmpty())
					{
						this.tagMap.keySet().removeAll(this.selectedTags);
						this.materialList.removeAll(this.selectedTags);
						this.selectedTags.clear();
						this.build();
					}

					return true;
				};

				glow = !this.selectedTags.isEmpty();

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
	 * This method creates button for given material.
	 * @param tag material which button must be created.
	 * @return new Button for material.
	 */
	@Override
    protected PanelItem createElementButton(Tag<EntityType> tag)
	{
        final String reference = Constants.BUTTON + "entity-group.";

		List<String> description = new ArrayList<>();

		if (this.selectedTags.contains(tag))
		{
			description.add(this.user.getTranslation(reference + "selected"));
		}

		description.add("");
		description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-choose"));

		if (this.selectedTags.contains(tag))
		{
			description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-deselect"));
		}
		else
		{
			description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-select"));
		}

		return new PanelItemBuilder().
                name(this.user.getTranslation(reference + "name", "[tag]",
				Utils.prettifyObject(tag, this.user))).
                icon(getIcon(tag, this.tagMap.get(tag))).
			description(description).
			clickHandler((panel, user1, clickType, slot) -> {
				// On right click change which entities are selected for deletion.
				if (clickType.isRightClick())
				{
					if (!this.selectedTags.add(tag))
					{
						// Remove material if it is already selected
						this.selectedTags.remove(tag);
					}

					this.build();
				}
				else
				{
					Consumer<Number> numberConsumer = number -> {
						if (number != null)
						{
							this.tagMap.put(tag, number.intValue());
						}

						// reopen panel
						this.build();
					};

					ConversationUtils.createNumericInput(numberConsumer,
						this.user,
						this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
						1,
						Integer.MAX_VALUE);
				}
				return true;
			}).
			glow(this.selectedTags.contains(tag)).
			build();
	}

    private @Nullable ItemStack getIcon(Tag<EntityType> entityTag, Integer quantity) {
        if (entityTag.getKey().getKey().contains("boat")) {
            return new ItemStack(Material.OAK_PLANKS, quantity); // Boats cannot be stacked
        }
        EntityType entType = Registry.ENTITY_TYPE.stream().filter(entityTag::isTagged).findAny().orElse(null);
        String eggName = entType.getKey().getKey().toUpperCase(Locale.ENGLISH) + "_SPAWN_EGG";
        Material result;
        try {
            result = Material.valueOf(eggName);
        } catch (Exception e) {
            result = Material.PAPER;
        }
        return new ItemStack(result, quantity);

    }


}
