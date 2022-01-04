package world.bentobox.challenges.panel.admin;


import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Material;
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
import world.bentobox.challenges.managers.ChallengesManager;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.panel.CommonPagedPanel;
import world.bentobox.challenges.panel.CommonPanel;
import world.bentobox.challenges.panel.ConversationUtils;
import world.bentobox.challenges.panel.util.ItemSelector;
import world.bentobox.challenges.panel.util.ChallengeSelector;
import world.bentobox.challenges.panel.util.MultiBlockSelector;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;


/**
 * This class contains all necessary elements to create Levels Edit GUI.
 */
public class EditLevelPanel extends CommonPagedPanel<Challenge>
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
     * @param challengeLevel ChallengeLevel that must be edited.
     */
    private EditLevelPanel(ChallengesAddon addon,
        User user,
        World world,
        String topLabel,
        String permissionPrefix,
        ChallengeLevel challengeLevel)
    {
        super(addon, user, world, topLabel, permissionPrefix);
        this.challengeLevel = challengeLevel;
        this.currentMenuType = MenuType.PROPERTIES;
    }


    /**
     * @param challengeLevel ChallengeLevel that must be edited.
     */
    private EditLevelPanel(CommonPanel parentGUI, ChallengeLevel challengeLevel)
    {
        super(parentGUI);
        this.challengeLevel = challengeLevel;
        this.currentMenuType = MenuType.PROPERTIES;
    }


    /**
     * Open the Challenges Level Edit GUI.
     *
     * @param addon the addon
     * @param world the world
     * @param user the user
     * @param topLabel the top label
     * @param permissionPrefix the permission prefix
     * @param level - level that needs editing
     */
    public static void open(ChallengesAddon addon,
        User user,
        World world,
        String topLabel,
        String permissionPrefix,
        ChallengeLevel level)
    {
        new EditLevelPanel(addon, user, world, topLabel, permissionPrefix, level).build();
    }


    /**
     * Open the Challenges Level Edit GUI.
     *
     * @param panel - Parent Panel
     * @param level - level that needs editing
     */
    public static void open(CommonPanel panel,  ChallengeLevel level)
    {
        new EditLevelPanel(panel, level).build();
    }


    // ---------------------------------------------------------------------
    // Section: Methods
    // ---------------------------------------------------------------------


    /**
     * This method is called when filter value is updated.
     */
    @Override
    protected void updateFilters()
    {
        // Do nothing here.
    }


    /**
     * This method builds all necessary elements in GUI panel.
     */
    @Override
    protected void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
            this.user.getTranslation(Constants.TITLE + "edit-level",
                "[level]", this.challengeLevel.getFriendlyName()));

        PanelUtils.fillBorder(panelBuilder);

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
        panelBuilder.listener(new IconChanger());

        panelBuilder.item(10, this.createButton(Button.NAME));

        panelBuilder.item(19, this.createButton(Button.ICON));
        panelBuilder.item(28, this.createButton(Button.LOCKED_ICON));
        panelBuilder.item(22, this.createButton(Button.DESCRIPTION));
        panelBuilder.item(25, this.createButton(Button.ORDER));

        panelBuilder.item(31, this.createButton(Button.WAIVER_AMOUNT));
    }


    /**
     * This class populate LevelsEditGUI with level rewards.
     * @param panelBuilder PanelBuilder where icons must be added.
     */
    private void buildRewardsPanel(PanelBuilder panelBuilder)
    {
        panelBuilder.item(12, this.createButton(Button.REWARD_TEXT));
        panelBuilder.item(21, this.createButton(Button.REWARD_COMMANDS));

        panelBuilder.item(13, this.createButton(Button.REWARD_ITEMS));
        panelBuilder.item(22, this.createButton(Button.REWARD_EXPERIENCE));
        panelBuilder.item(31, this.createButton(Button.REWARD_MONEY));

        if (!this.challengeLevel.getRewardItems().isEmpty())
        {
            panelBuilder.item(33, this.createButton(Button.ADD_IGNORED_META));
        }

        if (!this.challengeLevel.getIgnoreRewardMetaData().isEmpty())
        {
            panelBuilder.item(34, this.createButton(Button.REMOVE_IGNORED_META));
        }
    }


    /**
     * This class populate LevelsEditGUI with level challenges.
     * @param panelBuilder PanelBuilder where icons must be added.
     */
    private void buildChallengesPanel(PanelBuilder panelBuilder)
    {
        List<Challenge> challengeList = this.addon.getChallengesManager().
            getLevelChallenges(this.challengeLevel).stream().
            filter(challenge -> this.searchString.isBlank() ||
                challenge.getFriendlyName().toLowerCase().contains(this.searchString.toLowerCase()) ||
                challenge.getUniqueId().toLowerCase().contains(this.searchString.toLowerCase()) ||
                challenge.getChallengeType().name().toLowerCase().contains(this.searchString)).
            collect(Collectors.toList());

        this.populateElements(panelBuilder, challengeList);

        panelBuilder.item(39, this.createButton(Button.ADD_CHALLENGES));
        panelBuilder.item(41, this.createButton(Button.REMOVE_CHALLENGES));
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
        final String reference = Constants.BUTTON + menuType.name().toLowerCase() + ".";

        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(reference + "description"));
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-select"));

        ItemStack icon;
        boolean glow;
        PanelItem.ClickHandler clickHandler;

        switch (menuType)
        {
            case PROPERTIES -> {
                icon = new ItemStack(Material.CRAFTING_TABLE);
                clickHandler = (panel, user, clickType, slot) -> {
                    this.currentMenuType = MenuType.PROPERTIES;
                    this.build();

                    return true;
                };
                glow = this.currentMenuType.equals(MenuType.PROPERTIES);
            }
            case CHALLENGES -> {
                icon = new ItemStack(Material.RAIL);
                clickHandler = (panel, user, clickType, slot) -> {
                    this.currentMenuType = MenuType.CHALLENGES;
                    this.build();

                    return true;
                };
                glow = this.currentMenuType.equals(MenuType.CHALLENGES);
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

        return new PanelItemBuilder().
            icon(icon).
            name(name).
            description(description).
            glow(glow).
            clickHandler(clickHandler).
            build();
    }


    /**
     * This method creates given challenge icon. On click it should open Edit Challenge GUI.
     * @param challenge Challenge which icon must be created.
     * @return PanelItem that represents given challenge.
     */
    @Override
    protected PanelItem createElementButton(Challenge challenge)
    {
        return new PanelItemBuilder().
            name(Util.translateColorCodes(challenge.getFriendlyName())).
            description(this.generateChallengeDescription(challenge, null)).
            description("").
            description(this.user.getTranslation(Constants.TIPS + "click-to-edit")).
            icon(challenge.getIcon()).
            clickHandler((panel, user, clickType, slot) -> {
                // Open challenges edit screen.
                EditChallengePanel.open(this, challenge);
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
        final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";

        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(reference + "description"));

        ItemStack icon;
        boolean glow;
        PanelItem.ClickHandler clickHandler;

        switch (button)
        {
            case REWARD_TEXT -> {
                icon = new ItemStack(Material.WRITTEN_BOOK);

                description.add(this.user.getTranslation(reference + "value"));
                description.add(Util.translateColorCodes(this.challengeLevel.getRewardText()));

                clickHandler = (panel, user, clickType, i) ->
                {
                    // Create consumer that process description change
                    Consumer<List<String>> consumer = value ->
                    {
                        if (value != null)
                        {
                            this.challengeLevel.setRewardText(String.join("\n", value));
                        }

                        this.build();
                    };

                    if (!this.challengeLevel.getRewardText().isEmpty() && clickType.isShiftClick())
                    {
                        // Reset to the empty value
                        consumer.accept(Collections.emptyList());
                    }
                    else
                    {
                        // start conversation
                        ConversationUtils.createStringListInput(consumer,
                            user,
                            user.getTranslation(Constants.CONVERSATIONS + "write-reward-text"),
                            user.getTranslation(Constants.CONVERSATIONS + "reward-text-changed"));
                    }


                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                if (!this.challengeLevel.getRewardText().isEmpty())
                {
                    description.add(this.user.getTranslation(Constants.TIPS + "shift-click-to-reset"));
                }
            }
            case REWARD_ITEMS -> {

                if (this.challengeLevel.getRewardItems().isEmpty())
                {
                    description.add(this.user.getTranslation(reference + "none"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + "title"));

                    Utils.groupEqualItems(this.challengeLevel.getRewardItems(), this.challengeLevel.getIgnoreRewardMetaData()).
                        stream().
                        sorted(Comparator.comparing(ItemStack::getType)).
                        forEach(itemStack ->
                            description.add(this.user.getTranslationOrNothing(reference + "list",
                                "[item]", Utils.prettifyObject(itemStack, this.user))));
                }

                icon = new ItemStack(Material.CHEST);
                clickHandler = (panel, user, clickType, slot) -> {
                    ItemSelector.open(this.user,
                        this.challengeLevel.getRewardItems(),
                        (status, value) -> {
                            if (status)
                            {
                                this.challengeLevel.setRewardItems(value);
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
                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.challengeLevel.getRewardExperience())));
                icon = new ItemStack(Material.EXPERIENCE_BOTTLE);
                clickHandler = (panel, user, clickType, i) -> {
                    Consumer<Number> numberConsumer = number -> {
                        if (number != null)
                        {
                            this.challengeLevel.setRewardExperience(number.intValue());
                        }

                        // reopen panel
                        this.build();
                    };
                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        Integer.MAX_VALUE);

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case REWARD_MONEY -> {
                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.challengeLevel.getRewardMoney())));
                icon = new ItemStack(this.addon.isEconomyProvided() ? Material.GOLD_INGOT : Material.BARRIER);
                clickHandler = (panel, user, clickType, i) -> {
                    Consumer<Number> numberConsumer = number -> {
                        if (number != null)
                        {
                            this.challengeLevel.setRewardMoney(number.doubleValue());
                        }

                        // reopen panel
                        this.build();
                    };
                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        Double.MAX_VALUE);

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case REWARD_COMMANDS -> {
                icon = new ItemStack(Material.COMMAND_BLOCK);

                description.add(this.user.getTranslation(reference + "value"));
                description.addAll(this.challengeLevel.getRewardCommands());

                clickHandler = (panel, user, clickType, i) ->
                {
                    // Create consumer that process description change
                    Consumer<List<String>> consumer = value ->
                    {
                        if (value != null)
                        {
                            this.challengeLevel.setRewardCommands(value);
                        }

                        this.build();
                    };

                    if (!this.challengeLevel.getRewardCommands().isEmpty() && clickType.isShiftClick())
                    {
                        // Reset to the empty value
                        consumer.accept(Collections.emptyList());
                    }
                    else
                    {
                        // start conversation
                        ConversationUtils.createStringListInput(consumer,
                            user,
                            user.getTranslation(Constants.CONVERSATIONS + "write-reward-commands"),
                            user.getTranslation(Constants.CONVERSATIONS + "reward-commands-changed"));
                    }

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                if (!this.challengeLevel.getRewardCommands().isEmpty())
                {
                    description.add(this.user.getTranslation(Constants.TIPS + "shift-click-to-reset"));
                }
            }
            case ADD_IGNORED_META -> {
                if (this.challengeLevel.getIgnoreRewardMetaData().isEmpty())
                {
                    description.add(this.user.getTranslation(reference + "none"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + "title"));

                    this.challengeLevel.getIgnoreRewardMetaData().stream().
                        sorted(Comparator.comparing(Material::name)).
                        forEach(itemStack ->
                            description.add(this.user.getTranslationOrNothing(reference + "list",
                                "[item]", Utils.prettifyObject(itemStack, this.user))));
                }

                icon = new ItemStack(Material.GREEN_SHULKER_BOX);

                clickHandler = (panel, user, clickType, slot) -> {
                    if (this.challengeLevel.getRewardItems().isEmpty())
                    {
                        // Do nothing if no requirements are set.
                        return true;
                    }

                    // Allow choosing only from inventory items.
                    Set<Material> collection = Arrays.stream(Material.values()).collect(Collectors.toSet());
                    this.challengeLevel.getRewardItems().stream().
                        map(ItemStack::getType).
                        forEach(collection::remove);
                    collection.addAll(this.challengeLevel.getIgnoreRewardMetaData());

                    if (Material.values().length == collection.size())
                    {
                        // If all materials are blocked, then do not allow to open gui.
                        return true;
                    }

                    MultiBlockSelector.open(this.user,
                        MultiBlockSelector.Mode.ANY,
                        collection,
                        (status, materials) ->
                        {
                            if (status)
                            {
                                materials.addAll(this.challengeLevel.getIgnoreRewardMetaData());
                                this.challengeLevel.setIgnoreRewardMetaData(new HashSet<>(materials));
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
                    if (this.challengeLevel.getIgnoreRewardMetaData().isEmpty())
                    {
                        // Do nothing if no requirements are set.
                        return true;
                    }

                    // Allow choosing only from inventory items.
                    Set<Material> collection = Arrays.stream(Material.values()).collect(Collectors.toSet());
                    collection.removeAll(this.challengeLevel.getIgnoreRewardMetaData());

                    MultiBlockSelector.open(this.user,
                        MultiBlockSelector.Mode.ANY,
                        collection,
                        (status, materials) ->
                        {
                            if (status)
                            {
                                this.challengeLevel.getIgnoreRewardMetaData().removeAll(materials);
                            }

                            this.build();
                        });
                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-remove"));
            }
            case NAME -> {
                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_NAME, this.challengeLevel.getFriendlyName()));

                icon = new ItemStack(Material.NAME_TAG);

                clickHandler = (panel, user, clickType, i) ->
                {
                    // Create consumer that process description change
                    Consumer<String> consumer = value ->
                    {
                        if (value != null)
                        {
                            this.challengeLevel.setFriendlyName(value);
                        }

                        this.build();
                    };

                    // start conversation
                    ConversationUtils.createStringInput(consumer,
                        user,
                        user.getTranslation(Constants.CONVERSATIONS + "write-name"),
                        user.getTranslation(Constants.CONVERSATIONS + "name-changed"));

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case DESCRIPTION -> {
                icon = new ItemStack(Material.WRITTEN_BOOK);

                description.add(this.user.getTranslation(reference + "value"));
                description.add(Util.translateColorCodes(this.challengeLevel.getUnlockMessage()));

                clickHandler = (panel, user, clickType, i) ->
                {
                    // Create consumer that process description change
                    Consumer<List<String>> consumer = value ->
                    {
                        if (value != null)
                        {
                            this.challengeLevel.setUnlockMessage(String.join("\n", value));
                        }

                        this.build();
                    };

                    if (!this.challengeLevel.getUnlockMessage().isEmpty() && clickType.isShiftClick())
                    {
                        // Reset to the empty value
                        consumer.accept(Collections.emptyList());
                    }
                    else
                    {
                        // start conversation
                        ConversationUtils.createStringListInput(consumer,
                            user,
                            user.getTranslation(Constants.CONVERSATIONS + "write-description"),
                            user.getTranslation(Constants.CONVERSATIONS + "description-changed"));
                    }

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                if (!this.challengeLevel.getUnlockMessage().isEmpty())
                {
                    description.add(this.user.getTranslation(Constants.TIPS + "shift-click-to-reset"));
                }
            }
            case ICON, LOCKED_ICON -> {
                icon = button == Button.LOCKED_ICON ?
                    this.challengeLevel.getLockedIcon() :
                    this.challengeLevel.getIcon();

                clickHandler = (panel, user, clickType, i) ->
                {
                    this.selectedButton = button;
                    this.build();
                    return true;
                };

                if (this.selectedButton != button)
                {
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
                }
                else
                {
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-on-item"));
                }

                glow = this.selectedButton == button;
            }
            case ORDER -> {
                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.challengeLevel.getOrder())));

                icon = new ItemStack(Material.HOPPER, Math.max(1, this.challengeLevel.getOrder()));
                clickHandler = (panel, user, clickType, i) -> {
                    Consumer<Number> numberConsumer = number -> {
                        if (number != null)
                        {
                            this.challengeLevel.setOrder(number.intValue());
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        2000);

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case WAIVER_AMOUNT -> {
                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.challengeLevel.getWaiverAmount())));

                icon = new ItemStack(Material.HOPPER, Math.max(1, this.challengeLevel.getWaiverAmount()));
                clickHandler = (panel, user, clickType, i) -> {
                    Consumer<Number> numberConsumer = number -> {
                        if (number != null)
                        {
                            this.challengeLevel.setWaiverAmount(number.intValue());
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        2000);

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case ADD_CHALLENGES -> {
                icon = new ItemStack(Material.WATER_BUCKET);
                clickHandler = (panel, user, clickType, slot) -> {
                    ChallengesManager manager = this.addon.getChallengesManager();

                    // Get all challenge that is not in current level.
                    List<Challenge> challengeList = manager.getAllChallenges(this.world);
                    challengeList.removeAll(manager.getLevelChallenges(this.challengeLevel));

                    // Generate descriptions for these challenges
                    Map<Challenge, List<String>> challengeDescriptionMap = challengeList.stream().
                        collect(Collectors.toMap(challenge -> challenge,
                            challenge -> this.generateChallengeDescription(challenge, null),
                            (a, b) -> b,
                            () -> new LinkedHashMap<>(challengeList.size())));

                    // Open select gui
                    ChallengeSelector.open(this.user,
                        Material.BLUE_STAINED_GLASS_PANE,
                        challengeDescriptionMap,
                        (status, valueSet) -> {
                            if (status)
                            {
                                valueSet.forEach(challenge ->
                                    manager.addChallengeToLevel(challenge, this.challengeLevel));
                            }

                        this.build();
                    });

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-add"));
            }
            case REMOVE_CHALLENGES -> {
                icon = new ItemStack(Material.LAVA_BUCKET);
                clickHandler = (panel, user, clickType, slot) -> {
                    ChallengesManager manager = this.addon.getChallengesManager();

                    // Get all challenge that is in current level.
                    List<Challenge> challengeList = manager.getLevelChallenges(this.challengeLevel);

                    // Generate descriptions for these challenges
                    Map<Challenge, List<String>> challengeDescriptionMap = challengeList.stream().
                        collect(Collectors.toMap(challenge -> challenge,
                            challenge -> this.generateChallengeDescription(challenge, null),
                            (a, b) -> b,
                            () -> new LinkedHashMap<>(challengeList.size())));

                    // Open select gui
                    ChallengeSelector.open(this.user,
                        Material.RED_STAINED_GLASS_PANE,
                        challengeDescriptionMap,
                        (status, valueSet) -> {
                            if (status)
                            {
                                valueSet.forEach(challenge ->
                                    manager.removeChallengeFromLevel(challenge, this.challengeLevel));
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

        return new PanelItemBuilder().
            icon(icon).
            name(name).
            description(description).
            glow(glow).
            clickHandler(clickHandler).
            build();
    }


    // ---------------------------------------------------------------------
    // Section: Classes
    // ---------------------------------------------------------------------


    /**
     * This class allows changing icon for Generator Tier
     */
    private class IconChanger implements PanelListener
    {
        /**
         * Process inventory click. If generator icon is selected and user clicks on item in his inventory, then change
         * icon to the item from inventory.
         *
         * @param user the user
         * @param event the event
         */
        @Override
        public void onInventoryClick(User user, InventoryClickEvent event)
        {
            // Handle icon changing
            if (EditLevelPanel.this.selectedButton != null &&
                event.getCurrentItem() != null &&
                !event.getCurrentItem().getType().equals(Material.AIR) &&
                event.getRawSlot() > 44)
            {
                // set material and amount only. Other data should be removed.

                if (EditLevelPanel.this.selectedButton == Button.ICON)
                {
                    EditLevelPanel.this.challengeLevel.setIcon(event.getCurrentItem().clone());
                    // Deselect icon
                    EditLevelPanel.this.selectedButton = null;
                    // Rebuild icon
                    EditLevelPanel.this.build();
                }
                else if (EditLevelPanel.this.selectedButton == Button.LOCKED_ICON)
                {
                    EditLevelPanel.this.challengeLevel.setLockedIcon(event.getCurrentItem().clone());
                    // Deselect icon
                    EditLevelPanel.this.selectedButton = null;
                    // Rebuild icon
                    EditLevelPanel.this.build();
                }
            }
        }


        /**
         * On inventory close.
         *
         * @param event the event
         */
        @Override
        public void onInventoryClose(InventoryCloseEvent event)
        {
            // Do nothing
        }


        /**
         * Setup current listener.
         */
        @Override
        public void setup()
        {
            // Do nothing
        }
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
        LOCKED_ICON,
        DESCRIPTION,
        ORDER,
        WAIVER_AMOUNT,

        REWARD_TEXT,
        REWARD_ITEMS,
        REWARD_EXPERIENCE,
        REWARD_MONEY,
        REWARD_COMMANDS,

        ADD_IGNORED_META,
        REMOVE_IGNORED_META,

        ADD_CHALLENGES,
        REMOVE_CHALLENGES
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
    private final ChallengeLevel challengeLevel;

    /**
     * Variable holds current active menu.
     */
    private MenuType currentMenuType;

    private Button selectedButton;
}