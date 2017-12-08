import { Component, ViewChild } from "@angular/core";
import { AlertController, ToastController, NavParams, LoadingController, NavController, Navbar } from "ionic-angular";
import { BankAccountService } from "../../services/bankAccountService";
import { AnalyticsService } from "../../services/analyticsService";
import { BankAccess } from "../../api/BankAccess";
import { AccountAnalytics } from "../../api/AccountAnalytics";
import { BookingGroup } from "../../api/BookingGroup";
import { AppConfig } from "../../app/app.config";
import { BookingGroupPage } from "./bookingGroup";

@Component({
  selector: 'page-analytics',
  templateUrl: 'analytics.html'
})
export class AnalyticsPage {

  analytics: AccountAnalytics;
  bankAccess: BankAccess;
  bankAccountId: string;

  @ViewChild(Navbar) navBar: Navbar;

  constructor(
    public navCtrl: NavController,
    public navparams: NavParams,
    private alertCtrl: AlertController,
    private toastCtrl: ToastController,
    private loadingCtrl: LoadingController,
    private bankAccountService: BankAccountService,
    private analyticsService: AnalyticsService) {

    this.bankAccess = navparams.data.bankAccess;
    this.bankAccountId = navparams.data.bankAccount.id;
  }

  ngOnInit() {
    this.bankAccountService.bookingsChangedObservable.subscribe(changed => {
      this.loadAnalytics();
    })
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
      },
      error => {
        if (error == "SYNC_IN_PROGRESS") {
          this.toastCtrl.create({
            message: 'Account sync in progress',
            showCloseButton: true,
            position: 'top'
          }).present();
        }
      })
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

  itemSelected(label: string, bookingGroups: Array<BookingGroup>) {
    this.navCtrl.push(BookingGroupPage, { label: label, bookingGroups: bookingGroups })
  }

}
