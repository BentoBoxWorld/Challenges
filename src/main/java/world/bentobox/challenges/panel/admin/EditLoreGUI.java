package world.bentobox.challenges.panel.admin;


import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.PanelListener;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.config.SettingsUtils.ChallengeLore;
import world.bentobox.challenges.config.SettingsUtils.LevelLore;
import world.bentobox.challenges.panel.CommonGUI;
import world.bentobox.challenges.utils.GuiUtils;


/**
 * This class allows to change Input ItemStacks to different ItemStacks.
 */
public class EditLoreGUI extends CommonGUI
{
	public EditLoreGUI(CommonGUI parent, LoreType loreType)
	{
		super(parent);

		this.lore = loreType;
		this.activeValues = new ArrayList<>();

		switch (this.lore)
		{
			case CHALLENGES:

				for (ChallengeLore lore : this.addon.getChallengesSettings().getChallengeLoreMessage())
				{
					this.activeValues.add(lore.name());
				}

				break;
			case LEVELS:

				for (LevelLore lore : this.addon.getChallengesSettings().getLevelLoreMessage())
				{
					this.activeValues.add(lore.name());
				}

				break;
		}
	}


	/**
	 * This is static call method for easier GUI opening.
	 * @param parent Parent GUI.
	 * @param loreType loreType that will be edited.
	 */
	public static void open(CommonGUI parent, LoreType loreType)
	{
		new EditLoreGUI(parent, loreType).build();
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * This method builds panel that allows to change given number value.
	 */
	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().
			name(this.user.getTranslation("challenges.gui.title.admin.lore-edit")).
			user(this.user).
			listener(new CustomPanelListener());

		GuiUtils.fillBorder(panelBuilder, 5, Material.MAGENTA_STAINED_GLASS_PANE);

		// Define all active buttons
		panelBuilder.item(1, this.getButton(Button.SAVE));

		panelBuilder.item(3, this.getButton(Button.ADD));
		panelBuilder.item(4, this.getButton(Button.REMOVE));

		// TODO: Need 2 View Buttons
		// One for closes / One for opened.
//		panelBuilder.item(6, this.getButton(Button.VIEW));

		panelBuilder.item(44, this.returnButton);

		// necessary as I have a border around this GUI
		int currentIndex = 10;

		// Only 21 elements will be displayed. On porpoise!
		for (int i = 0; i < this.activeValues.size() || i > 21; i++)
		{
			panelBuilder.item(currentIndex++, this.getLoreButton(this.activeValues.get(i)));

			// Border element
			if (currentIndex % 9 == 8)
			{
				currentIndex += 2;
			}

			// Just in case. Should never occur.
			if (currentIndex % 9 == 0)
			{
				currentIndex++;
			}
		}

		panelBuilder.build();
	}


	/**
	 * This method create button that does some functionality in current gui.
	 * @param button Button functionality.
	 * @return PanelItem.
	 */
	private PanelItem getButton(Button button)
	{
		ItemStack icon;
		String name;
		List<String> description;
		PanelItem.ClickHandler clickHandler;

		switch (button)
		{
			case SAVE:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.save");
				description = Collections.emptyList();
				icon = new ItemStack(Material.COMMAND_BLOCK);
				clickHandler = (panel, user, clickType, slot) -> {

					switch (this.lore)
					{
						case CHALLENGES:
						{
							List<ChallengeLore> lore = this.activeValues.stream().
								map(ChallengeLore::valueOf).
								collect(Collectors.toCollection(() -> new ArrayList<>(this.activeValues.size())));

							this.addon.getChallengesSettings().setChallengeLoreMessage(lore);

							break;
						}
						case LEVELS:
						{
							List<LevelLore> lore = this.activeValues.stream().
								map(LevelLore::valueOf).
								collect(Collectors.toCollection(() -> new ArrayList<>(this.activeValues.size())));

							this.addon.getChallengesSettings().setLevelLoreMessage(lore);

							break;
						}
					}

					// Save and return to parent gui.
					this.parentGUI.build();

					return true;
				};
				break;
			}
			case ADD:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.add");
				description = Collections.emptyList();
				icon = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
				clickHandler = (panel, user, clickType, slot) -> {
					new AddLoreElementGUI(element -> {
						this.activeValues.add(element);
						this.build();
					});

					return true;
				};

				break;
			}
			case REMOVE:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.remove-selected");
				description = Collections.emptyList();
				icon = new ItemStack(Material.RED_STAINED_GLASS_PANE);
				clickHandler = (panel, user, clickType, slot) -> {
					new RemoveLoreElementGUI((element, index) -> {
						if (this.activeValues.get(index).equals(element))
						{
							this.activeValues.remove(element);
						}

						this.build();
					});

					return true;
				};

