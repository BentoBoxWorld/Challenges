package world.bentobox.challenges.panel.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import java.util.*;

import net.wesjd.anvilgui.AnvilGUI;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.ItemParser;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.Challenges;
import world.bentobox.challenges.panel.CommonGUI;
import world.bentobox.challenges.panel.util.ItemSwitchGUI;
import world.bentobox.challenges.panel.util.NumberGUI;
import world.bentobox.challenges.panel.util.SelectEnvironmentGUI;
import world.bentobox.challenges.panel.util.StringListGUI;
import world.bentobox.challenges.utils.GuiUtils;


/**
 * This class contains all necessary methods that creates GUI and allow to edit challenges
 * properties.
 */
public class EditChallengeGUI extends CommonGUI
{
// ---------------------------------------------------------------------
// Section: Constructors
// ---------------------------------------------------------------------


	/**
	 * {@inheritDoc}
	 * @param challenge challenge that needs editing.
	 */
	public EditChallengeGUI(ChallengesAddon addon,
		World world,
		User user,
		Challenges challenge,
		String topLabel,
		String permissionPrefix)
	{
		this(addon, world, user, challenge, topLabel, permissionPrefix, null);
	}


	/**
	 * {@inheritDoc}
	 * @param challenge challenge that needs editing.
	 */
	public EditChallengeGUI(ChallengesAddon addon,
		World world,
		User user,
		Challenges challenge,
		String topLabel,
		String permissionPrefix,
		CommonGUI parentGUI)
	{
		super(addon, world, user, topLabel, permissionPrefix, parentGUI);
		this.challenge = challenge;

		// Default panel should be Properties.
		this.currentMenuType = MenuType.PROPERTIES;
	}


// ---------------------------------------------------------------------
// Section: Panel Creation related methods
// ---------------------------------------------------------------------


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
			this.user.getTranslation("challenges.gui.admin.edit-challenge-title"));

		GuiUtils.fillBorder(panelBuilder);

		panelBuilder.item(2, this.createMenuButton(MenuType.PROPERTIES));
		panelBuilder.item(4, this.createMenuButton(MenuType.REQUIREMENTS));
		panelBuilder.item(6, this.createMenuButton(MenuType.REWARDS));

		if (this.currentMenuType.equals(MenuType.PROPERTIES))
		{
			this.buildMainPropertiesPanel(panelBuilder);
		}
		else if (this.currentMenuType.equals(MenuType.REQUIREMENTS))
		{
			switch (this.challenge.getChallengeType())
			{
				case INVENTORY:
					this.buildInventoryRequirementsPanel(panelBuilder);
					break;
				case ISLAND:
					this.buildIslandRequirementsPanel(panelBuilder);
					break;
				case LEVEL:
					this.buildOtherRequirementsPanel(panelBuilder);
					break;
			}
		}
		else if (this.currentMenuType.equals(MenuType.REWARDS))
		{
			this.buildRewardsPanel(panelBuilder);
		}

		panelBuilder.item(44, this.returnButton);

		panelBuilder.build();
	}


	/**
	 * This class populate ChallengesEditGUI with main challenge settings.
	 * @param panelBuilder PanelBuilder where icons must be added.
	 */
	private void buildMainPropertiesPanel(PanelBuilder panelBuilder)
	{
		panelBuilder.item(10, this.createButton(Button.NAME));
		panelBuilder.item(13, this.createButton(Button.TYPE));
		panelBuilder.item(16, this.createButton(Button.DEPLOYED));

		panelBuilder.item(19, this.createButton(Button.ICON));
		panelBuilder.item(22, this.createButton(Button.DESCRIPTION));
		panelBuilder.item(25, this.createButton(Button.ORDER));

		panelBuilder.item(28, this.createButton(Button.ENVIRONMENT));
		panelBuilder.item(31, this.createButton(Button.REMOVE_ON_COMPLETE));
	}


	/**
	 * This class populates ChallengesEditGUI with island challenges requirement elements.
	 * @param panelBuilder PanelBuilder where icons must be added.
	 */
	private void buildIslandRequirementsPanel(PanelBuilder panelBuilder)
	{
		panelBuilder.item(19, this.createButton(Button.REQUIRED_ENTITIES));
		panelBuilder.item(28, this.createButton(Button.REMOVE_ENTITIES));

		panelBuilder.item(21, this.createButton(Button.REQUIRED_BLOCKS));
		panelBuilder.item(29, this.createButton(Button.REMOVE_BLOCKS));

		panelBuilder.item(23, this.createButton(Button.SEARCH_RADIUS));
		panelBuilder.item(25, this.createButton(Button.REQUIRED_PERMISSIONS));
	}


	/**
	 * This class populates ChallengesEditGUI with inventory challenges requirement elements.
	 * @param panelBuilder PanelBuilder where icons must be added.
	 */
	private void buildInventoryRequirementsPanel(PanelBuilder panelBuilder)
	{
		panelBuilder.item(10, this.createButton(Button.REQUIRED_ITEMS));
		panelBuilder.item(19, this.createButton(Button.REMOVE_ITEMS));

		panelBuilder.item(25, this.createButton(Button.REQUIRED_PERMISSIONS));
	}


	/**
	 * This class populates ChallengesEditGUI with other challenges requirement elements.
	 * @param panelBuilder PanelBuilder where icons must be added.
	 */
	private void buildOtherRequirementsPanel(PanelBuilder panelBuilder)
	{
		panelBuilder.item(10, this.createButton(Button.REQUIRED_EXPERIENCE));
		panelBuilder.item(19, this.createButton(Button.REMOVE_EXPERIENCE));

		panelBuilder.item(12, this.createButton(Button.REQUIRED_MONEY));
		panelBuilder.item(21, this.createButton(Button.REMOVE_MONEY));

		panelBuilder.item(23, this.createButton(Button.REQUIRED_LEVEL));

		panelBuilder.item(25, this.createButton(Button.REQUIRED_PERMISSIONS));
	}


	/**
	 * This class populates ChallengesEditGUI with challenges reward elements.
	 * @param panelBuilder PanelBuilder where icons must be added.
	 */
	private void buildRewardsPanel(PanelBuilder panelBuilder)
	{
		panelBuilder.item(10, this.createButton(Button.REWARD_TEXT));
		panelBuilder.item(19, this.createButton(Button.REWARD_COMMANDS));

		panelBuilder.item(11, this.createButton(Button.REWARD_ITEM));
		panelBuilder.item(20, this.createButton(Button.REWARD_EXPERIENCE));
		panelBuilder.item(29, this.createButton(Button.REWARD_MONEY));

		panelBuilder.item(22, this.createButton(Button.REPEATABLE));

		if (this.challenge.isRepeatable())
		{
			panelBuilder.item(31, this.createButton(Button.REPEAT_COUNT));

			panelBuilder.item(15, this.createButton(Button.REPEAT_REWARD_TEXT));
			panelBuilder.item(24, this.createButton(Button.REPEAT_REWARD_COMMANDS));

			panelBuilder.item(16, this.createButton(Button.REPEAT_REWARD_ITEM));
			panelBuilder.item(25, this.createButton(Button.REPEAT_REWARD_EXPERIENCE));
			panelBuilder.item(34, this.createButton(Button.REPEAT_REWARD_MONEY));
		}
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
			case REQUIREMENTS:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.requirements");
				description = Collections.emptyList();
				icon = new ItemStack(Material.HOPPER);
				clickHandler = (panel, user, clickType, slot) -> {
					this.currentMenuType = MenuType.REQUIREMENTS;
					this.build();

					return true;
				};
				glow = this.currentMenuType.equals(MenuType.REQUIREMENTS);
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
			case TYPE:
			{
				List<String> values = new ArrayList<>(Challenges.ChallengeType.values().length);

				for (Challenges.ChallengeType type : Challenges.ChallengeType.values())
				{
					values.add((this.challenge.getChallengeType().equals(type) ? "§2" : "§c") +
						this.user.getTranslation("challenges.gui.admin.descriptions." + type.name().toLowerCase()));
				}

				name = this.user.getTranslation("challenges.gui.admin.buttons.type");
				description = values;

				if (this.challenge.getChallengeType().equals(Challenges.ChallengeType.ISLAND))
				{
					icon = new ItemStack(Material.GRASS_BLOCK);
				}
				else if (this.challenge.getChallengeType().equals(Challenges.ChallengeType.INVENTORY))
				{
					icon = new ItemStack(Material.CHEST);
				}
				else if (this.challenge.getChallengeType().equals(Challenges.ChallengeType.LEVEL))
				{
					icon = new ItemStack(Material.EXPERIENCE_BOTTLE);
				}
				else
				{
					icon = this.challenge.getIcon();
				}

				clickHandler = (panel, user, clickType, slot) -> {
					if (clickType.isRightClick())
					{
						this.challenge.setChallengeType(
							this.getPreviousType(this.challenge.getChallengeType()));
					}
					else
					{
						this.challenge.setChallengeType(
							this.getNextType(this.challenge.getChallengeType()));
					}

					this.build();

					return true;
				};
				glow = false;
				break;
			}
			case DEPLOYED:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.deployed");

				if (this.challenge.isDeployed())
				{
					description = Collections.singletonList(this.user.getTranslation("challenges.gui.admin.descriptions.enabled"));
				}
				else
				{
					description = Collections.singletonList(this.user.getTranslation("challenges.gui.admin.descriptions.disabled"));
				}

				icon = new ItemStack(Material.LEVER);
				clickHandler = (panel, user, clickType, slot) -> {
					this.challenge.setDeployed(!this.challenge.isDeployed());

					this.build();
					return true;
				};
				glow = this.challenge.isDeployed();
				break;
			}
			case ICON:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.icon");
				description = Collections.emptyList();
				icon = this.challenge.getIcon();
				clickHandler = (panel, user, clickType, slot) -> {
					new AnvilGUI(this.addon.getPlugin(),
						this.user.getPlayer(),
						this.challenge.getIcon().getType().name(),
						(player, reply) -> {
							ItemStack newIcon = ItemParser.parse(reply);

							if (newIcon != null)
							{
								this.challenge.setIcon(newIcon);
							}
							else
							{
								this.user.sendMessage("challenges.errors.wrong-icon", "[value]", reply);
							}

							this.build();
							return reply;
						});

					return true;
				};
				glow = false;
				break;
			}
			case DESCRIPTION:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.description");
				description = Collections.emptyList();
				icon = new ItemStack(Material.WRITTEN_BOOK);
				clickHandler = (panel, user, clickType, slot) -> {
					new StringListGUI(this.user, this.challenge.getDescription(), (status, value) -> {
						if (status)
						{
							this.challenge.setDescription(value);
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
				name = this.user.getTranslation("challenges.gui.admin.buttons.order");
				description = Collections.singletonList(
					this.user.getTranslation("challenges.gui.admin.descriptions.order",
						"[value]",
						Integer.toString(this.challenge.getSlot())));
				icon = new ItemStack(Material.DROPPER);
				clickHandler = (panel, user, clickType, slot) -> {
					new NumberGUI(this.user, this.challenge.getSlot(), -1, 54, (status, value) -> {
						if (status)
						{
							this.challenge.setSlot(value);
						}

						this.build();
					});

					return true;
				};
				glow = false;
				break;
			}
			case ENVIRONMENT:
			{
				List<String> values = new ArrayList<>(World.Environment.values().length);

				for (World.Environment environment : World.Environment.values())
				{
					values.add((this.challenge.getEnvironment().contains(environment.name()) ? "§2" : "§c") +
						this.user.getTranslation("challenges.gui.admin.descriptions." + environment.name()));
				}

				name = this.user.getTranslation("challenges.gui.admin.buttons.environment");
				description = values;
				icon = new ItemStack(Material.DROPPER);
				clickHandler = (panel, user, clickType, slot) -> {
					new SelectEnvironmentGUI(this.user, this.challenge.getEnvironment(), (status, value) -> {
						if (status)
						{
							this.challenge.setEnvironment(value);
						}

						this.build();
					});

					return true;
				};
				glow = false;
				break;
			}
			case REMOVE_ON_COMPLETE:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.remove-on-complete");
				description = Collections.emptyList();

				if (this.challenge.isRemoveWhenCompleted())
				{
					icon = new ItemStack(Material.LAVA_BUCKET);
				}
				else
				{
					icon = new ItemStack(Material.BUCKET);
				}

				clickHandler = (panel, user, clickType, slot) -> {
					this.challenge.setRemoveWhenCompleted(!this.challenge.isRemoveWhenCompleted());
					this.build();

					return true;
				};
				glow = this.challenge.isRemoveWhenCompleted();
				break;
			}
			case NAME:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.name");
				description = Collections.emptyList();
				icon = new ItemStack(Material.DROPPER);
				clickHandler = (panel, user, clickType, slot) -> {
					new AnvilGUI(this.addon.getPlugin(),
						this.user.getPlayer(),
						this.challenge.getFriendlyName(),
						(player, reply) -> {
							this.challenge.setFriendlyName(reply);
							this.build();
							return reply;
						});

					return true;
				};
				glow = false;
				break;
			}

			case REQUIRED_ENTITIES:
			{
				List<String> values = new ArrayList<>(this.challenge.getRequiredEntities().size());

				for (Map.Entry<EntityType, Integer> entry : this.challenge.getRequiredEntities().entrySet())
				{
					values.add(entry.getKey().name() + " " + entry.getValue());
				}

				name = this.user.getTranslation("challenges.gui.admin.buttons.entities");
				description = values;
				icon = new ItemStack(Material.CREEPER_HEAD);
				clickHandler = (panel, user, clickType, slot) -> {
					new ManageEntitiesGUI(this.addon,
						this.world,
						this.user,
						this.challenge.getRequiredEntities(),
						this.topLabel,
						this.permissionPrefix,
						this).build();

					return true;
				};
				glow = false;
				break;
			}
			case REMOVE_ENTITIES:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.remove-entities");

				if (this.challenge.isRemoveEntities())
				{
					description = Collections.singletonList(this.user.getTranslation("challenges.gui.admin.descriptions.enabled"));
				}
				else
				{
					description = Collections.singletonList(this.user.getTranslation("challenges.gui.admin.descriptions.disabled"));
				}

				icon = new ItemStack(Material.LEVER);
				clickHandler = (panel, user, clickType, slot) -> {
					this.challenge.setRemoveEntities(!this.challenge.isRemoveEntities());

					this.build();
					return true;
				};
				glow = this.challenge.isRemoveEntities();
				break;
			}
			case REQUIRED_BLOCKS:
			{
				List<String> values = new ArrayList<>(this.challenge.getRequiredBlocks().size());

				for (Map.Entry<Material, Integer> entry : this.challenge.getRequiredBlocks().entrySet())
				{
					values.add(entry.getKey().name() + " " + entry.getValue());
				}

				name = this.user.getTranslation("challenges.gui.admin.buttons.blocks");
				description = values;
				icon = new ItemStack(Material.STONE);
				clickHandler = (panel, user, clickType, slot) -> {
					new ManageBlocksGUI(this.addon,
						this.world,
						this.user,
						this.challenge.getRequiredBlocks(),
						this.topLabel,
						this.permissionPrefix,
						this).build();

					return true;
				};
				glow = false;
				break;
			}
			case REMOVE_BLOCKS:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.remove-blocks");

				if (this.challenge.isRemoveBlocks())
				{
					description = Collections.singletonList(this.user.getTranslation("challenges.gui.admin.descriptions.enabled"));
				}
				else
				{
					description = Collections.singletonList(this.user.getTranslation("challenges.gui.admin.descriptions.disabled"));
				}

				icon = new ItemStack(Material.LEVER);
				clickHandler = (panel, user, clickType, slot) -> {
					this.challenge.setRemoveBlocks(!this.challenge.isRemoveBlocks());

					this.build();
					return true;
				};
				glow = this.challenge.isRemoveBlocks();
				break;
			}
			case SEARCH_RADIUS:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.search-radius");
				description = Collections.singletonList(
					this.user.getTranslation("challenges.gui.admin.descriptions.search-radius",
						"[value]",
						Integer.toString(this.challenge.getSearchRadius())));
				icon = new ItemStack(Material.COBBLESTONE_WALL);
				clickHandler = (panel, user, clickType, slot) -> {
					new NumberGUI(this.user, this.challenge.getSearchRadius(), 0, (status, value) -> {
						if (status)
						{
							this.challenge.setSearchRadius(value);
						}

						this.build();
					});

					return true;
				};
				glow = false;
				break;
			}
			case REQUIRED_PERMISSIONS:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.permissions");
				description = new ArrayList<>(this.challenge.getReqPerms());
				icon = new ItemStack(Material.REDSTONE_LAMP);
				clickHandler = (panel, user, clickType, slot) -> {
					new StringListGUI(this.user, this.challenge.getReqPerms(), (status, value) -> {
						if (status)
						{
							this.challenge.setReqPerms(new HashSet<>(value));
						}

						this.build();
					});

					return true;
				};
				glow = false;
				break;
			}
			case REQUIRED_ITEMS:
			{
				List<String> values = new ArrayList<>(this.challenge.getRequiredItems().size());

				for (ItemStack itemStack : this.challenge.getRequiredItems())
				{
					values.add(itemStack.getType().name() + " " + itemStack.getAmount());
				}

				name = this.user.getTranslation("challenges.gui.admin.buttons.required-items");
				description = values;
				icon = new ItemStack(Material.CHEST);
				clickHandler = (panel, user, clickType, slot) -> {
					new ItemSwitchGUI(this.user, this.challenge.getRequiredItems(), (status, value) -> {
						if (status)
						{
							this.challenge.setRequiredItems(value);
						}

						this.build();
					});

					return true;
				};
				glow = false;
				break;
			}
			case REMOVE_ITEMS:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.remove-items");

				if (this.challenge.isTakeItems())
				{
					description = Collections.singletonList(this.user.getTranslation("challenges.gui.admin.descriptions.enabled"));
				}
				else
				{
					description = Collections.singletonList(this.user.getTranslation("challenges.gui.admin.descriptions.disabled"));
				}

				icon = new ItemStack(Material.LEVER);
				clickHandler = (panel, user, clickType, slot) -> {
					this.challenge.setTakeItems(!this.challenge.isTakeItems());

					this.build();
					return true;
				};
				glow = this.challenge.isTakeItems();
				break;
			}
			case REQUIRED_EXPERIENCE:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.required-exp");
				description = Collections.singletonList(
					this.user.getTranslation("challenges.gui.admin.descriptions.required-exp",
						"[value]",
						Integer.toString(this.challenge.getMaxTimes())));
				icon = new ItemStack(Material.EXPERIENCE_BOTTLE);
				clickHandler = (panel, user, clickType, slot) -> {
					new NumberGUI(this.user, this.challenge.getReqExp(), 0, (status, value) -> {
						if (status)
						{
							this.challenge.setReqExp(value);
						}

						this.build();
					});
					return true;
				};
				glow = false;
				break;
			}
			case REMOVE_EXPERIENCE:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.remove-exp");

				if (this.challenge.isTakeExperience())
				{
					description = Collections.singletonList(this.user.getTranslation("challenges.gui.admin.descriptions.enabled"));
				}
				else
				{
					description = Collections.singletonList(this.user.getTranslation("challenges.gui.admin.descriptions.disabled"));
				}

				icon = new ItemStack(Material.LEVER);
				clickHandler = (panel, user, clickType, slot) -> {
					this.challenge.setTakeExperience(!this.challenge.isTakeExperience());

					this.build();
					return true;
				};
				glow = this.challenge.isTakeExperience();
				break;
			}
			case REQUIRED_LEVEL:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.required-level");
				description = Collections.singletonList(
					this.user.getTranslation("challenges.gui.admin.descriptions.required-level",
						"[value]",
						Long.toString(this.challenge.getReqIslandlevel())));
				icon = new ItemStack(Material.BEACON);
				clickHandler = (panel, user, clickType, slot) -> {
					new NumberGUI(this.user, (int) this.challenge.getReqIslandlevel(), (status, value) -> {
						if (status)
						{
							this.challenge.setReqIslandlevel(value);
						}

						this.build();
					});

					return true;
				};
				glow = false;
				break;
			}
			case REQUIRED_MONEY:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.required-money");
				description = Collections.singletonList(
					this.user.getTranslation("challenges.gui.admin.descriptions.required-money",
						"[value]",
						Integer.toString(this.challenge.getReqMoney())));
				icon = new ItemStack(Material.GOLD_INGOT);
				clickHandler = (panel, user, clickType, slot) -> {
					new NumberGUI(this.user, this.challenge.getReqMoney(), 0, (status, value) -> {
						if (status)
						{
							this.challenge.setReqMoney(value);
						}

						this.build();
					});
					return true;
				};
				glow = false;
				break;
			}
			case REMOVE_MONEY:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.remove-money");

				if (this.challenge.isTakeMoney())
				{
					description = Collections.singletonList(this.user.getTranslation("challenges.gui.admin.descriptions.enabled"));
				}
				else
				{
					description = Collections.singletonList(this.user.getTranslation("challenges.gui.admin.descriptions.disabled"));
				}

				icon = new ItemStack(Material.LEVER);
				clickHandler = (panel, user, clickType, slot) -> {
					this.challenge.setTakeMoney(!this.challenge.isTakeMoney());

					this.build();
					return true;
				};
				glow = this.challenge.isTakeMoney();
				break;
			}

			case REWARD_TEXT:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.reward-text");
				description = Collections.singletonList(this.challenge.getRewardText());
				icon = new ItemStack(Material.WRITTEN_BOOK);
				clickHandler = (panel, user, clickType, slot) -> {
					new AnvilGUI(this.addon.getPlugin(),
						this.user.getPlayer(),
						this.challenge.getRewardText(),
						(player, reply) -> {
							this.challenge.setRewardText(reply);
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
				List<String> values = new ArrayList<>(this.challenge.getRewardItems().size());

				for (ItemStack itemStack : this.challenge.getRewardItems())
				{
					values.add(itemStack.getType().name() + " " + itemStack.getAmount());
				}

				name = this.user.getTranslation("challenges.gui.admin.buttons.reward-items");
				description = values;
				icon = new ItemStack(Material.CHEST);
				clickHandler = (panel, user, clickType, slot) -> {
					new ItemSwitchGUI(this.user, this.challenge.getRewardItems(), (status, value) -> {
						if (status)
						{
							this.challenge.setRewardItems(value);
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
						Integer.toString(this.challenge.getRewardExp())));
				icon = new ItemStack(Material.EXPERIENCE_BOTTLE);
				clickHandler = (panel, user, clickType, slot) -> {
					new NumberGUI(this.user, this.challenge.getReqExp(), 0, (status, value) -> {
						if (status)
						{
							this.challenge.setRewardExp(value);
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
						Integer.toString(this.challenge.getRewardMoney())));
				icon = new ItemStack(Material.GOLD_INGOT);
				clickHandler = (panel, user, clickType, slot) -> {
					new NumberGUI(this.user, this.challenge.getRewardMoney(), 0, (status, value) -> {
						if (status)
						{
							this.challenge.setRewardMoney(value);
						}

						this.build();
					});

					return true;
				};
				glow = false;
				break;
			}
			case REWARD_COMMANDS:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.reward-command");
				description = this.challenge.getRewardCommands();
				icon = new ItemStack(Material.COMMAND_BLOCK);
				clickHandler = (panel, user, clickType, slot) -> {
					new StringListGUI(this.user, this.challenge.getRewardCommands(), (status, value) -> {
						if (status)
						{
							this.challenge.setRewardCommands(value);
						}

						this.build();
					});

					return true;
				};
				glow = false;
				break;
			}

			case REPEATABLE:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.repeatable");

				if (this.challenge.isRepeatable())
				{
					description = Collections.singletonList(this.user.getTranslation("challenges.gui.admin.descriptions.enabled"));
				}
				else
				{
					description = Collections.singletonList(this.user.getTranslation("challenges.gui.admin.descriptions.disabled"));
				}

				icon = new ItemStack(Material.LEVER);
				clickHandler = (panel, user, clickType, slot) -> {
					this.challenge.setRepeatable(!this.challenge.isRepeatable());

					this.build();
					return true;
				};
				glow = this.challenge.isRepeatable();
				break;
			}
			case REPEAT_COUNT:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.repeat-count");
				description = Collections.singletonList(
					this.user.getTranslation("challenges.gui.admin.descriptions.repeat-count",
						"[value]",
						Integer.toString(this.challenge.getMaxTimes())));
				icon = new ItemStack(Material.COBBLESTONE_WALL);
				clickHandler = (panel, user, clickType, slot) -> {
					new NumberGUI(this.user, this.challenge.getMaxTimes(), 0, (status, value) -> {
						if (status)
						{
							this.challenge.setMaxTimes(value);
						}

						this.build();
					});

					return true;
				};
				glow = false;
				break;
			}

			case REPEAT_REWARD_TEXT:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.repeat-reward-text");
				description = Collections.singletonList(this.challenge.getRepeatRewardText());
				icon = new ItemStack(Material.WRITTEN_BOOK);
				clickHandler = (panel, user, clickType, slot) -> {
					new AnvilGUI(this.addon.getPlugin(),
						this.user.getPlayer(),
						this.challenge.getRepeatRewardText(),
						(player, reply) -> {
							this.challenge.setRepeatRewardText(reply);
							this.build();
							return reply;
						});

					return true;
				};
				glow = false;
				break;
			}
			case REPEAT_REWARD_ITEM:
			{
				List<String> values = new ArrayList<>(this.challenge.getRepeatItemReward().size());

				for (ItemStack itemStack : this.challenge.getRepeatItemReward())
				{
					values.add(itemStack.getType().name() + " " + itemStack.getAmount());
				}

				name = this.user.getTranslation("challenges.gui.admin.buttons.repeat-reward-items");
				description = values;
				icon = new ItemStack(Material.TRAPPED_CHEST);
				clickHandler = (panel, user, clickType, slot) -> {
					new ItemSwitchGUI(this.user, this.challenge.getRepeatItemReward(), (status, value) -> {
						if (status)
						{
							this.challenge.setRepeatItemReward(value);
						}

						this.build();
					});

					return true;
				};
				glow = false;
				break;
			}
			case REPEAT_REWARD_EXPERIENCE:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.repeat-reward-exp");
				description = Collections.singletonList(
					this.user.getTranslation("challenges.gui.admin.descriptions.repeat-reward-exp",
						"[value]",
						Integer.toString(this.challenge.getRepeatExpReward())));
				icon = new ItemStack(Material.GLASS_BOTTLE);
				clickHandler = (panel, user, clickType, slot) -> {
					new NumberGUI(this.user, this.challenge.getRepeatExpReward(), 0, (status, value) -> {
						if (status)
						{
							this.challenge.setRepeatExpReward(value);
						}

						this.build();
					});

					return true;
				};
				glow = false;
				break;
			}
			case REPEAT_REWARD_MONEY:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.repeat-reward-money");
				description = Collections.singletonList(
					this.user.getTranslation("challenges.gui.admin.descriptions.repeat-reward-money",
						"[value]",
						Integer.toString(this.challenge.getRepeatMoneyReward())));
				icon = new ItemStack(Material.GOLD_NUGGET);
				clickHandler = (panel, user, clickType, slot) -> {
					new NumberGUI(this.user, this.challenge.getRepeatMoneyReward(), 0, (status, value) -> {
						if (status)
						{
							this.challenge.setRepeatMoneyReward(value);
						}

						this.build();
					});

					return true;
				};
				glow = false;
				break;
			}
			case REPEAT_REWARD_COMMANDS:
			{
				name = this.user.getTranslation("challenges.gui.admin.buttons.repeat-reward-command");
				description = this.challenge.getRepeatRewardCommands();
				icon = new ItemStack(Material.COMMAND_BLOCK);
				clickHandler = (panel, user, clickType, slot) -> {
					new StringListGUI(this.user, this.challenge.getRepeatRewardCommands(), (status, value) -> {
						if (status)
						{
							this.challenge.setRepeatRewardCommands(value);
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

		return new PanelItem(icon, name, description, glow, clickHandler, false);
	}


	/**
	 * This method returns next challenge type from given.
	 * @param type Given challenge type.
	 * @return Next Challenge Type.
	 */
	private Challenges.ChallengeType getNextType(Challenges.ChallengeType type)
	{
		Challenges.ChallengeType[] values = Challenges.ChallengeType.values();

		for (int i = 0; i < values.length; i++)
		{
			if (values[i].equals(type))
			{
				if (i + 1 == values.length)
				{
					return values[0];
				}
				else
				{
					return values[i + 1];
				}
			}
		}

		return type;
	}


	/**
	 * This method returns previous challenge type from given.
	 * @param type Given challenge type.
	 * @return Previous Challenge Type.
	 */
	private Challenges.ChallengeType getPreviousType(Challenges.ChallengeType type)
	{
		Challenges.ChallengeType[] values = Challenges.ChallengeType.values();

		for (int i = 0; i < values.length; i++)
		{
			if (values[i].equals(type))
			{
				if (i > 0)
				{
					return values[i - 1];
				}
				else
				{
					return values[values.length - 1];
				}
			}
		}

		return type;
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * Represents different types of menus
	 */
	private enum MenuType
	{
		PROPERTIES,
		REQUIREMENTS,
		REWARDS
	}


	/**
	 * Represents different buttons that could be in menus.
	 */
	private enum Button
	{
		NAME,
		TYPE,
		DEPLOYED,
		ICON,
		DESCRIPTION,
		ORDER,
		ENVIRONMENT,
		REMOVE_ON_COMPLETE,

		REQUIRED_ENTITIES,
		REMOVE_ENTITIES,
		REQUIRED_BLOCKS,
		REMOVE_BLOCKS,
		SEARCH_RADIUS,
		REQUIRED_PERMISSIONS,
		REQUIRED_ITEMS,
		REMOVE_ITEMS,
		REQUIRED_EXPERIENCE,
		REMOVE_EXPERIENCE,
		REQUIRED_LEVEL,
		REQUIRED_MONEY,
		REMOVE_MONEY,

		REWARD_TEXT,
		REWARD_ITEM,
		REWARD_EXPERIENCE,
		REWARD_MONEY,
		REWARD_COMMANDS,

		REPEATABLE,
		REPEAT_COUNT,

		REPEAT_REWARD_TEXT,
		REPEAT_REWARD_ITEM,
		REPEAT_REWARD_EXPERIENCE,
		REPEAT_REWARD_MONEY,
		REPEAT_REWARD_COMMANDS,
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * Variable holds challenge thats needs editing.
	 */
	private Challenges challenge;

	/**
	 * Variable holds current active menu.
	 */
	private MenuType currentMenuType;
}
