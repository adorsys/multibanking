import { Component } from "@angular/core";
import { NavParams, NavController } from "ionic-angular";
import { Booking } from "../../api/Booking";
import { BookingEditPage } from "../booking-edit/bookingEdit";

@Component({
  selector: 'page-bookingDetail',
  templateUrl: 'bookingDetail.html'
})
export class BookingDetailPage {

  booking: Booking;

  constructor(
    public navCtrl: NavController,
    public navparams: NavParams
  ) {
    this.booking = navparams.data.booking;
  }

  editCategory() {
    this.navCtrl.push(BookingEditPage, {
      booking: this.booking
    });
  }

}
