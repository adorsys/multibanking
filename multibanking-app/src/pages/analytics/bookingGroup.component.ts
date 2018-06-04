import { Component } from "@angular/core";
import { NavParams, NavController } from "ionic-angular";
import { BookingGroup } from "../../api/BookingGroup";
import { ENV } from "../../env/env";
import { GroupType } from "../../api/GroupType";
import { Moment } from "moment";
import { BookingPeriod } from "../../api/BookingPeriod";
import * as moment from 'moment';
import { BookingGroupDetailPage } from "./bookingGroupDetail.component";

@Component({
  selector: 'page-bookingGroup',
  templateUrl: 'bookingGroup.component.html'
})
export class BookingGroupPage {

  referenceDate: Moment;
  label: string;
  bookingGroups: BookingGroup[];
  bankAccessId: string;
  bankAccountId: string;

  constructor(public navparams: NavParams,
    public navCtrl: NavController) {
    this.referenceDate = navparams.data.date;
    this.label = navparams.data.label;
    this.bankAccessId = navparams.data.bankAccessId;
    this.bankAccountId = navparams.data.bankAccountId;
    this.bookingGroups = this.sortGroups(navparams.data.bookingGroups.groups);
  }

  sortGroups(bookingGroups: BookingGroup[]): BookingGroup[] {
    return bookingGroups.sort((group1: BookingGroup, group2: BookingGroup) => {
      if (moment(this.getExecutionDate(group1)).isAfter(moment(this.getExecutionDate(group2)))) {
        return 1;
      } else {
        return -1;
      }
    });
  }

  getMatchingBookingPeriod(bookingGroup: BookingGroup): BookingPeriod {
    return bookingGroup.bookingPeriods.find((period: BookingPeriod) => {
      let start: Moment = moment(period.start);
      return start.month() == this.referenceDate.month() && start.year() == this.referenceDate.year();
    });
  }

  getAmount(bookingGroup: BookingGroup) {
    let period = this.getMatchingBookingPeriod(bookingGroup);
    return period && period.amount ? period.amount : bookingGroup.amount;
  }

  getExecutionDate(bookingGroup: BookingGroup) {
    if (this.isRecurrent(bookingGroup)) {
      let period = this.getMatchingBookingPeriod(bookingGroup);
      if (period && period.bookings.length == 1) {
        return period.bookings[0].executionDate;
      }
      return null;
    }
    return null;
  }

  isExecuted(bookingGroup: BookingGroup) {
    if (this.isRecurrent(bookingGroup)) {
      let period = this.getMatchingBookingPeriod(bookingGroup);
      if (period && period.bookings.length == 1) {
        return period.bookings[0].executed;
      }
    }
    return false;
  } 

  getCompanyLogoUrl(bookingGroup: BookingGroup) {
    return ENV.api_url + "/image/" + bookingGroup.contract.logo;
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

  itemSelected(bookingGroup: BookingGroup) {
    this.navCtrl.push(BookingGroupDetailPage,
      {
        date: this.referenceDate,
        bankAccessId: this.bankAccessId,
        bankAccountId: this.bankAccountId,
        bookingGroup: bookingGroup
      })
  }
}