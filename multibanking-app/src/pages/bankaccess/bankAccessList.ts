import {Component} from '@angular/core';
import {NavController} from 'ionic-angular';
import {NavParams} from 'ionic-angular';
import {BankAccountService} from "../../services/BankAccountService";
import {BankAccountListPage} from "../bankaccount/bankaccountList";
import {BankAccessCreatePage} from "./bankAccessCreate";
import {BankAccessService} from "../../services/BankAccessService";

@Component({
  selector: 'page-bankaccessList',
  templateUrl: 'bankAccessList.html'
})
export class BankAccessListPage {

  userId;
  bankaccesses;

  constructor(public navCtrl: NavController, private navparams: NavParams, private bankAccountService: BankAccountService, private bankAccessService: BankAccessService) {
    this.userId = navparams.data.userId;
      this.bankaccesses = navparams.data.bankAccesses;
  }

  itemSelected(bankAccess) {
    this.bankAccountService.getBankAccounts(this.userId, bankAccess.id).subscribe(response => {
      this.navCtrl.push(BankAccountListPage, {userId: this.userId, bankAccessId: bankAccess.id, bankAccounts: response});
    })
  }

  createBankAccess() {
    this.navCtrl.push(BankAccessCreatePage, {userId: this.userId, parent: this});
  }

  bankAccessCreated() {
      this.bankAccessService.getBankAccesses(this.userId).subscribe(response => {
        this.bankaccesses = response;
      });
  }

}
