import { BookingPeriodTO } from './../../../multibanking-api/bookingPeriodTO';
import { BookingGroupTO } from './../../../multibanking-api/bookingGroupTO';
import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AlertController, LoadingController, ToastController } from '@ionic/angular';
import * as moment from 'moment';
import { BaseChartDirective } from 'ng2-charts';
import { Observable, Subscriber } from 'rxjs';

import { Link } from '../../../multibanking-api/link';
import { ResourceBankAccount } from '../../../multibanking-api/resourceBankAccount';
import { ResourceUpdateAuthResponseTO } from '../../../multibanking-api/resourceUpdateAuthResponseTO';
import { BankAccountService } from '../../services/rest/bankAccount.service';
import { getHierarchicalRouteParam } from '../../utils/utils';
import { AnalyticsService } from '../../services/rest/analytics.service';
import { AnalyticsTO } from '../../../multibanking-api/analyticsTO';
import { Moment } from 'moment';
import { ConsentService } from '../../services/rest/consent.service';
import { ContractTO } from '../../../multibanking-api/contractTO';
import { Budget } from '../../model/budget';
import { AggregatedGroups } from '../../model/aggregatedGroups';


@Component({
  selector: 'app-analytics',
  templateUrl: './analytics.page.html',
  styleUrls: ['./analytics.page.scss'],
})
export class AnalyticsPage implements OnInit {

  analytics: AnalyticsTO;
  backLink: string;
  bankAccessId: string;
  bankAccount: ResourceBankAccount;

  periods: Moment[][] = [];
  analyticsDate: Moment;
  referenceDate: Moment;
  forecast = false;
  budget: Budget = {
    periodStart: moment(),
    periodEnd: moment(),
    incomeFix: { amount: 0, groups: [] },
    incomeOther: { amount: 0, groups: [] },
    expensesFix: { amount: 0, groups: [] },
    expensesVariable: { amount: 0, groups: [] },
    expensesOther: { amount: 0, groups: [] }
  };

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

  @ViewChild(BaseChartDirective, {static: true}) _chart;

  constructor(
    private activatedRoute: ActivatedRoute,
    private alertController: AlertController,
    private toastController: ToastController,
    private loadingController: LoadingController,
    private bankAccountService: BankAccountService,
    private consentService: ConsentService,
    private analyticsService: AnalyticsService
  ) {}

  ngOnInit() {
    this.bankAccessId = getHierarchicalRouteParam(this.activatedRoute.snapshot, 'access-id');
    this.bankAccount = this.activatedRoute.snapshot.data.bankAccount;
    this.backLink = `/bankconnections/${this.bankAccessId}`;

    if (!this.bankAccount.lastSync || moment(this.bankAccount.lastSync).isBefore(moment(), 'day')) {
      this.syncBookings();
    } else {
      this.loadAnalytics();
    }
  }

  loadAnalytics() {
    this.analyticsService.getAnalytics(this.bankAccessId, this.bankAccount.id).subscribe(
      response => {
        this.analyticsLoaded(response);
      },
      messages => {
        if (messages instanceof Array) {
          messages.forEach(async message => {
            if (message.key === 'RESOURCE_NOT_FOUND') {
              // ignore
            } else if (message.key === 'SYNC_IN_PROGRESS') {
              const toast = await this.toastController.create({
                message: 'Account sync in progress',
                showCloseButton: true,
                position: 'top'
              });
              toast.present();
            } else {
              const alert = await this.alertController.create({
                message: message.renderedMessage,
                buttons: ['OK']
              });
              alert.present();
            }
          });
        }
      }
    );
  }

  analyticsLoaded(accountAnalytics: AnalyticsTO) {
    this.analytics = accountAnalytics;
    this.analyticsDate = moment(accountAnalytics.analyticsDate);
    this.referenceDate = moment(accountAnalytics.analyticsDate);

    const referenceGroup = this.getReferenceGroup();

    this.periods = [];
    this.lineChartLabels = [];

    if (referenceGroup) {
      const analyticsStart: Moment = moment().subtract(12, 'month').day(1);
      const analyticsEnd: Moment = moment().add(13, 'month').day(1);

      referenceGroup.bookingPeriods
        .filter(period => moment(period.start).isAfter(analyticsStart) && moment(period.end).isBefore(analyticsEnd))
        .forEach(period => this.periods.push([moment(period.start), moment(period.end)]));

      this.initChart(referenceGroup);
      const referencePeriod = this.findPeriod(referenceGroup.bookingPeriods, this.referenceDate);
      this.budget = this.calculateBudget(moment(referencePeriod.start), moment(referencePeriod.end));
    }
  }

