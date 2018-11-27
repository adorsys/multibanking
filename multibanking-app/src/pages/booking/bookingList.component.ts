import { Component, ViewChild, ElementRef } from "@angular/core";
import { NavController, AlertController, ToastController, NavParams, LoadingController, Navbar } from "ionic-angular";
import { BankAccountService } from "../../services/bankAccount.service";
import { BookingService } from "../../services/booking.service";
import { ImageService } from '../../services/image.service';
import { PaymentCreatePage } from "../payment/paymentCreate.component";
import { BookingDetailPage } from "../booking-detail/bookingDetail.component";
import { AnalyticsService } from "../../services/analytics.service";
import { Moment } from "moment";
import * as moment from 'moment';
import { BankAccess, ResourceBankAccount, AccountAnalyticsEntity, ExecutedBooking, Booking, BookingPeriod } from "../../model/multibanking/models";
import { Pageable } from "../../model/pageable";
import { AutoCompleteComponent } from "ionic2-auto-complete";
import { BookingAutoCompleteService } from "../../services/bookingAutoComplete.service";

@Component({
  selector: 'page-bookingList',
  templateUrl: 'bookingList.component.html'
})
export class BookingListPage {

  bankAccess: BankAccess;
  bankAccount: ResourceBankAccount;
  getLogo: Function;
  pageable: Pageable;
  bookingMonths: Moment[] = [];

  @ViewChild(AutoCompleteComponent) autocomplete: AutoCompleteComponent;
  @ViewChild(Navbar) navBar: Navbar;
  @ViewChild('headerTag') headerTag: ElementRef;
  @ViewChild('scrollableTag') scrollableTag: ElementRef;

  constructor(
    public navCtrl: NavController,
    public navparams: NavParams,
    private alertCtrl: AlertController,
    private toastCtrl: ToastController,
    private loadingCtrl: LoadingController,
    private bankAccountService: BankAccountService,
    public bookingAutoCompleteService: BookingAutoCompleteService,
    private bookingService: BookingService,
    private analyticsService: AnalyticsService,
    public logoService: ImageService
  ) {
    this.bankAccess = navparams.data.bankAccess;
    this.bankAccount = navparams.data.bankAccount;
    this.getLogo = logoService.getImage;
  }

  ngOnInit() {
    this.autocomplete.itemSelected.subscribe(booking => {
      let bookingMonth: any = moment(booking.bookingDate);
      bookingMonth.bookings = [booking];
      this.bookingMonths = [bookingMonth];
    });

    this.autocomplete.searchbarElem.ionClear.subscribe(() => {
      this.loadBookings();
    });

    if (!this.bankAccount.lastSync || moment(this.bankAccount.lastSync).isBefore(moment(), 'day')) {
      this.syncBookings(null);
    } else {
      this.loadBookings();
    }

    this.bankAccountService.bookingsChangedObservable.subscribe(changed => {
      this.loadBookings();
    });
  }

  ionViewDidEnter() {
    if (this.headerTag) {
      let offset = this.headerTag.nativeElement.offsetHeight;
      (<HTMLDivElement>this.scrollableTag.nativeElement).style.marginTop = offset + 'px';
    }
  }

  ionViewDidLoad() {
    this.navBar.backButtonClick = (e: UIEvent) => {
      this.navCtrl.parent.viewCtrl.dismiss();
    };
  }

