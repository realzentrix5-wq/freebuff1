# Eclipse SMP v1

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

The plugin jar is written to `target/EclipseSMP-1.0.0.jar`.

## Install

1. Build the jar (or copy `target/EclipseSMP-1.0.0.jar`).
2. Drop it into the server's `plugins/` folder.
3. Restart the server. Kingdoms are chosen near each world's spawn and built
   automatically; watch the console for "Kingdom is ready".
4. Players get the realm selector on join. Admins can use `/esmp`.

The resource pack is hosted by the plugin itself on port `8123` by default. Set
`resource-pack.public-url` to the address players can reach (for example
`http://play.example.com:8123/pack.zip`); otherwise the server IP is used.

## Commands

| Command | Description |
| --- | --- |
| `/esmp menu [player]` | Open the realm selector |
| `/esmp info` | Player counts per realm |
| `/esmp key portal [dimension]` | Show which realms a portal can reach |
| `/esmp kingdom build <dimension>` | Rebuild a kingdom (procedural + schematic) |
| `/esmp kingdom tp <dimension>` | Teleport to a kingdom spawn |
| `/esmp setspawn <dimension>` | Set a realm spawn |
| `/esmp setaltar <dimension>` | Set a revival altar |
| `/esmp setportal <dimension>` | Set a portal anchor |
| `/esmp boss list [dimension]` | Show each realm boss's state, tier, health, position and whether it is awake |
| `/esmp boss spawn <dimension>` | Spawn the boss once (caged/sleeping rules apply) |
| `/esmp spawn boss <dimension>` | Force-spawn the boss at your position, ignoring the once-only rule |
| `/esmp boss reset [dimension]` | Remove duplicates, restore the caged boss at full health and heal every player |
| `/esmp boss heal [dimension]` | Heal a realm boss back to full health (admin testing helper) |
| `/esmp boss kill [dimension]` | Kill a realm boss through the normal death path (admin testing helper) |
| `/esmp ritual start <dimension>` | Start a revival ritual |
| `/esmp redeem <code>` | Redeem a boss ritual code |
| `/esmp give-key <dimension>` | Give a portal key |
| `/esmp resetplayer <player>` | Reset a player's realm data |
| `/esmp give-table` | Give the guide book |
| `/esmp pack` | Re-send the resource pack |
| `/esmp reset` | Reset all plugin data |
| `/esmp reload` | Reload `config.yml` |

Permissions: `eclipsesmp.admin` (op), `eclipsesmp.menu`, `eclipsesmp.info`.

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

Schematics live in `plugins/EclipseSMP/schematics/` (bundled copies are extracted
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
catalysts and nine boss models (easy/medium/hard per realm). `/esmp pack` prints the
hosted URL and SHA-1.

Selection uses the 1.21.4+ item model definitions (`assets/minecraft/items/*.json`
with `minecraft:range_dispatch` on `minecraft:custom_model_data`); the legacy
`overrides` field no longer exists in 1.21.11. Boss models carry `fixed`/`gui`/`ground`
display transforms copied from the client's `ItemTransform` order, so the `ItemDisplay`
figure stands on the ground at ~2.8 blocks and faces the same way as the boss.

Run `python3 _tools/validate_pack.py` after editing the pack to re-check every model,
texture, threshold, transform and texture UV. Pass a 1.21.11 `client.jar` to also
validate the definitions against the official vanilla assets. `_tools/preview_boss.py`
(needs Pillow) renders the boss models isometrically for a quick visual sanity check
without launching the game.

## Repository layout

```
pom.xml
pack/                                   resource pack assets
src/main/java/com/eclipsesmp/
  EclipseSMP.java                    plugin: commands, kingdoms, bosses, portals, pack host
  SchematicPaster.java                  Sponge schematic reader and sliced paster
src/main/resources/
  config.yml, plugin.yml
  schematics/end.schem, overworld.schem bundled kingdom maps
_tools/                                 helper scripts used while developing/testing
```
