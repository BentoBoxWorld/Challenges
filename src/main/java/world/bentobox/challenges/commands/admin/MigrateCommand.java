package world.bentobox.challenges.commands.admin;


import java.util.List;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.challenges.ChallengesAddon;


public class MigrateCommand extends CompositeCommand
{
    /**
     * Instantiates a new Migrate command command.
     *
     * @param addon the addon
     * @param cmd the cmd
     */
    public MigrateCommand(Addon addon, CompositeCommand cmd)
    {
        super(addon, cmd, "migrate");
    }


    @Override
    public boolean execute(User user, String label, List<String> args)
    {
        ((ChallengesAddon) getAddon()).getChallengesManager().migrateDatabase(user, getWorld());

        return true;
    }


    @Override
    public void setup()
    {
        this.setPermission("challenges.admin");
        this.setParametersHelp("challenges.commands.admin.migrate.parameters");
        this.setDescription("challenges.commands.admin.migrate.description");
    }
}
