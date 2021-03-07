package world.bentobox.challenges.panel.admin;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.hooks.LangUtilsHook;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.panel.CommonGUI;
import world.bentobox.challenges.panel.util.NumberGUI;
import world.bentobox.challenges.panel.util.SelectBlocksGUI;
import world.bentobox.challenges.utils.GuiUtils;


/**
 * This class allows to edit material that are in required material map.
 */
public class ManageBlocksGUI extends CommonGUI
{
	public ManageBlocksGUI(ChallengesAddon addon,
		World world,
		User user,
		Map<Material, Integer> materialMap,
		String topLabel,
		String permissionPrefix,
		CommonGUI parentGUI)
	{
		super(addon, world, user, topLabel, permissionPrefix, parentGUI);
		this.materialMap = materialMap;

		this.materialList = new ArrayList<>(this.materialMap.keySet());

		// Sort materials by their ordinal value.
		this.materialList.sort(Comparator.comparing(Enum::ordinal));

		this.selectedMaterials = new HashSet<>();
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * This method builds all necessary elements in GUI panel.
	 */
	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).
			name(this.user.getTranslation("challenges.gui.title.admin.manage-blocks"));

		// Create nice border.
		GuiUtils.fillBorder(panelBuilder);

		panelBuilder.item(3, this.createButton(Button.ADD));
		panelBuilder.item(5, this.createButton(Button.REMOVE));

		final int MAX_ELEMENTS = 21;

		if (this.pageIndex < 0)
		{
			this.pageIndex = this.materialList.size() / MAX_ELEMENTS;
		}
		else if (this.pageIndex > (this.materialList.size() / MAX_ELEMENTS))
		{
			this.pageIndex = 0;
		}

		int entitiesIndex = MAX_ELEMENTS * this.pageIndex;

		// I want first row to be only for navigation and return button.
		int index = 10;

		while (entitiesIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
			entitiesIndex < this.materialList.size() &&
			index < 36)
		{
			if (!panelBuilder.slotOccupied(index))
			{
				panelBuilder.item(index, this.createElementButton(this.materialList.get(entitiesIndex++)));
			}

			index++;
		}

		// Navigation buttons only if necessary
		if (this.materialList.size() > MAX_ELEMENTS)
		{
			panelBuilder.item(18, this.getButton(CommonButtons.PREVIOUS));
			panelBuilder.item(26, this.getButton(CommonButtons.NEXT));
		}

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
		int lineLength = this.addon.getChallengesSettings().getLoreLineLength();
		PanelItemBuilder builder = new PanelItemBuilder();

		switch (button)
		{
			case ADD:
				builder.name(this.user.getTranslation("challenges.gui.buttons.admin.add"));
				builder.icon(Material.BUCKET);
				builder.clickHandler((panel, user1, clickType, slot) -> {

					new SelectBlocksGUI(this.user, false, new HashSet<>(this.materialList), (status, materials) -> {
						if (status)
						{
							materials.forEach(material -> {
								this.materialMap.put(material, 1);
								this.materialList.add(material);
							});
						}

						this.build();
					});
					return true;
				});
				break;
			case REMOVE:
				builder.name(this.user.getTranslation("challenges.gui.buttons.admin.remove-selected"));
				builder.description(GuiUtils.stringSplit(this.user.getTranslation("challenges.gui.descriptions.admin.remove-selected"), lineLength));
				builder.icon(Material.LAVA_BUCKET);
				builder.clickHandler((panel, user1, clickType, slot) -> {
					this.materialMap.keySet().removeAll(this.selectedMaterials);
					this.materialList.removeAll(this.selectedMaterials);
					this.build();
					return true;
				});
				break;
		}

		return builder.build();
	}


	/**
	 * This method creates button for given material.
	 * @param material material which button must be created.
	 * @return new Button for material.
	 */
	private PanelItem createElementButton(Material material)
	{
		return new PanelItemBuilder().
			name(LangUtilsHook.getMaterialName(material, user)).
			icon(GuiUtils.getMaterialItem(material, this.materialMap.get(material))).
			description(this.selectedMaterials.contains(material) ?
				this.user.getTranslation("challenges.gui.descriptions.admin.selected") : "").
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
					new NumberGUI(this.user,
						this.materialMap.get(material),
						1,
						this.addon.getChallengesSettings().getLoreLineLength(),
						(status, value) -> {
							if (status)
							{
								// Update value only when something changes.
								this.materialMap.put(material, value);
							}

							this.build();
						});
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
		ADD,
		REMOVE
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * Contains selected materials.
	 */
	private Set<Material> selectedMaterials;

	/**
	 * List of materials to avoid order issues.
	 */
	private List<Material> materialList;

	/**
	 * List of required materials.
	 */
	private Map<Material, Integer> materialMap;
}
