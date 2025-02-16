package world.bentobox.challenges.utils;


import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionType;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.hooks.LangUtilsHook;
import world.bentobox.bentobox.util.Util;


/**
 * Util methods used in different situations.
 */
public class Utils
{
	/**
	 * This method checks if 2 given item stacks are similar without durability check.
	 * @param input First item.
	 * @param stack Second item.
	 * @return {@code true} if items are equal, {@code false} otherwise.
	 */
	public static boolean isSimilarNoDurability(@Nullable ItemStack input, @Nullable ItemStack stack)
	{
		if (stack == null || input == null)
		{
			return false;
		}
		else if (stack == input)
		{
			return true;
		}
		else
		{
			return input.getType() == stack.getType() &&
				input.hasItemMeta() == stack.hasItemMeta() &&
				(!input.hasItemMeta() || Bukkit.getItemFactory().equals(input.getItemMeta(), stack.getItemMeta()));
		}
	}


	/**
	 * This method groups input items in single itemstack with correct amount and returns it.
	 * Allows to remove duplicate items from list.
	 * @param requiredItems Input item list
	 * @return List that contains unique items that cannot be grouped.
	 */
	public static List<ItemStack> groupEqualItems(List<ItemStack> requiredItems, Set<Material> ignoreMetaData)
	{
		List<ItemStack> returnItems = new ArrayList<>(requiredItems.size());

		// Group all equal items in single stack, as otherwise it will be too complicated to check if all
		// items are in players inventory.
		for (ItemStack item : requiredItems)
		{
			boolean isUnique = item != null;

			int i = 0;
			final int requiredSize = returnItems.size();

			while (i < requiredSize && isUnique)
			{
				ItemStack required = returnItems.get(i);

				// Merge items which meta can be ignored or is similar to item in required list.
				if (Utils.isSimilarNoDurability(required, item) ||
					ignoreMetaData.contains(item.getType()) && item.getType().equals(required.getType()))
				{
					required.setAmount(required.getAmount() + item.getAmount());
					isUnique = false;
				}

				i++;
			}

			if (isUnique)
			{
				// The same issue as in other places. Clone prevents from changing original item.
				returnItems.add(item.clone());
			}
		}

		return returnItems;
	}


	/**
	 * This method transforms given World into GameMode name. If world is not a GameMode
	 * world then it returns null.
	 * @param world World which gameMode name must be found out.
	 * @return GameMode name or null.
	 */
	public static String getGameMode(World world)
	{
		return BentoBox.getInstance().getIWM().getAddon(world).
			map(gameModeAddon -> gameModeAddon.getDescription().getName()).
			orElse(null);
	}


	/**
	 * This method allows to get next value from array list after given value.
	 * @param values Array that should be searched for given value.
	 * @param currentValue Value which next element should be found.
	 * @param <T> Instance of given object.
	 * @return Next value after currentValue in values array.
	 */
	public static <T> T getNextValue(T[] values, T currentValue)
	{
		for (int i = 0; i < values.length; i++)
		{
			if (values[i].equals(currentValue))
			{
				if (i + 1 == values.length)
				{
					return values[0];
				}
				else
				{
					return values[i + 1];
				}
			}
		}

		return currentValue;
	}


	/**
	 * This method allows to get previous value from array list after given value.
	 * @param values Array that should be searched for given value.
	 * @param currentValue Value which previous element should be found.
	 * @param <T> Instance of given object.
	 * @return Previous value before currentValue in values array.
	 */
	public static <T> T getPreviousValue(T[] values, T currentValue)
	{
		for (int i = 0; i < values.length; i++)
		{
			if (values[i].equals(currentValue))
			{
				if (i > 0)
				{
					return values[i - 1];
				}
				else
				{
					return values[values.length - 1];
				}
			}
		}

		return currentValue;
	}


