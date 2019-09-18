import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BankAccountService } from 'src/app/services/rest/bankAccount.service';
import { ResourceBankAccount } from 'src/multibanking-api/resourceBankAccount';
import * as moment from 'moment';
import { Moment } from 'moment';
import { Pageable } from 'src/app/model/pageable';
import { BookingService } from 'src/app/services/rest/booking.service';
import { ToastController, AlertController, LoadingController } from '@ionic/angular';
import { Booking } from 'src/multibanking-api/booking';
import { ExecutedBookingTO } from 'src/multibanking-api/executedBookingTO';
import { BookingPeriodTO } from 'src/multibanking-api/bookingPeriodTO';
import { AnalyticsService } from 'src/app/services/rest/analytics.service';
import { ResourceAnalyticsTO } from 'src/multibanking-api/resourceAnalyticsTO';
import { ImagesService } from 'src/app/services/rest/images.service';
import { ResourceUpdateAuthResponseTO } from 'src/multibanking-api/resourceUpdateAuthResponseTO';
import { Link } from 'src/multibanking-api/link';
import { ConsentService } from 'src/app/services/rest/consent.service';

@Component({
  selector: 'app-booking-list',
  templateUrl: './booking-list.page.html',
  styleUrls: ['./booking-list.page.scss'],
})
export class BookingListPage implements OnInit {

  bankAccessId: string;
  bankAccount: ResourceBankAccount;
  getLogo: (image: string) => string;
  pageable: Pageable;
  bookingMonths: Moment[] = [];

  constructor(private activatedRoute: ActivatedRoute,
              private bankAccountService: BankAccountService,
              private bookingService: BookingService,
              private analyticsService: AnalyticsService,
              private consentService: ConsentService,
              private toastController: ToastController,
              private alertController: AlertController,
              private loadingController: LoadingController,
              imagesService: ImagesService) {
    this.getLogo = imagesService.getImage;
  }

  ngOnInit() {
    this.bankAccessId = this.activatedRoute.snapshot.paramMap.get('access-id');
    this.bankAccount = this.activatedRoute.snapshot.data.bankAccount;

    if (!this.bankAccount.lastSync || moment(this.bankAccount.lastSync).isBefore(moment(), 'day')) {
      this.syncBookings();
    } else {
      this.loadBookings();
    }
  }

  loadBookings() {
    this.bookingMonths = [];
    this.bookingService.getBookings(this.bankAccessId, this.bankAccount.id).subscribe(
      response => {
        this.pageable = response;
        this.bookingsLoaded(response._embedded ? response._embedded.bookingList : []);
        this.loadAnalytics();
      },
      messages => {
        if (messages instanceof Array) {
          messages.forEach(async message => {
            if (message.key === 'SYNC_IN_PROGRESS') {
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
      });
  }

  loadAnalytics() {
    this.analyticsService.getAnalytics(this.bankAccessId, this.bankAccount.id).subscribe(
      response => {
        this.evalForecastBookings(response);
      },
      messages => {
        if (messages instanceof Array) {
          messages.forEach(async message => {
            if (message.key === 'RESOURCE_NOT_FOUND') {
              // ignore
            } else if (message.key === 'SYNC_IN_PROGRESS') {
              // ignore
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

  evalForecastBookings(analytics: ResourceAnalyticsTO) {
    const referenceDate = moment(analytics.analyticsDate);
    const nextMonth = referenceDate.clone().add(1, 'month');

    // collect bookings for current and next period
    let forecastBookings: ExecutedBookingTO[] = [];
    analytics.bookingGroups.forEach(group => {
      const currentPeriod = this.findPeriod(group.bookingPeriods, referenceDate.clone().startOf('month'),
        referenceDate.clone().endOf('month'));
      const nextPeriod = this.findPeriod(group.bookingPeriods, nextMonth.clone().startOf('month'), nextMonth.clone().endOf('month'));

      if (currentPeriod && currentPeriod.bookings) {
        forecastBookings = forecastBookings.concat(currentPeriod.bookings.filter(booking => !booking.executed));
      }
      if (nextPeriod && nextPeriod.bookings) {
        forecastBookings = forecastBookings.concat(nextPeriod.bookings.filter(booking => !booking.executed));
      }
    });

    const bookingIds = forecastBookings.map(booking => booking.bookingId);

    if (bookingIds.length > 0) {
      // map real bookings to forecast bookings
      this.bookingService.getBookingsByIds(this.bankAccessId, this.bankAccount.id, bookingIds)
        .subscribe((bookings: Booking[]) => {
          bookings.forEach((loadedBooking: any) => {
            const forecastBooking = forecastBookings.find(booking => booking.bookingId === loadedBooking.id);
            loadedBooking.bookingDate = forecastBooking.executionDate;
            loadedBooking.forecastBooking = forecastBooking;
          });
          this.bookingsLoaded(bookings);
        });
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

  findPeriod(periods: BookingPeriodTO[], referenceStart: Moment, referenceEnd: Moment): BookingPeriodTO {
    if (periods) {
      return periods.find((period: BookingPeriodTO) => {
        const periodEnd: Moment = moment(period.end);
        return periodEnd.isSameOrAfter(referenceStart, 'day') && periodEnd.isSameOrBefore(referenceEnd, 'day');
      });
    }
  }

  loadNextBookings(infiniteScroll) {
    if (this.pageable._links.next) {
      this.bookingService.getNextBookings(this.pageable._links.next.href).subscribe(response => {
        this.pageable = response;
        this.bookingsLoaded(response._embedded.bookingList);

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
    return '';
  }


  async syncBookings() {
    const loading = await this.loadingController.create({
      message: 'Please wait...'
    });
    loading.present();

    this.bankAccountService.syncBookings(this.bankAccessId, this.bankAccount.id).subscribe(
      (response) => {
        loading.dismiss();
        if (response && response.challenge) {
          this.presentTanPrompt(response);
        } else {
          this.loadBookings();
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

  downloadBookings() {
    this.bookingService.downloadBookings(this.bankAccessId, this.bankAccount.id).subscribe(data => {
      this.showFile(data);
    });
  }

  showFile(blob) {
    // It is necessary to create a new blob object with mime-type explicitly set
    // otherwise only Chrome works like it should
    const newBlob = new Blob([blob], { type: 'application/vnd.ms-excel' });

    // IE doesn't allow using a blob object directly as link href
    // instead it is necessary to use msSaveOrOpenBlob
    if (window.navigator && window.navigator.msSaveOrOpenBlob) {
      window.navigator.msSaveOrOpenBlob(newBlob);
      return;
    }

    // For other browsers:
    // Create a link pointing to the ObjectURL containing the blob.
    const data = window.URL.createObjectURL(newBlob);
    const link = document.createElement('a');
    link.href = data;
    link.download = 'bookings.csv';
    link.click();
    setTimeout(() =>
      // For Firefox it is necessary to delay revoking the ObjectURL
      window.URL.revokeObjectURL(data), 100);
  }

  async presentToast(opts) {
    const toast = await this.toastController.create(opts);
    toast.present();
  }

  async presentTanPrompt(consentAuthStatus: ResourceUpdateAuthResponseTO) {
    const alert = await this.alertController.create({
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
            this.submitTan(consentAuthStatus, data.TAN);
          }
        }
      ]
    });

    await alert.present();
  }

  public submitTan(consentAuthStatus: ResourceUpdateAuthResponseTO, tan: string) {
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
}
