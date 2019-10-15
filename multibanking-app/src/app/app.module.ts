import { environment } from 'src/environments/environment';
import { KeycloakService } from './services/auth/keycloak.service';
import { registerLocaleData } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import localeDe from '@angular/common/locales/de';
import { NgModule, LOCALE_ID, APP_INITIALIZER } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouteReuseStrategy } from '@angular/router';

import { IonicModule, IonicRouteStrategy } from '@ionic/angular';
import { IonicStorageModule } from '@ionic/storage';
import { AndroidFingerprintAuth } from '@ionic-native/android-fingerprint-auth/ngx';
import { SplashScreen } from '@ionic-native/splash-screen/ngx';
import { StatusBar } from '@ionic-native/status-bar/ngx';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { KEYCLOAK_HTTP_INTERCEPTOR } from './services/auth/keycloak.httpinterceptor';
import { KEYCLOAK_HTTP_PROVIDER } from './services/auth/keycloak.http';
import { SettingsHttpService } from './services/settings/settings.http.service';

registerLocaleData(localeDe);

export function appInitializer(settingsHttpService: SettingsHttpService, keycloakService: KeycloakService) {
  return () => settingsHttpService.initializeApp().then(async () => {
    if (!environment.isApp) {
      await keycloakService.init({
        initOptions: {
          adapter: 'default'
        }
      });
    }
  });
}

@NgModule({
  declarations: [AppComponent],
  entryComponents: [],
  imports: [
    BrowserModule,
    HttpClientModule,
    IonicModule.forRoot(),
    AppRoutingModule,
    IonicStorageModule.forRoot()
  ],
  providers: [
    StatusBar,
    SplashScreen,
    AndroidFingerprintAuth,
    KEYCLOAK_HTTP_PROVIDER,
    KEYCLOAK_HTTP_INTERCEPTOR,
    { provide: APP_INITIALIZER, useFactory: appInitializer, deps: [SettingsHttpService, KeycloakService], multi: true },
    { provide: LOCALE_ID, useValue: 'de' },
    { provide: RouteReuseStrategy, useClass: IonicRouteStrategy }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
