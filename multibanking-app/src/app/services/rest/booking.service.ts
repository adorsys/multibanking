import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ResourceBooking } from 'src/multibanking-api/resourceBooking';
import { environment } from 'src/environments/environment';
import { AbstractService } from './abstract.service';
import { map, catchError } from 'rxjs/operators';
import { Pageable } from 'src/app/model/pageable';

@Injectable({
  providedIn: 'root',
})
export class BookingService extends AbstractService {

  getBookingsByIds(accessId, accountId, bookingIds: string[]): Observable<ResourceBooking[]> {
    const params = new HttpParams({
      fromObject: {
        ids: bookingIds,
      }
    });

    return this.http.get(`${environment.api_url}/bankaccesses/${accessId}/accounts/${accountId}/bookings`, { params })
      .pipe(
        map((res: any) => res._embedded != null ? res._embedded.bookingList : []),
        catchError(this.handleError)
      );
  }

  getBookings(accessId, accountId): Observable<Pageable> {
    return this.http.get(`${environment.api_url}/bankaccesses/${accessId}/accounts/${accountId}/bookings`)
      .pipe(
        catchError(this.handleError)
      );
  }

  getNextBookings(url: string): Observable<Pageable> {
    return this.http.get(url)
      .pipe(
        catchError(this.handleError)
      );
  }

  getBooking(accessId, accountId, bookingId): Observable<ResourceBooking> {
    return this.http.get(`${environment.api_url}/bankaccesses/${accessId}/accounts/${accountId}/bookings/${bookingId}`)
      .pipe(
        map((res: any) => res._embedded != null ? res._embedded.bookingList : []),
        catchError(this.handleError)
      );
  }

  downloadBookings(accessId, accountId): Observable<any> {
    return this.http.get(`${environment.api_url}/bankaccesses/${accessId}/accounts/${accountId}/bookings/download`,
      { responseType: 'blob' })
      .pipe(
        map(res => {
          return new Blob([res], { type: 'application/csv' });
        }),
        catchError(this.handleError)
      );
  }

  getSearchIndex(accessId, accountId): Observable<{ [key: string]: Array<string> }> {
    return this.http.get(`${environment.api_url}/bankaccesses/${accessId}/accounts/${accountId}/bookings/index`)
      .pipe(
        map((res: any) => res != null ? res.bookingIdSearchList : {}),
        catchError(this.handleError)
      );
  }

}
