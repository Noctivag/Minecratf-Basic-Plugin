package de.noctivag.plugin.listeners;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.gui.CosmeticsMenu;
import de.noctivag.plugin.gui.CustomGUI;
import de.noctivag.plugin.gui.MainMenu;
import de.noctivag.plugin.managers.ParticleManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.Particle;

public class GUIListener implements Listener {
    private final Plugin plugin;
    private final ParticleManager particleManager;

    public GUIListener(Plugin plugin, ParticleManager particleManager) {
        this.plugin = plugin;
        this.particleManager = particleManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof CustomGUI)) {
            return;
        }

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Hauptmenü Klicks
        if (event.getInventory().getHolder() instanceof MainMenu) {
            handleMainMenuClick(event.getSlot(), player);
        }
        // Cosmetics Menü Klicks
        else if (event.getInventory().getHolder() instanceof CosmeticsMenu cosmeticsMenu) {
            handleCosmeticsMenuClick(event.getSlot(), player, cosmeticsMenu);
        }
    }

    private void handleMainMenuClick(int slot, Player player) {
        switch (slot) {
            case 10 -> new CosmeticsMenu().open(player);
            case 12 -> player.sendMessage("§aWarps werden bald verfügbar sein!");
            case 14 -> player.sendMessage("§aTägliche Belohnungen werden bald verfügbar sein!");
            case 16 -> player.sendMessage("§aStatistiken werden bald verfügbar sein!");
            case 28 -> player.sendMessage("§aShop wird bald verfügbar sein!");
            case 30 -> player.sendMessage("§aEvents werden bald verfügbar sein!");
            case 32 -> player.sendMessage("§aNachrichtensystem wird bald verfügbar sein!");
            case 34 -> player.sendMessage("§aEinstellungen werden bald verfügbar sein!");
        }
    }

    private void handleCosmeticsMenuClick(int slot, Player player, CosmeticsMenu menu) {
        if (slot == 22) { // Deaktivieren Button
            particleManager.stopEffect(player);
            player.sendMessage("§aPartikeleffekte wurden deaktiviert!");
            return;
        }

        Particle selectedParticle = menu.getParticleAtSlot(slot);
        if (selectedParticle != null) {
            particleManager.setPlayerParticle(player, selectedParticle);
            player.sendMessage("§aPartikeleffekt wurde aktiviert!");
        }
    }
}
