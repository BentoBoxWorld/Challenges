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

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;

/**
 * @author tastybento
 */
public abstract class MultiTagsSelector<T extends Keyed> extends PagedSelector<Tag<T>> {

    // Buttons common to both selectors.
    protected enum Button {
        ACCEPT_SELECTED, CANCEL
    }

    // Common fields.
    protected final List<Tag<T>> elements = new ArrayList<>();
    protected final Set<Tag<T>> selectedElements;
    protected final BiConsumer<Boolean, Collection<Tag<T>>> consumer;
    protected List<Tag<T>> filterElements;

    protected MultiTagsSelector(User user, Set<Tag<T>> excluded, BiConsumer<Boolean, Collection<Tag<T>>> consumer) {
        super(user);
        this.consumer = consumer;
        this.selectedElements = new HashSet<>();
        // Fill elements using the type‐specific method.
        for (Tag<T> tag : getTags()) {
            elements.add(tag);
        }
        elements.sort(Comparator.comparing(tag -> tag.getKey().getKey()));
        // Remove irrelevant tags (type‐specific)
        removeIrrelevantTags();
        // Remove any tags passed in as excluded.
        excluded.forEach(excludedTag -> elements.removeIf(tag -> tag.equals(excludedTag)));
        // Initially no filter is applied.
        this.filterElements = elements;
    }

    // ABSTRACT METHODS TO BE IMPLEMENTED BY SUBCLASSES:

    /** Return the tags from Bukkit (for example, Bukkit.getTags("blocks", Material.class)). */
    protected abstract Iterable<Tag<T>> getTags();

    /** Remove tags that are not needed (e.g. by checking the key string). */
    protected abstract void removeIrrelevantTags();

    /** Return the translation key used for the panel title. For example, "block-selector" or "entity-selector". */
    protected abstract String getTitleKey();

    /** Return the translation key prefix for element buttons (e.g. "block-group." or "entity-group."). */
    protected abstract String getElementGroupKey();

    /** Return the icon for the given tag. */
    protected abstract Material getIconForTag(Tag<T> tag);

    // COMMON METHODS:

    @Override
    protected void build() {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user);
        panelBuilder.name(this.user.getTranslation(Constants.TITLE + getTitleKey()));

        PanelUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);
        this.populateElements(panelBuilder, this.filterElements);

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
                    .filter(tag -> tag.getKey().getKey().toLowerCase(Locale.ENGLISH)
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
                this.selectedElements.forEach(tag -> description.add(this.user.getTranslation(reference + "element",
                        "[element]", Utils.prettifyObject(tag, this.user))));
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
    protected PanelItem createElementButton(Tag<T> tag) {
        final String reference = Constants.BUTTON + getElementGroupKey();
        List<String> description = new ArrayList<>();
        description.add(this.user.getTranslation(reference + "description", "[id]", tag.getKey().getKey()));

        if (this.selectedElements.contains(tag)) {
            description.add(this.user.getTranslation(reference + "selected"));
            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-deselect"));
        } else {
            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-select"));
        }

        return new PanelItemBuilder()
                .name(this.user.getTranslation(reference + "name", "[tag]", Utils.prettifyObject(tag, this.user)))
                .icon(getIconForTag(tag)).description(description).clickHandler((panel, user1, clickType, slot) -> {
                    // Toggle selection.
                    if (!this.selectedElements.add(tag)) {
                        this.selectedElements.remove(tag);
                    }
                    this.build();
                    return true;
                }).glow(this.selectedElements.contains(tag)).build();
    }
}
