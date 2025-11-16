# BasicPlugin API Documentation

## For Plugin Developers

BasicPlugin provides a public API and custom events that other plugins can use to integrate with its features.

---

## Maven/Gradle Dependency

Add BasicPlugin as a dependency in your plugin:

**Maven:**
```xml
<dependencies>
    <dependency>
        <groupId>de.noctivag</groupId>
        <artifactId>plugin</artifactId>
        <version>1.0-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

**plugin.yml:**
```yaml
depend: [BasicPlugin]
# or
softdepend: [BasicPlugin]
```

---

## Using the API

### Basic Usage

```java
import de.noctivag.plugin.api.BasicPluginAPI;

public class YourPlugin extends JavaPlugin {
    private BasicPluginAPI basicAPI;
    
    @Override
    public void onEnable() {
        basicAPI = BasicPluginAPI.getInstance();
        if (basicAPI != null) {
            getLogger().info("BasicPlugin API loaded!");
        }
    }
    
    public void makeSomeonesit(Player player) {
        if (basicAPI != null && basicAPI.makeSit(player)) {
            player.sendMessage("You are now sitting!");
        }
    }
}
```

### Available Methods

```java
// Make a player sit
boolean success = basicAPI.makeSit(player);

// Make a player stand up
boolean success = basicAPI.makeUnsit(player);

// Check if player is sitting
boolean sitting = basicAPI.isSitting(player);

// Check if a module is enabled
boolean enabled = basicAPI.isModuleEnabled("modules.cosmetics.sit");

// Get plugin version
String version = basicAPI.getVersion();
```

---

## Custom Events

BasicPlugin fires custom events that you can listen to:

### PlayerSitEvent (Cancellable)

Called when a player attempts to sit down.

```java
import de.noctivag.plugin.events.PlayerSitEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class YourListener implements Listener {
    
    @EventHandler
    public void onPlayerSit(PlayerSitEvent event) {
        Player player = event.getPlayer();
        ArmorStand seat = event.getSeat();
        
        // Cancel sitting in certain conditions
        if (player.getWorld().getName().equals("nopvp")) {
            event.setCancelled(true);
            player.sendMessage("You cannot sit in this world!");
        }
        
        // Or just log it
        Bukkit.getLogger().info(player.getName() + " sat down at " + seat.getLocation());
    }
}
```

### PlayerUnsitEvent

Called when a player stands up from sitting (not cancellable).

```java
import de.noctivag.plugin.events.PlayerUnsitEvent;

@EventHandler
public void onPlayerUnsit(PlayerUnsitEvent event) {
    Player player = event.getPlayer();
    player.sendMessage("You stood up!");
}
```

---

## Armor Stand Identification

BasicPlugin tags sit armor stands with PersistentDataContainer data:

```java
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

// Check if an armor stand is a BasicPlugin seat
if (armorStand.getPersistentDataContainer().has(
    new NamespacedKey(yourPlugin, "sit_seat_owner"), 
    PersistentDataType.STRING)) {
    // This is a sit seat
    String ownerUUID = armorStand.getPersistentDataContainer().get(
        new NamespacedKey(yourPlugin, "sit_seat_owner"), 
        PersistentDataType.STRING);
}

// Or check custom name
if (armorStand.customName() != null && 
    armorStand.customName().equals(Component.text("BasicPlugin_Seat"))) {
    // This is our seat
}
```

---

## Event Priorities

BasicPlugin uses the following event priorities to ensure compatibility:

- **Item Frame events**: `EventPriority.HIGH` with `ignoreCancelled = true`
- **Player Join**: `EventPriority.NORMAL`
- **Player Quit**: `EventPriority.MONITOR`
- **Entity Dismount**: `EventPriority.MONITOR` with `ignoreCancelled = true`

This means:
- Your plugin can cancel item frame placement/interaction before BasicPlugin processes it
- Join events run at normal priority alongside most plugins
- Quit/dismount cleanup happens at MONITOR (last) to ensure proper cleanup

---

## Best Practices

1. **Always null-check the API**: The API instance might be null if BasicPlugin is disabled.

2. **Use soft-depend**: Unless your plugin absolutely requires BasicPlugin, use `softdepend` instead of `depend`.

3. **Listen to custom events**: Use `PlayerSitEvent` to prevent sitting in restricted areas or during combat.

4. **Check module status**: Features can be disabled in config, so check `isModuleEnabled()` before assuming functionality exists.

5. **Respect cancellations**: If you cancel `PlayerSitEvent`, the player won't sit and the armor stand will be removed automatically.

---

## Example: Combat Plugin Integration

```java
public class CombatPlugin extends JavaPlugin implements Listener {
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerSit(PlayerSitEvent event) {
        Player player = event.getPlayer();
        
        // Prevent sitting during combat
        if (isInCombat(player)) {
            event.setCancelled(true);
            player.sendMessage("§cYou cannot sit during combat!");
        }
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            BasicPluginAPI api = BasicPluginAPI.getInstance();
            
            // Force player to stand when taking damage
            if (api != null && api.isSitting(player)) {
                api.makeUnsit(player);
                player.sendMessage("§eYou stood up due to taking damage!");
            }
        }
    }
}
```

---

## Support

For issues or questions, visit: https://github.com/Noctivag/Minecratf-Basic-Plugin
