package world.bentobox.challenges.panel.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import java.util.*;

import net.wesjd.anvilgui.AnvilGUI;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.panel.CommonGUI;
import world.bentobox.challenges.panel.util.ItemSwitchGUI;
import world.bentobox.challenges.panel.util.NumberGUI;
import world.bentobox.challenges.panel.util.SelectEnvironmentGUI;
import world.bentobox.challenges.panel.util.StringListGUI;
import world.bentobox.challenges.utils.GuiUtils;


/**
 * This class contains all necessary methods that creates GUI and allow to edit challenges
 * properties.
 * TODO: ISLAND is not repeatable.
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
		Challenge challenge,
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
		Challenge challenge,
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
			this.user.getTranslation("challenges.gui.title.admin.edit-challenge-title"));

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
				case OTHER:
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
		panelBuilder.item(30, this.createButton(Button.REMOVE_BLOCKS));

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
			case REQUIREMENTS:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.requirements");
				description = this.user.getTranslation("challenges.gui.descriptions.admin.requirements");
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

		return new PanelItem(icon, name, GuiUtils.stringSplit(description, this.addon.getChallengesSettings().getLoreLineLength()), glow, clickHandler, false);
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

		final int lineLength = this.addon.getChallengesSettings().getLoreLineLength();

		switch (button)
		{
			case TYPE:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.type");

				List<String> values = new ArrayList<>(5);
				values.add(this.user.getTranslation("challenges.gui.descriptions.admin.type"));

				values.add((this.challenge.getChallengeType().equals(Challenge.ChallengeType.ISLAND) ? "&2" : "&c") +
					this.user.getTranslation("challenges.gui.descriptions.type.island"));
				values.add((this.challenge.getChallengeType().equals(Challenge.ChallengeType.INVENTORY) ? "&2" : "&c") +
					this.user.getTranslation("challenges.gui.descriptions.type.inventory"));
				values.add((this.challenge.getChallengeType().equals(Challenge.ChallengeType.OTHER) ? "&2" : "&c") +
					this.user.getTranslation("challenges.gui.descriptions.type.other"));

				values.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]", this.challenge.getChallengeType().name()));

				description = values;

				if (this.challenge.getChallengeType().equals(Challenge.ChallengeType.ISLAND))
				{
					icon = new ItemStack(Material.GRASS_BLOCK);
				}
				else if (this.challenge.getChallengeType().equals(Challenge.ChallengeType.INVENTORY))
				{
					icon = new ItemStack(Material.CHEST);
				}
				else if (this.challenge.getChallengeType().equals(Challenge.ChallengeType.OTHER))
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.deployment");

				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.deployment"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]",
					this.challenge.isDeployed() ?
						this.user.getTranslation("challenges.gui.descriptions.enabled") :
						this.user.getTranslation("challenges.gui.descriptions.disabled")));

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
				name = this.user.getTranslation("challenges.gui.buttons.admin.icon");
				description = Collections.singletonList(this.user.getTranslation(
					"challenges.gui.descriptions.admin.icon-challenge"));
				icon = this.challenge.getIcon();
				clickHandler = (panel, user, clickType, slot) -> {
					new AnvilGUI(this.addon.getPlugin(),
						this.user.getPlayer(),
						this.challenge.getIcon().getType().name(),
						(player, reply) -> {
							Material material = Material.getMaterial(reply);

							if (material != null)
							{
								this.challenge.setIcon(new ItemStack(material));
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
			case DESCRIPTION:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.description");
				description = Collections.singletonList(this.user.getTranslation("challenges.gui.descriptions.admin.description"));
				icon = new ItemStack(Material.WRITTEN_BOOK);
				clickHandler = (panel, user, clickType, slot) -> {
					new StringListGUI(this.user, this.challenge.getDescription(), lineLength, (status, value) -> {
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.order");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.order"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]", Integer.toString(this.challenge.getOrder())));

				icon = new ItemStack(Material.DROPPER);
				clickHandler = (panel, user, clickType, slot) -> {
					new NumberGUI(this.user, this.challenge.getOrder(), -1, 54, (status, value) -> {
						if (status)
						{
							this.challenge.setOrder(value);
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.environment");

				description = new ArrayList<>(4);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.environment"));

				description.add((this.challenge.getEnvironment().contains(World.Environment.NORMAL) ? "&2" : "&c") +
					this.user.getTranslation("challenges.gui.descriptions.normal"));
				description.add((this.challenge.getEnvironment().contains(World.Environment.NETHER) ? "&2" : "&c") +
					this.user.getTranslation("challenges.gui.descriptions.nether"));
				description.add((this.challenge.getEnvironment().contains(World.Environment.THE_END) ? "&2" : "&c") +
					this.user.getTranslation("challenges.gui.descriptions.the-end"));

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
				name = this.user.getTranslation("challenges.gui.buttons.admin.remove-on-complete");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.remove-on-complete"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]",
					this.challenge.isRemoveWhenCompleted() ?
						this.user.getTranslation("challenges.gui.descriptions.enabled") :
						this.user.getTranslation("challenges.gui.descriptions.disabled")));

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
				name = this.user.getTranslation("challenges.gui.buttons.admin.name");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.name-challenge"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]", this.challenge.getFriendlyName()));

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
				name = this.user.getTranslation("challenges.gui.buttons.admin.required-entities");

				description = new ArrayList<>(this.challenge.getRequiredEntities().size() + 1);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.required-entities"));

				for (Map.Entry<EntityType, Integer> entry : this.challenge.getRequiredEntities().entrySet())
				{
					description.add(this.user.getTranslation("challenges.gui.descriptions.entity",
						"[entity]", entry.getKey().name(),
						"[count]", Integer.toString(entry.getValue())));
				}

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
				name = this.user.getTranslation("challenges.gui.buttons.admin.remove-entities");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.remove-entities"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]",
					this.challenge.isRemoveEntities() ?
						this.user.getTranslation("challenges.gui.descriptions.enabled") :
						this.user.getTranslation("challenges.gui.descriptions.disabled")));

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
				name = this.user.getTranslation("challenges.gui.buttons.admin.required-blocks");

				description = new ArrayList<>(this.challenge.getRequiredEntities().size() + 1);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.required-blocks"));

				for (Map.Entry<Material, Integer> entry : this.challenge.getRequiredBlocks().entrySet())
				{
					description.add(this.user.getTranslation("challenges.gui.descriptions.block",
						"[block]", entry.getKey().name(),
						"[count]", Integer.toString(entry.getValue())));
				}

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
				name = this.user.getTranslation("challenges.gui.buttons.admin.remove-blocks");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.remove-blocks"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]",
					this.challenge.isRemoveBlocks() ?
						this.user.getTranslation("challenges.gui.descriptions.enabled") :
						this.user.getTranslation("challenges.gui.descriptions.disabled")));

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
				name = this.user.getTranslation("challenges.gui.buttons.admin.search-radius");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.search-radius"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]", Integer.toString(this.challenge.getSearchRadius())));

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
				name = this.user.getTranslation("challenges.gui.buttons.admin.required-permissions");
				description = new ArrayList<>(this.challenge.getRequiredPermissions().size() + 1);
				description.add(this.user.getTranslation(
					"challenges.gui.descriptions.admin.required-permissions"));

				for (String permission : this.challenge.getRequiredPermissions())
				{
					description.add(this.user.getTranslation("challenges.gui.descriptions.permission",
						"[permission]", permission));
				}

				icon = new ItemStack(Material.REDSTONE_LAMP);
				clickHandler = (panel, user, clickType, slot) -> {
					new StringListGUI(this.user, this.challenge.getRequiredPermissions(), lineLength, (status, value) -> {
						if (status)
						{
							this.challenge.setRequiredPermissions(new HashSet<>(value));
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.required-items");

				description = new ArrayList<>(this.challenge.getRequiredEntities().size() + 1);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.required-items"));

				for (ItemStack itemStack : this.challenge.getRequiredItems())
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
					new ItemSwitchGUI(this.user, this.challenge.getRequiredItems(), lineLength, (status, value) -> {
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.remove-items");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.remove-items"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]",
					this.challenge.isTakeItems() ?
						this.user.getTranslation("challenges.gui.descriptions.enabled") :
						this.user.getTranslation("challenges.gui.descriptions.disabled")));

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
				name = this.user.getTranslation("challenges.gui.buttons.admin.required-experience");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.required-experience"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]", Integer.toString(this.challenge.getRequiredExperience())));

				icon = new ItemStack(Material.EXPERIENCE_BOTTLE);
				clickHandler = (panel, user, clickType, slot) -> {
					new NumberGUI(this.user, this.challenge.getRequiredExperience(), 0, (status, value) -> {
						if (status)
						{
							this.challenge.setRequiredExperience(value);
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.remove-experience");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.remove-experience"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]",
					this.challenge.isTakeExperience() ?
						this.user.getTranslation("challenges.gui.descriptions.enabled") :
						this.user.getTranslation("challenges.gui.descriptions.disabled")));

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
				name = this.user.getTranslation("challenges.gui.buttons.admin.required-level");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.required-level"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]", Long.toString(this.challenge.getRequiredIslandLevel())));

				if (this.addon.isLevelProvided())
				{
					icon = new ItemStack(Material.BEACON);
					clickHandler = (panel, user, clickType, slot) -> {
						new NumberGUI(this.user, (int) this.challenge.getRequiredIslandLevel(), lineLength, (status, value) -> {
							if (status)
							{
								this.challenge.setRequiredIslandLevel(value);
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
			case REQUIRED_MONEY:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.required-money");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.required-money"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]", Long.toString(this.challenge.getRequiredIslandLevel())));

				if (this.addon.isEconomyProvided())
				{
					icon = new ItemStack(Material.GOLD_INGOT);
					clickHandler = (panel, user, clickType, slot) -> {
						new NumberGUI(this.user, this.challenge.getRequiredMoney(), 0, (status, value) -> {
							if (status)
							{
								this.challenge.setRequiredMoney(value);
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
			case REMOVE_MONEY:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.remove-money");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.remove-money"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]",
					this.challenge.isTakeMoney() ?
						this.user.getTranslation("challenges.gui.descriptions.enabled") :
						this.user.getTranslation("challenges.gui.descriptions.disabled")));

				if (this.addon.isEconomyProvided())
				{
					icon = new ItemStack(Material.LEVER);
					clickHandler = (panel, user, clickType, slot) -> {
						this.challenge.setTakeMoney(!this.challenge.isTakeMoney());

						this.build();
						return true;
					};
				}
				else
				{
					icon = new ItemStack(Material.BARRIER);
					clickHandler = null;
				}

				glow = this.challenge.isTakeMoney();
				break;
			}

			case REWARD_TEXT:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.reward-text");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.reward-text"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]", "|" + this.challenge.getRewardText()));

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
				name = this.user.getTranslation("challenges.gui.buttons.admin.reward-items");

				description = new ArrayList<>(this.challenge.getRewardItems().size() + 1);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.reward-items"));

				for (ItemStack itemStack : this.challenge.getRewardItems())
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
					new ItemSwitchGUI(this.user, this.challenge.getRewardItems(), lineLength, (status, value) -> {
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.reward-experience");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.reward-experience"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]", Integer.toString(this.challenge.getRewardExperience())));
				icon = new ItemStack(Material.EXPERIENCE_BOTTLE);
				clickHandler = (panel, user, clickType, slot) -> {
					new NumberGUI(this.user, this.challenge.getRewardExperience(), 0, (status, value) -> {
						if (status)
						{
							this.challenge.setRewardExperience(value);
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
					"[value]", Integer.toString(this.challenge.getRewardMoney())));

				if (this.addon.isEconomyProvided())
				{
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
				description = new ArrayList<>(this.challenge.getRewardCommands().size() + 1);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.reward-commands"));

				for (String command : this.challenge.getRewardCommands())
				{
					description.add(this.user.getTranslation("challenges.gui.descriptions.command",
						"[command]", command));
				}

				icon = new ItemStack(Material.COMMAND_BLOCK);
				clickHandler = (panel, user, clickType, slot) -> {
					new StringListGUI(this.user, this.challenge.getRewardCommands(), lineLength, (status, value) -> {
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.repeatable");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.repeatable"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]",
					this.challenge.isRepeatable() ?
						this.user.getTranslation("challenges.gui.descriptions.enabled") :
						this.user.getTranslation("challenges.gui.descriptions.disabled")));

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
				name = this.user.getTranslation("challenges.gui.buttons.admin.repeat-count");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.repeat-count"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]", Integer.toString(this.challenge.getMaxTimes())));

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
				name = this.user.getTranslation("challenges.gui.buttons.admin.repeat-reward-text");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.repeat-reward-text"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]", "|" + this.challenge.getRepeatRewardText()));

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
				name = this.user.getTranslation("challenges.gui.buttons.admin.repeat-reward-items");

				description = new ArrayList<>(this.challenge.getRepeatItemReward().size() + 1);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.repeat-reward-items"));

				for (ItemStack itemStack : this.challenge.getRepeatItemReward())
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

				icon = new ItemStack(Material.TRAPPED_CHEST);
				clickHandler = (panel, user, clickType, slot) -> {
					new ItemSwitchGUI(this.user, this.challenge.getRepeatItemReward(), lineLength, (status, value) -> {
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.repeat-reward-experience");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.repeat-reward-experience"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]", Integer.toString(this.challenge.getRepeatExperienceReward())));

				icon = new ItemStack(Material.GLASS_BOTTLE);
				clickHandler = (panel, user, clickType, slot) -> {
					new NumberGUI(this.user, this.challenge.getRepeatExperienceReward(), 0, (status, value) -> {
						if (status)
						{
							this.challenge.setRepeatExperienceReward(value);
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
				name = this.user.getTranslation("challenges.gui.buttons.admin.repeat-reward-money");
				description = new ArrayList<>(2);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.repeat-reward-money"));
				description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
					"[value]", Integer.toString(this.challenge.getRepeatMoneyReward())));

				if (this.addon.isEconomyProvided())
				{
					icon = new ItemStack(Material.GOLD_NUGGET);
					clickHandler = (panel, user, clickType, slot) -> {
						new NumberGUI(this.user,
							this.challenge.getRepeatMoneyReward(),
							0,
							(status, value) -> {
								if (status)
								{
									this.challenge.setRepeatMoneyReward(value);
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
			case REPEAT_REWARD_COMMANDS:
			{
				name = this.user.getTranslation("challenges.gui.buttons.admin.repeat-reward-commands");
				description = new ArrayList<>(this.challenge.getRepeatRewardCommands().size() + 1);
				description.add(this.user.getTranslation("challenges.gui.descriptions.admin.repeat-reward-commands"));

				for (String command : this.challenge.getRepeatRewardCommands())
				{
					description.add(this.user.getTranslation("challenges.gui.descriptions.command",
						"[command]", command));
				}

				icon = new ItemStack(Material.COMMAND_BLOCK);
				clickHandler = (panel, user, clickType, slot) -> {
					new StringListGUI(this.user, this.challenge.getRepeatRewardCommands(), lineLength, (status, value) -> {
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

		return new PanelItem(icon, name, GuiUtils.stringSplit(description, lineLength), glow, clickHandler, false);
	}


	/**
	 * This method returns next challenge type from given.
	 * @param type Given challenge type.
	 * @return Next Challenge Type.
	 */
	private Challenge.ChallengeType getNextType(Challenge.ChallengeType type)
	{
		Challenge.ChallengeType[] values = Challenge.ChallengeType.values();

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
	private Challenge.ChallengeType getPreviousType(Challenge.ChallengeType type)
	{
		Challenge.ChallengeType[] values = Challenge.ChallengeType.values();

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
	private Challenge challenge;

	/**
	 * Variable holds current active menu.
	 */
	private MenuType currentMenuType;
}