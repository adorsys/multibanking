import {Component} from "@angular/core";
import {NavController, AlertController, ToastController, NavParams} from "ionic-angular";
import {BookingListPage} from "../booking/bookingList";
import {BankAccountService} from "../../services/bankAccountService";
import {AnalyticsService} from "../../services/analyticsService";

@Component({
  selector: 'page-analytics',
  templateUrl: 'analytics.html'
})
export class AnalyticsPage {

  analytics;
  userId;
  bankAccessId
  bankAccountId;
  bookings;

  constructor(public navCtrl: NavController,
              private navparams: NavParams,
              private alertCtrl: AlertController,
              private toastCtrl: ToastController,
              private bankAccountService: BankAccountService,
              private analyticsService: AnalyticsService) {

    this.userId = navparams.data.userId;
    this.bankAccessId = navparams.data.bankAccessId;
    this.bankAccountId = navparams.data.bankAccountId;

    bankAccountService.bookingsChangedObservable.subscribe(changed => {
      this.loadAnalytics();
    })
    this.loadAnalytics();
  }

  loadAnalytics() {
    this.analyticsService.getAnalytics(this.userId, this.bankAccessId, this.bankAccountId).subscribe(
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
    this.bankAccountService.syncBookings(this.userId, this.bankAccessId, this.bankAccountId, pin).subscribe(
      response => {
        this.bookings = response;
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

  showBookings() {
    this.navCtrl.push(BookingListPage, {
      userId: this.userId,
      bankAccessId: this.bankAccessId,
      bankAccountId: this.bankAccountId,
      bookings: this.bookings
    })
  }


}
