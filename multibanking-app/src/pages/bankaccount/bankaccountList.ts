import { Component } from "@angular/core";
import { NavController, NavParams } from "ionic-angular";
import { BankAccountService } from "../../services/bankAccountService";
import { BookingListPage } from "../booking/bookingList";
import { BankAccess } from "../../api/BankAccess";
import { BankAccount } from "../../api/BankAccount";

@Component({
  selector: 'page-bankaccountList',
  templateUrl: 'bankaccountList.html'
})
export class BankAccountListPage {

  bankAccess: BankAccess;
  bankAccounts: Array<BankAccount>;

  constructor(public navCtrl: NavController,
    private navparams: NavParams,
    private bankAccountService: BankAccountService) {

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
    this.navCtrl.push(BookingListPage, {
      bankAccess: this.bankAccess,
      bankAccountId: bankAccount.id,
    })
  }
}
