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
import world.bentobox.challenges.utils.GuiUtils;


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

		panelBuilder.item(4,
			new PanelItemBuilder().
				icon(Material.RED_STAINED_GLASS_PANE).
				name(this.user.getTranslation("challenges.gui.buttons.cancel")).
				clickHandler( (panel, user1, clickType, slot) -> {
					this.consumer.accept(false, null);
					return true;
				}).build());

		if (this.entities.size() > MAX_ELEMENTS)
		{
			// Navigation buttons if necessary

			panelBuilder.item(18,
				new PanelItemBuilder().
					icon(Material.SIGN).
					name(this.user.getTranslation("challenges.gui.buttons.previous")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.build(correctPage - 1);
						return true;
					}).build());

			panelBuilder.item(26,
				new PanelItemBuilder().
					icon(Material.SIGN).
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
			name(WordUtils.capitalize(entity.name().toLowerCase().replace("_", " "))).
			icon(itemStack).
			clickHandler((panel, user1, clickType, slot) -> {
				this.consumer.accept(true, entity);
				return true;
			}).build();
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
