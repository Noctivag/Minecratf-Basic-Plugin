package de.noctivag.plugin.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;
import java.util.Arrays;

public class MainMenu extends CustomGUI {

    public MainMenu() {
        super("§6§lServer Menü", 6); // 6 Reihen Inventar
        initializeItems();
    }

    private void initializeItems() {
        // Cosmetics (Partikel, Trails)
        setItem(10, Material.NETHER_STAR, "§b§lCosmetics", "§7Ändere deine Partikeleffekte", "§7und Trails!");

        // Warps & Teleport
        setItem(12, Material.ENDER_PEARL, "§5§lWarps", "§7Teleportiere dich zu", "§7wichtigen Orten!");

        // Daily Rewards
        setItem(14, Material.CHEST, "§e§lDaily Rewards", "§7Hole dir deine tägliche", "§7Belohnung ab!");

        // Stats & Achievements
        setItem(16, Material.DIAMOND, "§b§lStatistiken", "§7Siehe deine Erfolge", "§7und Statistiken!");

        // Shop & Economy
        setItem(28, Material.EMERALD, "§a§lShop", "§7Kaufe und verkaufe Items!");

        // Events & Minigames
        setItem(30, Material.DRAGON_HEAD, "§c§lEvents", "§7Aktuelle Events und", "§7Bosskämpfe!");

        // Mail & Messages
        setItem(32, Material.WRITABLE_BOOK, "§f§lNachrichten", "§7Sende und empfange", "§7Nachrichten!");

        // Settings
        setItem(34, Material.REDSTONE_TORCH, "§c§lEinstellungen", "§7Passe dein Spielerlebnis an!");
    }

    private void setItem(int slot, Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.stream(lore).map(Component::text).toList());
        item.setItemMeta(meta);
        getInventory().setItem(slot, item);
    }
}
