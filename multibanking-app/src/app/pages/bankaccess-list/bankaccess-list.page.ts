import { Component, OnInit } from '@angular/core';
import { NavController, ActionSheetController } from '@ionic/angular';
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
              private actionSheetCtrl: ActionSheetController,
              private keycloak: KeycloakService) { }

  ngOnInit() {
    this.admin = this.keycloak.getRoles().filter(role => role === 'rules_admin').length > 0;

    this.loadBankAccesses();
  }

  loadBankAccesses() {
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

  logout() {
    this.keycloak.logout()
      .then(() => {
        this.loginImplicit();
      });
  }

  loginImplicit() {
    this.keycloak.login({ prompt: 'login', redirectUri: '/bankconnections' });
  }

  itemTapped(bankAccess: ResourceBankAccess) {
    this.navCtrl.navigateForward([`/bankconnections/${bankAccess.id}`]);
  }

  deleteBankAccess(bankAccess: ResourceBankAccess) {
    this.bankAccessService.deleteBankAccess(bankAccess).subscribe(() =>
      this.loadBankAccesses()
    );
  }

  async presentActionSheet($event, bankAccess: ResourceBankAccess) {
    $event.stopPropagation();
    const actionSheet = await this.actionSheetCtrl.create({
      header: bankAccess.iban,
      buttons: [
        {
          text: 'Löschen',
          icon: 'trash',
          role: 'destructive',
          handler: () => {
            this.deleteBankAccess(bankAccess);
          }
        }, {
          text: 'Abbrechen',
          role: 'cancel',
          icon: 'close',
          handler: () => {
          }
        }
      ]
    });
    actionSheet.present();
  }
}
