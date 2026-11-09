const mineflayer = require('mineflayer');

const bot = mineflayer.createBot({
  host: '127.0.0.1',
  port: 25565,
  username: process.env.BOT_NAME || 'Bot',
  auth: 'offline',
  version: '1.21.11'
});

bot.on('spawn', () => {
  console.log('SPAWN', JSON.stringify(bot.entity.position));
  setTimeout(() => {
    console.log('SEND /dw spawn boss end');
    bot.chat('/dw spawn boss end');
  }, 9000);
  setTimeout(() => {
    console.log('SEND /dw boss reset end');
    bot.chat('/dw boss reset end');
  }, 16000);
  setTimeout(() => {
    console.log('SEND /dw pack');
    bot.chat('/dw pack');
  }, 21000);
  setTimeout(() => {
    console.log('QUIT');
    bot.quit();
  }, 27000);
});

bot.on('message', (msg) => console.log('CHAT', msg.toString()));
bot.on('error', (err) => console.log('ERR', err.message));
bot.on('kicked', (reason) => console.log('KICKED', JSON.stringify(reason)));
bot.on('end', (reason) => console.log('END', reason));
