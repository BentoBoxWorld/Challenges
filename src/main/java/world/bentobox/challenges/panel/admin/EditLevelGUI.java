package world.bentobox.challenges.panel.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.wesjd.anvilgui.AnvilGUI;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.ChallengesManager;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.panel.CommonGUI;
import world.bentobox.challenges.panel.util.ItemSwitchGUI;
import world.bentobox.challenges.panel.util.NumberGUI;
import world.bentobox.challenges.panel.util.SelectChallengeGUI;
import world.bentobox.challenges.panel.util.StringListGUI;
import world.bentobox.challenges.utils.GuiUtils;


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
		ChallengeLevel challengeLevel,
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
		ChallengeLevel challengeLevel,
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
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
			this.user.getTranslation("challenges.gui.admin.edit-level-title"));

		GuiUtils.fillBorder(panelBuilder);

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

		panelBuilder.item(44, this.returnButton);

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
		panelBuilder.item(12, this.createButton(Button.REWARD_DESCRIPTION));
		panelBuilder.item(21, this.createButton(Button.REWARD_COMMANDS));

		panelBuilder.item(13, this.createButton(Button.REWARD_ITEM));
		panelBuilder.item(22, this.createButton(Button.REWARD_EXPERIENCE));
		panelBuilder.item(31, this.createButton(Button.REWARD_MONEY));
	}


	/**
	 * This class populate LevelsEditGUI with level challenges.
	 * @param panelBuilder PanelBuilder where icons must be added.
	 */
	private void buildChallengesPanel(PanelBuilder panelBuilder)
	{
		List<Challenge> challengeList = this.addon.getChallengesManager().getLevelChallenges(this.challengeLevel);

		final int MAX_ELEMENTS = 21;

		if (this.pageIndex < 0)
		{
			this.pageIndex = challengeList.size() / MAX_ELEMENTS;
		}
		else if (this.pageIndex > (challengeList.size() / MAX_ELEMENTS))
		{
			this.pageIndex = 0;
		}

		int challengeIndex = MAX_ELEMENTS * this.pageIndex;

		// I want first row to be only for navigation and return button.
		int index = 10;

		while (challengeIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
			challengeIndex < challengeList.size() &&
			index < 36)
		{
			if (!panelBuilder.slotOccupied(index))
			{
				panelBuilder.item(index, this.createChallengeIcon(challengeList.get(challengeIndex++)));
			}

			index++;
		}

		// Navigation buttons only if necessary
		if (challengeList.size() > MAX_ELEMENTS)
		{
			panelBuilder.item(18, this.getButton(CommonButtons.PREVIOUS));
			panelBuilder.item(26, this.getButton(CommonButtons.NEXT));
		}

		panelBuilder.item(39, this.createButton(Button.ADD_CHALLENGE));
		panelBuilder.item(41, this.createButton(Button.REMOVE_CHALLENGE));
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

		return new PanelItem(icon, name, GuiUtils.stringSplit(description), glow, clickHandler, false);
	}


	/**
	 * This method creates given challenge icon. On click it should open Edit Challenge GUI.
	 * @param challenge Challenge which icon must be created.
	 * @return PanelItem that represents given challenge.
	 */
	private PanelItem createChallengeIcon(Challenge challenge)
	{
		return new PanelItemBuilder().
			name(challenge.getFriendlyName()).
			description(GuiUtils.stringSplit(challenge.getDescription())).
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
	 * @return PanelItem that represents given button.
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
					new AnvilGUI(this.addon.getPlugin(),
						this.user.getPlayer(),
						this.challengeLevel.getFriendlyName(),
						(player, reply) -> {
							this.challengeLevel.setFriendlyName(reply);
							this.build();
							return reply;
						});

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
					new AnvilGUI(this.addon.getPlugin(),
						this.user.getPlayer(),
						this.challengeLevel.getIcon().getType().name(),
						(player, reply) -> {
							Material material = Material.getMaterial(reply);

							if (material != null)
							{
								this.challengeLevel.setIcon(new ItemStack(material));
								this.build();
							}
							else
							{
								this.user.sendMessage("challenges.errors.wrong-icon", "[value]", reply);
							}

							return reply;
						});

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
					new AnvilGUI(this.addon.getPlugin(),
						this.user.getPlayer(),
						this.challengeLevel.getUnlockMessage(),
						(player, reply) -> {
							this.challengeLevel.setUnlockMessage(reply);
							this.build();
							return reply;
						});
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
					new NumberGUI(this.user, this.challengeLevel.getOrder(), -1, 54, (status, value) -> {
						if (status)
						{
							this.challengeLevel.setOrder(value);
						}

						this.build();
					});

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
						Integer.toString(this.challengeLevel.getWaiverAmount())));
				icon = new ItemStack(Material.REDSTONE_TORCH);
				clickHandler = (panel, user, clickType, slot) -> {
					new NumberGUI(this.user, this.challengeLevel.getWaiverAmount(), 0, (status, value) -> {
						if (status)
						{
							this.challengeLevel.setWaiverAmount(value);
						}

						this.build();
					});

					return true;
				};
				glow = false;
				break;
			}

			case REWARD_DESCRIPTION:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.reward-text");
				description = Collections.singletonList(this.challengeLevel.getRewardText());
				icon = new ItemStack(Material.WRITTEN_BOOK);
				clickHandler = (panel, user, clickType, slot) -> {
					new AnvilGUI(this.addon.getPlugin(),
						this.user.getPlayer(),
						this.challengeLevel.getRewardText(),
						(player, reply) -> {
							this.challengeLevel.setRewardText(reply);
							this.build();
							return reply;
						});
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
					new ItemSwitchGUI(this.user, this.challengeLevel.getRewardItems(), (status, value) -> {
						if (status)
						{
							this.challengeLevel.setRewardItems(value);
						}

						this.build();
					});

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
						Integer.toString(this.challengeLevel.getRewardExperience())));
				icon = new ItemStack(Material.EXPERIENCE_BOTTLE);
				clickHandler = (panel, user, clickType, slot) -> {
					new NumberGUI(this.user, this.challengeLevel.getRewardExperience(), 0, (status, value) -> {
						if (status)
						{
							this.challengeLevel.setRewardExperience(value);
						}

						this.build();
					});

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
						Integer.toString(this.challengeLevel.getRewardMoney())));

				if (this.addon.isEconomyProvided())
				{
					icon = new ItemStack(Material.GOLD_INGOT);
					clickHandler = (panel, user, clickType, slot) -> {
						new NumberGUI(this.user, this.challengeLevel.getRewardMoney(), 0, (status, value) -> {
							if (status)
							{
								this.challengeLevel.setRewardMoney(value);
							}

							this.build();
						});

						return true;
					};
				}
				else
				{
					icon = new ItemStack(Material.BARRIER);
					clickHandler = null;
				}

				glow = false;
				break;
			}
			case REWARD_COMMANDS:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.reward-command");
				description = this.challengeLevel.getRewardCommands();
				icon = new ItemStack(Material.COMMAND_BLOCK);
				clickHandler = (panel, user, clickType, slot) -> {
					new StringListGUI(this.user, this.challengeLevel.getRewardCommands(), (status, value) -> {
						if (status)
						{
							this.challengeLevel.setRewardCommands(value);
						}

						this.build();
					});

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
					ChallengesManager manager = this.addon.getChallengesManager();

					// Get all challenge that is not in current challenge.
					List<Challenge> challengeList = manager.getAllChallenges(this.world);
					challengeList.removeAll(manager.getLevelChallenges(this.challengeLevel));

					new SelectChallengeGUI(this.user, challengeList, (status, value) -> {
						if (status)
						{
							manager.addChallengeToLevel(value, this.challengeLevel);
						}

						this.build();
					});

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
					ChallengesManager manager = this.addon.getChallengesManager();

					new SelectChallengeGUI(this.user, manager.getLevelChallenges(this.challengeLevel), (status, value) -> {
						if (status)
						{
							manager.removeChallengeFromLevel(value, this.challengeLevel);
						}

						this.build();
					});

					return true;
				};
				glow = false;
				break;
			}
			default:
				return null;
		}

		return new PanelItem(icon, name, GuiUtils.stringSplit(description), glow, clickHandler, false);
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
	private ChallengeLevel challengeLevel;

	/**
	 * Variable holds current active menu.
	 */
	private MenuType currentMenuType;
}