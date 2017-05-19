import {Component} from '@angular/core';
import {NavController, AlertController, ToastController} from 'ionic-angular';
import {NavParams} from 'ionic-angular';
import {BankAccountService} from "../../services/bankAccountService";
import {BookingService} from "../../services/bookingService";

@Component({
  selector: 'page-bookingList',
  templateUrl: 'bookingList.html'
})
export class BookingListPage {

  userId;
  bankAccessId
  bankAccountId;
  bookings;

  constructor(public navCtrl: NavController,
              private navparams: NavParams,
              private alertCtrl: AlertController,
              private toastCtrl: ToastController,
              private bankAccountService: BankAccountService,
              private bookingService: BookingService) {

    this.userId = navparams.data.userId;
    this.bankAccessId = navparams.data.bankAccessId;
    this.bankAccountId = navparams.data.bankAccountId;

    if (!navparams.data.bookings) {
      this.bookingService.getBookings(this.userId, this.bankAccessId, this.bankAccountId).subscribe(response => {
        this.bookings = response;
      })
    }
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

  itemSelected(booking) {
  }


}
