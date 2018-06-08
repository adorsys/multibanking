import { Component, ViewChild } from "@angular/core";
import { NavParams, NavController } from "ionic-angular";
import { BookingGroup } from "../../api/BookingGroup";
import { ENV } from "../../env/env";
import { GroupType } from "../../api/GroupType";
import { Moment } from "moment";
import { BookingPeriod } from "../../api/BookingPeriod";
import * as moment from 'moment';
import { BookingGroupDetailPage } from "./bookingGroupDetail.component";
import { AggregatedGroups } from "../../api/AggregatedGroups";
import { BaseChartDirective } from "ng2-charts";

@Component({
  selector: 'page-bookingGroup',
  templateUrl: 'bookingGroup.component.html'
})
export class BookingGroupPage {

  referenceDate: Moment;
  amount: number;
  label: string;
  aggregatedGroups: AggregatedGroups;
  bookingGroups: BookingGroup[];
  bankAccessId: string;
  bankAccountId: string;

  lineChartLabels: string[] = [];
  lineChartData: number[] = [];

  @ViewChild(BaseChartDirective) _chart;

  constructor(public navparams: NavParams,
    public navCtrl: NavController) {
    this.referenceDate = navparams.data.date;
    this.label = navparams.data.label;
    this.bankAccessId = navparams.data.bankAccessId;
    this.bankAccountId = navparams.data.bankAccountId;
    this.amount = navparams.data.amount;
    this.aggregatedGroups = navparams.data.aggregatedGroups;
    this.bookingGroups = this.sortGroups(navparams.data.aggregatedGroups);
  }

  ngOnInit() {
    this.amount = 0;
    this.bookingGroups.forEach(group => {
      this.lineChartLabels.push(group.name ? group.name : group.otherAccount);

      let tmpAmount = this.getPeriodAmount(group);
      tmpAmount = tmpAmount != 0 ? tmpAmount : group.amount;
      this.lineChartData.push(tmpAmount);
      this.amount += tmpAmount;
    }, this);
  }

  sortGroups(aggregatedGroups: AggregatedGroups): BookingGroup[] {
    return aggregatedGroups.groups.sort((group1: BookingGroup, group2: BookingGroup) => {
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
    let amount = this.getPeriodAmount(bookingGroup);
    amount = amount ? amount : bookingGroup.amount;
    return amount >= 0 ? amount : amount * -1;
  }

  periodAmountExists(bookingGroup: BookingGroup) {
    return this.getPeriodAmount(bookingGroup);
  }

  getPeriodAmount(bookingGroup: BookingGroup): number {
    let period = this.getMatchingBookingPeriod(bookingGroup);
    return period && period.amount ? period.amount : 0;
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

  chartClicked(e: any): void {
    let activePoints = this._chart.chart.getElementsAtEventForMode(e.event, 'point', e.event.options);
    var firstPoint = activePoints[0];

    if (firstPoint) {
      let group: BookingGroup = this.aggregatedGroups.groups[firstPoint._index];
      this.itemSelected(group);
    }
  }

  itemSelected(bookingGroup: BookingGroup) {
    let period = this.getMatchingBookingPeriod(bookingGroup);
    if (period) {
      this.navCtrl.push(BookingGroupDetailPage,
        {
          groupName: bookingGroup.name ? bookingGroup.name : bookingGroup.otherAccount,
          bankAccessId: this.bankAccessId,
          bankAccountId: this.bankAccountId,
          bookingPeriod: period
        })
    }
  }
}