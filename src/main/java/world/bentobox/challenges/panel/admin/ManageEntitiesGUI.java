package world.bentobox.challenges.panel.admin;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.hooks.LangUtilsHook;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.panel.CommonGUI;
import world.bentobox.challenges.panel.util.NumberGUI;
import world.bentobox.challenges.panel.util.SelectEntityGUI;
import world.bentobox.challenges.utils.GuiUtils;


/**
 * This class allows to edit entities that are in required entities map.
 */
public class ManageEntitiesGUI extends CommonGUI
{
	public ManageEntitiesGUI(ChallengesAddon addon,
		World world,
		User user,
		Map<EntityType, Integer> requiredEntities,
		String topLabel,
		String permissionPrefix,
		CommonGUI parentGUI)
	{
		super(addon, world, user, topLabel, permissionPrefix, parentGUI);
		this.requiredEntities = requiredEntities;

		this.entityList = new ArrayList<>(this.requiredEntities.keySet());
		this.entityList.sort(Comparator.comparing(Enum::name));

		this.selectedEntities = new HashSet<>(EntityType.values().length);
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
			name(this.user.getTranslation("challenges.gui.title.admin.manage-entities"));

		// create border
		GuiUtils.fillBorder(panelBuilder);

		panelBuilder.item(3, this.createButton(Button.ADD));
		panelBuilder.item(5, this.createButton(Button.REMOVE));
		panelBuilder.item(8, this.createButton(Button.SWITCH));

		final int MAX_ELEMENTS = 21;

		if (this.pageIndex < 0)
		{
			this.pageIndex = this.entityList.size() / MAX_ELEMENTS;
		}
		else if (this.pageIndex > (this.entityList.size() / MAX_ELEMENTS))
		{
			this.pageIndex = 0;
		}

		int entitiesIndex = MAX_ELEMENTS * this.pageIndex;

		// I want first row to be only for navigation and return button.
		int index = 10;

		while (entitiesIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
			entitiesIndex < this.entityList.size() &&
			index < 26)
		{
			if (!panelBuilder.slotOccupied(index))
			{
				panelBuilder.item(index, this.createEntityButton(this.entityList.get(entitiesIndex++)));
			}

			index++;
		}

		// Navigation buttons only if necessary
		if (this.entityList.size() > MAX_ELEMENTS)
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
					new SelectEntityGUI(this.user, this.requiredEntities.keySet(), this.asEggs, (status, entities) -> {
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
				});
				break;
			case REMOVE:
				builder.name(this.user.getTranslation("challenges.gui.buttons.admin.remove-selected"));
				builder.description(GuiUtils.stringSplit(this.user.getTranslation("challenges.gui.descriptions.admin.remove-selected"), lineLength));
				builder.icon(Material.LAVA_BUCKET);
				builder.clickHandler((panel, user1, clickType, slot) -> {
					this.requiredEntities.keySet().removeAll(this.selectedEntities);
					this.entityList.removeAll(this.selectedEntities);
					this.build();
					return true;
				});
				break;
			case SWITCH:
				builder.name(this.user.getTranslation("challenges.gui.buttons.admin.show-eggs"));
				builder.description(GuiUtils.stringSplit(this.user.getTranslation("challenges.gui.descriptions.admin.show-eggs"), lineLength));
				builder.icon(this.asEggs ? Material.EGG : Material.PLAYER_HEAD);
				builder.clickHandler((panel, user1, clickType, slot) -> {
					this.asEggs = !this.asEggs;
					this.build();
					return true;
				});
				break;
		}

		return builder.build();
	}


	/**
	 * This method creates button for given entity.
	 * @param entity Entity which button must be created.
	 * @return new Button for entity.
	 */
	private PanelItem createEntityButton(EntityType entity)
	{
		return new PanelItemBuilder().
			name(LangUtilsHook.getEntityName(entity, user)).
			description(this.selectedEntities.contains(entity) ?
				this.user.getTranslation("challenges.gui.descriptions.admin.selected") : "").
			icon(this.asEggs ?
				GuiUtils.getEntityEgg(entity, this.requiredEntities.get(entity)) :
				GuiUtils.getEntityHead(entity, this.requiredEntities.get(entity))).
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
					new NumberGUI(this.user,
						this.requiredEntities.get(entity),
						1,
						this.addon.getChallengesSettings().getLoreLineLength(),
						(status, value) -> {
							if (status)
							{
								// Update value only when something changes.
								this.requiredEntities.put(entity, value);
							}

							this.build();
						});
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
		ADD,
		REMOVE,
		SWITCH
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * List with entities to avoid list irregularities.
	 */
	private List<EntityType> entityList;

	/**
	 * Set with entities that are selected.
	 */
	private Set<EntityType> selectedEntities;

	/**
	 * Map that contains all entities and their cound.
	 */
	private Map<EntityType, Integer> requiredEntities;

	/**
	 * Boolean indicate if entities should be displayed as eggs or mob heads.
	 */
	private boolean asEggs;
}
