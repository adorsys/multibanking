import {Component} from "@angular/core";
import {NavController, NavParams, LoadingController} from "ionic-angular";
import {BankAccessService} from "../../services/bankAccessService";
import { BankAccess } from "../../api/BankAccess";

@Component({
  selector: 'page-bankaccess-update',
  templateUrl: 'bankAccessUpdate.html'
})
export class BankAccessUpdatePage {

  bankAccess: BankAccess;
  parent;

  constructor(public navCtrl: NavController,
              private navparams: NavParams,
              private loadingCtrl: LoadingController,
              private bankAccessService: BankAccessService) {

    this.bankAccess = navparams.data.bankAccess;
    this.parent = navparams.data.parent;
  }

  public updateBankAccess() {
    let loading = this.loadingCtrl.create({
      content: 'Please wait...'
    });
    loading.present();

    this.bankAccessService.updateBankAcccess(this.bankAccess).subscribe(response => {
      loading.dismiss();

      this.parent.bankAccessesChanged();
      this.navCtrl.pop();
    })
  }


}
