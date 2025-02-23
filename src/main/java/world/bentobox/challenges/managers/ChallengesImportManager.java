package world.bentobox.challenges.managers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.json.BentoboxTypeAdapterFactory;
import world.bentobox.bentobox.database.json.adapters.TagTypeAdapterFactory;
import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.util.ItemParser;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.ChallengesAddon;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.database.object.requirements.InventoryRequirements;
import world.bentobox.challenges.database.object.requirements.IslandRequirements;
import world.bentobox.challenges.database.object.requirements.OtherRequirements;
import world.bentobox.challenges.database.object.requirements.StatisticRequirements;
import world.bentobox.challenges.database.object.requirements.StatisticRequirements.StatisticRec;
import world.bentobox.challenges.utils.Constants;
import world.bentobox.challenges.utils.Utils;


/**
 * Imports challenges
 * @author BONNe1704
 *
 */
public class ChallengesImportManager
{
    /**
     * Import challenges from file or link.
     * @param challengesAddon Challenges addon.
     */
    public ChallengesImportManager(ChallengesAddon challengesAddon)
    {
        this.addon = challengesAddon;
    }


    // ---------------------------------------------------------------------
    // Section: YAML Importers
    // ---------------------------------------------------------------------


    /**
     * This method imports generator tiers from template
     *
     * @param user - user
     * @param world - world to import into
     * @param file - file that must be imported
     */
    public void importFile(@Nullable User user, World world, String file)
    {
        File generatorFile = new File(this.addon.getDataFolder(), file.endsWith(".yml") ? file : file + ".yml");

        if (!generatorFile.exists())
        {
            if (user != null)
            {
                Utils.sendMessage(user,
                    world,
                    Constants.ERRORS + "no-file",
                    Constants.PARAMETER_FILE,
                    file);
            }

            return;
        }

        YamlConfiguration config = new YamlConfiguration();

        try
        {
            config.load(generatorFile);
        }
        catch (IOException | InvalidConfigurationException e)
        {
            if (user != null)
            {
                Utils.sendMessage(user,
                    world,
                    Constants.ERRORS + "no-load",
                    Constants.PARAMETER_FILE, file,
                    TextVariables.DESCRIPTION, e.getMessage());
            }

            this.addon.logError("Exception when loading file. " + e.getMessage());

            return;
        }

        Optional<GameModeAddon> optional = this.addon.getPlugin().getIWM().getAddon(world);

        if (optional.isEmpty())
        {
            if (user != null)
            {
                Utils.sendMessage(user,
                    world,
                    Constants.ERRORS + "not-a-gamemode-world",
                    Constants.PARAMETER_WORLD, world.getName());
            }

            this.addon.logWarning("Given world is not a gamemode world.");

            return;
        }

        this.addon.getChallengesManager().wipeDatabase(optional.get().getDescription().getName().toLowerCase());
        this.createChallenges(config, user, optional.get(), world);
    }


    /**
     * This method creates generator tier object from config file.
     *
     * @param config YamlConfiguration that contains all generators.
     * @param user User who calls reading.
     * @param gameMode GameMode in which generator tiers must be imported
     */
    private void createChallenges(YamlConfiguration config, @Nullable User user, GameModeAddon gameMode, World world)
    {
        final String prefix = gameMode.getDescription().getName().toLowerCase() + "_";

        long challengeCount = 0;
        long levelCount = 0;

        if (config.contains("challenges"))
        {
            ConfigurationSection reader = config.getConfigurationSection("challenges");

            if (reader != null)
            {
               challengeCount = reader.getKeys(false).stream().
                   mapToInt(challengeId -> this.createChallenge(challengeId,
                       prefix,
                       world,
                       reader.getConfigurationSection(challengeId))).
                   sum();
            }
        }

        if (config.contains("levels"))
        {
            ConfigurationSection reader = config.getConfigurationSection("levels");

            if (reader != null)
            {
                levelCount = reader.getKeys(false).stream().
                    mapToInt(levelId -> this.createLevel(levelId,
                        prefix,
                        world,
                        reader.getConfigurationSection(levelId))).
                    sum();
            }
        }

        if (user != null)
        {
            Utils.sendMessage(user,
                world,
                Constants.MESSAGES + "import-count",
                "[levels]", String.valueOf(levelCount),
                "[challenges]", String.valueOf(challengeCount));
        }

        this.addon.log("Imported " + challengeCount + " challenges and " +
            levelCount + " levels into database.");
    }


