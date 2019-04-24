package world.bentobox.challenges;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.DatabaseConnector;
import world.bentobox.bentobox.database.json.AbstractJSONDatabaseHandler;
import world.bentobox.bentobox.util.ItemParser;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.utils.GuiUtils;


/**
 * Imports challenges
 * @author tastybento
 *
 */
public class ChallengesImportManager
{

    private ChallengesAddon addon;
    private YamlConfiguration chal;

    /**
     * Import challenges from challenges.yml
     * @param challengesAddon
     */
    public ChallengesImportManager(ChallengesAddon challengesAddon) {
        this.addon = challengesAddon;
    }

    /**
     * Import challenges
     * @param user - user
     * @param world - world to import into
     * @param overwrite - true if previous ones should be overwritten
     * @return true if successful
     */
    public boolean importChallenges(User user, World world, boolean overwrite) {
        File challengeFile = new File(addon.getDataFolder(), "challenges.yml");
        if (!challengeFile.exists()) {
            user.sendMessage("challenges.errors.import-no-file");
            return false;
        }
        chal = new YamlConfiguration();
        try {
            chal.load(challengeFile);
        } catch (IOException | InvalidConfigurationException e) {
            user.sendMessage("challenges.errors.no-load","[message]", e.getMessage());
            return false;
        }
        makeLevels(user, world, overwrite);
        makeChallenges(user, world, overwrite);
        addon.getChallengesManager().save();
        return true;
    }

    private void makeLevels(User user, World world, boolean overwrite) {
        // Parse the levels
        String levels = chal.getString("challenges.levels", "");
        if (!levels.isEmpty()) {
            user.sendMessage("challenges.messages.import-levels");
            String[] lvs = levels.split(" ");
            int order = 0;
            for (String level : lvs) {
                ChallengeLevel challengeLevel = new ChallengeLevel();
                challengeLevel.setFriendlyName(level);
                challengeLevel.setUniqueId(Util.getWorld(world).getName() + "_" + level);
                challengeLevel.setOrder(order++);
                challengeLevel.setWorld(Util.getWorld(world).getName());
                challengeLevel.setWaiverAmount(chal.getInt("challenges.waiveramount"));
                // Check if there is a level reward
                ConfigurationSection unlock = chal.getConfigurationSection("challenges.levelUnlock." + level);
                if (unlock != null) {
                    challengeLevel.setUnlockMessage(unlock.getString("message", ""));
                    challengeLevel.setRewardText(unlock.getString("rewardDesc",""));
                    challengeLevel.setRewardItems(parseItems(unlock.getString("itemReward","")));
                    challengeLevel.setRewardMoney(unlock.getInt("moneyReward"));
                    challengeLevel.setRewardExperience(unlock.getInt("expReward"));
                    challengeLevel.setRewardCommands(unlock.getStringList("commands"));
                }
                addon.getChallengesManager().loadLevel(challengeLevel, overwrite, user, false);
            }
        } else {
            user.sendMessage("challenges.messages.no-levels");
        }
    }

