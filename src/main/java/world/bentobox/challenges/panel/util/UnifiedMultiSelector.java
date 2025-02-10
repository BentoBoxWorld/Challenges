package world.bentobox.challenges.panel.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.Constants;

/**
 * Unified abstract class for multi‐selector GUIs.
 * <p>
 * This class provides the common logic for building the GUI, filtering the list,
 * and creating the functional buttons. Subclasses must supply the list of available elements
 * and type‐specific details such as how to obtain an element’s display name, icon, and
 * string representation for filtering.
 * </p>
 *
 * @param <T> The type of element shown in the GUI (e.g. Material, EntityType, or Tag&lt;Material&gt;, etc.)
 */
public abstract class UnifiedMultiSelector<T> extends PagedSelector<T> {

    protected final Mode mode;

    public enum Mode {
        ALIVE, BLOCKS, ITEMS, ANY, ENTITY_TYPE
    }

    protected final List<T> elements;
    protected final Set<T> selectedElements;
    protected final BiConsumer<Boolean, Collection<T>> consumer;
    protected List<T> filterElements;

    protected UnifiedMultiSelector(User user, Mode mode, BiConsumer<Boolean, Collection<T>> consumer) {
        this(user, mode, null, consumer);
    }

    protected UnifiedMultiSelector(User user, Mode mode, List<T> elements,
            BiConsumer<Boolean, Collection<T>> consumer) {
        super(user);
        this.mode = mode;
        this.consumer = consumer;
        this.selectedElements = new HashSet<>();
        this.elements = (elements != null) ? elements : getElements(); // Use provided elements or get them from subclass
        this.elements.sort(Comparator.comparing(this::elementToString));
        this.filterElements = this.elements;
    }


    /**
     * Subclasses must return the complete list of available elements.
     */
    protected abstract List<T> getElements();

    /**
     * Returns the title key (to be appended to Constants.TITLE)
     * for this selector (for example, "entity-selector" or "block-selector").
     */
    protected abstract String getTitleKey();

    /**
     * Returns the translation key prefix used for element buttons
     * (for example, "entity." or "material.").
     */
    protected abstract String getElementKeyPrefix();

    /**
     * Returns the placeholder used for element buttons
     * (for example, "[entity]", or "[material]").
     */
    protected abstract String getElementPlaceholder();

    /**
     * Returns the icon for the given element.
     */
    protected abstract ItemStack getIcon(T element);

    /**
     * Returns the display name for the given element.
     * (For instance, by calling Utils.prettifyObject(element, user)).
     */
    protected abstract String getElementDisplayName(T element);

    /**
     * Returns a string representation of the element used for filtering.
     * (For enums you might simply return element.name().)
     */
    protected abstract String elementToString(T element);

    @Override
    protected void build() {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user);
        panelBuilder.name(this.user.getTranslation(Constants.TITLE + getTitleKey()));

        PanelUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

        // Populate the GUI with the filtered list.
        this.populateElements(panelBuilder, this.filterElements);

        // Add functional buttons.
        panelBuilder.item(3, createButton(Button.ACCEPT_SELECTED));
        panelBuilder.item(5, createButton(Button.CANCEL));

        panelBuilder.build();
    }

    @Override
    protected void updateFilters() {
        if (this.searchString == null || this.searchString.isBlank()) {
            this.filterElements = this.elements;
        } else {
            this.filterElements = this.elements.stream()
                    .filter(element -> elementToString(element).toLowerCase(Locale.ENGLISH)
                            .contains(this.searchString.toLowerCase(Locale.ENGLISH)))
                    .distinct().collect(Collectors.toList());
        }
    }

    private PanelItem createButton(Button button) {
        final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";
        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>();
        description.add(this.user.getTranslation(reference + "description"));

        ItemStack icon;
        PanelItem.ClickHandler clickHandler;

        switch (button) {
        case ACCEPT_SELECTED -> {
            if (!this.selectedElements.isEmpty()) {
                description.add(this.user.getTranslation(reference + "title"));
                for (T element : this.selectedElements) {
                    description.add(this.user.getTranslation(reference + "element", "[element]",
                            getElementDisplayName(element)));
                }
            }
            icon = new ItemStack(Material.COMMAND_BLOCK);
            clickHandler = (panel, user1, clickType, slot) -> {
                this.consumer.accept(true, this.selectedElements);
                return true;
            };
            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-save"));
        }
        case CANCEL -> {
            icon = new ItemStack(Material.IRON_DOOR);
            clickHandler = (panel, user1, clickType, slot) -> {
                this.consumer.accept(false, null);
                return true;
            };
            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-cancel"));
        }
        default -> {
            icon = new ItemStack(Material.PAPER);
            clickHandler = null;
        }
        }

        return new PanelItemBuilder().icon(icon).name(name).description(description).clickHandler(clickHandler).build();
    }

    @Override
    protected PanelItem createElementButton(T element) {
        final String reference = Constants.BUTTON + getElementKeyPrefix();
        List<String> description = new ArrayList<>();
        description.add(this.user.getTranslation(reference + "description", "[id]", elementToString(element)));

        if (this.selectedElements.contains(element)) {
            description.add(this.user.getTranslation(reference + "selected"));
            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-deselect"));
        } else {
            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-select"));
        }
        return new PanelItemBuilder()
                .name(this.user.getTranslation(reference + "name", getElementPlaceholder(),
                        getElementDisplayName(element)))
                .icon(getIcon(element)).description(description).clickHandler((panel, user1, clickType, slot) -> {
                    // Toggle the selection state.
                    if (!this.selectedElements.add(element)) {
                        this.selectedElements.remove(element);
                    }
                    this.build();
                    return true;
                }).glow(this.selectedElements.contains(element)).build();
    }

    protected enum Button {
        ACCEPT_SELECTED, CANCEL
    }
}
