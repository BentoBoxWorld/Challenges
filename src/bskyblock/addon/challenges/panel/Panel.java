package bskyblock.addon.challenges.panel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class Panel {

    private final Inventory gui;
    private final TreeMap<Integer, PanelItem> panelItems; 

    public Panel(Plugin plugin, String name, TreeMap<Integer, PanelItem> panelItems) {
        // Generate gui
        this.panelItems = panelItems;
        // Create the panel
        if (panelItems.lastKey() > 0) {
            // Make sure size is a multiple of 9
            int size = panelItems.lastKey() + 8;
            size -= (size % 9);
            gui = Bukkit.createInventory(null, size, name);
            // Fill the inventory and return
            for (Entry<Integer, PanelItem> en: panelItems.entrySet()) {
                gui.setItem(en.getKey(), en.getValue().getIcon());
            }
        } else {
            gui = Bukkit.createInventory(null, 9, name);
        }

    }

    public TreeMap<Integer, PanelItem> getPanelItems() {
        return panelItems;
    }

    public Inventory getPanel() {
        return gui;
    }

    public static PanelBuilder builder(Plugin plugin) {
        return new PanelBuilder(plugin);
    }

    public static class PanelBuilder {
        private TreeMap<Integer,PanelItem> panelItems = new TreeMap<>();
        private String name;
        private Plugin plugin;

        public PanelBuilder(Plugin plugin) {
            this.plugin = plugin;
        }

        public PanelBuilder addItem(PanelItem item) {
            // Fit into slots. Handle duplicates
            int index = item.getSlot();
            while (panelItems.containsKey(index) || index == 49) {
                index++;
            };
            panelItems.put(index, item);
            Bukkit.getLogger().info("DEBUG: added to slot " + index);
            return this;
        }

        public PanelBuilder name(String name) {
            this.name = name;
            return this;
        }

        public Panel build() {
            return new Panel(plugin, name, panelItems);
        }

    }

    public static PanelItemBuilder panelItemBuilder() {
        return new PanelItemBuilder();
    }

    public static class PanelItem {
        private final int slot;
        // The current index of the icon
        private int index = 0;
        // There is a list of icons for every toggle option
        private final List<ItemStack> icon;
        // Command to run when clicked
        private final List<String> commands;

        public PanelItem(ItemStack icon, String description, String name, int slot, List<String> toggleItems, boolean glow, List<String> commands) {
            this.slot = slot;
            this.commands = commands;
            List<ItemStack> result = new ArrayList<>();
            if (toggleItems.isEmpty()) {
                // Create the icon
                ItemMeta meta = icon.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
                meta.setLore(chop(description));
                // Set flags to neaten up the view
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                if (glow) {
                    meta.addEnchant(Enchantment.ARROW_DAMAGE, 0, true);
                }
                icon.setItemMeta(meta);
                result.add(icon); 
            } else {
                for (int i = 0; i < toggleItems.size(); i++) {
                    // Create the icon(s)
                    ItemMeta meta = icon.getItemMeta();
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
                    List<String> desc = chop(description);
                    desc.addAll(chop(toggleItems.get(i)));
                    meta.setLore(desc);
                    // Set flags to neaten up the view
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                    meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    if (glow) {
                        meta.addEnchant(Enchantment.ARROW_DAMAGE, 0, true);
                    }
                    icon.setItemMeta(meta);
                    result.add(icon);
                }
            }
            this.icon = result;
        }

        public ItemStack getIcon() {            
            return icon.get(index);
        }

        public ItemStack toggleIcon() {
            if (icon.size() < (index + 1)) {
                index++;
            } else {
                index = 0;
            }
            return icon.get(index);
        }

        public Integer getSlot() {
            return slot;
        }

        public List<String> getCommands() {
            return commands;
        }
    }

    public static class PanelItemBuilder {
        private ItemStack icon;
        private String description;
        private String name;
        private int slot;
        private List<String> toggleItems = new ArrayList<>();
        private boolean glow;
        private List<String> command = new ArrayList<>();

        public PanelItemBuilder setIcon(ItemStack icon) {
            this.icon = icon;
            return this;
        }
        public PanelItemBuilder setDescription(String description) {
            this.description = description;
            return this;
        }
        public PanelItemBuilder setName(String name) {
            this.name = name;
            return this;
        }
        public PanelItemBuilder setSlot(int slot) {
            this.slot = slot;
            return this;
        }
        public PanelItemBuilder setToggleItems(List<String> toggleItems) {
            this.toggleItems = toggleItems;
            return this;
        }
        public PanelItemBuilder setGlow(boolean glow) {
            this.glow = glow;
            return this;
        }
        public PanelItemBuilder setCommand(String command) {
            this.command.add(command);
            return this;
        }
        public PanelItem build() {
            return new PanelItem(icon, description, name, slot, toggleItems, glow, command);
        }


    }

    private static List<String> chop(String longLine) {
        longLine = ChatColor.translateAlternateColorCodes('&', longLine);
        // Split pip character requires escaping it
        String[] split = longLine.split("\\|");
        return new ArrayList<String>(Arrays.asList(split));
    }

}