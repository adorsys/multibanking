import {NgModule, ErrorHandler} from '@angular/core';
import {IonicApp, IonicModule, IonicErrorHandler} from 'ionic-angular';
import {AppConfig} from './app.config';
import {UserService} from '../services/UserService';
import {BankAccessService} from '../services/BankAccessService';
import {BankAccountService} from '../services/BankAccountService';
import {BookingService} from '../services/BookingService';
import {MyApp} from './app.component';
import {LoginPage} from '../pages/login/login';
import {RegisterPage} from '../pages/register/register';
import {BankAccessListPage} from '../pages/bankaccess/bankAccessList';
import {BankAccessCreatePage} from '../pages/bankaccess/bankAccessCreate';
import {BankAccountListPage} from '../pages/bankaccount/bankaccountList';
import {BookingListPage} from '../pages/booking/bookingList';

@NgModule({
  declarations: [
    MyApp,
    LoginPage,
    RegisterPage,
    BankAccessListPage,
    BankAccessCreatePage,
    BankAccountListPage,
    BookingListPage
  ],
  imports: [
    IonicModule.forRoot(MyApp)
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    MyApp,
    LoginPage,
    RegisterPage,
    BankAccessListPage,
    BankAccessCreatePage,
    BankAccountListPage,
    BookingListPage
  ],
  providers: [{provide: ErrorHandler, useClass: IonicErrorHandler},
    UserService,
    BankAccessService,
    BankAccountService,
    BookingService,
    AppConfig]
})
export class AppModule {
}
