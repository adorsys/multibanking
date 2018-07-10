import { Component } from "@angular/core";
import { NavController, NavParams } from "ionic-angular";
import { BankAccountService } from "../../services/bankAccount.service";
import { BookingTabsPage } from "../booking-tabs/booking-tabs.component";
import { BankAccessService } from "../../services/bankAccess.service";
import { BankAccess, BankAccount } from "../../model/multibanking/models";


@Component({
  selector: 'page-bankaccountList',
  templateUrl: 'bankaccountList.component.html'
})
export class BankAccountListPage {

  bankAccess: BankAccess;
  bankAccounts: Array<BankAccount>;

  constructor(public navCtrl: NavController,
    public navparams: NavParams,
    public bankAccountService: BankAccountService,
    public bankAccessService: BankAccessService) {

    this.bankAccess = navparams.data.bankAccess;
  }

  ngOnInit() {
    this.loadBankAccounts();

    this.bankAccessService.bankAccessDeletedObservable.subscribe(changed => {
      this.bankAccess = undefined;
    });
    this.bankAccountService.bookingsChangedObservable.subscribe(changed => {
      if (this.bankAccess) {
        this.loadBankAccounts();
      }
    });
  }

  loadBankAccounts() {  
    this.bankAccountService.getBankAccounts(this.bankAccess.id).subscribe(response => {
      this.bankAccounts = response;
    });
  }

  itemSelected(bankAccount) {
    this.navCtrl.push(BookingTabsPage, {
      bankAccess: this.bankAccess,
      bankAccount: bankAccount,
    });
  }
}
