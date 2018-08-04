package bentobox.addon.challenges.commands.admin;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import bentobox.addon.challenges.ChallengesAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;

public class ChallengesAdminImportCommand extends CompositeCommand {

    private ChallengesAddon addon;

    /**
     * Import challenges
     * @param addon
     * @param cmd
     */
    public ChallengesAdminImportCommand(ChallengesAddon addon, CompositeCommand cmd) {
        super(cmd, "cimport");
        this.addon = addon;
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        return addon.getImportManager().importChallenges(user, getWorld(), !args.isEmpty() && args.get(0).equalsIgnoreCase("overwrite"));
    }

    @Override
    public void setup() {
        this.setPermission("challenges.admin");
        this.setParameters("challenges.admin.import.parameters");
        this.setDescription("challenges.admin.import.description");
    }

    @Override
    public Optional<List<String>> tabComplete(User user, String alias, List<String> args) {
        String lastArg = !args.isEmpty() ? args.get(args.size()-1) : "";
        return Optional.of(Util.tabLimit(Arrays.asList("overwrite"), lastArg));
    }

}
