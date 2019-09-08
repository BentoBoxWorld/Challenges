package world.bentobox.challenges;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.World;

import world.bentobox.bentobox.database.json.BentoboxTypeAdapterFactory;
import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.challenges.database.object.ChallengeLevel;
import world.bentobox.challenges.database.object.Challenge;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.challenges.utils.Utils;


/**
 * Imports challenges
 * @author BONNe1704
 *
 */
public class ChallengesImportManager
{
    /**
     * Import challenges from default.json
     * @param challengesAddon
     */
    public ChallengesImportManager(ChallengesAddon challengesAddon)
	{
        this.addon = challengesAddon;
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

        // default configuration should be removed.
		// user made configuration should not!.
        boolean removeAtEnd =
			!Files.exists(Paths.get(this.addon.getDataFolder().getPath() + "/default.json"));

        // Safe json configuration to Challenges folder.
		this.addon.saveResource("default.json", false);

        try
        {
        	// This prefix will be used to all challenges. That is a unique way how to separate challenged for
			// each game mode.
			String uniqueIDPrefix = Utils.getGameMode(world) + "_";
        	DefaultDataHolder defaultChallenges = new DefaultJSONHandler(this.addon).loadObject();

        	// All new challenges should get correct ID. So we need to map it to loaded challenges.
        	defaultChallenges.getChallengeList().forEach(challenge -> {
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

			defaultChallenges.getLevelList().forEach(challengeLevel -> {
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

        if (removeAtEnd)
		{
			// Remove default.yml file from resources to avoid interacting with it.
			new File(this.addon.getDataFolder(), "default.json").delete();
		}

        return true;
    }


	/**
	 * This method loads downloaded challenges into memory.
	 * @param user User who calls downloaded challenge loading
	 * @param world Target world.
	 * @param downloadString String that need to be loaded via DefaultDataHolder.
	 * @return <code>true</code> if everything was successful, otherwise <code>false</code>.
	 */
	public boolean loadDownloadedChallenges(User user, World world, String downloadString)
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

		try
		{
			// This prefix will be used to all challenges. That is a unique way how to separate challenged for
			// each game mode.
			String uniqueIDPrefix = Utils.getGameMode(world) + "_";
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
				manager.loadChallenge(challenge, false, user, user == null);
			});

			downloadedChallenges.getLevelList().forEach(challengeLevel -> {
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
                String replacementString = Utils.getGameMode(world) + "_";
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


	private ChallengesAddon addon;
}