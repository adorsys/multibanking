import { Component, ViewChild } from "@angular/core";
import { AlertController, ToastController, NavParams, LoadingController, NavController, Navbar } from "ionic-angular";
import { BankAccountService } from "../../services/bankAccount.service";
import { AnalyticsService } from "../../services/analytics.service";
import { BookingGroupPage } from "./bookingGroup.component";
import { Moment } from "moment";
import * as moment from 'moment';
import { ENV } from "../../env/env";
import { BaseChartDirective } from "ng2-charts";
import { Budget } from "model/budget";
import { AccountAnalyticsEntity, BankAccess, BookingGroup, Contract, BookingPeriod } from "../../model/multibanking/models";
import { AggregatedGroups } from "model/aggregatedGroups";

@Component({
  selector: 'page-analytics',
  templateUrl: 'analytics.component.html'
})
export class AnalyticsPage {

  analytics: AccountAnalyticsEntity;
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
            if (message.key == "RESOURCE_NOT_FOUND") {
              //ignore
            }
            else if (message.key == "SYNC_IN_PROGRESS") {
              this.toastCtrl.create({
                message: 'Account sync in progress',
                showCloseButton: true,
                position: 'top'
              }).present();
            } else {
              this.alertCtrl.create({
                message: message.renderedMessage,
                buttons: ['OK']
              }).present();
            }
          });
        }
      }
    )
  }

  analyticsLoaded(accountAnalytics: AccountAnalyticsEntity) {
    this.analytics = accountAnalytics;
    this.analyticsDate = moment(accountAnalytics.analyticsDate);
    this.referenceDate = moment(accountAnalytics.analyticsDate);

    let referenceGroup = this.getReferenceGroup();

    this.periods = [];
    this.lineChartLabels = [];

    if (referenceGroup) {
      let analyticsStart: Moment = moment().subtract(12, "month").day(1);
      let analyticsEnd: Moment = moment().add(13, "month").day(1);

      referenceGroup.bookingPeriods
        .filter(period => moment(period.start).isAfter(analyticsStart) && moment(period.end).isBefore(analyticsEnd))
        .forEach(period => this.periods.push([moment(period.start), moment(period.end)]));

      this.initChart(referenceGroup);
      let referencePeriod = this.findPeriod(referenceGroup.bookingPeriods, this.referenceDate);
      this.budget = this.calculateBudget(moment(referencePeriod.start), moment(referencePeriod.end));
    }
  }

  getReferenceGroup() {
    let group = this.analytics.bookingGroups.find((group: BookingGroup) => {
      return group.salaryWage;
    });

    if (!group) {
      group = this.analytics.bookingGroups.find((group: BookingGroup) => {
        return group.contract.interval == Contract.IntervalEnum.MONTHLY
          && !group.contract.cancelled
          && this.getPeriodAmount(group, moment()) != 0;
      });
    }

    return group;
  }

  initChart(referenceGroup: BookingGroup) {
    this.periods.forEach(period => {
      this.lineChartLabels.push(referenceGroup.salaryWage ?
        moment(period[1]).format('MMM YYYY') : moment(period[0]).format('MMM YYYY'))

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

      if (this.includeGroup(group, periodStart)) {
        switch (group.type) {
          case BookingGroup.TypeEnum.RECURRENTINCOME:
            budget.incomeFix.amount += amount;
            budget.incomeFix.groups.push(group);
            break;
          case BookingGroup.TypeEnum.RECURRENTNONSEPA:
          case BookingGroup.TypeEnum.RECURRENTSEPA:
          case BookingGroup.TypeEnum.STANDINGORDER:
            budget.expensesFix.amount += amount;
            budget.expensesFix.groups.push(group);
            break;
          case BookingGroup.TypeEnum.CUSTOM:
            budget.expensesVariable.amount += amount;
            budget.expensesVariable.groups.push(group);
            break;
          case BookingGroup.TypeEnum.OTHEREXPENSES:
            budget.expensesOther.amount += amount;
            budget.expensesOther.groups.push(group);
            break;
          case BookingGroup.TypeEnum.OTHERINCOME:
            budget.incomeOther.amount += amount;
            budget.incomeOther.groups.push(group);
            break;
        }
      }
    })
    return budget;
  }

  getPeriodAmount(group: BookingGroup, periodStart: Moment): number {
    let period: BookingPeriod = this.findPeriod(group.bookingPeriods, periodStart);

    if (period && period.amount) {
      return period.amount;
    }

    if (periodStart.isSame(moment(), "month")) {
        console.log();
    }

    if (group.amount && moment().isSameOrBefore(periodStart, 'day')) {
      return group.amount;
    }

    return 0;
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

  includeGroup(group: BookingGroup, referenceDate: Moment): boolean {
    switch (group.type) {
      case BookingGroup.TypeEnum.OTHERINCOME:
      case BookingGroup.TypeEnum.OTHEREXPENSES:
      case BookingGroup.TypeEnum.STANDINGORDER:
      case BookingGroup.TypeEnum.CUSTOM:
        return true;
    }

    return this.findPeriod(group.bookingPeriods, referenceDate) != null;
  }

  findPeriod(periods: BookingPeriod[], referenceDate: Moment): BookingPeriod {
    if (periods) {
      return periods.find((period: BookingPeriod) => {
        let periodStart: Moment = moment(period.start);
        let periodEnd: Moment = moment(period.end);
        return referenceDate.isSameOrAfter(periodStart, 'day') && referenceDate.isSameOrBefore(periodEnd, 'day');
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
            } else if (message.key == "INVALID_PIN") {
              this.alertCtrl.create({
                message: 'Invalid pin',
                buttons: ['OK']
              }).present();
            } else {
              this.alertCtrl.create({
                message: message.renderedMessage,
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
      return date.isSameOrAfter(period[0], 'day') && date.isSameOrBefore(period[1], 'day');
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
      return this.referenceDate.isSameOrAfter(period[0], 'day') && this.referenceDate.isSameOrBefore(period[1], 'day');
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
