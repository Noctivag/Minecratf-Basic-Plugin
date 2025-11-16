package de.noctivag.plugin.economy;

import de.noctivag.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages player economy - balances, transactions
 */
public class EconomyManager {
    private final Plugin plugin;
    private final Map<UUID, Double> balances = new HashMap<>();
    private File economyFile;
    private FileConfiguration economyConfig;
    private double startingBalance;
    private String currencySymbol;

    public EconomyManager(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
        loadEconomyData();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        startingBalance = config.getDouble("modules.economy.starting-balance", 1000.0);
        currencySymbol = config.getString("modules.economy.currency-symbol", "$");
    }

    private void loadEconomyData() {
        economyFile = new File(plugin.getDataFolder(), "economy.yml");
        if (!economyFile.exists()) {
            try {
                economyFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create economy.yml", e);
            }
        }

        economyConfig = YamlConfiguration.loadConfiguration(economyFile);

        // Load all balances
        if (economyConfig.contains("balances")) {
            for (String uuidStr : economyConfig.getConfigurationSection("balances").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                double balance = economyConfig.getDouble("balances." + uuidStr);
                balances.put(uuid, balance);
            }
        }
    }

    public void save() {
        // Save all balances
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            economyConfig.set("balances." + entry.getKey().toString(), entry.getValue());
        }

        try {
            economyConfig.save(economyFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save economy.yml", e);
        }
    }

    public double getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, startingBalance);
    }

    public double getBalance(Player player) {
        return getBalance(player.getUniqueId());
    }

    public void setBalance(UUID uuid, double amount) {
        balances.put(uuid, Math.max(0, amount));
    }

    public void setBalance(Player player, double amount) {
        setBalance(player.getUniqueId(), amount);
    }

    public boolean hasBalance(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }

    public boolean hasBalance(Player player, double amount) {
        return hasBalance(player.getUniqueId(), amount);
    }

    public boolean withdraw(UUID uuid, double amount) {
        if (!hasBalance(uuid, amount)) {
            return false;
        }
        setBalance(uuid, getBalance(uuid) - amount);
        return true;
    }

    public boolean withdraw(Player player, double amount) {
        return withdraw(player.getUniqueId(), amount);
    }

    public void deposit(UUID uuid, double amount) {
        setBalance(uuid, getBalance(uuid) + amount);
    }

    public void deposit(Player player, double amount) {
        deposit(player.getUniqueId(), amount);
    }

    public boolean transfer(UUID from, UUID to, double amount) {
        if (!hasBalance(from, amount)) {
            return false;
        }
        withdraw(from, amount);
        deposit(to, amount);
        return true;
    }

    public boolean transfer(Player from, Player to, double amount) {
        return transfer(from.getUniqueId(), to.getUniqueId(), amount);
    }

    public String formatBalance(double amount) {
        return currencySymbol + String.format("%.2f", amount);
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void reload() {
        loadConfig();
        loadEconomyData();
    }
}
