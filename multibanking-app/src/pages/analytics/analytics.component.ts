import { Component, ViewChild } from "@angular/core";
import { AlertController, ToastController, NavParams, LoadingController, NavController, Navbar } from "ionic-angular";
import { BankAccountService } from "../../services/bankAccount.service";
import { AnalyticsService } from "../../services/analytics.service";
import { BankAccess } from "../../api/BankAccess";
import { AccountAnalytics } from "../../api/AccountAnalytics";
import { BookingGroup } from "../../api/BookingGroup";
import { AppConfig } from "../../app/app.config";
import { BookingGroupPage } from "./bookingGroup.component";
import { GroupType } from "../../api/GroupType";
import { Moment } from "moment";
import * as moment from 'moment';
import { BookingPeriod } from "../../api/BookingPeriod";
import { AggregatedGroups } from "../../api/AggregatedGroups";


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
  incomeFix: AggregatedGroups;
  incomeOther: AggregatedGroups;
  expensesFix: AggregatedGroups;
  expensesVariable: AggregatedGroups;
  expensesOther: AggregatedGroups;

  @ViewChild(Navbar) navBar: Navbar;

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
    for (let index = 1; index < 19; index++) {
      let date = start.clone().add(index, "months");
      if (index == 6) {
        this.referenceDate = date;
      }
      this.dates.push(date);
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

  loadAnalytics() {
    this.analyticsService.getAnalytics(this.bankAccess.id, this.bankAccountId).subscribe(
      response => {
        this.analytics = response;
        this.calculateBudget();
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

  calculateBudget() {
    this.incomeFix = { amount: 0, groups: [] };
    this.incomeOther = { amount: 0, groups: [] };

    this.expensesFix = { amount: 0, groups: [] };
    this.expensesVariable = { amount: 0, groups: [] };
    this.expensesOther = { amount: 0, groups: [] };

    this.analytics.bookingGroups.forEach((group: BookingGroup) => {
      if (this.includeGroup(group)) {
        switch (group.type) {
          case GroupType.RECURRENT_INCOME:
            this.incomeFix.amount += group.amount;
            this.incomeFix.groups.push(group);
            break;
          case GroupType.OTHER_INCOME:
            this.incomeOther.amount += group.amount;
            this.incomeOther.groups.push(group);
            break;
          case GroupType.RECURRENT_NONSEPA:
          case GroupType.RECURRENT_SEPA:
          case GroupType.STANDING_ORDER:
            this.expensesFix.amount += group.amount;
            this.expensesFix.groups.push(group);
            break;
          case GroupType.CUSTOM:
            this.expensesVariable.amount += group.amount;
            this.expensesVariable.groups.push(group);
            break;
          case GroupType.OTHER_EXPENSES:
            this.expensesOther.amount += group.amount;
            this.expensesOther.groups.push(group);
            break;
        }
      }
    })
  }

  includeGroup(group: BookingGroup): boolean {
    if (group.amount == 0) {
      return false;
    }

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
      return start.month() == this.referenceDate.month() && start.year() == this.referenceDate.year();
    });

    if (!period) {
      return false;
    }

    let periodBookingDate: string = period.bookingDates.find((bookingDate: string) => {
      return moment(bookingDate).month() == this.referenceDate.month();
    });

    return periodBookingDate != null;
  }

  getCompanyLogoUrl(bookingGroup: BookingGroup) {
    return AppConfig.api_url + "/image/" + bookingGroup.contract.logo;
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
    this.calculateBudget();
  }

  itemSelected(label: string, bookingGroups: AggregatedGroups) {
    this.navCtrl.push(BookingGroupPage, { label: label, bookingGroups: bookingGroups })
  }

}
