import {NgModule, ErrorHandler} from '@angular/core';
import {IonicApp, IonicModule, IonicErrorHandler} from 'ionic-angular';
import {BankAccessService} from '../services/bankAccessService';
import {BankAccountService} from '../services/bankAccountService';
import {BookingService} from '../services/bookingService';
import {MyApp} from './app.component';
import {BankAccessListPage} from '../pages/bankaccess/bankAccessList';
import {BankAccessCreatePage} from '../pages/bankaccess/bankAccessCreate';
import {BankAccountListPage} from '../pages/bankaccount/bankaccountList';
import {BookingListPage} from '../pages/booking/bookingList';
import {AnalyticsPage} from "../pages/analytics/analytics";
import {AnalyticsService} from "../services/analyticsService";
import {KeycloakService} from '../auth/keycloak.service';
import {KEYCLOAK_HTTP_PROVIDER} from '../auth/keycloak.http';

@NgModule({
  declarations: [
    MyApp,
    BankAccessListPage,
    BankAccessCreatePage,
    BankAccountListPage,
    BookingListPage,
    AnalyticsPage
  ],
  imports: [
    IonicModule.forRoot(MyApp)
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    MyApp,
    BankAccessListPage,
    BankAccessCreatePage,
    BankAccountListPage,
    BookingListPage,
    AnalyticsPage
  ],
  providers: [{provide: ErrorHandler, useClass: IonicErrorHandler},
    BankAccessService,
    BankAccountService,
    BookingService,
    AnalyticsService,
    KeycloakService,
    KEYCLOAK_HTTP_PROVIDER]
})
export class AppModule {
}
