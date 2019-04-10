import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subject } from "rxjs";
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ENV } from "../env/env";
import { ResourceBankAccount } from '../model/multibanking/models';
import { Consent } from '../model/multibanking/consent';

@Injectable()
export class BankAccountService {

  public bookingsChangedObservable = new Subject();

  constructor(private http: HttpClient) {
  }

  getBankAccounts(accessId: string): Observable<Array<ResourceBankAccount>> {
    return this.http.get(`${ENV.api_url}/bankaccesses/${accessId}/accounts`)
      .map((res: any) => res._embedded.bankAccountEntityList)
      .catch(this.handleError);
  }

  syncBookings(accessId: string, accountId: string, pin: string): Observable<Consent> {
    return this.http.put(`${ENV.api_url}/bankaccesses/${accessId}/accounts/${accountId}/sync`, pin)
      .map((consent: Consent) => {
        if (consent) {
          return consent;
        } else {
          this.bookingsChangedObservable.next(true);
        }
        
      })
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
