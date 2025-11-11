# Minecraft Advanced Plugin - Next Level Edition

An advanced, modular all-in-one Minecraft server plugin with extensive features, external plugin integration (LuckPerms, PlaceholderAPI), and complete module control.

## 🌟 What's New in Next Level Edition

### 🔌 External Plugin Compatibility
- **LuckPerms Integration**: Full prefix/suffix sync and permission system integration
- **PlaceholderAPI Integration**: Complete placeholder expansion for compatibility with other plugins
- **Soft Dependencies**: Works seamlessly with or without external plugins

### ⚙️ Complete Module System
- **Every feature can be individually enabled/disabled** via config.yml
- Fine-grained control over all functionality
- Disable what you don't need for better performance
- Module status checking in all commands

### ⏱️ Advanced Cooldown System
- Per-command configurable cooldowns
- Persistent across server restarts
- Bypass permissions available
- Automatic cleanup of expired cooldowns

### 💾 Auto-Save System
- Configurable automatic saving intervals
- Saves player data, homes, warps, and cooldowns
- Async operation for zero lag
- Debug logging for monitoring

### 🎨 Enhanced Chat System
- Full chat formatting with prefixes/suffixes
- LuckPerms prefix/suffix sync in chat
- Color permission system
- Nickname support in chat messages

### 🚀 Performance Optimizations
- **Fixed critical nametag performance issue** (no longer updates all players)
- Async database operations
- Batch nametag updates
- Player data caching
- HikariCP connection pooling for MySQL

## Features

### 🎖️ Rank & Permission System
Built-in rank management system with **BungeeCord network support**:
- **BungeeCord Detection**: Automatically detects if running on a BungeeCord network
- **Dual Database Support**: 
  - **Standalone Mode**: SQLite database for local storage
  - **Network Mode**: MySQL/MariaDB for cross-server synchronization
- **Multiple Ranks**: Create unlimited custom ranks with priorities
- **Rank Inheritance**: Ranks can inherit permissions from other ranks
- **Custom Prefixes & Suffixes**: Set colored prefixes and suffixes for each rank
- **Permission Management**: Fine-grained permission control per rank
- **Player Rank Assignment**: Assign multiple ranks to players
- **Network Synchronization**: Changes sync instantly across all BungeeCord servers

#### Default Ranks
- **default** - Basic player rank with cosmetic permissions
- **vip** - VIP rank with extended permissions
- **mod** - Moderator rank with admin tools
- **admin** - Full administrative access

#### Database Modes
**Standalone Mode (SQLite)**:
- Automatically used when BungeeCord is not detected
- Stores data locally in `data.db` file
- No configuration required

**Network Mode (MySQL)**:
- Automatically activated when BungeeCord is detected
- Requires MySQL/MariaDB database
- Configure in `config.yml`:
```yaml
database:
  mysql:
    host: localhost
    port: 3306
    database: minecraft_ranks
    username: root
    password: password
```
- Automatic table creation on first startup
- HikariCP connection pooling for optimal performance

### 🏠 Home System
- `/sethome [name]` - Set a home location (max 5 homes)
- `/home [name]` - Teleport to a home location
- `/delhome <name>` - Delete a home location
- `/homes` - List all your homes

### 🌍 Warp System
- `/setwarp <name>` - Create a server warp point (admin)
- `/warp <name>` - Teleport to a warp point
- `/delwarp <name>` - Delete a warp point (admin)
- `/warps` - List all available warps

### 📍 Spawn System
- `/spawn` - Teleport to the server spawn
- `/setspawn` - Set the server spawn point (admin)

### 🚀 Teleportation Commands
- `/tp <player>` - Teleport to a player (admin)
- `/tpa <player>` - Send a teleport request to a player
- `/tphere <player>` - Teleport a player to you (admin)
- `/tpaccept` - Accept a pending teleport request
- `/tpdeny` - Deny a pending teleport request

### 👤 Player Customization
- `/prefix <text>` - Set a custom chat prefix
- `/unprefix` - Remove your custom prefix
- `/nick <name>` - Set a custom nickname
- `/unnick` - Remove your nickname
- `/joinmessage` - Manage custom join messages

### ⚡ Basic Commands
- `/heal` - Restore health and remove fire
- `/feed` - Restore hunger and saturation
- `/fly` - Toggle flight mode
- `/gmc` - Switch to Creative mode
- `/gms` - Switch to Survival mode
- `/gmsp` - Switch to Spectator mode
- `/clearinventory` - Clear your inventory

