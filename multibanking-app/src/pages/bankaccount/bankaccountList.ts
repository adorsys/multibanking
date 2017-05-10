import {Component} from "@angular/core";
import {NavController, NavParams} from "ionic-angular";
import {BankAccountService} from "../../services/bankAccountService";
import {AnalyticsPage} from "../analytics/analytics";

@Component({
  selector: 'page-bankaccountList',
  templateUrl: 'bankaccountList.html'
})
export class BankAccountListPage {

  userId;
  bankAccessId;
  bankAccounts;

  constructor(public navCtrl: NavController,
              private navparams: NavParams,
              private bankAccountService: BankAccountService) {

    this.userId = navparams.data.userId;
    this.bankAccessId = navparams.data.bankAccessId;

    this.loadBankAccounts();

    bankAccountService.bookingsChangedObservable.subscribe(changed => {
      this.loadBankAccounts();
    })
  }

  loadBankAccounts() {
    this.bankAccountService.getBankAccounts(this.userId, this.bankAccessId).subscribe(response => {
      this.bankAccounts = response;
    })
  }

  itemSelected(bankAccount) {
    this.navCtrl.push(AnalyticsPage, {
      userId: this.userId,
      bankAccessId: this.bankAccessId,
      bankAccountId: bankAccount.id,
    })

  }
}
