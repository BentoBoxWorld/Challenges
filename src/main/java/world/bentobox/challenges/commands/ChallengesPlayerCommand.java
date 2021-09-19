package world.bentobox.challenges.commands;

import java.util.List;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.panel.user.ChallengesPanel;
import world.bentobox.challenges.utils.Utils;


public class ChallengesPlayerCommand extends CompositeCommand
{
    public ChallengesPlayerCommand(ChallengesAddon addon, CompositeCommand cmd)
    {
        super(addon,
            cmd,
            addon.getChallengesSettings().getPlayerMainCommand().split(" ")[0],
            addon.getChallengesSettings().getPlayerMainCommand().split(" "));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canExecute(User user, String label, List<String> args)
    {
        if (!this.getIWM().inWorld(user.getWorld()) ||
            Util.getWorld(this.getWorld()) != Util.getWorld(user.getWorld())) {
            // Not a GameMode world.
            Utils.sendMessage(user, user.getTranslation("general.errors.wrong-world"));
            return false;
        }

        if (!((ChallengesAddon) this.getAddon()).getChallengesManager().hasAnyChallengeData(this.getWorld()))
        {
            // Do not open gui if there is no challenges.
            this.getAddon().logError("There are no challenges set up in " + this.getWorld() + "!");

            // Show admin better explanation.
            if (user.isOp() || user.hasPermission(this.getPermissionPrefix() + "admin.challenges"))
            {
                String topLabel = this.getIWM().getAddon(this.getWorld()).
                    map(GameModeAddon::getAdminCommand).
                    map(optionalAdminCommand -> optionalAdminCommand.map(CompositeCommand::getTopLabel).orElse(this.getTopLabel())).
                    orElse(this.getTopLabel());
                Utils.sendMessage(user, user.getTranslation("challenges.errors.no-challenges-admin",
                    "[command]",
                    topLabel + " " + this.<ChallengesAddon>getAddon().getChallengesSettings().getAdminMainCommand().split(" ")[0]));

            }
            else
            {
                Utils.sendMessage(user, user.getTranslation("challenges.errors.no-challenges"));
            }

            return false;
        }

        if (this.getIslands().getIsland(this.getWorld(), user) == null)
        {
            // Do not open gui if there is no island for this player.
            Utils.sendMessage(user, user.getTranslation("general.errors.no-island"));
            return false;
        } else if (ChallengesAddon.CHALLENGES_WORLD_PROTECTION.isSetForWorld(this.getWorld()) &&
            !this.getIslands().locationIsOnIsland(user.getPlayer(), user.getLocation()))
        {
            // Do not open gui if player is not on the island, but challenges requires island for
            // completion.
            Utils.sendMessage(user, user.getTranslation("challenges.errors.not-on-island"));
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
            ChallengesPanel.open(this.getAddon(),
                this.getWorld(),
                user,
                this.getTopLabel(),
                this.getPermissionPrefix());

            return true;
        }
        // Show help
        showHelp(this, user);
        return false;
    }


    @Override
    public void setup()
    {
        this.setPermission("challenges");
        this.setParametersHelp("challenges.commands.user.main.parameters");
        this.setDescription("challenges.commands.user.main.description");

        new CompleteChallengeCommand(this.getAddon(), this);
        this.setOnlyPlayer(true);
    }
}
