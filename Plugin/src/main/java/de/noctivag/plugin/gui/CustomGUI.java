package de.noctivag.plugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class CustomGUI implements InventoryHolder {
    private final Inventory inventory;

    public CustomGUI(String title, int rows) {
        this.inventory = Bukkit.createInventory(this, rows * 9, title);
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }
}
