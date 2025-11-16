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
import de.noctivag.plugin.permissions.PermissionManager;
import de.noctivag.plugin.managers.TeleportWarmupManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import de.noctivag.plugin.tabcomplete.JoinMessageTabCompleter;
import de.noctivag.plugin.crafting.RecipeManager;
import de.noctivag.plugin.listeners.ItemFrameListener;
import de.noctivag.plugin.listeners.InvisibleItemFrameListener;
import de.noctivag.plugin.listeners.VanishListener;
import de.noctivag.plugin.listeners.entity.GhastSpeedListener;

public final class Plugin extends JavaPlugin {
    private ConfigManager configManager;
    private JoinMessageManager joinMessageManager;
    private DataManager dataManager;
    private PlayerDataManager playerDataManager;
    private MessageManager messageManager;
    private PermissionManager permissionManager;
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
    private RecipeManager recipeManager;
    
    // New cosmetic managers
    private de.noctivag.plugin.managers.CrawlManager crawlManager;
    private de.noctivag.plugin.managers.AfkManager afkManager;
    private de.noctivag.plugin.managers.RideManager rideManager;

    // New systems
    private ModuleManager moduleManager;
    private LuckPermsHook luckPermsHook;
    private PlaceholderAPIHook placeholderAPIHook;
    private CooldownManager cooldownManager;
    private int autoSaveTask = -1;
    
    // New feature managers
    private de.noctivag.plugin.gui.GuiManager guiManager;
    private de.noctivag.plugin.gui.ChatInputManager chatInputManager;
    private de.noctivag.plugin.economy.EconomyManager economyManager;
    private de.noctivag.plugin.teleport.BackManager backManager;
    private TeleportWarmupManager teleportWarmupManager;
    private de.noctivag.plugin.messaging.MessagingManager messagingManager;
    private de.noctivag.plugin.kits.KitManager kitManager;
    private de.noctivag.plugin.moderation.ModerationManager moderationManager;