				break;
			}
			case VIEW:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.view");
				description = Collections.emptyList();
				icon = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
				clickHandler = (panel, user, clickType, slot) -> {
					return true;
				};

				break;
			}
			default:
				return null;
		}

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(GuiUtils.stringSplit(description, this.addon.getChallengesSettings().getLoreLineLength())).
			glow(false).
			clickHandler(clickHandler).
			build();
	}


	/**
	 * This method creates button for lore element.
	 * @param loreElement String that represents current lore element.
	 * @return PanelItem.
	 */
	private PanelItem getLoreButton(String loreElement)
	{
		switch (this.lore)
		{
			case CHALLENGES:
				return this.getChallengeLoreButton(loreElement);
			case LEVELS:
				return this.getLevelLoreButton(loreElement);
			default:
				// this should never happen!
				return null;
		}
	}


	/**
	 * This method creates button for challenge lore element.
	 * @param loreElement String that represents current challenge lore element.
	 * @return PanelItem.
	 */
	private PanelItem getChallengeLoreButton(String loreElement)
	{
		Material icon;
		String name = loreElement;
		List<String> description = new ArrayList<>();
		description.add(this.user.getTranslation(REFERENCE_DESCRIPTION + "lore." + loreElement.toLowerCase()));

		PanelItem.ClickHandler clickHandler = (panel, user1, clickType, slot) -> true;

		switch (ChallengeLore.valueOf(loreElement))
		{
			case LEVEL:
			{
				icon = Material.DIRT;
				break;
			}
			case STATUS:
			{
				icon = Material.LEVER;
				break;
			}
			case COUNT:
			{
				icon = Material.REPEATER;
				break;
			}
			case DESCRIPTION:
			{
				icon = Material.WRITTEN_BOOK;
				break;
			}
			case WARNINGS:
			{
				icon = Material.LAVA_BUCKET;
				break;
			}
			case ENVIRONMENT:
			{
				icon = Material.GLASS;
				break;
			}
			case REQUIREMENTS:
			{
				icon = Material.HOPPER;
				break;
			}
			case REWARD_TEXT:
			{
				icon = Material.PAPER;
				break;
			}
			case REWARD_OTHER:
			{
				icon = Material.CHEST;
				break;
			}
			case REWARD_ITEMS:
			{
				icon = Material.TRAPPED_CHEST;
				break;
			}
			case REWARD_COMMANDS:
			{
				icon = Material.COMMAND_BLOCK;
				break;
			}
			default:
			{
				icon = Material.BARRIER;
				break;
			}
		}

		return new PanelItemBuilder().
			name(name).
			icon(icon).
			description(description).
			clickHandler(clickHandler).
			glow(false).
			build();
	}


	/**
	 * This method creates button for challenge level lore element.
	 * @param loreElement String that represents current challenge level lore element.
	 * @return PanelItem.
	 */
	private PanelItem getLevelLoreButton(String loreElement)
	{
		Material icon;
		String name = loreElement;
		List<String> description = new ArrayList<>();
		description.add(this.user.getTranslation(REFERENCE_DESCRIPTION + "lore." + loreElement.toLowerCase()));

		PanelItem.ClickHandler clickHandler = (panel, user1, clickType, slot) -> true;

		switch (LevelLore.valueOf(loreElement))
		{
			case LEVEL_STATUS:
			{
				icon = Material.DIRT;
				break;
			}
			case CHALLENGE_COUNT:
			{
				icon = Material.REPEATER;
				break;
			}
			case UNLOCK_MESSAGE:
			{
				icon = Material.WRITTEN_BOOK;
				break;
			}
			case WAIVER_AMOUNT:
			{
				icon = Material.COMPARATOR;
				break;
			}
			case LEVEL_REWARD_TEXT:
			{
				icon = Material.PAPER;
				break;
			}
			case LEVEL_REWARD_OTHER:
			{
				icon = Material.CHEST;
				break;
			}
			case LEVEL_REWARD_ITEMS:
			{
				icon = Material.TRAPPED_CHEST;
				break;
			}
			case LEVEL_REWARD_COMMANDS:
			{
				icon = Material.COMMAND_BLOCK;
				break;
			}
			default:
			{
				icon = Material.BARRIER;
				break;
			}
		}

		return new PanelItemBuilder().
			name(name).
			icon(icon).
			description(description).
			clickHandler(clickHandler).
			glow(false).
			build();
	}


