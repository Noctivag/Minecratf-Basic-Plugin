package de.noctivag.plugin.listeners.entity;

import de.noctivag.plugin.Plugin;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GhastSpeedListener implements Listener {

    private final Plugin plugin;
    private final Map<UUID, Integer> ghastTasks = new HashMap<>();

    public GhastSpeedListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() != EntityType.GHAST) {
            return;
        }

        Player player = event.getPlayer();
        Ghast ghast = (Ghast) event.getRightClicked();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType().toString().equals("SADDLE") && itemInHand.hasItemMeta() && itemInHand.getItemMeta().displayName() != null && itemInHand.getItemMeta().displayName().equals(Component.text("Happy Ghast Saddle"))) {
            if (ghast.getPassengers().isEmpty() && !ghastTasks.containsKey(ghast.getUniqueId())) {
                ghast.addPassenger(player);

                int soulSpeedLevel = itemInHand.getEnchantmentLevel(Enchantment.SOUL_SPEED);
                
                BukkitRunnable movementTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!ghast.isValid() || ghast.getPassengers().isEmpty() || !(ghast.getPassengers().get(0) instanceof Player)) {
                            this.cancel();
                            ghastTasks.remove(ghast.getUniqueId());
                            return;
                        }

                        Player rider = (Player) ghast.getPassengers().get(0);
                        Vector direction = rider.getLocation().getDirection();
                        
                        double speedMultiplier = 1.0 + (soulSpeedLevel * 0.5); // 1.5, 2.0, 2.5
                        
                        ghast.setVelocity(direction.multiply(speedMultiplier));
                    }
                };

                movementTask.runTaskTimer(plugin, 0L, 5L);
                ghastTasks.put(ghast.getUniqueId(), movementTask.getTaskId());
            }
        }
    }

    @EventHandler
    public void onEntityDismount(EntityDismountEvent event) {
        if (event.getDismounted().getType() == EntityType.GHAST && event.getEntity() instanceof Player) {
            Ghast ghast = (Ghast) event.getDismounted();
            UUID ghastId = ghast.getUniqueId();

            if (ghastTasks.containsKey(ghastId)) {
                int taskId = ghastTasks.get(ghastId);
                plugin.getServer().getScheduler().cancelTask(taskId);
                ghastTasks.remove(ghastId);
                
                // Reset velocity to prevent it from flying off
                ghast.setVelocity(new Vector(0, 0, 0));
            }
        }
    }
}
