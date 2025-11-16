package de.noctivag.plugin;

import de.noctivag.plugin.messages.MessageManager;
import de.noctivag.plugin.permissions.PermissionManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

public class WorkbenchCommand implements CommandExecutor {

    private final MessageManager messageManager;
    private final PermissionManager permissionManager;

    public WorkbenchCommand(MessageManager messageManager, PermissionManager permissionManager) {
        this.messageManager = messageManager;
        this.permissionManager = permissionManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messageManager.getError("error.players_only"));
            return true;
        }

        try {
            switch (label.toLowerCase()) {
                case "craftingtable", "craft", "workbench" -> {
                    if (!permissionManager.hasPermission(player, "craftingtable")) {
                        player.sendMessage(messageManager.getError("error.no_permission"));
                        return true;
                    }
                    player.openInventory(player.getServer().createInventory(player, InventoryType.WORKBENCH));
                }
                case "anvil" -> {
                    if (!permissionManager.hasPermission(player, "anvil")) {
                        player.sendMessage(messageManager.getError("error.no_permission"));
                        return true;
                    }
                    player.openInventory(player.getServer().createInventory(player, InventoryType.ANVIL));
                }
                case "enderchest", "ec" -> {
                    if (!permissionManager.hasPermission(player, "enderchest")) {
                        player.sendMessage(messageManager.getError("error.no_permission"));
                        return true;
                    }
                    player.openInventory(player.getEnderChest());
                }
                case "grindstone" -> {
                    if (!permissionManager.hasPermission(player, "grindstone")) {
                        player.sendMessage(messageManager.getError("error.no_permission"));
                        return true;
                    }
                    player.openInventory(player.getServer().createInventory(player, InventoryType.GRINDSTONE));
                }
                case "smithingtable" -> {
                    if (!permissionManager.hasPermission(player, "smithingtable")) {
                        player.sendMessage(messageManager.getError("error.no_permission"));
                        return true;
                    }
                    player.openInventory(player.getServer().createInventory(player, InventoryType.SMITHING));
                }
                case "stonecutter" -> {
                    if (!permissionManager.hasPermission(player, "stonecutter")) {
                        player.sendMessage(messageManager.getError("error.no_permission"));
                        return true;
                    }
                    player.openInventory(player.getServer().createInventory(player, InventoryType.STONECUTTER));
                }
                case "loom" -> {
                    if (!permissionManager.hasPermission(player, "loom")) {
                        player.sendMessage(messageManager.getError("error.no_permission"));
                        return true;
                    }
                    player.openInventory(player.getServer().createInventory(player, InventoryType.LOOM));
                }
                case "cartography", "cartographytable" -> {
                    if (!permissionManager.hasPermission(player, "cartography")) {
                        player.sendMessage(messageManager.getError("error.no_permission"));
                        return true;
                    }
                    player.openInventory(player.getServer().createInventory(player, InventoryType.CARTOGRAPHY));
                }
                default -> {
                    player.sendMessage(messageManager.getError("error.unknown_command"));
                    return false;
                }
            }
        } catch (Exception e) {
            player.sendMessage(messageManager.getError("error.generic"));
            return false;
        }
        return true;
    }
}
