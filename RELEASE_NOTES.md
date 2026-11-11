# Eclipse SMP v1.0.0

First release under the Eclipse SMP name (formerly DimensionWars). Everything is
generated from `config.yml` — no manual world building, no external web host.

## Files

| File | What it is |
| --- | --- |
| `target/EclipseSMP-1.0.0.jar` | The plugin. Drop it into `plugins/`. |
| `EclipseSMP-ResourcePack-1.0.0.zip` | The pack on its own, for manual installs. |
| `_server/plugins/EclipseSMP/resourcepack/EclipseSMP-ResourcePack.zip` | The copy the plugin writes at startup and serves. |

The jar already contains the pack: the plugin hashes it, serves it on port 8123
and sends the URL with the correct sha1, so players download it automatically.
Set `resource-pack.public-url` if players connect from outside the host.

## Three realms

- **Overworld** — The Deep Guardian. **Nether** — The Piglin Warlord.
  **End** — The Void Reaper.
- Each realm has a 400x400 kingdom with a plaza, throne, four towers, a cage and
  a key portal. The Overworld and End hearts are schematic maps (`end.schem`,
  `overworld.schem`); the Nether is procedural.
- Three tiers per boss (normal / medium / hard) with separate health, damage,
  armour and knockback settings.
- Bosses sleep in their cage and wake when an invader comes within
  `kingdoms.wake-radius` blocks, then calm down again once the area is empty.

## Items

- **Portal key** (netherite ingot, one per realm colour): shaped recipe of two
  Ominous Bottles, four ritual blocks, a netherite ingot and a nether star,
  yielding three keys.
- **Netherite warblade**: right-click sonic boom (40 hearts, 12s cooldown),
  left-click ability on a 15s cooldown.
- **Ritual catalysts** per realm, plus the guide book and the realm selector.

## Rules worth knowing

- All boss damage bypasses armour, enchantments and absorption.
- Player hearts depend on the realm's boss state: 10 by default, 5 once the
  boss is defeated, 8 during a ritual, 12 when blessed.
- The revival ritual takes 10 minutes and ends with a death attack against the
  opposing team: 100 hearts plus 300 durability off every invader's armour.

## Requirements

- Paper 1.21.11 (or a compatible 1.21.x build); the pack targets resource pack
  format 75.
- Java 21.

## Known limitations

- The custom boss figures are 14 units wide and 32 tall, so the inventory and
  hotbar icons read as a tall, narrow figure rather than filling the slot.
- Boss items are `paper` with `custom_model_data`; a stack without the component
  is plain paper. When handing them out by command, keep the component:
  `minecraft:paper[minecraft:custom_model_data={floats:[2001.0]}]`.

## Commands

See `README.md` for the full table. The command is `/esmp`, with the aliases
`/dw`, `/dimensionwars` and `/eclipse`.