### 🔨 Workbench Commands
Open various workbenches without placing blocks:
- `/craftingtable` - Open crafting table
- `/anvil` - Open anvil
- `/enderchest` - Open ender chest
- `/grindstone` - Open grindstone
- `/smithingtable` - Open smithing table
- `/stonecutter` - Open stonecutter
- `/loom` - Open loom
- `/cartography` - Open cartography table

### 👮 Admin Commands
- `/kick <player> [reason]` - Kick a player from the server
- `/invsee <player>` - View a player's inventory
- `/day` - Set time to day
- `/night` - Set time to night
- `/sun` - Set weather to clear
- `/rain` - Set weather to rain

### 🎭 Cosmetic Features
- `/sit` - Sit down where you're standing
- `/cam` - Toggle camera/spectator mode
- `/menu` - Open the server GUI menu
- Particle effects (configurable)
- Custom join/quit messages

### 🎯 Rank Management Commands
- `/rank` - View your current rank
- `/rank <player>` - View another player's rank
- `/rank list` - List all available ranks
- `/rank create <name> <prefix> <priority>` - Create a new rank (admin)
- `/rank delete <name>` - Delete a rank (admin)
- `/rank setprefix <name> <prefix>` - Set rank prefix (admin)
- `/rank setsuffix <name> <suffix>` - Set rank suffix (admin)
- `/rank setpriority <name> <priority>` - Set rank priority (admin)
- `/rank addperm <name> <permission>` - Add permission to rank (admin)
- `/rank removeperm <name> <permission>` - Remove permission from rank (admin)
- `/setrank <player> <rank>` - Assign a rank to a player (admin)

## Permissions

### Basic Commands
- `basiccommands.heal` - Use /heal command
- `basiccommands.feed` - Use /feed command
- `basiccommands.fly` - Use /fly command
- `basiccommands.gamemode.*` - Use all gamemode commands
- `basiccommands.clearinventory` - Use /clearinventory command

### Workbench Commands
- `workbench.*` - Access to all workbenches (default: true)
- `workbench.craftingtable`, `workbench.anvil`, etc.

### Essentials Commands
- `essentials.sethome` - Set home points
- `essentials.home` - Teleport to homes
- `essentials.delhome` - Delete homes
- `essentials.setwarp` - Create warps (admin)
- `essentials.warp` - Use warps
- `essentials.spawn` - Use /spawn
- `essentials.setspawn` - Set spawn point (admin)
- `essentials.tp` - Teleport to players (admin)
- `essentials.tpa` - Send teleport requests
- `essentials.tphere` - Teleport players to you (admin)

### Rank System
- `rank.list` - View all ranks
- `rank.admin` - Full rank management access

### Admin Commands
- `admin.kick` - Kick players
- `admin.invsee` - View player inventories
- `admin.time` - Change world time
- `admin.weather` - Change weather

## Configuration

### Main Config (config.yml)
```yaml
settings:
  language: de_DE
  auto-save-interval: 300
  debug-mode: false

# Database configuration for BungeeCord network mode
database:
  mysql:
    host: localhost
    port: 3306
    database: minecraft_ranks
    username: root
    password: password

commands:
  heal:
    cooldown: 300
    cost: 100
  nick:
    min-length: 3
    max-length: 16
    allow-colors: true
  home:
    max-homes: 5
  teleport:
    request-timeout: 60
```

### Database Storage
The plugin automatically determines which database to use:

**Standalone Mode (No BungeeCord)**:
- Uses SQLite database stored in `plugins/plugin/data.db`
- No configuration needed
- Automatic table creation

**BungeeCord Network Mode**:
- Uses MySQL/MariaDB for network-wide synchronization
- Configure MySQL connection in `config.yml` (see above)
- All servers in the network share the same rank data
- Automatic table creation on first server startup
- HikariCP connection pooling (configurable pool sizes)

**Note**: In BungeeCord mode, the old YAML files (`ranks.yml`, `player_ranks.yml`) are not used. All data is stored in the MySQL database.

### Data Storage

**Standalone Mode**:
- `data.db` - SQLite database with all rank/permission data

**BungeeCord Network Mode**:
- MySQL database with tables: `ranks`, `player_ranks`, `settings`

