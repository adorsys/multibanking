import { NgModule, LOCALE_ID } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouteReuseStrategy } from '@angular/router';

import { IonicModule, IonicRouteStrategy } from '@ionic/angular';
import { SplashScreen } from '@ionic-native/splash-screen/ngx';
import { StatusBar } from '@ionic-native/status-bar/ngx';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HttpModule } from '@angular/http';
import { HttpClientModule } from '@angular/common/http';
import { IonicStorageModule } from '@ionic/storage';
import { AndroidFingerprintAuth } from '@ionic-native/android-fingerprint-auth/ngx';
import localeDe from '@angular/common/locales/de';
import { registerLocaleData } from '@angular/common';
import { KEYCLOAK_HTTP_INTERCEPTOR } from './services/auth/keycloak.httpinterceptor';
import { KEYCLOAK_HTTP_PROVIDER } from './services/auth/keycloak.http';

registerLocaleData(localeDe);

@NgModule({
  declarations: [AppComponent],
  entryComponents: [],
  imports: [
    BrowserModule,
    HttpModule,
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
    { provide: LOCALE_ID, useValue: 'de' },
    { provide: RouteReuseStrategy, useClass: IonicRouteStrategy }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
