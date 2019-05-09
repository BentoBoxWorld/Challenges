package world.bentobox.challenges.panel.util;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import java.util.*;
import java.util.function.BiConsumer;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.utils.GuiUtils;


/**
 * This class creates new GUI that allows to select single challenge, which is returned via consumer.
 */
public class SelectChallengeGUI
{
	public SelectChallengeGUI(User user, Map<Challenge, List<String>> challengesDescriptionMap, int lineLength, BiConsumer<Boolean, Set<Challenge>> consumer)
	{
		this.consumer = consumer;
		this.user = user;
		this.challengesList = new ArrayList<>(challengesDescriptionMap.keySet());
		this.challengesDescriptionMap = challengesDescriptionMap;
		this.lineLength = lineLength;
		this.selectedChallenges = new HashSet<>(this.challengesList.size());

		this.build(0);
	}


	/**
	 * This method builds panel that allows to select single challenge from input challenges.
	 */
	private void build(int pageIndex)
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(this.user.getTranslation("challenges.gui.title.admin.select-challenge"));

		GuiUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

		// Maximal elements in page.
		final int MAX_ELEMENTS = 21;

		final int correctPage;

		if (pageIndex < 0)
		{
			correctPage = this.challengesList.size() / MAX_ELEMENTS;
		}
		else if (pageIndex > (this.challengesList.size() / MAX_ELEMENTS))
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
				name(this.user.getTranslation("challenges.gui.buttons.return")).
				clickHandler( (panel, user1, clickType, slot) -> {
					this.consumer.accept(false, null);
					return true;
				}).build());

		if (this.challengesList.size() > MAX_ELEMENTS)
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

		int challengesIndex = MAX_ELEMENTS * correctPage;

		// I want first row to be only for navigation and return button.
		int index = 10;

		while (challengesIndex < ((correctPage + 1) * MAX_ELEMENTS) &&
			challengesIndex < this.challengesList.size() &&
			index < 36)
		{
			if (!panelBuilder.slotOccupied(index))
			{
				panelBuilder.item(index,
					this.createChallengeButton(this.challengesList.get(challengesIndex++)));
			}

			index++;
		}

		panelBuilder.item(44,
			new PanelItemBuilder().
				icon(Material.OAK_DOOR).
				name(this.user.getTranslation("challenges.gui.buttons.return")).
				clickHandler( (panel, user1, clickType, slot) -> {
					this.consumer.accept(false, null);
					return true;
				}).build());

		panelBuilder.build();
	}


	/**
	 * This method builds PanelItem for given challenge.
	 * @param challenge Challenge which PanelItem must be created.
	 * @return new PanelItem for given Challenge.
	 */
	private PanelItem createChallengeButton(Challenge challenge)
	{
		List<String> description;

		if (this.selectedChallenges.contains(challenge))
		{
			description = new ArrayList<>();
			description.add(this.user.getTranslation("challenges.gui.descriptions.admin.selected"));
			description.addAll(this.challengesDescriptionMap.get(challenge));
		}
		else
		{
			description = this.challengesDescriptionMap.get(challenge);
		}


		return new PanelItemBuilder().
			name(ChatColor.translateAlternateColorCodes('&', challenge.getFriendlyName())).
			description(GuiUtils.stringSplit(description, this.lineLength)).
			icon(challenge.getIcon()).
			clickHandler((panel, user1, clickType, slot) -> {
				if (clickType == ClickType.RIGHT)
				{
					// If challenge is not selected, then select :)
					if (!this.selectedChallenges.remove(challenge))
					{
						this.selectedChallenges.add(challenge);
					}

					// Reset button.
					panel.getInventory().setItem(slot, this.createChallengeButton(challenge).getItem());
				}
				else
				{
					this.selectedChallenges.add(challenge);
					this.consumer.accept(true, this.selectedChallenges);
				}

				return true;
			}).
			glow(this.selectedChallenges.contains(challenge)).
			build();
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * This variable stores consumer.
	 */
	private BiConsumer<Boolean, Set<Challenge>> consumer;

	/**
	 * User who runs GUI.
	 */
	private User user;

	/**
	 * Current value.
	 */
	private List<Challenge> challengesList;

	/**
	 * Selected challenges that will be returned to consumer.
	 */
	private Set<Challenge> selectedChallenges;

	/**
	 * Map that contains all challenge descriptions
	 */
	private Map<Challenge, List<String>> challengesDescriptionMap;

	/**
	 * This variable stores how large line can be, before warp it.
	 */
	private int lineLength;
}
