import {Component} from "@angular/core";
import {NavController, NavParams} from "ionic-angular";
import {BankAccountService} from "../../services/bankAccountService";
import {BookingListPage} from "../booking/bookingList";

@Component({
  selector: 'page-bankaccountList',
  templateUrl: 'bankaccountList.html'
})
export class BankAccountListPage {

  bankAccess;
  bankAccounts;

  constructor(public navCtrl: NavController,
              private navparams: NavParams,
              private bankAccountService: BankAccountService) {

    this.bankAccess = navparams.data.bankAccess;

    this.loadBankAccounts();

    bankAccountService.bookingsChangedObservable.subscribe(changed => {
      this.loadBankAccounts();
    })
  }

  loadBankAccounts() {
    this.bankAccountService.getBankAccounts(this.bankAccess.id).subscribe(response => {
      this.bankAccounts = response;
    })
  }

  itemSelected(bankAccount) {
    this.navCtrl.push(BookingListPage, {
      bankAccess: this.bankAccess,
      bankAccountId: bankAccount.id,
    })
  }
}
