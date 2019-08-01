package world.bentobox.challenges.commands;

import java.util.List;
import java.util.Optional;

import world.bentobox.bentobox.api.addons.GameModeAddon;
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


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canExecute(User user, String label, List<String> args)
    {
        Optional<GameModeAddon> optionalAddon = this.getAddon().getPlugin().getIWM().getAddon(this.getWorld());

        if (!optionalAddon.isPresent())
        {
            // Not a GameMode world.
            user.sendMessage("general.errors.wrong-world");
            return false;
        }

        if (!((ChallengesAddon) this.getAddon()).getChallengesManager().hasAnyChallengeData(this.getWorld()))
        {
            // Do not open gui if there is no challenges.

            this.getAddon().getLogger().severe("There are no challenges set up in " + this.getWorld() + "!");

            // Show admin better explanation.
            if (user.isOp() || user.hasPermission(this.getPermissionPrefix() + "admin.challenges"))
            {
                String topLabel = optionalAddon.get().getAdminCommand().orElseGet(this::getParent).getTopLabel();
                user.sendMessage("challenges.errors.no-challenges-admin", "[command]", topLabel + " challenges");
            }
            else
            {
                user.sendMessage("challenges.errors.no-challenges");
            }

            return false;
        }

        if (this.getPlugin().getIslands().getIsland(this.getWorld(), user.getUniqueId()) == null)
        {
            // Do not open gui if there is no island for this player.
            user.sendMessage("general.errors.no-island");
            return false;
        }

        return true;
    }


    @Override
    public boolean execute(User user, String label, List<String> args)
    {
        // Open up the challenges GUI
        if (user.isPlayer())
        {
            new ChallengesGUI((ChallengesAddon) this.getAddon(),
                    this.getWorld(),
                    user,
                    this.getTopLabel(),
                    this.getPermissionPrefix()).build();
            return true;
        }
        // Show help
        showHelp(this, user);
        return false;
    }


    @Override
    public void setup()
    {
        this.setPermission(CHALLENGE_COMMAND);
        this.setParametersHelp("challenges.commands.user.main.parameters");
        this.setDescription("challenges.commands.user.main.description");

        new CompleteChallengeCommand(this.getAddon(), this);
        this.setOnlyPlayer(true);
    }
}
