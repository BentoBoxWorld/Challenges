package world.bentobox.challenges.panel.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.Utils;

/**
 * A multi-selector GUI for choosing biomes. Extends the unified multi-selector base and
 * supplies the biome-specific details (element list, icons, display names).
 *
 * <p>Biomes are a registry-backed type in modern Minecraft, so they are handled by their
 * namespaced key rather than as an enum.
 */
public class MultiBiomeSelector extends UnifiedMultiSelector<Biome> {

    private final Set<Biome> excluded;

    private MultiBiomeSelector(User user, Set<Biome> excluded,
            BiConsumer<Boolean, Collection<Biome>> consumer) {
        super(user, Mode.ANY, consumer);
        this.excluded = excluded;
    }

    /**
     * Opens the biome selector.
     *
     * @param user     the user who opens the GUI.
     * @param excluded biomes to hide from the list (e.g. ones already selected).
     * @param consumer callback receiving the confirmation flag and the chosen biomes.
     */
    public static void open(User user, Set<Biome> excluded,
            BiConsumer<Boolean, Collection<Biome>> consumer) {
        new MultiBiomeSelector(user, excluded, consumer).build();
    }

    /**
     * Opens the biome selector with no exclusions.
     *
     * @param user     the user who opens the GUI.
     * @param consumer callback receiving the confirmation flag and the chosen biomes.
     */
    public static void open(User user, BiConsumer<Boolean, Collection<Biome>> consumer) {
        new MultiBiomeSelector(user, new HashSet<>(), consumer).build();
    }

    @Override
    protected List<Biome> getElements() {
        return StreamSupport.stream(Registry.BIOME.spliterator(), false)
                .filter(biome -> excluded == null || !excluded.contains(biome))
                .sorted(Comparator.comparing(MultiBiomeSelector::biomeKey))
                .collect(Collectors.toList());
    }

    @Override
    protected String getTitleKey() {
        return "biome-selector";
    }

    @Override
    protected String getElementKeyPrefix() {
        return "biome.";
    }

    @Override
    protected String getElementPlaceholder() {
        return "[biome]";
    }

    @Override
    protected ItemStack getIcon(Biome element) {
        return new ItemStack(iconMaterial(biomeKey(element)));
    }

    @Override
    protected String getElementDisplayName(Biome element) {
        return Utils.prettifyBiome(biomeKey(element));
    }

    @Override
    protected String elementToString(Biome element) {
        return biomeKey(element);
    }

    /**
     * Returns the namespaced key string (e.g. "minecraft:plains") for a biome.
     *
     * @param biome the biome.
     * @return its namespaced key.
     */
    public static String biomeKey(Biome biome) {
        return biome.getKey().toString();
    }

    /**
     * Picks a rough representative icon for a biome key. Biomes have no natural item, so this
     * is only a visual hint; the biome name in the button title is what identifies it.
     *
     * @param key the biome key.
     * @return a Material to use as the button icon.
     */
    private static Material iconMaterial(String key) {
        String path = key.contains(":") ? key.substring(key.indexOf(':') + 1) : key;

        if (path.contains("nether") || path.contains("basalt") || path.contains("soul") || path.contains("crimson")
                || path.contains("warped")) {
            return Material.NETHERRACK;
        }
        if (path.contains("end")) {
            return Material.END_STONE;
        }
        if (path.contains("ocean") || path.contains("river")) {
            return Material.WATER_BUCKET;
        }
        if (path.contains("desert") || path.contains("beach") || path.contains("badlands")) {
            return Material.SAND;
        }
        if (path.contains("snow") || path.contains("frozen") || path.contains("ice") || path.contains("cold")) {
            return Material.SNOW_BLOCK;
        }
        if (path.contains("cave") || path.contains("deep") || path.contains("lush")) {
            return Material.STONE;
        }
        if (path.contains("mushroom")) {
            return Material.RED_MUSHROOM_BLOCK;
        }

        return Material.GRASS_BLOCK;
    }
}
