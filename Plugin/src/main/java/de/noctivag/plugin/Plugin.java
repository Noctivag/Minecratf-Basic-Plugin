package de.noctivag.plugin;

import de.noctivag.plugin.config.ConfigManager;
import de.noctivag.plugin.data.PlayerDataManager;
import de.noctivag.plugin.listeners.PlayerListener;
import de.noctivag.plugin.tabcomplete.GlobalTabCompleter;
import de.noctivag.plugin.data.DataManager;
import de.noctivag.plugin.messages.MessageManager;
import de.noctivag.plugin.managers.*;
import de.noctivag.plugin.modules.ModuleManager;
import de.noctivag.plugin.integrations.LuckPermsHook;
import de.noctivag.plugin.integrations.PlaceholderAPIHook;
import de.noctivag.plugin.permissions.RankManager;
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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import de.noctivag.plugin.tabcomplete.JoinMessageTabCompleter;

public final class Plugin extends JavaPlugin {
    private ConfigManager configManager;
    private JoinMessageManager joinMessageManager;
    private DataManager dataManager;
    private PlayerDataManager playerDataManager;
    private MessageManager messageManager;
    private SitManager sitManager;
    private SleepManager sleepManager;
    private TabListManager tabListManager;
    private NametagManager nametagManager;
    private TriggerCamCommand triggerCamCommand;
    private VanishCommand vanishCommand;
    private InvseeCommand invseeCommand;
    private RankManager rankManager;
    private HomeManager homeManager;
    private WarpManager warpManager;
    private SpawnCommand spawnCommand;

    // New systems
    private ModuleManager moduleManager;
    private LuckPermsHook luckPermsHook;
    private PlaceholderAPIHook placeholderAPIHook;
    private CooldownManager cooldownManager;
    private int autoSaveTask = -1;

