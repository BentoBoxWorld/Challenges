package world.bentobox.challenges.panel.util;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.advancement.Advancement;
import org.bukkit.inventory.ItemStack;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.Constants;


/**
 * This GUI allows to select single entity and return it via Consumer.
 */
public class SingleAdvancementSelector extends PagedSelector<Advancement>
{
    /**
     * Instantiates a new Single advancement selector.
     *
     * @param user the user
     * @param mode the mode
     * @param excluded the excluded
     * @param consumer the consumer
     */
    private SingleAdvancementSelector(User user, Mode mode, Set<Advancement> excluded,
            BiConsumer<Boolean, Advancement> consumer)
    {
        super(user);
        this.elements = new ArrayList<Advancement>();
        this.consumer = consumer;
        Bukkit.advancementIterator().forEachRemaining(elements::add);
        elements.removeIf(a -> a.getDisplay() == null); // Remove any that don't get displayed
        elements.sort(Comparator.comparing(advancement -> advancement.getDisplay().getTitle()));
        // Init without filters applied.
        this.filterElements = this.elements;
    }


    /**
     * This method opens GUI that allows to select challenge type.
     *
     * @param user User who opens GUI.
     * @param consumer Consumer that allows to get clicked type.
     */
    public static void open(User user, Mode mode, Set<Advancement> excluded, BiConsumer<Boolean, Advancement> consumer)
    {
        new SingleAdvancementSelector(user, mode, excluded, consumer).build();
    }


    /**
     * This method opens GUI that allows to select challenge type.
     *
     * @param user User who opens GUI.
     * @param consumer Consumer that allows to get clicked type.
     */
    public static void open(User user, BiConsumer<Boolean, Advancement> consumer)
    {
        new SingleAdvancementSelector(user, Mode.ANY, new HashSet<>(), consumer).build();
    }


    /**
     * This method opens GUI that allows to select challenge type.
     *
     * @param user User who opens GUI.
     * @param consumer Consumer that allows to get clicked type.
     */
    public static void open(User user, Mode mode, BiConsumer<Boolean, Advancement> consumer)
    {
        new SingleAdvancementSelector(user, mode, new HashSet<>(), consumer).build();
    }


    // ---------------------------------------------------------------------
    // Section: Methods
    // ---------------------------------------------------------------------


    /**
     * This method builds
     */
    @Override
    protected void build() {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user);
        panelBuilder.name(this.user.getTranslation(Constants.TITLE + "advancement-selector"));

        PanelUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

        this.populateElements(panelBuilder, this.filterElements);

        panelBuilder.item(4, this.createButton());

        panelBuilder.build();
    }


    /**
     * This method is called when filter value is updated.
     */
    @Override
    protected void updateFilters() {
        if (this.searchString == null || this.searchString.isBlank()) {
            this.filterElements = this.elements;
        } else {
            this.filterElements = this.elements.stream().filter(element -> {
                // If element name is set and name contains search field, then do not filter out.
                return element.getDisplay().getTitle().toLowerCase().contains(this.searchString.toLowerCase());
            }).distinct().collect(Collectors.toList());
        }
    }


    /**
     * This method builds PanelItem for given entity.
     * @param entity Entity which PanelItem must be created.
     * @return new PanelItem for given Entity.
     */
    @Override
    protected PanelItem createElementButton(Advancement advancement) {
        final String reference = Constants.BUTTON + "advancement.";
        List<String> description = new ArrayList<>();
        description.add(this.user.getTranslation(reference + "description", "[description]",
                advancement.getDisplay().getDescription()));
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-choose"));


        return new PanelItemBuilder()
                .name(this.user.getTranslation(reference + "name", "[name]",
                        advancement.getDisplay().getTitle()))
                .icon(getIcon(advancement)).description(description)
                .clickHandler((panel, user1, clickType, slot) -> {
                    this.consumer.accept(true, advancement);
                    return true;
                }).build();
    }

    /**
     * Get an ItemStack icon for any entity type, or PAPER if it's not really known
     * @param et entity type
     * @return ItemStack
     */
    public static ItemStack getIcon(Advancement advancement) {
        return advancement.getDisplay().getIcon();
    }

    /**
     * This method creates PanelItem button of requested type.
     * @return new PanelItem with requested functionality.
     */
    private PanelItem createButton() {
        final String reference = Constants.BUTTON + "cancel.";

        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(reference + "description"));

        ItemStack icon = new ItemStack(Material.IRON_DOOR);
        PanelItem.ClickHandler clickHandler = (panel, user1, clickType, slot) -> {
            this.consumer.accept(false, null);
            return true;
        };

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-cancel"));

        return new PanelItemBuilder().icon(icon).name(name).description(description).clickHandler(clickHandler).build();
    }


    // ---------------------------------------------------------------------
    // Section: Mode
    // ---------------------------------------------------------------------


    public enum Mode {
        ANY
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * List with elements that will be displayed in current GUI.
     */
    private final List<Advancement> elements;

    /**
     * This variable stores consumer.
     */
    private final BiConsumer<Boolean, Advancement> consumer;

    /**
     * Stores filtered items.
     */
    private List<Advancement> filterElements;
}
