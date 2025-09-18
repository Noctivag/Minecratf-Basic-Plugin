package de.noctivag.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

public class WorkbenchCommand implements CommandExecutor {
    private static final Component NO_PERMISSION = Component.text("Dafür hast du keine Berechtigung!").color(NamedTextColor.RED);
    private static final Component PLAYERS_ONLY = Component.text("Nur Spieler können diesen Befehl nutzen.").color(NamedTextColor.RED);

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(PLAYERS_ONLY);
            return true;
        }

        try {
            switch (label.toLowerCase()) {
                case "craftingtable", "craft", "workbench" -> {
                    if (!player.hasPermission("workbench.craftingtable")) {
                        player.sendMessage(NO_PERMISSION);
                        return true;
                    }
                    player.openInventory(player.getServer().createInventory(player, InventoryType.WORKBENCH));
                }
                case "anvil" -> {
                    if (!player.hasPermission("workbench.anvil")) {
                        player.sendMessage(NO_PERMISSION);
                        return true;
                    }
                    player.openInventory(player.getServer().createInventory(player, InventoryType.ANVIL));
                }
                case "enderchest", "ec" -> {
                    if (!player.hasPermission("workbench.enderchest")) {
                        player.sendMessage(NO_PERMISSION);
                        return true;
                    }
                    player.openInventory(player.getEnderChest());
                }
                case "grindstone" -> {
                    if (!player.hasPermission("workbench.grindstone")) {
                        player.sendMessage(NO_PERMISSION);
                        return true;
                    }
                    player.openInventory(player.getServer().createInventory(player, InventoryType.GRINDSTONE));
                }
                case "smithingtable" -> {
                    if (!player.hasPermission("workbench.smithingtable")) {
                        player.sendMessage(NO_PERMISSION);
                        return true;
                    }
                    player.openInventory(player.getServer().createInventory(player, InventoryType.SMITHING));
                }
                case "stonecutter" -> {
                    if (!player.hasPermission("workbench.stonecutter")) {
                        player.sendMessage(NO_PERMISSION);
                        return true;
                    }
                    player.openInventory(player.getServer().createInventory(player, InventoryType.STONECUTTER));
                }
                case "loom" -> {
                    if (!player.hasPermission("workbench.loom")) {
                        player.sendMessage(NO_PERMISSION);
                        return true;
                    }
                    player.openInventory(player.getServer().createInventory(player, InventoryType.LOOM));
                }
                case "cartography", "cartographytable" -> {
                    if (!player.hasPermission("workbench.cartography")) {
                        player.sendMessage(NO_PERMISSION);
                        return true;
                    }
                    player.openInventory(player.getServer().createInventory(player, InventoryType.CARTOGRAPHY));
                }
                default -> {
                    player.sendMessage(Component.text("Unbekannter Werkbank-Typ!").color(NamedTextColor.RED));
                    return false;
                }
            }
        } catch (Exception e) {
            player.sendMessage(Component.text("Ein Fehler ist aufgetreten!").color(NamedTextColor.RED));
            return false;
        }
        return true;
    }
}
