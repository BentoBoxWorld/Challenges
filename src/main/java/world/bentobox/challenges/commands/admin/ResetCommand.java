package world.bentobox.challenges.commands.admin;


import java.util.*;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.tasks.TryToComplete;


/**
 * This command allows to reset challenges without a gui.
 */
public class ResetCommand extends CompositeCommand
{
    /**
     * Default constructor for Composite Command.
     * @param addon Challenges addon.
     * @param cmd Parent Command.
     */
    public ResetCommand(Addon addon, CompositeCommand cmd)
    {
        super(addon, cmd, "reset");
        this.addon = (ChallengesAddon) addon;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setup()
    {
        this.setPermission("reset");
        this.setParametersHelp("challenges.commands.admin.reset.parameters");
        this.setDescription("challenges.commands.admin.reset.description");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute(User user, String label, List<String> args)
    {
        if (args.isEmpty())
        {
            if (user.isPlayer())
            {
                user.sendMessage("challenges.errors.no-name");
            }
            else
            {
                this.addon.logError("Missing parameters");
            }
        }
        else if (args.size() < 2)
        {
            if (user.isPlayer())
            {
                user.sendMessage("challenges.errors.missing-arguments");
            }
            else
            {
                this.addon.logError("Missing parameters");
            }
        }
        else if (!args.get(1).isEmpty())
        {
            UUID targetUUID = this.getPlayers().getUUID(args.get(0));

            if (targetUUID == null)
            {
                if (user.isPlayer())
                {
                    user.sendMessage("general.errors.unknown-player", TextVariables.NAME, args.get(0));
                }
                else
                {
                    this.addon.logError("Unknonw player name " + args.get(0));
                }

                return false;
            }

            // Add world name back at the start

            if (args.get(1).equals("all"))
            {
                this.addon.getChallengesManager().resetAllChallenges(targetUUID, this.getWorld(), user.getUniqueId());

                if (user.isPlayer())
                {
                    user.sendMessage("challenges.messages.admin.reset-all",
                        "[player]", User.getInstance(targetUUID).getName());
                }
                else
                {
                    this.addon.log("All challenges for user " +
                        User.getInstance(targetUUID).getName() + " was reset!");
                }

                return true;
            }
            else
            {
                String challengeName = Util.getWorld(this.getWorld()).getName() + "_" + args.get(1);
                Challenge challenge = this.addon.getChallengesManager().getChallenge(challengeName);

                if (challenge != null)
                {
                    if (this.addon.getChallengesManager().isChallengeComplete(targetUUID, this.getWorld(), challenge))
                    {
                        this.addon.getChallengesManager().resetChallenge(targetUUID, this.getWorld(), challenge, user.getUniqueId());

                        if (user.isPlayer())
                        {
                            user.sendMessage("challenges.messages.admin.reset",
                                    "[name]", challenge.getFriendlyName(),
                                    "[player]", User.getInstance(targetUUID).getName());
                        }
                        else
                        {
                            this.addon.log("Challenge " + challenge.getFriendlyName() + " was reset for player " +
                                    User.getInstance(targetUUID).getName());
                        }
                    }
                    else
                    {
                        if (user.isPlayer())
                        {
                            user.sendMessage("challenges.messages.admin.not-completed");
                        }
                        else
                        {
                            this.addon.log("Challenge is not completed yet");
                        }
                    }

                    return true;
                }
                else
                {
                    if (user.isPlayer())
                    {
                        user.sendMessage("challenges.errors.unknown-challenge");
                    }
                    else
                    {
                        this.addon.logError("Unknown challenge " + args.get(1));
                    }

                    return false;
                }
            }
        }

        this.showHelp(this, user);
        return false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<List<String>> tabComplete(User user, String alias, List<String> args)
    {
        String lastString = args.get(args.size() - 1);

        final List<String> returnList = new ArrayList<>();
        final int size = args.size();

        switch (size)
        {
            case 3:
                // Create suggestions with all challenges that is available for users.

                returnList.addAll(Util.getOnlinePlayerList(user));
                break;
            case 4:
                // Create suggestions with all challenges that is available for users.
                returnList.addAll(this.addon.getChallengesManager().getAllChallengesNames(this.getWorld()).stream().
                    map(challenge -> challenge.replaceFirst(Util.getWorld(this.getWorld()).getName() + "_", "")).
                    collect(Collectors.toList()));

                returnList.add("all");

                break;
            default:
            {
                returnList.addAll(Collections.singletonList("help"));
                break;
            }
        }

        return Optional.of(Util.tabLimit(returnList, lastString));
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * Variable that holds challenge addon. Single casting.
     */
    private ChallengesAddon addon;
}
