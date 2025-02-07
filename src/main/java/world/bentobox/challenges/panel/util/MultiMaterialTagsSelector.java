package world.bentobox.challenges.panel.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.Utils;

/**
 * This class provides a multi-selector GUI for selecting material tags.
 * It extends the unified multi-selector base class and supplies the tag‑specific
 * implementations such as retrieving the tag list, filtering unwanted tags,
 * selecting an appropriate icon, and generating display names.
 *
 * <p>
 * Two static open methods are provided: one that accepts a filtering mode and an excluded set,
 * and one that uses default parameters.
 * </p>
 *
 * @see UnifiedMultiSelector
 */
public class MultiMaterialTagsSelector extends UnifiedMultiSelector<Tag<Material>> {

    /**
     * A map of specific tags to custom icon materials.
     */
    public static final Map<Tag<Material>, Material> ICONS = Map.of(
            Tag.AIR, Material.BARRIER, Tag.FIRE, Material.TORCH, Tag.CANDLE_CAKES, Material.CAKE, Tag.PORTALS,
            Material.MAGENTA_STAINED_GLASS_PANE, Tag.WALL_HANGING_SIGNS, Material.ACACIA_SIGN, Tag.WALL_SIGNS,
            Material.OAK_SIGN,
            Tag.WALL_CORALS, Material.BUBBLE_CORAL_FAN, Tag.CAVE_VINES, Material.VINE
    );

    private final Mode mode;
    private final Set<Tag<Material>> excluded;

    /**
     * Modes for filtering material tags.
     */
    public enum Mode {
        BLOCKS, ITEMS, ANY
    }

    /**
     * Private constructor.
     *
     * @param user     the user opening the selector
     * @param mode     filtering mode (BLOCKS, ITEMS, or ANY)
     * @param excluded a set of tags to exclude from display
     * @param consumer the callback to receive the selected tags or cancellation
     */
    private MultiMaterialTagsSelector(User user, Mode mode, Set<Tag<Material>> excluded,
            BiConsumer<Boolean, Collection<Tag<Material>>> consumer) {
        super(user, consumer);
        this.mode = mode; // Not currently used
        this.excluded = excluded;
    }

    /**
     * Opens the material tag selector GUI with a specified mode and exclusions.
     *
     * @param user     the user who opens the GUI
     * @param mode     the filtering mode (BLOCKS, ITEMS, or ANY)
     * @param excluded a set of tags to exclude
     * @param consumer a callback to receive the result
     */
    public static void open(User user, Mode mode, Set<Tag<Material>> excluded,
            BiConsumer<Boolean, Collection<Tag<Material>>> consumer) {
        new MultiMaterialTagsSelector(user, mode, excluded, consumer).build();
    }

    /**
     * Opens the material tag selector GUI with default parameters (mode ANY and no exclusions).
     *
     * @param user     the user who opens the GUI
     * @param consumer a callback to receive the result
     */
    public static void open(User user, BiConsumer<Boolean, Collection<Tag<Material>>> consumer) {
        new MultiMaterialTagsSelector(user, Mode.ANY, new HashSet<>(), consumer).build();
    }

