import {Injectable} from '@angular/core';
import {AppConfig} from '../app/app.config';
import {Http, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import 'rxjs/Rx';

@Injectable()
export class BookingService {

  constructor(private http: Http) {
  }

  getBookings(userId, accessId, accountId) {
    return this.http.get(AppConfig.api_url + "/users/" + userId + "/bankaccesses/" + accessId + "/accounts/" + accountId + "/bookings")
      .map((res: Response) => res.json()._embedded != null ? res.json()._embedded.bookingEntityList : [])
      .catch(this.handleError);
  }

  handleError(error) {
    console.error(error);
    return Observable.throw(error.json().error || 'Server error');
  }


}
