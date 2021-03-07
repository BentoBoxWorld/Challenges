package world.bentobox.challenges.panel.util;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.hooks.LangUtilsHook;
import world.bentobox.challenges.utils.GuiUtils;


/**
 * This GUI allows to select single entity and return it via Consumer.
 */
public class SelectEntityGUI
{
	public SelectEntityGUI(User user, BiConsumer<Boolean, Set<EntityType>> consumer)
	{
		this(user, Collections.emptySet(), true, consumer);
	}


	public SelectEntityGUI(User user, Set<EntityType> excludedEntities, boolean asEggs, BiConsumer<Boolean, Set<EntityType>> consumer)
	{
		this.consumer = consumer;
		this.user = user;
		this.asEggs = asEggs;

		this.entities = new ArrayList<>(EntityType.values().length);
		this.selectedEntities = new HashSet<>(EntityType.values().length);

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
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(this.user.getTranslation("challenges.gui.title.admin.select-entity"));

		GuiUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

		// Maximal elements in page.
		final int MAX_ELEMENTS = 21;

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

		panelBuilder.item(3,
			new PanelItemBuilder().
				icon(Material.RED_STAINED_GLASS_PANE).
				name(this.user.getTranslation("challenges.gui.buttons.admin.cancel")).
				clickHandler( (panel, user1, clickType, slot) -> {
					this.consumer.accept(false, null);
					return true;
				}).build());

		List<String> description = new ArrayList<>();
		if (!this.selectedEntities.isEmpty())
		{
			description.add(this.user.getTranslation("challenges.gui.descriptions.admin.selected") + ":");
			this.selectedEntities.forEach(entity -> description.add(" - " + LangUtilsHook.getEntityName(entity, user)));
		}

		panelBuilder.item(5,
			new PanelItemBuilder().
				icon(Material.GREEN_STAINED_GLASS_PANE).
				name(this.user.getTranslation("challenges.gui.buttons.admin.accept")).
				description(description).
				clickHandler( (panel, user1, clickType, slot) -> {
					this.consumer.accept(true, this.selectedEntities);
					return true;
				}).build());

		if (this.entities.size() > MAX_ELEMENTS)
		{
			// Navigation buttons if necessary

			panelBuilder.item(18,
				new PanelItemBuilder().
					icon(Material.OAK_SIGN).
					name(this.user.getTranslation("challenges.gui.buttons.previous")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.build(correctPage - 1);
						return true;
					}).build());

			panelBuilder.item(26,
				new PanelItemBuilder().
					icon(Material.OAK_SIGN).
					name(this.user.getTranslation("challenges.gui.buttons.next")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.build(correctPage + 1);
						return true;
					}).build());
		}

		int entitiesIndex = MAX_ELEMENTS * correctPage;

		// I want first row to be only for navigation and return button.
		int slot = 10;

		while (entitiesIndex < ((correctPage + 1) * MAX_ELEMENTS) &&
			entitiesIndex < this.entities.size() &&
			slot < 36)
		{
			if (!panelBuilder.slotOccupied(slot))
			{
				panelBuilder.item(slot,
					this.createEntityButton(this.entities.get(entitiesIndex++)));
			}

			slot++;
		}

		panelBuilder.item(44,
			new PanelItemBuilder().
				icon(Material.OAK_DOOR).
				name(this.user.getTranslation("challenges.gui.buttons.return")).
				clickHandler( (panel, user1, clickType, i) -> {
					this.consumer.accept(false, null);
					return true;
				}).build());

		panelBuilder.build();
	}


	/**
	 * This method builds PanelItem for given entity.
	 * @param entity Entity which PanelItem must be created.
	 * @return new PanelItem for given Entity.
	 */
	private PanelItem createEntityButton(EntityType entity)
	{
		ItemStack itemStack = this.asEggs ? GuiUtils.getEntityEgg(entity) : GuiUtils.getEntityHead(entity);

		return new PanelItemBuilder().
			name(LangUtilsHook.getEntityName(entity, user)).
			icon(itemStack).
			description(this.selectedEntities.contains(entity) ?
				this.user.getTranslation("challenges.gui.descriptions.admin.selected") : "").
			clickHandler((panel, user1, clickType, slot) -> {
				if (clickType.isRightClick())
				{
					if (!this.selectedEntities.add(entity))
					{
						this.selectedEntities.remove(entity);
					}

					panel.getInventory().setItem(slot, this.createEntityButton(entity).getItem());
				}
				else
				{
					this.selectedEntities.add(entity);
					this.consumer.accept(true, this.selectedEntities);
				}

				return true;
			}).
			glow(this.selectedEntities.contains(entity)).
			build();
	}



// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * This variable stores consumer.
	 */
	private BiConsumer<Boolean, Set<EntityType>> consumer;

	/**
	 * Set that contains selected entities.
	 */
	private Set<EntityType> selectedEntities;

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
