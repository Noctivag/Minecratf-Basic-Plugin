# Plugin Funktions- und Permissions-Prüfung

## ✅ Commands mit Permission-Checks

### Prefix/Nick System
- [x] `/prefix` - plugin.prefix (default: true)
- [x] `/unprefix` - plugin.prefix (default: true)
- [x] `/nick` - plugin.nick (default: true)
- [x] `/unnick` - plugin.nick (default: true)

### Workbench Commands
- [x] `/craftingtable` - workbench.craftingtable (default: true)
- [x] `/anvil` - workbench.anvil (default: true)
- [x] `/enderchest` - workbench.enderchest (default: true)
- [x] `/grindstone` - workbench.grindstone (default: true)
- [x] `/smithingtable` - workbench.smithingtable (default: true)
- [x] `/stonecutter` - workbench.stonecutter (default: true)
- [x] `/loom` - workbench.loom (default: true)
- [x] `/cartography` - workbench.cartography (default: true)

### Basic Commands
- [x] `/heal` - basiccommands.heal (default: op)
- [x] `/feed` - basiccommands.feed (default: op)
- [x] `/clearinventory` - basiccommands.clearinventory (default: op)
- [x] `/fly` - basiccommands.fly (default: op)
- [x] `/gmc` - basiccommands.gamemode.creative (default: op)
- [x] `/gms` - basiccommands.gamemode.survival (default: op)
- [x] `/gmsp` - basiccommands.gamemode.spectator (default: op)

### Trigger Commands
- [x] `/sit` - plugin.sit (default: true)
- [x] `/cam` - plugin.cam (default: true)

### Menu & Join Message
- [x] `/menu` - plugin.menu (default: true)
- [x] `/joinmessage` - plugin.joinmessage (default: op)

## ✅ Permission-Hierarchie

### plugin.* (default: op)
Gibt Zugriff auf:
- plugin.prefix
- plugin.nick
- plugin.joinmessage
- plugin.sit
- plugin.cam
- plugin.menu
- basiccommands.*
- workbench.*

### basiccommands.* (default: op)
Gibt Zugriff auf:
- basiccommands.heal
- basiccommands.feed
- basiccommands.clearinventory
- basiccommands.fly
- basiccommands.gamemode.*

### workbench.* (default: true)
Gibt Zugriff auf alle Workbench-Commands

## ✅ ColorUtils Unterstützung

### Unterstützte Formate:
- Legacy-Codes: `&a`, `&c`, `&e` usw.
- Hex-Codes: `#FF0000` oder `&#FF0000`
- Gradient: `<gradient:#FF0000:#0000FF>text</gradient>`
- Rainbow: `<rainbow>text</rainbow>`

### Verwendbar in:
- /prefix
- /nick
- /joinmessage

## ✅ Konfigurierbare Einstellungen

### config.yml
- Sprache (de_DE)
- Auto-Save Intervall
- Debug-Modus
- Join/Quit Messages
- Command Cooldowns

### Alle Permissions in plugin.yml konfigurierbar:
- default: true (für alle Spieler)
- default: op (nur für OPs)
- Individuell über Permissions-Plugin überschreibbar

## ✅ Entferntes Event-System
- EventManager - ENTFERNT
- EventMenu - ENTFERNT
- CosmeticsMenu - ENTFERNT
- ParticleManager - ENTFERNT
- Event-Konfiguration in config.yml - ENTFERNT

## Vollständiger Funktionstest

### 1. Prefix/Nick Commands
```
/prefix &c[Admin]
/prefix <gradient:#FF0000:#00FF00>[VIP]</gradient>
/nick <rainbow>TestName</rainbow>
```

### 2. Workbench Commands
```
/craftingtable
/anvil
/enderchest
```

### 3. Basic Commands
```
/heal
/feed
/fly
/gmc
```

### 4. Trigger Commands
```
/sit - Setzt den Spieler auf einen unsichtbaren ArmorStand (+0.3 Höhe)
/cam - Aktiviert Kamera-Modus (Adventure + Unsichtbarkeit + Flug)
```

### 5. Menu
```
/menu - Öffnet das Hauptmenü (ohne Cosmetics/Events)
```

## Status: ✅ ALLE CHECKS BESTANDEN

- Alle Commands haben Permission-Checks
- Alle Permissions sind in plugin.yml definiert
- Alle Permissions sind konfigurierbar (default: true/op)
- ColorUtils unterstützt alle gewünschten Formate
- Event-System vollständig entfernt
- Plugin kompiliert erfolgreich
