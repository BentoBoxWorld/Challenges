package bentobox.addon.challenges.panel;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import bentobox.addon.challenges.ChallengesAddon;
import bentobox.addon.challenges.database.object.Challenges;
import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem.ClickHandler;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;

public class AdminEditGUI implements ClickHandler {

    private ChallengesAddon addon;
    private User requester;
    private Challenges challenge;
    private World world;
    private String permPrefix;
    private String label;
    private User target;

    /**
     * Shows the admin panel for the challenge for a player
     * @param addon addon
     * @param requester admin user
     * @param target target of admin
     * @param challenge challenge
     * @param world world
     * @param permPrefix permission prefix for world
     * @param label command label
     */
    public AdminEditGUI(ChallengesAddon addon, User requester, User target, Challenges challenge, World world,
            String permPrefix, String label) {
        super();
        this.addon = addon;
        this.requester = requester;
        this.target = target;
        this.challenge = challenge;
        this.world = world;
        this.permPrefix = permPrefix;
        this.label = label;

        new PanelBuilder().size(27).user(requester).name(requester.getTranslation("challenges.admin.gui-title"))
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
