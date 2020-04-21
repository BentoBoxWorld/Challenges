//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.challenges.panel.util;


import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.requirements.InventoryRequirements;
import world.bentobox.challenges.database.object.requirements.IslandRequirements;
import world.bentobox.challenges.database.object.requirements.OtherRequirements;
import world.bentobox.challenges.database.object.requirements.Requirements;
import world.bentobox.challenges.utils.GuiUtils;


/**
 * This class creates GUI that allows to select challenge type.
 */
public class ChallengeTypeGUI
{
	/**
	 * Default constructor that builds gui.
	 * @param user User who opens GUI.
	 * @param lineLength Lore line length
	 * @param consumer Consumer that allows to get clicked type.
	 */
	private ChallengeTypeGUI(User user, int lineLength, BiConsumer<Challenge.ChallengeType, Requirements> consumer)
	{
		this.user = user;
		this.lineLength = lineLength;
		this.consumer = consumer;
	}


	/**
	 * This method opens GUI that allows to select challenge type.
	 * @param user User who opens GUI.
	 * @param lineLength Lore line length
	 * @param consumer Consumer that allows to get clicked type.
	 */
	public static void open(User user, int lineLength, BiConsumer<Challenge.ChallengeType, Requirements> consumer)
	{
		new ChallengeTypeGUI(user, lineLength, consumer).build();
	}


	/**
	 * This method builds GUI that allows to select challenge type.
	 */
	private void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().
			user(this.user).
			type(Panel.Type.HOPPER).
			name(this.user.getTranslation("challenges.gui.title.admin.type-select"));

		panelBuilder.item(0, this.getButton(Challenge.ChallengeType.INVENTORY));
		panelBuilder.item(1, this.getButton(Challenge.ChallengeType.ISLAND));
		panelBuilder.item(2, this.getButton(Challenge.ChallengeType.OTHER));

		panelBuilder.build();
	}


	/**
	 * Creates ChallengeType button.
	 * @param type Challenge type which button must be created.
	 * @return PanelItem button.
	 */
	private PanelItem getButton(Challenge.ChallengeType type)
	{
		ItemStack icon;
		String name = this.user.getTranslation("challenges.gui.buttons.admin.type." + type.name().toLowerCase());
		List<String> description = new ArrayList<>();
		description.add(this.user.getTranslation("challenges.gui.descriptions.type." + type.name().toLowerCase()));
		PanelItem.ClickHandler clickHandler;

		switch (type)
		{
			case INVENTORY:
				icon = new ItemStack(Material.CHEST);
				clickHandler = ((panel, user1, clickType, slot) -> {
					this.consumer.accept(type, new InventoryRequirements());
					return true;
				});
				break;
			case ISLAND:
				icon = new ItemStack(Material.GRASS_BLOCK);
				clickHandler = ((panel, user1, clickType, slot) -> {
					this.consumer.accept(type, new IslandRequirements());
					return true;
				});
				break;
			case OTHER:
				icon = new ItemStack(Material.EXPERIENCE_BOTTLE);
				clickHandler = ((panel, user1, clickType, slot) -> {
					this.consumer.accept(type, new OtherRequirements());
					return true;
				});
				break;
			default:
				return null;
		}

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(GuiUtils.stringSplit(description, this.lineLength)).
			clickHandler(clickHandler).
			build();
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * User who runs GUI.
	 */
	private final User user;

	/**
	 * Lore line max length.
	 */
	private final int lineLength;

	/**
	 * Consumer that returns Challenge Type.
	 */
	private final BiConsumer<Challenge.ChallengeType, Requirements> consumer;
}
