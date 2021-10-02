package world.bentobox.challenges.panel.admin;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.challenges.panel.CommonPagedPanel;
import world.bentobox.challenges.panel.CommonPanel;
import world.bentobox.challenges.panel.ConversationUtils;
import world.bentobox.challenges.panel.util.MultiEntitySelector;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;


/**
 * This class allows to edit entities that are in required entities map.
 */
public class ManageEntitiesPanel extends CommonPagedPanel<EntityType>
{
	private ManageEntitiesPanel(CommonPanel parentGUI, Map<EntityType, Integer> requiredEntities)
	{
		super(parentGUI);
		this.requiredEntities = requiredEntities;

		this.entityList = new ArrayList<>(this.requiredEntities.keySet());
		this.entityList.sort(Comparator.comparing(Enum::name));

		this.selectedEntities = new HashSet<>(EntityType.values().length);
		this.filterElements = this.entityList;
	}


	/**
	 * Open the Challenges Admin GUI.
	 */
	public static void open(CommonPanel parentGUI, Map<EntityType, Integer> requiredEntities)
	{
		new ManageEntitiesPanel(parentGUI, requiredEntities).build();
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
			this.filterElements = this.entityList;
		}
		else
		{
			this.filterElements = this.entityList.stream().
				filter(element -> {
					// If element name is set and name contains search field, then do not filter out.
					return element.name().toLowerCase().contains(this.searchString.toLowerCase());
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
			name(this.user.getTranslation(Constants.TITLE + "manage-entities"));

		// create border
		PanelUtils.fillBorder(panelBuilder);

		panelBuilder.item(3, this.createButton(Button.ADD_ENTITY));
		panelBuilder.item(5, this.createButton(Button.REMOVE_ENTITY));
		panelBuilder.item(8, this.createButton(Button.SWITCH_ENTITY));

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
			case ADD_ENTITY -> {
				icon = new ItemStack(Material.BUCKET);
				clickHandler = (panel, user1, clickType, slot) -> {
					MultiEntitySelector.open(this.user,
						this.asEggs,
						MultiEntitySelector.Mode.ALIVE,
						this.requiredEntities.keySet(),
						(status, entities) -> {
							if (status)
							{
								entities.forEach(entity -> {
									this.requiredEntities.put(entity, 1);
									this.entityList.add(entity);
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
			case REMOVE_ENTITY -> {

				if (!this.selectedEntities.isEmpty())
				{
					description.add(this.user.getTranslation(reference + "title"));
					this.selectedEntities.forEach(entity -> {
						description.add(this.user.getTranslation(reference + "entity",
							"[entity]", Utils.prettifyObject(entity, this.user)));
					});
				}

				icon = new ItemStack(Material.LAVA_BUCKET);

				clickHandler = (panel, user1, clickType, slot) ->
				{
					if (!this.selectedEntities.isEmpty())
					{
						this.requiredEntities.keySet().removeAll(this.selectedEntities);
						this.entityList.removeAll(this.selectedEntities);
						this.selectedEntities.clear();
						this.build();
					}

					return true;
				};

				glow = !this.entityList.isEmpty();

				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-remove"));
			}
			case SWITCH_ENTITY -> {
				icon = new ItemStack(this.asEggs ? Material.EGG : Material.PLAYER_HEAD);

				clickHandler = (panel, user1, clickType, slot) -> {
					this.asEggs = !this.asEggs;
					this.build();
					return true;
				};
				glow = false;

				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
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
	 * This method creates button for given entity.
	 * @param entity Entity which button must be created.
	 * @return new Button for entity.
	 */
	@Override
	protected PanelItem createElementButton(EntityType entity)
	{
		final String reference = Constants.BUTTON + "entity.";

		List<String> description = new ArrayList<>();

		if (this.selectedEntities.contains(entity))
		{
			description.add(this.user.getTranslation(reference + "selected"));
		}

		description.add("");
		description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-choose"));

		if (this.selectedEntities.contains(entity))
		{
			description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-deselect"));
		}
		else
		{
			description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-select"));
		}

		return new PanelItemBuilder().
			name(this.user.getTranslation(reference + "name", "[entity]",
				Utils.prettifyObject(entity, this.user))).
			icon(this.asEggs ?
				PanelUtils.getEntityEgg(entity, this.requiredEntities.get(entity)) :
				PanelUtils.getEntityHead(entity, this.requiredEntities.get(entity))).
			description(description).
			clickHandler((panel, user1, clickType, slot) -> {
				// On right click change which entities are selected for deletion.
				if (clickType.isRightClick())
				{
					if (!this.selectedEntities.add(entity))
					{
						// Remove entity if it is already selected
						this.selectedEntities.remove(entity);
					}

					this.build();
				}
				else
				{
					Consumer<Number> numberConsumer = number -> {
						if (number != null)
						{
							this.requiredEntities.put(entity, number.intValue());
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
			glow(this.selectedEntities.contains(entity)).
			build();
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * Functional buttons in current GUI.
	 */
	private enum Button
	{
		ADD_ENTITY,
		REMOVE_ENTITY,
		SWITCH_ENTITY
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * List with entities to avoid list irregularities.
	 */
	private final List<EntityType> entityList;

	/**
	 * Set with entities that are selected.
	 */
	private final Set<EntityType> selectedEntities;

	/**
	 * Map that contains all entities and their cound.
	 */
	private final Map<EntityType, Integer> requiredEntities;

	/**
	 * Boolean indicate if entities should be displayed as eggs or mob heads.
	 */
	private boolean asEggs;

	/**
	 * Stores filtered items.
	 */
	private List<EntityType> filterElements;
}