    /**
     * This method creates challenge from given config section.
     * @param challengeId Challenge ID.
     * @param prefix GameMode prefix.
     * @param world world where challenge is created.
     * @param section Configuration Section that contains information.
     * @return 1 if challenge is created, otherwise 0.
     */
    private int createChallenge(String challengeId,
        String prefix,
        World world,
        @Nullable ConfigurationSection section)
    {
        if (section == null)
        {
            return 0;
        }

        try
        {
            Challenge challenge = new Challenge();
            challenge.setUniqueId(prefix + challengeId);

            challenge.setFriendlyName(section.getString("name", challengeId));
            challenge.setIcon(matchIcon(section.getString("icon"), new ItemStack(Material.PAPER)));

            // Read description
            if (section.isList("description"))
            {
                challenge.setDescription(section.getStringList("description"));
            }
            else if (section.isString("description"))
            {
                String description = section.getString("description");

                if (description != null)
                {
                    // Define as list.
                    challenge.setDescription(Arrays.asList(
                        description.replaceAll("\\|", "\n").
                            split("\n").clone()));
                }
            }

            challenge.setDeployed(section.getBoolean("deployed", true));
            challenge.setOrder(section.getInt("order", 0));
            challenge.setChallengeType(matchChallengeType(section.getString("type"),
                Challenge.ChallengeType.ISLAND_TYPE));

            // Read environment
            Set<World.Environment> environments = new HashSet<>();
            challenge.setEnvironment(environments);

            if (section.isList("environments"))
            {
                section.getStringList("environments").
                    forEach(text -> environments.add(matchEnvironment(text,
                        World.Environment.NORMAL)));
            }
            else if (section.isString("environments"))
            {
                environments.add(matchEnvironment(section.getString("environments"),
                    World.Environment.NORMAL));
            }

            challenge.setRemoveWhenCompleted(section.getBoolean("remove-completed", false));

            // Read Requirements
            this.populateRequirements(challenge, section.getConfigurationSection("requirements"));
            // Read Rewards
            this.populateRewards(challenge, section.getConfigurationSection("rewards"));

            // Check Repeating status
            challenge.setRepeatable(section.getBoolean("repeatable", false));
            challenge.setMaxTimes(section.getInt("repeat-times", -1));

            if (challenge.isRepeatable())
            {
                // Read Repeat Rewards
                this.populateRepeatRewards(challenge,
                    section.getConfigurationSection("repeat-rewards"));
            }

            this.addon.getChallengesManager().saveChallenge(challenge);
            this.addon.getChallengesManager().loadChallenge(challenge, world, true, null, true);
        }
        catch (Exception e)
        {
            return 0;
        }

        return 1;
    }


