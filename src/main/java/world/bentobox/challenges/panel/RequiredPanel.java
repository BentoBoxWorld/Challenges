package world.bentobox.challenges.panel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem.ClickHandler;
import world.bentobox.bentobox.api.panels.PanelListener;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;

/**
 * Handles the requirements for a challenge
 * Items, blocks, entities
 * @author tastybento
 * @deprecated All panels are reworked.
 */
@Deprecated
public class RequiredPanel implements ClickHandler, PanelListener {
    private static final int CONTROL_NUMBER = 4;
    private Challenge challenge;
    private User user;
    private Panel panel;
    private Panel referringPanel;

    /**
     * @param challenge
     * @param user
     */
    public RequiredPanel(Challenge challenge, User user, Panel referringPanel) {
        this.challenge = challenge;
        this.user = user;
        this.panel = openPanel();
        this.referringPanel = referringPanel;
    }

    private Panel openPanel() {
        PanelBuilder pb = new PanelBuilder().listener(this).name("Required Items").size(49);
        // Add the name and description icon
        pb.item(new PanelItemBuilder().icon(Material.BOOK).name(challenge.getFriendlyName()).description(challenge.getDescription()).clickHandler(this).build());
        // Add take all button
        pb.item(new PanelItemBuilder().icon(Material.BOOK).name("Take Items").description(challenge.isTakeItems() ? "Yes" : "No").clickHandler(this).build());
        // Add save button
        pb.item(new PanelItemBuilder().icon(Material.BOOK).name("Save").clickHandler(this).build());
        // Add cancel button
        pb.item(new PanelItemBuilder().icon(Material.BOOK).name("Cancel").clickHandler(this).build());

        switch (challenge.getChallengeType()) {
        case INVENTORY:
            // Show the required items in the inventory
            challenge.getRequiredItems().stream().map(i -> new PanelItemBuilder().icon(i).clickHandler(this).build()).forEach(pb::item);
            return pb.user(user).build();
        case ISLAND:
            // Create the blocks required
            challenge.getRequiredBlocks().entrySet().stream().map(en -> new ItemStack(en.getKey(), en.getValue())).map(i -> new PanelItemBuilder().icon(i).clickHandler(this).build()).forEach(pb::item);
            // Create the entities required
            challenge.getRequiredEntities().entrySet().stream().map(this::toSpawnEgg).map(i -> new PanelItemBuilder()
                    .icon(i)
                    .name(Util.prettifyText(i.getType().toString()))
                    .description("Entity")
                    .clickHandler(this)
                    .build()).forEach(pb::item);
            return pb.user(user).build();
        case OTHER:

            break;
        default:
            break;

        }
        return panel;
    }

    private ItemStack toSpawnEgg(Entry<EntityType, Integer> en) {
        Material mat = Material.getMaterial(en.getKey().name() + "_SPAWN_EGG");
        if (mat != null) {
            return new ItemStack(mat, en.getValue());
        } else {
            return new ItemStack(Material.COW_SPAWN_EGG);
        }
    }


    @Override
    public boolean onClick(Panel panel, User user, ClickType clickType, int slot) {
        Bukkit.getLogger().info("DEBUG: slot = " + slot);
        return slot < CONTROL_NUMBER;
    }

    @Override
    public void setup() {
        // nothing to do

    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {

    }


    @Override
    public void onInventoryClick(User user, InventoryClickEvent event) {
        // Allow drag and drop
        event.setCancelled(event.getRawSlot() < CONTROL_NUMBER);

        Bukkit.getLogger().info("DEBUG: inv slot = " + event.getSlot());
        Bukkit.getLogger().info("DEBUG: inv slot type = " + event.getSlotType());
        if (event.getSlot() == 1) {
            // Take items
            challenge.setTakeItems(!challenge.isTakeItems());
            // Update item
            event.getInventory().setItem(event.getSlot(), new PanelItemBuilder().icon(Material.BOOK).name("Take Items").description(challenge.isTakeItems() ? "Yes" : "No").build().getItem());
            return;
        }
        if (event.getSlot() == 3) {
            // Cancel
            referringPanel.open(user);
            return;
            // Return to previous panel
        }
        // Save
        if (event.getSlot() != 2) {
            return;
        }
        // Save changes
        switch (challenge.getChallengeType()) {
        case INVENTORY:
            List<ItemStack> reqItems = new ArrayList<>();
            // Skip first item
            for (int i = CONTROL_NUMBER; i < event.getInventory().getSize(); i++) {
                if (event.getInventory().getItem(i) != null) {
                    reqItems.add(event.getInventory().getItem(i));
                }
            }
            challenge.setRequiredItems(reqItems);
            user.sendMessage("challenges.admin.saved");
            // TODO: save challenges
            event.getInventory().setItem(event.getSlot(), new PanelItemBuilder().icon(Material.BOOK).name("Save").description("Saved").build().getItem());
            break;
        case ISLAND:
            break;
            case OTHER:
            break;
        default:
            break;

        }

    }


}
