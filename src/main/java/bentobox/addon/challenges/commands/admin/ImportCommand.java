package bentobox.addon.challenges.commands.admin;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import bentobox.addon.challenges.ChallengesAddon;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;

public class ImportCommand extends CompositeCommand {

    /**
     * Import challenges
     * @param addon
     * @param cmd
     */
    public ImportCommand(Addon addon, CompositeCommand cmd) {
        super(addon, cmd, "import");
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        return ((ChallengesAddon)getAddon()).getImportManager().importChallenges(user, getWorld(), !args.isEmpty() && args.get(0).equalsIgnoreCase("overwrite"));
    }

    @Override
    public void setup() {
        this.setPermission("challenges.admin");
        this.setParametersHelp("challenges.admin.import.parameters");
        this.setDescription("challenges.admin.import.description");
    }

    @Override
    public Optional<List<String>> tabComplete(User user, String alias, List<String> args) {
        String lastArg = !args.isEmpty() ? args.get(args.size()-1) : "";
        return Optional.of(Util.tabLimit(Arrays.asList("overwrite"), lastArg));
    }

}
