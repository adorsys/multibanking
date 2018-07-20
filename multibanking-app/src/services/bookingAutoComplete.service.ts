import { Injectable } from '@angular/core';
import { AutoCompleteService } from "ionic2-auto-complete";
import { BookingService } from './booking.service';
import { Booking } from 'model/multibanking/models';
import { Moment } from "moment";
import * as moment from 'moment';
import { CurrencyPipe } from '@angular/common';

@Injectable()
export class BookingAutoCompleteService implements AutoCompleteService {

  labelAttribute = "name";
  bankAccessId: string;
  bankAccountId: string;
  searchIndex: { [key: string]: Array<string> };

  constructor(private bookingService: BookingService,
    private cp: CurrencyPipe) {
  }

  getResults(term: string) {
    if (term && term.length > 2 && this.searchIndex) {
      let bookingIds: string[] = Object.keys(this.searchIndex).filter((key) => {
        return this.searchIndex[key].find((index: string) => {
          return index.toLocaleLowerCase().search(term.toLocaleLowerCase()) > -1;
        })
      })

      if (bookingIds && bookingIds.length > 0) {
        return this.bookingService.getBookingsByIds(this.bankAccessId, this.bankAccountId, bookingIds)
      }
    }

    return [];
  }

  getItemLabel(booking: Booking): any {
    let receiver;
    if (booking.bookingCategory && booking.bookingCategory.receiver) {
      receiver = booking.bookingCategory.receiver;
    } else if (booking.otherAccount && booking.otherAccount.owner) {
      receiver = booking.otherAccount.owner;
    }
    return moment(booking.bookingDate).format('DD.MM.YYYY') + " - " + receiver + " " + this.cp.transform(booking.amount, 'EUR', 'symbol');
  }



  loadSearchIndex(bankAccessId, bankAccountId) {
    this.bankAccessId = bankAccessId;
    this.bankAccountId = bankAccountId;
    this.bookingService.getSearchIndex(bankAccessId, bankAccountId).subscribe(
      response => {
        this.searchIndex = response;
      },
      error => {
        //ignore
      }
    );

  }
}