    /**
     * Imports challenges
     * @param overwrite
     */
    private void makeChallenges(User user, World world, boolean overwrite) {
        int size = 0;
        // Parse the challenge file
        ConfigurationSection chals = chal.getConfigurationSection("challenges.challengeList");
        user.sendMessage("challenges.messages.import-challenges");

        for (String challenge : chals.getKeys(false)) {
            Challenge newChallenge = new Challenge();
            newChallenge.setUniqueId(Util.getWorld(world).getName() + "_" + challenge);
            newChallenge.setDeployed(true);
            ConfigurationSection details = chals.getConfigurationSection(challenge);
            newChallenge.setFriendlyName(details.getString("friendlyname", challenge));
            newChallenge.setDescription(GuiUtils.stringSplit(
                details.getString("description", ""),
                this.addon.getChallengesSettings().getLoreLineLength()));
            newChallenge.setIcon(ItemParser.parse(details.getString("icon", "") + ":1"));

            if (details.getString("type", "").equalsIgnoreCase("level"))
            {
                // Fix for older version config
                newChallenge.setChallengeType(Challenge.ChallengeType.OTHER);
            }
            else
            {
                newChallenge.setChallengeType(Challenge.ChallengeType.valueOf(details.getString("type","INVENTORY").toUpperCase()));
            }

            newChallenge.setTakeItems(details.getBoolean("takeItems",true));
            newChallenge.setRewardText(details.getString("rewardText", ""));
            newChallenge.setRewardCommands(details.getStringList("rewardcommands"));
            newChallenge.setRewardMoney(details.getInt("moneyReward",0));
            newChallenge.setRewardExperience(details.getInt("expReward"));
            newChallenge.setRepeatable(details.getBoolean("repeatable"));
            newChallenge.setRepeatRewardText(details.getString("repeatRewardText",""));
            newChallenge.setRepeatMoneyReward(details.getInt("repearMoneyReward"));
            newChallenge.setRepeatExperienceReward(details.getInt("repeatExpReward"));
            newChallenge.setRepeatRewardCommands(details.getStringList("repeatrewardcommands"));
            newChallenge.setMaxTimes(details.getInt("maxtimes"));
            // TODO reset allowed
            newChallenge.setRequiredMoney(details.getInt("requiredMoney"));
            newChallenge.setRequiredExperience(details.getInt("requiredExp"));
            String reqItems = details.getString("requiredItems","");
            if (newChallenge.getChallengeType().equals(Challenge.ChallengeType.INVENTORY)) {
                newChallenge.setRequiredItems(parseItems(reqItems));
            } else if (newChallenge.getChallengeType().equals(Challenge.ChallengeType.OTHER)) {
                newChallenge.setRequiredIslandLevel(Long.parseLong(reqItems));
            } else if (newChallenge.getChallengeType().equals(Challenge.ChallengeType.ISLAND)) {
                parseEntities(newChallenge, reqItems);
            }
            newChallenge.setRewardItems(parseItems(details.getString("itemReward", "")));
            newChallenge.setRepeatItemReward(parseItems(details.getString("repeatItemReward", "")));
            // Save
            this.addon.getChallengesManager().addChallengeToLevel(newChallenge,
                addon.getChallengesManager().getLevel(Util.getWorld(world).getName() + "_" + details.getString("level", "")));

            if (addon.getChallengesManager().loadChallenge(newChallenge, overwrite, user, false)) {
                size++;
            }
        }

        user.sendMessage("challenges.messages.import-number", "[number]", String.valueOf(size));
    }

    /**
     * Run through entity types and materials and try to match to the string given
     * @param challenge - challenge to be adjusted
     * @param string - string from YAML file
     */
    private void parseEntities(Challenge challenge, String string) {
        Map<EntityType, Integer> req = new EnumMap<>(EntityType.class);
        Map<Material, Integer> blocks = new EnumMap<>(Material.class);
        if (!string.isEmpty()) {
            for (String s : string.split(" ")) {
                String[] part = s.split(":");
                try {
                    Arrays.asList(EntityType.values()).stream().filter(t -> t.name().equalsIgnoreCase(part[0])).forEach(t -> req.put(t, Integer.valueOf(part[1])));
                    Arrays.asList(Material.values()).stream().filter(t -> t.name().equalsIgnoreCase(part[0])).forEach(t -> blocks.put(t, Integer.valueOf(part[1])));
                } catch (Exception e) {
                    addon.getLogger().severe("Cannot parse '" + s + "'. Skipping...");
                }
            }
        }
        challenge.setRequiredEntities(req);
        challenge.setRequiredBlocks(blocks);
    }

    private List<ItemStack> parseItems(String reqList) {
        List<ItemStack> result = new ArrayList<>();
        if (!reqList.isEmpty()) {
            for (String s : reqList.split(" ")) {
                ItemStack item = ItemParser.parse(s);
                if (item != null) {
                    result.add(item);
                }
            }
        }
        return result;
    }


// ---------------------------------------------------------------------
// Section: Default Challenge Loader
// ---------------------------------------------------------------------


