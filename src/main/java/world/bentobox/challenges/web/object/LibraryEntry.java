package world.bentobox.challenges.web.object;


import org.bukkit.Material;
import org.eclipse.jdt.annotation.NonNull;

import com.google.gson.JsonObject;


/**
 * The type Library entry.
 * @param name Name of the library entry
 * @param icon Icon of the library entry
 * @param description Description of the library entry
 * @param repository Link to the repository
 * @param language Language of the entry
 * @param slot order of the entry
 * @param gameMode Made primary for gamemode
 * @param author Author of the entry
 * @param version version of the challenges.
 */
public record LibraryEntry(String name, Material icon, String description, String repository,
						   String language, int slot, String gameMode, String author, String version)
{
	/**
	 * Default constructor.
	 * @param object Json Object that must be translated to LibraryEntry.
	 * @throws IllegalArgumentException if a required field is missing or null.
	 */
	public static LibraryEntry fromJson(@NonNull JsonObject object)
	{
		String iconStr = getString(object, "icon");
		Material material = iconStr != null ? Material.matchMaterial(iconStr) : null;

		return new LibraryEntry(
			getString(object, "name"),
			(material != null) ? material : Material.PAPER,
			getString(object, "description"),
			getString(object, "repository"),
			getString(object, "language"),
			getInt(object, "slot"),
			getString(object, "for"),
			getString(object, "author"),
			getString(object, "version"));
	}


	private static String getString(@NonNull JsonObject obj, @NonNull String key)
	{
		var el = obj.get(key);
		if (el == null || el.isJsonNull())
		{
			throw new IllegalArgumentException("Missing required field: \"" + key + "\"");
		}
		return el.getAsString();
	}


	private static int getInt(@NonNull JsonObject obj, @NonNull String key)
	{
		var el = obj.get(key);
		if (el == null || el.isJsonNull())
		{
			throw new IllegalArgumentException("Missing required field: \"" + key + "\"");
		}
		return el.getAsInt();
	}


	public static LibraryEntry fromTemplate(String name, Material icon)
	{
		return new LibraryEntry(name, icon, "", "", "", 0, "", "", "");
	}
}