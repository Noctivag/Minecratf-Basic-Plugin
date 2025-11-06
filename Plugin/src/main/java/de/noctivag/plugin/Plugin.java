package de.noctivag.plugin;

import de.noctivag.plugin.config.ConfigManager;
import de.noctivag.plugin.data.PlayerDataManager;
import de.noctivag.plugin.listeners.PlayerListener;
import de.noctivag.plugin.tabcomplete.GlobalTabCompleter;
import de.noctivag.plugin.data.DataManager;
import de.noctivag.plugin.messages.MessageManager;
import de.noctivag.plugin.managers.SitManager;
import de.noctivag.plugin.managers.TabListManager;
import de.noctivag.plugin.managers.NametagManager;
import de.noctivag.plugin.managers.HomeManager;
import de.noctivag.plugin.managers.WarpManager;
import de.noctivag.plugin.permissions.RankManager;
import de.noctivag.plugin.utils.ScheduleManager;
import de.noctivag.plugin.commands.TriggerSitCommand;
import de.noctivag.plugin.commands.TriggerCamCommand;
import de.noctivag.plugin.commands.VanishCommand;
import de.noctivag.plugin.commands.InvseeCommand;
import de.noctivag.plugin.commands.ranks.RankCommand;
import de.noctivag.plugin.commands.ranks.SetRankCommand;
import de.noctivag.plugin.commands.teleport.HomeCommands;
import de.noctivag.plugin.commands.teleport.WarpCommands;
import de.noctivag.plugin.commands.teleport.SpawnCommand;
import de.noctivag.plugin.commands.teleport.TeleportCommands;
import de.noctivag.plugin.commands.admin.AdminCommands;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import de.noctivag.plugin.tabcomplete.JoinMessageTabCompleter;

public final class Plugin extends JavaPlugin {
    private final HashMap<String, String> prefixMap = new HashMap<>();
    private final HashMap<String, String> nickMap = new HashMap<>();
    private ConfigManager configManager;
    private JoinMessageManager joinMessageManager;
    private DataManager dataManager;
    private PlayerDataManager playerDataManager;
    private MessageManager messageManager;
    private ScheduleManager scheduleManager;
    private SitManager sitManager;
    private TabListManager tabListManager;
    private NametagManager nametagManager;
    private TriggerCamCommand triggerCamCommand;
    private VanishCommand vanishCommand;
    private InvseeCommand invseeCommand;
    private RankManager rankManager;
    private HomeManager homeManager;
    private WarpManager warpManager;
    private SpawnCommand spawnCommand;

