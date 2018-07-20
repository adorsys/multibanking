import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { ENV } from "../env/env";
import { ResourceBooking } from '../model/multibanking/models';
import { Pageable } from '../model/pageable';
import { BookingsIndexEntity } from 'model/multibanking/bookingsIndexEntity';

@Injectable()
export class BookingService {

  constructor(private http: HttpClient) {
  }

  getBookingsByIds(accessId, accountId, bookingIds: string[]): Observable<ResourceBooking[]> {
    const params = new HttpParams({
      fromObject: {
        ids: bookingIds,
      }
    });

    return this.http.get(`${ENV.api_url}/bankaccesses/${accessId}/accounts/${accountId}/bookings`, { params: params })
      .map((res: any) => res._embedded != null ? res._embedded.bookingEntityList : [])
      .catch(this.handleError);
  }

  getBookings(accessId, accountId): Observable<Pageable> {
    return this.http.get(`${ENV.api_url}/bankaccesses/${accessId}/accounts/${accountId}/bookings`)
      .catch(this.handleError);
  }

  getNextBookings(url: string): Observable<Pageable> {
    return this.http.get(url)
      .catch(this.handleError);
  }

  getBooking(accessId, accountId, bookingId): Observable<ResourceBooking> {
    return this.http.get(`${ENV.api_url}/bankaccesses/${accessId}/accounts/${accountId}/bookings/${bookingId}`)
      .map((res: any) => res._embedded != null ? res._embedded.bookingEntityList : [])
      .catch(this.handleError);
  }

  downloadBookings(accessId, accountId): Observable<any> {
    return this.http.get(`${ENV.api_url}/bankaccesses/${accessId}/accounts/${accountId}/bookings/download`,
      { responseType: 'blob' })
      .map(res => {
        return new Blob([res], { type: 'application/csv' })
      })
      .catch(this.handleError);
  }

  getSearchIndex(accessId, accountId): Observable<{ [key: string]: Array<string> }> {
    return this.http.get(`${ENV.api_url}/bankaccesses/${accessId}/accounts/${accountId}/bookings/index`)
      .map((res: any) => res != null ? res.bookingIdSearchList : {})
      .catch(this.handleError);
  }

  handleError(error: HttpErrorResponse): Observable<any> {
    console.error(error);
    let result: Observable<any>;
    if (error.error) {
      if (error.error.messages) {
        result = Observable.throw(error.error.messages);
      } else {
        result = Observable.throw(JSON.parse(error.error).messages);
      }
    } else {
      result = Observable.throw(error || 'Server error');
    }
    return result;
  }


}
