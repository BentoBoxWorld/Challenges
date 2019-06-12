package world.bentobox.challenges;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.database.json.BentoboxTypeAdapterFactory;
import world.bentobox.bentobox.database.objects.DataObject;
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
    public boolean loadDefaultChallenges(User user, World world)
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

        try
        {
        	// This prefix will be used to all challenges. That is a unique way how to separate challenged for
			// each game mode.
			String uniqueIDPrefix = Util.getWorld(world).getName() + "_";
        	DefaultDataHolder defaultChallenges = new DefaultJSONHandler(this.addon).loadObject();

        	// All new challenges should get correct ID. So we need to map it to loaded challenges.
        	defaultChallenges.getChallengeList().parallelStream().forEach(challenge -> {
        		// Set correct challenge ID
        		challenge.setUniqueId(uniqueIDPrefix + challenge.getUniqueId());
				// Set up correct level ID if it is necessary
        		if (!challenge.getLevel().isEmpty())
				{
					challenge.setLevel(uniqueIDPrefix + challenge.getLevel());
				}
				// Load challenge in memory
				manager.loadChallenge(challenge, false, user, user == null);
			});

			defaultChallenges.getLevelList().parallelStream().forEach(challengeLevel -> {
				// Set correct level ID
				challengeLevel.setUniqueId(uniqueIDPrefix + challengeLevel.getUniqueId());
				// Set correct world name
				challengeLevel.setWorld(Util.getWorld(world).getName());
				// Reset names for all challenges.
				challengeLevel.setChallenges(challengeLevel.getChallenges().stream().
					map(challenge -> uniqueIDPrefix + challenge).
					collect(Collectors.toSet()));
				// Load level in memory
				manager.loadLevel(challengeLevel, false, user, user == null);
			});
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

        this.addon.getChallengesManager().save();

        // Remove default.yml file from resources to avoid interacting with it.
        new File(this.addon.getDataFolder(), "default.json").delete();

        return true;
    }


    // ---------------------------------------------------------------------
    // Section: Default generation
    // ---------------------------------------------------------------------


    /**
     * Create method that can generate default challenge file from existing challenges in given world.
     * This method will create default.json file in Challenges folder.
	 * @param user User who calls this method.
     * @param world from which challenges must be stored.
	 * @param overwrite indicates if existing default.json file can be overwritten.
	 * @return <code>true</code> if everything was successful, otherwise <code>false</code>
     */
    public boolean generateDefaultChallengeFile(User user, World world, boolean overwrite)
    {
        File defaultFile = new File(this.addon.getDataFolder(), "default.json");

        if (defaultFile.exists())
		{
			if (overwrite)
			{
				if (user.isPlayer())
				{
					user.sendMessage("challenges.messages.defaults-file-overwrite");
				}
				else
				{
					this.addon.logWarning("challenges.messages.defaults-file-overwrite");
				}

				defaultFile.delete();
			}
			else
			{
				if (user.isPlayer())
				{
					user.sendMessage("challenges.errors.defaults-file-exist");
				}
				else
				{
					this.addon.logWarning("challenges.errors.defaults-file-exist");
				}

				return false;
			}
		}

        try
        {
            if (defaultFile.createNewFile())
            {
                String replacementString = Util.getWorld(world).getName() + "_";
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
						ChallengeLevel clone = challengeLevel.clone();
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

				BufferedWriter writer = new BufferedWriter(new FileWriter(defaultFile, false));
				writer.write(Objects.requireNonNull(
					new DefaultJSONHandler(this.addon).toJsonString(defaultChallenges)));
				writer.close();
            }
        }
        catch (IOException e)
        {
			if (user.isPlayer())
			{
				user.sendMessage("challenges.errors.defaults-file-error");
			}

			this.addon.logError("Could not save json file: " + e.getMessage());
			return false;
		}
        finally
		{
			if (user.isPlayer())
			{
				user.sendMessage("challenges.messages.defaults-file-completed", "[world]", world.getName());
			}
			else
			{
				this.addon.logWarning("challenges.messages.defaults-file-completed");
			}
		}

		return true;
	}


    // ---------------------------------------------------------------------
    // Section: Private classes for default challegnes
    // ---------------------------------------------------------------------


    /**
     * This Class allows to load default challenges and their levels as objects much easier.
     */
	private final class DefaultJSONHandler
    {
		/**
		 * This constructor inits JSON builder that will be used to parese challenges.
		 * @param addon Challenges Adddon
		 */
		DefaultJSONHandler(ChallengesAddon addon)
        {
			GsonBuilder builder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().enableComplexMapKeySerialization();
			// Register adapters
			builder.registerTypeAdapterFactory(new BentoboxTypeAdapterFactory(addon.getPlugin()));
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
		DefaultDataHolder loadObject()
		{
			File defaultFile = new File(this.addon.getDataFolder(), "default.json");

			StringBuilder builder = new StringBuilder();

			try
			{
				Files.readAllLines(defaultFile.toPath()).forEach(builder::append);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}


			return this.gson.fromJson(builder.toString(), DefaultDataHolder.class);
		}


    	// ---------------------------------------------------------------------
    	// Section: Variables
    	// ---------------------------------------------------------------------


		/**
		 * Holds JSON builder object.
		 */
		private Gson gson;

		/**
		 * Holds ChallengesAddon object.
		 */
		private ChallengesAddon addon;
    }


	/**
	 * This is simple object that will allow to store all current challenges and levels
	 * in single file.
	 */
	private final class DefaultDataHolder implements DataObject
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
		public List<Challenge> getChallengeList()
		{
			return challengeList;
		}


		/**
		 * This method sets given list as default challenge list.
		 * @param challengeList new default challenge list.
		 */
		public void setChallengeList(List<Challenge> challengeList)
		{
			this.challengeList = challengeList;
		}


		/**
		 * This method returns list of default challenge levels.
		 * @return List that contains default challenge levels.
		 */
		public List<ChallengeLevel> getLevelList()
		{
			return challengeLevelList;
		}


		/**
		 * This method sets given list as default challenge level list.
		 * @param levelList new default challenge level list.
		 */
		public void setLevelList(List<ChallengeLevel> levelList)
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
}