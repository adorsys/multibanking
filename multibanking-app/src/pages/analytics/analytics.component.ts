import { Component, ViewChild } from "@angular/core";
import { AlertController, ToastController, NavParams, LoadingController, NavController, Navbar } from "ionic-angular";
import { BankAccountService } from "../../services/bankAccount.service";
import { AnalyticsService } from "../../services/analytics.service";
import { BankAccess } from "../../api/BankAccess";
import { AccountAnalytics } from "../../api/AccountAnalytics";
import { BookingGroup } from "../../api/BookingGroup";
import { BookingGroupPage } from "./bookingGroup.component";
import { GroupType } from "../../api/GroupType";
import { Moment } from "moment";
import * as moment from 'moment';
import { BookingPeriod } from "../../api/BookingPeriod";
import { AggregatedGroups } from "../../api/AggregatedGroups";
import { ENV } from "../../env/env";
import { Budget } from "../../api/Budget";
import { BaseChartDirective } from "ng2-charts";

@Component({
  selector: 'page-analytics',
  templateUrl: 'analytics.component.html'
})
export class AnalyticsPage {

  analytics: AccountAnalytics;
  bankAccess: BankAccess;
  bankAccountId: string;

  dates: Moment[] = [];
  referenceDate: Moment;
  forecast: boolean = false;
  budget: Budget = {
    incomeFix: { amount: 0, groups: [] },
    incomeOther: { amount: 0, groups: [] },
    expensesFix: { amount: 0, groups: [] },
    expensesVariable: { amount: 0, groups: [] },
    expensesOther: { amount: 0, groups: [] }
  }

  lineChartData: Array<any> = [
    { data: [], label: 'Fix incomings' },
    { data: [], label: 'Other incomings' },
    { data: [], label: 'Fix expenses' },
    { data: [], label: 'Variable expenses' },
    { data: [], label: 'Other expenses' }
  ];
  lineChartLabels: Array<any> = [];
  lineChartColors: Array<any> = [
    {
      backgroundColor: 'rgba(66,244,107,0.1)',
      borderColor: 'rgba(66,244,107,1)'
    },
    {
      backgroundColor: 'rgba(66,155,244,0.1)',
      borderColor: 'rgba(66,155,244,1)'
    },
    {
      backgroundColor: 'rgba(255,255,61,0.1)',
      borderColor: 'rgba(255,255,61,1)'
    },
    {
      backgroundColor: 'rgba(255,148,61,0.1)',
      borderColor: 'rgba(255,148,61,1)'
    },
    {
      backgroundColor: 'rgba(255,99,61,0.1)',
      borderColor: 'rgba(255,99,61,1)'
    }
  ];

  @ViewChild(Navbar) navBar: Navbar;
  @ViewChild(BaseChartDirective) _chart;

  constructor(
    public navCtrl: NavController,
    public navparams: NavParams,
    private alertCtrl: AlertController,
    private toastCtrl: ToastController,
    private loadingCtrl: LoadingController,
    private bankAccountService: BankAccountService,
    private analyticsService: AnalyticsService
  ) {
    this.bankAccess = navparams.data.bankAccess;
    this.bankAccountId = navparams.data.bankAccount.id;
  }

  ngOnInit() {
    let start: Moment = moment().subtract(6, "months");
    for (let index = 1; index < 13; index++) {
      let date = start.clone().add(index, "months");
      if (index == 6) {
        this.referenceDate = date;
      }
      this.dates.push(date);
      this.lineChartLabels.push(date.format('MMM YYYY'))
    }

    this.bankAccountService.bookingsChangedObservable.subscribe(changed => {
      this.loadAnalytics();
    });
    this.loadAnalytics();
  }

  ionViewDidLoad() {
    this.navBar.backButtonClick = (e: UIEvent) => {
      this.navCtrl.parent.viewCtrl.dismiss();
    };
  }

  public chartClicked(e: any): void {
    console.log();
    // this.referenceDate = date;
    // this.forecast = date.isAfter(moment(), "month");
  }

  public chartHovered(e: any): void {
    console.log(e);
  }

  loadAnalytics() {
    this.analyticsService.getAnalytics(this.bankAccess.id, this.bankAccountId).subscribe(
      response => {
        this.analytics = response;
        this.initChart();
        this.budget = this.calculateBudget(this.referenceDate, this.forecast);
      },
      error => {
        if (error == "SYNC_IN_PROGRESS") {
          this.toastCtrl.create({
            message: 'Account sync in progress',
            showCloseButton: true,
            position: 'top'
          }).present();
        }
      });
  }

