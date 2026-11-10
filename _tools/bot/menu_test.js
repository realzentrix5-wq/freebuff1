const mineflayer = require('mineflayer');

const bot = mineflayer.createBot({
  host: '127.0.0.1',
  port: 25565,
  username: process.env.BOT_NAME || 'MenuBot',
  auth: 'offline',
  version: '1.21.11'
});

// 1.21 network chat components arrive as nested {type, value} compounds.
function text(node) {
  if (node == null) return '';
  if (typeof node === 'string') return node;
  if (Array.isArray(node)) return node.map(text).join('');
  if (typeof node !== 'object') return '';
  if (node.type === 'string' && typeof node.value === 'string') return node.value;
  if (typeof node.text === 'string') return node.text + text(node.extra);
  if (node.type === 'compound') return text(node.value);
  if (node.type === 'list') return text(node.value && node.value.value);
  return text(node.text) + text(node.extra);
}

function describe(item) {
  if (!item) return '(empty)';
  const name = text(item.customName) || text(item.displayName) || item.name;
  const lore = (item.customLore || []).map(text).filter(Boolean);
  return { name, lore };
}

bot.on('spawn', () => {
  console.log('SPAWN', JSON.stringify(bot.entity.position));
  setTimeout(() => bot.chat('/dw menu'), 3000);
});

bot.on('windowOpen', (window) => {
  console.log('WINDOW', JSON.stringify(window.title));
  setTimeout(() => {
    for (const [slot, item] of Object.entries(window.slots)) {
      if (item) console.log('SLOT', slot, JSON.stringify(describe(item)));
    }
    setTimeout(() => bot.quit(), 500);
  }, 700);
});

bot.on('message', (msg) => console.log('CHAT', msg.toString()));
bot.on('error', (err) => console.log('ERR', err.message));
bot.on('kicked', (reason) => console.log('KICKED', JSON.stringify(reason)));
bot.on('end', (reason) => console.log('END', reason));

setTimeout(() => { console.log('TIMEOUT'); bot.quit(); }, 25000);
