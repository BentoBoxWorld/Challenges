package world.bentobox.challenges.panel.admin;


import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.config.Settings;
import world.bentobox.challenges.config.SettingsUtils.GuiMode;
import world.bentobox.challenges.panel.CommonGUI;
import world.bentobox.challenges.panel.util.NumberGUI;
import world.bentobox.challenges.panel.util.SelectBlocksGUI;
import world.bentobox.challenges.utils.GuiUtils;


/**
 * This Class creates GUI that allows to change Challenges Addon Settings via in-game
 * menu.
 */
public class EditSettingsGUI extends CommonGUI
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
    public EditSettingsGUI(ChallengesAddon addon,
            World world,
            User user,
            String topLabel,
            String permissionPrefix)
    {
        this(addon, world, user, topLabel, permissionPrefix, null);
    }


    /**
     * @param addon Addon where panel operates.
     * @param world World from which panel was created.
     * @param user User who created panel.
     * @param topLabel Command top label which creates panel (f.e. island or ai)
     * @param permissionPrefix Command permission prefix (f.e. bskyblock.)
     */
    public EditSettingsGUI(ChallengesAddon addon,
            World world,
            User user,
            String topLabel,
            String permissionPrefix,
            CommonGUI parentGUI)
    {
        super(addon, world, user, topLabel, permissionPrefix, parentGUI);
        this.settings = this.addon.getChallengesSettings();
    }


    // ---------------------------------------------------------------------
    // Section: Methods
    // ---------------------------------------------------------------------


    @Override
    public void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
                this.user.getTranslation("challenges.gui.title.admin.settings-title"));

        GuiUtils.fillBorder(panelBuilder);

        panelBuilder.item(10, this.getSettingsButton(Button.ENABLE_TITLE));

        if (this.settings.isShowCompletionTitle())
        {
            panelBuilder.item(19, this.getSettingsButton(Button.TITLE_SHOWTIME));
        }

        panelBuilder.item(28, this.getSettingsButton(Button.BROADCAST));

        panelBuilder.item(20, this.getSettingsButton(Button.GLOW_COMPLETED));
        panelBuilder.item(29, this.getSettingsButton(Button.REMOVE_COMPLETED));

        panelBuilder.item(21, this.getSettingsButton(Button.LOCKED_LEVEL_ICON));
        panelBuilder.item(30, this.getSettingsButton(Button.FREE_AT_TOP));

        panelBuilder.item(22, this.getSettingsButton(Button.GAMEMODE_GUI));

        if (this.settings.isUseCommonGUI())
        {
            // This should be active only when single gui is enabled.
            panelBuilder.item(31, this.getSettingsButton(Button.GAMEMODE_GUI_VIEW_MODE));
        }

        panelBuilder.item(14, this.getSettingsButton(Button.LORE_LENGTH));
        panelBuilder.item(23, this.getSettingsButton(Button.CHALLENGE_LORE));
        panelBuilder.item(32, this.getSettingsButton(Button.LEVEL_LORE));

        panelBuilder.item(24, this.getSettingsButton(Button.HISTORY));

        if (this.settings.isStoreHistory())
        {
            panelBuilder.item(33, this.getSettingsButton(Button.PURGE_HISTORY));
        }

        panelBuilder.item(25, this.getSettingsButton(Button.RESET_CHALLENGES));
        panelBuilder.item(34, this.getSettingsButton(Button.STORE_MODE));

        // Return Button
        panelBuilder.item(44, this.returnButton);

        panelBuilder.build();
    }


    @SuppressWarnings("deprecation")
    private PanelItem getSettingsButton(Button button)
    {
        ItemStack icon;
        String name;
        List<String> description;
        boolean glow;
        PanelItem.ClickHandler clickHandler;

        switch (button)
        {
        case RESET_CHALLENGES:
        {
            description = new ArrayList<>(2);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.reset-on-new"));
            description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]",
                    this.settings.isResetChallenges() ?
                            this.user.getTranslation("challenges.gui.descriptions.enabled") :
                                this.user.getTranslation("challenges.gui.descriptions.disabled")));
            name = this.user.getTranslation("challenges.gui.buttons.admin.reset-on-new");
            icon = new ItemStack(Material.LAVA_BUCKET);

            clickHandler = (panel, user1, clickType, i) -> {
                this.settings.setResetChallenges(
                        !this.settings.isResetChallenges());

                panel.getInventory().setItem(i, this.getSettingsButton(button).getItem());
                return true;
            };

            glow = this.settings.isResetChallenges();
            break;
        }
        case BROADCAST:
        {
            description = new ArrayList<>(2);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.broadcast"));
            description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]",
                    this.settings.isBroadcastMessages() ?
                            this.user.getTranslation("challenges.gui.descriptions.enabled") :
                                this.user.getTranslation("challenges.gui.descriptions.disabled")));

            description = new ArrayList<>(2);

            name = this.user.getTranslation("challenges.gui.buttons.admin.broadcast");
            icon = new ItemStack(Material.JUKEBOX);
            clickHandler = (panel, user1, clickType, i) -> {
                this.settings.setBroadcastMessages(
                        !this.settings.isBroadcastMessages());
                panel.getInventory().setItem(i, this.getSettingsButton(button).getItem());
                return true;
            };
            glow = this.settings.isBroadcastMessages();

            break;
        }
        case REMOVE_COMPLETED:
        {
            description = new ArrayList<>(2);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.remove-completed"));
            description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]",
                    this.settings.isRemoveCompleteOneTimeChallenges() ?
                            this.user.getTranslation("challenges.gui.descriptions.enabled") :
                                this.user.getTranslation("challenges.gui.descriptions.disabled")));

            name = this.user.getTranslation("challenges.gui.buttons.admin.remove-completed");
            icon = new ItemStack(Material.MAGMA_BLOCK);
            clickHandler = (panel, user1, clickType, i) -> {
                this.settings.setRemoveCompleteOneTimeChallenges(
                        !this.settings.isRemoveCompleteOneTimeChallenges());
                panel.getInventory().setItem(i, this.getSettingsButton(button).getItem());
                return true;
            };
            glow = this.settings.isRemoveCompleteOneTimeChallenges();

            break;
        }
        case LORE_LENGTH:
        {
            description = new ArrayList<>(2);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.line-length"));
            description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]", Integer.toString(this.settings.getLoreLineLength())));
            name = this.user.getTranslation("challenges.gui.buttons.admin.line-length");
            icon = new ItemStack(Material.ANVIL);
            clickHandler = (panel, user1, clickType, i) -> {
                new NumberGUI(this.user,
                        this.settings.getLoreLineLength(),
                        0,
                        this.settings.getLoreLineLength(),
                        (status, value) -> {
                            if (status)
                            {
                                this.settings.setLoreLineLength(value);
                            }

                            panel.getInventory().setItem(i, this.getSettingsButton(button).getItem());
                        });

                return true;
            };
            glow = false;
            break;
        }
        case LEVEL_LORE:
        {
            description = new ArrayList<>(2);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.level-lore"));
            name = this.user.getTranslation("challenges.gui.buttons.admin.level-lore");
            icon = new ItemStack(Material.MAP);
            clickHandler = (panel, user1, clickType, i) -> {

                EditLoreGUI.open(this, EditLoreGUI.LoreType.LEVELS);

                return true;
            };
            glow = false;
            break;
        }
        case CHALLENGE_LORE:
        {
            description = new ArrayList<>(2);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.challenge-lore"));
            name = this.user.getTranslation("challenges.gui.buttons.admin.challenge-lore");
            icon = new ItemStack(Material.PAPER);
            clickHandler = (panel, user1, clickType, i) -> {

                EditLoreGUI.open(this, EditLoreGUI.LoreType.CHALLENGES);

                return true;
            };
            glow = false;
            break;
        }
        case FREE_AT_TOP:
        {
            description = new ArrayList<>(2);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.free-at-top"));
            description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]",
                    this.settings.isAddCompletedGlow() ?
                            this.user.getTranslation("challenges.gui.descriptions.enabled") :
                                this.user.getTranslation("challenges.gui.descriptions.disabled")));
            name = this.user.getTranslation("challenges.gui.buttons.admin.free-at-top");
            icon = new ItemStack(Material.FILLED_MAP);
            clickHandler = (panel, user1, clickType, i) -> {
                this.settings.setFreeChallengesFirst(!this.settings.isFreeChallengesFirst());
                panel.getInventory().setItem(i, this.getSettingsButton(button).getItem());
                return true;
            };
            glow = this.settings.isFreeChallengesFirst();
            break;
        }
        case GLOW_COMPLETED:
        {
            description = new ArrayList<>(2);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.glow"));
            description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]",
                    this.settings.isAddCompletedGlow() ?
                            this.user.getTranslation("challenges.gui.descriptions.enabled") :
                                this.user.getTranslation("challenges.gui.descriptions.disabled")));


            name = this.user.getTranslation("challenges.gui.buttons.admin.glow");
            icon = new ItemStack(Material.GLOWSTONE);
            clickHandler = (panel, user1, clickType, i) -> {
                this.settings.setAddCompletedGlow(!this.settings.isAddCompletedGlow());
                panel.getInventory().setItem(i, this.getSettingsButton(button).getItem());
                return true;
            };
            glow = this.settings.isAddCompletedGlow();
            break;
        }
        case GAMEMODE_GUI_VIEW_MODE:
        {
            description = new ArrayList<>(2);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.gui-view-mode"));
            description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]",
                    this.settings.getUserGuiMode().equals(GuiMode.GAMEMODE_LIST) ?
                            this.user.getTranslation("challenges.gui.descriptions.enabled") :
                                this.user.getTranslation("challenges.gui.descriptions.disabled")));
            name = this.user.getTranslation("challenges.gui.buttons.admin.gui-view-mode");
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

                panel.getInventory().setItem(i, this.getSettingsButton(button).getItem());
                return true;
            };
            glow = this.settings.getUserGuiMode().equals(GuiMode.GAMEMODE_LIST);
            break;
        }
        case GAMEMODE_GUI:
        {
            description = new ArrayList<>(2);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.gui-mode"));
            description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]",
                    this.settings.isUseCommonGUI() ?
                            this.user.getTranslation("challenges.gui.descriptions.enabled") :
                                this.user.getTranslation("challenges.gui.descriptions.disabled")));
            name = this.user.getTranslation("challenges.gui.buttons.admin.gui-mode");
            icon = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            clickHandler = (panel, user1, clickType, i) -> {
                this.settings.setUseCommonGUI(!this.settings.isUseCommonGUI());
                // We cannot use single item changing as this option enabling/disabling will change other
                // option visibility.
                this.build();
                return true;
            };
            glow = this.settings.isUseCommonGUI();
            break;
        }
        case HISTORY:
        {
            description = new ArrayList<>(2);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.history-store"));
            description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]",
                    this.settings.isStoreHistory() ?
                            this.user.getTranslation("challenges.gui.descriptions.enabled") :
                                this.user.getTranslation("challenges.gui.descriptions.disabled")));
            name = this.user.getTranslation("challenges.gui.buttons.admin.history-store");
            icon = new ItemStack(Material.WRITTEN_BOOK);
            clickHandler = (panel, user1, clickType, i) -> {
                this.settings.setStoreHistory(!this.settings.isStoreHistory());

                // Need to rebuild all as new buttons will show up.
                this.build();
                return true;
            };
            glow = this.settings.isStoreHistory();
            break;
        }
        case PURGE_HISTORY:
        {
            description = new ArrayList<>(2);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.history-lifespan"));
            description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]", Integer.toString(this.settings.getLifeSpan())));
            name = this.user.getTranslation("challenges.gui.buttons.admin.history-lifespan");
            icon = new ItemStack(Material.FLINT_AND_STEEL);
            clickHandler = (panel, user1, clickType, i) -> {
                new NumberGUI(this.user,
                        this.settings.getLifeSpan(),
                        0,
                        this.settings.getLoreLineLength(),
                        (status, value) -> {
                            if (status)
                            {
                                this.settings.setLifeSpan(value);
                            }

                            panel.getInventory().setItem(i, this.getSettingsButton(button).getItem());
                        });

                return true;
            };
            glow = false;
            break;
        }
        case STORE_MODE:
        {
            description = new ArrayList<>(2);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.island-store"));
            description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]",
                    this.settings.isStoreAsIslandData() ?
                            this.user.getTranslation("challenges.gui.descriptions.enabled") :
                                this.user.getTranslation("challenges.gui.descriptions.disabled")));
            name = this.user.getTranslation("challenges.gui.buttons.admin.island-store");
            icon = new ItemStack(Material.GRASS_BLOCK);
            clickHandler = (panel, user1, clickType, i) -> {
                this.settings.setStoreAsIslandData(!this.settings.isStoreAsIslandData());
                // TODO: Data Migration must be added here.
                panel.getInventory().setItem(i, this.getSettingsButton(button).getItem());
                return true;
            };
            glow = this.settings.isStoreAsIslandData();
            break;
        }
        case LOCKED_LEVEL_ICON:
        {
            description = new ArrayList<>(1);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.default-locked-icon"));
            name = this.user.getTranslation("challenges.gui.buttons.admin.default-locked-icon");
            icon = this.settings.getLockedLevelIcon();
            clickHandler = (panel, user, clickType, slot) -> {

                new SelectBlocksGUI(this.user, true, (status, materials) -> {
                    if (status)
                    {
                        materials.forEach(material ->
                            this.settings.setLockedLevelIcon(new ItemStack(material)));
                    }

                    this.build();
                });

                return true;
            };
            glow = false;
            break;
        }
        case ENABLE_TITLE:
        {
            description = new ArrayList<>(2);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.title-enable"));
            description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]",
                    this.settings.isShowCompletionTitle() ?
                            this.user.getTranslation("challenges.gui.descriptions.enabled") :
                                this.user.getTranslation("challenges.gui.descriptions.disabled")));
            name = this.user.getTranslation("challenges.gui.buttons.admin.title-enable");
            icon = new ItemStack(ChallengesAddon.SIGN_MATERIAL);
            clickHandler = (panel, user1, clickType, i) -> {
                this.settings.setShowCompletionTitle(!this.settings.isShowCompletionTitle());

                // Need to rebuild all as new buttons will show up.
                this.build();
                return true;
            };
            glow = this.settings.isShowCompletionTitle();
            break;
        }
        case TITLE_SHOWTIME:
        {
            description = new ArrayList<>(2);
            description.add(this.user.getTranslation("challenges.gui.descriptions.admin.title-showtime"));
            description.add(this.user.getTranslation("challenges.gui.descriptions.current-value",
                    "[value]", Integer.toString(this.settings.getTitleShowtime())));
            name = this.user.getTranslation("challenges.gui.buttons.admin.title-showtime");
            icon = new ItemStack(Material.CLOCK);
            clickHandler = (panel, user1, clickType, i) -> {
                new NumberGUI(this.user,
                        this.settings.getTitleShowtime(),
                        0,
                        this.settings.getLoreLineLength(),
                        (status, value) -> {
                            if (status)
                            {
                                this.settings.setTitleShowtime(value);
                            }

                            panel.getInventory().setItem(i, this.getSettingsButton(button).getItem());
                        });

                return true;
            };
            glow = false;
            break;
        }
        default:
            return new PanelItemBuilder().build();
        }

        return new PanelItemBuilder().
                icon(icon).
                name(name).
                description(GuiUtils.stringSplit(description, this.settings.getLoreLineLength())).
                glow(glow).
                clickHandler(clickHandler).
                build();
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * This enum holds all settings buttons that must have been displayed in this panel.
     */
    private enum Button
    {
        RESET_CHALLENGES,
        BROADCAST,
        REMOVE_COMPLETED,
        LORE_LENGTH,
        LEVEL_LORE,
        CHALLENGE_LORE,
        FREE_AT_TOP,
        GAMEMODE_GUI_VIEW_MODE,
        GAMEMODE_GUI,
        HISTORY,
        PURGE_HISTORY,
        STORE_MODE,
        GLOW_COMPLETED,
        LOCKED_LEVEL_ICON,
        ENABLE_TITLE,
        TITLE_SHOWTIME
    }


    /**
     * This allows faster access to challenges settings object.
     */
    private Settings settings;
}
