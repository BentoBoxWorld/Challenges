package bentobox.addon.challenges;

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
 */
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
        // Material:Qty
        if (part.length == 2) {
            return two(s, part);
        } else if (part.length == 3) {
            return three(s, part);
        } else if (part.length == 6 && (part[0].contains("POTION") || part[0].equalsIgnoreCase("TIPPED_ARROW"))) {
            return potion(s, part);
        }
        showError(s);
        return null;

    }

    private ItemStack potion(String s, String[] part) {
        int reqAmount = 0;
        try {
            reqAmount = Integer.parseInt(part[5]);
        } catch (Exception e) {
            addon.getLogger().severe(() -> "Could not parse the quantity of the potion or tipped arrow " + s);
            return null;
        }
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
        if (part[4].equalsIgnoreCase("SPLASH")) {
            result = new ItemStack(Material.SPLASH_POTION);
        } else if (part[4].equalsIgnoreCase("LINGER")) {
            result = new ItemStack(Material.LINGERING_POTION);
        }
        if (part[0].equalsIgnoreCase("TIPPED_ARROW")) {
            result = new ItemStack(Material.TIPPED_ARROW);
        }
        result.setAmount(reqAmount);
        PotionMeta potionMeta = (PotionMeta)(result.getItemMeta());
        PotionType type = PotionType.valueOf(part[1].toUpperCase());
        boolean isUpgraded = (part[2].isEmpty() || part[2].equalsIgnoreCase("1")) ? false: true;
        boolean isExtended = part[3].equalsIgnoreCase("EXTENDED") ? true : false;
        PotionData data = new PotionData(type, isExtended, isUpgraded);
        potionMeta.setBasePotionData(data);

        result.setAmount(reqAmount);
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
