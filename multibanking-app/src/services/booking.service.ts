import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Booking } from "../api/Booking";
import { Pageable } from '../api/Pageable';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ENV } from "../env/env";

@Injectable()
export class BookingService {

  constructor(private http: HttpClient) {
  }

  getBookingsByIds(accessId, accountId, bookingIds: string[]): Observable<Booking[]> {
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

  getBooking(accessId, accountId, bookingId): Observable<Booking> {
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

  handleError(error): Observable<any> {
    console.error(error);
    let errorJson = error.json();
    if (errorJson) {
      if (errorJson.message == "SYNC_IN_PROGRESS") {
        return Observable.throw(errorJson.message);
      }
      return Observable.throw(errorJson || 'Server error');
    }
    return Observable.throw(error || 'Server error');
  }


}
