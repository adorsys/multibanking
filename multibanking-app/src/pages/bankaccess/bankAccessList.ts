import {Component} from '@angular/core';
import {NavController} from 'ionic-angular';
import {BankAccountService} from "../../services/bankAccountService";
import {BankAccountListPage} from "../bankaccount/bankaccountList";
import {BankAccessCreatePage} from "./bankAccessCreate";
import {BankAccessService} from "../../services/bankAccessService";
import {KeycloakService} from "../../auth/keycloak.service";
import {BankAccessUpdatePage} from "./bankAccessUpdate";

@Component({
  selector: 'page-bankaccessList',
  templateUrl: 'bankAccessList.html'
})
export class BankAccessListPage {

  userId;
  bankaccesses;

  constructor(public navCtrl: NavController, private bankAccountService: BankAccountService, private bankAccessService: BankAccessService,
              private keycloakService: KeycloakService) {

      this.userId = keycloakService.getUsername();
      this.bankAccessService.getBankAccesses(this.userId).subscribe(
        response => {
          this.bankaccesses = response
        });
  }

  itemSelected(bankAccess) {
    this.navCtrl.push(BankAccountListPage, {
      userId: this.userId,
      bankAccess: bankAccess,
    });
  }

  createBankAccess() {
    this.navCtrl.push(BankAccessCreatePage, {userId: this.userId, parent: this});
  }

  bankAccessesChanged() {
    this.bankAccessService.getBankAccesses(this.userId).subscribe(response => {
      this.bankaccesses = response;
    });
  }

  editBankAccess($event, bankAccess) {
    $event.stopPropagation();
    this.navCtrl.push(BankAccessUpdatePage, {bankAccess: bankAccess, parent: this});
  }

  deleteBankAccess($event, bankAccess) {
    $event.stopPropagation();
    this.bankAccessService.deleteBankAccess(this.userId, bankAccess.id).subscribe(response => {
      this.bankAccessesChanged();
    });
  }

}
