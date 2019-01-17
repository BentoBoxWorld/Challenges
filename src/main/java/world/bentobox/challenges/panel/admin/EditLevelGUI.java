package world.bentobox.challenges.panel.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.ChallengeLevels;
import world.bentobox.challenges.database.object.Challenges;
import world.bentobox.challenges.panel.CommonGUI;


/**
 * This class contains all necessary elements to create Levels Edit GUI.
 */
public class EditLevelGUI extends CommonGUI
{
// ---------------------------------------------------------------------
// Section: Constructors
// ---------------------------------------------------------------------


	/**
	 * {@inheritDoc}
	 * @param challengeLevel ChallengeLevel that must be edited.
	 */
	public EditLevelGUI(ChallengesAddon addon,
		World world,
		User user,
		ChallengeLevels challengeLevel,
		String topLabel,
		String permissionPrefix)
	{
		this(addon, world, user, challengeLevel, topLabel, permissionPrefix, null);
	}


	/**
	 * {@inheritDoc}
	 * @param challengeLevel ChallengeLevel that must be edited.
	 */
	public EditLevelGUI(ChallengesAddon addon,
		World world,
		User user,
		ChallengeLevels challengeLevel,
		String topLabel,
		String permissionPrefix,
		CommonGUI parentGUI)
	{
		super(addon, world, user, topLabel, permissionPrefix, parentGUI);
		this.challengeLevel = challengeLevel;
		this.currentMenuType = MenuType.PROPERTIES;
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
		PanelBuilder panelBuilder = new PanelBuilder().name(
			this.user.getTranslation("challenges.gui.admin.edit-level-title"));

		panelBuilder.item(2, this.createMenuButton(MenuType.PROPERTIES));
		panelBuilder.item(4, this.createMenuButton(MenuType.REWARDS));
		panelBuilder.item(6, this.createMenuButton(MenuType.CHALLENGES));

		if (this.currentMenuType.equals(MenuType.PROPERTIES))
		{
			this.buildMainPropertiesPanel(panelBuilder);
		}
		else if (this.currentMenuType.equals(MenuType.CHALLENGES))
		{
			this.buildChallengesPanel(panelBuilder);
		}
		else if (this.currentMenuType.equals(MenuType.REWARDS))
		{
			this.buildRewardsPanel(panelBuilder);
		}

		panelBuilder.item(53, this.returnButton);

		panelBuilder.build();
	}


	/**
	 * This class populate LevelsEditGUI with main level settings.
	 * @param panelBuilder PanelBuilder where icons must be added.
	 */
	private void buildMainPropertiesPanel(PanelBuilder panelBuilder)
	{
		panelBuilder.item(10, this.createButton(Button.NAME));

		panelBuilder.item(19, this.createButton(Button.ICON));
		panelBuilder.item(22, this.createButton(Button.UNLOCK_MESSAGE));
		panelBuilder.item(25, this.createButton(Button.ORDER));

		panelBuilder.item(31, this.createButton(Button.WAIVER_AMOUNT));
	}


	/**
	 * This class populate LevelsEditGUI with level rewards.
	 * @param panelBuilder PanelBuilder where icons must be added.
	 */
	private void buildRewardsPanel(PanelBuilder panelBuilder)
	{
		panelBuilder.item(11, this.createButton(Button.REWARD_DESCRIPTION));
		panelBuilder.item(20, this.createButton(Button.REWARD_ITEM));
		panelBuilder.item(29, this.createButton(Button.REWARD_EXPERIENCE));
		panelBuilder.item(38, this.createButton(Button.REWARD_MONEY));
		panelBuilder.item(47, this.createButton(Button.REWARD_COMMANDS));
	}


	/**
	 * This class populate LevelsEditGUI with level challenges.
	 * @param panelBuilder PanelBuilder where icons must be added.
	 */
	private void buildChallengesPanel(PanelBuilder panelBuilder)
	{
		List<Challenges> challenges = this.addon.getChallengesManager().getChallenges(this.challengeLevel);

		if (this.pageIndex < 0)
		{
			this.pageIndex = 0;
		}
		else if (this.pageIndex > (challenges.size() / 18))
		{
			this.pageIndex = challenges.size() / 18;
		}

		int challengeIndex = 18 * this.pageIndex;
		int elementIndex = 9;

		while (challengeIndex < ((this.pageIndex + 1) * 18) &&
			challengeIndex < challenges.size())
		{
			panelBuilder.item(elementIndex++, this.createChallengeIcon(challenges.get(challengeIndex)));
			challengeIndex++;
		}

		if (this.pageIndex > 0)
		{
			panelBuilder.item(29, this.getButton(CommonButtons.PREVIOUS));
		}

		if (challengeIndex < challenges.size())
		{
			panelBuilder.item(33, this.getButton(CommonButtons.NEXT));
		}

		panelBuilder.item(30, this.createButton(Button.ADD_CHALLENGE));
		panelBuilder.item(32, this.createButton(Button.REMOVE_CHALLENGE));
	}


// ---------------------------------------------------------------------
// Section: Other methods
// ---------------------------------------------------------------------


