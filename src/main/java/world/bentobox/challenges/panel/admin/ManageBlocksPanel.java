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
import org.bukkit.inventory.ItemStack;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.challenges.panel.CommonPagedPanel;
import world.bentobox.challenges.panel.CommonPanel;
import world.bentobox.challenges.panel.ConversationUtils;
import world.bentobox.challenges.panel.util.MultiBlockSelector;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;


/**
 * This class allows to edit material that are in required material map.
 */
public class ManageBlocksPanel extends CommonPagedPanel<Material>
{
	private ManageBlocksPanel(CommonPanel parentGUI, Map<Material, Integer> materialMap)
	{
		super(parentGUI);
		this.materialMap = materialMap;
		this.materialList = new ArrayList<>(this.materialMap.keySet());

		// Sort materials by their ordinal value.
		this.materialList.sort(Comparator.comparing(Enum::name));

		this.selectedMaterials = new HashSet<>();

		// Init without filters applied.
		this.filterElements = this.materialList;
	}


	/**
	 * Open the Challenges Admin GUI.
	 */
	public static void open(CommonPanel parentGUI, Map<Material, Integer> materialMap)
	{
		new ManageBlocksPanel(parentGUI, materialMap).build();
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
			name(this.user.getTranslation(Constants.TITLE + "manage-blocks"));

		// Create nice border.
		PanelUtils.fillBorder(panelBuilder);

		panelBuilder.item(3, this.createButton(Button.ADD_BLOCK));
		panelBuilder.item(5, this.createButton(Button.REMOVE_BLOCK));

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
			case ADD_BLOCK -> {
				icon = new ItemStack(Material.BUCKET);
				clickHandler = (panel, user1, clickType, slot) ->
				{
					MultiBlockSelector.open(this.user,
						MultiBlockSelector.Mode.BLOCKS,
						new HashSet<>(this.materialList),
						(status, materials) ->
						{
							if (status)
							{
								materials.forEach(material ->
								{
									this.materialMap.put(material, 1);
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
			case REMOVE_BLOCK -> {

				if (!this.selectedMaterials.isEmpty())
				{
					description.add(this.user.getTranslation(reference + "title"));
					this.selectedMaterials.forEach(material -> {
						description.add(this.user.getTranslation(reference + "material",
							"[material]", Utils.prettifyObject(material, this.user)));
					});
				}

				icon = new ItemStack(Material.LAVA_BUCKET);

				clickHandler = (panel, user1, clickType, slot) ->
				{
					if (!this.selectedMaterials.isEmpty())
					{
						this.materialMap.keySet().removeAll(this.selectedMaterials);
						this.materialList.removeAll(this.selectedMaterials);
						this.selectedMaterials.clear();
						this.build();
					}

					return true;
				};

				glow = !this.selectedMaterials.isEmpty();

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
	 * @param material material which button must be created.
	 * @return new Button for material.
	 */
	@Override
	protected PanelItem createElementButton(Material material)
	{
		final String reference = Constants.BUTTON + "material.";

		List<String> description = new ArrayList<>();

		if (this.selectedMaterials.contains(material))
		{
			description.add(this.user.getTranslation(reference + "selected"));
		}

		description.add("");
		description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-choose"));

		if (this.selectedMaterials.contains(material))
		{
			description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-deselect"));
		}
		else
		{
			description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-select"));
		}

		return new PanelItemBuilder().
			name(this.user.getTranslation(reference + "name", "[material]",
				Utils.prettifyObject(material, this.user))).
			icon(PanelUtils.getMaterialItem(material, this.materialMap.get(material))).
			description(description).
			clickHandler((panel, user1, clickType, slot) -> {
				// On right click change which entities are selected for deletion.
				if (clickType.isRightClick())
				{
					if (!this.selectedMaterials.add(material))
					{
						// Remove material if it is already selected
						this.selectedMaterials.remove(material);
					}

					this.build();
				}
				else
				{
					Consumer<Number> numberConsumer = number -> {
						if (number != null)
						{
							this.materialMap.put(material, number.intValue());
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
			glow(this.selectedMaterials.contains(material)).
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
		ADD_BLOCK,
		REMOVE_BLOCK
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * Contains selected materials.
	 */
	private final Set<Material> selectedMaterials;

	/**
	 * List of materials to avoid order issues.
	 */
	private final List<Material> materialList;

	/**
	 * List of required materials.
	 */
	private final Map<Material, Integer> materialMap;

	/**
	 * Stores filtered items.
	 */
	private List<Material> filterElements;
}
