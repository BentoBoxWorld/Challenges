package world.bentobox.challenges.panel.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;


/**
 * This GUI allows to select single entity and return it via Consumer.
 */
public class SingleEntitySelector extends PagedSelector<EntityType>
{

    /**
     * Non entities that really cannot be used
     */
    private static final List<EntityType> NON_ENTITIES = List.of(EntityType.UNKNOWN, EntityType.BLOCK_DISPLAY,
            EntityType.ITEM_DISPLAY,
            EntityType.TEXT_DISPLAY, EntityType.FALLING_BLOCK, EntityType.FIREBALL, EntityType.FISHING_BOBBER,
            EntityType.GIANT, EntityType.ILLUSIONER, EntityType.INTERACTION, EntityType.LIGHTNING_BOLT,
            EntityType.LLAMA_SPIT, EntityType.MARKER, EntityType.SHULKER_BULLET, EntityType.SMALL_FIREBALL,
            EntityType.DRAGON_FIREBALL, EntityType.EVOKER_FANGS, EntityType.BREEZE_WIND_CHARGE,
            EntityType.AREA_EFFECT_CLOUD);

    /**
     * Instantiates a new Single entity selector.
     *
     * @param user the user
     * @param asEggs the boolean
     * @param mode the mode
     * @param excluded the excluded
     * @param consumer the consumer
     */
    private SingleEntitySelector(User user, Mode mode, Set<EntityType> excluded,
            BiConsumer<Boolean, EntityType> consumer)
    {
        super(user);
        this.consumer = consumer;
        this.elements = Arrays.stream(EntityType.values()).filter(entity -> !excluded.contains(entity))
                .filter(entity -> !NON_ENTITIES.contains(entity))
                .filter(entity -> {
                    if (mode == Mode.ALIVE) {
                        return entity.isAlive();
                    } else {
                        return true;
                    }
                }).
                // Sort by names
                sorted(Comparator.comparing(EntityType::name)).collect(Collectors.toList());
        // Init without filters applied.
        this.filterElements = this.elements;
    }


    /**
     * This method opens GUI that allows to select challenge type.
     *
     * @param user User who opens GUI.
     * @param consumer Consumer that allows to get clicked type.
     */
    public static void open(User user, Mode mode, Set<EntityType> excluded, BiConsumer<Boolean, EntityType> consumer)
    {
        new SingleEntitySelector(user, mode, excluded, consumer).build();
    }


    /**
     * This method opens GUI that allows to select challenge type.
     *
     * @param user User who opens GUI.
     * @param consumer Consumer that allows to get clicked type.
     */
    public static void open(User user, BiConsumer<Boolean, EntityType> consumer)
    {
        new SingleEntitySelector(user, Mode.ANY, new HashSet<>(), consumer).build();
    }


    /**
     * This method opens GUI that allows to select challenge type.
     *
     * @param user User who opens GUI.
     * @param consumer Consumer that allows to get clicked type.
     */
    public static void open(User user, Mode mode, BiConsumer<Boolean, EntityType> consumer)
    {
        new SingleEntitySelector(user, mode, new HashSet<>(), consumer).build();
    }


    // ---------------------------------------------------------------------
    // Section: Methods
    // ---------------------------------------------------------------------


    /**
     * This method builds
     */
    @Override
    protected void build() {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user);
        panelBuilder.name(this.user.getTranslation(Constants.TITLE + "entity-selector"));

        PanelUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

        this.populateElements(panelBuilder, this.filterElements);

        panelBuilder.item(4, this.createButton());

        panelBuilder.build();
    }


    /**
     * This method is called when filter value is updated.
     */
    @Override
    protected void updateFilters() {
        if (this.searchString == null || this.searchString.isBlank()) {
            this.filterElements = this.elements;
        } else {
            this.filterElements = this.elements.stream().filter(element -> {
                // If element name is set and name contains search field, then do not filter out.
                return element.name().toLowerCase().contains(this.searchString.toLowerCase());
            }).distinct().collect(Collectors.toList());
        }
    }


    /**
     * This method builds PanelItem for given entity.
     * @param entity Entity which PanelItem must be created.
     * @return new PanelItem for given Entity.
     */
    @Override
    protected PanelItem createElementButton(EntityType entity) {
        final String reference = Constants.BUTTON + "entity.";

        List<String> description = new ArrayList<>();
        description.add(this.user.getTranslation(reference + "description", "[id]", entity.name()));
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-choose"));


        return new PanelItemBuilder()
                .name(this.user.getTranslation(reference + "name", "[entity]", Utils.prettifyObject(entity, this.user)))
                .icon(getIcon(entity)).description(description)
                .clickHandler((panel, user1, clickType, slot) -> {
                    this.consumer.accept(true, entity);
                    return true;
                }).build();
    }

    /**
     * Get an ItemStack icon for any entity type, or PAPER if it's not really known
     * @param et entity type
     * @return ItemStack
     */
    public static ItemStack getIcon(EntityType et) {
        // Check for Materials like boats that are named the same as their entitytype.
        Material m = Material.getMaterial(et.getKey().getKey().toUpperCase(Locale.ENGLISH));
        if (m != null) {
            return new ItemStack(m);
        }
        // Try to get the spawn egg for the given entity by using the naming convention.
        String spawnEggName = et.name() + "_SPAWN_EGG";
        try {
            Material spawnEgg = Material.valueOf(spawnEggName);
            // If found, return an ItemStack of the spawn egg.
            return new ItemStack(spawnEgg);
        } catch (IllegalArgumentException ex) {
            // No spawn egg material exists for this entity type.
        }
        // Fallback
        return switch (et) {
        case EYE_OF_ENDER -> new ItemStack(Material.ENDER_EYE);
        case LEASH_KNOT -> new ItemStack(Material.LEAD);
        case OMINOUS_ITEM_SPAWNER -> new ItemStack(Material.TRIAL_SPAWNER);
        case PLAYER -> new ItemStack(Material.PLAYER_HEAD);
        case SPAWNER_MINECART -> new ItemStack(Material.MINECART);
        case TRADER_LLAMA -> new ItemStack(Material.LLAMA_SPAWN_EGG);
        case WITHER_SKULL -> new ItemStack(Material.WITHER_SKELETON_SKULL);
        default -> new ItemStack(Material.PAPER);

        };
    }

    /**
     * This method creates PanelItem button of requested type.
     * @return new PanelItem with requested functionality.
     */
    private PanelItem createButton() {
        final String reference = Constants.BUTTON + "cancel.";

        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(reference + "description"));

        ItemStack icon = new ItemStack(Material.IRON_DOOR);
        PanelItem.ClickHandler clickHandler = (panel, user1, clickType, slot) -> {
            this.consumer.accept(false, null);
            return true;
        };

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-cancel"));

        return new PanelItemBuilder().icon(icon).name(name).description(description).clickHandler(clickHandler).build();
    }


    // ---------------------------------------------------------------------
    // Section: Mode
    // ---------------------------------------------------------------------


    public enum Mode {
        ALIVE, ANY
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * List with elements that will be displayed in current GUI.
     */
    private final List<EntityType> elements;

    /**
     * This variable stores consumer.
     */
    private final BiConsumer<Boolean, EntityType> consumer;

    /**
     * Stores filtered items.
     */
    private List<EntityType> filterElements;
}
