import {Injectable} from '@angular/core';
import {AppConfig} from '../app/app.config';
import {Http, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import 'rxjs/Rx';

@Injectable()
export class BankAccessService {

  constructor(private http: Http) {
  }

  getBankAccesses(userId) {
    return this.http.get(AppConfig.api_url + userId + "/bankaccesses")
      .map((res: Response) => res.json()._embedded != null ? res.json()._embedded.bankAccessEntityList : [])
      .catch(this.handleError);
  }

  crateBankAcccess(userId, bankaccess) {
    return this.http.post(AppConfig.api_url + userId + "/bankaccesses", bankaccess)
      .catch(this.handleError);
  }

  handleError(error) {
    console.error(error.json());
    return Observable.throw(error.json() || 'Server error');
  }


}
