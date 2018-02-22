import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app.module';
import { AppConfig } from './app.config';

if (AppConfig.production) {
  enableProdMode();
}

platformBrowserDynamic().bootstrapModule(AppModule);
