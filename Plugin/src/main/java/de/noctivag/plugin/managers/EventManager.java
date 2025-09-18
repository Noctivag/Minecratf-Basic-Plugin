package de.noctivag.plugin.managers;

import de.noctivag.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.util.*;
import java.time.Duration;

public class EventManager {
    private final Plugin plugin;
    private final Map<String, BossBar> activeBossBars = new HashMap<>();
    private final Map<String, Set<UUID>> eventParticipants = new HashMap<>();
    private final Map<UUID, Integer> playerPoints = new HashMap<>();

    public EventManager(Plugin plugin) {
        this.plugin = plugin;
        scheduleEvents();
    }

    private void scheduleEvents() {
        // Dragon Event alle 2 Stunden
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            startDragonEvent();
        }, 20L * 60 * 120, 20L * 60 * 120);

        // Wither Event jede Stunde
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            startWitherEvent();
        }, 20L * 60 * 60, 20L * 60 * 60);

        // Zombie-Horde alle 30 Minuten
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            startZombieHorde();
        }, 20L * 60 * 30, 20L * 60 * 30);
    }

    public void startDragonEvent() {
        World end = Bukkit.getWorlds().stream()
            .filter(w -> w.getEnvironment() == World.Environment.THE_END)
            .findFirst()
            .orElse(null);

        if (end == null) return;

        BossBar bossBar = Bukkit.createBossBar(
            "§5§lDrachen-Event", BarColor.PURPLE, BarStyle.SOLID);
        activeBossBars.put("dragon", bossBar);

        // Spawn Dragon
        Location spawnLoc = end.getSpawnLocation();
        EnderDragon dragon = (EnderDragon) end.spawnEntity(spawnLoc, EntityType.ENDER_DRAGON);

        // Ankündigung
        Bukkit.broadcast(Component.text("§5§lDas Drachen-Event hat begonnen!"));
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.showTitle(Title.title(
                Component.text("§5§lDrachen-Event"),
                Component.text("§7Der Kampf beginnt!"),
                Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(3), Duration.ofSeconds(1))
            ));
            bossBar.addPlayer(p);
        });

        // Event-Ende nach 15 Minuten
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            endEvent("dragon");
        }, 20L * 60 * 15);
    }

    public void startWitherEvent() {
        // Ähnliche Implementierung wie Dragon-Event
    }

    public void startZombieHorde() {
        // Ähnliche Implementierung wie Dragon-Event
    }

    private void endEvent(String eventId) {
        BossBar bossBar = activeBossBars.remove(eventId);
        if (bossBar != null) {
            bossBar.removeAll();
        }

        Set<UUID> participants = eventParticipants.remove(eventId);
        if (participants != null) {
            participants.forEach(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    addEventPoints(player, 100); // Basis-Punkte für Teilnahme
                }
            });
        }
    }

    public void addEventPoints(Player player, int points) {
        UUID uuid = player.getUniqueId();
        playerPoints.merge(uuid, points, Integer::sum);
        player.sendMessage(Component.text("§a+" + points + " Event-Punkte!"));
    }

    public int getEventPoints(Player player) {
        return playerPoints.getOrDefault(player.getUniqueId(), 0);
    }

    public void joinEvent(Player player, String eventId) {
        eventParticipants.computeIfAbsent(eventId, k -> new HashSet<>())
            .add(player.getUniqueId());
        player.sendMessage(Component.text("§aDu nimmst nun am Event teil!"));
    }
}
