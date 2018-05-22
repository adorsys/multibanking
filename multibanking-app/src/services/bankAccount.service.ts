import { Injectable } from '@angular/core';
import { AppConfig } from '../app/app.config';
import { Observable } from 'rxjs/Observable';
import { Subject } from "rxjs";
import { BankAccount } from "../api/BankAccount";
import { Booking } from "../api/Booking";
import { HttpClient } from '@angular/common/http';

@Injectable()
export class BankAccountService {

  public bookingsChangedObservable = new Subject();

  constructor(private http: HttpClient) {
  }

  getBankAccounts(accessId: string): Observable<Array<BankAccount>> {
    return this.http.get(AppConfig.api_url + "/bankaccesses/" + accessId + "/accounts")
      .map((res: any) => res._embedded.bankAccountEntityList)
      .catch(this.handleError);
  }

  syncBookings(accessId: string, accountId: string, pin: string): Observable<Array<Booking>> {
    return this.http.put(AppConfig.api_url + "/bankaccesses/" + accessId + "/accounts/" + accountId + "/sync", pin)
      .map((res: Response) => {
        this.bookingsChangedObservable.next(true);
      })
      .catch(this.handleError);
  }

  handleError(error) {
    console.error(error);
    let errorJson = error.json();
    if (errorJson) {
      return Observable.throw(errorJson || 'Server error');
    }

    return Observable.throw(error || 'Server error');
  }



}
