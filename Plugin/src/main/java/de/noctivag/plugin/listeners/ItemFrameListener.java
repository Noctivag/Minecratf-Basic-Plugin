package de.noctivag.plugin.listeners;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import de.noctivag.plugin.Plugin;

public class ItemFrameListener implements Listener {

    private final Plugin plugin;

    public ItemFrameListener(Plugin plugin) {
        this.plugin = plugin;
    }

    // Make existing frames invisible when right-clicked while holding the special frame
    @EventHandler(priority = org.bukkit.event.EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ItemFrame itemFrame) {
            ItemStack itemInHand = event.getPlayer().getInventory().getItemInMainHand();
            if (itemInHand.getType() == Material.ITEM_FRAME) {
                ItemMeta meta = itemInHand.getItemMeta();
                if (meta != null && meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "invisible-frame"), PersistentDataType.BYTE)) {
                    itemFrame.setVisible(false);
                    itemFrame.setGlowing(false);
                    event.setCancelled(true);
                }
            }
        }
    }

    // Ensure newly placed frames are invisible immediately when using the special frame item
    @EventHandler(priority = org.bukkit.event.EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        if (event.getEntity() instanceof ItemFrame itemFrame) {
            ItemStack hand = event.getItemStack();
            if (hand != null && hand.getType() == Material.ITEM_FRAME) {
                ItemMeta meta = hand.getItemMeta();
                if (meta != null && meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "invisible-frame"), PersistentDataType.BYTE)) {
                    itemFrame.setVisible(false);
                    itemFrame.setGlowing(false);
                }
            }
        }
    }
}