	/**
	 * Sanitizes the provided input. It replaces spaces and hyphens with underscores and lower cases the input.
	 * This code also removes all color codes from the input.
	 * @param input input to sanitize
	 * @return sanitized input
	 */
	public static String sanitizeInput(String input)
	{
		return ChatColor.stripColor(
			Util.translateColorCodes(input.toLowerCase(Locale.ENGLISH).
				replace(" ", "_").
				replace("-", "_")));
	}


	/**
	 * Send given message to user and add prefix to the start of the message.
	 *
	 * @param user User who need to receive message.
	 * @param world Reference to world where message must be send.
	 * @param translation String of message that must be send.
	 * @param parameters Parameters that must be added to translation.
	 */
	public static void sendMessage(User user, World world, String translation, String... parameters)
	{
		user.sendMessage(user.getTranslation(world, Constants.CONVERSATIONS + "prefix") +
			user.getTranslation(world, translation, parameters));
	}


	/**
	 * Prettify World.Environment object for user.
	 * @param object Object that must be pretty.
	 * @param user User who will see the object.
	 * @return Prettified string for World.Environment.
	 */
	public static String prettifyObject(World.Environment object, User user)
	{
		// Find addon structure with:
		// [addon]:
		//   environments:
		//     [environment]:
		//       name: [name]
		String translation = user.getTranslationOrNothing(Constants.ENVIRONMENTS + object.name().toLowerCase() + ".name");

		if (!translation.isEmpty())
		{
			// We found our translation.
			return translation;
		}

		// Find addon structure with:
		// [addon]:
		//   environments:
		//     [environment]: [name]

		translation = user.getTranslationOrNothing(Constants.ENVIRONMENTS + object.name().toLowerCase());

		if (!translation.isEmpty())
		{
			// We found our translation.
			return translation;
		}

		// Find general structure with:
		// environments:
		//   [environment]: [name]

		translation = user.getTranslationOrNothing("environments." + object.name().toLowerCase());

		if (!translation.isEmpty())
		{
			// We found our translation.
			return translation;
		}

		// Lang Utils do not have Environment :(
		//LangUtilsHook.getEnvrionmentName(object, user);

		// Nothing was found. Use just a prettify text function.
		return Util.prettifyText(object.name());
	}


	/**
	 * Prettify World.Environment object description for user.
	 * @param object Object that must be pretty.
	 * @param user User who will see the object.
	 * @return Prettified description string for World.Environment.
	 */
	public static String prettifyDescription(World.Environment object, User user)
	{
		// Find addon structure with:
		// [addon]:
		//   environments:
		//     [environment]:
		//       description: [text]
		String translation = user.getTranslationOrNothing(Constants.ENVIRONMENTS + object.name().toLowerCase() + ".description");

		if (!translation.isEmpty())
		{
			// We found our translation.
			return translation;
		}

		// No text to return.
		return "";
	}

    /**
     * Prettify the Tag object for user.
     * @param object a tag, like ALL_HANGING_SIGNS
     * @param user user
     * @return prettified tag
     */
    public static String prettifyObject(@Nullable Tag<?> object, User user) {
        // Nothing to translate
        if (object == null) {
            return "";
        }
        String translation = user.getTranslationOrNothing(
                Constants.MATERIALS + object.getKey().getKey().toLowerCase(Locale.ENGLISH) + ".name");
        String any = user.getTranslationOrNothing(Constants.MATERIALS + "any");
        // Prettify and remove last s
        String tag = any + Util.prettifyText(object.getKey().getKey()).replaceAll("s$", "");

        return translation.isEmpty() ? tag : translation;
    }

    /**
     * Prettify object
     * @param <T> class that extends Enum
     * @param object that extends Enum
     * @param user use who will see the text
     * @return string of pretty text for user
     */
    public static <T extends Enum<T>> String prettifyObject(@Nullable T object, User user) {
        if (object == null) {
            return "";
        }
        if (object instanceof Material m) {
            return prettifyMaterial(m, user);
        }
        // Build a translation key using the enum name.
        String translation = user
                .getTranslationOrNothing(Constants.MATERIALS + object.name().toLowerCase(Locale.ENGLISH) + ".name");
        String any = user.getTranslationOrNothing(Constants.MATERIALS + "any");
        // Use the enum's name and prettify it (for example, convert ALL_HANGING_SIGNS to "All Hanging Sign")
        String tag = any + Util.prettifyText(object.name()).replaceAll("s$", "");
        return translation.isEmpty() ? tag : translation;
    }

