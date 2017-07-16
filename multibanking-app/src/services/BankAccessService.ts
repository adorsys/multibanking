import { Injectable } from '@angular/core';
import { AppConfig } from '../app/app.config';
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/Rx';

@Injectable()
export class BankAccessService {

  constructor(private http: Http) {
  }

  getBankAccesses(): Observable<any> {
    return this.http.get(AppConfig.api_url + "/bankaccesses")
      .map((res: Response) => res.json()._embedded != null ? res.json()._embedded.bankAccessEntityList : [])
      .catch(this.handleError);
  }

  createBankAcccess(bankaccess): Observable<any> {
    return this.http.post(AppConfig.api_url + "/bankaccesses", bankaccess)
      .catch(this.handleError);
  }

  updateBankAcccess(bankaccess): Observable<any> {
    return this.http.put(AppConfig.api_url + "/bankaccesses/" + bankaccess.id, bankaccess)
      .catch(this.handleError);
  }

  deleteBankAccess(accessId): Observable<any> {
    return this.http.delete(AppConfig.api_url + "/bankaccesses/" + accessId)
      .catch(this.handleError);
  }

  handleError(error) {
    console.error(error);
    let errorJson = error.json();
    if (errorJson) {
      return Observable.throw(errorJson || 'Server error');
    } else {
      return Observable.throw(error || 'Server error');
    }
  }


}
