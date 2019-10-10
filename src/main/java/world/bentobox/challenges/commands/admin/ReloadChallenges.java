package world.bentobox.challenges.commands.admin;

import java.util.List;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.ChallengesManager;


/**
 * This class allows to reload challenges addon.
 */
public class ReloadChallenges extends CompositeCommand
{
    /**
     * Admin command to reloads challenges addon.
     * @param parent
     */
    public ReloadChallenges(Addon addon, CompositeCommand parent)
    {
        super(addon, parent, "reload");
        this.manager = ((ChallengesAddon) getAddon()).getChallengesManager();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setup()
    {
        this.setPermission("admin.challenges");
        this.setParametersHelp("challenges.commands.admin.reload.parameters");
        this.setDescription("challenges.commands.admin.reload.description");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute(User user, String label, List<String> args)
    {
        if (args.isEmpty())
        {
            this.manager.load();
            user.sendMessage("general.success");
            return true;
        }
        else if (args.get(0).equalsIgnoreCase("hard"))
        {
            this.manager.reload();
            user.sendMessage("general.success");
            return true;
        }
        else
        {
            this.showHelp(this, user);
            return false;
        }
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    private ChallengesManager manager;
}
