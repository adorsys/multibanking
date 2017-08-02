import { BrowserModule } from '@angular/platform-browser';
import { ErrorHandler, NgModule } from '@angular/core';
import { IonicApp, IonicErrorHandler, IonicModule } from 'ionic-angular';
import { SplashScreen } from '@ionic-native/splash-screen';
import { StatusBar } from '@ionic-native/status-bar';
import { HttpModule } from '@angular/http';
import { AutoCompleteModule } from 'ionic2-auto-complete';

import { MyApp } from './app.component';

import { BankAccessListPage } from '../pages/bankaccess/bankAccessList';
import { BankAccessCreatePage } from '../pages/bankaccess/bankAccessCreate';
import { BankAccountListPage } from '../pages/bankaccount/bankaccountList';
import { BookingListPage } from '../pages/booking/bookingList';
import { AnalyticsPage } from "../pages/analytics/analytics";
import { AnalyticsService } from "../services/analyticsService";
import { BankAccessService } from '../services/bankAccessService';
import { BankAccountService } from '../services/bankAccountService';
import { BookingService } from '../services/bookingService';
import { KeycloakService } from '../auth/keycloak.service';
import { KEYCLOAK_HTTP_PROVIDER } from '../auth/keycloak.http';
import { BankAccessUpdatePage } from "../pages/bankaccess/bankAccessUpdate";
import { BankAutoCompleteService } from "../services/bankAutoCompleteService";

@NgModule({
  declarations: [
    MyApp,
    BankAccessListPage,
    BankAccessCreatePage,
    BankAccessUpdatePage,
    BankAccountListPage,
    BookingListPage,
    AnalyticsPage
  ],
  imports: [
    BrowserModule,
    AutoCompleteModule,
    IonicModule.forRoot(MyApp),
    HttpModule
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    MyApp,
    BankAccessListPage,
    BankAccessCreatePage,
    BankAccessUpdatePage,
    BankAccountListPage,
    BookingListPage,
    AnalyticsPage
  ],
  providers: [
    StatusBar,
    SplashScreen,
    BankAccessService,
    BankAccountService,
    BookingService,
    AnalyticsService,
    KeycloakService,
    BankAutoCompleteService,
    KEYCLOAK_HTTP_PROVIDER,
    { provide: ErrorHandler, useClass: IonicErrorHandler }
  ]
})
export class AppModule {
}
