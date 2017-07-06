import {Component} from '@angular/core';
import {NavController} from 'ionic-angular';
import {BankAccountService} from "../../services/bankAccountService";
import {BankAccountListPage} from "../bankaccount/bankaccountList";
import {BankAccessCreatePage} from "./bankAccessCreate";
import {BankAccessService} from "../../services/bankAccessService";
import {BankAccessUpdatePage} from "./bankAccessUpdate";

@Component({
  selector: 'page-bankaccessList',
  templateUrl: 'bankAccessList.html'
})
export class BankAccessListPage {

  bankaccesses;

  constructor(public navCtrl: NavController, private bankAccountService: BankAccountService, private bankAccessService: BankAccessService) {

      this.bankAccessService.getBankAccesses().subscribe(
        response => {
          this.bankaccesses = response
        });
  }

  itemSelected(bankAccess) {
    this.navCtrl.push(BankAccountListPage, {
      bankAccess: bankAccess,
    });
  }

  createBankAccess() {
    this.navCtrl.push(BankAccessCreatePage, {parent: this});
  }

  bankAccessesChanged() {
    this.bankAccessService.getBankAccesses().subscribe(response => {
      this.bankaccesses = response;
    });
  }

  editBankAccess($event, bankAccess) {
    $event.stopPropagation();
    this.navCtrl.push(BankAccessUpdatePage, {bankAccess: bankAccess, parent: this});
  }

  deleteBankAccess($event, bankAccess) {
    $event.stopPropagation();
    this.bankAccessService.deleteBankAccess(bankAccess.id).subscribe(response => {
      this.bankAccessesChanged();
    });
  }

}
