package world.bentobox.challenges.panel.util;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Tag;

import world.bentobox.bentobox.api.user.User;

public class MultiMaterialTagsSelector extends MultiTagsSelector<Material> {

    public static final Map<Tag<Material>, Material> ICONS = Map.of(
        Tag.AIR, Material.BARRIER,
        Tag.FIRE, Material.TORCH,
        Tag.CANDLE_CAKES, Material.CAKE,
        Tag.PORTALS, Material.MAGENTA_STAINED_GLASS_PANE,
        Tag.WALL_HANGING_SIGNS, Material.ACACIA_SIGN,
        Tag.WALL_SIGNS, Material.OAK_SIGN,
        Tag.WALL_CORALS, Material.BUBBLE_CORAL_FAN
        // ... add other mappings as needed
    );

    public enum Mode {
        BLOCKS, ITEMS, ANY
    }

    private MultiMaterialTagsSelector(User user, Mode mode, Set<Tag<Material>> excluded,
                                      java.util.function.BiConsumer<Boolean, java.util.Collection<Tag<Material>>> consumer) {
        super(user, excluded, consumer);
        // Future, use the mode to perform additional filtering.
    }

    public static void open(User user, Mode mode, Set<Tag<Material>> excluded,
                            java.util.function.BiConsumer<Boolean, java.util.Collection<Tag<Material>>> consumer) {
        new MultiMaterialTagsSelector(user, mode, excluded, consumer).build();
    }

    public static void open(User user,
                            java.util.function.BiConsumer<Boolean, java.util.Collection<Tag<Material>>> consumer) {
        open(user, Mode.ANY, new HashSet<>(), consumer);
    }

    @Override
    protected Iterable<Tag<Material>> getTags() {
        return Bukkit.getTags("blocks", Material.class);
    }

    @Override
    protected void removeIrrelevantTags() {
        // Remove irrelevant tags
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("SPAWNABLE"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("PLACE"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("TEMPT"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("_ON"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("BASE"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("SOUND_BLOCKS"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("DRAGON"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("VALID"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("INCORRECT"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("INFINIBURN"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("MINEABLE"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("TOOL"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("SNIFFER"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("OVERRIDE"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("OVERWORLD"));
        elements.remove(Tag.BLOCKS_WIND_CHARGE_EXPLOSIONS);
        elements.remove(Tag.CONVERTABLE_TO_MUD);
        elements.remove(Tag.DAMPENS_VIBRATIONS);
        elements.remove(Tag.DOES_NOT_BLOCK_HOPPERS);
        elements.remove(Tag.ENCHANTMENT_POWER_PROVIDER);
        elements.remove(Tag.ENCHANTMENT_POWER_TRANSMITTER);
        elements.remove(Tag.ENDERMAN_HOLDABLE);
        elements.remove(Tag.FEATURES_CANNOT_REPLACE);
        elements.remove(Tag.FALL_DAMAGE_RESETTING);
        elements.remove(Tag.FROG_PREFER_JUMP_TO);
        elements.remove(Tag.MAINTAINS_FARMLAND);
        elements.remove(Tag.MANGROVE_LOGS_CAN_GROW_THROUGH);
        elements.remove(Tag.MANGROVE_ROOTS_CAN_GROW_THROUGH);
        elements.remove(Tag.BEE_GROWABLES);
        elements.remove(Tag.MOB_INTERACTABLE_DOORS);
        elements.remove(Tag.HOGLIN_REPELLENTS);
        elements.remove(Tag.PIGLIN_REPELLENTS);
        elements.remove(Tag.SNAPS_GOAT_HORN);
        elements.remove(Tag.SOUL_SPEED_BLOCKS);
        elements.remove(Tag.STRIDER_WARM_BLOCKS);
        elements.remove(Tag.SWORD_EFFICIENT);
        elements.remove(Tag.UNSTABLE_BOTTOM_CENTER);
        elements.remove(Tag.COMPLETES_FIND_TREE_TUTORIAL);
        elements.remove(Tag.GUARDED_BY_PIGLINS);
        elements.remove(Tag.IMPERMEABLE);
        elements.remove(Tag.PREVENT_MOB_SPAWNING_INSIDE);
        elements.remove(Tag.SMELTS_TO_GLASS);
        elements.remove(Tag.WITHER_IMMUNE);
    }

    @Override
    protected String getTitleKey() {
        return "block-selector";
    }

    @Override
    protected String getElementGroupKey() {
        return "block-group.";
    }

    @Override
    protected Material getIconForTag(Tag<Material> tag) {
        return ICONS.getOrDefault(tag, Registry.MATERIAL.stream().filter(tag::isTagged)
                .filter(Material::isItem).findAny().orElse(Material.PAPER));
    }
}

