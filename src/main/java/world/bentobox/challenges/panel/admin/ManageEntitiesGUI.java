package world.bentobox.challenges.panel.admin;


import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import java.util.*;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.panel.CommonGUI;
import world.bentobox.challenges.panel.util.NumberGUI;
import world.bentobox.challenges.panel.util.SelectEntityGUI;
import world.bentobox.challenges.utils.HeadLib;


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
			name(this.user.getTranslation("challenges.gui.admin.edit-entities"));

		// create border
		this.fillBorder(panelBuilder);

		panelBuilder.item(3, this.createButton(Button.ADD));
		panelBuilder.item(5, this.createButton(Button.REMOVE));

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
			entitiesIndex < this.entityList.size())
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
		PanelItemBuilder builder = new PanelItemBuilder();

		switch (button)
		{
			case ADD:
				builder.name(this.user.getTranslation("challenges.gui.button.add"));
				builder.icon(Material.BUCKET);
				builder.clickHandler((panel, user1, clickType, slot) -> {
					new SelectEntityGUI(this.user, (status, entity) -> {
						if (status)
						{
							if (!this.requiredEntities.containsKey(entity))
							{
								this.requiredEntities.put(entity, 1);
								this.entityList.add(entity);
							}
						}

						this.build();
					});
					return true;
				});
				break;
			case REMOVE:
				builder.name(this.user.getTranslation("challenges.gui.button.remove-selected"));
				builder.icon(Material.LAVA_BUCKET);
				builder.clickHandler((panel, user1, clickType, slot) -> {
					this.requiredEntities.keySet().removeAll(this.selectedEntities);
					this.entityList.removeAll(this.selectedEntities);
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
			name(WordUtils.capitalize(entity.name().toLowerCase().replace("_", " "))).
			icon(this.asEggs ? this.getEntityEgg(entity) : this.getEntityHead(entity)).
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
					new NumberGUI(this.user, this.requiredEntities.get(entity), 1, (status, value) -> {
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


	/**
	 * This method transforms entity into egg or block that corresponds given entity. If entity egg is not
	 * found, then it is replaced by block that represents entity or barrier block.
	 * @param entity Entity which egg must be returned.
	 * @return ItemStack that may be egg for given entity.
	 */
	private ItemStack getEntityEgg(EntityType entity)
	{
		ItemStack itemStack;

		switch (entity)
		{
			case PIG_ZOMBIE:
				itemStack = new ItemStack(Material.ZOMBIE_PIGMAN_SPAWN_EGG);
				break;
			case ENDER_DRAGON:
				itemStack = new ItemStack(Material.DRAGON_EGG);
				break;
			case WITHER:
				itemStack = new ItemStack(Material.SOUL_SAND);
				break;
			case PLAYER:
				itemStack = new ItemStack(Material.PLAYER_HEAD);
				break;
			case MUSHROOM_COW:
				itemStack = new ItemStack(Material.MOOSHROOM_SPAWN_EGG);
				break;
			case SNOWMAN:
				itemStack = new ItemStack(Material.CARVED_PUMPKIN);
				break;
			case IRON_GOLEM:
				itemStack = new ItemStack(Material.IRON_BLOCK);
				break;
			case ARMOR_STAND:
				itemStack = new ItemStack(Material.ARMOR_STAND);
				break;
			default:
				Material material = Material.getMaterial(entity.name() + "_SPAWN_EGG");

				if (material == null)
				{
					itemStack = new ItemStack(Material.BARRIER);
				}
				else
				{
					itemStack = new ItemStack(material);
				}

				break;
		}

		itemStack.setAmount(this.requiredEntities.get(entity));

		return itemStack;
	}


	/**
	 * This method transforms entity into player head with skin that corresponds given entity. If entity head
	 * is not found, then it is replaced by barrier block.
	 * @param entity Entity which head must be returned.
	 * @return ItemStack that may be head for given entity.
	 */
	private ItemStack getEntityHead(EntityType entity)
	{
		ItemStack itemStack;

		switch (entity)
		{
			case PLAYER:
				itemStack = new ItemStack(Material.PLAYER_HEAD);
				break;
			case WITHER_SKELETON:
				itemStack = new ItemStack(Material.WITHER_SKELETON_SKULL);
				break;
			case ARMOR_STAND:
				itemStack = new ItemStack(Material.ARMOR_STAND);
				break;
			case SKELETON:
				itemStack = new ItemStack(Material.SKELETON_SKULL);
				break;
			case GIANT:
			case ZOMBIE:
				itemStack = new ItemStack(Material.ZOMBIE_HEAD);
				break;
			case CREEPER:
				itemStack = new ItemStack(Material.CREEPER_HEAD);
				break;
			case ENDER_DRAGON:
				itemStack = new ItemStack(Material.DRAGON_HEAD);
				break;
			default:
				HeadLib head = HeadLib.getHead(entity.name());

				if (head == null)
				{
					itemStack = new ItemStack(Material.BARRIER);
				}
				else
				{
					itemStack = head.toItemStack(this.requiredEntities.get(entity));
				}
				break;
		}

		return itemStack;
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
