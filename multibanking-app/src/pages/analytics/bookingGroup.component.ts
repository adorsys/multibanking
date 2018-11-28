import { Component, ViewChild } from "@angular/core";
import { NavParams, NavController } from "ionic-angular";
import { ENV } from "../../env/env";
import { Moment } from "moment";
import * as moment from 'moment';
import { BookingGroupDetailPage } from "./bookingGroupDetail.component";
import { BaseChartDirective } from "ng2-charts";
import { AggregatedGroups } from "model/aggregatedGroups";
import { BookingGroup, BookingPeriod } from "../../model/multibanking/models";

@Component({
  selector: 'page-bookingGroup',
  templateUrl: 'bookingGroup.component.html'
})
export class BookingGroupPage {

  period: Moment[];
  amount: number;
  label: string;
  forecast: boolean;
  aggregatedGroups: AggregatedGroups;
  bookingGroups: BookingGroup[];
  bankAccessId: string;
  bankAccountId: string;

  lineChartLabels: string[] = [];
  lineChartData: number[] = [];

  @ViewChild(BaseChartDirective) _chart;

  constructor(public navparams: NavParams,
    public navCtrl: NavController) {
    this.period = navparams.data.period;
    this.label = navparams.data.label;
    this.bankAccessId = navparams.data.bankAccessId;
    this.bankAccountId = navparams.data.bankAccountId;
    this.amount = navparams.data.amount;
    this.aggregatedGroups = navparams.data.aggregatedGroups;
    this.forecast = moment().isSameOrBefore(this.period[0], 'day');
    this.bookingGroups = this.sortGroups(navparams.data.aggregatedGroups)
      .filter(group => {
        let tmpAmount = this.forecast ? this.getAmount(group) : this.getPeriodAmount(group);
        return tmpAmount && tmpAmount != 0;
      })
      .filter(group => {
        return !group.contract || !group.contract.cancelled || this.getMatchingBookingPeriod(group)
      });
  }

  ngOnInit() {
    this.amount = 0;
    this.bookingGroups.forEach(group => {
      let tmpAmount = this.forecast ? this.getAmount(group) : this.getPeriodAmount(group);
      tmpAmount = tmpAmount != 0 ? tmpAmount : group.amount;
      if (tmpAmount && tmpAmount != 0) {
        this.lineChartLabels.push(group.name ? group.name : group.otherAccount);
        this.lineChartData.push(tmpAmount);
        this.amount += tmpAmount;
      }
    }, this);
  }

  sortGroups(aggregatedGroups: AggregatedGroups): BookingGroup[] {
    return aggregatedGroups.groups.sort((group1: BookingGroup, group2: BookingGroup) => {
      if (moment(this.getExecutionDate(group1)).isAfter(moment(this.getExecutionDate(group2)))) {
        return -1;
      } else {
        return 1;
      }
    });
  }

  getMatchingBookingPeriod(bookingGroup: BookingGroup): BookingPeriod {
    if (!bookingGroup.bookingPeriods) {
      return undefined;
    }

    return bookingGroup.bookingPeriods.find((groupPeriod: BookingPeriod) => {
      let groupPeriodEnd: Moment = moment(groupPeriod.end);
      return groupPeriodEnd.isSameOrAfter(this.period[0], 'day') && groupPeriodEnd.isSameOrBefore(this.period[1], 'day');
    });
  }

  getAmount(bookingGroup: BookingGroup) {
    let amount = this.getPeriodAmount(bookingGroup);
    amount = amount > 0 ? amount : bookingGroup.amount;
    return amount >= 0 ? amount : amount * -1;
  }

  getPeriodAmount(bookingGroup: BookingGroup): number {
    let period = this.getMatchingBookingPeriod(bookingGroup);
    if (period && period.amount) {
      return period.amount;
    }
    return 0;
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
      case BookingGroup.TypeEnum.RECURRENTINCOME:
      case BookingGroup.TypeEnum.RECURRENTNONSEPA:
      case BookingGroup.TypeEnum.RECURRENTSEPA:
      case BookingGroup.TypeEnum.STANDINGORDER:
        return true;
      case BookingGroup.TypeEnum.OTHERINCOME:
      case BookingGroup.TypeEnum.CUSTOM:
      case BookingGroup.TypeEnum.OTHEREXPENSES:
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