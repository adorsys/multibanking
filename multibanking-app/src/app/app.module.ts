import { AutoCompleteModule } from 'ionic2-auto-complete';
import { BrowserModule } from '@angular/platform-browser';
import { ErrorHandler, NgModule } from '@angular/core';
import { IonicApp, IonicErrorHandler, IonicModule } from 'ionic-angular';
import { SplashScreen } from '@ionic-native/splash-screen';
import { StatusBar } from '@ionic-native/status-bar';
import { MomentModule } from 'ngx-moment';
import { ChartsModule } from 'ng2-charts';
import {CurrencyPipe} from '@angular/common'

import { AnalyticsPage } from "../pages/analytics/analytics.component";
import { AnalyticsService } from "../services/analytics.service";
import { BankAccessCreatePage } from '../pages/bankaccess/bankAccessCreate.component';
import { BankAccessListPage } from '../pages/bankaccess/bankAccessList.component';
import { BankAccessService } from '../services/bankAccess.service';
import { BankAccessUpdatePage } from "../pages/bankaccess/bankAccessUpdate.component";
import { BankAccountListPage } from '../pages/bankaccount/bankaccountList.component';
import { BankAccountService } from '../services/bankAccount.service';
import { BankAutoCompleteService } from "../services/bankAutoComplete.service";
import { BookingGroupPage } from "../pages/analytics/bookingGroup.component";
import { BookingListPage } from '../pages/booking/bookingList.component';
import { BookingService } from '../services/booking.service';
import { ContractsComponent } from "../pages/contracts/contracts.component";
import { CyclePipe } from '../pages/contracts/cycle.pipe';
import { KEYCLOAK_HTTP_PROVIDER } from '../auth/keycloak.http';
import { KeycloakService } from '../auth/keycloak.service';
import { ImageService } from '../services/image.service';
import { MyApp } from './app.component';
import { PaymentCreatePage } from '../pages/payment/paymentCreate.component';
import { PaymentService } from '../services/payment.service';
import { BookingDetailPage } from '../pages/booking-detail/bookingDetail.component';
import { RulesService } from '../services/rules.service';
import { RuleEditPage } from '../pages/rule-edit/ruleEdit.component';
import { BookingTabsPage } from '../pages/booking-tabs/booking-tabs.component';
import { RulesCustomPage } from '../pages/rules-custom/rulesCustom.component';
import { RulesStaticPage } from '../pages/rules-static/rulesStatic.component';
import { ListActionDirective } from '../directives/listAction.directive';
import { RulesCustomAutoCompleteService } from '../services/rulesCustomAutoComplete.service';
import { RulesStaticAutoCompleteService } from '../services/rulesStaticAutoComplete.service';
import { HttpClientModule } from '@angular/common/http';
import { ContractService } from '../services/contract.service';
import { BookingGroupDetailPage } from '../pages/analytics/bookingGroupDetail.component';
import { CategoriesPage } from '../pages/categories/categories.component';
import { ResourceTreeDirective } from '../directives/resourceTree.directive';
import { ConfigTabsPage } from '../pages/config-tabs/config-tabs.component';
import { ContractBlacklistPage } from '../pages/contract-blacklist/contract-blacklist.component';
import { BookingGroupsPage } from '../pages/booking-groups/booking-groups.component';
import { BookingImagesPage } from '../pages/booking-images/booking-images.component';
import { BanksPage } from '../pages/banks/banks.component';
import { BankService } from '../services/bank.service';
import { BookingAutoCompleteService } from '../services/bookingAutoComplete.service';


@NgModule({
  declarations: [
    ListActionDirective,
    ResourceTreeDirective,
    AnalyticsPage,
    BankAccessCreatePage,
    BankAccessListPage,
    BankAccessUpdatePage,
    BankAccountListPage,
    BookingGroupPage,
    BookingGroupDetailPage,
    BookingListPage,
    BookingDetailPage,
    RuleEditPage,
    PaymentCreatePage,
    RulesStaticPage,
    RulesCustomPage,
    ContractsComponent,
    BookingTabsPage,
    CategoriesPage,
    CyclePipe,
    ConfigTabsPage,
    ContractBlacklistPage,
    BookingGroupsPage,
    BookingImagesPage,
    BanksPage,
    MyApp,
  ],
  imports: [
    AutoCompleteModule,
    BrowserModule,
    HttpClientModule,
    MomentModule,
    ChartsModule,
    IonicModule.forRoot(MyApp),
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    ResourceTreeDirective,
    AnalyticsPage,
    BankAccessCreatePage,
    BankAccessListPage,
    BankAccessUpdatePage,
    BankAccountListPage,
    BookingGroupPage,
    BookingGroupDetailPage,
    BookingListPage,
    BookingDetailPage,
    RuleEditPage,
    PaymentCreatePage,
    RulesStaticPage,
    RulesCustomPage,
    ContractsComponent,
    BookingTabsPage,
    CategoriesPage,
    ConfigTabsPage,
    BookingGroupsPage,
    ContractBlacklistPage,
    BookingImagesPage,
    BanksPage,
    MyApp,
  ],
  providers: [
    AnalyticsService,
    RulesService,
    BankAccessService,
    BankAccountService,
    BankAutoCompleteService,
    RulesCustomAutoCompleteService,
    RulesStaticAutoCompleteService,
    BookingAutoCompleteService,
    BookingService,
    ContractService,
    KEYCLOAK_HTTP_PROVIDER,
    KeycloakService,
    ImageService,
    PaymentService,
    BankService,
    SplashScreen,
    StatusBar,
    CurrencyPipe,
    { provide: ErrorHandler, useClass: IonicErrorHandler },
  ]
})
export class AppModule { }
