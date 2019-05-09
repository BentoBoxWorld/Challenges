package world.bentobox.challenges.commands.admin;


import java.util.Collections;
import java.util.List;
import java.util.Optional;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.ChallengesAddon;


/**
 * This method generates default challenges file.
 */
public class DefaultsCommand extends CompositeCommand
{

    /**
     * Constructor that inits generate defaults command.
     *
     * @param addon Addon that inits this command
     * @param cmd Parent command
     */
    public DefaultsCommand(Addon addon, CompositeCommand cmd)
    {
        super(addon, cmd, "defaults");
        this.addon = (ChallengesAddon) addon;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setup()
    {
        this.setPermission("admin.challenges");
        this.setParametersHelp("challenges.commands.admin.defaults.parameters");
        this.setDescription("challenges.commands.admin.defaults.description");

        // Register sub commands
        // This method reloads challenges addon
        new ImportCommand(this);
        // Import ASkyBlock Challenges
        new GenerateCommand(this);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute(User user, String label, List<String> args)
    {
        return this.showHelp(this, user);
    }


// ---------------------------------------------------------------------
// Section: Private Classes
// ---------------------------------------------------------------------


    /**
     * This class allows to process import command.
     */
    private class ImportCommand extends CompositeCommand
    {
        /**
         * Default constructor for import method.
         * @param parent composite command
         */
        private ImportCommand(CompositeCommand parent)
        {
            super(DefaultsCommand.this.addon, parent, "import");
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public void setup()
        {
            this.setPermission("admin.challenges");
            this.setParametersHelp("challenges.commands.admin.defaults-import.parameters");
            this.setDescription("challenges.commands.admin.defaults-import.description");
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public boolean execute(User user, String label, List<String> args)
        {
            return DefaultsCommand.this.addon.getImportManager().loadDefaultChallenges(user, this.getWorld());
        }
    }


    /**
     * This class allows to process generate command.
     */
    private class GenerateCommand extends CompositeCommand
    {
        /**
         * Default constructor for generate method.
         * @param parent composite command
         */
        private GenerateCommand(CompositeCommand parent)
        {
            super(DefaultsCommand.this.addon, parent, "generate");
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public void setup()
        {
            this.setPermission("admin.challenges");
            this.setParametersHelp("challenges.commands.admin.defaults-generate.parameters");
            this.setDescription("challenges.commands.admin.defaults-generate.description");
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public boolean execute(User user, String label, List<String> args)
        {
            return DefaultsCommand.this.addon.getImportManager().generateDefaultChallengeFile(
                user,
                this.getWorld(),
                !args.isEmpty() && args.get(0).equalsIgnoreCase("overwrite"));
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<List<String>> tabComplete(User user, String alias, List<String> args)
        {
            String lastArg = !args.isEmpty() ? args.get(args.size() - 1) : "";
            return Optional.of(Util.tabLimit(Collections.singletonList("overwrite"), lastArg));
        }
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * Holds challenges addon as variable.
     */
    private ChallengesAddon addon;
}
