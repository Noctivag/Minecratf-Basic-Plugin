package de.noctivag.plugin;

import de.noctivag.plugin.managers.CooldownManager;
import de.noctivag.plugin.modules.ModuleManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BasicCommands implements CommandExecutor {
    private final Plugin plugin;
    private static final Component NO_PERMISSION = Component.text("Dafür hast du keine Berechtigung!").color(NamedTextColor.RED);
    private static final Component PLAYERS_ONLY = Component.text("Nur Spieler können diesen Befehl nutzen.").color(NamedTextColor.RED);
    private static final Component MODULE_DISABLED = Component.text("Diese Funktion ist derzeit deaktiviert!").color(NamedTextColor.RED);

    public BasicCommands(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(PLAYERS_ONLY);
            return true;
        }

        ModuleManager moduleManager = plugin.getModuleManager();
        CooldownManager cooldownManager = plugin.getCooldownManager();

        try {
            switch (label.toLowerCase()) {
                case "heal" -> {
                    // Check if module is enabled
                    if (!moduleManager.isFeatureEnabled("basic-commands", "heal")) {
                        player.sendMessage(MODULE_DISABLED);
                        return true;
                    }

                    if (!player.hasPermission("basiccommands.heal")) {
                        player.sendMessage(NO_PERMISSION);
                        return true;
                    }

                    // Check cooldown
                    if (cooldownManager.hasCooldown(player, "heal")) {
                        long remaining = cooldownManager.getRemainingCooldown(player, "heal");
                        player.sendMessage(Component.text("Bitte warte noch " + remaining + " Sekunden!").color(NamedTextColor.RED));
                        return true;
                    }

                    player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
                    player.setFireTicks(0);
                    player.sendMessage(Component.text("Du wurdest geheilt!").color(NamedTextColor.GREEN));

                    // Set cooldown
                    cooldownManager.setCooldownFromConfig(player, "heal");
                }
                case "feed" -> {
                    // Check if module is enabled
                    if (!moduleManager.isFeatureEnabled("basic-commands", "feed")) {
                        player.sendMessage(MODULE_DISABLED);
                        return true;
                    }

                    if (!player.hasPermission("basiccommands.feed")) {
                        player.sendMessage(NO_PERMISSION);
                        return true;
                    }

                    // Check cooldown
                    if (cooldownManager.hasCooldown(player, "feed")) {
                        long remaining = cooldownManager.getRemainingCooldown(player, "feed");
                        player.sendMessage(Component.text("Bitte warte noch " + remaining + " Sekunden!").color(NamedTextColor.RED));
                        return true;
                    }

                    player.setFoodLevel(20);
                    player.setSaturation(20f);
                    player.sendMessage(Component.text("Dein Hunger wurde gestillt!").color(NamedTextColor.GREEN));

                    // Set cooldown
                    cooldownManager.setCooldownFromConfig(player, "feed");
                }
                case "clearinventory", "ci" -> {
                    // Check if module is enabled
                    if (!moduleManager.isFeatureEnabled("basic-commands", "clearinventory")) {
                        player.sendMessage(MODULE_DISABLED);
                        return true;
                    }

                    if (!player.hasPermission("basiccommands.clearinventory")) {
                        player.sendMessage(NO_PERMISSION);
                        return true;
                    }

                    player.getInventory().clear();
                    player.sendMessage(Component.text("Dein Inventar wurde geleert!").color(NamedTextColor.GREEN));
                }
                case "fly" -> {
                    // Check if module is enabled
                    if (!moduleManager.isFeatureEnabled("basic-commands", "fly")) {
                        player.sendMessage(MODULE_DISABLED);
                        return true;
                    }

                    if (!player.hasPermission("basiccommands.fly")) {
                        player.sendMessage(NO_PERMISSION);
                        return true;
                    }

                    boolean newState = !player.getAllowFlight();
                    player.setAllowFlight(newState);
                    player.setFlying(newState);
                    player.sendMessage(Component.text("Flugmodus " + (newState ? "aktiviert" : "deaktiviert") + "!")
                        .color(newState ? NamedTextColor.GREEN : NamedTextColor.RED));
                }
                case "gmc" -> {
                    // Check if module is enabled
                    if (!moduleManager.isFeatureEnabled("gamemode", "creative")) {
                        player.sendMessage(MODULE_DISABLED);
                        return true;
                    }

                    if (!player.hasPermission("basiccommands.gamemode.creative")) {
                        player.sendMessage(NO_PERMISSION);
                        return true;
                    }

                    player.setGameMode(org.bukkit.GameMode.CREATIVE);
                    player.sendMessage(Component.text("Spielmodus auf Kreativ gesetzt!").color(NamedTextColor.GREEN));
                }
                case "gms" -> {
                    // Check if module is enabled
                    if (!moduleManager.isFeatureEnabled("gamemode", "survival")) {
                        player.sendMessage(MODULE_DISABLED);
                        return true;
                    }

                    if (!player.hasPermission("basiccommands.gamemode.survival")) {
                        player.sendMessage(NO_PERMISSION);
                        return true;
                    }

                    player.setGameMode(org.bukkit.GameMode.SURVIVAL);
                    player.sendMessage(Component.text("Spielmodus auf Überleben gesetzt!").color(NamedTextColor.GREEN));
                }
                case "gmsp" -> {
                    // Check if module is enabled
                    if (!moduleManager.isFeatureEnabled("gamemode", "spectator")) {
                        player.sendMessage(MODULE_DISABLED);
                        return true;
                    }

                    if (!player.hasPermission("basiccommands.gamemode.spectator")) {
                        player.sendMessage(NO_PERMISSION);
                        return true;
                    }

                    player.setGameMode(org.bukkit.GameMode.SPECTATOR);
                    player.sendMessage(Component.text("Spielmodus auf Zuschauer gesetzt!").color(NamedTextColor.GREEN));
                }
                default -> {
                    player.sendMessage(Component.text("Unbekannter Befehl!").color(NamedTextColor.RED));
                    return false;
                }
            }
        } catch (Exception e) {
            player.sendMessage(Component.text("Ein Fehler ist aufgetreten!").color(NamedTextColor.RED));
            plugin.getLogger().severe("Error in BasicCommands: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
