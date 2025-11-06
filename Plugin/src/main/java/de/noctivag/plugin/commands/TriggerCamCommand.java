package de.noctivag.plugin.commands;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TriggerCamCommand implements CommandExecutor {
    private final Map<UUID, GameMode> previousGameModes;
    private final Map<UUID, Location> previousLocations;
    private final Map<UUID, ArmorStand> cameraDummies;
    private final Map<UUID, Boolean> previousFlyingState;
    private final Map<UUID, Boolean> previousAllowFlight;
    private final Map<UUID, Integer> distanceCheckTasks;
    
    private Plugin plugin;
    private int maxDistance = 300;

    public TriggerCamCommand() {
        this.previousGameModes = new HashMap<>();
        this.previousLocations = new HashMap<>();
        this.cameraDummies = new HashMap<>();
        this.previousFlyingState = new HashMap<>();
        this.previousAllowFlight = new HashMap<>();
        this.distanceCheckTasks = new HashMap<>();
    }
    
    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
        if (plugin instanceof de.noctivag.plugin.Plugin mainPlugin) {
            this.maxDistance = mainPlugin.getConfigManager().getConfig().getInt("camera.max-distance", 300);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Befehl kann nur von Spielern ausgeführt werden!");
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        if (!player.hasPermission("plugin.cam")) {
            player.sendMessage("§cDu hast keine Berechtigung für diesen Befehl!");
            return true;
        }

        // Wenn der Spieler bereits im Kamera-Modus ist, wechsle zurück
        if (previousGameModes.containsKey(playerId)) {
            GameMode previousMode = previousGameModes.remove(playerId);
            Location previousLocation = previousLocations.remove(playerId);
            ArmorStand dummy = cameraDummies.remove(playerId);
            Boolean wasFlying = previousFlyingState.remove(playerId);
            Boolean couldFly = previousAllowFlight.remove(playerId);
            
            // Stoppe Distance-Check Task
            Integer taskId = distanceCheckTasks.remove(playerId);
            if (taskId != null && plugin != null) {
                Bukkit.getScheduler().cancelTask(taskId);
            }
            
            // Entferne Unsichtbarkeit
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            
            // Setze GameMode zurück
            player.setGameMode(previousMode);
            
            // Stelle Flugstatus wieder her
            if (couldFly != null) {
                player.setAllowFlight(couldFly);
            }
            if (wasFlying != null && wasFlying) {
                player.setFlying(true);
            }
            
            // Teleportiere zur ursprünglichen Position
            if (previousLocation != null) {
                player.teleport(previousLocation);
            }
            
            // Entferne den Dummy-ArmorStand
            if (dummy != null && !dummy.isDead()) {
                dummy.remove();
            }
            
            player.sendMessage("§aKamera-Modus deaktiviert. Zurück zu " + previousMode.name() + ".");
        } else {
            // Speichere den aktuellen GameMode und Position
            GameMode currentMode = player.getGameMode();
            Location currentLocation = player.getLocation().clone();
            
            // Erstelle einen sichtbaren ArmorStand als Spieler-Dummy
            ArmorStand dummy = player.getWorld().spawn(currentLocation, ArmorStand.class);
            dummy.setVisible(true);
            dummy.setGravity(false);
            dummy.setInvulnerable(true);
            dummy.setBasePlate(false);
            dummy.setArms(true);
            dummy.setSmall(false);
            dummy.setCollidable(false);
            dummy.setCanPickupItems(false);
            dummy.setCustomName("§e" + player.getName());
            dummy.setCustomNameVisible(true);
            
            // Erstelle Spielerkopf mit Skin
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setOwningPlayer(player);
                playerHead.setItemMeta(skullMeta);
            }
            
            // Kopiere komplettes Spieler-Equipment zum Dummy
            dummy.getEquipment().setHelmet(playerHead);  // Spielerkopf mit Skin
            dummy.getEquipment().setChestplate(player.getInventory().getChestplate());
            dummy.getEquipment().setLeggings(player.getInventory().getLeggings());
            dummy.getEquipment().setBoots(player.getInventory().getBoots());
            dummy.getEquipment().setItemInMainHand(player.getInventory().getItemInMainHand());
            dummy.getEquipment().setItemInOffHand(player.getInventory().getItemInOffHand());
            
            // Speichere Flugstatus
            previousFlyingState.put(playerId, player.isFlying());
            previousAllowFlight.put(playerId, player.getAllowFlight());
            
            // Speichere alles
            previousGameModes.put(playerId, currentMode);
            previousLocations.put(playerId, currentLocation);
            cameraDummies.put(playerId, dummy);
            
            // Mache Spieler unsichtbar und aktiviere Flug in Adventure-Modus
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
            player.setFlying(true);
            
            // Starte Distance-Check Task
            if (plugin != null) {
                int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                    if (!player.isOnline() || !previousLocations.containsKey(playerId)) {
                        return;
                    }
                    
                    Location startLocation = previousLocations.get(playerId);
                    Location currentLoc = player.getLocation();
                    double distance = startLocation.distance(currentLoc);
                    
                    if (distance > maxDistance) {
                        // Berechne Richtungsvektor und limitiere auf maxDistance
                        double ratio = maxDistance / distance;
                        double newX = startLocation.getX() + (currentLoc.getX() - startLocation.getX()) * ratio;
                        double newY = startLocation.getY() + (currentLoc.getY() - startLocation.getY()) * ratio;
                        double newZ = startLocation.getZ() + (currentLoc.getZ() - startLocation.getZ()) * ratio;
                        
                        Location newLocation = new Location(startLocation.getWorld(), newX, newY, newZ, 
                                                            currentLoc.getYaw(), currentLoc.getPitch());
                        player.teleport(newLocation);
                        player.sendMessage("§cMaximale Kamera-Distanz erreicht! (" + maxDistance + " Blöcke)");
                    }
                }, 10L, 10L); // Alle 0.5 Sekunden prüfen
                
                distanceCheckTasks.put(playerId, taskId);
            }
            
            player.sendMessage("§aKamera-Modus aktiviert. Nutze den Befehl erneut, um zurückzukehren.");
            player.sendMessage("§7Du kannst fliegen und dich umsehen. Dein Körper bleibt sichtbar.");
            player.sendMessage("§7Maximale Distanz: §e" + maxDistance + " Blöcke");
        }

        return true;
    }

    /**
     * Entfernt einen Spieler aus der Tracking-Map beim Verlassen des Servers
     * @param playerId Die UUID des Spielers
     */
    public void removePlayer(UUID playerId) {
        previousGameModes.remove(playerId);
        previousLocations.remove(playerId);
        
        ArmorStand dummy = cameraDummies.remove(playerId);
        if (dummy != null && !dummy.isDead()) {
            dummy.remove();
        }
    }

    /**
     * Stellt alle Spieler wieder her (z.B. beim Plugin-Disable)
     */
    public void restoreAllPlayers() {
        // Entferne alle Dummies
        for (ArmorStand dummy : cameraDummies.values()) {
            if (dummy != null && !dummy.isDead()) {
                dummy.remove();
            }
        }
        
        previousGameModes.clear();
        previousLocations.clear();
        cameraDummies.clear();
    }

    /**
     * Prüft ob ein Spieler im Kamera-Modus ist
     * @param playerId Die UUID des Spielers
     * @return true wenn der Spieler im Kamera-Modus ist
     */
    public boolean isInCameraMode(UUID playerId) {
        return previousGameModes.containsKey(playerId);
    }
}
