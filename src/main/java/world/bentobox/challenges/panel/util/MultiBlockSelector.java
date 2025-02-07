package world.bentobox.challenges.panel.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.Utils;

/**
 * This class provides a multi-selector GUI for selecting blocks (i.e. Materials).
 * It extends the unified multi-selector base class and provides the type-specific
 * implementations required for block selection.
 */
public class MultiBlockSelector extends UnifiedMultiSelector<Material> {


    private final Set<Material> excluded;

    /**
     * Private constructor.
     *
     * @param user     the user opening the selector
     * @param mode     the mode indicating whether to show only blocks, only items, or any
     * @param excluded a set of Materials to exclude from the list
     * @param consumer the callback to be invoked when the user confirms or cancels
     */
    private MultiBlockSelector(User user, Mode mode, Set<Material> excluded,
            BiConsumer<Boolean, Collection<Material>> consumer) {
        super(user, mode, consumer);

        if (excluded == null) {
            excluded = new HashSet<>();
        }
        this.excluded = excluded;
        // Add default exclusions
        this.excluded.add(Material.AIR);
        this.excluded.add(Material.CAVE_AIR);
        this.excluded.add(Material.VOID_AIR);
        this.excluded.add(Material.PISTON_HEAD);
        this.excluded.add(Material.MOVING_PISTON);
        this.excluded.add(Material.BARRIER);
    }

    /**
     * Opens the MultiBlockSelector GUI with a specified mode and exclusions.
     *
     * @param user     the user who opens the GUI
     * @param mode     the mode for filtering (BLOCKS, ITEMS, or ANY)
     * @param excluded a set of Materials to exclude
     * @param consumer a callback to receive the result
     */
    public static void open(User user, Mode mode, Set<Material> excluded,
            BiConsumer<Boolean, Collection<Material>> consumer) {
        new MultiBlockSelector(user, mode, excluded, consumer).build();
    }

    /**
     * Opens the MultiBlockSelector GUI with default mode (ANY) and no exclusions.
     *
     * @param user     the user who opens the GUI
     * @param consumer a callback to receive the result
     */
    public static void open(User user, BiConsumer<Boolean, Collection<Material>> consumer) {
        new MultiBlockSelector(user, Mode.ANY, new HashSet<>(), consumer).build();
    }

    @Override
    protected List<Material> getElements() {
        return Arrays.stream(Material.values()).filter(material -> excluded == null || !excluded.contains(material))
                .filter(material -> {
            switch (mode) {
            case BLOCKS:
                return material.isBlock();
            case ITEMS:
                return material.isItem();
            default:
                return true;
            }
        }).sorted(Comparator.comparing(Material::name)).collect(Collectors.toList());
    }

    @Override
    protected String getTitleKey() {
        return "block-selector";
    }

    @Override
    protected String getElementKeyPrefix() {
        return "material.";
    }

    @Override
    protected ItemStack getIcon(Material element) {
        return PanelUtils.getMaterialItem(element);
    }

    @Override
    protected String getElementDisplayName(Material element) {
        return Utils.prettifyObject(element, this.user);
    }

    @Override
    protected String elementToString(Material element) {
        return element.name();
    }
}