    @Override
    public void onEnable() {
        try {
            this.configManager = new ConfigManager(this);
            this.dataManager = new DataManager(this);
            this.playerDataManager = new PlayerDataManager(this);
            this.messageManager = new MessageManager(this);
            this.joinMessageManager = new JoinMessageManager(this);
            this.scheduleManager = new ScheduleManager(this);
            this.sitManager = new SitManager(this);
            this.tabListManager = new TabListManager(this);
            this.nametagManager = new NametagManager(this);
            this.triggerCamCommand = new TriggerCamCommand();
            this.triggerCamCommand.setPlugin(this);
            this.vanishCommand = new VanishCommand();
            this.invseeCommand = new InvseeCommand();
            this.rankManager = new RankManager(this);
            this.homeManager = new HomeManager(this);
            this.warpManager = new WarpManager(this);
            this.spawnCommand = new SpawnCommand(this);

            PluginAPI.init(this);

            dataManager.loadData();
            joinMessageManager.reload();
            
            // Lade Spielerdaten in HashMaps für Kompatibilität
            prefixMap.putAll(playerDataManager.getAllPrefixes());
            nickMap.putAll(playerDataManager.getAllNicknames());

            registerCommands();
            registerTabCompleters();
            registerListeners();
            
            // Starte TabList Updater
            tabListManager.startTabListUpdater();

            getLogger().info("Plugin vollständig aktiviert!");
        } catch (Exception e) {
            getLogger().severe("Fehler beim Starten des Plugins: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }


    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new TabListListener(this, prefixMap, nickMap, joinMessageManager), this);
        getServer().getPluginManager().registerEvents(new PrefixListener(this, prefixMap, nickMap), this);
    }

    private void registerTabCompleters() {
        GlobalTabCompleter globalCompleter = new GlobalTabCompleter();

        // Registriere TabCompleter für alle relevanten Befehle
        String[] commands = {
            "prefix", "unprefix", "suffix", "unsuffix", "nick", "unnick",
            "heal", "feed", "fly", "vanish", "invsee",
            "gmc", "gms", "gmsp", "craftingtable", "anvil",
            "enderchest", "grindstone", "smithingtable",
            "stonecutter", "loom", "cartography", "sit", "cam",
            "rank", "setrank", "home", "sethome", "delhome",
            "warp", "setwarp", "delwarp", "spawn", "setspawn",
            "tp", "tpa", "tphere", "tpaccept", "tpdeny"
        };

        for (String cmd : commands) {
            PluginCommand command = getCommand(cmd);
            if (command != null) {
                command.setTabCompleter(globalCompleter);
            }
        }
    }

    private void registerCommands() {
        // Prefix/Nick Commands
        registerPrefixCommands();

        // Workbench Commands
        registerWorkbenchCommands();

        // Basic Commands
        registerBasicCommands();

        // Trigger Commands (sit & cam)
        registerTriggerCommands();

        // Rank Commands
        registerRankCommands();

        // Teleport Commands
        registerTeleportCommands();

        // Admin Commands (includes vanish & invsee)
        registerAdminCommands();
    }

    private void registerTriggerCommands() {
        PluginCommand sitCmd = getCommand("sit");
        if (sitCmd != null) {
            sitCmd.setExecutor(new TriggerSitCommand(sitManager));
        }

        PluginCommand camCmd = getCommand("cam");
        if (camCmd != null) {
            camCmd.setExecutor(triggerCamCommand);
        }
    }

    private void registerPrefixCommands() {
        // Prefix/Suffix/Nick Commands
        PluginCommand prefixCmd = getCommand("prefix");
        PluginCommand unprefixCmd = getCommand("unprefix");
        PluginCommand suffixCmd = getCommand("suffix");
        PluginCommand unsuffixCmd = getCommand("unsuffix");
        PluginCommand nickCmd = getCommand("nick");
        PluginCommand unnickCmd = getCommand("unnick");
        PluginCommand joinMessageCmd = getCommand("joinmessage");

        if (prefixCmd != null) {
            prefixCmd.setExecutor(new PrefixCommand(playerDataManager, nametagManager));
        }
        if (unprefixCmd != null) {
            unprefixCmd.setExecutor(new UnPrefixCommand(playerDataManager, nametagManager));
        }
        if (suffixCmd != null) {
            suffixCmd.setExecutor(new SuffixCommand(playerDataManager, nametagManager));
        }
        if (unsuffixCmd != null) {
            unsuffixCmd.setExecutor(new UnSuffixCommand(playerDataManager, nametagManager));
        }
        if (nickCmd != null) {
            nickCmd.setExecutor(new NickCommand(playerDataManager, nametagManager));
        }
        if (unnickCmd != null) {
            unnickCmd.setExecutor(new UnNickCommand(playerDataManager, nametagManager));
        }
        if (joinMessageCmd != null) {
            joinMessageCmd.setExecutor(new JoinMessageCommand(this, joinMessageManager));
            joinMessageCmd.setTabCompleter(new JoinMessageTabCompleter());
        }
    }

    private void registerWorkbenchCommands() {
        WorkbenchCommand workbenchExecutor = new WorkbenchCommand();
        String[] workbenchCommands = {
            "craftingtable", "anvil", "enderchest", "grindstone",
            "smithingtable", "stonecutter", "loom", "cartography"
        };

        for (String cmd : workbenchCommands) {
            PluginCommand command = getCommand(cmd);
            if (command != null) {
                command.setExecutor(workbenchExecutor);
            }
        }
    }

    private void registerBasicCommands() {
        BasicCommands basicExecutor = new BasicCommands();
        String[] basicCommands = {
            "heal", "feed", "clearinventory", "fly",
            "gmc", "gms", "gmsp"
        };

        for (String cmd : basicCommands) {
            PluginCommand command = getCommand(cmd);
            if (command != null) {
                command.setExecutor(basicExecutor);
            }
        }
    }

    private void registerRankCommands() {
        PluginCommand rankCmd = getCommand("rank");
        if (rankCmd != null) {
            rankCmd.setExecutor(new RankCommand(this));
        }

        PluginCommand setRankCmd = getCommand("setrank");
        if (setRankCmd != null) {
            setRankCmd.setExecutor(new SetRankCommand(this));
        }
    }

    private void registerTeleportCommands() {
        // Home commands
        HomeCommands homeCommands = new HomeCommands(homeManager);
        String[] homeCommandNames = {"sethome", "home", "delhome", "homes"};
        for (String cmd : homeCommandNames) {
            PluginCommand command = getCommand(cmd);
            if (command != null) {
                command.setExecutor(homeCommands);
            }
        }

        // Warp commands
        WarpCommands warpCommands = new WarpCommands(warpManager);
        String[] warpCommandNames = {"setwarp", "warp", "delwarp", "warps"};
        for (String cmd : warpCommandNames) {
            PluginCommand command = getCommand(cmd);
            if (command != null) {
                command.setExecutor(warpCommands);
            }
        }

        // Spawn commands
        String[] spawnCommandNames = {"spawn", "setspawn"};
        for (String cmd : spawnCommandNames) {
            PluginCommand command = getCommand(cmd);
            if (command != null) {
                command.setExecutor(spawnCommand);
            }
        }

        // Teleport commands
        TeleportCommands teleportCommands = new TeleportCommands(this);
        String[] tpCommandNames = {"tp", "tpa", "tphere", "tpaccept", "tpdeny"};
        for (String cmd : tpCommandNames) {
            PluginCommand command = getCommand(cmd);
            if (command != null) {
                command.setExecutor(teleportCommands);
            }
        }
    }

    private void registerAdminCommands() {
        // Vanish & Invsee from our changes
        PluginCommand vanishCmd = getCommand("vanish");
        if (vanishCmd != null) {
            vanishCmd.setExecutor(vanishCommand);
        }
        
        // Admin commands from feature branch
        AdminCommands adminCommands = new AdminCommands();
        String[] adminCommandNames = {"kick", "invsee", "day", "night", "sun", "rain"};
        for (String cmd : adminCommandNames) {
            PluginCommand command = getCommand(cmd);
            if (command != null) {
                // Use our InvseeCommand instead of the one from AdminCommands
                if (cmd.equals("invsee")) {
                    command.setExecutor(invseeCommand);
                } else {
                    command.setExecutor(adminCommands);
                }
            }
        }
    }

    @Override
    public void onDisable() {
        // Save all data only if initialized
        if (playerDataManager != null) {
            playerDataManager.savePlayerData();
        }
        if (dataManager != null) {
            dataManager.saveData();
        }
        if (joinMessageManager != null) {
            joinMessageManager.saveConfig();
        }
        if (configManager != null) {
            configManager.saveConfig();
        }
        if (sitManager != null) {
            sitManager.removeAllSeats();
        }
        if (triggerCamCommand != null) {
            triggerCamCommand.restoreAllPlayers();
        }
        if (tabListManager != null) {
            tabListManager.stopTabListUpdater();
        }
        if (homeManager != null) {
            homeManager.saveHomes();
        }
        if (warpManager != null) {
            warpManager.saveWarps();
        }
        if (rankManager != null) {
            // RankManager saves automatically to database
        }

        getLogger().info("Plugin deaktiviert - Alle Daten gespeichert (falls initialisiert).");
    }

    // Getter für die API und andere Klassen
    public ConfigManager getConfigManager() {
        return configManager;
    }

    public HashMap<String, String> getPrefixMap() {
        return prefixMap;
    }

    public HashMap<String, String> getNickMap() {
        return nickMap;
    }

    public JoinMessageManager getJoinMessageManager() {
        return joinMessageManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public ScheduleManager getScheduleManager() {
        return scheduleManager;
    }

    public SitManager getSitManager() {
        return sitManager;
    }

    public TabListManager getTabListManager() {
        return tabListManager;
    }

    public NametagManager getNametagManager() {
        return nametagManager;
    }

    public TriggerCamCommand getTriggerCamCommand() {
        return triggerCamCommand;
    }

    public RankManager getRankManager() {
        return rankManager;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public SpawnCommand getSpawnCommand() {
        return spawnCommand;
    }
}
