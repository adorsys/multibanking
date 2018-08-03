import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ENV } from "../env/env";
import { ResourceBankAccess } from '../model/multibanking/models';

@Injectable()
export class BankAccessService {

  public bankAccessDeletedObservable = new Subject();

  constructor(private http: HttpClient) {
  }

  getBankAccesses(): Observable<Array<ResourceBankAccess>> {
    return this.http.get(`${ENV.api_url}/bankaccesses`)
      .map((res: any) => {
        return res._embedded != null ? res._embedded.bankAccessEntityList : []
      })
      .catch(this.handleError);
  }

  createBankAcccess(bankaccess: ResourceBankAccess): Observable<Object> {
    return this.http.post(`${ENV.api_url}/bankaccesses`, bankaccess, { responseType: 'text' })
      .catch(this.handleError);
  }

  updateBankAcccess(bankaccess: ResourceBankAccess): Observable<any> {
    return this.http.put(`${ENV.api_url}/bankaccesses/${bankaccess.id}`, bankaccess)
      .catch(this.handleError);
  }

  deleteBankAccess(accessId): Observable<any> {
    return this.http.delete(`${ENV.api_url}/bankaccesses/${accessId}`)
      .catch(this.handleError)
      .finally(() => this.bankAccessDeletedObservable.next(accessId));
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
