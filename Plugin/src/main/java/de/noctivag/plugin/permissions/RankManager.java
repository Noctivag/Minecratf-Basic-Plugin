package de.noctivag.plugin.permissions;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.database.DatabaseProvider;
import de.noctivag.plugin.database.MySQLProvider;
import de.noctivag.plugin.database.SQLiteProvider;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player ranks and permissions with automatic database detection
 */
public class RankManager {
    private final Plugin plugin;
    private final Map<String, Rank> ranks;
    private final Map<UUID, Set<String>> playerRanks;
    private final DatabaseProvider database;
    private String defaultRank;
    private final boolean isBungeeCord;

    public RankManager(Plugin plugin) {
        this.plugin = plugin;
        this.ranks = new ConcurrentHashMap<>();
        this.playerRanks = new ConcurrentHashMap<>();
        
        // Check if BungeeCord mode is enabled in config (default: false)
        boolean bungeeCordEnabled = plugin.getConfig().getBoolean("database.bungeecord-mode", false);
        this.isBungeeCord = bungeeCordEnabled && detectBungeeCord();
        
        // Initialize appropriate database provider
        if (isBungeeCord) {
            plugin.getLogger().info("BungeeCord mode enabled! Using MySQL for network-wide rank synchronization");
            this.database = createMySQLProvider();
        } else {
            plugin.getLogger().info("Using SQLite for local storage");
            this.database = new SQLiteProvider(plugin);
        }
        
        // Initialize database
        database.initialize();
        
        // Load data from database
        loadFromDatabase();
        
        // Create default ranks if none exist
        createDefaultRanks();
    }
    
