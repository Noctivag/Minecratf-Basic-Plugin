package de.noctivag.plugin.permissions;

import de.noctivag.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages player ranks and permissions
 */
public class RankManager {
    private final Plugin plugin;
    private final Map<String, Rank> ranks;
    private final Map<UUID, Set<String>> playerRanks;
    private final File ranksFile;
    private final File playerRanksFile;
    private String defaultRank;

    public RankManager(Plugin plugin) {
        this.plugin = plugin;
        this.ranks = new HashMap<>();
        this.playerRanks = new HashMap<>();
        this.ranksFile = new File(plugin.getDataFolder(), "ranks.yml");
        this.playerRanksFile = new File(plugin.getDataFolder(), "player_ranks.yml");
        this.defaultRank = "default";
        
        loadRanks();
        loadPlayerRanks();
        createDefaultRanks();
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

            // Create VIP rank
            Rank vipRank = new Rank("vip", "&6[VIP] &e", "", 10);
            vipRank.addPermission("basiccommands.fly");
            vipRank.addPermission("basiccommands.heal");
            vipRank.addPermission("basiccommands.feed");
            vipRank.addInheritedRank("default");
            ranks.put("vip", vipRank);

            // Create Mod rank
            Rank modRank = new Rank("mod", "&9[Mod] &b", "", 50);
            modRank.addPermission("essentials.tp");
            modRank.addPermission("essentials.tphere");
            modRank.addPermission("admin.kick");
            modRank.addPermission("admin.time");
            modRank.addPermission("admin.weather");
            modRank.addInheritedRank("vip");
            ranks.put("mod", modRank);

            // Create Admin rank
            Rank adminRank = new Rank("admin", "&c[Admin] &4", "", 100);
            adminRank.addPermission("*");
            adminRank.addInheritedRank("mod");
            ranks.put("admin", adminRank);

            saveRanks();
        }
    }

    public void loadRanks() {
        if (!ranksFile.exists()) {
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(ranksFile);
        ConfigurationSection ranksSection = config.getConfigurationSection("ranks");
        
        if (ranksSection != null) {
            for (String rankName : ranksSection.getKeys(false)) {
                ConfigurationSection rankSection = ranksSection.getConfigurationSection(rankName);
                if (rankSection != null) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("prefix", rankSection.getString("prefix", ""));
                    data.put("suffix", rankSection.getString("suffix", ""));
                    data.put("priority", rankSection.getInt("priority", 0));
                    data.put("permissions", rankSection.getStringList("permissions"));
                    data.put("inherited", rankSection.getStringList("inherited"));
                    
                    Rank rank = Rank.deserialize(rankName, data);
                    ranks.put(rankName, rank);
                }
            }
        }

        defaultRank = config.getString("default-rank", "default");
    }

    public void saveRanks() {
        FileConfiguration config = new YamlConfiguration();
        config.set("default-rank", defaultRank);

        for (Map.Entry<String, Rank> entry : ranks.entrySet()) {
            String rankName = entry.getKey();
            Rank rank = entry.getValue();
            Map<String, Object> data = rank.serialize();
            
            config.set("ranks." + rankName + ".prefix", data.get("prefix"));
            config.set("ranks." + rankName + ".suffix", data.get("suffix"));
            config.set("ranks." + rankName + ".priority", data.get("priority"));
            config.set("ranks." + rankName + ".permissions", data.get("permissions"));
            config.set("ranks." + rankName + ".inherited", data.get("inherited"));
        }

        try {
            config.save(ranksFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save ranks.yml", e);
        }
    }

    public void loadPlayerRanks() {
        if (!playerRanksFile.exists()) {
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(playerRanksFile);
        ConfigurationSection playersSection = config.getConfigurationSection("players");
        
        if (playersSection != null) {
            for (String uuidStr : playersSection.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                List<String> rankNames = playersSection.getStringList(uuidStr);
                playerRanks.put(uuid, new HashSet<>(rankNames));
            }
        }
    }

    public void savePlayerRanks() {
        FileConfiguration config = new YamlConfiguration();

        for (Map.Entry<UUID, Set<String>> entry : playerRanks.entrySet()) {
            config.set("players." + entry.getKey().toString(), new ArrayList<>(entry.getValue()));
        }

        try {
            config.save(playerRanksFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save player_ranks.yml", e);
        }
    }

    public Rank getRank(String name) {
        return ranks.get(name.toLowerCase());
    }

    public void createRank(String name, String prefix, String suffix, int priority) {
        Rank rank = new Rank(name.toLowerCase(), prefix, suffix, priority);
        ranks.put(name.toLowerCase(), rank);
        saveRanks();
    }

    public void deleteRank(String name) {
        ranks.remove(name.toLowerCase());
        saveRanks();
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
        savePlayerRanks();
    }

    public void addPlayerRank(UUID playerId, String rankName) {
        Set<String> ranks = playerRanks.computeIfAbsent(playerId, k -> new HashSet<>());
        ranks.add(rankName.toLowerCase());
        savePlayerRanks();
    }

    public void removePlayerRank(UUID playerId, String rankName) {
        Set<String> ranks = playerRanks.get(playerId);
        if (ranks != null) {
            ranks.remove(rankName.toLowerCase());
            if (ranks.isEmpty()) {
                playerRanks.remove(playerId);
            }
            savePlayerRanks();
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
        saveRanks();
    }
}
