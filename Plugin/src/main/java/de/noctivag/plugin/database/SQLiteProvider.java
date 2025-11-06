package de.noctivag.plugin.database;

import de.noctivag.plugin.permissions.Rank;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * SQLite implementation for standalone server mode
 */
public class SQLiteProvider implements DatabaseProvider {
    private final JavaPlugin plugin;
    private final File databaseFile;
    private Connection connection;
    
    public SQLiteProvider(JavaPlugin plugin) {
        this.plugin = plugin;
        this.databaseFile = new File(plugin.getDataFolder(), "data.db");
    }
    
    @Override
    public void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            createTables();
            plugin.getLogger().info("SQLite database initialized successfully (Standalone mode)");
        } catch (ClassNotFoundException | SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize SQLite database", e);
        }
    }
    
    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create ranks table
            stmt.execute("CREATE TABLE IF NOT EXISTS ranks (" +
                    "name TEXT PRIMARY KEY, " +
                    "prefix TEXT, " +
                    "suffix TEXT, " +
                    "priority INTEGER, " +
                    "permissions TEXT, " +
                    "inherited TEXT" +
                    ")");
            
            // Create player_ranks table
            stmt.execute("CREATE TABLE IF NOT EXISTS player_ranks (" +
                    "player_uuid TEXT PRIMARY KEY, " +
                    "ranks TEXT" +
                    ")");
            
            // Create settings table
            stmt.execute("CREATE TABLE IF NOT EXISTS settings (" +
                    "key TEXT PRIMARY KEY, " +
                    "value TEXT" +
                    ")");
        }
    }
    
    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("SQLite database connection closed");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error closing SQLite connection", e);
        }
    }
    
    @Override
    public Map<String, Rank> loadRanks() {
        Map<String, Rank> ranks = new ConcurrentHashMap<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM ranks")) {
            
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
            plugin.getLogger().log(Level.SEVERE, "Error loading ranks from SQLite", e);
        }
        return ranks;
    }
    
    @Override
    public void saveRank(Rank rank) {
        String sql = "INSERT OR REPLACE INTO ranks (name, prefix, suffix, priority, permissions, inherited) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, rank.getName());
            pstmt.setString(2, rank.getPrefix());
            pstmt.setString(3, rank.getSuffix());
            pstmt.setInt(4, rank.getPriority());
            pstmt.setString(5, String.join(",", rank.getPermissions()));
            pstmt.setString(6, String.join(",", rank.getInheritedRanks()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving rank to SQLite: " + rank.getName(), e);
        }
    }
    
    @Override
    public void deleteRank(String rankName) {
        String sql = "DELETE FROM ranks WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, rankName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error deleting rank from SQLite: " + rankName, e);
        }
    }
    
    @Override
    public Map<UUID, Set<String>> loadPlayerRanks() {
        Map<UUID, Set<String>> playerRanks = new ConcurrentHashMap<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM player_ranks")) {
            
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
            plugin.getLogger().log(Level.SEVERE, "Error loading player ranks from SQLite", e);
        }
        return playerRanks;
    }
    
    @Override
    public void savePlayerRanks(UUID playerId, Set<String> ranks) {
        String sql = "INSERT OR REPLACE INTO player_ranks (player_uuid, ranks) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerId.toString());
            pstmt.setString(2, String.join(",", ranks));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving player ranks to SQLite: " + playerId, e);
        }
    }
    
    @Override
    public void removePlayerRanks(UUID playerId) {
        String sql = "DELETE FROM player_ranks WHERE player_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerId.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error removing player ranks from SQLite: " + playerId, e);
        }
    }
    
    @Override
    public String getDefaultRank() {
        String sql = "SELECT value FROM settings WHERE key = 'default_rank'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("value");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting default rank from SQLite", e);
        }
        return "default";
    }
    
    @Override
    public void setDefaultRank(String rankName) {
        String sql = "INSERT OR REPLACE INTO settings (key, value) VALUES ('default_rank', ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, rankName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error setting default rank in SQLite", e);
        }
    }
}
