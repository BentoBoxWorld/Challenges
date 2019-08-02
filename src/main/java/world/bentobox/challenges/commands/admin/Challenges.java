package world.bentobox.challenges.commands.admin;

import java.util.List;

import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.panel.admin.AdminGUI;


public class Challenges extends CompositeCommand
{

    /**
     * Admin command for challenges
     *
     * @param parent
     */
    public Challenges(ChallengesAddon addon, CompositeCommand parent)
    {
        super(addon, parent, "challenges");
    }


    @Override
    public void setup()
    {
        this.setPermission("admin.challenges");
        this.setParametersHelp("challenges.commands.admin.main.parameters");
        this.setDescription("challenges.commands.admin.main.description");

        // Register sub commands

        // This method reloads challenges addon
        new ReloadChallenges(getAddon(), this);
        // Import ASkyBlock Challenges
        new ImportCommand(getAddon(), this);
        // Defaults processing command
        new DefaultsCommand(this.getAddon(), this);

        // Complete challenge command
        new CompleteCommand(this.getAddon(), this);

        // Reset challenge command
        new ResetCommand(this.getAddon(), this);

        new ShowChallenges(this.getAddon(), this);

        new MigrateCommand(this.getAddon(), this);
    }


    @Override
    public boolean execute(User user, String label, List<String> args)
    {
        // Open up the admin challenges GUI
        if (user.isPlayer())
        {
            new AdminGUI((ChallengesAddon) this.getAddon(),
                this.getWorld(),
                user,
                this.getTopLabel(),
                this.getPermissionPrefix()).build();

            return true;
        }
        return false;
    }
}
