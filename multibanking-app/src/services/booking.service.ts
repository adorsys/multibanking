import { Injectable } from '@angular/core';
import { AppConfig } from '../app/app.config';
import { Http, Response, ResponseContentType } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Booking } from "../api/Booking";

@Injectable()
export class BookingService {

  constructor(private http: Http) {
  }

  getBookings(accessId, accountId): Observable<Array<Booking>> {
    return this.http.get(`${AppConfig.api_url}/bankaccesses/${accessId}/accounts/${accountId}/bookings`)
      .map((res: Response) => res.json()._embedded != null ? res.json()._embedded.bookingEntityList : [])
      .catch(this.handleError);
  }

  getBooking(accessId, accountId, bookingId): Observable<Booking> {
    return this.http.get(`${AppConfig.api_url}/bankaccesses/${accessId}/accounts/${accountId}/bookings/${bookingId}`)
      .map((res: Response) => res.json()._embedded != null ? res.json()._embedded.bookingEntityList : [])
      .catch(this.handleError);
  }

  downloadBookings(accessId, accountId): Observable<any> {
    return this.http.get(`${AppConfig.api_url}/bankaccesses/${accessId}/accounts/${accountId}/bookings/download`,
      { responseType: ResponseContentType.Blob })
      .map(res => {
        return new Blob([res.blob()], { type: 'application/csv' })
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
