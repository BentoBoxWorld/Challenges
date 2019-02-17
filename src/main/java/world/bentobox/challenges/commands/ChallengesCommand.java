package world.bentobox.challenges.commands;

import java.util.List;

import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.panel.user.ChallengesGUI;


public class ChallengesCommand extends CompositeCommand
{
    public static final String CHALLENGE_COMMAND = "challenges";


    public ChallengesCommand(ChallengesAddon addon, CompositeCommand cmd)
    {
        super(addon, cmd, CHALLENGE_COMMAND);
    }


    @Override
    public boolean execute(User user, String label, List<String> args)
    {
        // Open up the challenges GUI
        if (user.isPlayer())
        {
            if (this.getPlugin().getIslands().getIsland(this.getWorld(), user.getUniqueId()) != null)
            {
                new ChallengesGUI((ChallengesAddon) this.getAddon(),
                    this.getWorld(),
                    user,
                    this.getTopLabel(),
                    this.getPermissionPrefix()).build();
                return true;
            }
            else
            {
                user.sendMessage("general.errors.no-island");
                return false;
            }
        }
        // Show help
        showHelp(this, user);
        return false;
    }


    @Override
    public void setup()
    {
        this.setPermission(CHALLENGE_COMMAND);
        this.setParametersHelp("challen/ges.commands.user.parameters");
        this.setDescription("challenges.commands.user.description");
    }
}
