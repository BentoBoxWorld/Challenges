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
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;


/**
 * This class contains all necessary things that allows to select single material tag from all in game tags. Selected
 * tag will be returned via BiConsumer.
 */
public class MultiMaterialTagsSelector extends PagedSelector<Tag<Material>>
{

    public static final Map<Tag<Material>, Material> ICONS = Map.of(Tag.AIR, Material.BARRIER, Tag.FIRE,
            Material.TORCH, Tag.CANDLE_CAKES, Material.CAKE, Tag.PORTALS, Material.MAGENTA_STAINED_GLASS_PANE,
            Tag.WALL_HANGING_SIGNS, Material.ACACIA_SIGN, Tag.WALL_SIGNS, Material.OAK_SIGN, Tag.WALL_CORALS,
            Material.BUBBLE_CORAL_FAN);

    /**
     * Functional buttons in current GUI.
     */
    private enum Button {
        ACCEPT_SELECTED, CANCEL
    }

    public enum Mode {
        BLOCKS, ITEMS, ANY
    }

    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    private final List<Tag<Material>> elements = new ArrayList<>();

    /**
     * Set that contains selected materials.
     */
    private final Set<Tag<Material>> selectedElements;

    /**
     * This variable stores consumer.
     */
    private final BiConsumer<Boolean, Collection<Tag<Material>>> consumer;

    /**
     * Stores filtered items.
     */
    private List<Tag<Material>> filterElements;

    private MultiMaterialTagsSelector(User user, Mode mode, Set<Tag<Material>> excluded,
            BiConsumer<Boolean, Collection<Tag<Material>>> consumer) {
		super(user);
		this.consumer = consumer;
		
		this.selectedElements = new HashSet<>();
        Iterable<Tag<Material>> iterable = Bukkit.getTags("blocks", Material.class);
        iterable.forEach(elements::add);
        elements.sort(Comparator.comparing(tag -> tag.getKey().getKey()));
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
        // Remove excluded tags
        excluded.forEach(tag -> elements.removeIf(tag2 -> tag2.equals(tag)));
		// Init without filters applied.
		this.filterElements = this.elements;
	}


	/**
	 * This method opens GUI that allows to select challenge type.
	 *
	 * @param user User who opens GUI.
	 * @param consumer Consumer that allows to get clicked type.
	 */
    public static void open(User user, Mode mode, Set<Tag<Material>> excluded,
            BiConsumer<Boolean, Collection<Tag<Material>>> consumer)
	{
		new MultiMaterialTagsSelector(user, mode, excluded, consumer).build();
	}


	/**
	 * This method opens GUI that allows to select challenge type.
	 *
	 * @param user User who opens GUI.
	 * @param consumer Consumer that allows to get clicked type.
	 */
    public static void open(User user, BiConsumer<Boolean, Collection<Tag<Material>>> consumer)
	{
		new MultiMaterialTagsSelector(user, Mode.ANY, new HashSet<>(), consumer).build();
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * This method builds all necessary elements in GUI panel.
	 */
	@Override
	protected void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user);
		panelBuilder.name(this.user.getTranslation(Constants.TITLE + "block-selector"));

		PanelUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

		this.populateElements(panelBuilder, this.filterElements);

		panelBuilder.item(3, this.createButton(Button.ACCEPT_SELECTED));
		panelBuilder.item(5, this.createButton(Button.CANCEL));

		panelBuilder.build();
	}


	/**
	 * This method is called when filter value is updated.
	 */
	@Override
	protected void updateFilters()
	{
		if (this.searchString == null || this.searchString.isBlank())
		{
			this.filterElements = this.elements;
		}
		else
		{
			this.filterElements = this.elements.stream().
				filter(element -> {
					// If element name is set and name contains search field, then do not filter out.
                        return element.getKey().getKey().toLowerCase(Locale.ENGLISH)
                                .contains(this.searchString.toLowerCase(Locale.ENGLISH));
				}).
				distinct().
				collect(Collectors.toList());
		}
	}


	/**
	 * This method creates PanelItem button of requested type.
	 * @param button Button which must be created.
	 * @return new PanelItem with requested functionality.
	 */
	private PanelItem createButton(Button button)
	{
		final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";

		final String name = this.user.getTranslation(reference + "name");
		final List<String> description = new ArrayList<>(3);
		description.add(this.user.getTranslation(reference + "description"));

		ItemStack icon;
		PanelItem.ClickHandler clickHandler;

		switch (button)
		{
			case ACCEPT_SELECTED -> {
				if (!this.selectedElements.isEmpty())
				{
					description.add(this.user.getTranslation(reference + "title"));
					this.selectedElements.forEach(material ->
						description.add(this.user.getTranslation(reference + "element",
							"[element]", Utils.prettifyObject(material, this.user))));
				}

				icon = new ItemStack(Material.COMMAND_BLOCK);
				clickHandler = (panel, user1, clickType, slot) ->
				{
					this.consumer.accept(true, this.selectedElements);
					return true;
				};

				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-save"));
			}
			case CANCEL -> {

				icon = new ItemStack(Material.IRON_DOOR);

				clickHandler = (panel, user1, clickType, slot) ->
				{
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

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(description).
			clickHandler(clickHandler).
			build();
	}


	/**
	 * This method creates button for given material.
	 * @param materialTag material which button must be created.
	 * @return new Button for material.
	 */
	@Override
    protected PanelItem createElementButton(Tag<Material> materialTag)
	{
        final String reference = Constants.BUTTON + "block-group.";

		List<String> description = new ArrayList<>();
		description.add(this.user.getTranslation(reference + "description",
                "[id]", materialTag.getKey().getKey()));

		if (this.selectedElements.contains(materialTag))
		{
			description.add(this.user.getTranslation(reference + "selected"));
			description.add("");
			description.add(this.user.getTranslation(Constants.TIPS + "click-to-deselect"));
		}
		else
		{
			description.add("");
			description.add(this.user.getTranslation(Constants.TIPS + "click-to-select"));
		}

		return new PanelItemBuilder().
                name(this.user.getTranslation(reference + "name", "[tag]",
				Utils.prettifyObject(materialTag, this.user))).
                icon(getIcon(materialTag)).
			description(description).
			clickHandler((panel, user1, clickType, slot) -> {
				// On right click change which entities are selected for deletion.
                    if (!this.selectedElements.add(materialTag))
				{
					// Remove material if it is already selected
                        this.selectedElements.remove(materialTag);
				}

				this.build();
				return true;
			}).
			glow(this.selectedElements.contains(materialTag)).
			build();
	}

	
    private @Nullable Material getIcon(Tag<Material> materialTag) {
        return ICONS.getOrDefault(materialTag, Registry.MATERIAL.stream().filter(materialTag::isTagged)
                .filter(Material::isItem).findAny()
        .orElse(Material.PAPER));
    }

}
