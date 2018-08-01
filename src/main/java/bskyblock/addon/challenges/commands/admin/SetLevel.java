/**
 *
 */
package bskyblock.addon.challenges.commands.admin;

import java.util.List;

import bskyblock.addon.challenges.ChallengesAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;

/**
 * @author tastybento
 *
 */
public class SetLevel extends CompositeCommand {

    /**
     * @param plugin
     * @param label
     * @param string
     */
    public SetLevel(ChallengesAddon plugin, String label, String... string) {
        super(plugin, label, string);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param parent
     * @param label
     * @param aliases
     */
    public SetLevel(CompositeCommand parent, String label, String... aliases) {
        super(parent, label, aliases);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param label
     * @param aliases
     */
    public SetLevel(String label, String... aliases) {
        super(label, aliases);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see world.bentobox.bbox.api.commands.BSBCommand#setup()
     */
    @Override
    public void setup() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see world.bentobox.bbox.api.commands.BSBCommand#execute(world.bentobox.bbox.api.commands.User, java.util.List)
     */
    @Override
    public boolean execute(User user, String label, List<String> args) {
        // TODO Auto-generated method stub
        return false;
    }

}
