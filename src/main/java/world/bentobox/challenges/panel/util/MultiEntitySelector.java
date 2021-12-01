package world.bentobox.challenges.panel.util;


import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;


/**
 * This class contains all necessary things that allows to select single block from all ingame blocks. Selected
 * block will be returned via BiConsumer.
 */
public class MultiEntitySelector extends PagedSelector<EntityType>
{
	private MultiEntitySelector(User user, boolean asEgg, Mode mode, Set<EntityType> excluded, BiConsumer<Boolean, Collection<EntityType>> consumer)
	{
		super(user);

		this.consumer = consumer;
		this.asEgg = asEgg;
		this.selectedElements = new HashSet<>();

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
			// Sort by name
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
	public static void open(User user, boolean asEgg, Mode mode, Set<EntityType> excluded, BiConsumer<Boolean, Collection<EntityType>> consumer)
	{
		new MultiEntitySelector(user, asEgg, mode, excluded, consumer).build();
	}


	/**
	 * This method opens GUI that allows to select challenge type.
	 *
	 * @param user User who opens GUI.
	 * @param consumer Consumer that allows to get clicked type.
	 */
	public static void open(User user, boolean asEgg, BiConsumer<Boolean, Collection<EntityType>> consumer)
	{
		new MultiEntitySelector(user, asEgg, Mode.ANY, new HashSet<>(), consumer).build();
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * This method builds all necessary elements in GUI panel.
	 */
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
					return element.name().toLowerCase().contains(this.searchString.toLowerCase());
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
	 * This method creates button for given entity.
	 * @param entity entity which button must be created.
	 * @return new Button for entity.
	 */
	@Override
	protected PanelItem createElementButton(EntityType entity)
	{
		final String reference = Constants.BUTTON + "entity.";

		List<String> description = new ArrayList<>();
		description.add(this.user.getTranslation(reference + "description",
			"[id]", entity.name()));

		if (this.selectedElements.contains(entity))
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
			name(this.user.getTranslation(reference + "name", "[entity]",
				Utils.prettifyObject(entity, this.user))).
			icon(this.asEgg ? PanelUtils.getEntityEgg(entity) : PanelUtils.getEntityHead(entity)).
			description(description).
			clickHandler((panel, user1, clickType, slot) -> {
				// On right click change which entities are selected for deletion.
				if (!this.selectedElements.add(entity))
				{
					// Remove entity if it is already selected
					this.selectedElements.remove(entity);
				}

				this.build();
				return true;
			}).
			glow(this.selectedElements.contains(entity)).
			build();
	}


	/**
	 * Functional buttons in current GUI.
	 */
	private enum Button
	{
		ACCEPT_SELECTED,
		CANCEL
	}


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
	 * Set that contains selected materials.
	 */
	private final Set<EntityType> selectedElements;

	/**
	 * This variable stores consumer.
	 */
	private final BiConsumer<Boolean, Collection<EntityType>> consumer;

	/**
	 * Indicates that entity must be displayed as egg.
	 */
	private final boolean asEgg;

	/**
	 * Stores filtered items.
	 */
	private List<EntityType> filterElements;
}
