package world.bentobox.challenges.panel.admin;


import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitTask;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.PanelListener;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.panel.CommonPagedPanel;
import world.bentobox.challenges.panel.CommonPanel;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.GuiUtils;
import world.bentobox.challenges.utils.Utils;
import world.bentobox.challenges.web.object.LibraryEntry;


/**
 * This class contains all necessary elements to create GUI that lists all challenges.
 * It allows to edit them or remove, depending on given input mode.
 */
public class ListLibraryPanel extends CommonPagedPanel
{
    // ---------------------------------------------------------------------
    // Section: Constructor
    // ---------------------------------------------------------------------

    /**
     * @param parentGUI ParentGUI object.
     */
    private ListLibraryPanel(CommonPanel parentGUI)
    {
        super(parentGUI);
    }


    /**
     * @param addon Addon where panel operates.
     * @param world World from which panel was created.
     * @param user User who created panel.
     * @param topLabel Command top label which creates panel (f.e. island or ai)
     * @param permissionPrefix Command permission prefix (f.e. bskyblock.)
     */
    private ListLibraryPanel(ChallengesAddon addon,
            World world,
            User user,
            String topLabel,
            String permissionPrefix)
    {
        super(addon, user, world, topLabel, permissionPrefix);
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
        new ListLibraryPanel(addon, world, user, topLabel, permissionPrefix).build();
    }



    /**
     * This static method allows to easier open Library GUI.
     * @param parentGui ParentGUI object.
     */
    public static void open(CommonPanel parentGui)
    {
        new ListLibraryPanel(parentGui).build();
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
            this.user.getTranslation(Constants.TITLE + "library"));

        GuiUtils.fillBorder(panelBuilder);

        this.populateElements(panelBuilder,
            this.addon.getWebManager().getLibraryEntries(),
            o -> this.createEntryIcon((LibraryEntry) o));

        panelBuilder.item(4, this.createDownloadNow());
        panelBuilder.item(44, this.returnButton);

        panelBuilder.listener(new DownloadCanceller());

        panelBuilder.build();
    }


    /**
     * This creates download now button, that can skip waiting for automatic request.
     * @return PanelItem button that allows to manually download libraries.
     */
    private PanelItem createDownloadNow()
    {
        final String reference = Constants.BUTTON + "download.";

        final List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(reference + "description"));
        description.add(this.user.getTranslation(reference +
            (this.clearCache ? "enabled" : "disabled")));

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-download"));
        description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-toggle"));

        PanelItemBuilder itemBuilder = new PanelItemBuilder().
            name(this.user.getTranslation(reference + "name")).
            description(description).
            icon(Material.HOPPER).
            glow(this.clearCache);

        itemBuilder.clickHandler((panel, user1, clickType, slot) ->
        {
            if (clickType.isRightClick())
            {
                this.clearCache = !this.clearCache;
                panel.getInventory().setItem(slot, this.createDownloadNow().getItem());
            }
            else
            {
                this.addon.getWebManager().requestCatalogGitHubData(this.clearCache);

                // Fix multiclick issue.
                if (this.updateTask != null)
                {
                    this.updateTask.cancel();
                }

                // add some delay to rebuilding gui.
                this.updateTask = this.addon.getPlugin().getServer().getScheduler().runTaskLater(
                    this.addon.getPlugin(),
                    this::build,
                    100L);
            }

            return true;
        });

        return itemBuilder.build();
    }


    /**
     * This method creates button for given library entry.
     * @param libraryEntry LibraryEntry which button must be created.
     * @return Entry button.
     */
    private PanelItem createEntryIcon(LibraryEntry libraryEntry)
    {
        PanelItemBuilder itemBuilder = new PanelItemBuilder().
            name(ChatColor.translateAlternateColorCodes('&', libraryEntry.getName())).
            description(this.generateEntryDescription(libraryEntry)).
            description("").
            description(this.user.getTranslation(Constants.TIPS + "click-to-install")).
            icon(libraryEntry.getIcon()).
            glow(false);

        itemBuilder.clickHandler((panel, user1, clickType, i) -> {

            if (!this.blockedForDownland)
            {
                this.blockedForDownland = true;

                Utils.sendMessage(this.user,
                    this.user.getTranslation(Constants.CONVERSATIONS + "start-downloading"));

                // Run download task after 5 ticks.
                this.addon.getPlugin().getServer().getScheduler().
                    runTaskLaterAsynchronously(
                        this.addon.getPlugin(),
                        () -> this.addon.getWebManager().requestEntryGitHubData(this.user, this.world, libraryEntry),
                        5L);

                this.build();
            }

            return true;
        });

        return itemBuilder.build();
    }


    /**
     * This method generated description for LibraryEntry object.
     * @param entry LibraryEntry object which description must be generated.
     * @return List of strings that will be placed in ItemStack lore message.
     */
    private List<String> generateEntryDescription(LibraryEntry entry)
    {
        final String reference = Constants.DESCRIPTIONS + "library.";

        List<String> description = new ArrayList<>();

        description.add(this.user.getTranslation(reference + "author",
            "[author]", entry.getAuthor()));
        description.add(entry.getDescription());

        description.add(this.user.getTranslation(reference + "gamemode",
            "[gamemode]", entry.getForGameMode()));
        description.add(this.user.getTranslation(reference + "lang",
            "[lang]", entry.getLanguage()));
        description.add(this.user.getTranslation(reference + "version",
            "[version]", entry.getVersion()));

        return description;
    }


    /**
     * This class allows changing icon for Generator Tier
     */
    private class DownloadCanceller implements PanelListener
    {
        /**
         * On inventory click.
         *
         * @param user the user
         * @param event the event
         */
        @Override
        public void onInventoryClick(User user, InventoryClickEvent event)
        {
            // do nothing
        }


        /**
         * On inventory close.
         *
         * @param event the event
         */
        @Override
        public void onInventoryClose(InventoryCloseEvent event)
        {
            if (ListLibraryPanel.this.updateTask != null)
            {
                ListLibraryPanel.this.updateTask.cancel();
            }
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
// Section: Instance Variables
// ---------------------------------------------------------------------

    /**
     * Indicates if download now button should trigger cache clearing.
     */
    private boolean clearCache;

    /**
     * Stores update task that is triggered.
     */
    private BukkitTask updateTask = null;

    /**
     * This variable will protect against spam-click.
     */
    private boolean blockedForDownland;
}