    /**
     * Detects if the plugin is running on a BungeeCord network
     */
    private boolean detectBungeeCord() {
        try {
            Class.forName("net.md_5.bungee.api.ProxyServer");
            return Bukkit.getServer().spigot().getConfig().getBoolean("settings.bungeecord", false);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Creates MySQL provider from config settings
     */
    private DatabaseProvider createMySQLProvider() {
        String host = plugin.getConfig().getString("database.mysql.host", "localhost");
        int port = plugin.getConfig().getInt("database.mysql.port", 3306);
        String dbName = plugin.getConfig().getString("database.mysql.database", "minecraft_ranks");
        String username = plugin.getConfig().getString("database.mysql.username", "root");
        String password = plugin.getConfig().getString("database.mysql.password", "password");
        
        return new MySQLProvider(plugin, host, port, dbName, username, password);
    }
    
    /**
     * Load ranks and player data from database
     */
    private void loadFromDatabase() {
        // Load ranks
        Map<String, Rank> loadedRanks = database.loadRanks();
        ranks.putAll(loadedRanks);
        
        // Load player ranks
        Map<UUID, Set<String>> loadedPlayerRanks = database.loadPlayerRanks();
        playerRanks.putAll(loadedPlayerRanks);
        
        // Load default rank setting
        defaultRank = database.getDefaultRank();
        
        plugin.getLogger().info("Loaded " + ranks.size() + " ranks and " + playerRanks.size() + " player assignments from database");
    }

    private void createDefaultRanks() {
        if (ranks.isEmpty()) {
            // Create default rank
            Rank defaultRank = new Rank("default", "&7", "", 0);
            defaultRank.addPermission("plugin.sit");
            defaultRank.addPermission("plugin.cosmetics");
            defaultRank.addPermission("workbench.*");
            defaultRank.addPermission("essentials.home");
            defaultRank.addPermission("essentials.sethome");
            defaultRank.addPermission("essentials.delhome");
            defaultRank.addPermission("essentials.homes");
            defaultRank.addPermission("essentials.spawn");
            defaultRank.addPermission("essentials.warp");
            defaultRank.addPermission("essentials.warps");
            defaultRank.addPermission("essentials.tpa");
            defaultRank.addPermission("essentials.tpaccept");
            defaultRank.addPermission("essentials.tpdeny");
            ranks.put("default", defaultRank);
            database.saveRank(defaultRank);

            // Create VIP rank
            Rank vipRank = new Rank("vip", "&6[VIP] &e", "", 10);
            vipRank.addPermission("basiccommands.fly");
            vipRank.addPermission("basiccommands.heal");
            vipRank.addPermission("basiccommands.feed");
            vipRank.addInheritedRank("default");
            ranks.put("vip", vipRank);
            database.saveRank(vipRank);

            // Create Mod rank
            Rank modRank = new Rank("mod", "&9[Mod] &b", "", 50);
            modRank.addPermission("essentials.tp");
            modRank.addPermission("essentials.tphere");
            modRank.addPermission("admin.kick");
            modRank.addPermission("admin.time");
            modRank.addPermission("admin.weather");
            modRank.addInheritedRank("vip");
            ranks.put("mod", modRank);
            database.saveRank(modRank);

            // Create Admin rank
            Rank adminRank = new Rank("admin", "&c[Admin] &4", "", 100);
            adminRank.addPermission("*");
            adminRank.addInheritedRank("mod");
            ranks.put("admin", adminRank);
            database.saveRank(adminRank);
            
            plugin.getLogger().info("Created default ranks in database");
        }
    }

    public Rank getRank(String name) {
        return ranks.get(name.toLowerCase());
    }

    public void createRank(String name, String prefix, String suffix, int priority) {
        Rank rank = new Rank(name.toLowerCase(), prefix, suffix, priority);
        ranks.put(name.toLowerCase(), rank);
        database.saveRank(rank);
    }

    public void deleteRank(String name) {
        ranks.remove(name.toLowerCase());
        database.deleteRank(name.toLowerCase());
    }

    public Set<String> getAllRankNames() {
        return new HashSet<>(ranks.keySet());
    }

    public Set<Rank> getPlayerRanks(UUID playerId) {
        Set<String> rankNames = playerRanks.getOrDefault(playerId, new HashSet<>());
        if (rankNames.isEmpty()) {
            rankNames.add(defaultRank);
        }
        
        Set<Rank> result = new HashSet<>();
        for (String rankName : rankNames) {
            Rank rank = ranks.get(rankName);
            if (rank != null) {
                result.add(rank);
            }
        }
        return result;
    }

    public Rank getHighestRank(UUID playerId) {
        Set<Rank> playerRanks = getPlayerRanks(playerId);
        return playerRanks.stream()
                .max(Comparator.comparingInt(Rank::getPriority))
                .orElse(ranks.get(defaultRank));
    }

    public void setPlayerRank(UUID playerId, String rankName) {
        Set<String> ranks = new HashSet<>();
        ranks.add(rankName.toLowerCase());
        playerRanks.put(playerId, ranks);
        database.savePlayerRanks(playerId, ranks);
    }

    public void addPlayerRank(UUID playerId, String rankName) {
        Set<String> ranks = playerRanks.computeIfAbsent(playerId, k -> new HashSet<>());
        ranks.add(rankName.toLowerCase());
        database.savePlayerRanks(playerId, ranks);
    }

    public void removePlayerRank(UUID playerId, String rankName) {
        Set<String> ranks = playerRanks.get(playerId);
        if (ranks != null) {
            ranks.remove(rankName.toLowerCase());
            if (ranks.isEmpty()) {
                playerRanks.remove(playerId);
                database.removePlayerRanks(playerId);
            } else {
                database.savePlayerRanks(playerId, ranks);
            }
        }
    }

    public boolean hasPermission(UUID playerId, String permission) {
        Set<Rank> playerRanks = getPlayerRanks(playerId);
        
        for (Rank rank : playerRanks) {
            if (checkPermissionWithInheritance(rank, permission, new HashSet<>())) {
                return true;
            }
        }
        
        return false;
    }

    private boolean checkPermissionWithInheritance(Rank rank, String permission, Set<String> checked) {
        if (checked.contains(rank.getName())) {
            return false;
        }
        checked.add(rank.getName());

        if (rank.hasPermission(permission) || rank.hasPermission("*")) {
            return true;
        }

        for (String inheritedRankName : rank.getInheritedRanks()) {
            Rank inheritedRank = ranks.get(inheritedRankName);
            if (inheritedRank != null && checkPermissionWithInheritance(inheritedRank, permission, checked)) {
                return true;
            }
        }

        return false;
    }

    public String getDefaultRank() {
        return defaultRank;
    }

    public void setDefaultRank(String defaultRank) {
        this.defaultRank = defaultRank;
        database.setDefaultRank(defaultRank);
    }
    
    /**
     * Save a rank to the database
     */
    public void saveRank(Rank rank) {
        database.saveRank(rank);
    }
    
    /**
     * Shutdown the rank manager and close database connections
     */
    public void shutdown() {
        database.close();
        plugin.getLogger().info("RankManager shutdown complete");
    }
    
    /**
     * Check if running in BungeeCord mode
     */
    public boolean isBungeeCordMode() {
        return isBungeeCord;
    }
}
