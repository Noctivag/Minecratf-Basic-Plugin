package de.noctivag.plugin.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.noctivag.plugin.permissions.Rank;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * MySQL/MariaDB implementation for BungeeCord network mode
 */
public class MySQLProvider implements DatabaseProvider {
    private final JavaPlugin plugin;
    private HikariDataSource dataSource;
    
    public MySQLProvider(JavaPlugin plugin, String host, int port, String database, String username, String password) {
        this.plugin = plugin;
        setupDataSource(host, port, database, username, password);
    }
    
    private void setupDataSource(String host, int port, String database, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true", host, port, database));
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        this.dataSource = new HikariDataSource(config);
    }
    
    @Override
    public void initialize() {
        try (Connection conn = dataSource.getConnection()) {
            createTables(conn);
            plugin.getLogger().info("MySQL database initialized successfully (BungeeCord network mode)");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize MySQL database", e);
        }
    }
    
    private void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Create ranks table
            stmt.execute("CREATE TABLE IF NOT EXISTS ranks (" +
                    "name VARCHAR(64) PRIMARY KEY, " +
                    "prefix VARCHAR(255), " +
                    "suffix VARCHAR(255), " +
                    "priority INT, " +
                    "permissions TEXT, " +
                    "inherited TEXT" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            
            // Create player_ranks table
            stmt.execute("CREATE TABLE IF NOT EXISTS player_ranks (" +
                    "player_uuid VARCHAR(36) PRIMARY KEY, " +
                    "ranks TEXT" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            
            // Create settings table
            stmt.execute("CREATE TABLE IF NOT EXISTS settings (" +
                    "setting_key VARCHAR(64) PRIMARY KEY, " +
                    "setting_value TEXT" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        }
    }
    
    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("MySQL database connection pool closed");
        }
    }
    
    @Override
    public Map<String, Rank> loadRanks() {
        Map<String, Rank> ranks = new ConcurrentHashMap<>();
        String sql = "SELECT * FROM ranks";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String name = rs.getString("name");
                String prefix = rs.getString("prefix");
                String suffix = rs.getString("suffix");
                int priority = rs.getInt("priority");
                String permissionsStr = rs.getString("permissions");
                String inheritedStr = rs.getString("inherited");
                
                Rank rank = new Rank(name, prefix, suffix, priority);
                
                // Parse permissions
                if (permissionsStr != null && !permissionsStr.isEmpty()) {
                    String[] perms = permissionsStr.split(",");
                    for (String perm : perms) {
                        if (!perm.trim().isEmpty()) {
                            rank.addPermission(perm.trim());
                        }
                    }
                }
                
                // Parse inherited ranks
                if (inheritedStr != null && !inheritedStr.isEmpty()) {
                    String[] inherited = inheritedStr.split(",");
                    for (String inheritRank : inherited) {
                        if (!inheritRank.trim().isEmpty()) {
                            rank.addInheritedRank(inheritRank.trim());
                        }
                    }
                }
                
                ranks.put(name, rank);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error loading ranks from MySQL", e);
        }
        return ranks;
    }
    
    @Override
    public void saveRank(Rank rank) {
        String sql = "INSERT INTO ranks (name, prefix, suffix, priority, permissions, inherited) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE prefix=VALUES(prefix), suffix=VALUES(suffix), " +
                "priority=VALUES(priority), permissions=VALUES(permissions), inherited=VALUES(inherited)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rank.getName());
            pstmt.setString(2, rank.getPrefix());
            pstmt.setString(3, rank.getSuffix());
            pstmt.setInt(4, rank.getPriority());
            pstmt.setString(5, String.join(",", rank.getPermissions()));
            pstmt.setString(6, String.join(",", rank.getInheritedRanks()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving rank to MySQL: " + rank.getName(), e);
        }
    }
    
    @Override
    public void deleteRank(String rankName) {
        String sql = "DELETE FROM ranks WHERE name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rankName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error deleting rank from MySQL: " + rankName, e);
        }
    }
    
    @Override
    public Map<UUID, Set<String>> loadPlayerRanks() {
        Map<UUID, Set<String>> playerRanks = new ConcurrentHashMap<>();
        String sql = "SELECT * FROM player_ranks";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String uuidStr = rs.getString("player_uuid");
                String ranksStr = rs.getString("ranks");
                
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    Set<String> ranks = new HashSet<>();
                    if (ranksStr != null && !ranksStr.isEmpty()) {
                        String[] rankArray = ranksStr.split(",");
                        for (String rank : rankArray) {
                            if (!rank.trim().isEmpty()) {
                                ranks.add(rank.trim());
                            }
                        }
                    }
                    playerRanks.put(uuid, ranks);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in database: " + uuidStr);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error loading player ranks from MySQL", e);
        }
        return playerRanks;
    }
    
    @Override
    public void savePlayerRanks(UUID playerId, Set<String> ranks) {
        String sql = "INSERT INTO player_ranks (player_uuid, ranks) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE ranks=VALUES(ranks)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerId.toString());
            pstmt.setString(2, String.join(",", ranks));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving player ranks to MySQL: " + playerId, e);
        }
    }
    
    @Override
    public void removePlayerRanks(UUID playerId) {
        String sql = "DELETE FROM player_ranks WHERE player_uuid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerId.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error removing player ranks from MySQL: " + playerId, e);
        }
    }
    
    @Override
    public String getDefaultRank() {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = 'default_rank'";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("setting_value");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting default rank from MySQL", e);
        }
        return "default";
    }
    
    @Override
    public void setDefaultRank(String rankName) {
        String sql = "INSERT INTO settings (setting_key, setting_value) VALUES ('default_rank', ?) " +
                "ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rankName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error setting default rank in MySQL", e);
        }
    }
}