    /**
     * This method loads default challenges into memory.
     * @param user User who calls default challenge loading
     * @param world Target world.
     * @return <code>true</code> if everything was successful, otherwise <code>false</code>.
     */
    private boolean loadDefaultChallenges(User user, World world)
    {
        ChallengesManager manager = this.addon.getChallengesManager();

        // If exist any challenge or level that is bound to current world, then do not load default challenges.
        if (manager.hasAnyChallengeData(world.getName()))
        {
            if (user.isPlayer())
            {
                user.sendMessage("challenges.errors.exist-challenges-or-levels");
            }
            else
            {
                this.addon.logWarning("challenges.errors.exist-challenges-or-levels");
            }

            return false;
        }

        // Safe json configuration to Challenges folder.
        this.addon.saveResource("default.json", true);

        // Init Connector
        CustomJSONConnector connector = new CustomJSONConnector();

        // Load challenges
        CustomJSONHandler<Challenge> challengeHandler =
            new CustomJSONHandler<>(BentoBox.getInstance(), Challenge.class, connector);

        // Load levels
        CustomJSONHandler<ChallengeLevel> levelHandler =
            new CustomJSONHandler<>(BentoBox.getInstance(), ChallengeLevel.class, connector);

        try
        {
            challengeHandler.loadObjects().forEach(challenge -> manager.loadChallenge(challenge, false, user, false));
            levelHandler.loadObjects().forEach(level -> manager.loadLevel(level, false, user, false));
        }
        catch (NoSuchMethodException |
            ClassNotFoundException |
            IllegalAccessException |
            IntrospectionException |
            InvocationTargetException |
            InstantiationException e)
        {
            e.printStackTrace();
            return false;
        }

        this.addon.getChallengesManager().save();

        // Remove default.yml file from resources to avoid interacting with it.
        new File(this.addon.getDataFolder(), "default.json").delete();

        return true;
    }


    /**
     * This Class allows to load default challenges and their levels as objects much easier.
     */
    private final class CustomJSONHandler<T> extends AbstractJSONDatabaseHandler<T>
    {
        /**
         * {@inheritDoc}
         */
        CustomJSONHandler(BentoBox plugin, Class<T> type, DatabaseConnector databaseConnector)
        {
            super(plugin, type, databaseConnector);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public List<T> loadObjects() throws
            InstantiationException,
            IllegalAccessException,
            InvocationTargetException,
            ClassNotFoundException,
            IntrospectionException,
            NoSuchMethodException
        {
            return Collections.emptyList();
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public T loadObject(@NonNull String s) throws
            InstantiationException,
            IllegalAccessException,
            InvocationTargetException,
            ClassNotFoundException,
            IntrospectionException,
            NoSuchMethodException
        {
            return null;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public void saveObject(T t)
            throws IllegalAccessException, InvocationTargetException, IntrospectionException
        {
            // Will not be used in default challenge loading process.
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public void deleteObject(T t)
            throws IllegalAccessException, InvocationTargetException, IntrospectionException
        {
            // Will not be used in default challenge loading process.
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public boolean objectExists(String s)
        {
            // Will not be used in default challenge loading process.
            return false;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public void close()
        {
            // Will not be used in default challenge loading process.
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public void deleteID(String s)
        {
            // Will not be used in default challenge loading process.
        }
    }


    /**
     * Simple custom connector used for importing default challenges
     */
    private final class CustomJSONConnector implements DatabaseConnector
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public Object createConnection()
        {
            return null;
        }



        /**
         * {@inheritDoc}
         */
        @Override
        public void closeConnection()
        {

        }


        /**
         * {@inheritDoc}
         */
        @Override
        public String getConnectionUrl()
        {
            return null;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public String getUniqueId(String s)
        {
            return null;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public boolean uniqueIdExists(String s, String s1)
        {
            return false;
        }
    }
}