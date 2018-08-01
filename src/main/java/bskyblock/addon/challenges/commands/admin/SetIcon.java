/**
 *
 */
package bskyblock.addon.challenges.commands.admin;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import bskyblock.addon.challenges.ChallengesAddon;
import bskyblock.addon.challenges.database.object.Challenges;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;

/**
 * @author tastybento
 *
 */
public class SetIcon extends CompositeCommand {

    private ChallengesAddon addon;

    /**
     * @param parent
     * @param label
     * @param aliases
     */
    public SetIcon(ChallengesAddon addon, CompositeCommand parent) {
        super(parent, "seticon");
        this.addon = addon;
    }

    /* (non-Javadoc)
     * @see world.bentobox.bbox.api.commands.BSBCommand#setup()
     */
    @Override
    public void setup() {
        setParameters("challenges.admin.seticon.parameters");
        setDescription("challenges.admin.seticon.description");
    }

    /* (non-Javadoc)
     * @see world.bentobox.bbox.api.commands.BSBCommand#execute(world.bentobox.bbox.api.commands.User, java.util.List)
     */
    @Override
    public boolean execute(User user, String label, List<String> args) {
        ItemStack icon = user.getInventory().getItemInMainHand();
        if (args.isEmpty() || icon == null) {
            user.sendMessage("challenges.admin.seticon.description");
            return false;
        }
        Challenges challenge = addon.getChallengesManager().getChallenge(args.get(0), getWorld());
        // Check if this challenge name exists
        if (challenge == null) {
            user.sendMessage("challenges.admin.seticon.error.no-such-challenge");
            return false;
        }
        challenge.setIcon(icon);
        user.sendMessage("general.success");
        return true;
    }

}
