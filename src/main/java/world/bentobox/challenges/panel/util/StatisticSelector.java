package world.bentobox.challenges.panel.util;


import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.GuiUtils;
import world.bentobox.challenges.utils.Utils;


/**
 * This class contains all necessary things that allows to select single statistic. Selected
 * stats will be returned via BiConsumer.
 */
public class StatisticSelector extends PagedSelector<Statistic>
{
	/**
	 * Instantiates a new Statistic selector.
	 *
	 * @param user the user
	 * @param consumer the consumer
	 */
	private StatisticSelector(User user, BiConsumer<Boolean, Statistic> consumer)
	{
		super(user);

		this.consumer = consumer;
		this.elements = new ArrayList<>(Arrays.asList(Statistic.values()));
		this.elements.sort(Comparator.comparing(Statistic::name));

		// Init without filters applied.
		this.filterElements = this.elements;
	}


	/**
	 * This method opens GUI that allows to select challenge type.
	 *
	 * @param user User who opens GUI.
	 * @param consumer Consumer that allows to get clicked type.
	 */
	public static void open(User user, BiConsumer<Boolean, Statistic> consumer)
	{
		new StatisticSelector(user, consumer).build();
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
		panelBuilder.name(this.user.getTranslation(Constants.TITLE + "statistic-selector"));

		GuiUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

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
	 * This method creates PanelItem that represents given statistic.
	 * Some materials is not displayable in Inventory GUI, so they are replaced with "placeholder" items.
	 * @param statistic Material which icon must be created.
	 * @return PanelItem that represents given statistic.
	 */
	@Override
	protected PanelItem createElementButton(Statistic statistic)
	{
		final String reference = Constants.BUTTON + "statistic_element.";

		List<String> description = new ArrayList<>();

		String descriptionText = this.user.getTranslationOrNothing(reference + description,
			"[description]", Utils.prettifyDescription(statistic, user));

		if (!descriptionText.isEmpty())
		{
			description.add(descriptionText);
		}

		description.add("");
		description.add(this.user.getTranslation(Constants.TIPS + "click-to-choose"));

		return new PanelItemBuilder().
			name(this.user.getTranslation(reference + "name", "[statistic]",
				Utils.prettifyObject(statistic, this.user))).
			icon(Material.PAPER).
			description(description).
			clickHandler((panel, user1, clickType, slot) -> {
				this.consumer.accept(true, statistic);
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
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * List with elements that will be displayed in current GUI.
	 */
	private final List<Statistic> elements;

	/**
	 * This variable stores consumer.
	 */
	private final BiConsumer<Boolean, Statistic> consumer;

	/**
	 * Stores filtered items.
	 */
	private List<Statistic> filterElements;
}
