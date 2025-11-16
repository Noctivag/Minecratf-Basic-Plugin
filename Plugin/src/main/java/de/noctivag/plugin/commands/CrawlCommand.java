package de.noctivag.plugin.commands;

import de.noctivag.plugin.managers.CrawlManager;
import de.noctivag.plugin.messages.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CrawlCommand implements CommandExecutor {
    
    private final CrawlManager crawlManager;
    private final MessageManager messageManager;

    public CrawlCommand(CrawlManager crawlManager, MessageManager messageManager) {
        this.crawlManager = crawlManager;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messageManager.getMessage("general.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("plugin.crawl")) {
            player.sendMessage(messageManager.getMessage("general.no-permission"));
            return true;
        }

        if (crawlManager.isCrawling(player)) {
            crawlManager.stopCrawling(player);
            player.sendMessage(messageManager.getMessage("crawl.disabled"));
        } else {
            crawlManager.startCrawling(player);
            player.sendMessage(messageManager.getMessage("crawl.enabled"));
        }

        return true;
    }
}
