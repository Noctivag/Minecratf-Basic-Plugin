# 🎮 Minecraft Plugin - Vollständige Funktionsübersicht

## ✅ Build-Status
- **Status**: BUILD SUCCESS
- **JAR-Größe**: 69 KB
- **Java-Dateien**: 27
- **Kompiliert für**: Java 21, Minecraft 1.21.8

---

## 📋 Command-Übersicht mit Berechtigungen

### 🏷️ Prefix & Nickname System
| Command | Permission | Standard | Beschreibung |
|---------|-----------|----------|--------------|
| `/prefix <text>` | `plugin.prefix` | ✅ Alle | Setzt deinen Chat-Prefix |
| `/unprefix` | `plugin.prefix` | ✅ Alle | Entfernt deinen Prefix |
| `/nick <name>` | `plugin.nick` | ✅ Alle | Ändert deinen Anzeigenamen |
| `/unnick` | `plugin.nick` | ✅ Alle | Entfernt deinen Nickname |

**Unterstützte Farbformate:**
```
&c[Admin]                                    # Legacy-Codes
#FF0000[VIP]                                 # Hex-Codes
<gradient:#FF0000:#00FF00>[VIP]</gradient>   # Farbverläufe
<rainbow>[★]</rainbow>                       # Rainbow-Effekt
```

---

### 🛠️ Workbench Commands
| Command | Permission | Standard | Beschreibung |
|---------|-----------|----------|--------------|
| `/craftingtable` | `workbench.craftingtable` | ✅ Alle | Öffnet Werkbank |
| `/anvil` | `workbench.anvil` | ✅ Alle | Öffnet Amboss |
| `/enderchest` | `workbench.enderchest` | ✅ Alle | Öffnet Endertruhe |
| `/grindstone` | `workbench.grindstone` | ✅ Alle | Öffnet Schleifstein |
| `/smithingtable` | `workbench.smithingtable` | ✅ Alle | Öffnet Schmiedetisch |
| `/stonecutter` | `workbench.stonecutter` | ✅ Alle | Öffnet Steinsäger |
| `/loom` | `workbench.loom` | ✅ Alle | Öffnet Webstuhl |
| `/cartography` | `workbench.cartography` | ✅ Alle | Öffnet Kartentisch |

**Aliases**: `/craft`, `/workbench`, `/ec`, `/cartographytable`

---

### ⚡ Basic Commands (Admin)
| Command | Permission | Standard | Beschreibung |
|---------|-----------|----------|--------------|
| `/heal` | `basiccommands.heal` | 🔒 OP | Heilt vollständig |
| `/feed` | `basiccommands.feed` | 🔒 OP | Stillt Hunger |
| `/clearinventory` | `basiccommands.clearinventory` | 🔒 OP | Leert Inventar |
| `/fly` | `basiccommands.fly` | 🔒 OP | Aktiviert/Deaktiviert Flug |
| `/gmc` | `basiccommands.gamemode.creative` | 🔒 OP | Kreativmodus |
| `/gms` | `basiccommands.gamemode.survival` | 🔒 OP | Überlebensmodus |
| `/gmsp` | `basiccommands.gamemode.spectator` | 🔒 OP | Zuschauermodus |

**Aliases**: `/ci`

---

### 🎯 Trigger Commands
| Command | Permission | Standard | Beschreibung |
|---------|-----------|----------|--------------|
| `/sit` | `plugin.sit` | ✅ Alle | Setzt dich hin/lässt dich aufstehen |
| `/cam` | `plugin.cam` | ✅ Alle | Aktiviert Kamera-Modus |

**Sit-Modus:**
- Spieler sitzt auf unsichtbarem ArmorStand
- Position um +0.3 Blöcke erhöht (verhindert Glitching)

**Cam-Modus:**
- Spieler wird unsichtbar (Potion Effect)
- Adventure-Modus mit aktiviertem Flug
- Sichtbarer ArmorStand-Dummy mit Equipment bleibt zurück
- Kann NICHT durch Blöcke fliegen (im Gegensatz zu Spectator)
- Teleport zurück zur Startposition beim Beenden

**Aliases**: `/triggersit`, `/triggercam`, `/camera`

---

### 📢 Join Message System
| Command | Permission | Standard | Beschreibung |
|---------|-----------|----------|--------------|
| `/joinmessage set <player> <msg>` | `plugin.joinmessage` | 🔒 OP | Setzt Join-Message |
| `/joinmessage remove <player>` | `plugin.joinmessage` | 🔒 OP | Entfernt Join-Message |
| `/joinmessage toggle <player>` | `plugin.joinmessage` | 🔒 OP | Schaltet Message um |
| `/joinmessage reload` | `plugin.joinmessage.reload` | 🔒 OP | Lädt Config neu |

**Aliases**: `/jmsg`, `/joinmsg`

**Farbcodes werden unterstützt** - siehe Prefix-Beispiele oben!

---

### 🎨 Menu System
| Command | Permission | Standard | Beschreibung |
|---------|-----------|----------|--------------|
| `/menu` | `plugin.menu` | ✅ Alle | Öffnet Hauptmenü |

**Aliases**: `/servermenu`, `/gui`

---

## 🔑 Permission-Hierarchie