    /**
     * Retrieves the list of available material tags.
     *
     * <p>
     * This method obtains tags using Bukkit’s tag API for the "blocks" category,
     * sorts them by their key name, applies several removeIf filters to eliminate irrelevant tags,
     * and then removes any tags provided in the excluded set.
     * </p>
     *
     * @return a sorted and filtered list of Tag&lt;Material&gt;
     */
    @Override
    protected List<Tag<Material>> getElements() {
        List<Tag<Material>> list = new ArrayList<>();
        Iterable<Tag<Material>> iterable = Bukkit.getTags("blocks", Material.class);
        iterable.forEach(list::add);
        list.sort(Comparator.comparing(tag -> tag.getKey().getKey()));

        // Remove irrelevant tags based on their key.
        list.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("SPAWNABLE"));
        list.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("PLACE"));
        list.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("TEMPT"));
        list.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("_ON"));
        list.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("BASE"));
        list.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("SOUND_BLOCKS"));
        list.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("DRAGON"));
        list.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("VALID"));
        list.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("INCORRECT"));
        list.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("INFINIBURN"));
        list.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("MINEABLE"));
        list.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("TOOL"));
        list.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("SNIFFER"));
        list.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("OVERRIDE"));
        list.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("OVERWORLD"));

        // Remove specific known tags.
        list.remove(Tag.BLOCKS_WIND_CHARGE_EXPLOSIONS);
        list.remove(Tag.CONVERTABLE_TO_MUD);
        list.remove(Tag.DAMPENS_VIBRATIONS);
        list.remove(Tag.DOES_NOT_BLOCK_HOPPERS);
        list.remove(Tag.ENCHANTMENT_POWER_PROVIDER);
        list.remove(Tag.ENCHANTMENT_POWER_TRANSMITTER);
        list.remove(Tag.ENDERMAN_HOLDABLE);
        list.remove(Tag.FEATURES_CANNOT_REPLACE);
        list.remove(Tag.FALL_DAMAGE_RESETTING);
        list.remove(Tag.FROG_PREFER_JUMP_TO);
        list.remove(Tag.MAINTAINS_FARMLAND);
        list.remove(Tag.MANGROVE_LOGS_CAN_GROW_THROUGH);
        list.remove(Tag.MANGROVE_ROOTS_CAN_GROW_THROUGH);
        list.remove(Tag.BEE_GROWABLES);
        list.remove(Tag.MOB_INTERACTABLE_DOORS);
        list.remove(Tag.HOGLIN_REPELLENTS);
        list.remove(Tag.PIGLIN_REPELLENTS);
        list.remove(Tag.SNAPS_GOAT_HORN);
        list.remove(Tag.SOUL_SPEED_BLOCKS);
        list.remove(Tag.STRIDER_WARM_BLOCKS);
        list.remove(Tag.SWORD_EFFICIENT);
        list.remove(Tag.UNSTABLE_BOTTOM_CENTER);
        list.remove(Tag.COMPLETES_FIND_TREE_TUTORIAL);
        list.remove(Tag.GUARDED_BY_PIGLINS);
        list.remove(Tag.IMPERMEABLE);
        list.remove(Tag.PREVENT_MOB_SPAWNING_INSIDE);
        list.remove(Tag.SMELTS_TO_GLASS);
        list.remove(Tag.WITHER_IMMUNE);

        // Remove any tags specified in the excluded set.
        if (excluded != null) {
            for (Tag<Material> ex : excluded) {
                list.removeIf(tag -> tag.equals(ex));
            }
        }
        return list;
    }

    /**
     * Returns the title key used for the GUI.
     *
     * @return "block-selector"
     */
    @Override
    protected String getTitleKey() {
        return "block-selector";
    }

    /**
     * Returns the translation key prefix for individual element buttons.
     *
     * @return "block-group."
     */
    @Override
    protected String getElementKeyPrefix() {
        return "block-group.";
    }

    /**
     * Returns the icon for the given material tag.
     *
     * <p>
     * This method first checks the ICONS map; if a mapping exists for the tag, that material is used.
     * Otherwise, it searches through the Bukkit material registry for any material tagged by the given tag
     * that is also an item. If none is found, it falls back to PAPER.
     * </p>
     *
     * @param element the Tag&lt;Material&gt; for which to determine the icon
     * @return an ItemStack representing the icon
     */
    @Override
    protected ItemStack getIcon(Tag<Material> element) {
        Material iconMaterial = ICONS.getOrDefault(element, Registry.MATERIAL.stream().filter(element::isTagged)
                .filter(Material::isItem).findAny().orElse(Material.PAPER));
        return new ItemStack(iconMaterial);
    }

    /**
     * Returns the display name for the given material tag.
     *
     * @param element the Tag&lt;Material&gt;
     * @return a pretty-printed string for display
     */
    @Override
    protected String getElementDisplayName(Tag<Material> element) {
        return Utils.prettifyObject(element, this.user);
    }

    /**
     * Returns a string representation of the tag used for filtering.
     *
     * @param element the Tag&lt;Material&gt;
     * @return the tag's key (as a string)
     */
    @Override
    protected String elementToString(Tag<Material> element) {
        return element.getKey().getKey();
    }
}
