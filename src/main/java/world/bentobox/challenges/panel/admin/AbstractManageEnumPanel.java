package world.bentobox.challenges.panel.admin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.inventory.ItemStack;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.challenges.panel.CommonPagedPanel;
import world.bentobox.challenges.panel.CommonPanel;
import world.bentobox.challenges.panel.ConversationUtils;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;

/**
 * Abstract class that works with any enum type.
 * This class contains all the common logic: filtering, building the panel, creating the element buttons, etc.
 * @author tastybento
 */
public abstract class AbstractManageEnumPanel<T extends Enum<T>> extends CommonPagedPanel<T> {

    protected final Map<T, Integer> itemsMap;
    protected final List<T> itemList;
    protected final Set<T> selectedItems;
    protected List<T> filterElements;

    protected AbstractManageEnumPanel(CommonPanel parentGUI, Map<T, Integer> itemsMap) {
        super(parentGUI);
        this.itemsMap = itemsMap;
        this.itemList = new ArrayList<>(itemsMap.keySet());
        // Sort by the enum name (alphabetical order)
        this.itemList.sort(Comparator.comparing(Enum::name));
        this.selectedItems = new HashSet<>();
        this.filterElements = this.itemList;
    }

    /**
     * Update the filter list based on the search string.
     */
    @Override
    protected void updateFilters() {
        if (this.searchString == null || this.searchString.isBlank()) {
            this.filterElements = this.itemList;
        } else {
            this.filterElements = this.itemList.stream()
                    .filter(element -> element.name().toLowerCase().contains(this.searchString.toLowerCase()))
                    .distinct().collect(Collectors.toList());
        }
    }

    /**
     * Creates a button for an element.
     */
    @Override
    protected PanelItem createElementButton(T element) {
        final String reference = getElementTranslationPrefix();
        List<String> description = new ArrayList<>();

        if (selectedItems.contains(element)) {
            description.add(this.user.getTranslation(reference + "selected"));
        }

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-choose"));

        if (selectedItems.contains(element)) {
            description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-deselect"));
        } else {
            description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-select"));
        }

        return new PanelItemBuilder()
                .name(this.user.getTranslation(reference + "name", getElementPlaceholder(),
                        Utils.prettifyObject(element, this.user)))
                .icon(getElementIcon(element, itemsMap.get(element))).description(description)
                .clickHandler((panel, user1, clickType, slot) -> {
                    // On right click, toggle selection.
                    if (clickType.isRightClick()) {
                        if (!selectedItems.add(element)) {
                            selectedItems.remove(element);
                        }
                        this.build();
                    } else {
                        // On left click, open a numeric input conversation.
                        Consumer<Number> numberConsumer = number -> {
                            if (number != null) {
                                itemsMap.put(element, number.intValue());
                            }
                            this.build();
                        };

                        ConversationUtils.createNumericInput(numberConsumer, this.user,
                                this.user.getTranslation(Constants.CONVERSATIONS + "input-number"), 1,
                                Integer.MAX_VALUE);
                    }
                    return true;
                }).glow(selectedItems.contains(element)).build();
    }

    /**
     * Build the panel.
     */
    @Override
    protected void build() {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user)
                .name(this.user.getTranslation(getPanelTitleKey()));

        // Create a border.
        PanelUtils.fillBorder(panelBuilder);

        // Add the functional buttons (buttons like add, remove, etc.)
        addFunctionalButtons(panelBuilder);

        // Populate the panel with the filtered items.
        populateElements(panelBuilder, this.filterElements);

        // Add the return button.
        panelBuilder.item(getReturnButtonSlot(), this.returnButton);

        panelBuilder.build();
    }

    protected int getReturnButtonSlot() {
        return 44;
    }

    // --- Abstract methods that concrete subclasses must implement ---

    /**
     * Returns the ItemStack icon for a given element.
     */
    protected abstract ItemStack getElementIcon(T element, int count);

    /**
     * Returns the translation prefix for element buttons (e.g. "button.material." or "button.entity.").
     */
    protected abstract String getElementTranslationPrefix();

    /**
     * Returns the placeholder used in translation for the element (e.g. "[material]" or "[entity]").
     */
    protected abstract String getElementPlaceholder();

    /**
     * Returns the translation key for the panel title.
     */
    protected abstract String getPanelTitleKey();

    /**
     * Adds all the functional (non-element) buttons to the panel.
     */
    protected abstract void addFunctionalButtons(PanelBuilder panelBuilder);
}
