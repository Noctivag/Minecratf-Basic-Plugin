package de.noctivag.plugin.gui;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CosmeticsMenu extends CustomGUI {
    private static final Map<Integer, Particle> PARTICLE_SLOTS = new HashMap<>();

    static {
        PARTICLE_SLOTS.put(10, Particle.HEART);
        PARTICLE_SLOTS.put(11, Particle.FLAME);
        PARTICLE_SLOTS.put(12, Particle.PORTAL);
        PARTICLE_SLOTS.put(13, Particle.NOTE);
        PARTICLE_SLOTS.put(14, Particle.END_ROD);
        PARTICLE_SLOTS.put(15, Particle.TOTEM_OF_UNDYING);
        PARTICLE_SLOTS.put(16, Particle.DRAGON_BREATH);
    }

    public CosmeticsMenu() {
        super("§d§lCosmetics Menü", 3);
        initializeItems();
    }

    private void initializeItems() {
        // Partikel Effekte
        setParticleItem(10, Material.RED_DYE, "§c§lHerzen", Particle.HEART);
        setParticleItem(11, Material.BLAZE_POWDER, "§6§lFlammen", Particle.FLAME);
        setParticleItem(12, Material.OBSIDIAN, "§5§lPortal", Particle.PORTAL);
        setParticleItem(13, Material.NOTE_BLOCK, "§e§lNoten", Particle.NOTE);
        setParticleItem(14, Material.END_ROD, "§f§lStrahlen", Particle.END_ROD);
        setParticleItem(15, Material.TOTEM_OF_UNDYING, "§6§lTotem", Particle.TOTEM_OF_UNDYING);
        setParticleItem(16, Material.DRAGON_BREATH, "§d§lDrachenatem", Particle.DRAGON_BREATH);

        // Deaktivieren Button
        ItemStack disable = new ItemStack(Material.BARRIER);
        ItemMeta meta = disable.getItemMeta();
        meta.displayName(Component.text("§c§lEffekte deaktivieren"));
        disable.setItemMeta(meta);
        getInventory().setItem(22, disable);
    }

    private void setParticleItem(int slot, Material material, String name, Particle particle) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(
            Component.text("§7Klicke, um diesen"),
            Component.text("§7Partikeleffekt zu aktivieren!")
        ));
        item.setItemMeta(meta);
        getInventory().setItem(slot, item);
    }

    public Particle getParticleAtSlot(int slot) {
        return PARTICLE_SLOTS.get(slot);
    }
}