    /**
     * Populates requirements for the given challenge.
     *
     * @param challenge the challenge
     * @param section the section
     */
    private void populateRequirements(Challenge challenge, ConfigurationSection section)
    {
        switch (challenge.getChallengeType())
        {
            case INVENTORY_TYPE -> {
                InventoryRequirements requirements = new InventoryRequirements();
                challenge.setRequirements(requirements);

                requirements.setTakeItems(section.getBoolean("take-items", false));
                List<ItemStack> requiredItems = new ArrayList<>();
                requirements.setRequiredItems(requiredItems);

                if (section.isList("items"))
                {
                    section.getStringList("items").
                        forEach(text -> {
                            ItemStack itemStack = ItemParser.parse(text);

                            if (itemStack != null)
                            {
                                requiredItems.add(itemStack);
                            }
                        });
                }
            }
            case ISLAND_TYPE -> {
                IslandRequirements requirements = new IslandRequirements();
                challenge.setRequirements(requirements);

                requirements.setRemoveBlocks(section.getBoolean("remove-blocks", false));
                requirements.setRequiredBlocks(this.createMaterialMap(section.getConfigurationSection("blocks")));

                requirements.setRemoveEntities(section.getBoolean("remove-entities", false));
                requirements.setRequiredEntities(this.createEntityMap(section.getConfigurationSection("entities")));

                requirements.setSearchRadius(section.getInt("search-distance", 10));
            }
            case OTHER_TYPE -> {
                OtherRequirements requirements = new OtherRequirements();
                challenge.setRequirements(requirements);

                requirements.setTakeMoney(section.getBoolean("take-money", false));
                requirements.setRequiredMoney(section.getDouble("money", 0));

                requirements.setTakeExperience(section.getBoolean("take-experience", false));
                requirements.setRequiredExperience(section.getInt("experience", 0));

                requirements.setRequiredIslandLevel(section.getInt("level", 0));
            }
            case STATISTIC_TYPE -> {
                StatisticRequirements requirements = new StatisticRequirements();
                challenge.setRequirements(requirements);
                List<StatisticRec> list = new ArrayList<>();
                list.add(new StatisticRec(matchStatistic(section.getString("statistic")),
                        matchEntity(section.getString("entity")), matchMaterial(section.getString("material")),
                        section.getInt("amount", 0), section.getBoolean("reduce", false)));
                // TODO: Add support for multiple stat challenge
                requirements.setStatisticList(list);
            }
        }

        // Read permissions
        if (challenge.getRequirements() != null)
        {
            Set<String> permissions = new HashSet<>();
            challenge.getRequirements().setRequiredPermissions(permissions);

            if (section.isList("permissions"))
            {
                permissions.addAll(section.getStringList("permissions"));
            }
            else if (section.isString("permissions"))
            {
                String description = section.getString("permissions");

                if (description != null)
                {
                    // Define as list.
                    permissions.addAll(Arrays.asList(
                        description.replaceAll("\\|", "\n").
                            split("\n").clone()));
                }
            }
        }
    }


    /**
     * This method populates material map from given section field.
     * @param section Section that contains material.
     * @return Map that links material and number.
     */
    private Map<Material, Integer> createMaterialMap(ConfigurationSection section)
    {
        Map<Material, Integer> materialMaps = new HashMap<>();

        if (section != null)
        {
            for (String materialKey : section.getKeys(false))
            {
                Material material = matchMaterial(materialKey);

                if (material != null)
                {
                    materialMaps.put(material, section.getInt(materialKey, 0));
                }
            }
        }

        return materialMaps;
    }


    /**
     * This method populates entity map from given section field.
     * @param section Section that contains material.
     * @return Map that links entity and number.
     */
    private Map<EntityType, Integer> createEntityMap(ConfigurationSection section)
    {
        Map<EntityType, Integer> entityMap = new HashMap<>();

        if (section != null)
        {
            for (String EntityType : section.getKeys(false))
            {
                EntityType entity = matchEntity(EntityType);

                if (entity != null)
                {
                    entityMap.put(entity, section.getInt(EntityType, 0));
                }
            }
        }

        return entityMap;
    }


    /**
     * This method populates rewards for a challenge.
     * @param challenge Challenge
     * @param section Section that contains rewards
     */
    private void populateRewards(Challenge challenge, @Nullable ConfigurationSection section)
    {
        List<ItemStack> rewardItems = new ArrayList<>();
        challenge.setRewardItems(rewardItems);

        if (section == null)
        {
            return;
        }

        challenge.setRewardText(section.getString("text", ""));

        if (section.isList("items"))
        {
            section.getStringList("items").
                forEach(text -> {
                    ItemStack itemStack = ItemParser.parse(text);

                    if (itemStack != null)
                    {
                        rewardItems.add(itemStack);
                    }
                });
        }

        challenge.setRewardExperience(section.getInt("experience", 0));
        challenge.setRewardMoney(section.getDouble("money", 0));

        if (section.isList("commands"))
        {
            challenge.setRewardCommands(section.getStringList("commands"));
        }
        else if (section.isString("commands"))
        {
            String description = section.getString("commands");

            if (description != null)
            {
                // Define as list.
                challenge.setRewardCommands(Arrays.asList(
                    description.replaceAll("\\|", "\n").
                        split("\n").clone()));
            }
        }
    }


