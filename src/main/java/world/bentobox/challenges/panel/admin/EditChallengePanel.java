package world.bentobox.challenges.panel.admin;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Fluid;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.PanelListener;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.requirements.InventoryRequirements;
import world.bentobox.challenges.database.object.requirements.IslandRequirements;
import world.bentobox.challenges.database.object.requirements.OtherRequirements;
import world.bentobox.challenges.database.object.requirements.StatisticRequirements;
import world.bentobox.challenges.panel.CommonPanel;
import world.bentobox.challenges.panel.ConversationUtils;
import world.bentobox.challenges.panel.util.EnvironmentSelector;
import world.bentobox.challenges.panel.util.ItemSelector;
import world.bentobox.challenges.panel.util.MultiBlockSelector;
import world.bentobox.challenges.panel.util.SingleBlockSelector;
import world.bentobox.challenges.panel.util.SingleEntitySelector;
import world.bentobox.challenges.panel.util.StatisticSelector;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;

/**
 * This class contains all necessary methods that creates GUI and allow to edit
 * challenges properties.
 */
public class EditChallengePanel extends CommonPanel {
    // ---------------------------------------------------------------------
    // Section: Constructors
    // ---------------------------------------------------------------------

    /**
     * @param addon            Addon where panel operates.
     * @param world            World from which panel was created.
     * @param user             User who created panel.
     * @param topLabel         Command top label which creates panel (f.e. island or
     *                         ai)
     * @param permissionPrefix Command permission prefix (f.e. bskyblock.)
     * @param challenge        - challenge that needs editing
     */
    private EditChallengePanel(ChallengesAddon addon, User user, World world, String topLabel, String permissionPrefix,
            Challenge challenge) {
        super(addon, user, world, topLabel, permissionPrefix);
        this.challenge = challenge;
        this.currentMenuType = MenuType.PROPERTIES;
    }

    /**
     * @param panel     Parent panel
     * @param challenge challenge that needs editing.
     */
    private EditChallengePanel(CommonPanel panel, Challenge challenge) {
        super(panel);
        this.challenge = challenge;
        // Default panel should be Properties.
        this.currentMenuType = MenuType.PROPERTIES;
    }

    /**
     * Open the Challenges Edit GUI.
     *
     * @param addon            the addon
     * @param world            the world
     * @param user             the user
     * @param topLabel         the top label
     * @param permissionPrefix the permission prefix
     * @param challenge        - challenge that needs editing
     */
    public static void open(ChallengesAddon addon, User user, World world, String topLabel, String permissionPrefix,
            Challenge challenge) {
        new EditChallengePanel(addon, user, world, topLabel, permissionPrefix, challenge).build();
    }

    /**
     * Open the Challenges Edit GUI.
     *
     * @param panel     - Parent Panel
     * @param challenge - challenge that needs editing
     */
    public static void open(CommonPanel panel, Challenge challenge) {
        new EditChallengePanel(panel, challenge).build();
    }