  getReferenceGroup() {
    let group = this.analytics.bookingGroups.find((bookingGroup: BookingGroupTO) => {
      return bookingGroup.salaryWage;
    });

    if (!group) {
      group = this.analytics.bookingGroups.find((bookingGroup: BookingGroupTO) => {
        return bookingGroup.contract.interval === ContractTO.IntervalEnum.MONTHLY
          && !bookingGroup.contract.cancelled
          && this.getPeriodAmount(bookingGroup, moment()) !== 0;
      });
    }

    return group;
  }

  initChart(referenceGroup: BookingGroupTO) {
    this.periods.forEach(period => {
      this.lineChartLabels.push(referenceGroup.salaryWage ?
        moment(period[1]).format('MMM YYYY') : moment(period[0]).format('MMM YYYY'));

      const budget = this.calculateBudget(period[0], period[1]);

      this.lineChartData[0].data.push(budget.incomeFix.amount);
      this.lineChartData[1].data.push(budget.incomeOther.amount);
      this.lineChartData[2].data.push(budget.expensesFix.amount * -1);
      this.lineChartData[3].data.push(budget.expensesVariable.amount * -1);
      this.lineChartData[4].data.push(budget.expensesOther.amount * -1);
    });
    this._chart.refresh();
  }

  calculateBudget(periodStart: Moment, periodEnd: Moment): Budget {
    const budget: Budget = {
      periodStart,
      periodEnd,
      incomeFix: { amount: 0, groups: [] },
      incomeOther: { amount: 0, groups: [] },
      expensesFix: { amount: 0, groups: [] },
      expensesVariable: { amount: 0, groups: [] },
      expensesOther: { amount: 0, groups: [] }
    };

    this.analytics.bookingGroups.forEach((group: BookingGroupTO) => {
      const amount = this.getPeriodAmount(group, periodStart);

      if (this.includeGroup(group, periodStart)) {
        switch (group.type) {
          case BookingGroupTO.TypeEnum.RECURRENTINCOME:
            budget.incomeFix.amount += amount;
            budget.incomeFix.groups.push(group);
            break;
          case BookingGroupTO.TypeEnum.RECURRENTNONSEPA:
          case BookingGroupTO.TypeEnum.RECURRENTSEPA:
          case BookingGroupTO.TypeEnum.STANDINGORDER:
            budget.expensesFix.amount += amount;
            budget.expensesFix.groups.push(group);
            break;
          case BookingGroupTO.TypeEnum.CUSTOM:
            budget.expensesVariable.amount += amount;
            budget.expensesVariable.groups.push(group);
            break;
          case BookingGroupTO.TypeEnum.OTHEREXPENSES:
            budget.expensesOther.amount += amount;
            budget.expensesOther.groups.push(group);
            break;
          case BookingGroupTO.TypeEnum.OTHERINCOME:
            budget.incomeOther.amount += amount;
            budget.incomeOther.groups.push(group);
            break;
        }
      }
    })
    return budget;
  }

  getPeriodAmount(group: BookingGroupTO, periodStart: Moment): number {
    const period: BookingPeriodTO = this.findPeriod(group.bookingPeriods, periodStart);

    if (period && period.amount) {
      return period.amount;
    }

    if (group.contract && group.contract.cancelled) {
      return 0;
    }

    if (group.amount && moment().isSameOrBefore(periodStart, 'day')) {
      return group.amount;
    }

    return 0;
  }

  isRecurrent(group: BookingGroupTO) {
    switch (group.type) {
      case BookingGroupTO.TypeEnum.RECURRENTINCOME:
      case BookingGroupTO.TypeEnum.RECURRENTNONSEPA:
      case BookingGroupTO.TypeEnum.RECURRENTSEPA:
      case BookingGroupTO.TypeEnum.STANDINGORDER:
        return true;
      case BookingGroupTO.TypeEnum.OTHERINCOME:
      case BookingGroupTO.TypeEnum.CUSTOM:
      case BookingGroupTO.TypeEnum.OTHEREXPENSES:
        return false;
    }
  }

  includeGroup(group: BookingGroupTO, referenceDate: Moment): boolean {
    switch (group.type) {
      case BookingGroupTO.TypeEnum.OTHERINCOME:
      case BookingGroupTO.TypeEnum.OTHEREXPENSES:
      case BookingGroupTO.TypeEnum.STANDINGORDER:
      case BookingGroupTO.TypeEnum.CUSTOM:
        return true;
    }

    return this.findPeriod(group.bookingPeriods, referenceDate) != null;
  }

