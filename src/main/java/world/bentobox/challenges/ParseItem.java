package world.bentobox.challenges;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

/**
 * Class that parses a string into an ItemStack
 * Used for converting config file entries to objects
 * @author tastybento
 *
 * @deprecated
 * @see world.bentobox.bentobox.util.ItemParser#parse(String)
 */
@Deprecated
public class ParseItem {

    private final ItemStack item;
    private ChallengesAddon addon;

    public ParseItem(ChallengesAddon addon, String s) {
        this.addon = addon;
        item = parseItem(s);
    }

    /**
     * Parse a string into an itemstack
     * @param s - input string
     * @return ItemStack or null if parsing failed
     */
    private ItemStack parseItem(String s) {
        String[] part = s.split(":");
        if (part.length > 0 && (part[0].equalsIgnoreCase("POTION") || part[0].equalsIgnoreCase("TIPPED_ARROW"))) {
            return potion(s, part);
        }
        // Material:Qty
        if (part.length == 2) {
            return two(s, part);
        } else if (part.length == 3) {
            return three(s, part);
        }
        showError(s);
        return null;

    }

    private ItemStack potion(String s, String[] part) {
        /*
         * # Format POTION:NAME:<LEVEL>:<EXTENDED>:<SPLASH/LINGER>:QTY
            # LEVEL, EXTENDED, SPLASH, LINGER are optional.
            # LEVEL is a number, 1 or 2
            # LINGER is for V1.9 servers and later
            # Examples:
            # POTION:STRENGTH:1:EXTENDED:SPLASH:1
            # POTION:INSTANT_DAMAGE:2::LINGER:2
            # POTION:JUMP:2:NOTEXTENDED:NOSPLASH:1
            # POTION:WEAKNESS::::1   -  any weakness potion
         */

        ItemStack result = new ItemStack(Material.POTION);
        if (part[0].equalsIgnoreCase("TIPPED_ARROW")) {
            result = new ItemStack(Material.TIPPED_ARROW);
        } else if (part[0].equalsIgnoreCase("SPLASH_POTION")) {
            result = new ItemStack(Material.SPLASH_POTION);
        } else if (part[0].equalsIgnoreCase("LINGERING_POTION")) {
            result = new ItemStack(Material.LINGERING_POTION);
        }
        int reqAmount = 0;
        String amount = "1";
        String level = "1";
        String ext = "";
        String splashLinger = "";
        switch (part.length) {
        case 3:
            amount = part[2];
            break;
        case 4:
            level = part[2];
            amount = part[3];
            break;
        case 5:
            level = part[2];
            ext = part[3];
            amount = part[4];
            break;
        case 6:
            level = part[2];
            ext = part[3];
            splashLinger = part[4];
            amount = part[5];
            break;

        default:
            // Because I don't know!
            return null;
        }
        // Parse the quantity
        try {
            reqAmount = Integer.parseInt(amount);
        } catch (Exception e) {
            addon.getLogger().severe(() -> "Could not parse the quantity of the potion or tipped arrow " + s);
            return null;
        }
        result.setAmount(reqAmount);

        // Parse the legacy splash / linger
        if (splashLinger.equalsIgnoreCase("SPLASH")) {
            result = new ItemStack(Material.SPLASH_POTION);
        } else if (splashLinger.equalsIgnoreCase("LINGER")) {
            result = new ItemStack(Material.LINGERING_POTION);
        }
        // Parse the type of potion
        PotionMeta potionMeta = (PotionMeta)(result.getItemMeta());
        PotionType type = PotionType.valueOf(part[1].toUpperCase());
        boolean isUpgraded = (level.isEmpty() || level.equalsIgnoreCase("1")) ? false: true;
        boolean isExtended = ext.equalsIgnoreCase("EXTENDED") ? true : false;
        PotionData data = new PotionData(type, isExtended, isUpgraded);
        potionMeta.setBasePotionData(data);
        result.setItemMeta(potionMeta);
        return result;
    }

    private ItemStack three(String s, String[] part) {
        // Rearrange
        String[] twoer = {part[0], part[2]};
        ItemStack result = two(s, twoer);
        if (result == null) {
            showError(s);
            return null;
        }

        return result;

    }

    private void showError(String s) {
        addon.getLogger().severe(() -> "Problem with " + s + " in config.yml!");
    }

    private ItemStack two(String s, String[] part) {
        int reqAmount = 0;
        try {
            reqAmount = Integer.parseInt(part[1]);
        } catch (Exception e) {
            showError(s);
            return null;
        }
        Material reqItem = Material.getMaterial(part[0].toUpperCase() + "_ITEM");
        if (reqItem == null) {
            // Try the item
            reqItem = Material.getMaterial(part[0].toUpperCase());
        }

        if (reqItem == null) {
            showError(s);
            return null;
        }
        return new ItemStack(reqItem, reqAmount);

    }

    /**
     * @return the item
     */
    public ItemStack getItem() {
        return item;
    }
}
