package world.bentobox.challenges.panel.admin;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.hooks.LangUtilsHook;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.requirements.InventoryRequirements;
import world.bentobox.challenges.database.object.requirements.IslandRequirements;
import world.bentobox.challenges.database.object.requirements.OtherRequirements;
import world.bentobox.challenges.panel.CommonGUI;
import world.bentobox.challenges.panel.util.ItemSwitchGUI;
import world.bentobox.challenges.panel.util.NumberGUI;
import world.bentobox.challenges.panel.util.SelectBlocksGUI;
import world.bentobox.challenges.panel.util.SelectEnvironmentGUI;
import world.bentobox.challenges.panel.util.StringListGUI;
import world.bentobox.challenges.utils.GuiUtils;
import world.bentobox.challenges.utils.Utils;


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
     * @param addon Addon where panel operates.
     * @param world World from which panel was created.
     * @param user User who created panel.
     * @param topLabel Command top label which creates panel (f.e. island or ai)
     * @param permissionPrefix Command permission prefix (f.e. bskyblock.)
     * @param challenge - challenge that needs editing
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
     * @param addon Addon where panel operates.
     * @param world World from which panel was created.
     * @param user User who created panel.
     * @param topLabel Command top label which creates panel (f.e. island or ai)
     * @param permissionPrefix Command permission prefix (f.e. bskyblock.)
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

        // Set line length.
        this.lineLength = this.addon.getChallengesSettings().getLoreLineLength();
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

        // Every time when this GUI is build, save challenge
        // This will ensure that all main things will be always stored
        this.addon.getChallengesManager().saveChallenge(this.challenge);

        panelBuilder.build();
    }


    /**
     * This class populate ChallengesEditGUI with main challenge settings.
     * @param panelBuilder PanelBuilder where icons must be added.
     */
    private void buildMainPropertiesPanel(PanelBuilder panelBuilder)
    {
        panelBuilder.item(10, this.createButton(Button.NAME));
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
        panelBuilder.item(19, this.createRequirementButton(RequirementButton.REQUIRED_ENTITIES));
        panelBuilder.item(28, this.createRequirementButton(RequirementButton.REMOVE_ENTITIES));

        panelBuilder.item(21, this.createRequirementButton(RequirementButton.REQUIRED_BLOCKS));
        panelBuilder.item(30, this.createRequirementButton(RequirementButton.REMOVE_BLOCKS));

        panelBuilder.item(23, this.createRequirementButton(RequirementButton.SEARCH_RADIUS));
        panelBuilder.item(25, this.createRequirementButton(RequirementButton.REQUIRED_PERMISSIONS));
    }


    /**
     * This class populates ChallengesEditGUI with inventory challenges requirement elements.
     * @param panelBuilder PanelBuilder where icons must be added.
     */
    private void buildInventoryRequirementsPanel(PanelBuilder panelBuilder)
    {
        panelBuilder.item(10, this.createRequirementButton(RequirementButton.REQUIRED_ITEMS));
        panelBuilder.item(19, this.createRequirementButton(RequirementButton.REMOVE_ITEMS));

        panelBuilder.item(25, this.createRequirementButton(RequirementButton.REQUIRED_PERMISSIONS));
    }


    /**
     * This class populates ChallengesEditGUI with other challenges requirement elements.
     * @param panelBuilder PanelBuilder where icons must be added.
     */
    private void buildOtherRequirementsPanel(PanelBuilder panelBuilder)
    {
        panelBuilder.item(10, this.createRequirementButton(RequirementButton.REQUIRED_EXPERIENCE));
        panelBuilder.item(19, this.createRequirementButton(RequirementButton.REMOVE_EXPERIENCE));

        panelBuilder.item(12, this.createRequirementButton(RequirementButton.REQUIRED_MONEY));
        panelBuilder.item(21, this.createRequirementButton(RequirementButton.REMOVE_MONEY));

        panelBuilder.item(23, this.createRequirementButton(RequirementButton.REQUIRED_LEVEL));

        panelBuilder.item(25, this.createRequirementButton(RequirementButton.REQUIRED_PERMISSIONS));
    }


    /**
     * This class populates ChallengesEditGUI with challenges reward elements.
     * @param panelBuilder PanelBuilder where icons must be added.
     */
    private void buildRewardsPanel(PanelBuilder panelBuilder)
    {
        panelBuilder.item(10, this.createRewardButton(RewardButton.REWARD_TEXT));
        panelBuilder.item(19, this.createRewardButton(RewardButton.REWARD_COMMANDS));

        panelBuilder.item(11, this.createRewardButton(RewardButton.REWARD_ITEM));
        panelBuilder.item(20, this.createRewardButton(RewardButton.REWARD_EXPERIENCE));
        panelBuilder.item(29, this.createRewardButton(RewardButton.REWARD_MONEY));

        panelBuilder.item(22, this.createRewardButton(RewardButton.REPEATABLE));

        if (this.challenge.isRepeatable())
        {
            panelBuilder.item(31, this.createRewardButton(RewardButton.REPEAT_COUNT));

            panelBuilder.item(15, this.createRewardButton(RewardButton.REPEAT_REWARD_TEXT));
            panelBuilder.item(24, this.createRewardButton(RewardButton.REPEAT_REWARD_COMMANDS));

            panelBuilder.item(16, this.createRewardButton(RewardButton.REPEAT_REWARD_ITEM));
            panelBuilder.item(25, this.createRewardButton(RewardButton.REPEAT_REWARD_EXPERIENCE));
            panelBuilder.item(34, this.createRewardButton(RewardButton.REPEAT_REWARD_MONEY));
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

        return new PanelItemBuilder().
                icon(icon).
                name(name).
                description(GuiUtils.stringSplit(description, this.lineLength)).
                glow(glow).
                clickHandler(clickHandler).
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

                new SelectBlocksGUI(this.user, true, (status, materials) -> {
                    if (status)
                    {
                        materials.forEach(material ->
                        this.challenge.setIcon(new ItemStack(material)));
                    }

                    this.build();
                });

                return true;
            };
            glow = false;
            break;
        }
        case DESCRIPTION:
        {
            name = this.user.getTranslation("challenges.gui.buttons.admin.description");
            description = Collections.singletonList(
                    this.user.getTranslation("challenges.gui.descriptions.admin.description"));
            icon = new ItemStack(Material.WRITTEN_BOOK);
            clickHandler = (panel, user, clickType, slot) -> {
                new StringListGUI(this.user,
                        this.challenge.getDescription(),
                        this.lineLength,
                        (status, value) -> {
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
                new NumberGUI(this.user,
                        this.challenge.getOrder(),
                        -1,
                        9999,
                        this.lineLength,
                        (status, value) -> {
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
                    // If color code is removed from here, users can use color code in language files.
                    ChatColor.stripColor(this.user.getTranslation("challenges.gui.descriptions.normal")));
            description.add((this.challenge.getEnvironment().contains(World.Environment.NETHER) ? "&2" : "&c") +
                    ChatColor.stripColor(this.user.getTranslation("challenges.gui.descriptions.nether")));
            description.add((this.challenge.getEnvironment().contains(World.Environment.THE_END) ? "&2" : "&c") +
                    ChatColor.stripColor(this.user.getTranslation("challenges.gui.descriptions.the-end")));

            icon = new ItemStack(Material.DROPPER);
            clickHandler = (panel, user, clickType, slot) -> {
                new SelectEnvironmentGUI(this.user,
                        this.challenge.getEnvironment(),
                        (status, value) -> {
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

                this.getFriendlyName(reply -> {
                    if (reply != null)
                    {
                        this.challenge.setFriendlyName(reply);
                    }

                    this.build();
                },
                        this.user.getTranslation("challenges.gui.questions.admin.challenge-name"),
                        this.challenge.getFriendlyName()
                        );

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
                description(GuiUtils.stringSplit(description, this.lineLength)).
                glow(glow).
                clickHandler(clickHandler).
                build();
    }


    /**
     * This method creates buttons for requirements menu.
     * @param button Button which panel item must be created.
     * @return PanelItem that represents given button.
     */
    private PanelItem createRequirementButton(RequirementButton button)
    {
        ItemStack icon;
        String name;
        List<String> description;
        boolean glow;
        PanelItem.ClickHandler clickHandler;

        switch (button)
        {
        case REQUIRED_PERMISSIONS:
        {
            name = this.user.getTranslation("challenges.gui.buttons.admin.required-permissions");
            description = new ArrayList<>(this.challenge.getRequirements().getRequiredPermissions().size() + 1);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.required-permissions"));

            for (String permission : this.challenge.getRequirements().getRequiredPermissions())
            {
                description.add(this.user.getTranslation("challenges.gui.descriptions.permission",
                        "[permission]", permission));
            }

            icon = new ItemStack(Material.REDSTONE_LAMP);
            clickHandler = (panel, user, clickType, slot) -> {
                new StringListGUI(this.user,
                        this.challenge.getRequirements().getRequiredPermissions(),
                        lineLength,
                        (status, value) -> {
                            if (status)
                            {
                                this.challenge.getRequirements().setRequiredPermissions(new HashSet<>(value));
                            }

                            this.build();
                        });

                return true;
            };
            glow = false;
            break;
        }

        case REQUIRED_ENTITIES:
        case REMOVE_ENTITIES:
        case REQUIRED_BLOCKS:
        case REMOVE_BLOCKS:
        case SEARCH_RADIUS:
        {
            return this.createIslandRequirementButton(button);
        }

        case REQUIRED_ITEMS:
        case REMOVE_ITEMS:
        {
            return this.createInventoryRequirementButton(button);
        }

        case REQUIRED_EXPERIENCE:
        case REMOVE_EXPERIENCE:
        case REQUIRED_LEVEL:
        case REQUIRED_MONEY:
        case REMOVE_MONEY:
        {
            return this.createOtherRequirementButton(button);
        }

        default:
            return null;
        }

        return new PanelItemBuilder().
                icon(icon).
                name(name).
                description(GuiUtils.stringSplit(description, this.lineLength)).
                glow(glow).
                clickHandler(clickHandler).
                build();
    }


    /**
     * This method creates buttons for island requirements menu.
     * @param button Button which panel item must be created.
     * @return PanelItem that represents given button.
     */
    private PanelItem createIslandRequirementButton(RequirementButton button)
    {
        ItemStack icon;
        String name;
        List<String> description;
        boolean glow;
        PanelItem.ClickHandler clickHandler;

        final IslandRequirements requirements = this.challenge.getRequirements();

        switch (button)
        {
        case REQUIRED_ENTITIES:
        {
            name = this.user.getTranslation("challenges.gui.buttons.admin.required-entities");

            description = new ArrayList<>(requirements.getRequiredEntities().size() + 1);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.required-entities"));

            for (Map.Entry<EntityType, Integer> entry : requirements.getRequiredEntities().entrySet())
            {
                description.add(this.user.getTranslation("challenges.gui.descriptions.entity",
                        "[entity]", LangUtilsHook.getEntityName(entry.getKey(), user),
                        "[count]", Integer.toString(entry.getValue())));
            }

            icon = new ItemStack(Material.CREEPER_HEAD);
            clickHandler = (panel, user, clickType, slot) -> {
                new ManageEntitiesGUI(this.addon,
                        this.world,
                        this.user,
                        requirements.getRequiredEntities(),
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
                    requirements.isRemoveEntities() ?
                            this.user.getTranslation("challenges.gui.descriptions.enabled") :
                                this.user.getTranslation("challenges.gui.descriptions.disabled")));

            icon = new ItemStack(Material.LEVER);
            clickHandler = (panel, user, clickType, slot) -> {
                requirements.setRemoveEntities(!requirements.isRemoveEntities());

                this.build();
                return true;
            };
            glow = requirements.isRemoveEntities();
            break;
        }
        case REQUIRED_BLOCKS:
        {
            name = this.user.getTranslation("challenges.gui.buttons.admin.required-blocks");

            description = new ArrayList<>(requirements.getRequiredBlocks().size() + 1);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.required-blocks"));

            for (Map.Entry<Material, Integer> entry : requirements.getRequiredBlocks().entrySet())
            {
                description.add(this.user.getTranslation("challenges.gui.descriptions.block",
                        "[block]", LangUtilsHook.getMaterialName(entry.getKey(), user),
                        "[count]", Integer.toString(entry.getValue())));
            }

            icon = new ItemStack(Material.STONE);
            clickHandler = (panel, user, clickType, slot) -> {
                new ManageBlocksGUI(this.addon,
                        this.world,
                        this.user,
                        requirements.getRequiredBlocks(),
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
                    requirements.isRemoveBlocks() ?
                            this.user.getTranslation("challenges.gui.descriptions.enabled") :
                                this.user.getTranslation("challenges.gui.descriptions.disabled")));

            icon = new ItemStack(Material.LEVER);
            clickHandler = (panel, user, clickType, slot) -> {
                requirements.setRemoveBlocks(!requirements.isRemoveBlocks());

                this.build();
                return true;
            };
            glow = requirements.isRemoveBlocks();
            break;
        }
        case SEARCH_RADIUS:
        {
            name = this.user.getTranslation("challenges.gui.buttons.admin.search-radius");
            description = new ArrayList<>(2);
            description.add(this.user
                    .getTranslation("challenges.gui.descriptions.admin.search-radius"));
            description
            .add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]", Integer.toString(requirements.getSearchRadius())));

            icon = new ItemStack(Material.COBBLESTONE_WALL);

            // Search radius should not be larger then island radius.
            int maxSearchDistance =
                    this.addon.getPlugin().getIWM().getAddon(this.world).map(gameModeAddon ->
                    gameModeAddon.getWorldSettings().getIslandDistance()).orElse(100);

            clickHandler = (panel, user, clickType, slot) -> {
                new NumberGUI(this.user,
                        requirements.getSearchRadius(),
                        0,
                        maxSearchDistance,
                        this.lineLength,
                        (status, value) -> {
                            if (status)
                            {
                                requirements.setSearchRadius(value);
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
                description(GuiUtils.stringSplit(description, this.lineLength)).
                glow(glow).
                clickHandler(clickHandler).
                build();
    }


    /**
     * This method creates buttons for inventory requirements menu.
     * @param button Button which panel item must be created.
     * @return PanelItem that represents given button.
     */
    private PanelItem createInventoryRequirementButton(RequirementButton button)
    {
        ItemStack icon;
        String name;
        List<String> description;
        boolean glow;
        PanelItem.ClickHandler clickHandler;

        final InventoryRequirements requirements = this.challenge.getRequirements();

        switch (button)
        {
        case REQUIRED_ITEMS:
        {
            name = this.user.getTranslation("challenges.gui.buttons.admin.required-items");

            description = new ArrayList<>(requirements.getRequiredItems().size() + 1);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.required-items"));

            Utils.groupEqualItems(requirements.getRequiredItems()).forEach(itemStack ->
            description.addAll(this.generateItemStackDescription(itemStack)));

            icon = new ItemStack(Material.CHEST);
            clickHandler = (panel, user, clickType, slot) -> {
                new ItemSwitchGUI(this.user,
                        requirements.getRequiredItems(),
                        this.lineLength,
                        (status, value) -> {
                            if (status)
                            {
                                requirements.setRequiredItems(value);
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
                    requirements.isTakeItems() ?
                            this.user.getTranslation("challenges.gui.descriptions.enabled") :
                                this.user.getTranslation("challenges.gui.descriptions.disabled")));

            icon = new ItemStack(Material.LEVER);
            clickHandler = (panel, user, clickType, slot) -> {
                requirements.setTakeItems(!requirements.isTakeItems());

                this.build();
                return true;
            };
            glow = requirements.isTakeItems();
            break;
        }
        default:
            return null;
        }

        return new PanelItemBuilder().
                icon(icon).
                name(name).
                description(GuiUtils.stringSplit(description, this.lineLength)).
                glow(glow).
                clickHandler(clickHandler).
                build();
    }


    /**
     * This method creates buttons for other requirements menu.
     * @param button Button which panel item must be created.
     * @return PanelItem that represents given button.
     */
    private PanelItem createOtherRequirementButton(RequirementButton button)
    {
        ItemStack icon;
        String name;
        List<String> description;
        boolean glow;
        PanelItem.ClickHandler clickHandler;

        final OtherRequirements requirements = this.challenge.getRequirements();

        switch (button)
        {
        case REQUIRED_EXPERIENCE:
        {
            name = this.user.getTranslation("challenges.gui.buttons.admin.required-experience");
            description = new ArrayList<>(2);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.required-experience"));
            description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]", Integer.toString(requirements.getRequiredExperience())));

            icon = new ItemStack(Material.EXPERIENCE_BOTTLE);
            clickHandler = (panel, user, clickType, slot) -> {
                new NumberGUI(this.user,
                        requirements.getRequiredExperience(),
                        0,
                        this.lineLength,
                        (status, value) -> {
                            if (status)
                            {
                                requirements.setRequiredExperience(value);
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
                    requirements.isTakeExperience() ?
                            this.user.getTranslation("challenges.gui.descriptions.enabled") :
                                this.user.getTranslation("challenges.gui.descriptions.disabled")));

            icon = new ItemStack(Material.LEVER);
            clickHandler = (panel, user, clickType, slot) -> {
                requirements.setTakeExperience(!requirements.isTakeExperience());

                this.build();
                return true;
            };
            glow = requirements.isTakeExperience();
            break;
        }
        case REQUIRED_LEVEL:
        {
            name = this.user.getTranslation("challenges.gui.buttons.admin.required-level");
            description = new ArrayList<>(2);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.required-level"));
            description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]", Long.toString(requirements.getRequiredIslandLevel())));

            icon = new ItemStack(this.addon.isLevelProvided() ? Material.BEACON : Material.BARRIER);
            clickHandler = (panel, user, clickType, slot) -> {
                new NumberGUI(this.user,
                        (int) requirements.getRequiredIslandLevel(),
                        lineLength,
                        (status, value) -> {
                            if (status)
                            {
                                requirements.setRequiredIslandLevel(value);
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
            name = this.user.getTranslation("challenges.gui.buttons.admin.required-money");
            description = new ArrayList<>(2);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.required-money"));
            description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]", Double.toString(requirements.getRequiredMoney())));

            icon = new ItemStack(this.addon.isEconomyProvided() ? Material.GOLD_INGOT : Material.BARRIER);
            clickHandler = (panel, user, clickType, slot) -> {
                new NumberGUI(this.user,
                        (int) requirements.getRequiredMoney(),
                        0,
                        lineLength,
                        (status, value) -> {
                            if (status)
                            {
                                requirements.setRequiredMoney(value);
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
            name = this.user.getTranslation("challenges.gui.buttons.admin.remove-money");
            description = new ArrayList<>(2);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.remove-money"));
            description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]",
                    requirements.isTakeMoney() ?
                            this.user.getTranslation("challenges.gui.descriptions.enabled") :
                                this.user.getTranslation("challenges.gui.descriptions.disabled")));

            icon = new ItemStack(this.addon.isEconomyProvided() ? Material.LEVER : Material.BARRIER);
            clickHandler = (panel, user, clickType, slot) -> {
                requirements.setTakeMoney(!requirements.isTakeMoney());

                this.build();
                return true;
            };

            glow = requirements.isTakeMoney();
            break;
        }
        default:
            return null;
        }

        return new PanelItemBuilder().
                icon(icon).
                name(name).
                description(GuiUtils.stringSplit(description, this.lineLength)).
                glow(glow).
                clickHandler(clickHandler).
                build();
    }


    /**
     * This method creates buttons for rewards menu.
     * @param button Button which panel item must be created.
     * @return PanelItem that represents given button.
     */
    private PanelItem createRewardButton(RewardButton button)
    {
        ItemStack icon;
        String name;
        List<String> description;
        boolean glow;
        PanelItem.ClickHandler clickHandler;

        switch (button)
        {
        case REWARD_TEXT:
        {
            name = this.user.getTranslation("challenges.gui.buttons.admin.reward-text");
            description = new ArrayList<>(2);
            description
            .add(this.user.getTranslation("challenges.gui.descriptions.admin.reward-text"));
            description
            .add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]", "|" + this.challenge.getRewardText()));

            icon = new ItemStack(Material.WRITTEN_BOOK);
            clickHandler = (panel, user, clickType, slot) -> {
                new StringListGUI(this.user,
                        this.challenge.getRewardText(),
                        lineLength,
                        (status, value) -> {
                            if (status)
                            {
                                String singleLineMessage = value.stream().
                                        map(s -> s + "|").
                                        collect(Collectors.joining());

                                if (singleLineMessage.endsWith("|"))
                                {
                                    singleLineMessage = singleLineMessage
                                            .substring(0, singleLineMessage.length() - 1);
                                }

                                this.challenge.setRewardText(singleLineMessage);
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

            description = new ArrayList<>(this.challenge.getRewardItems().size() + 1);
            description.add(this.user
                    .getTranslation("challenges.gui.descriptions.admin.reward-items"));

            Utils.groupEqualItems(this.challenge.getRewardItems()).forEach(itemStack ->
            description.addAll(this.generateItemStackDescription(itemStack)));

            icon = new ItemStack(Material.CHEST);
            clickHandler = (panel, user, clickType, slot) -> {
                new ItemSwitchGUI(this.user,
                        this.challenge.getRewardItems(),
                        lineLength,
                        (status, value) -> {
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
            description.add(this.user
                    .getTranslation("challenges.gui.descriptions.admin.reward-experience"));
            description
            .add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]", Integer.toString(this.challenge.getRewardExperience())));
            icon = new ItemStack(Material.EXPERIENCE_BOTTLE);
            clickHandler = (panel, user, clickType, slot) -> {
                new NumberGUI(this.user,
                        this.challenge.getRewardExperience(),
                        0,
                        lineLength,
                        (status, value) -> {
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
            description.add(this.user
                    .getTranslation("challenges.gui.descriptions.admin.reward-money"));
            description
            .add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]", Integer.toString(this.challenge.getRewardMoney())));

            icon = new ItemStack(
                    this.addon.isEconomyProvided() ? Material.GOLD_INGOT : Material.BARRIER);
            clickHandler = (panel, user, clickType, slot) -> {
                new NumberGUI(this.user,
                        this.challenge.getRewardMoney(),
                        0,
                        lineLength,
                        (status, value) -> {
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
            name = this.user.getTranslation("challenges.gui.buttons.admin.reward-commands");
            description = new ArrayList<>(this.challenge.getRewardCommands().size() + 1);
            description.add(this.user
                    .getTranslation("challenges.gui.descriptions.admin.reward-commands"));

            for (String command : this.challenge.getRewardCommands())
            {
                description.add(this.user.getTranslation("challenges.gui.descriptions.command",
                        "[command]", command));
            }

            icon = new ItemStack(Material.COMMAND_BLOCK);
            clickHandler = (panel, user, clickType, slot) -> {
                new StringListGUI(this.user,
                        this.challenge.getRewardCommands(),
                        lineLength,
                        (status, value) -> {
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
            description
            .add(this.user.getTranslation("challenges.gui.descriptions.admin.repeatable"));
            description
            .add(this.user.getTranslation("challenges.gui.descriptions.current-value",
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
            description.add(this.user
                    .getTranslation("challenges.gui.descriptions.admin.repeat-count"));
            description
            .add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]", Integer.toString(this.challenge.getMaxTimes())));

            icon = new ItemStack(Material.COBBLESTONE_WALL);
            clickHandler = (panel, user, clickType, slot) -> {
                new NumberGUI(this.user,
                        this.challenge.getMaxTimes(),
                        0,
                        lineLength,
                        (status, value) -> {
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
            description.add(this.user
                    .getTranslation("challenges.gui.descriptions.admin.repeat-reward-text"));
            description
            .add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]", "|" + this.challenge.getRepeatRewardText()));

            icon = new ItemStack(Material.WRITTEN_BOOK);
            clickHandler = (panel, user, clickType, slot) -> {
                new StringListGUI(this.user,
                        this.challenge.getRepeatRewardText(),
                        lineLength,
                        (status, value) -> {
                            if (status)
                            {
                                String singleLineMessage = value.stream().
                                        map(s -> s + "|").
                                        collect(Collectors.joining());

                                if (singleLineMessage.endsWith("|"))
                                {
                                    singleLineMessage = singleLineMessage
                                            .substring(0, singleLineMessage.length() - 1);
                                }

                                this.challenge.setRepeatRewardText(singleLineMessage);
                            }

                            this.build();
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
            description.add(this.user
                    .getTranslation("challenges.gui.descriptions.admin.repeat-reward-items"));

            Utils.groupEqualItems(this.challenge.getRepeatItemReward()).forEach(itemStack ->
            description.addAll(this.generateItemStackDescription(itemStack)));

            icon = new ItemStack(Material.TRAPPED_CHEST);
            clickHandler = (panel, user, clickType, slot) -> {
                new ItemSwitchGUI(this.user,
                        this.challenge.getRepeatItemReward(),
                        lineLength,
                        (status, value) -> {
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
            name = this.user
                    .getTranslation("challenges.gui.buttons.admin.repeat-reward-experience");
            description = new ArrayList<>(2);
            description.add(this.user
                    .getTranslation("challenges.gui.descriptions.admin.repeat-reward-experience"));
            description
            .add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]", Integer.toString(this.challenge.getRepeatExperienceReward())));

            icon = new ItemStack(Material.GLASS_BOTTLE);
            clickHandler = (panel, user, clickType, slot) -> {
                new NumberGUI(this.user,
                        this.challenge.getRepeatExperienceReward(),
                        0,
                        lineLength,
                        (status, value) -> {
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
            description.add(this.user
                    .getTranslation("challenges.gui.descriptions.admin.repeat-reward-money"));
            description
            .add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]", Integer.toString(this.challenge.getRepeatMoneyReward())));

            icon = new ItemStack(
                    this.addon.isEconomyProvided() ? Material.GOLD_NUGGET : Material.BARRIER);
            clickHandler = (panel, user, clickType, slot) -> {
                new NumberGUI(this.user,
                        this.challenge.getRepeatMoneyReward(),
                        0,
                        lineLength,
                        (status, value) -> {
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
            name =
                    this.user.getTranslation("challenges.gui.buttons.admin.repeat-reward-commands");
            description = new ArrayList<>(this.challenge.getRepeatRewardCommands().size() + 1);
            description.add(this.user
                    .getTranslation("challenges.gui.descriptions.admin.repeat-reward-commands"));

            for (String command : this.challenge.getRepeatRewardCommands())
            {
                description.add(this.user.getTranslation("challenges.gui.descriptions.command",
                        "[command]", command));
            }

            icon = new ItemStack(Material.COMMAND_BLOCK);
            clickHandler = (panel, user, clickType, slot) -> {
                new StringListGUI(this.user,
                        this.challenge.getRepeatRewardCommands(),
                        lineLength,
                        (status, value) -> {
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

        return new PanelItemBuilder().
                icon(icon).
                name(name).
                description(GuiUtils.stringSplit(description, this.lineLength)).
                glow(glow).
                clickHandler(clickHandler).
                build();
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
        DEPLOYED,
        ICON,
        DESCRIPTION,
        ORDER,
        ENVIRONMENT,
        REMOVE_ON_COMPLETE,
    }


    /**
     * Represents different rewards buttons that are used in menus.
     */
    private enum RewardButton
    {
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


    /**
     * Represents different requirement buttons that are used in menus.
     */
    private enum RequirementButton
    {
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

    /**
     * LineLength variable.
     */
    private final int lineLength;
}
