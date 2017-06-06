import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app.module';
import { AppConfig } from './app.config';
import { KeycloakService } from '../auth/keycloak.service';

if (AppConfig.production) {
  enableProdMode();
}

KeycloakService.init()
  .then(() => platformBrowserDynamic().bootstrapModule(AppModule))
  .catch(e => window.location.reload());

