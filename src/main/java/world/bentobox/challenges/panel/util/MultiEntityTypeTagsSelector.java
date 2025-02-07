package world.bentobox.challenges.panel.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.Utils;

/**
 * This class provides a multi-selector GUI for selecting entity-type tags.
 * It extends the unified multi-selector base class (UnifiedMultiSelector) and supplies
 * all tag-specific implementations such as retrieving the tag list, filtering unwanted tags,
 * and choosing an appropriate icon.
 *
 * @see UnifiedMultiSelector
 */
public class MultiEntityTypeTagsSelector extends UnifiedMultiSelector<Tag<EntityType>> {

    private final Mode mode;
    private final Set<Tag<EntityType>> excluded;

    /**
     * Defines filtering modes.
     */
    public enum Mode {
        ENTITY_TYPE, ANY
    }

    /**
     * Private constructor.
     *
     * @param user     the user opening the selector
     * @param mode     the mode (ENTITY_TYPE or ANY) that might influence filtering behavior
     * @param excluded a set of tags to be excluded from display
     * @param consumer a callback to receive the selected tags (or cancellation)
     */
    private MultiEntityTypeTagsSelector(User user, Mode mode, Set<Tag<EntityType>> excluded,
            BiConsumer<Boolean, java.util.Collection<Tag<EntityType>>> consumer) {
        super(user, consumer);
        this.mode = mode; // This is not currently used
        this.excluded = excluded;
    }

    /**
     * Opens the entity-type tag selector GUI with the specified mode and exclusions.
     *
     * @param user     the user who opens the GUI
     * @param mode     filtering mode (ENTITY_TYPE or ANY)
     * @param excluded a set of tags to exclude
     * @param consumer a callback to receive the result
     */
    public static void open(User user, Mode mode, Set<Tag<EntityType>> excluded,
            BiConsumer<Boolean, java.util.Collection<Tag<EntityType>>> consumer) {
        new MultiEntityTypeTagsSelector(user, mode, excluded, consumer).build();
    }

    /**
     * Opens the entity-type tag selector GUI with default parameters (mode ANY and no exclusions).
     *
     * @param user     the user who opens the GUI
     * @param consumer a callback to receive the result
     */
    public static void open(User user,
            BiConsumer<Boolean, java.util.Collection<Tag<EntityType>>> consumer) {
        new MultiEntityTypeTagsSelector(user, Mode.ANY, new HashSet<>(), consumer).build();
    }

    /**
     * Retrieves the list of available entity-type tags.
     * <p>
     * This method uses Bukkit’s tag API to get all tags for "entity_types" (of type EntityType),
     * sorts them by their key, then removes any that are deemed irrelevant based on their key name
     * (for example, tags containing "AXOLOTL", "IMMUNE", etc.) and any tags specified in the excluded set.
     * </p>
     *
     * @return a sorted and filtered list of Tag&lt;EntityType&gt;
     */
    @Override
    protected List<Tag<EntityType>> getElements() {
        List<Tag<EntityType>> tagList = new ArrayList<>();
        Iterable<Tag<EntityType>> iterable = Bukkit.getTags("entity_types", EntityType.class);
        iterable.forEach(tagList::add);
        tagList.sort(Comparator.comparing(tag -> tag.getKey().getKey()));

        // Remove irrelevant tags based on key contents.
        tagList.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("AXOLOTL"));
        tagList.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("IMMUNE"));
        tagList.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("IGNORES"));
        tagList.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("FRIEND"));
        tagList.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("SENSITIVE"));
        tagList.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("PROJECTILE"));

        // Remove specific known tags.
        tagList.remove(org.bukkit.Tag.ENTITY_TYPES_ARROWS);
        tagList.remove(org.bukkit.Tag.ENTITY_TYPES_BEEHIVE_INHABITORS);
        tagList.remove(org.bukkit.Tag.ENTITY_TYPES_CAN_TURN_IN_BOATS);
        tagList.remove(org.bukkit.Tag.ENTITY_TYPES_DISMOUNTS_UNDERWATER);
        tagList.remove(org.bukkit.Tag.ENTITY_TYPES_FALL_DAMAGE_IMMUNE);
        tagList.remove(org.bukkit.Tag.ENTITY_TYPES_FREEZE_HURTS_EXTRA_TYPES);
        tagList.remove(org.bukkit.Tag.ENTITY_TYPES_INVERTED_HEALING_AND_HARM);
        tagList.remove(org.bukkit.Tag.ENTITY_TYPES_NO_ANGER_FROM_WIND_CHARGE);
        tagList.remove(org.bukkit.Tag.ENTITY_TYPES_NON_CONTROLLING_RIDER);
        tagList.remove(org.bukkit.Tag.ENTITY_TYPES_NOT_SCARY_FOR_PUFFERFISH);
        tagList.remove(org.bukkit.Tag.ENTITY_TYPES_FROG_FOOD);

        // Remove any tags specified in the excluded set.
        if (excluded != null) {
            for (Tag<EntityType> ex : excluded) {
                tagList.removeIf(tag -> tag.equals(ex));
            }
        }
        return tagList;
    }

    /**
     * Returns the title key used to build the GUI title.
     *
     * @return "entity-selector"
     */
    @Override
    protected String getTitleKey() {
        return "entity-selector";
    }

    /**
     * Returns the translation key prefix for individual element buttons.
     *
     * @return "entity-group."
     */
    @Override
    protected String getElementKeyPrefix() {
        return "entity-group.";
    }

    /**
     * Returns the icon for the given entity-type tag.
     * <p>
     * If the tag’s key contains "boat", an oak boat icon is used. Otherwise, the method attempts
     * to find any EntityType that is tagged by this tag and constructs a spawn egg material name
     * (e.g. "CREEPER_SPAWN_EGG"). If no matching material is found, a PAPER icon is returned.
     * </p>
     *
     * @param element the Tag&lt;EntityType&gt; for which to determine the icon
     * @return an ItemStack representing the icon
     */
    @Override
    protected ItemStack getIcon(Tag<EntityType> element) {
        Material iconMaterial;
        if (element.getKey().getKey().contains("boat")) {
            iconMaterial = Material.OAK_BOAT;
        } else {
            EntityType entType = Registry.ENTITY_TYPE.stream().filter(element::isTagged).findAny().orElse(null);
            if (entType != null) {
                String eggName = entType.getKey().getKey().toUpperCase(Locale.ENGLISH) + "_SPAWN_EGG";
                try {
                    iconMaterial = Material.valueOf(eggName);
                } catch (Exception e) {
                    iconMaterial = Material.PAPER;
                }
            } else {
                iconMaterial = Material.PAPER;
            }
        }
        return new ItemStack(iconMaterial);
    }

    /**
     * Returns the display name for the given tag.
     *
     * @param element the Tag&lt;EntityType&gt;
     * @return a pretty-printed string for display
     */
    @Override
    protected String getElementDisplayName(Tag<EntityType> element) {
        return Utils.prettifyObject(element, this.user);
    }

    /**
     * Returns a string representation of the tag used for filtering.
     *
     * @param element the Tag&lt;EntityType&gt;
     * @return the tag’s key (as a string)
     */
    @Override
    protected String elementToString(Tag<EntityType> element) {
        return element.getKey().getKey();
    }
}
