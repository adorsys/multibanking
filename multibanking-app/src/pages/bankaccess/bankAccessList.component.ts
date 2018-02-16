import { Component } from '@angular/core';
import { NavController } from 'ionic-angular';
import { BankAccountListPage } from "../bankaccount/bankaccountList.component";
import { BankAccessCreatePage } from "./bankAccessCreate.component";
import { BankAccessService } from "../../services/bankAccess.service";
import { BankAccessUpdatePage } from "./bankAccessUpdate.component";
import { BankAccess } from "../../api/BankAccess";
import { KeycloakService } from '../../auth/keycloak.service';

@Component({
  selector: 'page-bankaccessList',
  templateUrl: 'bankAccessList.component.html'
})
export class BankAccessListPage {

  bankaccesses: Array<BankAccess>;

  constructor(public navCtrl: NavController,
    private bankAccessService: BankAccessService,
    private keycloakService: KeycloakService) {
  }

  ngOnInit() {
    console.log("init Keycloak");
    KeycloakService.init({ onLoad: 'check-sso', checkLoginIframe: false, adapter: 'default' }).then(() => {
      console.log("Keycloak initialized, authenticated: " + this.keycloakService.authenticated());
      if (this.keycloakService.authenticated()) {
        this.bankAccessService.getBankAccesses().subscribe(
          response => {
            this.bankaccesses = response
          });
      } else {
        this.keycloakService.login();
      }
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

}
