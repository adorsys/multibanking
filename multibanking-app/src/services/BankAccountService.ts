import {Injectable} from '@angular/core';
import {AppConfig} from '../app/app.config';
import {Http, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import 'rxjs/Rx';

@Injectable()
export class BankAccountService {

  constructor(private http: Http, private appConfig: AppConfig) {
  }

  getBankAccounts(userId, accessId) {
    return this.http.get(this.appConfig.API_URL+"/users/"+userId+"/bankaccesses/"+accessId+"/accounts")
      .map((res:Response) => res.json()._embedded.bankAccountEntityList)
      .catch(this.handleError);
  }

  syncBookings(userId, accessId, accountId, pin) {
    return this.http.put(this.appConfig.API_URL + "/users/" + userId + "/bankaccesses/" + accessId + "/accounts/" + accountId + "/sync", pin)
      .map((res: Response) => res.json()._embedded != null ? res.json()._embedded.bookingEntityList : [])
      .catch(this.handleError);
  }

  handleError(error) {
    console.error(error);
    return Observable.throw(error.json().error || 'Server error');
  }


}
