import {Component} from '@angular/core';
import {NavController} from 'ionic-angular';
import {NavParams} from 'ionic-angular';
import {BookingService} from "../../services/BookingService";
import {BookingListPage} from "../booking/bookingList";

@Component({
  selector: 'page-bankaccountList',
  templateUrl: 'bankaccountList.html'
})
export class BankAccountListPage {

  userId;
  bankAccessId;
  bankAccounts;

  constructor(public navCtrl: NavController, private navparams: NavParams, private bookingService: BookingService) {
    this.userId = navparams.data.userId;
    this.bankAccessId = navparams.data.bankAccessId;
    this.bankAccounts = navparams.data.bankAccounts;
  }

  itemSelected(bankAccount) {
    this.bookingService.getBookings(this.userId, this.bankAccessId, bankAccount.id).subscribe(response => {
      this.navCtrl.push(BookingListPage, {
        userId: this.userId,
        bankAccessId: this.bankAccessId,
        bankAccountId: bankAccount.id,
        bookings: response
      })
    })
  }

}
