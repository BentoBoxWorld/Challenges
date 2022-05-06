package world.bentobox.challenges.panel.admin;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.config.Settings;
import world.bentobox.challenges.config.SettingsUtils.GuiMode;
import world.bentobox.challenges.config.SettingsUtils.VisibilityMode;
import world.bentobox.challenges.panel.CommonPanel;
import world.bentobox.challenges.panel.ConversationUtils;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;


/**
 * This Class creates GUI that allows to change Challenges Addon Settings via in-game
 * menu.
 */
public class EditSettingsPanel extends CommonPanel
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
     */
    private EditSettingsPanel(ChallengesAddon addon,
        User user,
        World world,
        String topLabel,
        String permissionPrefix)
    {
        super(addon, user, world, topLabel, permissionPrefix);
        this.settings = this.addon.getChallengesSettings();
    }


    /**
     * @param parentGUI Parent GUI.
     */
    private EditSettingsPanel(CommonPanel parentGUI)
    {
        super(parentGUI);
        this.settings = this.addon.getChallengesSettings();
    }


    /**
     * Open the Challenges Admin GUI.
     *
     * @param addon the addon
     * @param world the world
     * @param user the user
     * @param topLabel the top label
     * @param permissionPrefix the permission prefix
     */
    public static void open(ChallengesAddon addon,
        World world,
        User user,
        String topLabel,
        String permissionPrefix)
    {
        new EditSettingsPanel(addon, user, world, topLabel, permissionPrefix).build();
    }


    /**
     * Open the Challenges Admin GUI.
     */
    public static void open(CommonPanel parentGUI)
    {
        new EditSettingsPanel(parentGUI).build();
    }


    // ---------------------------------------------------------------------
    // Section: Methods
    // ---------------------------------------------------------------------


    @Override
    protected void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
            this.user.getTranslation(Constants.TITLE + "settings"));

        PanelUtils.fillBorder(panelBuilder);

        panelBuilder.item(10, this.getSettingsButton(Button.SHOW_TITLE));

        if (this.settings.isShowCompletionTitle())
        {
            panelBuilder.item(19, this.getSettingsButton(Button.TITLE_SHOWTIME));
        }

        panelBuilder.item(28, this.getSettingsButton(Button.BROADCAST));

        panelBuilder.item(11, this.getSettingsButton(Button.GLOW_COMPLETED));
        panelBuilder.item(20, this.getSettingsButton(Button.REMOVE_COMPLETED));
        panelBuilder.item(29, this.getSettingsButton(Button.VISIBILITY_MODE));

        panelBuilder.item(21, this.getSettingsButton(Button.LOCKED_LEVEL_ICON));

        panelBuilder.item(22, this.getSettingsButton(Button.GAMEMODE_GUI));

        if (this.settings.isUseCommonGUI())
        {
            // This should be active only when single gui is enabled.
            panelBuilder.item(31, this.getSettingsButton(Button.ACTIVE_WORLD_LIST));
        }

        panelBuilder.item(24, this.getSettingsButton(Button.STORE_HISTORY));

        if (this.settings.isStoreHistory())
        {
            panelBuilder.item(33, this.getSettingsButton(Button.PURGE_HISTORY));
        }

        panelBuilder.item(25, this.getSettingsButton(Button.RESET_ON_NEW));
        panelBuilder.item(34, this.getSettingsButton(Button.DATA_PER_ISLAND));

        // Return Button
        panelBuilder.item(44, this.returnButton);
        panelBuilder.listener(new IconChanger());
        panelBuilder.build();
    }


    private PanelItem getSettingsButton(Button button)
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
            case RESET_ON_NEW -> {
                description.add(this.user.getTranslation(reference +
                    (this.settings.isResetChallenges() ? "enabled" : "disabled")));

                icon = new ItemStack(Material.LAVA_BUCKET);
                clickHandler = (panel, user1, clickType, i) -> {
                    this.settings.setResetChallenges(!this.settings.isResetChallenges());
                    panel.getInventory().setItem(i, this.getSettingsButton(button).getItem());
                    this.addon.saveSettings();
                    return true;
                };
                glow = this.settings.isResetChallenges();

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
            }
            case BROADCAST -> {
                description.add(this.user.getTranslation(reference +
                    (this.settings.isBroadcastMessages() ? "enabled" : "disabled")));

                icon = new ItemStack(Material.JUKEBOX);
                clickHandler = (panel, user1, clickType, i) -> {
                    this.settings.setBroadcastMessages(!this.settings.isBroadcastMessages());
                    panel.getInventory().setItem(i, this.getSettingsButton(button).getItem());
                    this.addon.saveSettings();
                    return true;
                };
                glow = this.settings.isBroadcastMessages();

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
            }
            case REMOVE_COMPLETED -> {
                description.add(this.user.getTranslation(reference +
                    (this.settings.isRemoveCompleteOneTimeChallenges() ? "enabled" : "disabled")));

                icon = new ItemStack(Material.MAGMA_BLOCK);
                clickHandler = (panel, user1, clickType, i) -> {
                    this.settings.setRemoveCompleteOneTimeChallenges(!this.settings.isRemoveCompleteOneTimeChallenges());
                    panel.getInventory().setItem(i, this.getSettingsButton(button).getItem());
                    this.addon.saveSettings();
                    return true;
                };
                glow = this.settings.isRemoveCompleteOneTimeChallenges();

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
            }
            case ACTIVE_WORLD_LIST -> {
                description.add(this.user.getTranslation(reference +
                    (this.settings.getUserGuiMode().equals(GuiMode.GAMEMODE_LIST) ?
                        "disabled" : "enabled")));

                icon = new ItemStack(Material.STONE_BUTTON);
                clickHandler = (panel, user1, clickType, i) -> {
                    if (this.settings.getUserGuiMode().equals(GuiMode.GAMEMODE_LIST))
                    {
                        this.settings.setUserGuiMode(GuiMode.CURRENT_WORLD);
                    }
                    else
                    {
                        this.settings.setUserGuiMode(GuiMode.GAMEMODE_LIST);
                    }

                    this.addon.saveSettings();
                    panel.getInventory().setItem(i, this.getSettingsButton(button).getItem());
                    return true;
                };
                glow = this.settings.getUserGuiMode().equals(GuiMode.GAMEMODE_LIST);

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case GAMEMODE_GUI -> {
                description.add(this.user.getTranslation(reference +
                    (this.settings.isUseCommonGUI() ? "enabled" : "disabled")));

                icon = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                clickHandler = (panel, user1, clickType, i) -> {
                    this.settings.setUseCommonGUI(!this.settings.isUseCommonGUI());
                    // Need to rebuild more icons
                    this.build();
                    this.addon.saveSettings();
                    return true;
                };
                glow = this.settings.isUseCommonGUI();

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
            }
            case STORE_HISTORY -> {
                description.add(this.user.getTranslation(reference +
                    (this.settings.isStoreHistory() ? "enabled" : "disabled")));

                icon = new ItemStack(Material.WRITTEN_BOOK);
                clickHandler = (panel, user1, clickType, i) -> {
                    this.settings.setStoreHistory(!this.settings.isStoreHistory());
                    panel.getInventory().setItem(i, this.getSettingsButton(button).getItem());
                    this.addon.saveSettings();
                    return true;
                };
                glow = this.settings.isStoreHistory();

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
            }
            case PURGE_HISTORY -> {
                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getLifeSpan())));

                icon = new ItemStack(Material.FLINT_AND_STEEL, Math.max(1, this.settings.getLifeSpan()));
                clickHandler = (panel, user, clickType, i) -> {
                    Consumer<Number> numberConsumer = number -> {
                        if (number != null)
                        {
                            this.settings.setLifeSpan(number.intValue());
                            this.addon.saveSettings();
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
            case DATA_PER_ISLAND -> {
                description.add(this.user.getTranslation(reference +
                    (this.settings.isStoreAsIslandData() ? "enabled" : "disabled")));

                icon = new ItemStack(Material.GRASS_BLOCK);
                clickHandler = (panel, user1, clickType, i) -> {
                    this.settings.setStoreAsIslandData(!this.settings.isStoreAsIslandData());
                    // TODO: Migration
                    panel.getInventory().setItem(i, this.getSettingsButton(button).getItem());
                    this.addon.saveSettings();
                    return true;
                };
                glow = this.settings.isStoreAsIslandData();

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
            }
            case GLOW_COMPLETED -> {
                description.add(this.user.getTranslation(reference +
                    (this.settings.isAddCompletedGlow() ? "enabled" : "disabled")));

                icon = new ItemStack(Material.GLOWSTONE);
                clickHandler = (panel, user1, clickType, i) -> {
                    this.settings.setAddCompletedGlow(!this.settings.isAddCompletedGlow());
                    panel.getInventory().setItem(i, this.getSettingsButton(button).getItem());
                    this.addon.saveSettings();
                    return true;
                };
                glow = this.settings.isAddCompletedGlow();

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
            }
            case LOCKED_LEVEL_ICON -> {
                icon = this.settings.getLockedLevelIcon();

                clickHandler = (panel, user, clickType, i) ->
                {
                    if (this.selectedButton != null)
                    {
                        this.selectedButton = null;
                    }
                    else
                    {
                        this.selectedButton = button;
                    }

                    panel.getInventory().setItem(i, this.getSettingsButton(button).getItem());
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
            case SHOW_TITLE -> {
                description.add(this.user.getTranslation(reference +
                    (this.settings.isShowCompletionTitle() ? "enabled" : "disabled")));

                icon = new ItemStack(Material.OAK_SIGN);
                clickHandler = (panel, user1, clickType, i) -> {
                    this.settings.setShowCompletionTitle(!this.settings.isShowCompletionTitle());
                    panel.getInventory().setItem(i, this.getSettingsButton(button).getItem());
                    this.addon.saveSettings();
                    return true;
                };
                glow = this.settings.isShowCompletionTitle();

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
            }
            case TITLE_SHOWTIME -> {
                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getTitleShowtime())));

                icon = new ItemStack(Material.CLOCK, Math.max(1, this.settings.getTitleShowtime()));
                clickHandler = (panel, user, clickType, i) -> {
                    Consumer<Number> numberConsumer = number -> {
                        if (number != null)
                        {
                            this.settings.setTitleShowtime(number.intValue());
                            this.addon.saveSettings();
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
            case VISIBILITY_MODE -> {
                description.add(this.user.getTranslation(reference +
                    (this.settings.getVisibilityMode().equals(VisibilityMode.VISIBLE) ? "enabled" : "disabled")) +
                    this.user.getTranslation(reference + "visible"));
                description.add(this.user.getTranslation(reference +
                    (this.settings.getVisibilityMode().equals(VisibilityMode.HIDDEN) ? "enabled" : "disabled")) +
                    this.user.getTranslation(reference + "hidden"));
                description.add(this.user.getTranslation(reference +
                    (this.settings.getVisibilityMode().equals(VisibilityMode.TOGGLEABLE) ? "enabled" : "disabled")) +
                    this.user.getTranslation(reference + "toggleable"));

                if (this.settings.getVisibilityMode().equals(VisibilityMode.VISIBLE))
                {
                    icon = new ItemStack(Material.OAK_PLANKS);
                }
                else if (this.settings.getVisibilityMode().equals(VisibilityMode.HIDDEN))
                {
                    icon = new ItemStack(Material.OAK_SLAB);
                }
                else
                {
                    icon = new ItemStack(Material.OAK_BUTTON);
                }

                clickHandler = (panel, user, clickType, slot) -> {
                    if (clickType.isRightClick())
                    {
                        this.settings.setVisibilityMode(Utils.getPreviousValue(VisibilityMode.values(),
                            this.settings.getVisibilityMode()));
                    }
                    else
                    {
                        this.settings.setVisibilityMode(Utils.getNextValue(VisibilityMode.values(),
                            this.settings.getVisibilityMode()));
                    }

                    // Rebuild just this icon
                    panel.getInventory().setItem(slot, this.getSettingsButton(button).getItem());
                    this.addon.saveSettings();
                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-cycle"));
                description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-cycle"));
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
            if (EditSettingsPanel.this.selectedButton != null &&
                event.getCurrentItem() != null &&
                !event.getCurrentItem().getType().equals(Material.AIR) &&
                event.getRawSlot() > 44)
            {
                // set material and amount only. Other data should be removed.

                if (EditSettingsPanel.this.selectedButton == Button.LOCKED_LEVEL_ICON)
                {
                    EditSettingsPanel.this.settings.setLockedLevelIcon(event.getCurrentItem().clone());
                    EditSettingsPanel.this.addon.saveSettings();

                    // Deselect icon
                    EditSettingsPanel.this.selectedButton = null;
                    EditSettingsPanel.this.build();
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
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * This enum holds all settings buttons that must have been displayed in this panel.
     */
    private enum Button
    {
        RESET_ON_NEW,
        BROADCAST,
        REMOVE_COMPLETED,
        ACTIVE_WORLD_LIST,
        GAMEMODE_GUI,
        STORE_HISTORY,
        PURGE_HISTORY,
        DATA_PER_ISLAND,
        GLOW_COMPLETED,
        LOCKED_LEVEL_ICON,
        SHOW_TITLE,
        TITLE_SHOWTIME,
        /**
         * This allows to switch between different challenges visibility modes.
         */
        VISIBILITY_MODE
    }


    /**
     * This allows faster access to challenges settings object.
     */
    private final Settings settings;

    /**
     * Allows changing locked level icon.
     */
    private Button selectedButton;
}
