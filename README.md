# Eclipse SMP — DimensionWars

Paper **1.21.11** plugin (Java 21) that turns a server into three competing realms.

- **Three 400×400 kingdoms** (Overworld, Nether, End) that build themselves on startup,
  each with a plaza, roads, walls, gates, towers, an altar, a portal and a boss cage.
- **Kingdom maps**: bundled Sponge schematics are pasted over the kingdom heart
  (`end.schem` = Eclipse SMP End Castle, `overworld.schem` = uploaded Overworld map).
  The Nether kingdom uses the procedural build.
- **Three-phase bosses** with four abilities each, health bars, boss healing items,
  a sleeping caged form and a wake/calm cycle.
- **Key portals**: craft 3 keys, insert them into a kingdom portal's item frames and
  open a portal to another realm.
- **Altar revival rituals**, the **Netherite Warblade**, per-realm hearts and a
  self-hosted resource pack with custom key, sword, catalyst and boss models.

## Requirements

- JDK 21+
- Maven 3.9+
- Paper 1.21.11 (or a compatible 1.21.x build)

## Build

```bash
mvn package
```

The plugin jar is written to `target/DimensionWars.jar`.

## Install

1. Build the jar (or copy `target/DimensionWars.jar`).
2. Drop it into the server's `plugins/` folder.
3. Restart the server. Kingdoms are chosen near each world's spawn and built
   automatically; watch the console for "Kingdom is ready".
4. Players get the realm selector on join. Admins can use `/dw`.

The resource pack is hosted by the plugin itself on port `8123` by default. Set
`resource-pack.public-url` to the address players can reach (for example
`http://play.example.com:8123/pack.zip`); otherwise the server IP is used.

## Commands

| Command | Description |
| --- | --- |
| `/dw menu [player]` | Open the realm selector |
| `/dw info` | Player counts per realm |
| `/dw key portal [dimension]` | Show which realms a portal can reach |
| `/dw kingdom build <dimension>` | Rebuild a kingdom (procedural + schematic) |
| `/dw kingdom tp <dimension>` | Teleport to a kingdom spawn |
| `/dw setspawn <dimension>` | Set a realm spawn |
| `/dw setaltar <dimension>` | Set a revival altar |
| `/dw setportal <dimension>` | Set a portal anchor |
| `/dw boss spawn <dimension>` | Spawn the boss once (caged/sleeping rules apply) |
| `/dw spawn boss <dimension>` | Force-spawn the boss at your position, ignoring the once-only rule |
| `/dw boss reset [dimension]` | Remove duplicates, restore the caged boss at full health and heal every player |
| `/dw ritual start <dimension>` | Start a revival ritual |
| `/dw redeem <code>` | Redeem a boss ritual code |
| `/dw give-key <dimension>` | Give a portal key |
| `/dw resetplayer <player>` | Reset a player's realm data |
| `/dw give-table` | Give the guide book |
| `/dw pack` | Re-send the resource pack |
| `/dw reset` | Reset all plugin data |
| `/dw reload` | Reload `config.yml` |

Permissions: `dimensionwars.admin` (op), `dimensionwars.menu`, `dimensionwars.info`.

## Key recipe

Shaped recipe in a crafting table (output: **3 keys**):

```
O G O      O = Ominous Bottle (2)
G N G      G = realm block (5) — Gold Block, Purpur Block or Emerald Block
G S G      N = Netherite Ingot
           S = Nether Star
```

The key item is a netherite ingot with `custom_model_data` 3001–3003, matching the
resource pack overrides.

## Kingdom maps

Schematics live in `plugins/DimensionWars/schematics/` (bundled copies are extracted
on first use) and are configured in `config.yml`:

```yaml
kingdoms:
  schematics:
    enabled: true
    blocks-per-tick: 12000
    place-air: true
    sink: 4
    end: end
    nether: ''
    overworld: overworld
```

Both Sponge v2 (`.schematic`) and v3 (`.schem`) files are supported by the built-in
paster, so WorldEdit/FAWE is not required. Chunks for the kingdom footprint are
generated asynchronously before building, which keeps the server tick stable.

## Resource pack

Pack assets are in `pack/` and are zipped/hosted by the plugin (`pack_format` 75 for
1.21.11). Custom models: three portal keys, the Netherite Warblade, three ritual
catalysts and nine boss models (easy/medium/hard per realm). `/dw pack` prints the
hosted URL and SHA-1.

## Repository layout

```
pom.xml
pack/                                   resource pack assets
src/main/java/com/dimensionwars/
  DimensionWars.java                    plugin: commands, kingdoms, bosses, portals, pack host
  SchematicPaster.java                  Sponge schematic reader and sliced paster
src/main/resources/
  config.yml, plugin.yml
  schematics/end.schem, overworld.schem bundled kingdom maps
_tools/                                 helper scripts used while developing/testing
```