    @Override
    public void onEnable() {
        try {
            // Core systems
            this.configManager = new ConfigManager(this);

            // Module manager - initialize early to check module states
            this.moduleManager = new ModuleManager(this);
            getLogger().info("Module system initialized");

            // External integrations
            this.luckPermsHook = new LuckPermsHook(this);
            if (luckPermsHook.hook()) {
                getLogger().info("LuckPerms integration enabled!");
            }

            this.placeholderAPIHook = new PlaceholderAPIHook(this);
            if (placeholderAPIHook.hook()) {
                getLogger().info("PlaceholderAPI integration enabled!");
            }

            // Data managers
            this.dataManager = new DataManager(this);
            this.playerDataManager = new PlayerDataManager(this);
            this.messageManager = new MessageManager(this);
            this.joinMessageManager = new JoinMessageManager(this);

            // Feature managers
            this.cooldownManager = new CooldownManager(this);
            if (moduleManager.isModuleEnabled("modules.cosmetics.sit")) {
                this.sitManager = new SitManager(this);
            }
            if (moduleManager.isModuleEnabled("modules.sleep")) {
                this.sleepManager = new SleepManager(this);
            }
            if (moduleManager.isModuleEnabled("modules.tablist")) {
                this.tabListManager = new TabListManager(this);
            }
            if (moduleManager.isModuleEnabled("modules.nametags")) {
                this.nametagManager = new NametagManager(this);
            }
            if (moduleManager.isModuleEnabled("modules.cosmetics.camera")) {
                this.triggerCamCommand = new TriggerCamCommand();
                this.triggerCamCommand.setPlugin(this);
            }
            if (moduleManager.isModuleEnabled("modules.cosmetics.vanish")) {
                this.vanishCommand = new VanishCommand();
            }
            if (moduleManager.isModuleEnabled("modules.admin-commands.invsee")) {
                this.invseeCommand = new InvseeCommand();
            }
            if (moduleManager.isModuleEnabled("modules.ranks")) {
                this.rankManager = new RankManager(this);
            }
            if (moduleManager.isModuleEnabled("modules.homes")) {
                this.homeManager = new HomeManager(this);
            }
            if (moduleManager.isModuleEnabled("modules.warps")) {
                this.warpManager = new WarpManager(this);
            }
            if (moduleManager.isModuleEnabled("modules.spawn")) {
                this.spawnCommand = new SpawnCommand(this);
            }

            PluginAPI.init(this);

            dataManager.loadData();
            joinMessageManager.reload();

            registerCommands();
            registerTabCompleters();
            registerListeners();

            if (nametagManager != null) {
                refreshNametagsForOnlinePlayers();
            }

            // Starte TabList Updater
            if (tabListManager != null) {
                tabListManager.startTabListUpdater();
            }

            // Start auto-save task
            startAutoSaveTask();

            getLogger().info("Plugin fully activated!");
            getLogger().info("Modules enabled: " + countEnabledModules() + " modules active");
        } catch (Exception e) {
            getLogger().severe("Error starting plugin: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }


    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new TabListListener(this, playerDataManager, joinMessageManager), this);
        getServer().getPluginManager().registerEvents(new PrefixListener(this, playerDataManager), this);

        if (triggerCamCommand != null) {
            getServer().getPluginManager().registerEvents(new de.noctivag.plugin.listeners.CameraListener(this, triggerCamCommand), this);
        }

        if (sleepManager != null) {
            getServer().getPluginManager().registerEvents(sleepManager, this);
        }

        // Register chat listener if chat formatting is enabled
        if (moduleManager.isModuleEnabled("chat")) {
            getServer().getPluginManager().registerEvents(new de.noctivag.plugin.listeners.ChatListener(this), this);
        }
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
        BasicCommands basicExecutor = new BasicCommands(this);
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
        // Stop auto-save task
        stopAutoSaveTask();

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
        if (cooldownManager != null) {
            cooldownManager.saveCooldowns();
        }
        if (rankManager != null) {
            // RankManager saves automatically to database
        }
        if (placeholderAPIHook != null && placeholderAPIHook.isEnabled()) {
            placeholderAPIHook.unregister();
        }

        getLogger().info("Plugin disabled - All data saved.");
    }

    // Getter für die API und andere Klassen
    public ConfigManager getConfigManager() {
        return configManager;
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

    private void refreshNametagsForOnlinePlayers() {
        if (nametagManager == null) {
            return;
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            nametagManager.loadNametag(onlinePlayer);
        }
    }

    /**
     * Start auto-save task for player data
     */
    private void startAutoSaveTask() {
        int interval = getConfig().getInt("settings.auto-save-interval", 300);

        if (interval <= 0) {
            getLogger().info("Auto-save is disabled (interval set to 0)");
            return;
        }

        // Convert seconds to ticks (20 ticks = 1 second)
        long intervalTicks = interval * 20L;

        autoSaveTask = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                if (playerDataManager != null) {
                    playerDataManager.savePlayerData();
                }
                if (homeManager != null) {
                    homeManager.saveHomes();
                }
                if (warpManager != null) {
                    warpManager.saveWarps();
                }
                if (cooldownManager != null && cooldownManager.isEnabled()) {
                    cooldownManager.saveCooldowns();
                }

                if (getConfig().getBoolean("settings.debug-mode", false)) {
                    getLogger().info("Auto-save completed");
                }
            } catch (Exception e) {
                getLogger().severe("Error during auto-save: " + e.getMessage());
            }
        }, intervalTicks, intervalTicks).getTaskId();

        getLogger().info("Auto-save enabled (interval: " + interval + " seconds)");
    }

    /**
     * Stop auto-save task
     */
    private void stopAutoSaveTask() {
        if (autoSaveTask != -1) {
            getServer().getScheduler().cancelTask(autoSaveTask);
            autoSaveTask = -1;
        }
    }

    /**
     * Count enabled modules for logging
     */
    private int countEnabledModules() {
        if (moduleManager == null) {
            return 0;
        }
        return (int) moduleManager.getModuleStates().values().stream()
                .filter(enabled -> enabled)
                .count();
    }

    // Getters for new systems
    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public LuckPermsHook getLuckPermsHook() {
        return luckPermsHook;
    }

    public PlaceholderAPIHook getPlaceholderAPIHook() {
        return placeholderAPIHook;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
}
