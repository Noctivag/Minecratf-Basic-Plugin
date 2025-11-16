package de.noctivag.plugin.listeners;

import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class InvisibleItemFrameListener implements Listener {

    private final Plugin plugin;

    public InvisibleItemFrameListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame)) {
            return;
        }

        Player player = event.getPlayer();
        ItemFrame itemFrame = (ItemFrame) event.getRightClicked();

        if (itemFrame.isVisible()) {
            return;
        }

        if (player.isSneaking() && itemFrame.getItem().getType() == org.bukkit.Material.AIR) {
            event.setCancelled(true);
            itemFrame.remove();
            ItemStack itemFrameItem = new ItemStack(org.bukkit.Material.ITEM_FRAME);
            ItemMeta meta = itemFrameItem.getItemMeta();
            if (meta != null) {
                meta.displayName(net.kyori.adventure.text.Component.text("§fInvisible Item Frame"));
                PersistentDataContainer data = meta.getPersistentDataContainer();
                data.set(new org.bukkit.NamespacedKey(plugin, "invisible-frame"), PersistentDataType.BYTE, (byte) 1);
                itemFrameItem.setItemMeta(meta);
            }
            player.getInventory().addItem(itemFrameItem);
            player.sendMessage("§aYou have retrieved the invisible item frame.");
        }
    }
}
