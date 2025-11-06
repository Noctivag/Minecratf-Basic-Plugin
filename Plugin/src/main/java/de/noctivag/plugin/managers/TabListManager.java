package de.noctivag.plugin.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TabListManager {
    private final Plugin plugin;
    private int taskId = -1;

    public TabListManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void startTabListUpdater() {
        if (taskId != -1) {
            return; // Bereits gestartet
        }

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            updateTabListForAll();
        }, 0L, 20L); // Jede Sekunde aktualisieren
    }

    public void stopTabListUpdater() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    private void updateTabListForAll() {
        // RAM Berechnung
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024); // MB
        long totalMemory = runtime.totalMemory() / (1024 * 1024); // MB
        long freeMemory = runtime.freeMemory() / (1024 * 1024); // MB
        long usedMemory = totalMemory - freeMemory;
        int ramPercentage = (int) ((usedMemory * 100) / maxMemory);

        // TPS Berechnung (Paper API)
        double tps = 20.0;
        try {
            tps = Bukkit.getTPS()[0]; // Letzten Minute TPS
            if (tps > 20.0) tps = 20.0; // Cap bei 20
        } catch (Exception e) {
            // Fallback wenn getTPS nicht verfügbar
        }

        // MSPT Berechnung (durchschnittliche Tick-Zeit in Millisekunden)
        double mspt = 0.0;
        try {
            long[] tickTimes = Bukkit.getServer().getTickTimes();
            if (tickTimes != null && tickTimes.length > 0) {
                long sum = 0;
                for (long time : tickTimes) {
                    sum += time;
                }
                mspt = (sum / tickTimes.length) / 1_000_000.0; // Nanosekunden zu Millisekunden
            }
        } catch (Exception e) {
            // Fallback
            mspt = (1000.0 / tps);
        }

        // Farben basierend auf Werten
        NamedTextColor ramColor = ramPercentage < 70 ? NamedTextColor.GREEN : 
                                  ramPercentage < 85 ? NamedTextColor.YELLOW : NamedTextColor.RED;
        
        NamedTextColor tpsColor = tps >= 19.0 ? NamedTextColor.GREEN : 
                                  tps >= 17.0 ? NamedTextColor.YELLOW : NamedTextColor.RED;
        
        NamedTextColor msptColor = mspt <= 40 ? NamedTextColor.GREEN : 
                                   mspt <= 50 ? NamedTextColor.YELLOW : NamedTextColor.RED;

        // Header erstellen
        Component header = Component.text()
            .append(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH))
            .append(Component.newline())
            .append(Component.text("  Server Performance  ", NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH))
            .build();

        // Footer erstellen
        Component footer = Component.text()
            .append(Component.text("RAM: ", NamedTextColor.GRAY))
            .append(Component.text(usedMemory + "MB", ramColor))
            .append(Component.text(" / ", NamedTextColor.DARK_GRAY))
            .append(Component.text(maxMemory + "MB", NamedTextColor.GRAY))
            .append(Component.text(" (" + ramPercentage + "%)", ramColor))
            .append(Component.newline())
            .append(Component.text("TPS: ", NamedTextColor.GRAY))
            .append(Component.text(String.format("%.2f", tps), tpsColor))
            .append(Component.text(" / 20.0", NamedTextColor.DARK_GRAY))
            .append(Component.newline())
            .append(Component.text("MSPT: ", NamedTextColor.GRAY))
            .append(Component.text(String.format("%.2f", mspt) + "ms", msptColor))
            .append(Component.text(" / 50ms", NamedTextColor.DARK_GRAY))
            .append(Component.newline())
            .append(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH))
            .build();

        // Für alle Online-Spieler aktualisieren
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendPlayerListHeaderAndFooter(header, footer);
        }
    }

    public void clearTabList(Player player) {
        player.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
    }
}
