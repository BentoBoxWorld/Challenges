package world.bentobox.challenges.panel.util;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;

import world.bentobox.bentobox.api.user.User;

public class MultiEntityTypeTagsSelector extends MultiTagsSelector<EntityType> {

    // The ICONS map is empty here, but you could add mappings if needed.
    public static final java.util.Map<Tag<EntityType>, Material> ICONS = java.util.Map.of();

    public enum Mode {
        ENTITY_TYPE, ANY
    }

    private MultiEntityTypeTagsSelector(User user, Mode mode, Set<Tag<EntityType>> excluded,
            java.util.function.BiConsumer<Boolean, java.util.Collection<Tag<EntityType>>> consumer) {
        super(user, excluded, consumer);
        // Add mode-specific behavior here if needed.
    }

    public static void open(User user, Mode mode, Set<Tag<EntityType>> excluded,
            java.util.function.BiConsumer<Boolean, java.util.Collection<Tag<EntityType>>> consumer) {
        new MultiEntityTypeTagsSelector(user, mode, excluded, consumer).build();
    }

    public static void open(User user,
            java.util.function.BiConsumer<Boolean, java.util.Collection<Tag<EntityType>>> consumer) {
        open(user, Mode.ANY, new HashSet<>(), consumer);
    }

    @Override
    protected Iterable<Tag<EntityType>> getTags() {
        return Bukkit.getTags("entity_types", EntityType.class);
    }

    @Override
    protected void removeIrrelevantTags() {
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("AXOLOTL"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("IMMUNE"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("IGNORES"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("FRIEND"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("SENSITIVE"));
        elements.removeIf(t -> t.getKey().getKey().toUpperCase(Locale.ENGLISH).contains("PROJECTILE"));
        elements.remove(Tag.ENTITY_TYPES_ARROWS);
        elements.remove(Tag.ENTITY_TYPES_BEEHIVE_INHABITORS);
        elements.remove(Tag.ENTITY_TYPES_CAN_TURN_IN_BOATS);
        elements.remove(Tag.ENTITY_TYPES_DISMOUNTS_UNDERWATER);
        elements.remove(Tag.ENTITY_TYPES_FALL_DAMAGE_IMMUNE);
        elements.remove(Tag.ENTITY_TYPES_FREEZE_HURTS_EXTRA_TYPES);
        elements.remove(Tag.ENTITY_TYPES_INVERTED_HEALING_AND_HARM);
        elements.remove(Tag.ENTITY_TYPES_NO_ANGER_FROM_WIND_CHARGE);
        elements.remove(Tag.ENTITY_TYPES_NON_CONTROLLING_RIDER);
        elements.remove(Tag.ENTITY_TYPES_NOT_SCARY_FOR_PUFFERFISH);
        elements.remove(Tag.ENTITY_TYPES_FROG_FOOD);
        // ... and so on
    }

    @Override
    protected String getTitleKey() {
        return "entity-selector";
    }

    @Override
    protected String getElementGroupKey() {
        return "entity-group.";
    }

    @Override
    protected Material getIconForTag(Tag<EntityType> tag) {
        if (tag.getKey().getKey().contains("boat")) {
            return Material.OAK_BOAT;
        }
        EntityType entType = Registry.ENTITY_TYPE.stream().filter(tag::isTagged).findAny().orElse(null);
        if (entType == null) {
            return Material.PAPER;
        }
        String eggName = entType.getKey().getKey().toUpperCase(Locale.ENGLISH) + "_SPAWN_EGG";
        try {
            return Material.valueOf(eggName);
        } catch (Exception e) {
            return Material.PAPER;
        }
    }
}
