import {Component} from '@angular/core';
import {NavController, AlertController} from 'ionic-angular';
import {RegisterPage} from '../register/register';
import {BankAccessListPage} from "../bankaccess/bankAccessList";
import {BankAccessService} from "../../services/BankAccessService";

@Component({
  selector: 'page-login',
  templateUrl: 'login.html'
})
export class LoginPage {

  credentials = {userId: ''};

  constructor(public navCtrl: NavController, public alertCtrl: AlertController, private bankAccessService: BankAccessService) {
  }

  public login() {
    this.bankAccessService.getBankAccesses(this.credentials.userId).subscribe(
      response => {
        this.navCtrl.push(BankAccessListPage, {userId: this.credentials.userId, bankAccesses: response});
      },
      err => {
        if (err.message = 404) {
          let alert = this.alertCtrl.create({
            title: 'User not found!',
            buttons: ['OK']
          });
          alert.present();
        }
      })
  }

  public createAccount() {
    this.navCtrl.push(RegisterPage);
  }

}
