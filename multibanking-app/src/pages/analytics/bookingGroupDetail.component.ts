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

  referenceDate: Moment;
  bookingGroup: BookingGroup;
  bookings: ExecutedBooking[] = [];
  bankAccessId: string;
  bankAccountId: string;

  constructor(public navparams: NavParams,
    private bookingService: BookingService) {
    this.referenceDate = navparams.data.date;
    this.bankAccessId = navparams.data.bankAccessId;
    this.bankAccountId = navparams.data.bankAccountId;
    this.bookingGroup = navparams.data.bookingGroup;
    
    this.initBookingGroup(navparams.data.bookingGroup);
  }

  initBookingGroup(group: BookingGroup) {
    let bookingIds: string[] = [];

    let period: BookingPeriod = this.getMatchingBookingPeriod(group, this.referenceDate);
    if (period) {
      bookingIds = bookingIds.concat(period.bookings.map(booking => booking.bookingId));

      let bookings = [];
      this.bookingService.getBookingsByIds(this.bankAccessId, this.bankAccountId, bookingIds).subscribe((response: Booking[]) => {
        response.forEach(loadedBooking => {
          let executedBooking = period.bookings.find(booking => booking.bookingId == loadedBooking.id);
          if (executedBooking) {
            executedBooking.loadedBooking = loadedBooking;
            executedBooking.executionDate = this.getExecutionDate(executedBooking);
            if (!executedBooking.executed) {
              executedBooking.loadedBooking.amount = period.amount ? period.amount : group.amount;
            }
            bookings.push(executedBooking);
          }
        });

        this.bookings = this.sortBookings(bookings);
      })
    }
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
    return ENV.api_url + "/image/" + booking.bookingCategory.contract.logo;
  }
}