  initChart() {
    let start: Moment = moment().subtract(6, "months");
    for (let index = 1; index < 13; index++) {
      let date = start.clone().add(index, "months");
      let budget = this.calculateBudget(date, date.isAfter(moment(), "month"))

      this.lineChartData[0].data.push(budget.incomeFix.amount);
      this.lineChartData[1].data.push(budget.incomeOther.amount);
      this.lineChartData[2].data.push(budget.expensesFix.amount * -1);
      this.lineChartData[3].data.push(budget.expensesVariable.amount * -1);
      this.lineChartData[4].data.push(budget.expensesOther.amount * -1);
    }
    this._chart.refresh();
  }

  calculateBudget(referenceDate: Moment, forecast: boolean): Budget {
    let budget: Budget = {
      incomeFix: { amount: 0, groups: [] },
      incomeOther: { amount: 0, groups: [] },
      expensesFix: { amount: 0, groups: [] },
      expensesVariable: { amount: 0, groups: [] },
      expensesOther: { amount: 0, groups: [] }
    };

    this.analytics.bookingGroups.forEach((group: BookingGroup) => {
      let amount = this.getAmount(group, referenceDate, forecast);

      if (amount != 0 && this.includeGroup(group, referenceDate)) {
        switch (group.type) {
          case GroupType.RECURRENT_INCOME:
            budget.incomeFix.amount += amount;
            budget.incomeFix.groups.push(group);
            break;
          case GroupType.OTHER_INCOME:
            budget.incomeOther.amount += amount;
            budget.incomeOther.groups.push(group);
            break;
          case GroupType.RECURRENT_NONSEPA:
          case GroupType.RECURRENT_SEPA:
          case GroupType.STANDING_ORDER:
            budget.expensesFix.amount += amount;
            budget.expensesFix.groups.push(group);
            break;
          case GroupType.CUSTOM:
            budget.expensesVariable.amount += amount;
            budget.expensesVariable.groups.push(group);
            break;
          case GroupType.OTHER_EXPENSES:
            budget.expensesOther.amount += amount;
            budget.expensesOther.groups.push(group);
            break;
        }
      }
    })
    return budget;
  }

  getAmount(group: BookingGroup, referenceDate: Moment, forecast: boolean): number {
    let period: BookingPeriod = group.bookingPeriods.find((period: BookingPeriod) => {
      let start: Moment = moment(period.start);
      return start.month() == referenceDate.month() && start.year() == referenceDate.year();
    });

    if (period) {
      return period.amount ? period.amount : group.amount
    }

    if (forecast) {
      return group.amount;
    }

    return 0;
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

  includeGroup(group: BookingGroup, referenceDate: Moment): boolean {
    switch (group.type) {
      case GroupType.OTHER_INCOME:
      case GroupType.OTHER_INCOME:
      case GroupType.STANDING_ORDER:
      case GroupType.CUSTOM:
      case GroupType.OTHER_EXPENSES:
        return true;
    }

    let period: BookingPeriod = group.bookingPeriods.find((period: BookingPeriod) => {
      let start: Moment = moment(period.start);
      return start.month() == referenceDate.month() && start.year() == referenceDate.year();
    });

    return period != null;
  }

  getCompanyLogoUrl(bookingGroup: BookingGroup) {
    return ENV.api_url + "/image/" + bookingGroup.contract.logo;
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
        loading.dismiss();
      },
      error => {
        if (error && error.messages) {
          error.messages.forEach(message => {
            if (message.key == "SYNC_IN_PROGRESS") {
              this.toastCtrl.create({
                message: 'Account sync in progress',
                showCloseButton: true,
                position: 'top'
              }).present();
            }
            else if (message.key == "INVALID_PIN") {
              this.alertCtrl.create({
                message: 'Invalid pin',
                buttons: ['OK']
              }).present();
            }
          })
        }

      })
  }

  newDateSelected(date: Moment) {
    this.referenceDate = date;
    this.forecast = date.isAfter(moment(), "month");
    this.budget = this.calculateBudget(date, this.forecast);
  }


  itemSelected(label: string, bookingGroups: AggregatedGroups) {
    this.navCtrl.push(BookingGroupPage,
      {
        label: label,
        date: this.referenceDate,
        bankAccessId: this.bankAccess.id,
        bankAccountId: this.bankAccountId,
        bookingGroups: bookingGroups
      })
  }

}