  findPeriod(periods: BookingPeriodTO[], referenceDate: Moment): BookingPeriodTO {
    if (periods) {
      return periods.find((period: BookingPeriodTO) => {
        const periodStart: Moment = moment(period.start);
        const periodEnd: Moment = moment(period.end);
        return referenceDate.isSameOrAfter(periodStart, 'day') && referenceDate.isSameOrBefore(periodEnd, 'day');
      });
    }
  }

  async syncBookings() {
    const loading = await this.loadingController.create({
      message: 'Please wait...'
    });
    loading.present();

    this.bankAccountService.syncBookings(this.bankAccessId, this.bankAccount.id).subscribe(
      response => {
        loading.dismiss();
        if (response && response.challenge) {
          this.presentTanPrompt(response).subscribe(tan => {
            this.submitTan(response, tan);
          });
        } else {
          this.loadAnalytics();
          this.bankAccount.lastSync = moment().toDate();
        }
      },
      messages => {
        loading.dismiss();
        if (messages instanceof Array) {
          messages.forEach(async message => {
            if (message.key === 'SYNC_IN_PROGRESS') {
              const toast = await this.toastController.create({
                message: 'Account sync in progress',
                showCloseButton: true,
                position: 'top'
              });
              toast.present();
            } else if (message.key === 'INVALID_PIN') {
              const toast = await this.toastController.create({
                message: 'Invalid pin',
                buttons: ['OK']
              });
              toast.present();
            } else {
              const toast = await this.toastController.create({
                message: message.renderedMessage,
                buttons: ['OK']
              });
              toast.present();
            }
          });
        }
      });
  }

  presentTanPrompt(consentAuthStatus: ResourceUpdateAuthResponseTO): Observable<string> {
    return new Observable((observer: Subscriber<string>) => {
      this.alertController.create({
        header: consentAuthStatus.challenge.additionalInformation,
        inputs: [
          {
            name: 'TAN',
            type: 'text',
          }
        ],
        buttons: [
          {
            text: 'Ok',
            handler: data => {
              observer.next(data.TAN);
            }
          }
        ]
      })
        .then(alert => alert.present());
    });
  }

  private submitTan(consentAuthStatus: ResourceUpdateAuthResponseTO, tan: string) {
    // tslint:disable-next-line:no-string-literal
    const updateAuthenticationLink: Link = consentAuthStatus._links['transactionAuthorisation'];
    this.consentService.updateAuthentication(updateAuthenticationLink.href, { scaAuthenticationData: tan })
      .subscribe(
        () => {
          this.syncBookings();
        },
        messages => {
          if (messages instanceof Array) {
            messages.forEach(async message => {
              const alert = await this.alertController.create({
                message: message.renderedMessage,
                buttons: ['OK']
              });
              alert.present();
            });
          }
        });
  }

  newDateSelected(date: Moment) {
    this.referenceDate = date;
    this.forecast = date.isSameOrAfter(moment(), 'month');

    // tslint:disable-next-line: no-shadowed-variable
    const period = this.periods.find((period: Moment[]) => {
      return date.isSameOrAfter(period[0], 'day') && date.isSameOrBefore(period[1], 'day');
    });

    this.budget = this.calculateBudget(period[0], period[1]);
  }

  chartClicked(e: any): void {
    const activePoints = this._chart.chart.getElementsAtEventForMode(e.event, 'point', e.event.options);
    const firstPoint = activePoints[0];

    if (firstPoint) {
      const period = this.periods[firstPoint._index];
      const budget = this.calculateBudget(period[0], period[1]);
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

      // this.navCtrl.push(BookingGroupPage,
      //   {
      //     label: this.lineChartLabels[firstPoint._datasetIndex],
      //     period,
      //     bankAccessId: this.bankAccess.id,
      //     bankAccountId: this.bankAccountId,
      //     aggregatedGroups: bookingGroups
      //   })
    }
  }

  itemSelected(label: string, aggregatedGroups: AggregatedGroups) {
    // tslint:disable-next-line: no-shadowed-variable
    const period = this.periods.find((period: Moment[]) => {
      return this.referenceDate.isSameOrAfter(period[0], 'day') && this.referenceDate.isSameOrBefore(period[1], 'day');
    });

    // this.navCtrl.push(BookingGroupPage,
    //   {
    //     label: label,
    //     period: period,
    //     bankAccessId: this.bankAccess.id,
    //     bankAccountId: this.bankAccountId,
    //     aggregatedGroups: aggregatedGroups,
    //   });
  }

}
