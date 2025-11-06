# BungeeCord Network Support - Implementation Guide

## Overview

The plugin now includes automatic BungeeCord detection and network-wide rank synchronization. This allows you to manage ranks and permissions across multiple servers in your BungeeCord network from a single, shared database.

## How It Works

### Automatic Detection

On startup, the plugin performs the following checks:

1. **Check for BungeeCord API**: Looks for `net.md_5.bungee.api.ProxyServer` class
2. **Check spigot.yml**: Verifies `settings.bungeecord` is set to `true`
3. **Database Selection**: 
   - If BungeeCord detected → Use MySQL/MariaDB
   - If not detected → Use SQLite

### Database Modes

#### Standalone Mode (SQLite)
```
Server (Standalone)
    └── SQLite (data.db)
        ├── ranks table
        ├── player_ranks table
        └── settings table
```

- **Storage**: Local file in plugin folder (`data.db`)
- **Configuration**: None required
- **Use Case**: Single server setups
- **Advantages**: No external database needed, simple setup

#### BungeeCord Network Mode (MySQL)
```
BungeeCord Proxy
    ├── Server 1 (Hub)
    ├── Server 2 (Survival)
    ├── Server 3 (Creative)
    └── Server 4 (Minigames)
         ↓
    All connect to shared MySQL database
         ↓
    MySQL Database
        ├── ranks table
        ├── player_ranks table
        └── settings table
```

- **Storage**: Shared MySQL/MariaDB database
- **Configuration**: Required in `config.yml`
- **Use Case**: BungeeCord networks with multiple servers
- **Advantages**: Synchronized ranks across all servers, real-time updates

## Setup Instructions

### For Standalone Servers

1. Install the plugin
2. Start the server
3. Plugin automatically creates SQLite database
4. Done! No configuration needed

### For BungeeCord Networks

#### Step 1: Database Setup

Create a MySQL/MariaDB database:

```sql
CREATE DATABASE minecraft_ranks CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'minecraft'@'%' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON minecraft_ranks.* TO 'minecraft'@'%';
FLUSH PRIVILEGES;
```

#### Step 2: Enable BungeeCord

On each server in your network, edit `spigot.yml`:

```yaml
settings:
  bungeecord: true
```

#### Step 3: Configure Plugin

On each server, edit `plugins/plugin/config.yml`:

```yaml
database:
  mysql:
    host: your-database-host.com  # or localhost if on same machine
    port: 3306
    database: minecraft_ranks
    username: minecraft
    password: your_secure_password
    # Optional: Connection pool settings
    max-pool-size: 10
    min-idle: 2
```

#### Step 4: Install Plugin

1. Place the plugin JAR in the `plugins` folder of **each server**
2. Ensure all servers have the **same MySQL configuration**
3. Start all servers

#### Step 5: Verify

On any server, run:
```
/rank list
```

You should see the default ranks. Create a new rank on one server:
```
/rank create builder "&3[Builder] &b" 20
/rank addperm builder worldedit.*
```

The rank should be immediately available on all other servers!

## Database Schema

### Tables Created Automatically

#### `ranks` table
```sql
CREATE TABLE ranks (
    name VARCHAR(64) PRIMARY KEY,
    prefix VARCHAR(255),
    suffix VARCHAR(255),
    priority INT,
    permissions TEXT,
    inherited TEXT
);
```

#### `player_ranks` table
```sql
CREATE TABLE player_ranks (
    player_uuid VARCHAR(36) PRIMARY KEY,
    ranks TEXT
);
```

#### `settings` table
```sql
CREATE TABLE settings (
    setting_key VARCHAR(64) PRIMARY KEY,
    setting_value TEXT
);
```

## Command Examples

### Creating Custom Groups

```bash
# Create a custom builder rank
/rank create builder "&3[Builder] &b" 20

# Add WorldEdit permissions to the builder rank
/rank addperm builder worldedit.*
/rank addperm builder essentials.build

# Create a donator rank that inherits from default
/rank create donator "&6[Donator] &e" 15
/rank addperm donator essentials.fly
/rank addperm donator essentials.heal

# Assign a player to the builder rank
/setrank PlayerName builder
```

### Managing Permissions

```bash
# Add specific permission nodes
/rank addperm vip minecraft.command.gamemode
/rank addperm mod essentials.kick
/rank addperm admin *

# Remove permissions
/rank removeperm vip some.unwanted.permission

# List all ranks
/rank list

# View a player's rank
/rank PlayerName
```

### Modifying Ranks

```bash
# Change rank prefix
/rank setprefix vip "&6[VIP] &e"

# Change rank suffix
/rank setsuffix vip "&7"

# Change rank priority
/rank setpriority vip 25

# Delete a rank
/rank delete oldrank
```

## Network Synchronization

### How Synchronization Works

1. **Real-time Updates**: When you modify a rank on any server, the change is immediately written to MySQL
2. **Player Login**: When a player joins any server, their ranks are loaded from the shared database
3. **Permission Checks**: All permission checks query the current database state
4. **No Caching Issues**: Changes propagate instantly without server restarts

### Example Workflow

