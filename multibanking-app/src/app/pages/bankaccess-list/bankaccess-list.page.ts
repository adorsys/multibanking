import { Component, OnInit } from '@angular/core';
import { NavController } from '@ionic/angular';
import { KeycloakService } from 'src/app/services/auth/keycloak.service';
import { BankAccessService } from 'src/app/services/rest/bankAccess.service';
import { ResourceBankAccess } from 'src/multibanking-api/resourceBankAccess';

@Component({
  selector: 'app-bankaccess-list',
  templateUrl: './bankaccess-list.page.html',
  styleUrls: ['./bankaccess-list.page.scss'],
})
export class BankaccessListPage implements OnInit {

  admin: boolean;

  bankAccesses: ResourceBankAccess[];

  constructor(private bankAccessService: BankAccessService,
              private navCtrl: NavController,
              private keycloak: KeycloakService) { }

  ngOnInit() {
    this.admin = this.keycloak.getRoles().filter(role => role === 'rules_admin').length > 0;

    this.bankAccessService.getBankAccesses().subscribe((response) => {
      this.bankAccesses = response;
    });
  }

  createConnection() {
    this.navCtrl.navigateForward([`/create-consent`]);
  }

  adminArea() {
    this.navCtrl.navigateForward([`/admin`]);
  }

  itemTapped(bankAccess: ResourceBankAccess) {
    this.navCtrl.navigateForward([`/bankconnections/${bankAccess.id}`]);
  }
}