	/**
	 * Prettify Material object for user.
	 * @param object Object that must be pretty.
	 * @param user User who will see the object.
	 * @return Prettified string for Material.
	 */
    public static String prettifyMaterial(@Nullable Material object, User user)
	{
		// Nothing to translate
		if (object == null)
		{
			return "";
		}

		// Find addon structure with:
		// [addon]:
		//   materials:
		//     [material]:
		//       name: [name]
		String translation = user.getTranslationOrNothing(Constants.MATERIALS + object.name().toLowerCase() + ".name");

		if (!translation.isEmpty())
		{
			// We found our translation.
			return translation;
		}

		// Find addon structure with:
		// [addon]:
		//   materials:
		//     [material]: [name]

		translation = user.getTranslationOrNothing(Constants.MATERIALS + object.name().toLowerCase());

		if (!translation.isEmpty())
		{
			// We found our translation.
			return translation;
		}

		// Find general structure with:
		// materials:
		//   [material]: [name]

		translation = user.getTranslationOrNothing("materials." + object.name().toLowerCase());

		if (!translation.isEmpty())
		{
			// We found our translation.
			return translation;
		}

		// Use Lang Utils Hook to translate material
		return LangUtilsHook.getMaterialName(object, user);
	}


	/**
	 * Prettify Material object description for user.
	 * @param object Object that must be pretty.
	 * @param user User who will see the object.
	 * @return Prettified description string for Material.
	 */
	public static String prettifyDescription(@Nullable Material object, User user)
	{
		// Nothing to translate
		if (object == null)
		{
			return "";
		}

		// Find addon structure with:
		// [addon]:
		//   materials:
		//     [material]:
		//       description: [text]
		String translation = user.getTranslationOrNothing(Constants.MATERIALS + object.name().toLowerCase() + ".description");

		if (!translation.isEmpty())
		{
			// We found our translation.
			return translation;
		}

		// No text to return.
		return "";
	}


	/**
	 * Prettify EntityType object for user.
	 * @param object Object that must be pretty.
	 * @param user User who will see the object.
	 * @return Prettified string for EntityType.
	 */
	public static String prettifyObject(@Nullable EntityType object, User user)
	{
		// Nothing to translate
		if (object == null)
		{
			return "";
		}

		// Find addon structure with:
		// [addon]:
		//   entities:
		//     [entity]:
		//       name: [name]
		String translation = user.getTranslationOrNothing(Constants.ENTITIES + object.name().toLowerCase() + ".name");

		if (!translation.isEmpty())
		{
			// We found our translation.
			return translation;
		}

		// Find addon structure with:
		// [addon]:
		//   entities:
		//     [entity]: [name]

		translation = user.getTranslationOrNothing(Constants.ENTITIES + object.name().toLowerCase());

		if (!translation.isEmpty())
		{
			// We found our translation.
			return translation;
		}

		// Find general structure with:
		// entities:
		//   [entity]: [name]

		translation = user.getTranslationOrNothing("entities." + object.name().toLowerCase());

		if (!translation.isEmpty())
		{
			// We found our translation.
			return translation;
		}

		// Use Lang Utils Hook to translate material
		return LangUtilsHook.getEntityName(object, user);
	}


	/**
	 * Prettify EntityType object description for user.
	 * @param object Object that must be pretty.
	 * @param user User who will see the object.
	 * @return Prettified description string for EntityType.
	 */
	public static String prettifyDescription(@Nullable EntityType object, User user)
	{
		// Nothing to translate
		if (object == null)
		{
			return "";
		}

		// Find addon structure with:
		// [addon]:
		//   entities:
		//     [entity]:
		//       description: [text]
		String translation = user.getTranslationOrNothing(Constants.ENTITIES + object.name().toLowerCase() + ".description");

		if (!translation.isEmpty())
		{
			// We found our translation.
			return translation;
		}

		// No text to return.
		return "";
	}


