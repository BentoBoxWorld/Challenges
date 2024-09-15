package world.bentobox.challenges.panel.admin;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.panel.CommonPanel;
import world.bentobox.challenges.panel.ConversationUtils;
import world.bentobox.challenges.panel.util.ChallengeTypeSelector;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;
import world.bentobox.challenges.web.WebManager;


/**
 * This class contains Main
 */
public class AdminPanel extends CommonPanel
{
    // ---------------------------------------------------------------------
    // Section: Constructor
    // ---------------------------------------------------------------------

    /**
     * @param addon Addon where panel operates.
     * @param world World from which panel was created.
     * @param user User who created panel.
     * @param topLabel Command top label which creates panel (f.e. island or ai)
     * @param permissionPrefix Command permission prefix (f.e. bskyblock.)
     */
    private AdminPanel(ChallengesAddon addon,
        World world,
        User user,
        User viewer,
        String topLabel,
        String permissionPrefix)
    {
        super(addon, user, viewer, world, topLabel, permissionPrefix);
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
        new AdminPanel(addon, world, user, user, topLabel, permissionPrefix).build();
    }


    // ---------------------------------------------------------------------
    // Section: Methods
    // ---------------------------------------------------------------------


    /**
     * {@inheritDoc}
     */
    @Override
    protected void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
            this.user.getTranslation(Constants.TITLE + "admin-gui"));

        PanelUtils.fillBorder(panelBuilder);

        panelBuilder.item(10, this.createButton(Button.COMPLETE_USER_CHALLENGES));
        panelBuilder.item(19, this.createButton(Button.RESET_USER_CHALLENGES));

        // Add All Player Data removal.
        panelBuilder.item(28, this.createButton(Button.USER_WIPE));

        // Add Challenges
        panelBuilder.item(12, this.createButton(Button.ADD_CHALLENGE));
        panelBuilder.item(13, this.createButton(Button.ADD_LEVEL));

        // Edit Challenges
        panelBuilder.item(21, this.createButton(Button.EDIT_CHALLENGE));
        panelBuilder.item(22, this.createButton(Button.EDIT_LEVEL));

        // Remove Challenges
        panelBuilder.item(30, this.createButton(Button.DELETE_CHALLENGE));
        panelBuilder.item(31, this.createButton(Button.DELETE_LEVEL));

        // Import Challenges
        panelBuilder.item(14, this.createButton(Button.IMPORT_TEMPLATE));
        panelBuilder.item(15, this.createButton(Button.IMPORT_DATABASE));
        panelBuilder.item(33, this.createButton(Button.LIBRARY));
        // Export Challenges
        panelBuilder.item(24, this.createButton(Button.EXPORT_CHALLENGES));

        // Edit Addon Settings
        panelBuilder.item(16, this.createButton(Button.EDIT_SETTINGS));

        // Button that deletes everything from challenges addon

		if (this.wipeAll)
		{
			panelBuilder.item(34, this.createButton(Button.COMPLETE_WIPE));
		}
		else
		{
			panelBuilder.item(34, this.createButton(Button.CHALLENGE_WIPE));
		}

        panelBuilder.item(44, this.returnButton);

        panelBuilder.build();
    }


    /**
     * This method is used to create PanelItem for each button type.
     * @param button Button which must be created.
     * @return PanelItem with necessary functionality.
     */
    private PanelItem createButton(Button button)
    {
        final String name = this.user.getTranslation(Constants.BUTTON + button.name().toLowerCase() + ".name");
        List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(Constants.BUTTON + button.name().toLowerCase() + ".description"));

        ItemStack icon;
        boolean glow;
        PanelItem.ClickHandler clickHandler;


        switch (button)
        {
            case COMPLETE_USER_CHALLENGES -> {
                icon = new ItemStack(Material.WRITTEN_BOOK);
                clickHandler = (panel, user, clickType, slot) -> {
                    ListUsersPanel.open(this, ListUsersPanel.Mode.COMPLETE);
                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-open"));
            }
            case RESET_USER_CHALLENGES -> {
                icon = new ItemStack(Material.WRITABLE_BOOK);
                clickHandler = (panel, user, clickType, slot) -> {
                    if (clickType.isRightClick())
                    {
                        this.resetAllMode = !this.resetAllMode;
                        this.build();
                    }
                    else
                    {
                        ListUsersPanel.open(this,
                            this.resetAllMode ? ListUsersPanel.Mode.RESET_ALL : ListUsersPanel.Mode.RESET);
                    }

                    return true;
                };
                glow = this.resetAllMode;

                description.add("");
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "left-click-to-open"));
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "right-click-to-reset-all"));
            }
            case ADD_CHALLENGE -> {
                icon = new ItemStack(Material.BOOK);
                clickHandler = (panel, user, clickType, slot) -> {

                    String gameModePrefix = Utils.getGameMode(this.world).toLowerCase() + "_";

                    // This consumer process new bundle creating with a name and id from given
                    // consumer value..
                    Consumer<String> challengeIdConsumer = value -> {
                        if (value != null)
                        {
                            ChallengeTypeSelector.open(this.user,
                                (type, requirements) -> EditChallengePanel.open(this,
                                    this.addon.getChallengesManager().createChallenge(
                                        gameModePrefix + Utils.sanitizeInput(value),
                                        value,
                                        type,
                                        requirements)));
                        }
                        else
                        {
                            // Operation is canceled. Open this panel again.
                            this.build();
                        }
                    };

                    // This function checks if generator with a given ID already exist.
                    Function<String, Boolean> validationFunction = uniqueId ->
                        !this.addon.getChallengesManager().containsChallenge(gameModePrefix + Utils.sanitizeInput(uniqueId));

                    // Call a conversation API to get input string.
                    ConversationUtils.createIDStringInput(challengeIdConsumer,
                        validationFunction,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "write-name"),
                        this.user.getTranslation(Constants.CONVERSATIONS + "new-object-created",
                            Constants.PARAMETER_WORLD, this.world.getName()),
                        Constants.CONVERSATIONS + "object-already-exists");

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-create"));
            }
            case ADD_LEVEL -> {
                icon = new ItemStack(Material.BOOK);
                clickHandler = (panel, user, clickType, slot) -> {

                    String gameModePrefix = Utils.getGameMode(this.world).toLowerCase() + "_";

                    // This consumer process new bundle creating with a name and id from given
                    // consumer value..
                    Consumer<String> levelIdConsumer = value -> {
                        if (value != null)
                        {
                            EditLevelPanel.open(this,
                                this.addon.getChallengesManager().createLevel(
                                    gameModePrefix + Utils.sanitizeInput(value),
                                    value,
                                    world));
                        }
                        else
                        {
                            // Operation is canceled. Open this panel again.
                            this.build();
                        }
                    };

                    // This function checks if generator with a given ID already exist.
                    Function<String, Boolean> validationFunction = uniqueId ->
                        !this.addon.getChallengesManager().containsLevel(gameModePrefix + Utils.sanitizeInput(uniqueId));

                    // Call a conversation API to get input string.
                    ConversationUtils.createIDStringInput(levelIdConsumer,
                        validationFunction,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "write-name"),
                        this.user.getTranslation(Constants.CONVERSATIONS + "new-object-created",
                            Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world)),
                        Constants.CONVERSATIONS + "object-already-exists");

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-create"));
            }
            case EDIT_CHALLENGE -> {
                icon = new ItemStack(Material.ANVIL);
                clickHandler = (panel, user, clickType, slot) -> {
                    ListChallengesPanel.open(this, ListChallengesPanel.Mode.EDIT);
                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-open"));
            }
            case EDIT_LEVEL -> {
                icon = new ItemStack(Material.ANVIL);
                clickHandler = (panel, user, clickType, slot) -> {
                    ListLevelsPanel.open(this, ListLevelsPanel.Mode.EDIT);
                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-open"));
            }
            case DELETE_CHALLENGE -> {
                icon = new ItemStack(Material.LAVA_BUCKET);
                clickHandler = (panel, user, clickType, slot) -> {
                    ListChallengesPanel.open(this, ListChallengesPanel.Mode.DELETE);
                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-open"));
            }
            case DELETE_LEVEL -> {
                icon = new ItemStack(Material.LAVA_BUCKET);
                clickHandler = (panel, user, clickType, slot) -> {
                    ListLevelsPanel.open(this, ListLevelsPanel.Mode.DELETE);
                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-open"));
            }
            case EDIT_SETTINGS -> {
                icon = new ItemStack(Material.CRAFTING_TABLE);
                clickHandler = (panel, user, clickType, slot) -> {
                    EditSettingsPanel.open(this);
                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-open"));
            }
            case IMPORT_DATABASE -> {
                icon = new ItemStack(Material.BOOKSHELF);
                clickHandler = (panel, user, clickType, slot) -> {
                    LibraryPanel.open(this, LibraryPanel.Library.DATABASE);
                    return true;
                };
                glow = true;

                description.add("");
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-open"));
            }
            case IMPORT_TEMPLATE -> {
                icon = new ItemStack(Material.BOOKSHELF);
                clickHandler = (panel, user, clickType, slot) -> {
                    LibraryPanel.open(this, LibraryPanel.Library.TEMPLATE);
                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-open"));
            }
            case EXPORT_CHALLENGES -> {
                icon = new ItemStack(Material.HOPPER);
                clickHandler = (panel, user, clickType, slot) -> {

                    // This consumer process file exporting after user input is returned.
                    Consumer<String> fileNameConsumer = value -> {
                        if (value != null)
                        {
                            this.addon.getImportManager().generateDatabaseFile(this.user,
                                this.world,
                                Utils.sanitizeInput(value));
                        }

                        this.build();
                    };

                    // This function checks if file can be created.
                    Function<String, Boolean> validationFunction = fileName ->
                    {
                        String sanitizedName = Utils.sanitizeInput(fileName);
                        return !new File(this.addon.getDataFolder(),
                            sanitizedName.endsWith(".json") ? sanitizedName : sanitizedName + ".json").exists();
                    };

                    // Call a conversation API to get input string.
                    ConversationUtils.createIDStringInput(fileNameConsumer,
                        validationFunction,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "exported-file-name"),
                        this.user.getTranslation(Constants.CONVERSATIONS + "database-export-completed",
                            Constants.PARAMETER_WORLD, world.getName()),
                        Constants.CONVERSATIONS + "file-name-exist");

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-export"));
            }
            case LIBRARY -> {
                if (WebManager.isEnabled())
                {
                    icon = new ItemStack(Material.COBWEB);
                }
                else
                {
                    icon = new ItemStack(Material.STRUCTURE_VOID);
                }

                clickHandler = (panel, user, clickType, slot) -> {
                    if (WebManager.isEnabled())
                    {
                        LibraryPanel.open(this, LibraryPanel.Library.WEB);
                    }

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-open"));
            }
            case COMPLETE_WIPE -> {
                icon = new ItemStack(Material.TNT);
                clickHandler = (panel, user, clickType, slot) -> {

                    if (clickType.isRightClick())
                    {
                        this.wipeAll = false;
                        this.build();
                    }
                    else
                    {
                        Consumer<Boolean> consumer = value -> {
                            if (value)
                            {
                                this.addon.getChallengesManager().wipeDatabase(this.wipeAll,
                                    Utils.getGameMode(this.world));
                            }

                            this.build();
                        };

                        // Create conversation that gets user acceptance to delete generator data.
                        ConversationUtils.createConfirmation(
                            consumer,
                            this.user,
                            this.user.getTranslation(Constants.CONVERSATIONS + "confirm-all-data-deletion",
                                Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world)),
                            this.user.getTranslation(Constants.CONVERSATIONS + "all-data-removed",
                                Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world)));
                    }

                    return true;
                };
                glow = true;

                description.add("");
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "left-click-to-wipe"));
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "right-click-to-switch"));
            }
            case CHALLENGE_WIPE -> {
                icon = new ItemStack(Material.TNT);
                clickHandler = (panel, user, clickType, slot) -> {

                    if (clickType.isRightClick())
                    {
                        this.wipeAll = true;
                        this.build();
                    }
                    else
                    {
                        Consumer<Boolean> consumer = value -> {
                            if (value)
                            {
                                this.addon.getChallengesManager().wipeDatabase(this.wipeAll,
                                    Utils.getGameMode(this.world));
                            }

                            this.build();
                        };

                        // Create conversation that gets user acceptance to delete generator data.
                        ConversationUtils.createConfirmation(
                            consumer,
                            this.user,
                            this.user.getTranslation(Constants.CONVERSATIONS + "confirm-challenge-data-deletion",
                                Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world)),
                            this.user.getTranslation(Constants.CONVERSATIONS + "challenge-data-removed",
                                Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world)));
                    }

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "left-click-to-wipe"));
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "right-click-to-switch"));
            }
            case USER_WIPE -> {
                icon = new ItemStack(Material.TNT);
                clickHandler = (panel, user, clickType, slot) -> {

                    Consumer<Boolean> consumer = value -> {
                        if (value)
                        {
                            this.addon.getChallengesManager().wipePlayers(Utils.getGameMode(this.world));
                        }

                        this.build();
                    };

                    // Create conversation that gets user acceptance to delete generator data.
                    ConversationUtils.createConfirmation(
                        consumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "confirm-user-data-deletion",
                            Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world)),
                        this.user.getTranslation(Constants.CONVERSATIONS + "user-data-removed",
                            Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world)));

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-wipe"));
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
    // Section: Enums
    // ---------------------------------------------------------------------


    /**
     * This enum contains all button variations. Just for cleaner code.
     */
    private enum Button
    {
        COMPLETE_USER_CHALLENGES,
        RESET_USER_CHALLENGES,
        ADD_CHALLENGE,
        ADD_LEVEL,
        EDIT_CHALLENGE,
        EDIT_LEVEL,
        DELETE_CHALLENGE,
        DELETE_LEVEL,
        EDIT_SETTINGS,
        IMPORT_DATABASE,
        IMPORT_TEMPLATE,
        EXPORT_CHALLENGES,
        /**
         * Allows to remove whole database
         */
        COMPLETE_WIPE,
        /**
         * Allows to remove only challenges and levels
         */
        CHALLENGE_WIPE,
        /**
         * Allows to remove only players data
         */
        USER_WIPE,
        /**
         * Allows to access Web Library
         */
        LIBRARY
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * This indicates if Reset Challenges must work as reset all.
     */
    private boolean resetAllMode;

    /**
     * This indicates if wipe button should clear all data, or only challenges.
     */
    private boolean wipeAll;
}