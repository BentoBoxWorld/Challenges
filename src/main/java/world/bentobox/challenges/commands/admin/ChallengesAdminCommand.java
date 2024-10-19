package world.bentobox.challenges.commands.admin;

import java.util.List;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.panel.admin.AdminPanel;


public class ChallengesAdminCommand extends CompositeCommand
{
    /**
     * Instantiates a new Challenges' admin command.
     *
     * @param addon the addon
     * @param parent the parent
     */
    public ChallengesAdminCommand(ChallengesAddon addon, CompositeCommand parent)
    {
        super(addon,
            parent,
            addon.getChallengesSettings().getAdminMainCommand().split(" ")[0],
            addon.getChallengesSettings().getAdminMainCommand().split(" "));
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
            AdminPanel.open(this.getAddon(),
                this.getWorld(),
                user,
                this.getTopLabel(),
                this.getPermissionPrefix());

            return true;
        }
        this.showHelp(this, user);
        return false;
    }
}
