import { Component, ViewChild } from "@angular/core";
import { NavController, AlertController, ToastController, NavParams, LoadingController, Navbar } from "ionic-angular";
import { BankAccountService } from "../../services/bankAccount.service";
import { BookingService } from "../../services/booking.service";
import { BankAccess } from "../../api/BankAccess";
import { Booking } from "../../api/Booking";
import { LogoService } from '../../services/logo.service';
import { PaymentCreatePage } from "../payment/paymentCreate.component";
import { BankAccount } from "../../api/BankAccount";
import { BookingDetailPage } from "../booking-detail/bookingDetail.component";
import { Pageable } from "../../api/Pageable";

@Component({
  selector: 'page-bookingList',
  templateUrl: 'bookingList.component.html'
})
export class BookingListPage {

  bankAccess: BankAccess;
  bankAccount: BankAccount;
  getLogo: Function;
  pageable: Pageable;
  bookings: Booking[];

  @ViewChild(Navbar) navBar: Navbar;

  constructor(
    public navCtrl: NavController,
    public navparams: NavParams,
    private alertCtrl: AlertController,
    private toastCtrl: ToastController,
    private loadingCtrl: LoadingController,
    private bankAccountService: BankAccountService,
    private bookingService: BookingService,
    public logoService: LogoService
  ) {
    this.bankAccess = navparams.data.bankAccess;
    this.bankAccount = navparams.data.bankAccount;
    this.getLogo = logoService.getLogo;
  }

  ngOnInit() {
    this.loadBookings();
  }

  ionViewDidLoad() {
    this.navBar.backButtonClick = (e: UIEvent) => {
      this.navCtrl.parent.viewCtrl.dismiss();
    };
  }

  loadBookings() {
    this.bookingService.getBookings(this.bankAccess.id, this.bankAccount.id).subscribe(
      response => {
        this.pageable = response;
        this.bookings = response._embedded ? response._embedded.bookingEntityList : [];
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

  loadNextBookings(infiniteScroll) {
    if (this.pageable._links.next) {
      this.bookingService.getNextBookings(this.pageable._links.next.href).subscribe(response => {
        this.pageable = response;
        this.bookings = this.bookings.concat(response._embedded.bookingEntityList);
        
        infiniteScroll.complete();
      });
    } else {
      infiniteScroll.complete();
    }
  }

  doInfinite(infiniteScroll) {
    this.loadNextBookings(infiniteScroll);
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

    this.bankAccountService.syncBookings(this.bankAccess.id, this.bankAccount.id, pin).subscribe(
      response => {
        this.loadBookings();
        loading.dismiss();
      },
      error => {
        loading.dismiss();
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
      });
  }

  downloadBookings() {
    this.bookingService.downloadBookings(this.bankAccess.id, this.bankAccount.id).subscribe(data => {
      this.showFile(data);
    });
  }

  showFile(blob) {
    // It is necessary to create a new blob object with mime-type explicitly set
    // otherwise only Chrome works like it should
    let newBlob = new Blob([blob], { type: "application/vnd.ms-excel" })

    // IE doesn't allow using a blob object directly as link href
    // instead it is necessary to use msSaveOrOpenBlob
    if (window.navigator && window.navigator.msSaveOrOpenBlob) {
      window.navigator.msSaveOrOpenBlob(newBlob);
      return;
    }

    // For other browsers:
    // Create a link pointing to the ObjectURL containing the blob.
    const data = window.URL.createObjectURL(newBlob);
    let link = document.createElement('a');
    link.href = data;
    link.download = "bookings.csv";
    link.click();
    setTimeout(() =>
      // For Firefox it is necessary to delay revoking the ObjectURL
      window.URL.revokeObjectURL(data), 100)
  }

  itemSelected(booking) {
    this.navCtrl.push(BookingDetailPage, {
      booking: booking
    });
  }

  createPayment() {
    this.navCtrl.push(PaymentCreatePage, {
      bankAccount: this.bankAccount,
      bankAccess: this.bankAccess
    });
  }
}