**Other Data Files** (both modes):
- `homes.yml` - Player home locations
- `warps.yml` - Server warp points
- `spawn.yml` - Server spawn location

## Installation

### Standalone Server
1. Download the plugin JAR file
2. Place it in your server's `plugins` folder
3. Start/restart your server
4. Default ranks will be created automatically in SQLite
5. Plugin is ready to use!

### BungeeCord Network
1. Download the plugin JAR file
2. Place it in the `plugins` folder of **each server** in your network
3. Set up a MySQL/MariaDB database
4. Configure database credentials in `config.yml` on all servers
5. Enable BungeeCord in `spigot.yml` (`settings.bungeecord: true`)
6. Start/restart all servers
7. Plugin will automatically create MySQL tables and sync ranks across the network

## Configuration Examples

### Enabling LuckPerms Integration
```yaml
integrations:
  luckperms:
    enabled: true  # Enable LuckPerms integration
    use-for-permissions: true  # Use LuckPerms for permission checks
    sync-display-names: true  # Sync prefix/suffix from LuckPerms
    primary-group-only: true  # Only use primary group
```

### Enabling PlaceholderAPI
```yaml
integrations:
  placeholderapi:
    enabled: true  # Enable PlaceholderAPI integration
    register-expansions: true  # Register plugin placeholders
```

### Configuring Cooldowns
```yaml
cooldowns:
  enabled: true
  persist-on-restart: true  # Save cooldowns across restarts
  bypass-permission: plugin.cooldown.bypass
  commands:
    heal: 300  # 5 minutes
    feed: 300
    home: 5
    warp: 5
    spawn: 10
    tpa: 30
```

### Disabling Unwanted Modules
```yaml
modules:
  # Disable cosmetic features
  cosmetics:
    enabled: false  # Disables sit, camera, vanish

  # Or disable specific features
  cosmetics:
    enabled: true
    sit:
      enabled: false  # Only disable sit
    camera:
      enabled: true
    vanish:
      enabled: true

  # Disable entire command groups
  workbenches:
    enabled: false  # Disables all workbench commands
```

## Available Placeholders (PlaceholderAPI)

When PlaceholderAPI is enabled:
- `%noctivagplugin_prefix%` - Player's custom prefix
- `%noctivagplugin_suffix%` - Player's custom suffix
- `%noctivagplugin_nickname%` - Player's nickname or real name
- `%noctivagplugin_displayname%` - Full display with prefix + name + suffix
- `%noctivagplugin_rank%` - Player's rank name
- `%noctivagplugin_rank_prefix%` - Rank's prefix
- `%noctivagplugin_rank_suffix%` - Rank's suffix
- `%noctivagplugin_homes_count%` - Number of homes set
- `%noctivagplugin_homes_max%` - Maximum homes allowed
- `%noctivagplugin_flying%` - Flight status (true/false)

## Requirements

- Minecraft 1.21.8+
- Paper or Spigot server
- Java 21+
- **For BungeeCord mode**: MySQL 5.7+ or MariaDB 10.2+
- **Optional**: LuckPerms 5.4+ for permission integration
- **Optional**: PlaceholderAPI 2.11.6+ for placeholder support

## Building from Source

```bash
cd Plugin
mvn clean package
```

The compiled JAR will be in `Plugin/target/plugin-1.0-SNAPSHOT.jar`

## Support

For issues, feature requests, or questions, please visit the GitHub repository.

## Version

Current Version: 1.0-SNAPSHOT

## License

This plugin is provided as-is for use on Minecraft servers.

---

## 🎯 Next Level Features Summary

This "Next Level" edition includes:

✅ **Full LuckPerms Integration** - Use LuckPerms for all permissions and prefixes
✅ **PlaceholderAPI Support** - Compatible with all PAPI-dependent plugins
✅ **Complete Module System** - Enable/disable any feature
✅ **Advanced Cooldown System** - Persistent, configurable, bypassable
✅ **Auto-Save System** - Never lose data
✅ **Performance Fixed** - Critical nametag lag issue resolved
✅ **Chat Integration** - Full chat formatting with external plugin support
✅ **Developer API** - Easy integration for other plugins
✅ **100% Configurable** - Every aspect can be customized

**This is a professional-grade, production-ready plugin suitable for large servers and networks.**
