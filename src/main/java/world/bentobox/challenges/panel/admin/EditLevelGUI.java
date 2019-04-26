package world.bentobox.challenges.panel.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.stream.Collectors;

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
			this.user.getTranslation("challenges.gui.title.admin.edit-level-title"));

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

		// Save challenge level every time this gui is build.
		// It will ensure that changes are stored in database.
		this.addon.getChallengesManager().saveLevel(this.challengeLevel);

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
		panelBuilder.item(28, this.createButton(Button.CLOSED_ICON));
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
		String description;
		boolean glow;
		PanelItem.ClickHandler clickHandler;

		switch (menuType)
		{
			case PROPERTIES:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.properties");
				description = this.user.getTranslation("challenges.gui.descriptions.admin.properties");
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.challenges");
				description = this.user.getTranslation("challenges.gui.descriptions.admin.challenges");
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.rewards");
				description = this.user.getTranslation("challenges.gui.descriptions.admin.rewards");
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

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(GuiUtils.stringSplit(description, this.addon.getChallengesSettings().getLoreLineLength())).
			glow(glow).
			clickHandler(clickHandler).
			build();
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
			description(GuiUtils.stringSplit(
				challenge.getDescription(),
				this.addon.getChallengesSettings().getLoreLineLength())).
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

		final int lineLength = this.addon.getChallengesSettings().getLoreLineLength();

		switch (button)
		{
			case NAME:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.name");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.name-level"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]", this.challengeLevel.getFriendlyName()));
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.icon");
				description = Collections.singletonList(this.user.getTranslation(
					"challenges.gui.descriptions.admin.icon-level"));
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
			case CLOSED_ICON:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.locked-icon");
				description = Collections.singletonList(this.user.getTranslation(
					"challenges.gui.descriptions.admin.locked-icon"));

				boolean isNull = this.challengeLevel.getLockedIcon() == null;

				if (isNull)
				{
					icon = new ItemStack(Material.BARRIER);
				}
				else
				{
					icon = this.challengeLevel.getLockedIcon().clone();
				}

				clickHandler = (panel, user, clickType, slot) -> {
					new AnvilGUI(this.addon.getPlugin(),
						this.user.getPlayer(),
						isNull ? "NULL" : icon.getType().name(),
						(player, reply) -> {
							if (reply.equals("NULL"))
							{
								this.challengeLevel.setLockedIcon(null);
								this.build();
								return reply;
							}

							Material material = Material.getMaterial(reply);

							if (material != null)
							{
								this.challengeLevel.setLockedIcon(new ItemStack(material));
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.description");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.description"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]", "|" + this.challengeLevel.getUnlockMessage()));
				icon = new ItemStack(Material.WRITABLE_BOOK);
				clickHandler = (panel, user, clickType, slot) -> {
					new StringListGUI(this.user, this.challengeLevel.getUnlockMessage(), lineLength, (status, value) -> {
						if (status)
						{
							this.challengeLevel.setUnlockMessage(value.stream().map(s -> s + "|").collect(Collectors.joining()));
						}

						this.build();
					});

					return true;
				};
				glow = false;
				break;
			}
			case ORDER:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.order");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.order"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]", Integer.toString(this.challengeLevel.getOrder())));
				icon = new ItemStack(Material.DROPPER);
				clickHandler = (panel, user, clickType, slot) -> {
					new NumberGUI(this.user, this.challengeLevel.getOrder(), -1, 54, lineLength, (status, value) -> {
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.waiver-amount");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.waiver-amount"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]", Integer.toString(this.challengeLevel.getWaiverAmount())));

				icon = new ItemStack(Material.REDSTONE_TORCH);
				clickHandler = (panel, user, clickType, slot) -> {
					new NumberGUI(this.user, this.challengeLevel.getWaiverAmount(), 0, lineLength, (status, value) -> {
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.reward-text");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.reward-text-level"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]", "|" + this.challengeLevel.getRewardText()));
				icon = new ItemStack(Material.WRITTEN_BOOK);
				clickHandler = (panel, user, clickType, slot) -> {
					new StringListGUI(this.user, this.challengeLevel.getRewardText(), lineLength, (status, value) -> {
						if (status)
						{
							this.challengeLevel.setRewardText(value.stream().map(s -> s + "|").collect(Collectors.joining()));
						}

						this.build();
					});

					return true;
				};
				glow = false;
				break;
			}
			case REWARD_ITEM:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.reward-items");

				description = new ArrayList<>(this.challengeLevel.getRewardItems().size() + 1);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.reward-items"));

				for (ItemStack itemStack : this.challengeLevel.getRewardItems())
				{
					description.add(this.user.getTranslation("challenges.gui.descriptions.item",
						"[item]", itemStack.getType().name(),
						"[count]", Integer.toString(itemStack.getAmount())));

					if (itemStack.hasItemMeta() && itemStack.getEnchantments().isEmpty())
					{
						description.add(this.user.getTranslation("challenges.gui.descriptions.item-meta",
							"[meta]", itemStack.getItemMeta().toString()));
					}

					for (Map.Entry<Enchantment, Integer> entry : itemStack.getEnchantments().entrySet())
					{
						description.add(this.user.getTranslation("challenges.gui.descriptions.item-enchant",
							"[enchant]", entry.getKey().getKey().getKey(), "[level]", Integer.toString(entry.getValue())));
					}
				}

				icon = new ItemStack(Material.CHEST);
				clickHandler = (panel, user, clickType, slot) -> {
					new ItemSwitchGUI(this.user, this.challengeLevel.getRewardItems(), lineLength, (status, value) -> {
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.reward-experience");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.reward-experience"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]", Integer.toString(this.challengeLevel.getRewardExperience())));
				icon = new ItemStack(Material.EXPERIENCE_BOTTLE);
				clickHandler = (panel, user, clickType, slot) -> {
					new NumberGUI(this.user, this.challengeLevel.getRewardExperience(), 0, lineLength, (status, value) -> {
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.reward-money");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.reward-money"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]", Integer.toString(this.challengeLevel.getRewardMoney())));

				if (this.addon.isEconomyProvided())
				{
					icon = new ItemStack(Material.GOLD_INGOT);
					clickHandler = (panel, user, clickType, slot) -> {
						new NumberGUI(this.user, this.challengeLevel.getRewardMoney(), 0, lineLength, (status, value) -> {
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.reward-commands");
				description = new ArrayList<>(this.challengeLevel.getRewardCommands().size() + 1);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.reward-commands"));

				for (String command : this.challengeLevel.getRewardCommands())
				{
					description.add(this.user.getTranslation("challenges.gui.descriptions.command",
						"[command]", command));
				}

				icon = new ItemStack(Material.COMMAND_BLOCK);
				clickHandler = (panel, user, clickType, slot) -> {
					new StringListGUI(this.user, this.challengeLevel.getRewardCommands(), lineLength, (status, value) -> {
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.add-challenge");
				description = Collections.singletonList(this.user.getTranslation("challenges.gui.descriptions.admin.add-challenge"));
				icon = new ItemStack(Material.WATER_BUCKET);
				clickHandler = (panel, user, clickType, slot) -> {
					ChallengesManager manager = this.addon.getChallengesManager();

					// Get all challenge that is not in current level.
					List<Challenge> challengeList = manager.getAllChallenges(this.world);
					challengeList.removeAll(manager.getLevelChallenges(this.challengeLevel));

					// Generate descriptions for these challenges
					Map<Challenge, List<String>> challengeDescriptionMap = challengeList.stream().
						collect(Collectors.toMap(challenge -> challenge,
							challenge -> this.generateChallengeDescription(challenge, this.user.getPlayer()),
							(a, b) -> b,
							() -> new LinkedHashMap<>(challengeList.size())));

					// Open select gui
					new SelectChallengeGUI(this.user, challengeDescriptionMap, lineLength, (status, valueSet) -> {
						if (status)
						{
							valueSet.forEach(challenge -> manager.addChallengeToLevel(challenge, this.challengeLevel));
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.remove-challenge");
				description = Collections.singletonList(this.user.getTranslation("challenges.gui.descriptions.admin.remove-challenge"));
				icon = new ItemStack(Material.LAVA_BUCKET);
				clickHandler = (panel, user, clickType, slot) -> {
					ChallengesManager manager = this.addon.getChallengesManager();

					// Get all challenge that is in current level.
					List<Challenge> challengeList = manager.getLevelChallenges(this.challengeLevel);

					// Generate descriptions for these challenges
					Map<Challenge, List<String>> challengeDescriptionMap = challengeList.stream().
						collect(Collectors.toMap(challenge -> challenge,
							challenge -> this.generateChallengeDescription(challenge, this.user.getPlayer()),
							(a, b) -> b,
							() -> new LinkedHashMap<>(challengeList.size())));

					// Open select gui
					new SelectChallengeGUI(this.user, challengeDescriptionMap, lineLength, (status, valueSet) -> {
						if (status)
						{
							valueSet.forEach(challenge -> manager.removeChallengeFromLevel(challenge, this.challengeLevel));
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



		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(GuiUtils.stringSplit(description, lineLength)).
			glow(glow).
			clickHandler(clickHandler).
			build();
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
		CLOSED_ICON,
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