	/**
	 * Prettify Statistic object for user.
	 * @param object Object that must be pretty.
	 * @param user User who will see the object.
	 * @return Prettified string for Statistic.
	 */
	public static String prettifyObject(@Nullable Statistic object, User user)
	{
		// Nothing to translate
		if (object == null)
		{
			return "";
		}

		// Find addon structure with:
		// [addon]:
		//   statistics:
		//     [statistic]:
		//       name: [name]
		String translation = user.getTranslationOrNothing(Constants.STATISTICS + object.name().toLowerCase() + ".name");

		if (!translation.isEmpty())
		{
			// We found our translation.
			return translation;
		}

		// Find addon structure with:
		// [addon]:
		//   statistics:
		//     [statistic]: [name]

		translation = user.getTranslationOrNothing(Constants.STATISTICS + object.name().toLowerCase());

		if (!translation.isEmpty())
		{
			// We found our translation.
			return translation;
		}

		// Find general structure with:
		// statistics:
		//   [statistic]: [name]

		translation = user.getTranslationOrNothing("statistics." + object.name().toLowerCase());

		if (!translation.isEmpty())
		{
			// We found our translation.
			return translation;
		}

		// Use Lang Utils Hook to translate material
		//return LangUtilsHook.getStatisticName(object, user);
		return Util.prettifyText(object.name());
	}


	/**
	 * Prettify Statistic object description for user.
	 * @param object Object that must be pretty.
	 * @param user User who will see the object.
	 * @return Prettified description string for Statistic.
	 */
	public static String prettifyDescription(@Nullable Statistic object, User user)
	{
		// Nothing to translate
		if (object == null)
		{
			return "";
		}

		// Find addon structure with:
		// [addon]:
		//   statistics:
		//     [statistic]:
		//       description: [text]
		String translation = user.getTranslationOrNothing(Constants.STATISTICS + object.name().toLowerCase() + ".description");

		if (!translation.isEmpty())
		{
			// We found our translation.
			return translation;
		}

		// No text to return.
		return "";
	}


	/**
	 * Prettify ItemStack object for user.
	 * @param object Object that must be pretty.
	 * @param user User who will see the object.
	 * @return Prettified string for ItemStack.
	 */
	public static String prettifyObject(@Nullable ItemStack object, User user)
	{
		// Nothing to translate
		if (object == null)
		{
			return "";
		}
        // Return the display name if it already has one
        if (object.hasItemMeta()) {
            ItemMeta im = object.getItemMeta();
            if (im.hasDisplayName()) {
                return im.getDisplayName();
            }
        }

		// Find addon structure with:
		// [addon]:
		//   item-stacks:
		//     [material]: ...
		//     meta:
		//       potion-type: ...
		//       ...
		//     generic: [amount] [name] [meta]
        String translation = "";
        if (object.hasItemMeta()) {
            ItemMeta im = object.getItemMeta();
            if (im instanceof PotionMeta pm) {
                translation = prettifyObject(object, pm, user);
            } else if (im instanceof SkullMeta sm) {
                translation = prettifyObject(object, sm, user);
            } else if (im instanceof BookMeta bm) {
                translation = prettifyObject(object, bm, user);
            } else if (im instanceof EnchantmentStorageMeta em) {
                translation = prettifyObject(object, em, user);
            } else {
                translation = prettifyObject(object, im, user);
            }
        }

		if (!translation.isEmpty())
		{
			// We found our translation.
			return translation;
		}

		// Find addon structure with:
		// [addon]:
		//   materials:
		//     [material]: [name]

		translation = user.getTranslationOrNothing(Constants.MATERIALS + object.getType().name().toLowerCase());

		if (!translation.isEmpty())
		{
			// We found our translation.
			return translation;
		}

		// Find general structure with:
		// materials:
		//   [material]: [name]

		translation = user.getTranslationOrNothing("materials." + object.getType().name().toLowerCase());

		if (!translation.isEmpty())
		{
			// We found our translation.
			return translation;
		}

		// Use Lang Utils
		return LangUtilsHook.getItemDisplayName(object, user);
	}


