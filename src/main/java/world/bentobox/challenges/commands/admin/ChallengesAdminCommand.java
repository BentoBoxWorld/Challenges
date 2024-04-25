package world.bentobox.challenges.commands.admin;

import java.util.List;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.panel.admin.AdminPanel;
import world.bentobox.challenges.panel.user.ChallengesPanel;


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
        BentoBox.getInstance().logDebug("Args size = " + args.size());
        // Open up the admin challenges GUI
        if (user.isPlayer())
        {
            if (args.isEmpty()) {
                AdminPanel.open(this.getAddon(),
                        this.getWorld(),
                        user,
                        this.getTopLabel(),
                        this.getPermissionPrefix());

                return true;
            } else if (args.size() == 1) {
                User target = getPlayers().getUser(args.get(0).trim());
                if (target == null) {
                    user.sendMessage("general.errors.unknown-player");
                    return false;
                }
                ChallengesPanel.view(this.getAddon(),
                        this.getWorld(),
                        target,
                        user,
                        this.getTopLabel(),
                        this.getPermissionPrefix());

                return true;
            }
            // Show help
            showHelp(this, user);
            return false;
        }
        return false;
    }
}