    // ---------------------------------------------------------------------
    // Section: Panel Creation related methods
    // ---------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    protected void build() {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(this.user
                .getTranslation(Constants.TITLE + "edit-challenge", "[challenge]", this.challenge.getFriendlyName()));

        PanelUtils.fillBorder(panelBuilder);

        panelBuilder.item(2, this.createMenuButton(MenuType.PROPERTIES));
        panelBuilder.item(4, this.createMenuButton(MenuType.REQUIREMENTS));
        panelBuilder.item(6, this.createMenuButton(MenuType.REWARDS));

        if (this.currentMenuType.equals(MenuType.PROPERTIES)) {
            this.buildMainPropertiesPanel(panelBuilder);
        } else if (this.currentMenuType.equals(MenuType.REQUIREMENTS)) {
            switch (this.challenge.getChallengeType()) {
            case INVENTORY_TYPE -> this.buildInventoryRequirementsPanel(panelBuilder);
            case ISLAND_TYPE -> this.buildIslandRequirementsPanel(panelBuilder);
            case OTHER_TYPE -> this.buildOtherRequirementsPanel(panelBuilder);
            case STATISTIC_TYPE -> this.buildStatisticRequirementsPanel(panelBuilder);
            }
        } else if (this.currentMenuType.equals(MenuType.REWARDS)) {
            this.buildRewardsPanel(panelBuilder);
        }

        panelBuilder.item(44, this.returnButton);

        // Every time when this GUI is build, save challenge
        // This will ensure that all main things will be always stored
        this.addon.getChallengesManager().saveChallenge(this.challenge);
        // If for some reason challenge is not loaded, do it.
        this.addon.getChallengesManager().loadChallenge(this.challenge, this.world, false, null, true);

        panelBuilder.build();
    }

    /**
     * This class populate ChallengesEditGUI with main challenge settings.
     * 
     * @param panelBuilder PanelBuilder where icons must be added.
     */
    private void buildMainPropertiesPanel(PanelBuilder panelBuilder) {
        panelBuilder.listener(new IconChanger());

        panelBuilder.item(10, this.createButton(Button.NAME));
        panelBuilder.item(16, this.createButton(Button.DEPLOYED));

        panelBuilder.item(19, this.createButton(Button.ICON));
        panelBuilder.item(22, this.createButton(Button.DESCRIPTION));
        panelBuilder.item(25, this.createButton(Button.ORDER));

        panelBuilder.item(28, this.createButton(Button.ENVIRONMENT));
        panelBuilder.item(31, this.createButton(Button.REMOVE_ON_COMPLETE));
    }

    /**
     * This class populates ChallengesEditGUI with island challenges requirement
     * elements.
     * 
     * @param panelBuilder PanelBuilder where icons must be added.
     */
    private void buildIslandRequirementsPanel(PanelBuilder panelBuilder) {
        panelBuilder.item(19, this.createRequirementButton(RequirementButton.REQUIRED_ENTITIES));
        panelBuilder.item(28, this.createRequirementButton(RequirementButton.REMOVE_ENTITIES));

        panelBuilder.item(21, this.createRequirementButton(RequirementButton.REQUIRED_BLOCKS));
        panelBuilder.item(22, this.createRequirementButton(RequirementButton.REQUIRED_MATERIALTAGS));
        panelBuilder.item(30, this.createRequirementButton(RequirementButton.REMOVE_BLOCKS));


        panelBuilder.item(23, this.createRequirementButton(RequirementButton.SEARCH_RADIUS));
        panelBuilder.item(25, this.createRequirementButton(RequirementButton.REQUIRED_PERMISSIONS));
    }

    /**
     * This class populates ChallengesEditGUI with inventory challenges requirement
     * elements.
     * 
     * @param panelBuilder PanelBuilder where icons must be added.
     */
    private void buildInventoryRequirementsPanel(PanelBuilder panelBuilder) {
        panelBuilder.item(10, this.createRequirementButton(RequirementButton.REQUIRED_ITEMS));
        panelBuilder.item(19, this.createRequirementButton(RequirementButton.REMOVE_ITEMS));

        if (!this.challenge.<InventoryRequirements>getRequirements().getRequiredItems().isEmpty()) {
            panelBuilder.item(12, this.createRequirementButton(RequirementButton.ADD_IGNORED_META));

            if (!this.challenge.<InventoryRequirements>getRequirements().getIgnoreMetaData().isEmpty()) {
                panelBuilder.item(21, this.createRequirementButton(RequirementButton.REMOVE_IGNORED_META));
            }
        }

        panelBuilder.item(25, this.createRequirementButton(RequirementButton.REQUIRED_PERMISSIONS));
    }

    /**
     * This class populates ChallengesEditGUI with other challenges requirement
     * elements.
     * 
     * @param panelBuilder PanelBuilder where icons must be added.
     */
    private void buildOtherRequirementsPanel(PanelBuilder panelBuilder) {
        panelBuilder.item(10, this.createRequirementButton(RequirementButton.REQUIRED_EXPERIENCE));
        panelBuilder.item(19, this.createRequirementButton(RequirementButton.REMOVE_EXPERIENCE));

        panelBuilder.item(12, this.createRequirementButton(RequirementButton.REQUIRED_MONEY));
        panelBuilder.item(21, this.createRequirementButton(RequirementButton.REMOVE_MONEY));

        panelBuilder.item(23, this.createRequirementButton(RequirementButton.REQUIRED_LEVEL));

        panelBuilder.item(25, this.createRequirementButton(RequirementButton.REQUIRED_PERMISSIONS));
    }

    /**
     * This class populates ChallengesEditGUI with other challenges requirement
     * elements.
     * 
     * @param panelBuilder PanelBuilder where icons must be added.
     */
    private void buildStatisticRequirementsPanel(PanelBuilder panelBuilder) {
        panelBuilder.item(10, this.createRequirementButton(RequirementButton.STATISTIC));
        panelBuilder.item(19, this.createRequirementButton(RequirementButton.REMOVE_STATISTIC));

        panelBuilder.item(11, this.createRequirementButton(RequirementButton.STATISTIC_AMOUNT));

        StatisticRequirements requirements = this.challenge.getRequirements();

        if (requirements.getStatistic() != null) {
            switch (requirements.getStatistic().getType()) {
            case ITEM -> panelBuilder.item(13, this.createRequirementButton(RequirementButton.STATISTIC_ITEMS));
            case BLOCK -> panelBuilder.item(13, this.createRequirementButton(RequirementButton.STATISTIC_BLOCKS));
            case ENTITY -> panelBuilder.item(13, this.createRequirementButton(RequirementButton.STATISTIC_ENTITIES));
            default -> {
            }
            }
        }

        panelBuilder.item(25, this.createRequirementButton(RequirementButton.REQUIRED_PERMISSIONS));
    }

    /**
     * This class populates ChallengesEditGUI with challenges reward elements.
     * 
     * @param panelBuilder PanelBuilder where icons must be added.
     */
    private void buildRewardsPanel(PanelBuilder panelBuilder) {
        panelBuilder.item(10, this.createRewardButton(RewardButton.REWARD_TEXT));
        panelBuilder.item(19, this.createRewardButton(RewardButton.REWARD_COMMANDS));

        panelBuilder.item(11, this.createRewardButton(RewardButton.REWARD_ITEMS));
        panelBuilder.item(20, this.createRewardButton(RewardButton.REWARD_EXPERIENCE));
        panelBuilder.item(29, this.createRewardButton(RewardButton.REWARD_MONEY));

        panelBuilder.item(22, this.createRewardButton(RewardButton.REPEATABLE));

        if (!this.challenge.getRewardItems().isEmpty() || !this.challenge.getRepeatItemReward().isEmpty()) {
            panelBuilder.item(31, this.createRewardButton(RewardButton.ADD_IGNORED_META));
        }

        if (!this.challenge.getIgnoreRewardMetaData().isEmpty()) {
            panelBuilder.item(32, this.createRewardButton(RewardButton.REMOVE_IGNORED_META));
        }

        if (this.challenge.isRepeatable()) {
            panelBuilder.item(13, this.createRewardButton(RewardButton.COOL_DOWN));
            panelBuilder.item(23, this.createRewardButton(RewardButton.REPEAT_COUNT));

            panelBuilder.item(15, this.createRewardButton(RewardButton.REPEAT_REWARD_TEXT));
            panelBuilder.item(24, this.createRewardButton(RewardButton.REPEAT_REWARD_COMMANDS));

            panelBuilder.item(16, this.createRewardButton(RewardButton.REPEAT_REWARD_ITEMS));
            panelBuilder.item(25, this.createRewardButton(RewardButton.REPEAT_REWARD_EXPERIENCE));
            panelBuilder.item(34, this.createRewardButton(RewardButton.REPEAT_REWARD_MONEY));
        }
    }

    // ---------------------------------------------------------------------
    // Section: Other methods
    // ---------------------------------------------------------------------

    /**
     * This method creates top menu buttons, that allows to switch "tabs".
     * 
     * @param menuType Menu Type which button must be constructed.
     * @return PanelItem that represents given menu type.
     */
    private PanelItem createMenuButton(MenuType menuType) {
        final String reference = Constants.BUTTON + menuType.name().toLowerCase() + ".";

        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(reference + "description"));
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-select"));

        ItemStack icon;
        boolean glow;
        PanelItem.ClickHandler clickHandler;

        switch (menuType) {
        case PROPERTIES -> {
            icon = new ItemStack(Material.CRAFTING_TABLE);
            clickHandler = (panel, user, clickType, slot) -> {
                this.currentMenuType = MenuType.PROPERTIES;
                this.build();

                return true;
            };
            glow = this.currentMenuType.equals(MenuType.PROPERTIES);
        }
        case REQUIREMENTS -> {
            icon = new ItemStack(Material.HOPPER);
            clickHandler = (panel, user, clickType, slot) -> {
                this.currentMenuType = MenuType.REQUIREMENTS;
                this.build();

                return true;
            };
            glow = this.currentMenuType.equals(MenuType.REQUIREMENTS);
        }
        case REWARDS -> {
            icon = new ItemStack(Material.DROPPER);
            clickHandler = (panel, user, clickType, slot) -> {
                this.currentMenuType = MenuType.REWARDS;
                this.build();

                return true;
            };
            glow = this.currentMenuType.equals(MenuType.REWARDS);
        }
        default -> {
            icon = new ItemStack(Material.PAPER);
            clickHandler = null;
            glow = false;
        }
        }

        return new PanelItemBuilder().icon(icon).name(name).description(description).glow(glow)
                .clickHandler(clickHandler).build();
    }

    /**
     * This method creates buttons for default main menu.
     * 
     * @param button Button which panel item must be created.
     * @return PanelItem that represents given button.
     */
    private PanelItem createButton(Button button) {
        final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";

        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(reference + "description"));

        ItemStack icon;
        boolean glow;
        PanelItem.ClickHandler clickHandler;

        switch (button) {
        case NAME -> {
            description.add(this.user.getTranslation(reference + "value", Constants.PARAMETER_NAME,
                    this.challenge.getFriendlyName()));

            icon = new ItemStack(Material.NAME_TAG);

            clickHandler = (panel, user, clickType, i) -> {
                // Create consumer that process description change
                Consumer<String> consumer = value -> {
                    if (value != null) {
                        this.challenge.setFriendlyName(value);
                    }

                    this.build();
                };

                // start conversation
                ConversationUtils.createStringInput(consumer, user,
                        user.getTranslation(Constants.CONVERSATIONS + "write-name"),
                        user.getTranslation(Constants.CONVERSATIONS + "name-changed"));

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case DEPLOYED -> {
            description
                    .add(this.user.getTranslation(reference + (this.challenge.isDeployed() ? "enabled" : "disabled")));

            icon = new ItemStack(Material.LEVER);
            clickHandler = (panel, user, clickType, slot) -> {
                if (this.challenge.isValid()) {
                    this.challenge.setDeployed(!this.challenge.isDeployed());
                } else {
                    Utils.sendMessage(this.user, this.world, Constants.CONVERSATIONS + "invalid-challenge",
                            Constants.PARAMETER_CHALLENGE, this.challenge.getFriendlyName());
                    this.challenge.setDeployed(false);
                }

                this.build();
                return true;
            };
            glow = this.challenge.isDeployed();

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
        }
        case ICON -> {
            icon = this.challenge.getIcon();
            clickHandler = (panel, user, clickType, i) -> {
                this.selectedButton = button;
                this.build();
                return true;
            };
            glow = this.selectedButton == button;

            if (this.selectedButton != button) {
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            } else {
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-on-item"));
            }
        }
        case DESCRIPTION -> {
            icon = new ItemStack(Material.WRITTEN_BOOK);

            description.add(this.user.getTranslation(reference + "value"));
            this.challenge.getDescription().forEach(line -> description.add(Util.translateColorCodes(line)));

            clickHandler = (panel, user, clickType, i) -> {
                // Create consumer that process description change
                Consumer<List<String>> consumer = value -> {
                    if (value != null) {
                        this.challenge.setDescription(value);
                    }

                    this.build();
                };

                if (!this.challenge.getDescription().isEmpty() && clickType.isShiftClick()) {
                    // Reset to the empty value
                    consumer.accept(Collections.emptyList());
                } else {
                    // start conversation
                    ConversationUtils.createStringListInput(consumer, user,
                            user.getTranslation(Constants.CONVERSATIONS + "write-description"),
                            user.getTranslation(Constants.CONVERSATIONS + "description-changed"));
                }

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

            if (!this.challenge.getDescription().isEmpty()) {
                description.add(this.user.getTranslation(Constants.TIPS + "shift-click-to-reset"));
            }
        }
        case ORDER -> {
            description.add(this.user.getTranslation(reference + "value", Constants.PARAMETER_NUMBER,
                    String.valueOf(this.challenge.getOrder())));

            icon = new ItemStack(Material.HOPPER, Math.max(1, this.challenge.getOrder()));
            clickHandler = (panel, user, clickType, i) -> {
                Consumer<Number> numberConsumer = number -> {
                    if (number != null) {
                        this.challenge.setOrder(number.intValue());
                    }

                    // reopen panel
                    this.build();
                };

                ConversationUtils.createNumericInput(numberConsumer, this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"), 0, 2000);

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case ENVIRONMENT -> {
            description.add(this.user.getTranslation(
                    this.challenge.getEnvironment().contains(World.Environment.NORMAL) ? reference + "enabled"
                            : reference + "disabled")
                    + Utils.prettifyObject(World.Environment.NORMAL, this.user));
            description.add(this.user.getTranslation(
                    this.challenge.getEnvironment().contains(World.Environment.NETHER) ? reference + "enabled"
                            : reference + "disabled")
                    + Utils.prettifyObject(World.Environment.NETHER, this.user));
            description.add(this.user.getTranslation(
                    this.challenge.getEnvironment().contains(World.Environment.THE_END) ? reference + "enabled"
                            : reference + "disabled")
                    + Utils.prettifyObject(World.Environment.THE_END, this.user));

            icon = new ItemStack(Material.DROPPER);
            clickHandler = (panel, user, clickType, slot) -> {
                EnvironmentSelector.open(this.user, this.challenge.getEnvironment(), (status, value) -> {
                    if (status) {
                        this.challenge.setEnvironment(value);
                    }

                    this.build();
                });

                return true;
            };
            glow = false;
        }
        case REMOVE_ON_COMPLETE -> {
            description.add(this.user
                    .getTranslation(reference + (this.challenge.isRemoveWhenCompleted() ? "enabled" : "disabled")));

            if (this.challenge.isRemoveWhenCompleted()) {
                icon = new ItemStack(Material.LAVA_BUCKET);
            } else {
                icon = new ItemStack(Material.BUCKET);
            }

            clickHandler = (panel, user, clickType, slot) -> {
                this.challenge.setRemoveWhenCompleted(!this.challenge.isRemoveWhenCompleted());
                this.build();

                return true;
            };
            glow = this.challenge.isRemoveWhenCompleted();

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
        }
        default -> {
            icon = new ItemStack(Material.PAPER);
            clickHandler = null;
            glow = false;
        }
        }

        return new PanelItemBuilder().icon(icon).name(name).description(description).glow(glow)
                .clickHandler(clickHandler).build();
    }

    /**
     * This method creates buttons for requirements menu.
     * 
     * @param button Button which panel item must be created.
     * @return PanelItem that represents given button.
     */
    private PanelItem createRequirementButton(RequirementButton button) {
        switch (button) {
        case REQUIRED_PERMISSIONS -> {
            String reference = Constants.BUTTON + button.name().toLowerCase() + ".";

            String name = this.user.getTranslation(reference + "name");
            final List<String> description = new ArrayList<>(3);
            description.add(this.user.getTranslation(reference + "description"));

            if (this.challenge.getRequirements().getRequiredPermissions().isEmpty()) {
                description.add(this.user.getTranslation(reference + "none"));
            } else {
                description.add(this.user.getTranslation(reference + "title"));

                this.challenge.getRequirements().getRequiredPermissions().forEach(permission -> description
                        .add(this.user.getTranslation(reference + "permission", "[permission]", permission)));
            }

            ItemStack icon = new ItemStack(Material.REDSTONE_LAMP);

            PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
                // Create consumer that process description change
                Consumer<List<String>> consumer = value -> {
                    if (value != null) {
                        this.challenge.getRequirements().setRequiredPermissions(new HashSet<>(value));
                    }

                    this.build();
                };

                if (!this.challenge.getRequirements().getRequiredPermissions().isEmpty() && clickType.isShiftClick()) {
                    // Reset to the empty value
                    consumer.accept(Collections.emptyList());
                } else {
                    // start conversation
                    ConversationUtils.createStringListInput(consumer, user,
                            user.getTranslation(Constants.CONVERSATIONS + "write-permissions"),
                            user.getTranslation(Constants.CONVERSATIONS + "permissions-changed"));
                }

                return true;
            };

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

            if (!this.challenge.getRequirements().getRequiredPermissions().isEmpty()) {
                description.add(this.user.getTranslation(Constants.TIPS + "shift-click-to-reset"));
            }

            return new PanelItemBuilder().icon(icon).name(name).description(description).clickHandler(clickHandler)
                    .build();
        }
        // Buttons for Island Requirements
        case REQUIRED_ENTITIES, REMOVE_ENTITIES, REQUIRED_BLOCKS, REMOVE_BLOCKS, SEARCH_RADIUS,
                REQUIRED_MATERIALTAGS -> {
            return this.createIslandRequirementButton(button);
        }
        // Buttons for Inventory Requirements
        case REQUIRED_ITEMS, REMOVE_ITEMS, ADD_IGNORED_META, REMOVE_IGNORED_META -> {
            return this.createInventoryRequirementButton(button);
        }
        // Buttons for Other Requirements
        case REQUIRED_EXPERIENCE, REMOVE_EXPERIENCE, REQUIRED_LEVEL, REQUIRED_MONEY, REMOVE_MONEY -> {
            return this.createOtherRequirementButton(button);
        }
        // Buttons for Statistic Requirements
        case STATISTIC, STATISTIC_BLOCKS, STATISTIC_ITEMS, STATISTIC_ENTITIES, STATISTIC_AMOUNT, REMOVE_STATISTIC -> {
            return this.createStatisticRequirementButton(button);
        }
        // Default behaviour.
        default -> {
            return PanelItem.empty();
        }
        }
    }

    /**
     * This method creates buttons for island requirements menu.
     * 
     * @param button Button which panel item must be created.
     * @return PanelItem that represents given button.
     */
    private PanelItem createIslandRequirementButton(RequirementButton button) {
        final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";

        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(reference + "description"));

        ItemStack icon;
        boolean glow;
        PanelItem.ClickHandler clickHandler;

        final IslandRequirements requirements = this.challenge.getRequirements();

        switch (button) {
        case REQUIRED_ENTITIES -> {
            if (requirements.getRequiredEntities().isEmpty()) {
                description.add(this.user.getTranslation(reference + "none"));
            } else {
                description.add(this.user.getTranslation(reference + "title"));

                requirements.getRequiredEntities().forEach(
                        (entity, count) -> description.add(this.user.getTranslation(reference + "list", "[entity]",
                                Utils.prettifyObject(entity, this.user), "[number]", String.valueOf(count))));
            }

            icon = new ItemStack(Material.CREEPER_HEAD);
            clickHandler = (panel, user, clickType, slot) -> {
                ManageEntitiesPanel.open(this, requirements.getRequiredEntities());
                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case REMOVE_ENTITIES -> {
            description.add(
                    this.user.getTranslation(reference + (requirements.isRemoveEntities() ? "enabled" : "disabled")));

            icon = new ItemStack(Material.LEVER);
            clickHandler = (panel, user, clickType, slot) -> {
                requirements.setRemoveEntities(!requirements.isRemoveEntities());
                this.build();
                return true;
            };
            glow = requirements.isRemoveEntities();

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
        }
        case REQUIRED_MATERIALTAGS -> {
            if (requirements.getRequiredMaterialTags().isEmpty()) {
                description.add(this.user.getTranslation(reference + "none"));
            } else {
                description.add(this.user.getTranslation(reference + "title"));
                // Add Material Tags only
                requirements.getRequiredMaterialTags()
                        .forEach((block, count) -> description.add(this.user.getTranslation(reference + "list",
                                "[block]", Utils.prettifyObject(block, this.user), "[number]", String.valueOf(count))));
            }

            icon = new ItemStack(Material.STONE_BRICKS);
            clickHandler = (panel, user, clickType, slot) -> {
                ManageTagsPanel.open(this, requirements.getRequiredMaterialTags());
                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }

        case REQUIRED_BLOCKS -> {
            if (requirements.getRequiredBlocks().isEmpty()) {
                description.add(this.user.getTranslation(reference + "none"));
            } else {
                description.add(this.user.getTranslation(reference + "title"));
                requirements.getRequiredBlocks()
                        .forEach((block, count) -> description.add(this.user.getTranslation(reference + "list",
                                "[block]", Utils.prettifyObject(block, this.user), "[number]", String.valueOf(count))));
            }

            icon = new ItemStack(Material.STONE);
            clickHandler = (panel, user, clickType, slot) -> {
                ManageBlocksPanel.open(this, requirements.getRequiredBlocks());
                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case REMOVE_BLOCKS -> {
            description.add(
                    this.user.getTranslation(reference + (requirements.isRemoveBlocks() ? "enabled" : "disabled")));

            icon = new ItemStack(Material.LEVER);
            clickHandler = (panel, user, clickType, slot) -> {
                requirements.setRemoveBlocks(!requirements.isRemoveBlocks());
                this.build();
                return true;
            };
            glow = requirements.isRemoveBlocks();

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
        }
        case SEARCH_RADIUS -> {
            description.add(this.user.getTranslation(reference + "value", Constants.PARAMETER_NUMBER,
                    String.valueOf(requirements.getSearchRadius())));
            icon = new ItemStack(Material.COBBLESTONE_WALL);
            clickHandler = (panel, user, clickType, i) -> {
                Consumer<Number> numberConsumer = number -> {
                    if (number != null) {
                        requirements.setSearchRadius(number.intValue());
                    }

                    // reopen panel
                    this.build();
                };

                int maxSearchDistance = this.addon.getPlugin().getIWM().getAddon(this.world)
                        .map(gameModeAddon -> gameModeAddon.getWorldSettings().getIslandDistance()).orElse(100);

                ConversationUtils.createNumericInput(numberConsumer, this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"), 1, maxSearchDistance);

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        default -> {
            icon = new ItemStack(Material.PAPER);
            clickHandler = null;
            glow = false;
        }
        }

        return new PanelItemBuilder().icon(icon).name(name).description(description).glow(glow)
                .clickHandler(clickHandler).build();
    }

    /**
     * This method creates buttons for inventory requirements menu.
     * 
     * @param button Button which panel item must be created.
     * @return PanelItem that represents given button.
     */
    private PanelItem createInventoryRequirementButton(RequirementButton button) {
        final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";

        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(reference + "description"));

        ItemStack icon;
        boolean glow;
        PanelItem.ClickHandler clickHandler;

        final InventoryRequirements requirements = this.challenge.getRequirements();

        switch (button) {
        case REQUIRED_ITEMS -> {
            if (requirements.getRequiredItems().isEmpty()) {
                description.add(this.user.getTranslation(reference + "none"));
            } else {
                description.add(this.user.getTranslation(reference + "title"));

                Utils.groupEqualItems(requirements.getRequiredItems(), requirements.getIgnoreMetaData()).stream()
                        .sorted(Comparator.comparing(ItemStack::getType))
                        .forEach(itemStack -> description.add(this.user.getTranslationOrNothing(reference + "list",
                                "[number]", String.valueOf(itemStack.getAmount()), "[item]",
                                Utils.prettifyObject(itemStack, this.user))));
            }

            icon = new ItemStack(Material.CHEST);
            clickHandler = (panel, user, clickType, slot) -> {
                ItemSelector.open(this.user, requirements.getRequiredItems(), (status, value) -> {
                    if (status) {
                        requirements.setRequiredItems(value);
                    }

                    this.build();
                });
                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case REMOVE_ITEMS -> {
            description
                    .add(this.user.getTranslation(reference + (requirements.isTakeItems() ? "enabled" : "disabled")));

            icon = new ItemStack(Material.LEVER);
            clickHandler = (panel, user, clickType, slot) -> {
                requirements.setTakeItems(!requirements.isTakeItems());
                this.build();
                return true;
            };
            glow = requirements.isTakeItems();

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
        }
        case ADD_IGNORED_META -> {
            if (requirements.getIgnoreMetaData().isEmpty()) {
                description.add(this.user.getTranslation(reference + "none"));
            } else {
                description.add(this.user.getTranslation(reference + "title"));

                requirements.getIgnoreMetaData().stream().sorted(Comparator.comparing(Material::name))
                        .forEach(itemStack -> description.add(this.user.getTranslationOrNothing(reference + "list",
                                "[item]", Utils.prettifyObject(itemStack, this.user))));
            }

            icon = new ItemStack(Material.GREEN_SHULKER_BOX);

            clickHandler = (panel, user, clickType, slot) -> {
                if (requirements.getRequiredItems().isEmpty()) {
                    // Do nothing if no requirements are set.
                    return true;
                }

                // Allow choosing only from inventory items.
                Set<Material> collection = Arrays.stream(Material.values()).collect(Collectors.toSet());
                requirements.getRequiredItems().stream().map(ItemStack::getType).forEach(collection::remove);
                collection.addAll(requirements.getIgnoreMetaData());

                if (Material.values().length == collection.size()) {
                    // If there are no items anymore, then do not allow opening gui.
                    return true;
                }

                MultiBlockSelector.open(this.user, MultiBlockSelector.Mode.ANY, collection, (status, materials) -> {
                    if (status) {
                        materials.addAll(requirements.getIgnoreMetaData());
                        requirements.setIgnoreMetaData(new HashSet<>(materials));
                    }

                    this.build();
                });
                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-add"));
        }
        case REMOVE_IGNORED_META -> {
            icon = new ItemStack(Material.RED_SHULKER_BOX);

            clickHandler = (panel, user, clickType, slot) -> {
                if (requirements.getIgnoreMetaData().isEmpty()) {
                    // Do nothing if no requirements are set.
                    return true;
                }

                // Allow choosing only from inventory items.
                Set<Material> collection = Arrays.stream(Material.values()).collect(Collectors.toSet());
                collection.removeAll(requirements.getIgnoreMetaData());

                MultiBlockSelector.open(this.user, MultiBlockSelector.Mode.ANY, collection, (status, materials) -> {
                    if (status) {
                        requirements.getIgnoreMetaData().removeAll(materials);
                    }

                    this.build();
                });
                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-remove"));
        }
        default -> {
            icon = new ItemStack(Material.PAPER);
            clickHandler = null;
            glow = false;
        }
        }

        return new PanelItemBuilder().icon(icon).name(name).description(description).glow(glow)
                .clickHandler(clickHandler).build();
    }

    /**
     * This method creates buttons for other requirements menu.
     * 
     * @param button Button which panel item must be created.
     * @return PanelItem that represents given button.
     */
    private PanelItem createOtherRequirementButton(RequirementButton button) {
        final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";

        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(reference + "description"));

        ItemStack icon;
        boolean glow;
        PanelItem.ClickHandler clickHandler;

        final OtherRequirements requirements = this.challenge.getRequirements();

        switch (button) {
        case REQUIRED_EXPERIENCE -> {
            description.add(this.user.getTranslation(reference + "value", Constants.PARAMETER_NUMBER,
                    String.valueOf(requirements.getRequiredExperience())));
            icon = new ItemStack(Material.EXPERIENCE_BOTTLE);
            clickHandler = (panel, user, clickType, i) -> {
                Consumer<Number> numberConsumer = number -> {
                    if (number != null) {
                        requirements.setRequiredExperience(number.intValue());
                    }

                    // reopen panel
                    this.build();
                };
                ConversationUtils.createNumericInput(numberConsumer, this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"), 0, Integer.MAX_VALUE);

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case REMOVE_EXPERIENCE -> {
            description.add(
                    this.user.getTranslation(reference + (requirements.isTakeExperience() ? "enabled" : "disabled")));

            icon = new ItemStack(Material.LEVER);
            clickHandler = (panel, user, clickType, slot) -> {
                requirements.setTakeExperience(!requirements.isTakeExperience());
                this.build();
                return true;
            };
            glow = requirements.isTakeExperience();

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
        }
        case REQUIRED_LEVEL -> {
            description.add(this.user.getTranslation(reference + "value", Constants.PARAMETER_NUMBER,
                    String.valueOf(requirements.getRequiredIslandLevel())));
            icon = new ItemStack(this.addon.isLevelProvided() ? Material.BEACON : Material.BARRIER);
            clickHandler = (panel, user, clickType, i) -> {
                Consumer<Number> numberConsumer = number -> {
                    if (number != null) {
                        requirements.setRequiredIslandLevel(number.longValue());
                    }

                    // reopen panel
                    this.build();
                };
                ConversationUtils.createNumericInput(numberConsumer, this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"), 0, Integer.MAX_VALUE);

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case REQUIRED_MONEY -> {
            description.add(this.user.getTranslation(reference + "value", Constants.PARAMETER_NUMBER,
                    String.valueOf(requirements.getRequiredMoney())));
            icon = new ItemStack(this.addon.isEconomyProvided() ? Material.GOLD_INGOT : Material.BARRIER);
            clickHandler = (panel, user, clickType, i) -> {
                Consumer<Number> numberConsumer = number -> {
                    if (number != null) {
                        requirements.setRequiredMoney(number.doubleValue());
                    }

                    // reopen panel
                    this.build();
                };
                ConversationUtils.createNumericInput(numberConsumer, this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"), 0, Double.MAX_VALUE);

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case REMOVE_MONEY -> {
            description
                    .add(this.user.getTranslation(reference + (requirements.isTakeMoney() ? "enabled" : "disabled")));

            icon = new ItemStack(this.addon.isEconomyProvided() ? Material.LEVER : Material.BARRIER);
            clickHandler = (panel, user, clickType, slot) -> {
                requirements.setTakeMoney(!requirements.isTakeMoney());
                this.build();
                return true;
            };
            glow = requirements.isTakeMoney();

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
        }
        default -> {
            icon = new ItemStack(Material.PAPER);
            clickHandler = null;
            glow = false;
        }
        }

        return new PanelItemBuilder().icon(icon).name(name).description(description).glow(glow)
                .clickHandler(clickHandler).build();
    }

    /**
     * Creates a button for statistic requirements.
     * 
     * @param button Button that must be created.
     * @return PanelItem button.
     */
    private PanelItem createStatisticRequirementButton(RequirementButton button) {
        final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";

        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(reference + "description"));

        ItemStack icon;
        boolean glow;
        PanelItem.ClickHandler clickHandler;

        final StatisticRequirements requirements = this.challenge.getRequirements();

        switch (button) {
        case STATISTIC -> {
            description.add(this.user.getTranslation(reference + "value", "[statistic]",
                    Utils.prettifyObject(requirements.getStatistic(), this.user)));

            icon = new ItemStack(requirements.getStatistic() == null ? Material.BARRIER : Material.PAPER);
            clickHandler = (panel, user, clickType, slot) -> {
                StatisticSelector.open(this.user, (status, statistic) -> {
                    if (status) {
                        requirements.setStatistic(statistic);
                        requirements.setMaterial(null);
                        requirements.setEntity(null);
                        requirements.setAmount(0);
                    }

                    this.build();
                });
                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case STATISTIC_AMOUNT -> {
            description.add(this.user.getTranslation(reference + "value", Constants.PARAMETER_NUMBER,
                    String.valueOf(requirements.getAmount())));
            icon = new ItemStack(Material.CHEST);
            clickHandler = (panel, user, clickType, i) -> {
                Consumer<Number> numberConsumer = number -> {
                    if (number != null) {
                        requirements.setAmount(number.intValue());
                    }

                    // reopen panel
                    this.build();
                };
                ConversationUtils.createNumericInput(numberConsumer, this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"), 0, Integer.MAX_VALUE);

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case REMOVE_STATISTIC -> {
            description.add(
                    this.user.getTranslation(reference + (requirements.isReduceStatistic() ? "enabled" : "disabled")));

            icon = new ItemStack(Material.LEVER);
            clickHandler = (panel, user, clickType, slot) -> {
                requirements.setReduceStatistic(!requirements.isReduceStatistic());
                this.build();
                return true;
            };
            glow = requirements.isReduceStatistic();

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
        }
        case STATISTIC_BLOCKS -> {
            description.add(this.user.getTranslation(reference + "value", "[block]",
                    Utils.prettifyObject(requirements.getMaterial(), this.user)));

            icon = requirements.getMaterial() == null ? new ItemStack(Material.BARRIER)
                    : new ItemStack(requirements.getMaterial());
            clickHandler = (panel, user, clickType, slot) -> {
                SingleBlockSelector.open(this.user, SingleBlockSelector.Mode.BLOCKS, (status, block) -> {
                    if (status) {
                        requirements.setMaterial(block);
                    }

                    this.build();
                });

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case STATISTIC_ITEMS -> {
            description.add(this.user.getTranslation(reference + "value", "[item]",
                    Utils.prettifyObject(requirements.getMaterial(), this.user)));

            icon = requirements.getMaterial() == null ? new ItemStack(Material.BARRIER)
                    : new ItemStack(requirements.getMaterial());
            clickHandler = (panel, user, clickType, slot) -> {
                SingleBlockSelector.open(this.user, SingleBlockSelector.Mode.ITEMS, (status, block) -> {
                    if (status) {
                        requirements.setMaterial(block);
                    }

                    this.build();
                });

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case STATISTIC_ENTITIES -> {
            description.add(this.user.getTranslation(reference + "value", "[entity]",
                    Utils.prettifyObject(requirements.getEntity(), this.user)));

            icon = requirements.getEntity() == null ? new ItemStack(Material.BARRIER)
                    : new ItemStack(PanelUtils.getEntityEgg(requirements.getEntity()));
            clickHandler = (panel, user, clickType, slot) -> {
                SingleEntitySelector.open(this.user, true, (status, entity) -> {
                    if (status) {
                        requirements.setEntity(entity);
                    }

                    this.build();
                });

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        default -> {
            icon = new ItemStack(Material.PAPER);
            clickHandler = null;
            glow = false;
        }
        }

        return new PanelItemBuilder().icon(icon).name(name).description(description).glow(glow)
                .clickHandler(clickHandler).build();
    }

    /**
     * This method creates buttons for rewards menu.
     * 
     * @param button Button which panel item must be created.
     * @return PanelItem that represents given button.
     */
    private PanelItem createRewardButton(RewardButton button) {
        final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";

        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(reference + "description"));

        ItemStack icon;
        boolean glow;
        PanelItem.ClickHandler clickHandler;

        switch (button) {
        case REWARD_TEXT -> {
            icon = new ItemStack(Material.WRITTEN_BOOK);

            description.add(this.user.getTranslation(reference + "value"));
            description.add(Util.translateColorCodes(this.challenge.getRewardText()));

            clickHandler = (panel, user, clickType, i) -> {
                // Create consumer that process description change
                Consumer<List<String>> consumer = value -> {
                    if (value != null) {
                        this.challenge.setRewardText(String.join("\n", value));
                    }

                    this.build();
                };

                if (!this.challenge.getRewardText().isEmpty() && clickType.isShiftClick()) {
                    // Reset to the empty value
                    consumer.accept(Collections.emptyList());
                } else {
                    // start conversation
                    ConversationUtils.createStringListInput(consumer, user,
                            user.getTranslation(Constants.CONVERSATIONS + "write-reward-text"),
                            user.getTranslation(Constants.CONVERSATIONS + "reward-text-changed"));
                }

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

            if (!this.challenge.getRewardText().isEmpty()) {
                description.add(this.user.getTranslation(Constants.TIPS + "shift-click-to-reset"));
            }
        }
        case REWARD_ITEMS -> {

            if (this.challenge.getRewardItems().isEmpty()) {
                description.add(this.user.getTranslation(reference + "none"));
            } else {
                description.add(this.user.getTranslation(reference + "title"));

                Utils.groupEqualItems(this.challenge.getRewardItems(), this.challenge.getIgnoreRewardMetaData())
                        .stream().sorted(Comparator.comparing(ItemStack::getType))
                        .forEach(itemStack -> description.add(this.user.getTranslationOrNothing(reference + "list",
                                "[number]", String.valueOf(itemStack.getAmount()), "[item]",
                                Utils.prettifyObject(itemStack, this.user))));
            }

            icon = new ItemStack(Material.CHEST);
            clickHandler = (panel, user, clickType, slot) -> {
                ItemSelector.open(this.user, this.challenge.getRewardItems(), (status, value) -> {
                    if (status) {
                        this.challenge.setRewardItems(value);
                    }

                    this.build();
                });

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case REWARD_EXPERIENCE -> {
            description.add(this.user.getTranslation(reference + "value", Constants.PARAMETER_NUMBER,
                    String.valueOf(this.challenge.getRewardExperience())));
            icon = new ItemStack(Material.EXPERIENCE_BOTTLE);
            clickHandler = (panel, user, clickType, i) -> {
                Consumer<Number> numberConsumer = number -> {
                    if (number != null) {
                        this.challenge.setRewardExperience(number.intValue());
                    }

                    // reopen panel
                    this.build();
                };
                ConversationUtils.createNumericInput(numberConsumer, this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"), 0, Integer.MAX_VALUE);

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case REWARD_MONEY -> {
            description.add(this.user.getTranslation(reference + "value", Constants.PARAMETER_NUMBER,
                    addon.getPlugin().getVault().map(v -> v.format(challenge.getRewardMoney()))
                            .orElse(String.valueOf(challenge.getRewardMoney()))));
            icon = new ItemStack(this.addon.isEconomyProvided() ? Material.GOLD_INGOT : Material.BARRIER);
            clickHandler = (panel, user, clickType, i) -> {
                Consumer<Number> numberConsumer = number -> {
                    if (number != null) {
                        this.challenge.setRewardMoney(number.doubleValue());
                    }

                    // reopen panel
                    this.build();
                };
                ConversationUtils.createNumericInput(numberConsumer, this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"), 0, Double.MAX_VALUE);

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case REWARD_COMMANDS -> {
            icon = new ItemStack(Material.COMMAND_BLOCK);

            description.add(this.user.getTranslation(reference + "value"));
            description.addAll(this.challenge.getRewardCommands());

            clickHandler = (panel, user, clickType, i) -> {
                // Create consumer that process description change
                Consumer<List<String>> consumer = value -> {
                    if (value != null) {
                        this.challenge.setRewardCommands(value);
                    }

                    this.build();
                };

                if (!this.challenge.getRewardCommands().isEmpty() && clickType.isShiftClick()) {
                    // Reset to the empty value
                    consumer.accept(Collections.emptyList());
                } else {
                    // start conversation
                    ConversationUtils.createStringListInput(consumer, user,
                            user.getTranslation(Constants.CONVERSATIONS + "write-reward-commands"),
                            user.getTranslation(Constants.CONVERSATIONS + "reward-commands-changed"));
                }

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

            if (!this.challenge.getRewardCommands().isEmpty()) {
                description.add(this.user.getTranslation(Constants.TIPS + "shift-click-to-reset"));
            }
        }
        case REPEATABLE -> {
            description.add(
                    this.user.getTranslation(reference + (this.challenge.isRepeatable() ? "enabled" : "disabled")));

            icon = new ItemStack(Material.LEVER);
            clickHandler = (panel, user, clickType, slot) -> {
                this.challenge.setRepeatable(!this.challenge.isRepeatable());
                this.build();
                return true;
            };
            glow = this.challenge.isRepeatable();

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
        }
        case REPEAT_COUNT -> {
            description.add(this.user.getTranslation(reference + "value", Constants.PARAMETER_NUMBER,
                    String.valueOf(this.challenge.getMaxTimes())));
            icon = new ItemStack(Material.COBBLESTONE_WALL);
            clickHandler = (panel, user, clickType, i) -> {
                Consumer<Number> numberConsumer = number -> {
                    if (number != null) {
                        this.challenge.setMaxTimes(number.intValue());
                    }

                    // reopen panel
                    this.build();
                };
                ConversationUtils.createNumericInput(numberConsumer, this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"), 0, Integer.MAX_VALUE);

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case COOL_DOWN -> {
            description.add(this.user.getTranslation(reference + "value", "[time]",
                    Utils.parseDuration(Duration.ofMillis(this.challenge.getTimeout()), this.user)));
            icon = new ItemStack(Material.CLOCK);
            clickHandler = (panel, user, clickType, i) -> {
                Consumer<Number> numberConsumer = number -> {
                    if (number != null) {
                        this.challenge.setTimeout(number.longValue() * 1000);
                    }

                    // reopen panel
                    this.build();
                };
                ConversationUtils.createNumericInput(numberConsumer, this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-seconds"), 0, Integer.MAX_VALUE);

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case REPEAT_REWARD_TEXT -> {
            icon = new ItemStack(Material.WRITTEN_BOOK);

            description.add(this.user.getTranslation(reference + "value"));
            description.add(Util.translateColorCodes(this.challenge.getRepeatRewardText()));

            clickHandler = (panel, user, clickType, i) -> {
                // Create consumer that process description change
                Consumer<List<String>> consumer = value -> {
                    if (value != null) {
                        this.challenge.setRepeatRewardText(String.join("\n", value));
                    }

                    this.build();
                };

                if (!this.challenge.getRepeatRewardText().isEmpty() && clickType.isShiftClick()) {
                    // Reset to the empty value
                    consumer.accept(Collections.emptyList());
                } else {
                    // start conversation
                    ConversationUtils.createStringListInput(consumer, user,
                            user.getTranslation(Constants.CONVERSATIONS + "write-repeat-reward-text"),
                            user.getTranslation(Constants.CONVERSATIONS + "repeat-reward-text-changed"));
                }

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

            if (!this.challenge.getRepeatRewardText().isEmpty()) {
                description.add(this.user.getTranslation(Constants.TIPS + "shift-click-to-reset"));
            }
        }
        case REPEAT_REWARD_ITEMS -> {

            if (this.challenge.getRepeatItemReward().isEmpty()) {
                description.add(this.user.getTranslation(reference + "none"));
            } else {
                description.add(this.user.getTranslation(reference + "title"));

                Utils.groupEqualItems(this.challenge.getRepeatItemReward(), this.challenge.getIgnoreRewardMetaData())
                        .stream().sorted(Comparator.comparing(ItemStack::getType))
                        .forEach(itemStack -> description.add(this.user.getTranslationOrNothing(reference + "list",
                                "[number]", String.valueOf(itemStack.getAmount()), "[item]",
                                Utils.prettifyObject(itemStack, this.user))));
            }

            icon = new ItemStack(Material.CHEST);
            clickHandler = (panel, user, clickType, slot) -> {
                ItemSelector.open(this.user, this.challenge.getRewardItems(), (status, value) -> {
                    if (status) {
                        this.challenge.setRepeatItemReward(value);
                    }

                    this.build();
                });

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case REPEAT_REWARD_EXPERIENCE -> {
            description.add(this.user.getTranslation(reference + "value", Constants.PARAMETER_NUMBER,
                    String.valueOf(this.challenge.getRepeatExperienceReward())));
            icon = new ItemStack(Material.EXPERIENCE_BOTTLE);
            clickHandler = (panel, user, clickType, i) -> {
                Consumer<Number> numberConsumer = number -> {
                    if (number != null) {
                        this.challenge.setRepeatExperienceReward(number.intValue());
                    }

                    // reopen panel
                    this.build();
                };
                ConversationUtils.createNumericInput(numberConsumer, this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"), 0, Integer.MAX_VALUE);

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case REPEAT_REWARD_MONEY -> {
            description.add(this.user.getTranslation(reference + "value", Constants.PARAMETER_NUMBER,
                    String.valueOf(this.challenge.getRepeatMoneyReward())));
            icon = new ItemStack(this.addon.isEconomyProvided() ? Material.GOLD_NUGGET : Material.BARRIER);
            clickHandler = (panel, user, clickType, i) -> {
                Consumer<Number> numberConsumer = number -> {
                    if (number != null) {
                        this.challenge.setRepeatMoneyReward(number.doubleValue());
                    }

                    // reopen panel
                    this.build();
                };
                ConversationUtils.createNumericInput(numberConsumer, this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"), 0, Double.MAX_VALUE);

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
        }
        case REPEAT_REWARD_COMMANDS -> {
            icon = new ItemStack(Material.COMMAND_BLOCK);

            description.add(this.user.getTranslation(reference + "value"));
            description.addAll(this.challenge.getRepeatRewardCommands());

            clickHandler = (panel, user, clickType, i) -> {
                // Create consumer that process description change
                Consumer<List<String>> consumer = value -> {
                    if (value != null) {
                        this.challenge.setRepeatRewardCommands(value);
                    }

                    this.build();
                };

                if (!this.challenge.getRepeatRewardCommands().isEmpty() && clickType.isShiftClick()) {
                    // Reset to the empty value
                    consumer.accept(Collections.emptyList());
                } else {
                    // start conversation
                    ConversationUtils.createStringListInput(consumer, user,
                            user.getTranslation(Constants.CONVERSATIONS + "write-repeat-reward-commands"),
                            user.getTranslation(Constants.CONVERSATIONS + "repeat-reward-commands-changed"));
                }

                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

            if (!this.challenge.getRepeatRewardCommands().isEmpty()) {
                description.add(this.user.getTranslation(Constants.TIPS + "shift-click-to-reset"));
            }
        }
        case ADD_IGNORED_META -> {
            if (this.challenge.getIgnoreRewardMetaData().isEmpty()) {
                description.add(this.user.getTranslation(reference + "none"));
            } else {
                description.add(this.user.getTranslation(reference + "title"));

                this.challenge.getIgnoreRewardMetaData().stream().sorted(Comparator.comparing(Material::name))
                        .forEach(itemStack -> description.add(this.user.getTranslationOrNothing(reference + "list",
                                "[item]", Utils.prettifyObject(itemStack, this.user))));
            }

            icon = new ItemStack(Material.GREEN_SHULKER_BOX);

            clickHandler = (panel, user, clickType, slot) -> {
                if (this.challenge.getRewardItems().isEmpty() && this.challenge.getRepeatItemReward().isEmpty()) {
                    // Do nothing if no requirements are set.
                    return true;
                }

                // Allow choosing only from inventory items.
                Set<Material> collection = Arrays.stream(Material.values()).collect(Collectors.toSet());
                this.challenge.getRewardItems().stream().map(ItemStack::getType).forEach(collection::remove);
                this.challenge.getRepeatItemReward().stream().map(ItemStack::getType).forEach(collection::remove);
                collection.addAll(this.challenge.getIgnoreRewardMetaData());

                if (Material.values().length == collection.size()) {
                    // If there are no items anymore, then do not allow opening gui.
                    return true;
                }

                MultiBlockSelector.open(this.user, MultiBlockSelector.Mode.ANY, collection, (status, materials) -> {
                    if (status) {
                        materials.addAll(this.challenge.getIgnoreRewardMetaData());
                        this.challenge.setIgnoreRewardMetaData(new HashSet<>(materials));
                    }

                    this.build();
                });
                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-add"));
        }
        case REMOVE_IGNORED_META -> {
            icon = new ItemStack(Material.RED_SHULKER_BOX);

            clickHandler = (panel, user, clickType, slot) -> {
                if (this.challenge.getIgnoreRewardMetaData().isEmpty()) {
                    // Do nothing if no requirements are set.
                    return true;
                }

                // Allow choosing only from inventory items.
                Set<Material> collection = Arrays.stream(Material.values()).collect(Collectors.toSet());
                collection.removeAll(this.challenge.getIgnoreRewardMetaData());

                MultiBlockSelector.open(this.user, MultiBlockSelector.Mode.ANY, collection, (status, materials) -> {
                    if (status) {
                        this.challenge.getIgnoreRewardMetaData().removeAll(materials);
                    }

                    this.build();
                });
                return true;
            };
            glow = false;

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-remove"));
        }
        default -> {
            icon = new ItemStack(Material.PAPER);
            clickHandler = null;
            glow = false;
        }
        }

        return new PanelItemBuilder().icon(icon).name(name).description(description).glow(glow)
                .clickHandler(clickHandler).build();
    }

    // ---------------------------------------------------------------------
    // Section: Classes
    // ---------------------------------------------------------------------

    /**
     * This class allows changing icon for Generator Tier
     */
    private class IconChanger implements PanelListener {
        /**
         * Process inventory click. If generator icon is selected and user clicks on
         * item in his inventory, then change icon to the item from inventory.
         *
         * @param user  the user
         * @param event the event
         */
        @Override
        public void onInventoryClick(User user, InventoryClickEvent event) {
            // Handle icon changing
            if (EditChallengePanel.this.selectedButton != null && event.getCurrentItem() != null
                    && !event.getCurrentItem().getType().equals(Material.AIR) && event.getRawSlot() > 44) {
                // set material and amount only. Other data should be removed.

                if (EditChallengePanel.this.selectedButton == Button.ICON) {
                    EditChallengePanel.this.challenge.setIcon(event.getCurrentItem().clone());
                    // Deselect icon
                    EditChallengePanel.this.selectedButton = null;
                    // Rebuild icon
                    EditChallengePanel.this.build();
                }
            }
        }

        /**
         * On inventory close.
         *
         * @param event the event
         */
        @Override
        public void onInventoryClose(InventoryCloseEvent event) {
            // Do nothing
        }

        /**
         * Setup current listener.
         */
        @Override
        public void setup() {
            // Do nothing
        }
    }

    // ---------------------------------------------------------------------
    // Section: Enums
    // ---------------------------------------------------------------------

    /**
     * Represents different types of menus
     */
    private enum MenuType {
        PROPERTIES, REQUIREMENTS, REWARDS
    }

    /**
     * Represents different buttons that could be in menus.
     */
    private enum Button {
        NAME, DEPLOYED, ICON, DESCRIPTION, ORDER, ENVIRONMENT, REMOVE_ON_COMPLETE,
    }

    /**
     * Represents different rewards buttons that are used in menus.
     */
    private enum RewardButton {
        REWARD_TEXT, REWARD_ITEMS, REWARD_EXPERIENCE, REWARD_MONEY, REWARD_COMMANDS,

        REPEATABLE, REPEAT_COUNT, COOL_DOWN,

        REPEAT_REWARD_TEXT, REPEAT_REWARD_ITEMS, REPEAT_REWARD_EXPERIENCE, REPEAT_REWARD_MONEY, REPEAT_REWARD_COMMANDS,

        ADD_IGNORED_META, REMOVE_IGNORED_META,
    }

    /**
     * Represents different requirement buttons that are used in menus.
     */
    private enum RequirementButton {
        REQUIRED_ENTITIES, REMOVE_ENTITIES, REQUIRED_BLOCKS, REMOVE_BLOCKS, SEARCH_RADIUS, REQUIRED_PERMISSIONS,
        REQUIRED_ITEMS, REMOVE_ITEMS, ADD_IGNORED_META, REMOVE_IGNORED_META, REQUIRED_EXPERIENCE, REMOVE_EXPERIENCE,
        REQUIRED_LEVEL, REQUIRED_MONEY, REMOVE_MONEY, STATISTIC, STATISTIC_BLOCKS, STATISTIC_ITEMS, STATISTIC_ENTITIES,
        STATISTIC_AMOUNT, REMOVE_STATISTIC, REQUIRED_MATERIALTAGS, REQUIRED_ENTITYTAGS,
    }

    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * Variable holds challenge thats needs editing.
     */
    private final Challenge challenge;

    private Button selectedButton;

    /**
     * Variable holds current active menu.
     */
    private MenuType currentMenuType;

}