  loadBookings() {
    this.bookingMonths = [];
    this.bookingService.getBookings(this.bankAccess.id, this.bankAccount.id).subscribe(
      response => {
        this.pageable = response;
        this.bookingsLoaded(response._embedded ? response._embedded.bookingEntityList : []);
        this.loadAnalytics();
        this.bookingAutoCompleteService.loadSearchIndex(this.bankAccess.id, this.bankAccount.id);
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

  loadAnalytics() {
    this.analyticsService.getAnalytics(this.bankAccess.id, this.bankAccount.id).subscribe(
      response => {
        this.evalForecastBookings(response)
      },
      messages => {
        if (messages instanceof Array) {
          messages.forEach(message => {
            if (message.key == "RESOURCE_NOT_FOUND") {
              //ignore
            } else if (message.key == "SYNC_IN_PROGRESS") {
              //ignore
            } else {
              this.alertCtrl.create({
                message: message.renderedMessage,
                buttons: ['OK']
              }).present();
            }
          });
        }
      }
    );
  }

  evalForecastBookings(analytics: AccountAnalyticsEntity) {
    let referenceDate = moment(analytics.analyticsDate);
    let nextMonth = referenceDate.clone().add(1, 'month');

    //collect bookings for current and next period
    let forecastBookings: ExecutedBooking[] = [];
    analytics.bookingGroups.forEach(group => {
      let currentPeriod = this.findPeriod(group.bookingPeriods, referenceDate.clone().startOf('month'), referenceDate.clone().endOf('month'));
      let nextPeriod = this.findPeriod(group.bookingPeriods, nextMonth.clone().startOf('month'), nextMonth.clone().endOf('month'));

      if (currentPeriod && currentPeriod.bookings) {
        forecastBookings = forecastBookings.concat(currentPeriod.bookings.filter(booking => !booking.executed));
      }
      if (nextPeriod && nextPeriod.bookings) {
        forecastBookings = forecastBookings.concat(nextPeriod.bookings.filter(booking => !booking.executed));
      }
    });

    let bookingIds = forecastBookings.map(booking => booking.bookingId);

    if (bookingIds.length > 0) {
      //map real bookings to forecast bookings
      this.bookingService.getBookingsByIds(this.bankAccess.id, this.bankAccount.id, bookingIds)
        .subscribe((bookings: Booking[]) => {
          bookings.forEach((loadedBooking: any) => {
            let forecastBooking = forecastBookings.find(booking => booking.bookingId == loadedBooking.id);
            loadedBooking.bookingDate = forecastBooking.executionDate;
            loadedBooking.forecastBooking = forecastBooking;
          });
          this.bookingsLoaded(bookings);
        })
    }
  }

  bookingsLoaded(bookings: Booking[]) {
    this.sortBookings(bookings).forEach(booking => {
      let bookingMonth: any = this.monthExist(moment(booking.bookingDate));
      if (!bookingMonth) {
        bookingMonth = moment(booking.bookingDate);
        bookingMonth.bookings = [];
        this.bookingMonths.push(bookingMonth);
        this.bookingMonths = this.sortBookingMonths(this.bookingMonths);
      }
      bookingMonth.bookings.push(booking);
      bookingMonth.bookings = this.sortBookings(bookingMonth.bookings);
    });
  }

  monthExist(month: Moment): Moment {
    return this.bookingMonths.find(bookingMonth => bookingMonth.isSame(month, 'month'));
  }

  sortBookingMonths(months: Moment[]): Moment[] {
    return months.sort((moment1: Moment, moment2: Moment) => {
      if (moment1.isAfter(moment2)) {
        return -1;
      } else {
        return 1;
      }
    });
  }

  sortBookings(bookings: Booking[]): Booking[] {
    return bookings.sort((booking1: Booking, booking2: Booking) => {
      if (moment(booking1.bookingDate).isAfter(booking2.bookingDate)) {
        return -1;
      } else {
        return 1;
      }
    });
  }

  findPeriod(periods: BookingPeriod[], referenceStart: Moment, referenceEnd: Moment): BookingPeriod {
    if (periods) {
      return periods.find((period: BookingPeriod) => {
        let periodEnd: Moment = moment(period.end);
        return periodEnd.isSameOrAfter(referenceStart, 'day') && periodEnd.isSameOrBefore(referenceEnd, 'day');
      });
    }
  }

  loadNextBookings(infiniteScroll) {
    if (this.pageable._links.next) {
      this.bookingService.getNextBookings(this.pageable._links.next.href).subscribe(response => {
        this.pageable = response;
        this.bookingsLoaded(response._embedded.bookingEntityList);

        infiniteScroll.complete();
      });
    } else {
      infiniteScroll.complete();
    }
  }

  doInfinite(infiniteScroll) {
    this.loadNextBookings(infiniteScroll);
  }

  getReceiver(booking: Booking): string {
    if (booking.bookingCategory && booking.bookingCategory.receiver) {
      return booking.bookingCategory.receiver;
    } else if (booking.otherAccount && booking.otherAccount.owner) {
      return booking.otherAccount.owner;
    }
    return "";
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
        loading.dismiss();
      })
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