	/**
	 * Prettify enchant string.
	 *
	 * @param enchantment the enchantment
	 * @param user the user
	 * @return the string
	 */
	public static String prettifyObject(Enchantment enchantment, User user)
	{
		if (enchantment == null)
		{
			return "";
		}

		String type = user.getTranslationOrNothing(Constants.ITEM_STACKS + "enchant." + enchantment.getKey().getKey());

		if (type.isEmpty())
		{
			type = LangUtilsHook.getEnchantName(enchantment, user);
		}

		return type;
	}


	/**
	 * Prettify type string.
	 *
	 * @param type the potion type
	 * @param user the user
	 * @return the string
	 */
	public static String prettifyObject(PotionType type, User user)
	{
		if (type == null)
		{
			return "";
		}

		String text = user.getTranslationOrNothing(Constants.ITEM_STACKS + "potion-type." + type.name().toLowerCase());

		if (text.isEmpty())
		{
			text = LangUtilsHook.getPotionBaseEffectName(type, user);
		}

		return text;
	}


	/**
	 * Prettify potion item string.
	 *
	 * @param item the item
	 * @param potionMeta the potion meta
	 * @param user the user
	 * @return the string
	 */
	public static String prettifyObject(ItemStack item, @Nullable PotionMeta potionMeta, User user) {
	    if (potionMeta == null) {
	        return "";
	    }

	    StringBuilder sb = new StringBuilder();

        // This regex looks for: potion: "minecraft:some_text"
        Pattern pattern = Pattern.compile("potion:\\s*\"minecraft:([^\"]+)\"");
        Matcher matcher = pattern.matcher(potionMeta.getAsComponentString());
        if (matcher.find()) {
            String potionType = matcher.group(1);
            String translation = user.getTranslationOrNothing(Constants.ITEM_STACKS + "potion." + potionType);
            sb.append(user.getTranslationOrNothing(Constants.ITEM_STACKS + "potion.name", TextVariables.NAME,
                    Util.prettifyText(translation.isBlank() ? potionType : translation)));
        }

	    // Append the potion form based on the item type.
	    Material material = item.getType();
	    if (material == Material.SPLASH_POTION) {
            sb.append(user.getTranslationOrNothing(Constants.ITEM_STACKS + "potion.splash"));
	    } else if (material == Material.LINGERING_POTION) {
            sb.append(user.getTranslationOrNothing(Constants.ITEM_STACKS + "potion.lingering"));
	    }
	    return sb.toString();
	}


	/**
	 * Prettify skull item string.
	 *
	 * @param item the item
	 * @param skullMeta the skull meta
	 * @param user the user
	 * @return the string
	 */
	public static String prettifyObject(ItemStack item, @Nullable SkullMeta skullMeta, User user)
	{
		if (skullMeta == null)
		{
			return "";
		}

		Material itemType = item.getType();
		final String metaReference = Constants.ITEM_STACKS + "meta.";

		String meta = user.getTranslationOrNothing(metaReference + "skull-meta",
			"[player-name]", skullMeta.getDisplayName());

		return user.getTranslationOrNothing(Constants.ITEM_STACKS + "generic",
			"[type]", prettifyObject(itemType, user),
			"[meta]", meta);
	}


