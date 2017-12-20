import { Component } from '@angular/core';
import { NavController } from 'ionic-angular';
import { BankAccountListPage } from "../bankaccount/bankaccountList";
import { BankAccessCreatePage } from "./bankAccessCreate";
import { BankAccessService } from "../../services/bankAccessService";
import { BankAccessUpdatePage } from "./bankAccessUpdate";
import { BankAccess } from "../../api/BankAccess";
import { KeycloakService } from '../../auth/keycloak.service';
import { RulesTabsPage } from '../rules-tabs/rules-tabs';

@Component({
  selector: 'page-bankaccessList',
  templateUrl: 'bankAccessList.html'
})
export class BankAccessListPage {

  bankaccesses: Array<BankAccess>;
  rulesAdmin: boolean;

  constructor(public navCtrl: NavController,
    private bankAccessService: BankAccessService,
    private keycloakService: KeycloakService) {
  }

  ngOnInit() {
    this.rulesAdmin = this.keycloakService.getRoles().filter(role => role == 'rules_admin').length > 0;
  
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
    this.navCtrl.push(BankAccessCreatePage, { parent: this });
  }

  bankAccessesChanged() {
    this.bankAccessService.getBankAccesses().subscribe(response => {
      this.bankaccesses = response;
    });
  }

  editBankAccess($event, bankAccess) {
    $event.stopPropagation();
    this.navCtrl.push(BankAccessUpdatePage, { bankAccess: bankAccess, parent: this });
  }

  deleteBankAccess($event, bankAccess) {
    $event.stopPropagation();
    this.bankAccessService.deleteBankAccess(bankAccess.id).subscribe(response => {
      this.bankAccessesChanged();
    });
  }

  editRules() {
    this.navCtrl.push(RulesTabsPage);
  }

}
