import {Injectable} from '@angular/core';
import {AppConfig} from '../app/app.config';
import {Http, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import 'rxjs/Rx';
import {Subject} from "rxjs";

@Injectable()
export class BankAccountService {

  public bookingsChangedObservable = new Subject();

  constructor(private http: Http) {
  }

  getBankAccounts(userId, accessId) {
    return this.http.get(AppConfig.api_url + userId + "/bankaccesses/" + accessId + "/accounts")
      .map((res: Response) => res.json()._embedded.bankAccountEntityList)
      .catch(this.handleError);
  }

  syncBookings(userId, accessId, accountId, pin) {
    return this.http.put(AppConfig.api_url + userId + "/bankaccesses/" + accessId + "/accounts/" + accountId + "/sync", pin)
      .map((res: Response) => {
        this.bookingsChangedObservable.next(true);

        res.json()._embedded != null ? res.json()._embedded.bookingEntityList : []
      })
      .catch(this.handleError);
  }

  handleError(error) {
    console.error(error);
    return Observable.throw(error.json().error || 'Server error');
  }


}
