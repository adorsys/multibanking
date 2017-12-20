import { Component } from "@angular/core";
import { NavController, NavParams } from "ionic-angular";
import { BankAccountService } from "../../services/bankAccountService";
import { BankAccess } from "../../api/BankAccess";
import { BankAccount } from "../../api/BankAccount";
import { BookingTabsPage } from "../booking-tabs/booking-tabs";


@Component({
  selector: 'page-bankaccountList',
  templateUrl: 'bankaccountList.html'
})
export class BankAccountListPage {

  bankAccess: BankAccess;
  bankAccounts: Array<BankAccount>;

  constructor(public navCtrl: NavController,
    public navparams: NavParams,
    public bankAccountService: BankAccountService) {

    this.bankAccess = navparams.data.bankAccess; 
  }

  ngOnInit() {
    this.loadBankAccounts();

    this.bankAccountService.bookingsChangedObservable.subscribe(changed => {
      this.loadBankAccounts();
    })
  }

  loadBankAccounts() {
    this.bankAccountService.getBankAccounts(this.bankAccess.id).subscribe(response => {
      this.bankAccounts = response;
    })
  }

  itemSelected(bankAccount) {
    this.navCtrl.push(BookingTabsPage, {
      bankAccess: this.bankAccess,
      bankAccount: bankAccount,
    })
  }
}
