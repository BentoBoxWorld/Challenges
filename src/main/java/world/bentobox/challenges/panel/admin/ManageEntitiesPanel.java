package world.bentobox.challenges.panel.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.challenges.panel.CommonPanel;
import world.bentobox.challenges.panel.util.MultiEntitySelector;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;

/**
 * The ManageEntitiesPanel class provides a graphical interface for administrators
 * to manage a collection of entities (of type {@link EntityType}) along with their required counts.
 * <p>
 * This panel enables adding new entities via a multi-selection tool, removing selected entities,
 * and toggling the display between entity eggs and mob heads. It extends the generic
 * {@code AbstractManageEnumPanel<EntityType>} to share common functionality with other paginated panels.
 * </p>
 *
 * <p><b>Usage:</b> To display this panel, call the static {@link #open(CommonPanel, Map)}
 * method with the parent panel and the map of entities to their counts.</p>
 */
public class ManageEntitiesPanel extends AbstractManageEnumPanel<EntityType> {

    /**
     * Flag to indicate whether entities should be displayed as eggs (true) or mob heads (false).
     */
    private boolean asEggs = true;

    /**
     * Private constructor that initializes the ManageEntitiesPanel with the provided
     * entities map.
     *
     * @param parentGUI        The parent panel that spawns this panel.
     * @param requiredEntities A map of EntityType objects to their required counts.
     */
    private ManageEntitiesPanel(CommonPanel parentGUI, Map<EntityType, Integer> requiredEntities) {
        super(parentGUI, requiredEntities);
    }

    /**
     * Opens the Manage Entities panel.
     *
     * @param parentGUI        The parent panel that spawns this panel.
     * @param requiredEntities A map of EntityType objects to their required counts.
     */
    public static void open(CommonPanel parentGUI, Map<EntityType, Integer> requiredEntities) {
        new ManageEntitiesPanel(parentGUI, requiredEntities).build();
    }

    /**
     * Provides the icon for an entity element.
     * <p>
     * Depending on the {@code asEggs} flag, this method returns either the egg icon or the mob head icon.
     * </p>
     *
     * @param entity The entity for which the icon is required.
     * @param count  The count to be displayed on the icon.
     * @return An ItemStack representing the entity icon.
     */
    @Override
    protected ItemStack getElementIcon(EntityType entity, int count) {
        return asEggs ? PanelUtils.getEntityEgg(entity, count) : PanelUtils.getEntityHead(entity, count);
    }

    /**
     * Returns the translation prefix used for buttons related to entities.
     *
     * @return A string containing the translation prefix for entity buttons.
     */
    @Override
    protected String getElementTranslationPrefix() {
        return Constants.BUTTON + "entity.";
    }

    /**
     * Returns the placeholder key used in translations for an entity.
     *
     * @return The placeholder key for entities.
     */
    @Override
    protected String getElementPlaceholder() {
        return "[entity]";
    }

    /**
     * Returns the translation key for the title of the panel.
     *
     * @return A string containing the translation key for the panel title.
     */
    @Override
    protected String getPanelTitleKey() {
        return Constants.TITLE + "manage-entities";
    }

    /**
     * Adds functional buttons (e.g., Add, Remove, and Switch Display Mode) to the panel.
     *
     * @param panelBuilder The PanelBuilder used to construct the panel.
     */
    @Override
    protected void addFunctionalButtons(PanelBuilder panelBuilder) {
        // Position 3: Button for adding new entities.
        panelBuilder.item(3, createButton(Button.ADD_ENTITY));
        // Position 5: Button for removing selected entities.
        panelBuilder.item(5, createButton(Button.REMOVE_ENTITY));
        // Position 8: Button to switch between displaying entity eggs and mob heads.
        panelBuilder.item(8, createButton(Button.SWITCH_ENTITY));
    }

    /**
     * Creates a functional button based on the specified action.
     *
     * @param button The button type to create.
     * @return A PanelItem representing the functional button.
     */
    private PanelItem createButton(Button button) {
        final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";
        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(reference + "description"));

        ItemStack icon;
        PanelItem.ClickHandler clickHandler;
        boolean glow;

        switch (button) {
        case ADD_ENTITY -> {
            icon = new ItemStack(Material.BUCKET);
            clickHandler = (panel, user, clickType, slot) -> {
                // Open a multi-selection tool to add new entities.
                MultiEntitySelector.open(this.user, this.asEggs, MultiEntitySelector.Mode.ALIVE, this.itemsMap.keySet(),
                        (status, entities) -> {
                            if (status) {
                                // For each selected entity, add it to the map with a default count.
                                entities.forEach(entity -> {
                                    this.itemsMap.put(entity, 1);
                                    this.itemList.add(entity);
                                });
                            }
                            // Rebuild the panel to reflect changes.
                            this.build();
                        });
                return true;
            };
            glow = false;
            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-add"));
            }
        case REMOVE_ENTITY -> {
                if (!this.selectedItems.isEmpty()) {
                // If any entities are selected, list them in the description.
                description.add(this.user.getTranslation(reference + "title"));
                this.selectedItems.forEach(entity -> description.add(this.user.getTranslation(reference + "entity",
                        "[entity]", Utils.prettifyObject(entity, this.user))));
            }
            icon = new ItemStack(Material.LAVA_BUCKET);
            clickHandler = (panel, user, clickType, slot) -> {
                if (!this.selectedItems.isEmpty()) {
                    // Remove all selected entities from the map and list.
                    this.itemsMap.keySet().removeAll(this.selectedItems);
                    this.itemList.removeAll(this.selectedItems);
                    this.selectedItems.clear();
                    // Rebuild the panel after removal.
                    this.build();
                }
                return true;
            };
            glow = !this.selectedItems.isEmpty();
            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-remove"));
        }
        case SWITCH_ENTITY -> {
            // Button to toggle the display mode between entity eggs and mob heads.
            icon = new ItemStack(asEggs ? Material.EGG : Material.PLAYER_HEAD);
            clickHandler = (panel, user, clickType, slot) -> {
                // Toggle the display mode flag and rebuild the panel.
                this.asEggs = !this.asEggs;
                    this.build();
                return true;
            };
            glow = false;
            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
        }
        default -> {
            icon = new ItemStack(Material.PAPER);
            clickHandler = null;
            glow = false;
        }
        }

        return new PanelItemBuilder().icon(icon).name(name).description(description).clickHandler(clickHandler)
                .glow(glow).build();
    }

    /**
     * Enumeration of functional buttons in the Manage Entities panel.
     */
    private enum Button {
        ADD_ENTITY, REMOVE_ENTITY, SWITCH_ENTITY
    }
}
