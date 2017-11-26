package bskyblock.addin.challenges.commands;

import java.util.Set;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import bskyblock.addin.challenges.Challenges;
import us.tastybento.bskyblock.api.commands.AbstractCommand;
import us.tastybento.bskyblock.api.commands.ArgumentHandler;
import us.tastybento.bskyblock.api.commands.CanUseResp;
import us.tastybento.bskyblock.config.Settings;
import us.tastybento.bskyblock.util.VaultHelper;

public class ChallengesCommand extends AbstractCommand implements Listener {
    private static final String CHALLENGE_COMMAND = "challenges";
    private Challenges plugin;

    public ChallengesCommand(Challenges plugin) {
        super(CHALLENGE_COMMAND, new String[]{"c", "challenge"}, true);
        plugin.getCommand(CHALLENGE_COMMAND).setExecutor(this);
        plugin.getCommand(CHALLENGE_COMMAND).setTabCompleter(this);
        this.plugin = plugin;
    }

    @Override
    public CanUseResp canUse(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return new CanUseResp(getLocale(sender).get("general.errors.use-in-game"));
        }

        // Basic permission check to use /challenges
        if (!VaultHelper.hasPerm(player, Settings.PERMPREFIX + "island.challenges")) {
            return new CanUseResp(getLocale(sender).get("general.errors.no-permission"));
        }

        return new CanUseResp(true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Open up the challenges GUI
        if (isPlayer) {
            player.openInventory(plugin.getManager().getChallenges(player));
        } else {
            // TODO
        }
    }

    @Override
    public void setup() {
        addArgument(new ArgumentHandler(label) {

            @Override
            public CanUseResp canUse(CommandSender sender) {
                return new CanUseResp(true);
            }

            @Override
            public void execute(CommandSender sender, String[] args) {
                // Create a copy of items in the player's main inventory
                Player player = (Player)sender;
                String name = UUID.randomUUID().toString();
                if (args.length > 0) {
                    name = args[0];
                }
                plugin.getManager().createChallenge(player, name);
            }

            @Override
            public Set<String> tabComplete(CommandSender sender, String[] args) {
                return null;
            }

            @Override
            public String[] usage(CommandSender sender) {
                return new String[] {null, "Make a challenge from the items in your inventory"};
            }
        }.alias("make"));
        
    }


}
