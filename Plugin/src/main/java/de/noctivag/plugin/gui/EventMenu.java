package de.noctivag.plugin.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;
import java.util.Arrays;
import java.util.List;

public class EventMenu extends CustomGUI {

    public EventMenu() {
        super("§c§lEvent Menü", 4);
        initializeItems();
    }

    private void initializeItems() {
        // Aktive Events
        setItem(10, Material.DRAGON_HEAD, "§5§lDrachen-Event",
            "§7Stelle dich dem Enderdrachen!",
            "§eKlicke zum Beitreten",
            "§aBelohnung: Dragon Egg");

        setItem(12, Material.WITHER_SKELETON_SKULL, "§8§lWither-Boss",
            "§7Bezwinge den Wither-Boss!",
            "§eKlicke zum Beitreten",
            "§aBelohnung: Nether Star");

        setItem(14, Material.ZOMBIE_HEAD, "§2§lZombie-Horde",
            "§7Überlebe die Zombie-Invasion!",
            "§eKlicke zum Beitreten",
            "§aBelohnung: Totem der Unsterblichkeit");

        // Zeitplan
        setItem(28, Material.CLOCK, "§e§lNächste Events",
            "§7Drachen-Event: §aIn 30 Minuten",
            "§7Wither-Boss: §aIn 1 Stunde",
            "§7Zombie-Horde: §aIn 2 Stunden");

        // Statistiken
        setItem(30, Material.PAPER, "§b§lDeine Event-Stats",
            "§7Teilnahmen: §e12",
            "§7Siege: §a5",
            "§7Rang: §6#3");

        // Belohnungen
        setItem(32, Material.CHEST, "§6§lEvent-Shop",
            "§7Tausche deine Event-Punkte",
            "§7gegen tolle Belohnungen ein!",
            "§eDeine Punkte: §61250");

        // Zurück zum Hauptmenü
        setItem(35, Material.BARRIER, "§c§lZurück",
            "§7Zum Hauptmenü");
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
