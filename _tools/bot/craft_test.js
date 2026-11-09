const mineflayer = require('mineflayer');
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
const bot = mineflayer.createBot({ host: '127.0.0.1', port: 25565, username: 'Bot', auth: 'offline', version: '1.21.11' });

async function placeOne(win, name, target) {
  const src = win.slots.findIndex((s, i) => s && s.name === name && i >= 10);
  if (src < 0) throw new Error(`missing ${name}`);
  console.log('PICK', name, 'from', src, '->', target);
  await bot.clickWindow(src, 0, 0);
  await sleep(250);
  await bot.clickWindow(target, 1, 0);
  await sleep(250);
  await bot.clickWindow(src, 0, 0);
  await sleep(250);
  console.log('SLOTS_NOW', win.slots.map((s, i) => (s ? `${i}:${s.name}x${s.count}` : null)).filter(Boolean).join(' '));
}

bot.once('spawn', async () => {
  try {
    console.log('SPAWN', JSON.stringify(bot.entity.position));
    await sleep(2500);
    bot.chat('/forceload add -1 -1 3 3');
    await sleep(1500);
    bot.chat('/tp Bot 0 6 12');
    await sleep(1200);
    bot.chat('/clear Bot');
    bot.chat('/setblock 2 5 14 minecraft:crafting_table');
    bot.chat('/give Bot minecraft:ominous_bottle 2');
    bot.chat('/give Bot minecraft:purpur_block 8');
    bot.chat('/give Bot minecraft:netherite_ingot 1');
    bot.chat('/give Bot minecraft:nether_star 1');
    await sleep(2500);

    // OGO / GNG / GSG with O = ominous bottle, G = purpur block, N = netherite, S = nether star
    const table = bot.blockAt(new (require('vec3').Vec3)(2, 5, 14));
    console.log('TABLE_BLOCK', table && table.name);
    if (!table || table.name !== 'crafting_table') { console.log('NO_TABLE'); bot.quit(); return; }
    const win = await bot.openBlock(table);
    await sleep(800);
    console.log('WINDOW_SLOTS', win.slots.map((s, i) => (s ? `${i}:${s.name}x${s.count}` : null)).filter(Boolean).join(' '));
    console.log('INV_START', win.inventoryStart, 'INV_END', win.inventoryEnd);
    const pattern = [
      ['ominous_bottle', 'purpur_block', 'ominous_bottle'],
      ['purpur_block', 'netherite_ingot', 'purpur_block'],
      ['purpur_block', 'nether_star', 'purpur_block']
    ];
    for (let row = 0; row < 3; row++) {
      for (let col = 0; col < 3; col++) {
        await placeOne(win, pattern[row][col], row * 3 + col + 1);
      }
    }
    await sleep(700);
    const out = win.slots[0];
    console.log('OUTPUT_JSON', JSON.stringify(out));
    if (out) {
      await bot.clickWindow(0, 0, 1);
      await sleep(900);
      const inv = {};
      for (const i of bot.inventory.items()) inv[i.name] = (inv[i.name] || 0) + i.count;
      console.log('INV_AFTER', JSON.stringify(inv));
      const keys = bot.inventory.items().filter((i) => i.name === 'netherite_ingot');
      console.log('NETHERITE_STACKS', keys.map((k) => JSON.stringify({ count: k.count, nbt: k.nbt, components: k.components })).join(' | '));
    }
    await win.close();
  } catch (err) {
    console.log('ERROR', err.stack);
  }
  bot.quit();
});

bot.on('message', (msg) => console.log('CHAT', msg.toString()));
bot.on('error', (err) => console.log('ERR', err.message));
bot.on('end', (reason) => console.log('END', reason));