### Haupt-Permissions
```yaml
plugin.*                    # Alle Plugin-Features (default: op)
├── plugin.prefix           # Prefix-Commands (default: true)
├── plugin.nick             # Nick-Commands (default: true)
├── plugin.joinmessage      # Join-Messages (default: op)
├── plugin.sit              # Sit-Command (default: true)
├── plugin.cam              # Cam-Command (default: true)
├── plugin.menu             # Menu-Command (default: true)
├── basiccommands.*         # Alle Basic-Commands (default: op)
└── workbench.*             # Alle Workbench-Commands (default: true)
```

### Basic Commands Hierarchie
```yaml
basiccommands.*                      # Alle Basic-Commands (default: op)
├── basiccommands.heal              # Heal-Command
├── basiccommands.feed              # Feed-Command
├── basiccommands.clearinventory    # Clear-Command
├── basiccommands.fly               # Fly-Command
└── basiccommands.gamemode.*        # Alle Gamemode-Commands
    ├── basiccommands.gamemode.creative
    ├── basiccommands.gamemode.survival
    └── basiccommands.gamemode.spectator
```

### Workbench Hierarchie
```yaml
workbench.*                    # Alle Workbench-Commands (default: true)
├── workbench.craftingtable
├── workbench.anvil
├── workbench.enderchest
├── workbench.grindstone
├── workbench.smithingtable
├── workbench.stonecutter
├── workbench.loom
└── workbench.cartography
```

---

## 🎨 ColorUtils - Erweiterte Farbunterstützung

### Unterstützte Formate

#### 1. Legacy Minecraft-Codes
```
&0 = Schwarz       &8 = Dunkelgrau
&1 = Dunkelblau    &9 = Blau
&2 = Dunkelgrün    &a = Grün
&3 = Dunkel Aqua   &b = Aqua
&4 = Dunkelrot     &c = Rot
&5 = Dunkel Lila   &d = Lila
&6 = Gold          &e = Gelb
&7 = Grau          &f = Weiß
```

#### 2. Hex-Codes
```
#FF0000           # Standard Hex
&#FF0000          # Legacy-Hex Format
```

#### 3. Gradient (Farbverlauf)
```
<gradient:#FF0000:#0000FF>Text</gradient>
<gradient:#FF0000:#00FF00:#0000FF>Multi-Gradient</gradient>
```

#### 4. Rainbow
```
<rainbow>Regenbogen-Text</rainbow>
```

### Verwendungsbeispiele

```
/prefix &c[&4Admin&c]
/prefix #FF0000[VIP]
/prefix <gradient:#FF0000:#00FF00>[Moderator]</gradient>
/prefix <rainbow>[★ Premium ★]</rainbow>
/nick <gradient:#00FFFF:#FF00FF>CoolName</gradient>
/joinmessage set Player &7[&a+&7] <rainbow>%player%</rainbow> &7joined!
```

---

## 📁 Konfigurationsdateien

### config.yml
```yaml
settings:
  language: de_DE
  auto-save-interval: 300
  debug-mode: false
  check-player-exists: true

join-messages:
  default-message: "&7[&a+&7] &e%player% &7hat den Server betreten"
  examples:
    - "<gradient:#FF0000:#00FF00>%player%</gradient>"
    - "<rainbow>%player%</rainbow>"

quit-messages:
  default-message: "&7[&c-&7] &e%player% &7hat den Server verlassen"

commands:
  heal:
    cooldown: 300
```

### Sprach-Dateien
- `messages_de_DE.yml` - Deutsche Nachrichten
- `messages_en_US.yml` - Englische Nachrichten
- `join_messages.yml` - Gespeicherte Join-Messages

---

## ✅ Entfernte Systeme

Die folgenden Systeme wurden vollständig entfernt:
- ❌ EventManager (Dragon/Wither Events)
- ❌ EventMenu (Event-GUI)
- ❌ CosmeticsMenu (Partikel-GUI)
- ❌ ParticleManager (Partikeleffekte)

---

## 🔧 Konfigurierbarkeit

### Alle Permissions sind anpassbar!

In `plugin.yml` oder per Permissions-Plugin (LuckPerms, etc.):

```yaml
# Beispiel: Erlaube allen Spielern den /heal Command
permissions:
  basiccommands.heal:
    default: true  # Ändern von 'op' zu 'true'

# Beispiel: Nur OPs dürfen /sit nutzen
permissions:
  plugin.sit:
    default: op    # Ändern von 'true' zu 'op'
```

### Per LuckPerms/PermissionsEx:
```
/lp group default permission set basiccommands.heal true
/lp group vip permission set plugin.prefix true
/lp user Spieler permission set basiccommands.* true
```

---

## 📊 Zusammenfassung

✅ **27 Java-Klassen** kompiliert  
✅ **24 Commands** registriert  
✅ **35+ Permissions** definiert  
✅ **Alle Commands** haben Permission-Checks  
✅ **Alle Permissions** sind konfigurierbar  
✅ **ColorUtils** unterstützt Legacy, Hex, Gradient & Rainbow  
✅ **Event-System** vollständig entfernt  
✅ **Sit/Cam-Modi** funktionieren korrekt  
✅ **Build erfolgreich** - Keine Fehler  

---

## 🚀 Installation

1. Lade `plugin-1.0-SNAPSHOT.jar` aus `/Plugin/target/` herunter
2. Platziere die JAR in `/plugins/` deines Paper-Servers
3. Starte den Server neu
4. Konfiguriere Permissions nach Bedarf
5. Fertig!

**Mindestanforderungen:**
- Paper/Spigot 1.21.8+
- Java 21+

---

**Plugin-Version**: 1.0-SNAPSHOT  
**Letzter Build**: 2025-11-05  
**Status**: Production Ready ✅
