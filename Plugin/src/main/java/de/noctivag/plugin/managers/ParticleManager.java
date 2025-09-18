package de.noctivag.plugin.managers;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParticleManager {
    private final JavaPlugin plugin;
    private final Map<UUID, BukkitRunnable> activeEffects = new HashMap<>();
    private final Map<UUID, Particle> playerParticles = new HashMap<>();

    public ParticleManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setPlayerParticle(Player player, Particle particle) {
        UUID playerId = player.getUniqueId();
        stopEffect(player);

        if (particle == null) {
            playerParticles.remove(playerId);
            return;
        }

        playerParticles.put(playerId, particle);
        startEffect(player, particle);
    }

    private void startEffect(Player player, Particle particle) {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    activeEffects.remove(player.getUniqueId());
                    return;
                }

                player.getWorld().spawnParticle(
                    particle,
                    player.getLocation().add(0, 2, 0),
                    3,
                    0.2,
                    0.2,
                    0.2,
                    0
                );
            }
        };

        task.runTaskTimer(plugin, 0L, 5L);
        activeEffects.put(player.getUniqueId(), task);
    }

    public void stopEffect(Player player) {
        UUID playerId = player.getUniqueId();
        if (activeEffects.containsKey(playerId)) {
            activeEffects.get(playerId).cancel();
            activeEffects.remove(playerId);
        }
    }

    public void stopAllEffects() {
        activeEffects.values().forEach(BukkitRunnable::cancel);
        activeEffects.clear();
    }

    public Particle getPlayerParticle(Player player) {
        return playerParticles.get(player.getUniqueId());
    }
}
