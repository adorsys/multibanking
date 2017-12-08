import { AutoCompleteModule } from 'ionic2-auto-complete';
import { BrowserModule } from '@angular/platform-browser';
import { ErrorHandler, NgModule } from '@angular/core';
import { HttpModule } from '@angular/http';
import { IonicApp, IonicErrorHandler, IonicModule } from 'ionic-angular';
import { SplashScreen } from '@ionic-native/splash-screen';
import { StatusBar } from '@ionic-native/status-bar';

import { AnalyticsPage } from "../pages/analytics/analytics";
import { AnalyticsService } from "../services/analyticsService";
import { BankAccessCreatePage } from '../pages/bankaccess/bankAccessCreate';
import { BankAccessListPage } from '../pages/bankaccess/bankAccessList';
import { BankAccessService } from '../services/bankAccessService';
import { BankAccessUpdatePage } from "../pages/bankaccess/bankAccessUpdate";
import { BankAccountListPage } from '../pages/bankaccount/bankaccountList';
import { BankAccountService } from '../services/bankAccountService';
import { BankAutoCompleteService } from "../services/bankAutoCompleteService";
import { BookingGroupPage } from "../pages/analytics/bookingGroup";
import { BookingListPage } from '../pages/booking/bookingList';
import { BookingService } from '../services/bookingService';
import { ContractService } from "../pages/contracts/contract.service";
import { ContractsComponent } from "../pages/contracts/contracts.component";
import { CyclePipe } from '../pages/contracts/cycle.pipe';
import { KEYCLOAK_HTTP_PROVIDER } from '../auth/keycloak.http';
import { KeycloakService } from '../auth/keycloak.service';
import { LogoService } from '../services/LogoService';
import { MyApp } from './app.component';
import { TabsPage } from '../pages/tabs/tabs';
import { PaymentCreatePage } from '../pages/payment/paymentCreate';
import { PaymentService } from '../services/PaymentService';
import { BookingDetailPage } from '../pages/booking-detail/bookingDetail';
import { BookingEditPage } from '../pages/booking-edit/bookingEdit';

@NgModule({
  declarations: [
    AnalyticsPage,
    BankAccessCreatePage,
    BankAccessListPage,
    BankAccessUpdatePage,
    BankAccountListPage,
    BookingGroupPage,
    BookingListPage,
    BookingDetailPage,
    BookingEditPage,
    PaymentCreatePage,
    ContractsComponent,
    TabsPage,
    CyclePipe,
    MyApp,
  ],
  imports: [
    AutoCompleteModule,
    BrowserModule,
    HttpModule,
    IonicModule.forRoot(MyApp),
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    AnalyticsPage,
    BankAccessCreatePage,
    BankAccessListPage,
    BankAccessUpdatePage,
    BankAccountListPage,
    BookingGroupPage,
    BookingListPage,
    BookingDetailPage,
    BookingEditPage,
    PaymentCreatePage,
    ContractsComponent,
    TabsPage,
    MyApp,
  ],
  providers: [
    AnalyticsService,
    BankAccessService,
    BankAccountService,
    BankAutoCompleteService,
    BookingService,
    ContractService,
    KEYCLOAK_HTTP_PROVIDER,
    KeycloakService,
    LogoService,
    PaymentService,
    SplashScreen,
    StatusBar,
    { provide: ErrorHandler, useClass: IonicErrorHandler },
  ]
})
export class AppModule {}
