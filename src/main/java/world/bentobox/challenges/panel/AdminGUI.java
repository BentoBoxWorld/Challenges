package world.bentobox.challenges.panel;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.Challenges;
import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem.ClickHandler;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;

public class AdminGUI implements ClickHandler {

    private ChallengesAddon addon;
    private User player;
    private Challenges challenge;
    private World world;
    private String permPrefix;
    private String label;

    /**
     * Shows the admin panel for the challenge
     * @param addon
     * @param player
     * @param challenge
     * @param world
     * @param permPrefix
     * @param label
     */
    public AdminGUI(ChallengesAddon addon, User player, Challenges challenge, World world,
            String permPrefix, String label) {
        super();
        this.addon = addon;
        this.player = player;
        this.challenge = challenge;
        this.world = world;
        this.permPrefix = permPrefix;
        this.label = label;

        new PanelBuilder().size(27).user(player).name(player.getTranslation("challenges.admin.gui-title"))
        .item(new PanelItemBuilder().icon(challenge.getIcon()).name("Icon").build())
        .item(9, new PanelItemBuilder().icon(new ItemStack(Material.WHITE_BANNER)).name("Description").description(challenge.getDescription()).build())
        .item(18, new PanelItemBuilder().icon(new ItemStack(Material.GREEN_STAINED_GLASS_PANE)).name("Active").build())
        .item(27, new PanelItemBuilder().icon(new ItemStack(Material.BOOK)).name("Edit required items").clickHandler(this).build())
        .build();
    }

    @Override
    public boolean onClick(Panel panel, User user, ClickType clickType, int slot) {
        if (slot == 27) {
            new RequiredPanel(challenge, user, panel);
        }
        return true;
    }


}
