package world.bentobox.challenges.panel.admin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.challenges.panel.CommonPanel;
import world.bentobox.challenges.panel.util.MultiBlockSelector;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;

/**
 * The ManageBlocksPanel class provides a graphical interface for administrators
 * to manage a collection of materials along with their required counts.
 * <p>
 * It extends the abstract generic class {@code AbstractManageEnumPanel<Material>},
 * which provides common functionality for panels that handle paginated elements.
 * This panel allows adding new materials using a multi-selection tool and
 * removing selected materials. When a material is clicked (left or right), a
 * numeric input conversation can be started to update the count associated with
 * that material.
 * </p>
 *
 * <p><b>Usage:</b> To display this panel, call the static {@link #open(CommonPanel, Map)}
 * method with the parent panel and the map of materials to their counts.</p>
 */
public class ManageBlocksPanel extends AbstractManageEnumPanel<Material> {

    /**
     * Private constructor that initializes the ManageBlocksPanel with the provided
     * material map.
     *
     * @param parentGUI   The parent panel that spawns this panel.
     * @param materialMap A map of Material objects to their required counts.
     */
    private ManageBlocksPanel(CommonPanel parentGUI, Map<Material, Integer> materialMap) {
        super(parentGUI, materialMap);
    }

    /**
     * Opens the Manage Blocks panel.
     *
     * @param parentGUI   The parent panel that spawns this panel.
     * @param materialMap A map of Material objects to their required counts.
     */
    public static void open(CommonPanel parentGUI, Map<Material, Integer> materialMap) {
        new ManageBlocksPanel(parentGUI, materialMap).build();
    }

    /**
     * Provides the icon for a material element.
     *
     * @param material The material for which the icon is required.
     * @param count    The count to be displayed on the icon.
     * @return An ItemStack representing the material icon.
     */
    @Override
    protected ItemStack getElementIcon(Material material, int count) {
        return PanelUtils.getMaterialItem(material, count);
    }

    /**
     * Returns the translation prefix used for buttons related to materials.
     *
     * @return A string containing the translation prefix for material buttons.
     */
    @Override
    protected String getElementTranslationPrefix() {
        return Constants.BUTTON + "material.";
    }

    /**
     * Returns the placeholder key used in translations for a material.
     *
     * @return The placeholder key for materials.
     */
    @Override
    protected String getElementPlaceholder() {
        return "[material]";
    }

    /**
     * Returns the translation key for the title of the panel.
     *
     * @return A string containing the translation key for the panel title.
     */
    @Override
    protected String getPanelTitleKey() {
        return Constants.TITLE + "manage-blocks";
    }

    /**
     * Adds functional buttons (e.g., Add and Remove) to the panel.
     *
     * @param panelBuilder The PanelBuilder used to construct the panel.
     */
    @Override
    protected void addFunctionalButtons(PanelBuilder panelBuilder) {
        // Position 3: Button for adding new materials.
        panelBuilder.item(3, createButton(Button.ADD_BLOCK));
        // Position 5: Button for removing selected materials.
        panelBuilder.item(5, createButton(Button.REMOVE_BLOCK));
    }

    /**
     * Creates a functional button based on the specified type.
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
        case ADD_BLOCK -> {
            icon = new ItemStack(Material.BUCKET);
            clickHandler = (panel, user, clickType, slot) -> {
                // Open a multi-selection tool to add new materials.
                MultiBlockSelector.open(this.user, MultiBlockSelector.Mode.BLOCKS, new HashSet<>(this.itemList),
                        (status, materials) -> {
                            if (status) {
                                // For each selected material, add it to the map with a default count.
                                materials.forEach(material -> {
                                    this.itemsMap.put(material, 1);
                                    this.itemList.add(material);
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
        case REMOVE_BLOCK -> {
                if (!this.selectedItems.isEmpty()) {
                // If any materials are selected, list them in the description.
                description.add(this.user.getTranslation(reference + "title"));
                this.selectedItems.forEach(material -> description.add(this.user.getTranslation(reference + "material",
                        "[material]", Utils.prettifyObject(material, this.user))));
                }
            icon = new ItemStack(Material.LAVA_BUCKET);
            clickHandler = (panel, user, clickType, slot) -> {
                if (!this.selectedItems.isEmpty()) {
                    // Remove all selected materials from the map and list.
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
     * Enumeration of functional buttons in the Manage Blocks panel.
     */
    private enum Button {
        ADD_BLOCK,
        REMOVE_BLOCK
    }
}
