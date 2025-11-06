# Minecraft Basic Plugin - Enhanced Edition

A comprehensive all-in-one Minecraft server plugin with rank/permission system and essential server functionality.

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

## Requirements

- Minecraft 1.21+
- Paper/Spigot server
- Java 21+
- **For BungeeCord mode**: MySQL 5.7+ or MariaDB 10.2+

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

**Note**: This is an enhanced version of the original basic plugin, now featuring a complete rank system and essential server commands similar to LuckPerms and EssentialsX.
