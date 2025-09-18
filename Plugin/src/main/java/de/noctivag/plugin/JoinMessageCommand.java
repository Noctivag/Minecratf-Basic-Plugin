package de.noctivag.plugin;

import de.noctivag.plugin.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.logging.Level;

public class JoinMessageCommand implements CommandExecutor {
    private final JoinMessageManager messageManager;
    private final Plugin plugin;

    // Vordefinierte Fehlermeldungen als Konstanten
    private static final Component USAGE_SET = Component.text("Verwendung: /joinmessage set <Spieler> <Nachricht>")
        .color(NamedTextColor.RED);
    private static final Component USAGE_REMOVE = Component.text("Verwendung: /joinmessage remove <Spieler>")
        .color(NamedTextColor.RED);
    private static final Component USAGE_DEFAULT = Component.text("Verwendung: /joinmessage setdefault <Nachricht>")
        .color(NamedTextColor.RED);

    public JoinMessageCommand(Plugin plugin, JoinMessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("plugin.joinmessage")) {
            Component noPermMsg = plugin.getMessageManager() != null ?
                plugin.getMessageManager().getMessage("no-permission") :
                Component.text("Keine Berechtigung!").color(NamedTextColor.RED);
            sender.sendMessage(noPermMsg);
            return true;
        }

        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "set" -> handleSet(sender, args);
                case "remove" -> handleRemove(sender, args);
                case "toggle", "enable", "disable" -> handleToggle(sender, args);
                case "setdefault" -> handleSetDefault(sender, args);
                case "reload" -> {
                    // reload join message config
                    if (!sender.hasPermission("plugin.joinmessage.reload")) {
                        sender.sendMessage(Component.text("Dafür hast du keine Berechtigung!").color(NamedTextColor.RED));
                    } else {
                        messageManager.reload();
                        sender.sendMessage(Component.text("Join-Nachrichten neu geladen.").color(NamedTextColor.GREEN));
                    }
                }
                default -> sendHelp(sender);
            }
        } catch (Exception e) {
            sender.sendMessage(Component.text()
                .content("Ein Fehler ist aufgetreten: ")
                .color(NamedTextColor.RED)
                .append(Component.text(e.getMessage())));
            if (plugin.getConfigManager() != null && plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().log(Level.SEVERE, "Exception in JoinMessageCommand", e);
            }
        }
        return true;
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(USAGE_SET);
            return;
        }

        String playerName = args[1];
        boolean checkExists = plugin.getConfigManager() != null &&
            plugin.getConfigManager().getConfig().getBoolean("settings.check-player-exists", true);

        if (checkExists && Bukkit.getPlayer(playerName) == null &&
            !sender.hasPermission("plugin.joinmessage.bypass")) {
            sender.sendMessage(Component.text()
                .content("Spieler nicht gefunden: ")
                .color(NamedTextColor.RED)
                .append(Component.text(playerName)));
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        messageManager.setCustomMessage(playerName, message);

        sender.sendMessage(Component.text()
            .content("Join-Nachricht für ")
            .color(NamedTextColor.GREEN)
            .append(Component.text(playerName))
            .append(Component.text(" wurde gesetzt zu: "))
            .append(ColorUtils.parseColor(message)));
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(USAGE_REMOVE);
            return;
        }

        String playerName = args[1].toLowerCase();
        if (messageManager.hasCustomMessage(playerName)) {
            messageManager.removeCustomMessage(playerName);
            sender.sendMessage(Component.text()
                .content("Join-Nachricht für ")
                .color(NamedTextColor.GREEN)
                .append(Component.text(playerName))
                .append(Component.text(" wurde entfernt.")));
        } else {
            sender.sendMessage(Component.text()
                .content("Für ")
                .color(NamedTextColor.RED)
                .append(Component.text(playerName))
                .append(Component.text(" existiert keine benutzerdefinierte Join-Nachricht.")));
        }
    }

    private void handleToggle(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(Component.text()
                .content("Verwendung: /joinmessage ")
                .color(NamedTextColor.RED)
                .append(Component.text(args.length > 0 ? args[0] : "toggle"))
                .append(Component.text(" <Spieler>")));
            return;
        }

        String playerName = args[1].toLowerCase();
        String action = args[0].toLowerCase();
        boolean shouldEnable = action.equals("enable") ||
            (action.equals("toggle") && messageManager.isMessageDisabled(playerName));

        messageManager.setMessageEnabled(playerName, shouldEnable);

        sender.sendMessage(Component.text()
            .content("Join-Nachrichten für ")
            .color(NamedTextColor.GRAY)
            .append(Component.text(playerName))
            .append(Component.text(" wurden "))
            .append(Component.text(shouldEnable ? "aktiviert" : "deaktiviert")
                .color(shouldEnable ? NamedTextColor.GREEN : NamedTextColor.RED)));
    }

    private void handleSetDefault(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(USAGE_DEFAULT);
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        messageManager.setDefaultMessage(message);

        sender.sendMessage(Component.text()
            .content("Standard Join-Nachricht wurde gesetzt zu: ")
            .color(NamedTextColor.GREEN)
            .append(ColorUtils.parseColor(message)));
    }

    // Statische Komponenten für häufig verwendete Nachrichten
    private static final Component HELP_HEADER = Component.text("=== Join-Nachrichten Hilfe ===")
        .color(NamedTextColor.GOLD);
    private static final Component HELP_COLORS = Component.text("Farbcodes: &#RRGGBB für Hex-Farben, & für Standardfarben")
        .color(NamedTextColor.GRAY);
    private static final Component HELP_PLACEHOLDERS = Component.text("Platzhalter: %player% für den Spielernamen")
        .color(NamedTextColor.GRAY);

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(HELP_HEADER);

        String[] commands = {
            "/joinmessage set <Spieler> <Nachricht> - Setzt eine benutzerdefinierte Join-Nachricht",
            "/joinmessage remove <Spieler> - Entfernt eine benutzerdefinierte Join-Nachricht",
            "/joinmessage toggle <Spieler> - Schaltet Join-Nachrichten für einen Spieler um",
            "/joinmessage enable <Spieler> - Aktiviert Join-Nachrichten für einen Spieler",
            "/joinmessage disable <Spieler> - Deaktiviert Join-Nachrichten für einen Spieler",
            "/joinmessage setdefault <Nachricht> - Setzt die Standard Join-Nachricht"
        };

        for (String cmd : commands) {
            sender.sendMessage(Component.text(cmd).color(NamedTextColor.YELLOW));
        }

        sender.sendMessage(HELP_COLORS);
        sender.sendMessage(HELP_PLACEHOLDERS);
    }
}
