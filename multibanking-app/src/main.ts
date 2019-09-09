import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { environment } from './environments/environment';
import { KeycloakService } from './app/services/auth/keycloak.service';

if (environment.production) {
  enableProdMode();
}

if (!environment.isApp) {
  KeycloakService.init({ adapter: 'default' })
    .then(() => bootstrap())
    .catch(err => console.error(err));
} else {
  bootstrap();
}
function bootstrap() {
  platformBrowserDynamic().bootstrapModule(AppModule)
    .catch(err => console.log(err));
}