    @Override
    public void onEnable() {
        try {
            // Core systems
            this.configManager = new ConfigManager(this);
            this.permissionManager = new PermissionManager(this);

            // Module manager - initialize early to check module states
            this.moduleManager = new ModuleManager(this);
            getLogger().info("Module system initialized");

            // External integrations (respect config toggles)
            if (getConfig().getBoolean("integrations.luckperms.enabled", false)) {
                this.luckPermsHook = new LuckPermsHook(this);
                if (luckPermsHook.hook()) {
                    getLogger().info("LuckPerms integration enabled!");
                } else {
                    getLogger().info("LuckPerms not found or failed to hook.");
                }
            } else {
                getLogger().info("LuckPerms integration disabled in config.");
            }

            if (getConfig().getBoolean("integrations.placeholderapi.enabled", false)
                    && getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                this.placeholderAPIHook = new PlaceholderAPIHook(this);
                if (placeholderAPIHook.hook()) {
                    getLogger().info("PlaceholderAPI integration enabled!");
                }
            } else {
                getLogger().info("PlaceholderAPI integration disabled (missing plugin or disabled in config).");
            }

            // Recipe manager
            this.recipeManager = new RecipeManager(this);
            recipeManager.registerRecipes();

            // Data managers
            this.dataManager = new DataManager(this);
            this.playerDataManager = new PlayerDataManager(this);
            this.messageManager = new MessageManager(this);
            this.joinMessageManager = new JoinMessageManager(this);

            // Feature managers
            this.cooldownManager = new CooldownManager(this);
            
            // GUI Manager (always enabled for admin config)
            this.guiManager = new de.noctivag.plugin.gui.GuiManager(this);
            this.chatInputManager = new de.noctivag.plugin.gui.ChatInputManager(this, guiManager);
            
            // New feature managers (module-based)
            if (moduleManager.isModuleEnabled("modules.economy")) {
                this.economyManager = new de.noctivag.plugin.economy.EconomyManager(this);
                getLogger().info("Economy system enabled");
            }
            
            if (moduleManager.isModuleEnabled("modules.teleportation")) {
                this.backManager = new de.noctivag.plugin.teleport.BackManager(this);
                this.teleportWarmupManager = new TeleportWarmupManager(this);
                getLogger().info("Advanced teleportation enabled");
            }
            
            if (moduleManager.isModuleEnabled("modules.messaging")) {
                this.messagingManager = new de.noctivag.plugin.messaging.MessagingManager(this);
                getLogger().info("Messaging system enabled");
            }
            
            if (moduleManager.isModuleEnabled("modules.kits")) {
                this.kitManager = new de.noctivag.plugin.kits.KitManager(this);
                getLogger().info("Kit system enabled");
            }
            
            if (moduleManager.isModuleEnabled("modules.moderation")) {
                this.moderationManager = new de.noctivag.plugin.moderation.ModerationManager(this);
                getLogger().info("Moderation system enabled");
            }
            
            if (moduleManager.isModuleEnabled("modules.cosmetics.sit")) {
                this.sitManager = new SitManager(this);
            }
            if (moduleManager.isModuleEnabled("modules.cosmetics.crawl")) {
                this.crawlManager = new de.noctivag.plugin.managers.CrawlManager(this);
            }
            if (moduleManager.isModuleEnabled("modules.cosmetics.afk")) {
                this.afkManager = new de.noctivag.plugin.managers.AfkManager(this);
            }
            if (moduleManager.isModuleEnabled("modules.cosmetics.ride")) {
                this.rideManager = new de.noctivag.plugin.managers.RideManager(this);
            }
            if (moduleManager.isModuleEnabled("modules.tablist")) {
                this.tabListManager = new TabListManager(this);
            }
            if (moduleManager.isModuleEnabled("modules.nametags")) {
                this.nametagManager = new NametagManager(this);
            }
            if (moduleManager.isModuleEnabled("modules.cosmetics.camera")) {
                this.triggerCamCommand = new TriggerCamCommand(this, messageManager);
            }
            if (moduleManager.isModuleEnabled("modules.cosmetics.vanish")) {
                this.vanishCommand = new VanishCommand(this, messageManager);
            }
            if (moduleManager.isModuleEnabled("modules.admin-commands.invsee")) {
                this.invseeCommand = new InvseeCommand(messageManager);
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
            de.noctivag.plugin.api.BasicPluginAPI.init(this);

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
            
            // Log compatibility information
            if (getConfig().getBoolean("settings.debug-mode", false)) {
                getLogger().info(de.noctivag.plugin.utils.PluginCompatibility.getCompatibilityReport());
            }
            
            // Warn about chat plugin conflicts
            if (moduleManager.isModuleEnabled("chat") && 
                de.noctivag.plugin.utils.PluginCompatibility.hasChatPluginConflict()) {
                getLogger().warning("⚠ Chat formatting plugin detected! If you're using DeluxeChat or ChatControl,");
                getLogger().warning("  set 'chat.enabled: false' in config.yml to avoid conflicts.");
            }
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
        getServer().getPluginManager().registerEvents(new ItemFrameListener(this), this);
        getServer().getPluginManager().registerEvents(new InvisibleItemFrameListener(this), this);
        getServer().getPluginManager().registerEvents(new VanishListener(this, vanishCommand), this);
        getServer().getPluginManager().registerEvents(new GhastSpeedListener(this), this);

        // GUI listener (always registered for config menu)
        if (guiManager != null) {
            getServer().getPluginManager().registerEvents(new de.noctivag.plugin.gui.GuiListener(this, guiManager, chatInputManager), this);
            getServer().getPluginManager().registerEvents(new de.noctivag.plugin.gui.ChatInputListener(this, chatInputManager), this);
        }

        if (teleportWarmupManager != null) {
            getServer().getPluginManager().registerEvents(new de.noctivag.plugin.listeners.TeleportWarmupListener(teleportWarmupManager), this);
        }

        if (triggerCamCommand != null) {
            getServer().getPluginManager().registerEvents(new de.noctivag.plugin.listeners.CameraListener(this, triggerCamCommand), this);
        }

        if (vanishCommand != null) {
            getServer().getPluginManager().registerEvents(new VanishListener(this, vanishCommand), this);
        }

        // Register chat listener if chat formatting is enabled
        if (moduleManager.isModuleEnabled("chat")) {
            getServer().getPluginManager().registerEvents(new de.noctivag.plugin.listeners.ChatListener(this), this);
        }
        
        // Register cosmetic listener for new features
        if (crawlManager != null || afkManager != null || rideManager != null) {
            getServer().getPluginManager().registerEvents(new de.noctivag.plugin.listeners.CosmeticListener(
                this, crawlManager, afkManager, rideManager), this);
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
        
        // New Cosmetic Commands
        registerCosmeticCommands();

        // Rank Commands
        registerRankCommands();

        // Teleport Commands
        registerTeleportCommands();
        registerAdvancedTeleportCommands(); // /back, /tprandom

        // Admin Commands (includes vanish & invsee)
        registerAdminCommands();

        // Utility commands (reload)
        registerUtilityCommands();

        // Root admin command (/plugin)
        registerPluginAdminCommand();
        
        // NEW FEATURE COMMANDS
        // GUI Config Command
        registerGuiCommands();
        
        // Economy Commands
        registerEconomyCommands();
        
        // Messaging Commands
        registerMessagingCommands();
        
        // Kit Commands
        registerKitCommands();
        
        // Moderation Commands
        registerModerationCommands();
    }

    private void registerTriggerCommands() {
        PluginCommand sitCmd = getCommand("sit");
        if (sitCmd != null) {
            sitCmd.setExecutor(new TriggerSitCommand(sitManager, messageManager));
        }

        PluginCommand camCmd = getCommand("cam");
        if (camCmd != null) {
            camCmd.setExecutor(triggerCamCommand);
        }
    }
    
    private void registerCosmeticCommands() {
        // Hat command
        PluginCommand hatCmd = getCommand("hat");
        if (hatCmd != null) {
            hatCmd.setExecutor(new de.noctivag.plugin.commands.HatCommand(messageManager));
        }
        
        // Crawl command
        PluginCommand crawlCmd = getCommand("crawl");
        if (crawlCmd != null && crawlManager != null) {
            crawlCmd.setExecutor(new de.noctivag.plugin.commands.CrawlCommand(crawlManager, messageManager));
        }
        
        // Lay command removed (future expansion placeholder)
        
        // AFK command
        PluginCommand afkCmd = getCommand("afk");
        if (afkCmd != null && afkManager != null) {
            afkCmd.setExecutor(new de.noctivag.plugin.commands.AfkCommand(afkManager, messageManager));
        }
        
        // Skull command
        PluginCommand skullCmd = getCommand("skull");
        if (skullCmd != null) {
            skullCmd.setExecutor(new de.noctivag.plugin.commands.SkullCommand(messageManager));
        }
        
        // Ride command
        PluginCommand rideCmd = getCommand("ride");
        if (rideCmd != null && rideManager != null) {
            rideCmd.setExecutor(new de.noctivag.plugin.commands.RideCommand(rideManager, messageManager));
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
            prefixCmd.setExecutor(new PrefixCommand(playerDataManager, nametagManager, messageManager));
        }
        if (unprefixCmd != null) {
            unprefixCmd.setExecutor(new UnPrefixCommand(playerDataManager, nametagManager, messageManager));
        }
        if (suffixCmd != null) {
            suffixCmd.setExecutor(new SuffixCommand(playerDataManager, nametagManager, messageManager));
        }
        if (unsuffixCmd != null) {
            unsuffixCmd.setExecutor(new UnSuffixCommand(playerDataManager, nametagManager, messageManager));
        }
        if (nickCmd != null) {
            nickCmd.setExecutor(new NickCommand(playerDataManager, nametagManager, messageManager));
        }
        if (unnickCmd != null) {
            unnickCmd.setExecutor(new UnNickCommand(playerDataManager, nametagManager, messageManager));
        }
        if (joinMessageCmd != null) {
            joinMessageCmd.setExecutor(new JoinMessageCommand(this, joinMessageManager, messageManager));
            joinMessageCmd.setTabCompleter(new JoinMessageTabCompleter());
        }
    }

    private void registerWorkbenchCommands() {
        WorkbenchCommand workbenchExecutor = new WorkbenchCommand(messageManager, permissionManager);
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
        BasicCommands basicExecutor = new BasicCommands(this, messageManager, permissionManager);
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
        TeleportCommands teleportCommands = new TeleportCommands(this, teleportWarmupManager);
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
        AdminCommands adminCommands = new AdminCommands(messageManager);
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

    private void registerUtilityCommands() {
        PluginCommand reloadCmd = getCommand("pluginreload");
        if (reloadCmd != null) {
            reloadCmd.setExecutor(new de.noctivag.plugin.commands.ReloadCommand(this));
        }
    }

    private void registerPluginAdminCommand() {
        PluginCommand root = getCommand("plugin");
        if (root != null) {
            root.setExecutor(new de.noctivag.plugin.commands.PluginAdminCommand(this));
            root.setTabCompleter(new de.noctivag.plugin.tabcomplete.PluginAdminTabCompleter(this));
        }
    }
    
    // NEW FEATURE COMMAND REGISTRATION METHODS
    
    private void registerGuiCommands() {
        if (guiManager == null) return;
        
        PluginCommand configCmd = getCommand("config");
        if (configCmd != null) {
            configCmd.setExecutor(new de.noctivag.plugin.commands.ConfigCommand(this, guiManager));
        }
    }
    
    private void registerEconomyCommands() {
        if (economyManager == null) return;
        
        de.noctivag.plugin.commands.economy.EconomyCommands ecoCommands = 
            new de.noctivag.plugin.commands.economy.EconomyCommands(this, economyManager);
        
        String[] ecoCommandNames = {"balance", "bal", "pay", "eco"};
        for (String cmd : ecoCommandNames) {
            PluginCommand command = getCommand(cmd);
            if (command != null) {
                command.setExecutor(ecoCommands);
            }
        }
    }
    
    private void registerMessagingCommands() {
        if (messagingManager == null) return;
        
        de.noctivag.plugin.commands.messaging.MessagingCommands msgCommands =
            new de.noctivag.plugin.commands.messaging.MessagingCommands(this, messagingManager);
        
        String[] msgCommandNames = {"msg", "tell", "whisper", "reply", "r", "ignore"};
        for (String cmd : msgCommandNames) {
            PluginCommand command = getCommand(cmd);
            if (command != null) {
                command.setExecutor(msgCommands);
            }
        }
    }
    
    private void registerKitCommands() {
        if (kitManager == null) return;
        
        de.noctivag.plugin.commands.kits.KitCommand kitCommand =
            new de.noctivag.plugin.commands.kits.KitCommand(this, kitManager);
        
        PluginCommand kitCmd = getCommand("kit");
        if (kitCmd != null) {
            kitCmd.setExecutor(kitCommand);
        }
        
        PluginCommand kitsCmd = getCommand("kits");
        if (kitsCmd != null) {
            kitsCmd.setExecutor(kitCommand);
        }
    }
    
    private void registerModerationCommands() {
        if (moderationManager == null) return;
        
        de.noctivag.plugin.commands.moderation.ModerationCommands modCommands =
            new de.noctivag.plugin.commands.moderation.ModerationCommands(this, moderationManager);
        
        String[] modCommandNames = {"ban", "tempban", "unban", "mute", "unmute", "warn", "warnings"};
        for (String cmd : modCommandNames) {
            PluginCommand command = getCommand(cmd);
            if (command != null) {
                command.setExecutor(modCommands);
            }
        }
    }
    
    // Also register /back and /tprandom for advanced teleportation
    private void registerAdvancedTeleportCommands() {
        if (backManager != null) {
            PluginCommand backCmd = getCommand("back");
            if (backCmd != null) {
                backCmd.setExecutor(new de.noctivag.plugin.commands.teleport.BackCommand(this, backManager));
            }
        }
        
        PluginCommand tprandomCmd = getCommand("tprandom");
        if (tprandomCmd != null && getConfig().getBoolean("modules.teleportation.random.enabled", true)) {
            tprandomCmd.setExecutor(new de.noctivag.plugin.commands.teleport.RandomTeleportCommand(this));
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
        if (crawlManager != null) {
            crawlManager.stopAll();
        }
        if (afkManager != null) {
            afkManager.cleanup();
        }
        if (rideManager != null) {
            rideManager.cleanup();
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
        if (economyManager != null) {
            economyManager.save();
        }
        if (kitManager != null) {
            kitManager.saveKits();
        }
        if (moderationManager != null) {
            moderationManager.save();
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

    public PermissionManager getPermissionManager() {
        return permissionManager;
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

    public VanishCommand getVanishCommand() {
        return vanishCommand;
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
                if (economyManager != null) {
                    economyManager.save();
                }
                if (kitManager != null) {
                    kitManager.saveKits();
                }
                if (moderationManager != null) {
                    moderationManager.save();
                }
                if (configManager != null) {
                    // Ensure config changes (e.g., via GUI) persist periodically
                    configManager.saveConfig();
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
