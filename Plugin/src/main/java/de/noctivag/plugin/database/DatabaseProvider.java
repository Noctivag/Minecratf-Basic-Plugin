package de.noctivag.plugin.database;

import de.noctivag.plugin.permissions.Rank;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Interface for database operations for rank/permission storage
 */
public interface DatabaseProvider {
    
    /**
     * Initialize the database and create tables if they don't exist
     */
    void initialize();
    
    /**
     * Close the database connection
     */
    void close();
    
    /**
     * Load all ranks from the database
     * @return Map of rank names to Rank objects
     */
    Map<String, Rank> loadRanks();
    
    /**
     * Save a rank to the database
     * @param rank The rank to save
     */
    void saveRank(Rank rank);
    
    /**
     * Delete a rank from the database
     * @param rankName The name of the rank to delete
     */
    void deleteRank(String rankName);
    
    /**
     * Load player rank assignments from the database
     * @return Map of player UUIDs to sets of rank names
     */
    Map<UUID, Set<String>> loadPlayerRanks();
    
    /**
     * Save player rank assignment to the database
     * @param playerId The player's UUID
     * @param ranks The set of rank names assigned to the player
     */
    void savePlayerRanks(UUID playerId, Set<String> ranks);
    
    /**
     * Remove all rank assignments for a player
     * @param playerId The player's UUID
     */
    void removePlayerRanks(UUID playerId);
    
    /**
     * Get the default rank name
     * @return The default rank name
     */
    String getDefaultRank();
    
    /**
     * Set the default rank name
     * @param rankName The default rank name
     */
    void setDefaultRank(String rankName);
}
