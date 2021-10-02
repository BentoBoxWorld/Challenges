package world.bentobox.challenges.panel.util;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.utils.Constants;


/**
 * This class creates new GUI that allows to select single challenge, which is returned via consumer.
 */
public class ChallengeSelector extends PagedSelector<Challenge>
{
	private ChallengeSelector(User user, Material border, Map<Challenge, List<String>> challengesDescriptionMap, BiConsumer<Boolean, Set<Challenge>> consumer)
	{
		super(user);
		this.consumer = consumer;
		this.challengesDescriptionMap = challengesDescriptionMap;
		this.border = border;

		this.elements = challengesDescriptionMap.keySet().stream().toList();
		this.selectedElements = new HashSet<>(this.elements.size());
		this.filterElements = this.elements;
	}


	/**
	 * This method opens GUI that allows to select challenge type.
	 *
	 * @param user User who opens GUI.
	 * @param consumer Consumer that allows to get clicked type.
	 */
	public static void open(User user, Material border, Map<Challenge, List<String>> challengesDescriptionMap, BiConsumer<Boolean, Set<Challenge>> consumer)
	{
		new ChallengeSelector(user, border, challengesDescriptionMap, consumer).build();
	}


	/**
	 * This method builds all necessary elements in GUI panel.
	 */
	@Override
	protected void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user);
		panelBuilder.name(this.user.getTranslation(Constants.TITLE + "challenge-selector"));

		PanelUtils.fillBorder(panelBuilder, this.border);

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
					return element.getUniqueId().toLowerCase().
						contains(this.searchString.toLowerCase()) ||
						element.getFriendlyName().toLowerCase().
							contains(this.searchString.toLowerCase());
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
					this.selectedElements.forEach(challenge -> {
						description.add(this.user.getTranslation(reference + "element",
							"[element]", challenge.getFriendlyName()));
					});
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
	 * This method creates button for given challenge.
	 * @param challenge challenge which button must be created.
	 * @return new Button for challenge.
	 */
	@Override
	protected PanelItem createElementButton(Challenge challenge)
	{
		final String reference = Constants.BUTTON + "entity.";

		List<String> description = new ArrayList<>(this.challengesDescriptionMap.get(challenge));
		description.add("");

		if (this.selectedElements.contains(challenge))
		{
			description.add(this.user.getTranslation(reference + "selected"));
			description.add(this.user.getTranslation(Constants.TIPS + "click-to-deselect"));
		}
		else
		{
			description.add(this.user.getTranslation(Constants.TIPS + "click-to-select"));
		}

		return new PanelItemBuilder().
			name(Util.translateColorCodes(challenge.getFriendlyName())).
			icon(challenge.getIcon()).
			description(description).
			clickHandler((panel, user1, clickType, slot) -> {
				// On right click change which entities are selected for deletion.
				if (!this.selectedElements.add(challenge))
				{
					// Remove challenge if it is already selected
					this.selectedElements.remove(challenge);
				}

				this.build();
				return true;
			}).
			glow(this.selectedElements.contains(challenge)).
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


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * This variable stores consumer.
	 */
	private final BiConsumer<Boolean, Set<Challenge>> consumer;

	/**
	 * Current value.
	 */
	private final List<Challenge> elements;

	/**
	 * Selected challenges that will be returned to consumer.
	 */
	private final Set<Challenge> selectedElements;

	/**
	 * Map that contains all challenge descriptions
	 */
	private final Map<Challenge, List<String>> challengesDescriptionMap;

	/**
	 * Border Material.
	 */
	private final Material border;

	/**
	 * Current value.
	 */
	private List<Challenge> filterElements;
}
