const { randomBytes, pbkdf2Sync } = require('crypto');
const password = 'delivery123';
const salt = randomBytes(16).toString('hex');
const hash = pbkdf2Sync(password, Buffer.from(salt, 'hex'), 120000, 64, 'sha512').toString('hex');
console.log(salt);
console.log(hash);
