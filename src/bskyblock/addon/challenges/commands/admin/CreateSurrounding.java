package bskyblock.addon.challenges.commands.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import bskyblock.addon.challenges.ChallengesAddon;
import us.tastybento.bskyblock.Constants;
import us.tastybento.bskyblock.api.commands.CompositeCommand;
import us.tastybento.bskyblock.api.commands.User;

public class CreateSurrounding extends CompositeCommand implements Listener {


    private ChallengesAddon addon;
    private Map<UUID, Map<Material, Integer>> blocks = new HashMap<>();
    private Map<UUID, String> name = new HashMap<>();

    /**
     * Admin command to make surrounding challenges
     * @param parent
     */
    public CreateSurrounding(ChallengesAddon addon, CompositeCommand parent) {
        super(parent, "surrounding");
        this.addon = addon;
        addon.getServer().getPluginManager().registerEvents(this, addon.getBSkyBlock());
    }

    @Override
    public void setup() {
        this.setOnlyPlayer(true);
        this.setPermission(Constants.PERMPREFIX + "admin.challenges");
        this.setParameters("challaneges.admin.create.surrounding.parameters");
        this.setDescription("challenges.admin.create.surrounding.description");
    }

    @Override
    public boolean execute(User user, List<String> args) {
        if (args.isEmpty()) {
            user.sendRawMessage("not enough args");
            return false;
        }
        // Tell user to hit objects to add to the surrounding object requirements
        user.sendRawMessage("Hit things to add them to the list of things required. Right click when done");
        blocks.computeIfAbsent(user.getUniqueId(), k -> new HashMap<>());
        name.put(user.getUniqueId(), args.get(0));
        return true;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (blocks.containsKey(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e) {
        blocks.remove(e.getPlayer().getUniqueId());
        name.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            addon.getLogger().info("DEBUG: left click");
            if (blocks.containsKey(e.getPlayer().getUniqueId())) {
                // Prevent damage
                e.setCancelled(true);
                Map<Material, Integer> blockMap = blocks.get(e.getPlayer().getUniqueId());
                blockMap.computeIfPresent(e.getClickedBlock().getType(), (state, amount) -> amount++);
                blockMap.putIfAbsent(e.getClickedBlock().getType(), 1);
                blocks.put(e.getPlayer().getUniqueId(), blockMap);
                User.getInstance(e.getPlayer()).sendRawMessage("You added one " + e.getClickedBlock().getType());
                return;
            };
        }
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            addon.getLogger().info("DEBUG: right click");
            if (blocks.containsKey(e.getPlayer().getUniqueId())) {
                e.setCancelled(true);
                User.getInstance(e.getPlayer()).sendRawMessage("Finished!");
                addon.getChallengesManager().createSurroundingChallenge(name.get(e.getPlayer().getUniqueId()), blocks.get(e.getPlayer().getUniqueId()));
                blocks.remove(e.getPlayer().getUniqueId());
                name.remove(e.getPlayer().getUniqueId());
            }
        }
    }
}
