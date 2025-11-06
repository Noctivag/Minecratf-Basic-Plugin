package de.noctivag.plugin.permissions;

import java.util.*;

/**
 * Represents a rank with permissions, prefix, suffix, and priority
 */
public class Rank {
    private final String name;
    private String prefix;
    private String suffix;
    private int priority;
    private final Set<String> permissions;
    private final Set<String> inheritedRanks;

    public Rank(String name) {
        this.name = name;
        this.prefix = "";
        this.suffix = "";
        this.priority = 0;
        this.permissions = new HashSet<>();
        this.inheritedRanks = new HashSet<>();
    }

    public Rank(String name, String prefix, String suffix, int priority) {
        this.name = name;
        this.prefix = prefix != null ? prefix : "";
        this.suffix = suffix != null ? suffix : "";
        this.priority = priority;
        this.permissions = new HashSet<>();
        this.inheritedRanks = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix != null ? prefix : "";
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix != null ? suffix : "";
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Set<String> getPermissions() {
        return new HashSet<>(permissions);
    }

    public void addPermission(String permission) {
        permissions.add(permission);
    }

    public void removePermission(String permission) {
        permissions.remove(permission);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission) || permissions.contains("*");
    }

    public Set<String> getInheritedRanks() {
        return new HashSet<>(inheritedRanks);
    }

    public void addInheritedRank(String rankName) {
        inheritedRanks.add(rankName);
    }

    public void removeInheritedRank(String rankName) {
        inheritedRanks.remove(rankName);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("prefix", prefix);
        data.put("suffix", suffix);
        data.put("priority", priority);
        data.put("permissions", new ArrayList<>(permissions));
        data.put("inherited", new ArrayList<>(inheritedRanks));
        return data;
    }

    public static Rank deserialize(String name, Map<String, Object> data) {
        Rank rank = new Rank(name);
        rank.setPrefix((String) data.getOrDefault("prefix", ""));
        rank.setSuffix((String) data.getOrDefault("suffix", ""));
        rank.setPriority((Integer) data.getOrDefault("priority", 0));
        
        List<String> perms = (List<String>) data.getOrDefault("permissions", new ArrayList<>());
        perms.forEach(rank::addPermission);
        
        List<String> inherited = (List<String>) data.getOrDefault("inherited", new ArrayList<>());
        inherited.forEach(rank::addInheritedRank);
        
        return rank;
    }
}
