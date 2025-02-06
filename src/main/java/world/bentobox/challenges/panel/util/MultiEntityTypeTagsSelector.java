package world.bentobox.challenges.panel.util;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;


/**
 * This class contains all necessary things that allows to select single entitytype tag from all in game tags. Selected
 * tag will be returned via BiConsumer.
 */
public class MultiEntityTypeTagsSelector extends PagedSelector<Tag<EntityType>>
{

    public static final Map<Tag<EntityType>, Material> ICONS = Map.of();

    /**
     * Functional buttons in current GUI.
     */
    private enum Button {
        ACCEPT_SELECTED, CANCEL
    }

    public enum Mode {
        ENTITY_TYPE, ANY
    }

    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    private final List<Tag<EntityType>> elements = new ArrayList<>();

    /**
     * Set that contains selected materials.
     */
    private final Set<Tag<EntityType>> selectedElements;

    /**
     * This variable stores consumer.
     */
    private final BiConsumer<Boolean, Collection<Tag<EntityType>>> consumer;

    /**
     * Stores filtered items.
     */
    private List<Tag<EntityType>> filterElements;

    private MultiEntityTypeTagsSelector(User user, Mode mode, Set<Tag<EntityType>> excluded,
            BiConsumer<Boolean, Collection<Tag<EntityType>>> consumer) {
		super(user);
		this.consumer = consumer;
		
		this.selectedElements = new HashSet<>();
        Iterable<Tag<EntityType>> iterable = Bukkit.getTags("entity_types", EntityType.class);
        iterable.forEach(elements::add);
        elements.sort(Comparator.comparing(tag -> tag.getKey().getKey()));
        // Remove irrelevant tags
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("AXOLOTL"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("IMMUNE"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("IGNORES"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("FRIEND"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("SENSITIVE"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("PROJECTILE"));
        elements.remove(Tag.ENTITY_TYPES_ARROWS);
        elements.remove(Tag.ENTITY_TYPES_BEEHIVE_INHABITORS);
        elements.remove(Tag.ENTITY_TYPES_CAN_TURN_IN_BOATS);
        elements.remove(Tag.ENTITY_TYPES_DISMOUNTS_UNDERWATER);
        elements.remove(Tag.ENTITY_TYPES_FALL_DAMAGE_IMMUNE);
        elements.remove(Tag.ENTITY_TYPES_FREEZE_HURTS_EXTRA_TYPES);
        elements.remove(Tag.ENTITY_TYPES_INVERTED_HEALING_AND_HARM);
        elements.remove(Tag.ENTITY_TYPES_NO_ANGER_FROM_WIND_CHARGE);
        elements.remove(Tag.ENTITY_TYPES_NON_CONTROLLING_RIDER);
        elements.remove(Tag.ENTITY_TYPES_NOT_SCARY_FOR_PUFFERFISH);
        elements.remove(Tag.ENTITY_TYPES_FROG_FOOD);
        // Remove excluded tags
        excluded.forEach(tag -> elements.removeIf(tag2 -> tag2.equals(tag)));
		// Init without filters applied.
		this.filterElements = this.elements;
	}


	/**
	 * This method opens GUI that allows to select challenge type.
	 *
	 * @param user User who opens GUI.
	 * @param consumer Consumer that allows to get clicked type.
	 */
    public static void open(User user, Mode mode, Set<Tag<EntityType>> excluded,
            BiConsumer<Boolean, Collection<Tag<EntityType>>> consumer)
	{
		new MultiEntityTypeTagsSelector(user, mode, excluded, consumer).build();
	}


	/**
	 * This method opens GUI that allows to select challenge type.
	 *
	 * @param user User who opens GUI.
	 * @param consumer Consumer that allows to get clicked type.
	 */
    public static void open(User user, BiConsumer<Boolean, Collection<Tag<EntityType>>> consumer)
	{
		new MultiEntityTypeTagsSelector(user, Mode.ANY, new HashSet<>(), consumer).build();
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * This method builds all necessary elements in GUI panel.
	 */
	@Override
	protected void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user);
        panelBuilder.name(this.user.getTranslation(Constants.TITLE + "entity-selector"));

		PanelUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

		this.populateElements(panelBuilder, this.filterElements);

		panelBuilder.item(3, this.createButton(Button.ACCEPT_SELECTED));
		panelBuilder.item(5, this.createButton(Button.CANCEL));

		panelBuilder.build();
	}


	/**
	 * This method is called when filter value is updated.
	 */
	@Override
	protected void updateFilters()
	{
		if (this.searchString == null || this.searchString.isBlank())
		{
			this.filterElements = this.elements;
		}
		else
		{
			this.filterElements = this.elements.stream().
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

		switch (button)
		{
			case ACCEPT_SELECTED -> {
				if (!this.selectedElements.isEmpty())
				{
					description.add(this.user.getTranslation(reference + "title"));
					this.selectedElements.forEach(material ->
						description.add(this.user.getTranslation(reference + "element",
							"[element]", Utils.prettifyObject(material, this.user))));
				}

				icon = new ItemStack(Material.COMMAND_BLOCK);
				clickHandler = (panel, user1, clickType, slot) ->
				{
					this.consumer.accept(true, this.selectedElements);
					return true;
				};

				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-save"));
			}
			case CANCEL -> {

				icon = new ItemStack(Material.IRON_DOOR);

				clickHandler = (panel, user1, clickType, slot) ->
				{
					this.consumer.accept(false, null);
					return true;
				};

				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-cancel"));
			}
			default -> {
				icon = new ItemStack(Material.PAPER);
				clickHandler = null;
			}
		}

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(description).
			clickHandler(clickHandler).
			build();
	}


	/**
	 * This method creates button for given material.
	 * @param materialTag material which button must be created.
	 * @return new Button for material.
	 */
	@Override
    protected PanelItem createElementButton(Tag<EntityType> materialTag)
	{
        final String reference = Constants.BUTTON + "entity-group.";

		List<String> description = new ArrayList<>();
		description.add(this.user.getTranslation(reference + "description",
                "[id]", materialTag.getKey().getKey()));

		if (this.selectedElements.contains(materialTag))
		{
			description.add(this.user.getTranslation(reference + "selected"));
			description.add("");
			description.add(this.user.getTranslation(Constants.TIPS + "click-to-deselect"));
		}
		else
		{
			description.add("");
			description.add(this.user.getTranslation(Constants.TIPS + "click-to-select"));
		}

		return new PanelItemBuilder().
                name(this.user.getTranslation(reference + "name", "[tag]",
				Utils.prettifyObject(materialTag, this.user))).
                icon(getIcon(materialTag)).
			description(description).
			clickHandler((panel, user1, clickType, slot) -> {
				// On right click change which entities are selected for deletion.
                    if (!this.selectedElements.add(materialTag))
				{
					// Remove material if it is already selected
                        this.selectedElements.remove(materialTag);
				}

				this.build();
				return true;
			}).
			glow(this.selectedElements.contains(materialTag)).
			build();
	}

	
    private @Nullable Material getIcon(Tag<EntityType> materialTag) {
        if (materialTag.getKey().getKey().contains("boat")) {
            return Material.OAK_BOAT;
        }
        EntityType entType = Registry.ENTITY_TYPE.stream().filter(materialTag::isTagged).findAny().orElse(null);
        String eggName = entType.getKey().getKey().toUpperCase(Locale.ENGLISH) + "_SPAWN_EGG";
        Material result;
        try {
            result = Material.valueOf(eggName);
        } catch (Exception e) {
            result = Material.PAPER;
        }
        return result;
    }

}
