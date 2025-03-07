package world.bentobox.challenges.panel.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.Utils;

/**
 * This class provides a multi-selector GUI for selecting entities.
 * It extends the unified multi-selector base class and supplies the
 * type-specific implementations required for entity selection.
 */
public class MultiEntitySelector extends UnifiedMultiSelector<EntityType> {

    private final Set<EntityType> excluded;

    /**
     * Private constructor.
     *
     * @param user     the user opening the selector
     * @param mode     determines whether to show only living entities (ALIVE) or all (ANY)
     * @param excluded a set of EntityType values to exclude
     * @param consumer the callback to be invoked when the user confirms or cancels
     */
    private MultiEntitySelector(User user, Mode mode, Set<EntityType> excluded,
            java.util.function.BiConsumer<Boolean, Collection<EntityType>> consumer) {
        super(user, mode, consumer);
        this.excluded = excluded;
    }

    /**
     * Opens the MultiEntitySelector GUI with the specified parameters.
     *
     * @param user     the user who opens the GUI
     * @param mode     the filtering mode (ALIVE or ANY)
     * @param excluded a set of EntityType values to exclude from the list
     * @param consumer a callback to receive the result
     */
    public static void open(User user, Mode mode, Set<EntityType> excluded,
            java.util.function.BiConsumer<Boolean, Collection<EntityType>> consumer) {
        new MultiEntitySelector(user, mode, excluded, consumer).build();
    }

    /**
     * Opens the MultiEntitySelector GUI with default parameters (mode ANY and no exclusions).
     *
     * @param user     the user who opens the GUI
     * @param consumer a callback to receive the result
     */
    public static void open(User user,
            java.util.function.BiConsumer<Boolean, Collection<EntityType>> consumer) {
        new MultiEntitySelector(user, Mode.ANY, new HashSet<>(), consumer).build();
    }

    /**
     * Returns the list of EntityType values to display, applying the specified exclusions and mode.
     */
    @Override
    protected List<EntityType> getElements() {
        return Arrays.stream(EntityType.values()).filter(entity -> excluded == null || !excluded.contains(entity))
                .filter(entity -> !SingleEntitySelector.NON_ENTITIES.contains(entity))
                .sorted(Comparator.comparing(EntityType::name)).collect(Collectors.toList());
    }

    /**
     * Returns the title key used to form the GUI title.
     */
    @Override
    protected String getTitleKey() {
        return "entity-selector";
    }

    /**
     * Returns the translation key prefix for element buttons.
     */
    @Override
    protected String getElementKeyPrefix() {
        return "entity.";
    }

    /**
     * Returns the icon for the given EntityType.
     * If asEgg is true, an entity spawn egg is returned; otherwise, the entity head is returned.
     */
    @Override
    protected ItemStack getIcon(EntityType element) {
        return SingleEntitySelector.getIcon(element);
    }

    /**
     * Returns the display name for the given EntityType.
     */
    @Override
    protected String getElementDisplayName(EntityType element) {
        return Utils.prettifyObject(element, this.user);
    }

    /**
     * Returns a string representation of the given EntityType used for filtering.
     */
    @Override
    protected String elementToString(EntityType element) {
        return element.name();
    }

    @Override
    protected String getElementPlaceholder() {
        return "[entity]";
    }
}
