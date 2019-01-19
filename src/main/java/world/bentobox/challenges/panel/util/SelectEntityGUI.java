package world.bentobox.challenges.panel.util;


import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.function.BiConsumer;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.HeadLib;


/**
 * This GUI allows to select single entity and return it via Consumer.
 */
public class SelectEntityGUI
{
	public SelectEntityGUI(User user, BiConsumer<Boolean, EntityType> consumer)
	{
		this(user, Collections.emptySet(), true, consumer);
	}


	public SelectEntityGUI(User user, Set<EntityType> excludedEntities, boolean asEggs, BiConsumer<Boolean, EntityType> consumer)
	{
		this.consumer = consumer;
		this.user = user;
		this.asEggs = asEggs;

		this.entities = new ArrayList<>(EntityType.values().length);

		for (EntityType entityType : EntityType.values())
		{
			if (entityType.isAlive() && !excludedEntities.contains(entityType))
			{
				this.entities.add(entityType);
			}
		}

		// Sort mobs by their name for easier search.
		this.entities.sort(Comparator.comparing(Enum::name));

		this.build(0);
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * This method builds
	 */
	private void build(int pageIndex)
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(this.user.getTranslation("challenges.gui.choose-entity-title"));

		// Maximal elements in page.
		final int MAX_ELEMENTS = 36;

		final int correctPage;

		if (pageIndex < 0)
		{
			correctPage = this.entities.size() / MAX_ELEMENTS;
		}
		else if (pageIndex > (this.entities.size() / MAX_ELEMENTS))
		{
			correctPage = 0;
		}
		else
		{
			correctPage = pageIndex;
		}

		// Navigation buttons

		panelBuilder.item(3,
			new PanelItemBuilder().
				icon(Material.SIGN).
				name(this.user.getTranslation("challenges.gui.buttons.previous")).
				clickHandler( (panel, user1, clickType, slot) -> {
					this.build(correctPage - 1);
					return true;
				}).build());

		panelBuilder.item(4,
			new PanelItemBuilder().
				icon(Material.OAK_DOOR).
				name(this.user.getTranslation("challenges.gui.buttons.return")).
				clickHandler( (panel, user1, clickType, slot) -> {
					this.consumer.accept(false, null);
					return true;
				}).build());

		panelBuilder.item(5,
			new PanelItemBuilder().
				icon(Material.SIGN).
				name(this.user.getTranslation("challenges.gui.buttons.next")).
				clickHandler( (panel, user1, clickType, slot) -> {
					this.build(correctPage + 1);
					return true;
				}).build());

		int entitiesIndex = MAX_ELEMENTS * correctPage;

		// I want first row to be only for navigation and return button.
		int index = 9;

		while (entitiesIndex < ((correctPage + 1) * MAX_ELEMENTS) &&
			entitiesIndex < this.entities.size())
		{
			panelBuilder.item(index++, this.createEntityButton(this.entities.get(entitiesIndex++)));
		}

		panelBuilder.build();

		panelBuilder.build();
	}


	/**
	 * This method builds PanelItem for given entity.
	 * @param entity Entity which PanelItem must be created.
	 * @return new PanelItem for given Entity.
	 */
	private PanelItem createEntityButton(EntityType entity)
	{
		return new PanelItemBuilder().
			name(WordUtils.capitalize(entity.name().toLowerCase().replace("_", " "))).
			icon(this.asEggs ? this.getEntityEgg(entity) : this.getEntityHead(entity)).
			clickHandler((panel, user1, clickType, slot) -> {
				this.consumer.accept(true, entity);
				return true;
			}).build();
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
					itemStack = head.toItemStack();
				}
				break;
		}

		return itemStack;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * This variable stores consumer.
	 */
	private BiConsumer<Boolean, EntityType> consumer;

	/**
	 * User who runs GUI.
	 */
	private User user;

	/**
	 * This variable stores if mobs must be displayed as Eggs "true" or Heads "false".
	 */
	private boolean asEggs;

	/**
	 * Entities that must be in list.
	 */
	private List<EntityType> entities;
}