// ---------------------------------------------------------------------
// Section: Select GUI
// ---------------------------------------------------------------------


	/**
	 * This class opens new GUI that add an element from all available lore values.
	 */
	private class AddLoreElementGUI
	{
		private AddLoreElementGUI(Consumer<String> selectedElement)
		{
			PanelBuilder panelBuilder = new PanelBuilder().
				name(EditLoreGUI.this.user.getTranslation("challenges.gui.title.admin.lore-add")).
				user(EditLoreGUI.this.user);

			GuiUtils.fillBorder(panelBuilder, 5, Material.MAGENTA_STAINED_GLASS_PANE);

			int currentIndex = 10;

			List<String> values = new ArrayList<>();

			// Populate list with all elements.
			switch (EditLoreGUI.this.lore)
			{
				case CHALLENGES:
					for (ChallengeLore value : ChallengeLore.values())
					{
						values.add(value.name());
					}
					break;
				case LEVELS:
					for (LevelLore value : LevelLore.values())
					{
						values.add(value.name());
					}
					break;
			}

			for (String value : values)
			{
				PanelItem item = EditLoreGUI.this.getLoreButton(value);

				item.setClickHandler((panel, user1, clickType, slot) -> {
					selectedElement.accept(value);
					return true;
				});

				panelBuilder.item(currentIndex++, item);

				// Border element
				if (currentIndex % 9 == 8)
				{
					currentIndex += 2;
				}

				// Just in case. Should never occur.
				if (currentIndex % 9 == 0)
				{
					currentIndex++;
				}

				// Just in case. Should never occur.
				if (currentIndex > 35)
				{
					break;
				}
			}

			panelBuilder.build();
		}
	}


	/**
	 * This class opens new GUI that remove an element from all available lore values.
	 */
	private class RemoveLoreElementGUI
	{
		private RemoveLoreElementGUI(BiConsumer<String, Integer> selectedElement)
		{
			PanelBuilder panelBuilder = new PanelBuilder().
				name(EditLoreGUI.this.user.getTranslation("challenges.gui.title.admin.lore-remove")).
				user(EditLoreGUI.this.user);

			GuiUtils.fillBorder(panelBuilder, 5, Material.MAGENTA_STAINED_GLASS_PANE);

			int currentIndex = 10;

			List<String> values = EditLoreGUI.this.activeValues;

			for (int i = 0; i < values.size(); i++)
			{
				final int counter = i;

				String value = values.get(counter);
				PanelItem item = EditLoreGUI.this.getLoreButton(value);

				item.setClickHandler((panel, user1, clickType, slot) -> {
					selectedElement.accept(value, counter);
					return true;
				});

				panelBuilder.item(currentIndex++, item);

				// Border element
				if (currentIndex % 9 == 8)
				{
					currentIndex += 2;
				}

				// Just in case. Should never occur.
				if (currentIndex % 9 == 0)
				{
					currentIndex++;
				}

				// Just in case. Should never occur.
				if (currentIndex > 35)
				{
					break;
				}
			}

			panelBuilder.build();
		}
	}


// ---------------------------------------------------------------------
// Section: Private classes
// ---------------------------------------------------------------------


	/**
	 * This CustomPanelListener allows to move items in current panel.
	 */
	private class CustomPanelListener implements PanelListener
	{
		@Override
		public void setup()
		{
		}


		@Override
		public void onInventoryClose(InventoryCloseEvent inventoryCloseEvent)
		{
		}


		@Override
		public void onInventoryClick(User user, InventoryClickEvent event)
		{
			// First row of elements should be ignored, as it contains buttons and blocked slots.
			event.setCancelled(event.getRawSlot() < 9 ||
				event.getRawSlot() < 35 ||
				event.getRawSlot() % 9 == 0 ||
				event.getRawSlot() % 9 == 8);
		}
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * This enum holds all button values in current gui.
	 */
	private enum Button
	{
		SAVE,
		ADD,
		REMOVE,
		VIEW,
		RETURN
	}


	/**
	 * This enum holds which Lore is edited with current GUI.
	 */
	public enum LoreType
	{
		CHALLENGES,
		LEVELS,
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * Lore that will be edited with current GUI.
	 */
	private final LoreType lore;

	/**
	 * List of lore elements that are currently enabled.
	 */
	private List<String> activeValues;


// ---------------------------------------------------------------------
// Section: Constants
// ---------------------------------------------------------------------


	private final static String REFERENCE_DESCRIPTION = "challenges.gui.descriptions.admin.";
}
