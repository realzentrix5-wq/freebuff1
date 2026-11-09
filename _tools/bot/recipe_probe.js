const mineflayer = require('mineflayer');
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
const bot = mineflayer.createBot({ host: '127.0.0.1', port: 25565, username: 'Bot', auth: 'offline', version: '1.21.11' });

bot.once('spawn', async () => {
  console.log('SPAWN', JSON.stringify(bot.entity.position));
  await sleep(2500);
  bot.chat('/tp Bot 0 6 12');
  await sleep(1200);
  bot.chat('/clear Bot');
  bot.chat('/setblock 2 6 14 minecraft:crafting_table');
  bot.chat('/give Bot minecraft:ominous_bottle 4');
  bot.chat('/give Bot minecraft:purpur_block 8');
  bot.chat('/give Bot minecraft:netherite_ingot 4');
  bot.chat('/give Bot minecraft:nether_star 2');
  await sleep(2500);
  bot.chat('/recipe give Bot dimensionwars:portal_key_end');
  await sleep(800);

  const netherite = bot.registry.itemsByName.netherite_ingot.id;
  const ominous = bot.registry.itemsByName.ominous_bottle.id;
  const three = bot.recipesFor(netherite, null, 3, true);
  console.log('RECIPES_OUT_3', three.length);
  for (const r of three) {
    console.log('JSON', JSON.stringify(r, (k, v) => (k === 'delta' ? undefined : v)).slice(0, 900));
  }
  const withOminous = bot.recipesAll(ominous, null, false);
  console.log('OMINOUS_AS_INGREDIENT', withOminous.length);
  for (const r of withOminous) {
    console.log('OJSON', JSON.stringify({ id: r.id, result: r.result, shape: r.inShape }).slice(0, 600));
  }
  const table = bot.blockAt(new (require('vec3').Vec3)(2, 6, 14));
  console.log('TABLE', table && table.name);
  if (three.length && table && table.name === 'crafting_table') {
    try {
      await bot.craft(three[0], 1, table);
      await sleep(1200);
      const inv = {};
      for (const i of bot.inventory.items()) inv[i.name] = (inv[i.name] || 0) + i.count;
      console.log('INV_AFTER', JSON.stringify(inv));
      for (const i of bot.inventory.items()) {
        if (i.nbt && i.nbt.value && i.nbt.value.CustomModelData) console.log('KEY', i.name, i.count, 'cmd=', i.nbt.value.CustomModelData.value);
      }
    } catch (e) {
      console.log('CRAFT_ERROR', e.message);
    }
  }
  bot.quit();
});

bot.on('message', (msg) => console.log('CHAT', msg.toString()));
bot.on('error', (err) => console.log('ERR', err.message));
bot.on('end', (reason) => console.log('END', reason));
