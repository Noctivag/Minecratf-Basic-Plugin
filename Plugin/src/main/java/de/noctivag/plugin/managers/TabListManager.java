package de.noctivag.plugin.managers;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.messages.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TabListManager {
    private final Plugin plugin;
    private final MessageManager messageManager;
    private int taskId = -1;

    public TabListManager(Plugin plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
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
        Component header = messageManager.getMessage("tablist.header.line1")
            .append(Component.newline())
            .append(messageManager.getMessage("tablist.header.line2"))
            .append(Component.newline())
            .append(messageManager.getMessage("tablist.header.line3"));

        // Footer erstellen
        Component footer = messageManager.getComponentMessage("tablist.footer.ram",
                Component.text(usedMemory + "MB", ramColor),
                Component.text(" / "),
                Component.text(maxMemory + "MB", NamedTextColor.GRAY),
                Component.text(" (" + ramPercentage + "%)", ramColor)
            )
            .append(Component.newline())
            .append(messageManager.getComponentMessage("tablist.footer.tps",
                Component.text(String.format("%.2f", tps), tpsColor),
                Component.text(" / 20.0")
            ))
            .append(Component.newline())
            .append(messageManager.getComponentMessage("tablist.footer.mspt",
                Component.text(String.format("%.2f", mspt) + "ms", msptColor),
                Component.text(" / 50ms")
            ))
            .append(Component.newline())
            .append(messageManager.getMessage("tablist.footer.line"));

        // Für alle Online-Spieler aktualisieren
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendPlayerListHeaderAndFooter(header, footer);
        }
    }

    public void clearTabList(Player player) {
        player.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
    }
}
