import {Component} from "@angular/core";
import {NavController, NavParams, LoadingController} from "ionic-angular";
import {BankAccessService} from "../../services/bankAccessService";

@Component({
  selector: 'page-bankaccess-create',
  templateUrl: 'bankAccessCreate.html'
})
export class BankAccessCreatePage {

  userId;
  bankAccess = {bankCode: '', bankLogin: '', pin: '', userId: '', storePin: true, storeBookings: true, categorizeBookings: true, storeAnalytics: true};
  parent;

  constructor(public navCtrl: NavController,
              private navparams: NavParams,
              private loadingCtrl: LoadingController,
              private bankAccessService: BankAccessService) {

    this.userId = navparams.data.userId;
    this.bankAccess.userId = navparams.data.userId;
    this.parent = navparams.data.parent;
  }

  public createBankAccess() {
    let loading = this.loadingCtrl.create({
      content: 'Please wait...'
    });
    loading.present();

    this.bankAccessService.createBankAcccess(this.bankAccess).subscribe(response => {
      loading.dismiss();

      this.parent.bankAccessesChanged();
      this.navCtrl.pop();
    })
  }


}