    /**
     * This method populates repeat rewards for a challenge.
     * @param challenge Challenge
     * @param section Section that contains rewards
     */
    private void populateRepeatRewards(Challenge challenge, @Nullable ConfigurationSection section)
    {
        List<ItemStack> rewardItems = new ArrayList<>();
        challenge.setRepeatItemReward(rewardItems);

        if (section == null)
        {
            return;
        }

        challenge.setRepeatRewardText(section.getString("text", ""));

        if (section.isList("items"))
        {
            section.getStringList("items").
                forEach(text -> {
                    ItemStack itemStack = ItemParser.parse(text);

                    if (itemStack != null)
                    {
                        rewardItems.add(itemStack);
                    }
                });
        }

        challenge.setRepeatExperienceReward(section.getInt("experience", 0));
        challenge.setRepeatMoneyReward(section.getDouble("money", 0));

        if (section.isList("commands"))
        {
            challenge.setRepeatRewardCommands(section.getStringList("commands"));
        }
        else if (section.isString("commands"))
        {
            String description = section.getString("commands");

            if (description != null)
            {
                // Define as list.
                challenge.setRepeatRewardCommands(Arrays.asList(
                    description.replaceAll("\\|", "\n").
                        split("\n").clone()));
            }
        }
    }


    /**
     * This method populates rewards for a level.
     * @param level level
     * @param section Section that contains rewards
     */
    private void populateRewards(ChallengeLevel level, @Nullable ConfigurationSection section)
    {
        List<ItemStack> rewardItems = new ArrayList<>();
        level.setRewardItems(rewardItems);

        if (section == null)
        {
            return;
        }

        level.setRewardText(section.getString("text", ""));

        if (section.isList("items"))
        {
            section.getStringList("items").
                forEach(text -> {
                    ItemStack itemStack = ItemParser.parse(text);

                    if (itemStack != null)
                    {
                        rewardItems.add(itemStack);
                    }
                });
        }

        level.setRewardExperience(section.getInt("experience", 0));
        level.setRewardMoney(section.getDouble("money", 0));

        if (section.isList("commands"))
        {
            level.setRewardCommands(section.getStringList("commands"));
        }
        else if (section.isString("commands"))
        {
            String description = section.getString("commands");

            if (description != null)
            {
                // Define as list.
                level.setRewardCommands(Arrays.asList(
                    description.replaceAll("\\|", "\n").
                        split("\n").clone()));
            }
        }
    }


    /**
     * This method creates Level
     * @param levelId Level Id
     * @param prefix Gamemode prefix
     * @param world World where level operates.
     * @param section Section that contains level info.
     * @return 1 if level created, 0 otherwise.
     */
    private int createLevel(String levelId,
        String prefix,
        World world,
        @Nullable ConfigurationSection section)
    {
        if (section == null)
        {
            return 0;
        }

        try
        {
            ChallengeLevel level = new ChallengeLevel();
            level.setUniqueId(prefix + levelId);

            level.setFriendlyName(section.getString("name", levelId));
            level.setIcon(matchIcon(section.getString("icon"), new ItemStack(Material.PAPER)));
            level.setLockedIcon(matchIcon(section.getString("icon")));

            level.setWorld(world.getName());

            level.setOrder(section.getInt("order", 0));
            level.setWaiverAmount(section.getInt("waiver", 0));

            level.setUnlockMessage(section.getString("description", ""));

            this.populateRewards(level, section.getConfigurationSection("rewards"));

            Set<String> challenges = new HashSet<>();
            level.setChallenges(challenges);

            if (section.isList("challenges"))
            {
                section.getStringList("challenges").forEach(text -> {
                    Challenge challenge = this.addon.getChallengesManager().getChallenge(prefix + text);

                    if (challenge != null)
                    {
                        challenges.add(challenge.getUniqueId());
                        this.addon.getChallengesManager().addChallengeToLevel(challenge, level);
                    }
                });
            }

            this.addon.getChallengesManager().saveLevel(level);
            this.addon.getChallengesManager().loadLevel(level, world,true, null, true);
        }
        catch (Exception ignored)
        {
            return 0;
        }

        return 1;
    }


    // ---------------------------------------------------------------------
    // Section: JSON Importers
    // ---------------------------------------------------------------------


