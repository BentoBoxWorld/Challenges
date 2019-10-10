package world.bentobox.challenges.web.object;


import org.bukkit.Material;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonObject;


/**
 * This objects allows to load each Challenges Catalog library entry.
 */
public class LibraryEntry
{
	/**
	 * Default constructor.
	 * @param object Json Object that must be translated to LibraryEntry.
	 */
	public LibraryEntry(@NonNull JsonObject object)
	{
		this.name = object.get("name").getAsString();

		Material material = Material.matchMaterial(object.get("icon").getAsString());
		this.icon = (material != null) ? material : Material.PAPER;

		this.description = object.get("description").getAsString();
		this.repository = object.get("repository").getAsString();
		this.language = object.get("language").getAsString();

		this.slot = object.get("slot").getAsInt();

		this.forGameMode = object.get("for").getAsString();
		this.author = object.get("author").getAsString();
		this.version = object.get("version").getAsString();
	}


	/**
	 * This method returns the name value.
	 * @return the value of name.
	 */
	@NonNull
	public String getName()
	{
		return name;
	}


	/**
	 * This method returns the icon value.
	 * @return the value of icon.
	 */
	@NonNull
	public Material getIcon()
	{
		return icon;
	}


	/**
	 * This method returns the description value.
	 * @return the value of description.
	 */
	@NonNull
	public String getDescription()
	{
		return description;
	}


	/**
	 * This method returns the repository value.
	 * @return the value of repository.
	 */
	@NonNull
	public String getRepository()
	{
		return repository;
	}


	/**
	 * This method returns the language value.
	 * @return the value of language.
	 */
	@NonNull
	public String getLanguage()
	{
		return language;
	}


	/**
	 * This method returns the slot value.
	 * @return the value of slot.
	 */
	@NonNull
	public int getSlot()
	{
		return slot;
	}


	/**
	 * This method returns the forGameMode value.
	 * @return the value of forGameMode.
	 */
	@NonNull
	public String getForGameMode()
	{
		return forGameMode;
	}


	/**
	 * This method returns the author value.
	 * @return the value of author.
	 */
	@NonNull
	public String getAuthor()
	{
		return author;
	}


	/**
	 * This method returns the version value.
	 * @return the value of version.
	 */
	@NonNull
	public String getVersion()
	{
		return version;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * Name of entry object
	 */
	private @NonNull String name;

	/**
	 * Defaults to {@link Material#PAPER}.
	 */
	private @NonNull Material icon;

	/**
	 * Description of entry object.
	 */
	private @NonNull String description;

	/**
	 * File name in challenges library.
	 */
	private @NonNull String repository;

	/**
	 * Language of content.
	 */
	private @Nullable String language;

	/**
	 * Desired slot number.
	 */
	private int slot;

	/**
	 * Main GameMode for which challenges were created.
	 */
	private @Nullable String forGameMode;

	/**
	 * Author (-s) who created current configuration.
	 */
	private @Nullable String author;

	/**
	 * Version of Challenges Addon, for which challenges were created.
	 */
	private @Nullable String version;
}