var fs = require('fs');

function readWriteSync() {
  let env = process.env.ENV || 'dev';

  var data = fs.readFileSync(`src/env/env.${env}.ts`, 'utf-8');
  fs.writeFileSync('src/env/env.ts', data, 'utf-8');
  
  consoleOut();
}

function consoleOut() {
  let env = process.env.ENV || 'dev';
  let filePath = `src/env/env.${env}.ts`;
  
  console.log("============ PREPARE ENV FILE =============");
  console.log("                                           ");
  console.log("                 "+env+"                   ");
  console.log("              "+filePath+"                 ");
  console.log("                                           ");
  console.log("===========================================");
}

readWriteSync();