	/**
	 * Prettify item string.
	 *
	 * @param item the item
	 * @param itemMeta the item meta
	 * @param user the user
	 * @return the string
	 */
	public static String prettifyObject(ItemStack item, @Nullable ItemMeta itemMeta, User user)
	{
		if (itemMeta == null)
		{
			return "";
		}

		StringBuilder builder = new StringBuilder();

		itemMeta.getEnchants().forEach((enchantment, level) -> {
			builder.append("\n");
			builder.append(user.getTranslationOrNothing(Constants.ITEM_STACKS + "meta.enchant-meta",
				"[type]", prettifyObject(enchantment, user),
				"[level]", String.valueOf(level)));
		});


		Material itemType = item.getType();
		final String itemReference = Constants.ITEM_STACKS + itemType.name().toLowerCase() + ".";

		String translation = user.getTranslationOrNothing(itemReference + "name",
			"[type]", prettifyObject(itemType, user),
			"[enchant]", builder.toString());

		if (translation.isEmpty())
		{
			translation = user.getTranslationOrNothing(Constants.ITEM_STACKS + "generic",
				"[type]", prettifyObject(itemType, user),
				"[meta]", builder.toString());
		}

		return translation;
	}


	/**
	 * Prettify enchantment storage string.
	 *
	 * @param item the item
	 * @param enchantmentMeta the enchantment storage meta
	 * @param user the user
	 * @return the string
	 */
	public static String prettifyObject(ItemStack item, @Nullable EnchantmentStorageMeta enchantmentMeta, User user)
	{
		if (enchantmentMeta == null)
		{
			return "";
		}

		StringBuilder builder = new StringBuilder();

		enchantmentMeta.getStoredEnchants().forEach((enchantment, level) -> {
			builder.append("\n");
			builder.append(user.getTranslationOrNothing(Constants.ITEM_STACKS + "meta.enchant-meta",
				"[type]", prettifyObject(enchantment, user),
				"[level]", String.valueOf(level)));
		});


		Material itemType = item.getType();
		final String itemReference = Constants.ITEM_STACKS + itemType.name().toLowerCase() + ".";

		String translation = user.getTranslationOrNothing(itemReference + "name",
			"[type]", prettifyObject(itemType, user),
			"[enchant]", builder.toString());

		if (translation.isEmpty())
		{
			translation = user.getTranslationOrNothing(Constants.ITEM_STACKS + "generic",
				"[type]", prettifyObject(itemType, user),
				"[meta]", builder.toString());
		}

		return translation;
	}


	/**
	 * Prettify book item string.
	 *
	 * @param item the item
	 * @param bookMeta the book meta
	 * @param user the user
	 * @return the string
	 */
	public static String prettifyObject(ItemStack item, @Nullable BookMeta bookMeta, User user)
	{
		if (bookMeta == null)
		{
			return "";
		}

		Material itemType = item.getType();
		final String metaReference = Constants.ITEM_STACKS + "meta.";

		String meta = user.getTranslationOrNothing(metaReference + "book-meta",
			"[title]", bookMeta.hasTitle() ? bookMeta.getTitle() : "",
			"[author]", bookMeta.hasAuthor() ? bookMeta.getAuthor() : "");

		return user.getTranslationOrNothing(Constants.ITEM_STACKS + "generic",
			"[type]", prettifyObject(itemType, user),
			"[meta]", meta);
	}


	/**
	 * This method parses duration to a readable format.
	 * @param duration that needs to be parsed.
	 * @return parsed duration string.
	 */
	public static String parseDuration(Duration duration, User user)
	{
		final String reference = Constants.DESCRIPTIONS + "challenge.cooldown.";

		String returnString = "";

		if (duration.toDays() > 0)
		{
			returnString += user.getTranslationOrNothing(reference + "in-days",
				Constants.PARAMETER_NUMBER, String.valueOf(duration.toDays()));
		}

		if (duration.toHoursPart() > 0)
		{
			returnString += user.getTranslationOrNothing(reference + "in-hours",
				Constants.PARAMETER_NUMBER, String.valueOf(duration.toHoursPart()));
		}

		if (duration.toMinutesPart() > 0)
		{
			returnString += user.getTranslationOrNothing(reference + "in-minutes",
				Constants.PARAMETER_NUMBER, String.valueOf(duration.toMinutesPart()));
		}

		if (duration.toSecondsPart() > 0 || returnString.isBlank())
		{
			returnString += user.getTranslationOrNothing(reference + "in-seconds",
				Constants.PARAMETER_NUMBER, String.valueOf(duration.toSecondsPart()));
		}

		return returnString;
	}
}
