package world.bentobox.challenges.panel.util;


import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;


/**
 * This GUI allows to select single entity and return it via Consumer.
 */
public class SingleEntitySelector extends PagedSelector<EntityType>
{
	/**
	 * Instantiates a new Single entity selector.
	 *
	 * @param user the user
	 * @param asEggs the boolean
	 * @param mode the mode
	 * @param excluded the excluded
	 * @param consumer the consumer
	 */
	private SingleEntitySelector(User user, boolean asEggs, Mode mode, Set<EntityType> excluded, BiConsumer<Boolean, EntityType> consumer)
	{
		super(user);
		this.consumer = consumer;
		this.asEggs = asEggs;
		this.elements = Arrays.stream(EntityType.values()).
			filter(entity -> !excluded.contains(entity)).
			filter(entity -> {
				if (mode == Mode.ALIVE)
				{
					return entity.isAlive();
				}
				else
				{
					return true;
				}
			}).
			// Sort by names
			sorted(Comparator.comparing(EntityType::name)).
			collect(Collectors.toList());
		// Init without filters applied.
		this.filterElements = this.elements;
	}


	/**
	 * This method opens GUI that allows to select challenge type.
	 *
	 * @param user User who opens GUI.
	 * @param consumer Consumer that allows to get clicked type.
	 */
	public static void open(User user, boolean asEggs, Mode mode, Set<EntityType> excluded, BiConsumer<Boolean, EntityType> consumer)
	{
		new SingleEntitySelector(user, asEggs, mode, excluded, consumer).build();
	}


	/**
	 * This method opens GUI that allows to select challenge type.
	 *
	 * @param user User who opens GUI.
	 * @param consumer Consumer that allows to get clicked type.
	 */
	public static void open(User user, boolean asEggs, BiConsumer<Boolean, EntityType> consumer)
	{
		new SingleEntitySelector(user, asEggs, Mode.ANY, new HashSet<>(), consumer).build();
	}


	/**
	 * This method opens GUI that allows to select challenge type.
	 *
	 * @param user User who opens GUI.
	 * @param consumer Consumer that allows to get clicked type.
	 */
	public static void open(User user, boolean asEggs, Mode mode, BiConsumer<Boolean, EntityType> consumer)
	{
		new SingleEntitySelector(user, asEggs, mode, new HashSet<>(), consumer).build();
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * This method builds
	 */
	@Override
	protected void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user);
		panelBuilder.name(this.user.getTranslation(Constants.TITLE + "entity-selector"));

		PanelUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

		this.populateElements(panelBuilder, this.filterElements);

		panelBuilder.item(4, this.createButton());

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
					return element.name().toLowerCase().contains(this.searchString.toLowerCase());
				}).
				distinct().
				collect(Collectors.toList());
		}
	}


	/**
	 * This method builds PanelItem for given entity.
	 * @param entity Entity which PanelItem must be created.
	 * @return new PanelItem for given Entity.
	 */
	@Override
	protected PanelItem createElementButton(EntityType entity)
	{
		final String reference = Constants.BUTTON + "entity.";

		List<String> description = new ArrayList<>();
		description.add(this.user.getTranslation(reference + "description",
			"[id]", entity.name()));
		description.add("");
		description.add(this.user.getTranslation(Constants.TIPS + "click-to-choose"));

		return new PanelItemBuilder().
			name(this.user.getTranslation(reference + "name", "[entity]",
				Utils.prettifyObject(entity, this.user))).
			icon(this.asEggs ? PanelUtils.getEntityEgg(entity) : PanelUtils.getEntityHead(entity)).
			description(description).
			clickHandler((panel, user1, clickType, slot) -> {
				this.consumer.accept(true, entity);
				return true;
			}).
			build();
	}


	/**
	 * This method creates PanelItem button of requested type.
	 * @return new PanelItem with requested functionality.
	 */
	private PanelItem createButton()
	{
		final String reference = Constants.BUTTON + "cancel.";

		final String name = this.user.getTranslation(reference + "name");
		final List<String> description = new ArrayList<>(3);
		description.add(this.user.getTranslation(reference + "description"));

		ItemStack icon = new ItemStack(Material.IRON_DOOR);
		PanelItem.ClickHandler clickHandler = (panel, user1, clickType, slot) ->
		{
			this.consumer.accept(false, null);
			return true;
		};

		description.add("");
		description.add(this.user.getTranslation(Constants.TIPS + "click-to-cancel"));

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(description).
			clickHandler(clickHandler).
			build();
	}


// ---------------------------------------------------------------------
// Section: Mode
// ---------------------------------------------------------------------


	public enum Mode
	{
		ALIVE,
		ANY
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * List with elements that will be displayed in current GUI.
	 */
	private final List<EntityType> elements;

	/**
	 * Indicates if entities are displayed as eggs or heads.
	 */
	private final boolean asEggs;

	/**
	 * This variable stores consumer.
	 */
	private final BiConsumer<Boolean, EntityType> consumer;

	/**
	 * Stores filtered items.
	 */
	private List<EntityType> filterElements;
}
