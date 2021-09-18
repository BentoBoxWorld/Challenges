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
import world.bentobox.challenges.database.object.requirements.*;
import world.bentobox.challenges.utils.Constants;


/**
 * This class creates GUI that allows to select challenge type.
 */
public record ChallengeTypeSelector(User user, BiConsumer<Challenge.ChallengeType, Requirements> consumer)
{
	/**
	 * This method opens GUI that allows to select challenge type.
	 *
	 * @param user User who opens GUI.
	 * @param consumer Consumer that allows to get clicked type.
	 */
	public static void open(User user, BiConsumer<Challenge.ChallengeType, Requirements> consumer)
	{
		new ChallengeTypeSelector(user, consumer).build();
	}


	/**
	 * This method builds GUI that allows to select challenge type.
	 */
	private void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().
			user(this.user).
			type(Panel.Type.HOPPER).
			name(this.user.getTranslation(Constants.TITLE + "type-selector"));

		panelBuilder.item(0, this.getButton(Challenge.ChallengeType.INVENTORY_TYPE));
		panelBuilder.item(1, this.getButton(Challenge.ChallengeType.ISLAND_TYPE));
		panelBuilder.item(2, this.getButton(Challenge.ChallengeType.OTHER_TYPE));
		panelBuilder.item(3, this.getButton(Challenge.ChallengeType.STATISTIC_TYPE));

		panelBuilder.build();
	}


	/**
	 * Creates ChallengeType button.
	 *
	 * @param type Challenge type which button must be created.
	 * @return PanelItem button.
	 */
	private PanelItem getButton(Challenge.ChallengeType type)
	{
		final String reference = Constants.BUTTON + type.name().toLowerCase() + ".";

		final String name = this.user.getTranslation(reference + "name");
		final List<String> description = new ArrayList<>(3);
		description.add(this.user.getTranslation(reference + "description"));
		description.add("");
		description.add(this.user.getTranslation(Constants.TIPS + "click-to-select"));

		ItemStack icon;
		PanelItem.ClickHandler clickHandler;

		switch (type)
		{
			case INVENTORY_TYPE -> {
				icon = new ItemStack(Material.CHEST);
				clickHandler = (
					(panel, user1, clickType, slot) ->
					{
						this.consumer.accept(type, new InventoryRequirements());
						return true;
					});
			}
			case ISLAND_TYPE -> {
				icon = new ItemStack(Material.GRASS_BLOCK);
				clickHandler = (
					(panel, user1, clickType, slot) ->
					{
						this.consumer.accept(type, new IslandRequirements());
						return true;
					});
			}
			case OTHER_TYPE -> {
				icon = new ItemStack(Material.EXPERIENCE_BOTTLE);
				clickHandler = (
					(panel, user1, clickType, slot) ->
					{
						this.consumer.accept(type, new OtherRequirements());
						return true;
					});
			}
			case STATISTIC_TYPE -> {
				icon = new ItemStack(Material.BOOK);
				clickHandler = (
					(panel, user1, clickType, slot) ->
					{
						this.consumer.accept(type, new StatisticRequirements());
						return true;
					});
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
}
