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
import { ENV } from "../../env/env";
import { Budget } from "../../api/Budget";
import { BaseChartDirective } from "ng2-charts";
import { AggregatedGroups } from "../../api/AggregatedGroups";
import { Contract } from "../../api/Contract";

@Component({
  selector: 'page-analytics',
  templateUrl: 'analytics.component.html'
})
export class AnalyticsPage {

  analytics: AccountAnalytics;
  bankAccess: BankAccess;
  bankAccountId: string;

  periods: Moment[][] = [];
  analyticsDate: Moment;
  referenceDate: Moment;
  forecast: boolean = false;
  budget: Budget = {
    periodStart: moment(),
    periodEnd: moment(),
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

  loadAnalytics() {
    this.analyticsService.getAnalytics(this.bankAccess.id, this.bankAccountId).subscribe(
      response => {
        this.analyticsLoaded(response);
      },
      messages => {
        if (messages instanceof Array) {
          messages.forEach(message => {
            if (message.key == "RESCOURCE_NOT_FOUND") {
              //ignore
            }
            else if (message.key == "SYNC_IN_PROGRESS") {
              this.toastCtrl.create({
                message: 'Account sync in progress',
                showCloseButton: true,
                position: 'top'
              }).present();
            }
          });
        }
      }
    )
  }

  analyticsLoaded(accountAnalytics: AccountAnalytics) {
    this.analytics = accountAnalytics;
    this.analyticsDate = moment(accountAnalytics.analyticsDate);
    this.referenceDate = moment(accountAnalytics.analyticsDate);

    let referenceGroup = this.getReferenceGroup();

    this.periods = [];
    this.lineChartLabels = [];

    referenceGroup.bookingPeriods.forEach(period => {
      this.periods.push([moment(period.start), moment(period.end)]);
      this.lineChartLabels.push(moment(period.end).format('MMM YYYY'))
    });

    this.initChart();
    let referencePeriod = this.findPeriod(referenceGroup.bookingPeriods, this.referenceDate);
    this.budget = this.calculateBudget(moment(referencePeriod.start), moment(referencePeriod.end));
  }

  getReferenceGroup() {
    let group = this.analytics.bookingGroups.find((group: BookingGroup) => {
      return group.salaryWage;
    });

    if (!group) {
      group = this.analytics.bookingGroups.find((group: BookingGroup) => {
        return group.contract.interval == Contract.IntervalEnum.MONTHLY;
      });
    }

    return group;
  }

  initChart() {
    this.periods.forEach(period => {
      let budget = this.calculateBudget(period[0], period[1])

      this.lineChartData[0].data.push(budget.incomeFix.amount);
      this.lineChartData[1].data.push(budget.incomeOther.amount);
      this.lineChartData[2].data.push(budget.expensesFix.amount * -1);
      this.lineChartData[3].data.push(budget.expensesVariable.amount * -1);
      this.lineChartData[4].data.push(budget.expensesOther.amount * -1);
    });
    this._chart.refresh();
  }

  calculateBudget(periodStart: Moment, periodEnd: Moment): Budget {
    let budget: Budget = {
      periodStart: periodStart,
      periodEnd: periodEnd,
      incomeFix: { amount: 0, groups: [] },
      incomeOther: { amount: 0, groups: [] },
      expensesFix: { amount: 0, groups: [] },
      expensesVariable: { amount: 0, groups: [] },
      expensesOther: { amount: 0, groups: [] }
    };

    this.analytics.bookingGroups.forEach((group: BookingGroup) => {
      let amount = this.getPeriodAmount(group, periodStart);

      if (amount && amount != 0 && this.includeGroup(group, periodStart)) {
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

  getPeriodAmount(group: BookingGroup, referenceDate: Moment): number {
    let period: BookingPeriod = this.findPeriod(group.bookingPeriods, referenceDate);

    if (period) {
      if (period.amount) {
        return period.amount;
      } else if (this.isRecurrent(group)) {
        return group.amount;
      }
    }

    if (!this.isRecurrent(group)) {
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
      case GroupType.OTHER_EXPENSES:
      case GroupType.STANDING_ORDER:
      case GroupType.CUSTOM:
        return true;
    }

    return this.findPeriod(group.bookingPeriods, referenceDate) != null;
  }

  findPeriod(periods: BookingPeriod[], referenceDate: Moment): BookingPeriod {
    if (periods) {
      return periods.find((period: BookingPeriod) => {
        let periodStart: Moment = moment(period.start);
        let periodEnd: Moment = moment(period.end);
        return referenceDate.isSameOrAfter(periodStart) && referenceDate.isSameOrBefore(periodEnd);
      });
    }
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
      messages => {
        if (messages instanceof Array) {
          messages.forEach(message => {
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
          });
        }
      })
  }

  newDateSelected(date: Moment) {
    this.referenceDate = date;
    this.forecast = date.isSameOrAfter(moment(), "month");

    let period = this.periods.find((period: Moment[]) => {
      return date.isSameOrAfter(period[0]) && date.isSameOrBefore(period[1]);
    });

    this.budget = this.calculateBudget(period[0], period[1]);
  }

  chartClicked(e: any): void {
    var activePoints = this._chart.chart.getElementsAtEventForMode(e.event, 'point', e.event.options);
    var firstPoint = activePoints[0];

    if (firstPoint) {
      let period = this.periods[firstPoint._index];
      let budget = this.calculateBudget(period[0], period[1]);
      let bookingGroups;

      switch (firstPoint._datasetIndex) {
        case 0:
          bookingGroups = budget.incomeFix;
          break;
        case 1:
          bookingGroups = budget.incomeOther;
          break;
        case 2:
          bookingGroups = budget.expensesFix;
          break;
        case 3:
          bookingGroups = budget.expensesVariable;
          break;
        case 4:
          bookingGroups = budget.expensesOther;
          break;
      }

      this.navCtrl.push(BookingGroupPage,
        {
          label: this.lineChartLabels[firstPoint._datasetIndex],
          period: period,
          bankAccessId: this.bankAccess.id,
          bankAccountId: this.bankAccountId,
          aggregatedGroups: bookingGroups
        })
    }
  }

  itemSelected(label: string, aggregatedGroups: AggregatedGroups) {
    let period = this.periods.find((period: Moment[]) => {
      return this.referenceDate.isSameOrAfter(period[0]) && this.referenceDate.isSameOrBefore(period[1]);
    });

    this.navCtrl.push(BookingGroupPage,
      {
        label: label,
        period: period,
        bankAccessId: this.bankAccess.id,
        bankAccountId: this.bankAccountId,
        aggregatedGroups: aggregatedGroups,
      })
  }

}