    /**
     * Import database file from local storage.
     *
     * @param user the user
     * @param world the world
     * @param fileName the file name
     */
    public void importDatabaseFile(User user, World world, String fileName)
    {
        World correctWorld = Util.getWorld(world);

        if (correctWorld == null)
        {
            this.addon.logError("Given world is not part of BentoBox");
            return;
        }

        ChallengesManager manager = this.addon.getChallengesManager();

        // If exist any generator that is bound to current world, then do not load generators.
        if (manager.hasAnyChallengeData(world.getName()))
        {
            this.addon.getPlugin().getIWM().getAddon(world).ifPresent(gameModeAddon ->
                manager.wipeDatabase(gameModeAddon.getDescription().getName().toLowerCase()));
        }

        try
        {
            // This prefix will be used to all generators. That is a unique way how to separate generators for
            // each game mode.
            String uniqueIDPrefix = Utils.getGameMode(world).toLowerCase() + "_";
            DefaultDataHolder downloadedChallenges = new DefaultJSONHandler(this.addon).loadObject(fileName);

            if (downloadedChallenges == null)
            {
                return;
            }

            // All new challenges should get correct ID. So we need to map it to loaded challenges.
            downloadedChallenges.getChallengeList().forEach(challenge -> {
                // Set correct challenge ID
                challenge.setUniqueId(uniqueIDPrefix + challenge.getUniqueId());
                // Set up correct level ID if it is necessary
                if (!challenge.getLevel().isEmpty())
                {
                    challenge.setLevel(uniqueIDPrefix + challenge.getLevel());
                }
                // Load challenge in memory
                manager.loadChallenge(challenge, world, false, user, user == null);
            });

            downloadedChallenges.getLevelList().forEach(challengeLevel -> {
                // Set correct level ID
                challengeLevel.setUniqueId(uniqueIDPrefix + challengeLevel.getUniqueId());
                // Set correct world name
                challengeLevel.setWorld(correctWorld.getName());
                // Reset names for all challenges.
                challengeLevel.setChallenges(challengeLevel.getChallenges().stream().
                    map(challenge -> uniqueIDPrefix + challenge).
                    collect(Collectors.toSet()));
                // Load level in memory
                manager.loadLevel(challengeLevel, world, false, user, user == null);
            });
        }
        catch (Exception e)
        {
            this.addon.getPlugin().logStacktrace(e);
            return;
        }

        manager.saveChallenges();
        manager.saveLevels();
    }


    /**
     * This method loads downloaded challenges into memory.
     * @param user User who calls downloaded challenge loading
     * @param world Target world.
     * @param downloadString String that need to be loaded via DefaultDataHolder.
     */
    public void loadDownloadedChallenges(User user, World world, String downloadString)
    {
        World correctWorld = Util.getWorld(world);

        if (correctWorld == null)
        {
            this.addon.logError("Given world is not part of BentoBox");
            return;
        }

        ChallengesManager manager = this.addon.getChallengesManager();

        // If exist any challenge or level that is bound to current world, then do not load default challenges.
        if (manager.hasAnyChallengeData(world.getName()))
        {
            if (user.isPlayer())
            {
                Utils.sendMessage(user, world, Constants.ERRORS + "exist-challenges-or-levels");
            }
            else
            {
                this.addon.logWarning("challenges.errors.exist-challenges-or-levels");
            }

            return;
        }

        try
        {
            // This prefix will be used to all challenges. That is a unique way how to separate challenged for
            // each game mode.
            String uniqueIDPrefix = Utils.getGameMode(world).toLowerCase() + "_";
            DefaultDataHolder downloadedChallenges = new DefaultJSONHandler(this.addon).loadWebObject(downloadString);

            // All new challenges should get correct ID. So we need to map it to loaded challenges.
            downloadedChallenges.getChallengeList().forEach(challenge -> {
                // Set correct challenge ID
                challenge.setUniqueId(uniqueIDPrefix + challenge.getUniqueId());
                // Set up correct level ID if it is necessary
                if (!challenge.getLevel().isEmpty())
                {
                    challenge.setLevel(uniqueIDPrefix + challenge.getLevel());
                }
                // Load challenge in memory
                manager.loadChallenge(challenge, world, false, user, user == null);
            });

            downloadedChallenges.getLevelList().forEach(challengeLevel -> {
                // Set correct level ID
                challengeLevel.setUniqueId(uniqueIDPrefix + challengeLevel.getUniqueId());
                // Set correct world name
                challengeLevel.setWorld(correctWorld.getName());
                // Reset names for all challenges.
                challengeLevel.setChallenges(challengeLevel.getChallenges().stream().
                        map(challenge -> uniqueIDPrefix + challenge).
                        collect(Collectors.toSet()));
                // Load level in memory
                manager.loadLevel(challengeLevel, world, false, user, user == null);
            });
        }
        catch (Exception e)
        {
            this.addon.getPlugin().logStacktrace(e);
            return;
        }

        this.addon.getChallengesManager().saveChallenges();
        this.addon.getChallengesManager().saveLevels();
    }


