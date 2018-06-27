import { Component } from "@angular/core";
import { NavParams } from "ionic-angular";
import { BookingGroup } from "../../api/BookingGroup";
import { ENV } from "../../env/env";
import { GroupType } from "../../api/GroupType";
import { Moment } from "moment";
import { BookingPeriod } from "../../api/BookingPeriod";
import * as moment from 'moment';
import { BookingService } from "../../services/booking.service";
import { Booking } from "../../api/Booking";
import { ExecutedBooking } from "../../api/ExecutedBooking";

@Component({
  selector: 'page-bookingGroupDetail',
  templateUrl: 'bookingGroupDetail.component.html'
})
export class BookingGroupDetailPage {

  groupName: string;
  bookingPeriod: BookingPeriod;
  bookings: ExecutedBooking[] = [];
  bankAccessId: string;
  bankAccountId: string;

  constructor(public navparams: NavParams,
    private bookingService: BookingService) {
    this.bankAccessId = navparams.data.bankAccessId;
    this.bankAccountId = navparams.data.bankAccountId;
    this.groupName = navparams.data.groupName;
    this.bookingPeriod = navparams.data.bookingPeriod;
  }

  ngOnInit() {
    let bookingIds = this.bookingPeriod.bookings.map(booking => booking.bookingId);

    let executedBookings: ExecutedBooking[] = [];
    this.bookingService.getBookingsByIds(this.bankAccessId, this.bankAccountId, bookingIds).subscribe((bookings: Booking[]) => {
      bookings.forEach(loadedBooking => {
        let executedBooking = this.bookingPeriod.bookings.find(booking => booking.bookingId == loadedBooking.id);
        executedBooking.loadedBooking = loadedBooking;
        executedBooking.executionDate = this.getExecutionDate(executedBooking);
        executedBookings.push(executedBooking);
      });

      this.bookings = this.sortBookings(executedBookings);
    })
  }

  getExecutionDate(booking: ExecutedBooking) {
    return booking.executed ? booking.loadedBooking.bookingDate : booking.executionDate;
  }

  getMatchingBookingPeriod(bookingGroup: BookingGroup, referenceDate: Moment): BookingPeriod {
    return bookingGroup.bookingPeriods.find((period: BookingPeriod) => {
      let start: Moment = moment(period.start);
      return start.month() == referenceDate.month() && start.year() == referenceDate.year();
    });
  }

  sortBookings(bookings: ExecutedBooking[]): ExecutedBooking[] {
    return bookings.sort((booking1: ExecutedBooking, booking2: ExecutedBooking) => {
      if (moment(booking1.executionDate).isAfter(booking2.executionDate)) {
        return 1;
      } else {
        return -1;
      }
    });
  }

  isRecurrent(group: BookingGroup) {
    switch (group.type) {
      case GroupType.RECURRENT_INCOME:
      case GroupType.RECURRENT_NONSEPA:
      case GroupType.RECURRENT_SEPA:
      case GroupType.STANDING_ORDER:
        return true;
      case GroupType.OTHER_INCOME:
      case GroupType.CUSTOM:
      case GroupType.OTHER_EXPENSES:
        return false;
    }
  }

  getCompanyLogoUrl(booking: Booking) {
    return ENV.api_url + "/image/" + booking.bookingCategory.logo;
  }
  getReceiver(booking: ExecutedBooking): string {
    if (booking.loadedBooking.bookingCategory && booking.loadedBooking.bookingCategory.receiver) {
      return booking.loadedBooking.bookingCategory.receiver;
    } else if (booking.loadedBooking.otherAccount && booking.loadedBooking.otherAccount.owner) {
      return booking.loadedBooking.otherAccount.owner;
    }
    return "";
  }

}