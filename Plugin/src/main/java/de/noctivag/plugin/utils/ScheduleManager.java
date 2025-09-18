package de.noctivag.plugin.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ScheduleManager {
    private final JavaPlugin plugin;
    private final Map<String, BossBar> notificationBars = new HashMap<>();

    public ScheduleManager(JavaPlugin plugin) {
        this.plugin = plugin;
        startGlobalTimer();
    }

    private void startGlobalTimer() {
        // Timer für alle 5 Minuten
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            LocalDateTime now = LocalDateTime.now();

            // Server-Restart-Ankündigung
            if (now.getHour() == 3 && now.getMinute() == 55) { // 5 Minuten vor 4 Uhr
                broadcastWarning("§c§lServer-Restart in 5 Minuten!", BarColor.RED);
            }

            // Event-Ankündigungen
            if (now.getMinute() % 30 == 25) { // 5 Minuten vor jedem Event
                broadcastWarning("§6§lEvent startet in 5 Minuten!", BarColor.YELLOW);
            }

        }, 20L * 60, 20L * 60); // Jede Minute prüfen
    }

    public void scheduleEvent(String eventName, Consumer<Void> action, Duration delay) {
        long ticks = delay.getSeconds() * 20;
        Bukkit.getScheduler().runTaskLater(plugin, () -> action.accept(null), ticks);

        // Ankündigung 5 Minuten vorher
        if (delay.getSeconds() > 300) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                broadcastWarning("§e" + eventName + " startet in 5 Minuten!", BarColor.YELLOW);
            }, ticks - (20L * 300));
        }
    }

    private void broadcastWarning(String message, BarColor color) {
        // Chat-Nachricht
        Bukkit.broadcast(Component.text(message));

        // Title für alle Spieler
        Title title = Title.title(
            Component.text(message),
            Component.text("§7Bereite dich vor!"),
            Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(3), Duration.ofSeconds(1))
        );

        // BossBar für 1 Minute anzeigen
        BossBar bar = Bukkit.createBossBar(message, color, BarStyle.SOLID);
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.showTitle(title);
            bar.addPlayer(p);
        });

        // BossBar nach 1 Minute entfernen
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            bar.removeAll();
        }, 20L * 60);
    }
}
