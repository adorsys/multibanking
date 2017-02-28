import {Injectable} from '@angular/core';
import {AppConfig} from '../app/app.config';
import {Http, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import 'rxjs/Rx';

@Injectable()
export class BankAccessService {

  constructor(private http: Http, private appConfig: AppConfig) {
  }

  getBankAccesses(userId) {
    return this.http.get(this.appConfig.API_URL+"/users/"+userId+"/bankaccesses")
      .map((res:Response) => res.json()._embedded != null ? res.json()._embedded.bankAccessEntityList : [])
      .catch(this.handleError);
  }

  crateBankAcccess(userId, bankaccess) {
    return this.http.post(this.appConfig.API_URL+"/users/"+userId+"/bankaccesses", bankaccess)
      .catch(this.handleError);
  }

  handleError(error) {
    console.error(error.json());
    return Observable.throw(error.json() || 'Server error');
  }


}
