const mineflayer = require('mineflayer');
const bot = mineflayer.createBot({host:'127.0.0.1',port:25565,username:'PackBot',auth:'offline',version:'1.21.11'});
bot.on('resourcePack', (url, hash) => console.log('PACK', url, hash));
bot._client.on('resource_pack_send', (p) => console.log('RAW_PACK', JSON.stringify(p).slice(0, 300)));
bot.on('message', (m) => console.log('CHAT', m.toString()));
bot.on('spawn', () => setTimeout(() => bot.quit(), 8000));
setTimeout(() => bot.quit(), 20000);