	/**
	 * This method creates top menu buttons, that allows to switch "tabs".
	 * @param menuType Menu Type which button must be constructed.
	 * @return PanelItem that represents given menu type.
	 */
	private PanelItem createMenuButton(MenuType menuType)
	{
		ItemStack icon;
		String name;
		List<String> description;
		boolean glow;
		PanelItem.ClickHandler clickHandler;

		switch (menuType)
		{
			case PROPERTIES:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.properties");
				description = Collections.emptyList();
				icon = new ItemStack(Material.CRAFTING_TABLE);
				clickHandler = (panel, user, clickType, slot) -> {
					this.currentMenuType = MenuType.PROPERTIES;
					this.build();

					return true;
				};
				glow = this.currentMenuType.equals(MenuType.PROPERTIES);
				break;
			}
			case CHALLENGES:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.challenges");
				description = Collections.emptyList();
				icon = new ItemStack(Material.RAIL);
				clickHandler = (panel, user, clickType, slot) -> {
					this.currentMenuType = MenuType.CHALLENGES;
					this.build();

					return true;
				};
				glow = this.currentMenuType.equals(MenuType.CHALLENGES);
				break;
			}
			case REWARDS:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.rewards");
				description = Collections.emptyList();
				icon = new ItemStack(Material.DROPPER);
				clickHandler = (panel, user, clickType, slot) -> {
					this.currentMenuType = MenuType.REWARDS;
					this.build();

					return true;
				};
				glow = this.currentMenuType.equals(MenuType.REWARDS);
				break;
			}
			default:
				return null;
		}

		return new PanelItem(icon, name, description, glow, clickHandler, false);
	}


	/**
	 * This method creates given challenge icon. On click it should open Edit Challenge GUI.
	 * @param challenge Challenge which icon must be created.
	 * @return PanelItem that represents given challenge.
	 */
	private PanelItem createChallengeIcon(Challenges challenge)
	{
		return new PanelItemBuilder().
			name(challenge.getFriendlyName()).
			description(challenge.getDescription()).
			icon(challenge.getIcon()).
			clickHandler((panel, user1, clickType, slot) -> {
				// Open challenges edit screen.
				new EditChallengeGUI(this.addon,
					this.world,
					this.user,
					challenge,
					this.topLabel,
					this.permissionPrefix,
					this).build();
				return true;
			}).
			glow(!challenge.isDeployed()).
			build();

	}


	/**
	 * This method creates buttons for default main menu.
	 * @param button Button which panel item must be created.
	 * @return PanelItem that represetns given button.
	 */
	private PanelItem createButton(Button button)
	{
		ItemStack icon;
		String name;
		List<String> description;
		boolean glow;
		PanelItem.ClickHandler clickHandler;

		switch (button)
		{
			case NAME:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.name");
				description = Collections.singletonList(this.challengeLevel.getFriendlyName());
				icon = new ItemStack(Material.DROPPER);
				clickHandler = (panel, user, clickType, slot) -> {
					// TODO: Create naming
					this.build();

					return true;
				};
				glow = false;
				break;
			}
			case ICON:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.icon");
				description = Collections.emptyList();
				icon = this.challengeLevel.getIcon();
				clickHandler = (panel, user, clickType, slot) -> {
					// TODO: how to change icon.
					this.build();

					return true;
				};
				glow = false;
				break;
			}
			case UNLOCK_MESSAGE:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.description");
				description = Collections.singletonList(this.challengeLevel.getUnlockMessage());
				icon = new ItemStack(Material.WRITABLE_BOOK);
				clickHandler = (panel, user, clickType, slot) -> {
					// TODO: Implement challenges description change GUI.
					this.build();
					return true;
				};
				glow = false;
				break;
			}
			case ORDER:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.order");
				description = Collections.singletonList(
					this.user.getTranslation("challenges.gui.admin.descriptions.order",
						"[value]",
						Integer.toString(this.challengeLevel.getOrder())));
				icon = new ItemStack(Material.DROPPER);
				clickHandler = (panel, user, clickType, slot) -> {
					// TODO: Possibility to change order.
					this.build();

					return true;
				};
				glow = false;
				break;
			}
			case WAIVER_AMOUNT:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.waiver-amount");
				description = Collections.singletonList(
					this.user.getTranslation("challenges.gui.admin.descriptions.waiver-amount",
						"[value]",
						Integer.toString(this.challengeLevel.getWaiveramount())));
				icon = new ItemStack(Material.REDSTONE_TORCH);
				clickHandler = (panel, user, clickType, slot) -> {
					// TODO: Possibility to change order.
					this.build();

					return true;
				};
				glow = false;
				break;
			}

			case REWARD_DESCRIPTION:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.reward-text");
				description = Collections.singletonList(this.challengeLevel.getRewardDescription());
				icon = new ItemStack(Material.WRITTEN_BOOK);
				clickHandler = (panel, user, clickType, slot) -> {
					// TODO: Implement challenges description change GUI.
					this.build();
					return true;
				};
				glow = false;
				break;
			}
			case REWARD_ITEM:
			{
				List<String> values = new ArrayList<>(this.challengeLevel.getRewardItems().size());

				for (ItemStack itemStack : this.challengeLevel.getRewardItems())
				{
					values.add(itemStack.getType().name() + " " + itemStack.getAmount());
				}

				name = this.user.getTranslation("challenges.gui.admin.buttons.reward-items");
				description = values;
				icon = new ItemStack(Material.CHEST);
				clickHandler = (panel, user, clickType, slot) -> {
					// TODO: Create Panel
					this.build();

					return true;
				};
				glow = false;
				break;
			}
			case REWARD_EXPERIENCE:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.reward-exp");
				description = Collections.singletonList(
					this.user.getTranslation("challenges.gui.admin.descriptions.reward-exp",
						"[value]",
						Integer.toString(this.challengeLevel.getExpReward())));
				icon = new ItemStack(Material.EXPERIENCE_BOTTLE);
				clickHandler = (panel, user, clickType, slot) -> {
					// TODO: Possibility to change order.
					this.build();

					return true;
				};
				glow = false;
				break;
			}
			case REWARD_MONEY:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.reward-money");
				description = Collections.singletonList(
					this.user.getTranslation("challenges.gui.admin.descriptions.reward-money",
						"[value]",
						Integer.toString(this.challengeLevel.getMoneyReward())));
				icon = new ItemStack(Material.GOLD_INGOT);
				clickHandler = (panel, user, clickType, slot) -> {
					// TODO: Possibility to change order.
					this.build();

					return true;
				};
				glow = false;
				break;
			}
			case REWARD_COMMANDS:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.reward-command");
				description = this.challengeLevel.getRewardCommands();
				icon = new ItemStack(Material.COMMAND_BLOCK);
				clickHandler = (panel, user, clickType, slot) -> {
					// TODO: Create naming
					this.build();

					return true;
				};
				glow = false;
				break;
			}

			case ADD_CHALLENGE:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.add-challenge");
				description = Collections.emptyList();
				icon = new ItemStack(Material.WATER_BUCKET);
				clickHandler = (panel, user, clickType, slot) -> {
					// TODO: Create naming
					this.build();

					return true;
				};
				glow = false;
				break;
			}
			case REMOVE_CHALLENGE:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.remove-challenge");
				description = Collections.emptyList();
				icon = new ItemStack(Material.LAVA_BUCKET);
				clickHandler = (panel, user, clickType, slot) -> {
					// TODO: Create naming
					this.build();

					return true;
				};
				glow = false;
				break;
			}
			default:
				return null;
		}

		return new PanelItem(icon, name, description, glow, clickHandler, false);
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * Represents different buttons that could be in menus.
	 */
	private enum Button
	{
		NAME,
		ICON,
		UNLOCK_MESSAGE,
		ORDER,
		WAIVER_AMOUNT,

		REWARD_DESCRIPTION,
		REWARD_ITEM,
		REWARD_EXPERIENCE,
		REWARD_MONEY,
		REWARD_COMMANDS,

		ADD_CHALLENGE,
		REMOVE_CHALLENGE
	}


	/**
	 * Represents different types of menus
	 */
	private enum MenuType
	{
		PROPERTIES,
		CHALLENGES,
		REWARDS
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * This variable holds current challenge level that is in editing GUI.
	 */
	private ChallengeLevels challengeLevel;

	/**
	 * Variable holds current active menu.
	 */
	private MenuType currentMenuType;
}