```
Admin on Hub Server:
    /rank create helper "&a[Helper] &2" 30
    /rank addperm helper essentials.kick
    
    → Saved to MySQL immediately

Player joins Survival Server:
    → Plugin loads ranks from MySQL
    → Player with "helper" rank has permissions
    → No manual sync needed!
```

## Connection Pooling

The plugin uses HikariCP for efficient database connection management:

```yaml
database:
  mysql:
    max-pool-size: 10    # Maximum connections in pool
    min-idle: 2          # Minimum idle connections
```

### Recommended Pool Sizes

- **Small Network** (1-3 servers): `max-pool-size: 5-10`
- **Medium Network** (4-10 servers): `max-pool-size: 10-20`
- **Large Network** (10+ servers): `max-pool-size: 20-30`

## Troubleshooting

### Plugin Not Detecting BungeeCord

**Check:**
1. Is `bungeecord: true` in `spigot.yml`?
2. Are you running a Paper/Spigot server (not vanilla)?
3. Check server logs for "BungeeCord detected" or "Standalone mode detected"

### MySQL Connection Failed

**Check:**
1. MySQL credentials in `config.yml`
2. MySQL server is running: `systemctl status mysql`
3. Firewall allows connection to MySQL port (3306)
4. User has correct permissions: `GRANT ALL PRIVILEGES ON minecraft_ranks.* TO 'user'@'%'`
5. Check plugin logs for specific error messages

### Ranks Not Syncing

**Check:**
1. All servers using the **same database** (check config.yml)
2. All servers can connect to MySQL (check logs)
3. Try reloading plugin: `/reload confirm` (not recommended in production)
4. Check MySQL tables exist: `SHOW TABLES;`

### Permission Issues

**Check:**
1. Player has correct rank: `/rank PlayerName`
2. Rank has permission: `/rank list` then check rank details
3. Permission node is correct (case-sensitive)
4. Rank inheritance is set up correctly

## Performance Considerations

### Database Location

- **Same Machine**: Lowest latency, best performance
- **Local Network**: Very good performance (< 1ms latency)
- **Remote Server**: May add latency, use connection pooling

### Optimization Tips

1. **Use Local Database**: Host MySQL on same machine as BungeeCord proxy or servers
2. **Connection Pooling**: Properly configure pool sizes
3. **Network Speed**: Ensure good network connection between servers and database
4. **Database Indexes**: Tables automatically include indexes on primary keys

## Migration from YAML to Database

If you have existing rank data in YAML files:

### Option 1: Automatic Migration (Standalone → BungeeCord)

1. Keep your existing `ranks.yml` and `player_ranks.yml`
2. Enable BungeeCord mode
3. Plugin will automatically import YAML data to MySQL on first run

### Option 2: Manual Migration

1. Note down all existing ranks and permissions
2. Enable BungeeCord mode with MySQL
3. Plugin creates default ranks
4. Recreate your custom ranks using `/rank` commands
5. Reassign players using `/setrank`

## Best Practices

### Rank Management

1. **Use Inheritance**: Create a hierarchy (default → vip → mod → admin)
2. **Specific Permissions**: Avoid wildcard (*) except for admin ranks
3. **Test Permissions**: Always test new ranks before assigning to players
4. **Backup Database**: Regularly backup your MySQL database

### Security

1. **Strong Passwords**: Use secure passwords for MySQL users
2. **Limit Access**: Only allow connections from your server IPs
3. **Regular Updates**: Keep MySQL/MariaDB updated
4. **Firewall Rules**: Restrict MySQL port (3306) access

### Network Setup

1. **Consistent Config**: Ensure all servers have identical `config.yml` database settings
2. **Test Before Production**: Test rank changes on a test server first
3. **Monitor Logs**: Check server logs for database connection issues
4. **Plan Downtime**: Schedule database maintenance during low-traffic times

## Example Network Configuration

### 3-Server BungeeCord Network

**Hub Server** (hub.yourserver.com):
```yaml
database:
  mysql:
    host: database.yourserver.com
    port: 3306
    database: minecraft_ranks
    username: mc_hub
    password: secure_password_here
```

**Survival Server** (survival.yourserver.com):
```yaml
database:
  mysql:
    host: database.yourserver.com  # Same database
    port: 3306
    database: minecraft_ranks      # Same database name
    username: mc_survival
    password: secure_password_here
```

**Creative Server** (creative.yourserver.com):
```yaml
database:
  mysql:
    host: database.yourserver.com  # Same database
    port: 3306
    database: minecraft_ranks      # Same database name
    username: mc_creative
    password: secure_password_here
```

**Result**: All three servers share the same rank system, players have consistent ranks everywhere!

## Conclusion

The BungeeCord network support provides:

✅ **Automatic Detection**: No manual mode switching needed
✅ **Zero Configuration** for standalone servers
✅ **Easy Setup** for BungeeCord networks
✅ **Real-time Sync**: Instant rank updates across all servers
✅ **Production Ready**: HikariCP pooling, thread-safe operations
✅ **Flexible**: Works with both SQLite and MySQL

Questions? Check the main README.md or open an issue on GitHub!