    // ---------------------------------------------------------------------
    // Section: Default generation
    // ---------------------------------------------------------------------


    public void generateDatabaseFile(User user, World world, String fileName)
    {
        File defaultFile = new File(this.addon.getDataFolder(),
            fileName.endsWith(".json") ? fileName : fileName + ".json");

        if (defaultFile.exists())
        {
            if (user.isPlayer())
            {
                Utils.sendMessage(user,
                    world,
                    Constants.ERRORS + "file-exist",
                    Constants.PARAMETER_FILE, fileName);
            }
            else
            {
                this.addon.logWarning(Constants.ERRORS + "file-exist");
            }

            return;
        }

        try
        {
            if (defaultFile.createNewFile())
            {
                String replacementString = Utils.getGameMode(world).toLowerCase() + "_";
                ChallengesManager manager = this.addon.getChallengesManager();

                List<Challenge> challengeList = manager.getAllChallenges(world).
                    stream().
                    map(challenge -> {
                        // Use clone to avoid any changes in existing challenges.
                        Challenge clone = challenge.clone();
                        // Remove world name from challenge id.
                        clone.setUniqueId(challenge.getUniqueId().replaceFirst(replacementString, ""));
                        // Remove world name from level id.
                        clone.setLevel(challenge.getLevel().replaceFirst(replacementString, ""));

                        return clone;
                    }).
                    collect(Collectors.toList());

                List<ChallengeLevel> levelList = manager.getLevels(world).
                    stream().
                    map(challengeLevel -> {
                        // Use clone to avoid any changes in existing levels.
                        ChallengeLevel clone = challengeLevel.copy();
                        // Remove world name from level ID.
                        clone.setUniqueId(challengeLevel.getUniqueId().replaceFirst(replacementString, ""));
                        // Remove world name.
                        clone.setWorld("");
                        // Challenges must be reassign, as they also contains world name.
                        clone.setChallenges(challengeLevel.getChallenges().stream().
                            map(challenge -> challenge.replaceFirst(replacementString, "")).
                            collect(Collectors.toSet()));

                        return clone;
                    }).
                    collect(Collectors.toList());

                DefaultDataHolder defaultChallenges = new DefaultDataHolder();
                defaultChallenges.setChallengeList(challengeList);
                defaultChallenges.setLevelList(levelList);
                defaultChallenges.setVersion(this.addon.getDescription().getVersion());

                try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(defaultFile), StandardCharsets.UTF_8))) {
                    writer.write(Objects.requireNonNull(
                        new DefaultJSONHandler(this.addon).toJsonString(defaultChallenges)));
                }
            }
        }
        catch (IOException e)
        {
            if (user.isPlayer())
            {
                Utils.sendMessage(user,
                    world,
                    Constants.ERRORS + "no-load",
                    Constants.PARAMETER_FILE, fileName,
                    TextVariables.DESCRIPTION, e.getMessage());
            }

            this.addon.logError("Could not save json file: " + e.getMessage());
        }
        finally
        {
            if (user.isPlayer())
            {
                Utils.sendMessage(user,
                    world,
                    Constants.CONVERSATIONS + "database-export-completed",
                    Constants.PARAMETER_WORLD, world.getName(),
                    Constants.PARAMETER_FILE, fileName);
            }
            else
            {
                this.addon.logWarning("Database Export Completed");
            }
        }
    }


    // ---------------------------------------------------------------------
    // Section: Static Methods
    // ---------------------------------------------------------------------


    /**
     * Match item stack.
     *
     * @param text the text
     * @return the item stack
     */
    @Nullable
    private static ItemStack matchIcon(@Nullable String text)
    {
        if (text == null || text.isBlank())
        {
            return new ItemStack(Material.PAPER);
        }
        else
        {
            return ItemParser.parse(text, new ItemStack(Material.PAPER));
        }
    }


    /**
     * Match item stack.
     *
     * @param text the text
     * @param defaultItem the default item
     * @return the item stack
     */
    @NonNull
    private static ItemStack matchIcon(@Nullable String text, ItemStack defaultItem)
    {
        ItemStack item = matchIcon(text);
        return item == null ? defaultItem : item;
    }


    /**
     * Match material.
     *
     * @param text the text
     * @return the material
     */
    @Nullable
    private static Material matchMaterial(@Nullable String text)
    {
        if (text == null || text.isBlank())
        {
            return null;
        }
        else
        {
            return Material.getMaterial(text.toUpperCase());
        }
    }


    /**
     * Match material.
     *
     * @param text the text
     * @param defaultItem the default item
     * @return the material
     */
    @NonNull
    private static Material matchMaterial(@Nullable String text, Material defaultItem)
    {
        Material item = matchMaterial(text);
        return item == null ? defaultItem : item;
    }


    /**
     * Match entity type.
     *
     * @param text the text
     * @return the entity type
     */
    @Nullable
    private static EntityType matchEntity(@Nullable String text)
    {
        if (text == null || text.isBlank())
        {
            return null;
        }
        else
        {
            try
            {
                return EntityType.valueOf(text.toUpperCase());
            }
            catch (Exception e)
            {
                return null;
            }
        }
    }


    /**
     * Match entity type.
     *
     * @param text the text
     * @param defaultItem the default item
     * @return the entity type
     */
    @NonNull
    private static EntityType matchEntity(@Nullable String text, EntityType defaultItem)
    {
        EntityType item = matchEntity(text);
        return item == null ? defaultItem : item;
    }


    /**
     * Match statistic value.
     *
     * @param text the text
     * @return the statistic
     */
    @Nullable
    private static Statistic matchStatistic(@Nullable String text)
    {
        if (text == null || text.isBlank())
        {
            return null;
        }
        else
        {
            try
            {
                return Statistic.valueOf(text.toUpperCase());
            }
            catch (Exception e)
            {
                return null;
            }
        }
    }


    /**
     * Match challenge type
     *
     * @param text the text
     * @param defaultType default type
     * @return the challenge type
     */
    private static Challenge.ChallengeType matchChallengeType(@Nullable String text, Challenge.ChallengeType defaultType)
    {
        if (text == null || text.isBlank())
        {
            return defaultType;
        }
        else
        {
            try
            {
                return Challenge.ChallengeType.valueOf(text.toUpperCase());
            }
            catch (Exception e)
            {
                return defaultType;
            }
        }
    }


    /**
     * Match world environment.
     *
     * @param text the text
     * @param defaultType the default type
     * @return the world environment
     */
    private static World.Environment matchEnvironment(@Nullable String text, World.Environment defaultType)
    {
        if (text == null || text.isBlank())
        {
            return defaultType;
        }
        else
        {
            try
            {
                return World.Environment.valueOf(text.toUpperCase());
            }
            catch (Exception e)
            {
                return defaultType;
            }
        }
    }


    // ---------------------------------------------------------------------
    // Section: Private classes for default challenges
    // ---------------------------------------------------------------------


    /**
     * This Class allows to load default challenges and their levels as objects much easier.
     */
    private static final class DefaultJSONHandler
    {
        /**
         * This constructor inits JSON builder that will be used to parse challenges.
         * @param addon Challenges Adddon
         */
        DefaultJSONHandler(ChallengesAddon addon)
        {
            GsonBuilder builder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().enableComplexMapKeySerialization();
            // Register adapters
            builder.registerTypeAdapterFactory(new BentoboxTypeAdapterFactory(addon.getPlugin()));
            builder.registerTypeAdapterFactory(new TagTypeAdapterFactory());
            // Keep null in the database
            builder.serializeNulls();
            // Allow characters like < or > without escaping them
            builder.disableHtmlEscaping();

            this.addon = addon;
            this.gson = builder.setPrettyPrinting().create();
        }


        /**
         * This method returns json object that is parsed to string. Json object is made from given instance.
         * @param instance Instance that must be parsed to json string.
         * @return String that contains JSON information from instance object.
         */
        String toJsonString(DefaultDataHolder instance)
        {
            // Null check
            if (instance == null)
            {
                this.addon.logError("JSON database request to store a null. ");
                return null;
            }

            return this.gson.toJson(instance);
        }


        /**
         * This method creates and adds to list all objects from default.json file.
         * @return List of all objects from default.json that is with T instance.
         */
        DefaultDataHolder loadObject(String fileName)
        {
            if (!fileName.endsWith(".json"))
            {
                fileName = fileName + ".json";
            }

            File defaultFile = new File(this.addon.getDataFolder(), fileName);

            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(defaultFile), StandardCharsets.UTF_8))
            {
                DefaultDataHolder object = this.gson.fromJson(reader, DefaultDataHolder.class);

                reader.close(); // NOSONAR Required to keep OS file handlers low and not rely on GC

                return object;
            }
            catch (FileNotFoundException e)
            {
                this.addon.logError("Could not load file '" + defaultFile.getName() + "': File not found.");
            }
            catch (Exception e)
            {
                this.addon.logError("Could not load objects " + defaultFile.getName() + " " + e.getMessage());
            }

            return null;
        }


        /**
         * This method creates and adds to list all objects from default.json file.
         * @return List of all objects from default.json that is with T instance.
         */
        DefaultDataHolder loadWebObject(String downloadedObject)
        {
            return this.gson.fromJson(downloadedObject, DefaultDataHolder.class);
        }


        // ---------------------------------------------------------------------
        // Section: Variables
        // ---------------------------------------------------------------------


        /**
         * Holds JSON builder object.
         */
        private final Gson gson;

        /**
         * Holds ChallengesAddon object.
         */
        private final ChallengesAddon addon;
    }


    /**
     * This is simple object that will allow to store all current challenges and levels
     * in single file.
     */
    private static final class DefaultDataHolder implements DataObject
    {
        /**
         * Default constructor. Creates object with empty lists.
         */
        DefaultDataHolder()
        {
            this.challengeList = Collections.emptyList();
            this.challengeLevelList = Collections.emptyList();
            this.version = "";
        }


        /**
         * This method returns stored challenge list.
         * @return list that contains default challenges.
         */
        List<Challenge> getChallengeList()
        {
            return challengeList;
        }


        /**
         * This method sets given list as default challenge list.
         * @param challengeList new default challenge list.
         */
        void setChallengeList(List<Challenge> challengeList)
        {
            this.challengeList = challengeList;
        }


        /**
         * This method returns list of default challenge levels.
         * @return List that contains default challenge levels.
         */
        List<ChallengeLevel> getLevelList()
        {
            return challengeLevelList;
        }


        /**
         * This method sets given list as default challenge level list.
         * @param levelList new default challenge level list.
         */
        void setLevelList(List<ChallengeLevel> levelList)
        {
            this.challengeLevelList = levelList;
        }


        /**
         * This method returns the version value.
         * @return the value of version.
         */
        public String getVersion()
        {
            return version;
        }


        /**
         * This method sets the version value.
         * @param version the version new value.
         *
         */
        public void setVersion(String version)
        {
            this.version = version;
        }


        /**
         * @return default.json
         */
        @Override
        public String getUniqueId()
        {
            return "default.json";
        }


        /**
         * @param uniqueId - unique ID the uniqueId to set
         */
        @Override
        public void setUniqueId(String uniqueId)
        {
            // method not used.
        }


        // ---------------------------------------------------------------------
        // Section: Variables
        // ---------------------------------------------------------------------


        /**
         * Holds a list with default challenges.
         */
        @Expose
        private List<Challenge> challengeList;

        /**
         * Holds a list with default levels.
         */
        @Expose
        private List<ChallengeLevel> challengeLevelList;

        /**
         * Holds a variable that stores in which addon version file was made.
         */
        @Expose
        private String version;
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    private final ChallengesAddon addon;
}