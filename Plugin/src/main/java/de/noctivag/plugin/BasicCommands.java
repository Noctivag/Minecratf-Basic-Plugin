package de.noctivag.plugin;

import de.noctivag.plugin.managers.CooldownManager;
import de.noctivag.plugin.messages.MessageManager;
import de.noctivag.plugin.modules.ModuleManager;
import de.noctivag.plugin.permissions.PermissionManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BasicCommands implements CommandExecutor {
    private final Plugin plugin;
    private final MessageManager messageManager;
    private final PermissionManager permissionManager;

    public BasicCommands(Plugin plugin, MessageManager messageManager, PermissionManager permissionManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.permissionManager = permissionManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messageManager.getError("error.players_only"));
            return true;
        }

        ModuleManager moduleManager = plugin.getModuleManager();
        CooldownManager cooldownManager = plugin.getCooldownManager();

        try {
            switch (label.toLowerCase()) {
                case "heal" -> {
                    if (!moduleManager.isFeatureEnabled("basic-commands", "heal")) {
                        player.sendMessage(messageManager.getError("error.module_disabled"));
                        return true;
                    }

                    if (!permissionManager.hasPermission(player, "heal")) {
                        player.sendMessage(messageManager.getError("error.no_permission"));
                        return true;
                    }

                    if (cooldownManager.hasCooldown(player, "heal")) {
                        long remaining = cooldownManager.getRemainingCooldown(player, "heal");
                        player.sendMessage(messageManager.getError("error.cooldown", remaining));
                        return true;
                    }

                    player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
                    player.setFireTicks(0);
                    player.sendMessage(messageManager.getMessage("basic_commands.heal.success"));

                    cooldownManager.setCooldownFromConfig(player, "heal");
                }
                case "feed" -> {
                    if (!moduleManager.isFeatureEnabled("basic-commands", "feed")) {
                        player.sendMessage(messageManager.getError("error.module_disabled"));
                        return true;
                    }

                    if (!permissionManager.hasPermission(player, "feed")) {
                        player.sendMessage(messageManager.getError("error.no_permission"));
                        return true;
                    }

                    if (cooldownManager.hasCooldown(player, "feed")) {
                        long remaining = cooldownManager.getRemainingCooldown(player, "feed");
                        player.sendMessage(messageManager.getError("error.cooldown", remaining));
                        return true;
                    }

                    player.setFoodLevel(20);
                    player.setSaturation(20f);
                    player.sendMessage(messageManager.getMessage("basic_commands.feed.success"));

                    cooldownManager.setCooldownFromConfig(player, "feed");
                }
                case "clearinventory", "ci" -> {
                    if (!moduleManager.isFeatureEnabled("basic-commands", "clearinventory")) {
                        player.sendMessage(messageManager.getError("error.module_disabled"));
                        return true;
                    }

                    if (!permissionManager.hasPermission(player, "clearinventory")) {
                        player.sendMessage(messageManager.getError("error.no_permission"));
                        return true;
                    }

                    player.getInventory().clear();
                    player.sendMessage(messageManager.getMessage("basic_commands.clearinventory.success"));
                }
                case "fly" -> {
                    if (!moduleManager.isFeatureEnabled("basic-commands", "fly")) {
                        player.sendMessage(messageManager.getError("error.module_disabled"));
                        return true;
                    }

                    if (!permissionManager.hasPermission(player, "fly")) {
                        player.sendMessage(messageManager.getError("error.no_permission"));
                        return true;
                    }

                    boolean newState = !player.getAllowFlight();
                    player.setAllowFlight(newState);
                    player.setFlying(newState);
                    player.sendMessage(newState ? messageManager.getMessage("basic_commands.fly.enabled") : messageManager.getMessage("basic_commands.fly.disabled"));
                }
                case "gmc" -> {
                    if (!moduleManager.isFeatureEnabled("gamemode", "creative")) {
                        player.sendMessage(messageManager.getError("error.module_disabled"));
                        return true;
                    }

                    if (!permissionManager.hasPermission(player, "gmc")) {
                        player.sendMessage(messageManager.getError("error.no_permission"));
                        return true;
                    }

                    player.setGameMode(org.bukkit.GameMode.CREATIVE);
                    player.sendMessage(messageManager.getMessage("basic_commands.gamemode.creative"));
                }
                case "gms" -> {
                    if (!moduleManager.isFeatureEnabled("gamemode", "survival")) {
                        player.sendMessage(messageManager.getError("error.module_disabled"));
                        return true;
                    }

                    if (!permissionManager.hasPermission(player, "gms")) {
                        player.sendMessage(messageManager.getError("error.no_permission"));
                        return true;
                    }

                    player.setGameMode(org.bukkit.GameMode.SURVIVAL);
                    player.sendMessage(messageManager.getMessage("basic_commands.gamemode.survival"));
                }
                case "gmsp" -> {
                    if (!moduleManager.isFeatureEnabled("gamemode", "spectator")) {
                        player.sendMessage(messageManager.getError("error.module_disabled"));
                        return true;
                    }

                    if (!permissionManager.hasPermission(player, "gmsp")) {
                        player.sendMessage(messageManager.getError("error.no_permission"));
                        return true;
                    }

                    player.setGameMode(org.bukkit.GameMode.SPECTATOR);
                    player.sendMessage(messageManager.getMessage("basic_commands.gamemode.spectator"));
                }
                default -> {
                    player.sendMessage(messageManager.getError("error.unknown_command"));
                    return false;
                }
            }
        } catch (Exception e) {
            player.sendMessage(messageManager.getError("error.generic"));
            plugin.getLogger().severe("Error in BasicCommands: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
