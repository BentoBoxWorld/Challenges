package bskyblock.addon.challenges.commands.admin;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import bskyblock.addon.challenges.ChallengesAddon;
import us.tastybento.bskyblock.Constants;
import us.tastybento.bskyblock.api.commands.CompositeCommand;
import us.tastybento.bskyblock.api.commands.User;

public class CreateSurrounding extends CompositeCommand implements Listener {


    private ChallengesAddon addon;
    HashMap<UUID,SurroundChallengeBuilder> inProgress = new HashMap<>();

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
        inProgress.put(user.getUniqueId(), new SurroundChallengeBuilder(addon).owner(user).name(args.get(0)));
        return true;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        e.setCancelled(inProgress.containsKey(e.getPlayer().getUniqueId()) ? true : false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e) {
        inProgress.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public boolean onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            addon.getLogger().info("DEBUG: left click");
            if (inProgress.containsKey(e.getPlayer().getUniqueId())) {
                // Prevent damage
                e.setCancelled(true);
                inProgress.get(e.getPlayer().getUniqueId()).addBlock(e.getClickedBlock().getType());
                User.getInstance(e.getPlayer()).sendRawMessage("You added one " + e.getClickedBlock().getType());
                return true;
            }
        }
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            addon.getLogger().info("DEBUG: right click");
            return finished(e, e.getPlayer().getUniqueId());
        }
        return false;
    }

    private boolean finished(Cancellable e, UUID uuid) {
        if (inProgress.containsKey(uuid)) {
            e.setCancelled(true);
            boolean status = inProgress.get(uuid).build();
            inProgress.remove(uuid);
            return status;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public boolean onPlayerInteract(PlayerInteractAtEntityEvent e) {
        addon.getLogger().info("DEBUG: right click entity");
        return finished(e, e.getPlayer().getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public boolean onLeft(EntityDamageByEntityEvent e) {
        addon.getLogger().info("DEBUG: left click entity");
        if (!(e.getDamager() instanceof Player)) {
            return false;
        }
        Player player = (Player)e.getDamager();
        if (inProgress.containsKey(player.getUniqueId())) {
            // Prevent damage
            e.setCancelled(true);
            inProgress.get(player.getUniqueId()).addEntity(e.getEntityType());
            User.getInstance(player).sendRawMessage("You added one " + e.getEntityType());
            return true;
        }
        return false;
    }
    

}
