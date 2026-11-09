Eclipse SMP resource pack (pack_format 75, supported 46-99, Minecraft 1.21.11+)

The plugin builds this folder into a zip, hashes it and serves it itself over HTTP,
so you do not need to host anything. Set resource-pack.public-url in config.yml if
players connect from outside the machine running the server.

Model overrides (CustomModelData lives on the item the plugin actually uses)

  Paper            1001  Nether menu icon         assets/dimensionwars/models/item/nether_icon.json
                   1002  End menu icon            assets/dimensionwars/models/item/end_icon.json
                   1003  Overworld menu icon      assets/dimensionwars/models/item/overworld_icon.json

                   2001  Piglin Warlord - easy    assets/dimensionwars/models/item/boss_nether.json
                   2002  Void Reaper - easy       assets/dimensionwars/models/item/boss_end.json
                   2003  Deep Guardian - easy     assets/dimensionwars/models/item/boss_overworld.json
                   2011  Piglin Warlord - medium  assets/dimensionwars/models/item/boss_nether_medium.json
                   2012  Void Reaper - medium     assets/dimensionwars/models/item/boss_end_medium.json
                   2013  Deep Guardian - medium   assets/dimensionwars/models/item/boss_overworld_medium.json
                   2021  Piglin Warlord - hard    assets/dimensionwars/models/item/boss_nether_hard.json
                   2022  Void Reaper - hard       assets/dimensionwars/models/item/boss_end_hard.json
                   2023  Deep Guardian - hard     assets/dimensionwars/models/item/boss_overworld_hard.json

  Netherite Ingot  3001  Nether portal key        assets/dimensionwars/models/item/key_nether.json
                   3002  End portal key           assets/dimensionwars/models/item/key_end.json
                   3003  Overworld portal key     assets/dimensionwars/models/item/key_overworld.json

  Netherite Sword  4001  Netherite Warblade      assets/dimensionwars/models/item/sword_netherite.json

Boss phases swap the model as health drops: 2001-2003 at 100-66%, 2011-2013 at
66-33%, 2021-2023 below 33%. Each tier has its own texture, so a phase change is
visible at a glance.

Textures
  assets/dimensionwars/textures/entity/*.png  boss models, 128x128, three tiers each
  assets/dimensionwars/textures/item/*.png    menu icons (128x128), keys and sword (16x16)

Without the pack the plugin still plays; bosses show their vanilla mob unless you set
bosses.hide-vanilla-model: false in config.yml.
