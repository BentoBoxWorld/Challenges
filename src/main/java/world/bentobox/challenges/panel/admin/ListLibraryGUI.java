package world.bentobox.challenges.panel.admin;


import org.bukkit.ChatColor;
import org.bukkit.World;
import java.util.ArrayList;
import java.util.List;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.panel.CommonGUI;
import world.bentobox.challenges.utils.GuiUtils;
import world.bentobox.challenges.web.object.LibraryEntry;


/**
 * This class contains all necessary elements to create GUI that lists all challenges.
 * It allows to edit them or remove, depending on given input mode.
 */
public class ListLibraryGUI extends CommonGUI
{
    // ---------------------------------------------------------------------
    // Section: Constructor
    // ---------------------------------------------------------------------

    /**
     * @param parentGUI ParentGUI object.
     */
    public ListLibraryGUI(CommonGUI parentGUI)
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
    public ListLibraryGUI(ChallengesAddon addon,
            World world,
            User user,
            String topLabel,
            String permissionPrefix)
    {
        super(addon, world, user, topLabel, permissionPrefix, null);
    }


    /**
     * This static method allows to easier open Library GUI.
     * @param parentGui ParentGUI object.
     */
    public static void open(CommonGUI parentGui)
    {
        new ListLibraryGUI(parentGui).build();
    }


    // ---------------------------------------------------------------------
    // Section: Methods
    // ---------------------------------------------------------------------


    /**
     * {@inheritDoc}
     */
    @Override
    public void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
            this.user.getTranslation("challenges.gui.title.admin.library-title"));

        GuiUtils.fillBorder(panelBuilder);

        List<LibraryEntry> libraryEntries = this.addon.getWebManager().getLibraryEntries();

        final int MAX_ELEMENTS = 21;

        if (this.pageIndex < 0)
        {
            this.pageIndex = libraryEntries.size() / MAX_ELEMENTS;
        }
        else if (this.pageIndex > (libraryEntries.size() / MAX_ELEMENTS))
        {
            this.pageIndex = 0;
        }

        int entryIndex = MAX_ELEMENTS * this.pageIndex;

        // I want first row to be only for navigation and return button.
        int index = 10;

        while (entryIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
                entryIndex < libraryEntries.size() &&
                index < 36)
        {
            if (!panelBuilder.slotOccupied(index))
            {
                panelBuilder.item(index, this.createEntryIcon(libraryEntries.get(entryIndex++)));
            }

            index++;
        }

        // Navigation buttons only if necessary
        if (libraryEntries.size() > MAX_ELEMENTS)
        {
            panelBuilder.item(18, this.getButton(CommonButtons.PREVIOUS));
            panelBuilder.item(26, this.getButton(CommonButtons.NEXT));
        }

        panelBuilder.item(44, this.returnButton);

        panelBuilder.build();
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
                icon(libraryEntry.getIcon()).
                glow(false);

        itemBuilder.clickHandler((panel, user1, clickType, i) -> {

            if (!this.blockedForDownland)
            {
                this.blockedForDownland = true;

                this.user.sendMessage("challenges.messages.admin.start-downloading");

                // Run download task after 5 ticks.
                this.addon.getPlugin().getServer().getScheduler().
                    runTaskLaterAsynchronously(
                        this.addon.getPlugin(),
                        () -> this.addon.getWebManager().requestEntryGitHubData(this.user, this.world, libraryEntry),
                        5L);

                if (this.parentGUI != null)
                {
                    this.parentGUI.build();
                }
                else
                {
                    this.user.closeInventory();
                }
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
        List<String> description = new ArrayList<>();

        description.add(this.user.getTranslation(REFERENCE_DESCRIPTION + "library-author",
            "[author]",
            entry.getAuthor()));
        description.add(entry.getDescription());

        description.add(this.user.getTranslation(REFERENCE_DESCRIPTION + "library-gamemode",
            "[gamemode]",
            entry.getForGameMode()));
        description.add(this.user.getTranslation(REFERENCE_DESCRIPTION + "library-lang",
            "[lang]",
            entry.getLanguage()));
        description.add(this.user.getTranslation(REFERENCE_DESCRIPTION + "library-version",
            "[version]",
            entry.getVersion()));

        return GuiUtils.stringSplit(description,
            this.addon.getChallengesSettings().getLoreLineLength());
    }


// ---------------------------------------------------------------------
// Section: Instance Variables
// ---------------------------------------------------------------------


    /**
     * This variable will protect against spam-click.
     */
    private boolean blockedForDownland;

    /**
     * Reference string to description.
     */
    private static final String REFERENCE_DESCRIPTION = "challenges.gui.descriptions.admin.";
}
