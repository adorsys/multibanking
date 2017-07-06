import {Component} from "@angular/core";
import {AlertController, ToastController, NavParams, LoadingController} from "ionic-angular";
import {BankAccountService} from "../../services/bankAccountService";
import {AnalyticsService} from "../../services/analyticsService";

@Component({
  selector: 'page-analytics',
  templateUrl: 'analytics.html'
})
export class AnalyticsPage {

  analytics;
  bankAccess
  bankAccountId;
  bookings;

  constructor(private navparams: NavParams,
              private alertCtrl: AlertController,
              private toastCtrl: ToastController,
              private loadingCtrl: LoadingController,
              private bankAccountService: BankAccountService,
              private analyticsService: AnalyticsService) {

    this.bankAccess = navparams.data.bankAccess;
    this.bankAccountId = navparams.data.bankAccountId;

    bankAccountService.bookingsChangedObservable.subscribe(changed => {
      this.loadAnalytics();
    })
    this.loadAnalytics();
  }

  loadAnalytics() {
    this.analyticsService.getAnalytics(this.bankAccess.id, this.bankAccountId).subscribe(
      response => {
        this.analytics = response;
      },
      error => {
        if (error == "SYNC_IN_PROGRESS") {
          this.toastCtrl.create({
            message: 'Account sync in progress',
            showCloseButton: true,
            position: 'top'
          }).present();
        }
      })
  }

  syncBookingsPromptPin() {
    let alert = this.alertCtrl.create({
      title: 'Pin',
      inputs: [
        {
          name: 'pin',
          placeholder: 'Bank Account Pin',
          type: 'password'
        }
      ],
      buttons: [
        {
          text: 'Cancel',
          role: 'cancel'
        },
        {
          text: 'Submit',
          handler: data => {
            if (data.pin.length > 0) {
              this.syncBookings(data.pin);
            }
          }
        }
      ]
    });
    alert.present();
  }

  syncBookings(pin) {
    if (!pin && !this.bankAccess.storePin) {
      return this.syncBookingsPromptPin();
    }

    let loading = this.loadingCtrl.create({
      content: 'Please wait...'
    });
    loading.present();

    this.bankAccountService.syncBookings(this.bankAccess.id, this.bankAccountId, pin).subscribe(
      response => {
        this.bookings = response;
        loading.dismiss();
      },
      error => {
        if (error == "SYNC_IN_PROGRESS") {
          this.toastCtrl.create({
            message: 'Account sync in progress',
            showCloseButton: true,
            position: 'top'
          }).present();
        }
      })
  }

}
