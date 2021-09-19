package world.bentobox.challenges.commands;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.tasks.TryToComplete;
import world.bentobox.challenges.utils.Utils;


/**
 * This command allows to complete challenges without a gui.
 */
public class CompleteChallengeCommand extends CompositeCommand
{
    /**
     * Default constructor for Composite Command.
     * @param addon Challenges addon.
     * @param cmd Parent Command.
     */
    public CompleteChallengeCommand(ChallengesAddon addon, CompositeCommand cmd)
    {
        super(addon,
            cmd,
            addon.getChallengesSettings().getPlayerCompleteCommand().split(" ")[0],
            addon.getChallengesSettings().getPlayerCompleteCommand().split(" "));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setup()
    {
        this.setOnlyPlayer(true);
        this.setPermission("challenges");
        this.setParametersHelp("challenges.commands.user.complete.parameters");
        this.setDescription("challenges.commands.user.complete.description");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute(User user, String label, List<String> args)
    {
        if (args.isEmpty())
        {
            user.sendMessage("challenges.errors.no-name");
            this.showHelp(this, user);
            return false;
        }
        else 
        {
            // Add world name back at the start
            String challengeName = Utils.getGameMode(this.getWorld()) + "_" + args.get(0);
            Challenge challenge = this.<ChallengesAddon>getAddon().getChallengesManager().getChallenge(challengeName);

            if (challenge != null)
            {
                int count = args.size() == 2 ? Integer.parseInt(args.get(1)) : 1;

                boolean canMultipleTimes =
                        user.hasPermission(this.getPermission() + ".multiple");

                if (!canMultipleTimes && count > 1)
                {
                    user.sendMessage("challenges.error.no-multiple-permission");
                    count = 1;
                }

                return TryToComplete.complete(this.getAddon(),
                        user,
                        challenge,
                        this.getWorld(),
                        this.getTopLabel(),
                        this.getPermissionPrefix(),
                        count);
            }
            else
            {
                user.sendMessage("challenges.errors.unknown-challenge");
                this.showHelp(this, user);
                return false;
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<List<String>> tabComplete(User user, String alias, List<String> args)
    {
        if (args.isEmpty()) return Optional.empty();

        String lastString = args.get(args.size() - 1);

        final List<String> returnList = new ArrayList<>();
        final int size = args.size();

        switch (size)
        {
        case 3:
            
            // Create suggestions with all challenges that is available for users.
            returnList.addAll(this.<ChallengesAddon>getAddon().getChallengesManager().getAllChallengesNames(this.getWorld()).
                stream().
                filter(challenge -> challenge.startsWith(Utils.getGameMode(this.getWorld()) + "_") ||
                    challenge.startsWith(Utils.getGameMode(this.getWorld()).toLowerCase() + "_")).
                map(challenge -> challenge.substring(Utils.getGameMode(this.getWorld()).length() + 1)).
                collect(Collectors.toList()));
            break;
        case 4:
            // Suggest a number of completions.
            if (lastString.isEmpty() || lastString.matches("[0-9]*"))
            {
                returnList.add("<number>");
            }

            break;
        default:
        {
            returnList.add("help");
            break;
        }
        }

        return Optional.of(Util.tabLimit(returnList, lastString));
    }
}
