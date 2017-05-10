import {Component} from '@angular/core';
import {NavController, NavParams} from 'ionic-angular';
import {BankAccessService} from "../../services/bankAccessService";

@Component({
  selector: 'page-bankaccess-create',
  templateUrl: 'bankAccessCreate.html'
})
export class BankAccessCreatePage {

  userId;
  bankAccess = {bankCode: '', bankLogin: '', pin: '', userId: ''};
  parent;

  constructor(public navCtrl: NavController, private navparams: NavParams, private bankAccessService: BankAccessService) {
    this.userId = navparams.data.userId;
    this.bankAccess.userId = navparams.data.userId;
    this.parent = navparams.data.parent;
  }

  public createBankAccess() {
    this.bankAccessService.crateBankAcccess(this.userId, this.bankAccess).subscribe(response => {
      this.parent.bankAccessCreated();
      this.navCtrl.pop();
    })
  }


}
