import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Contract } from '../api/Contract';
import { ENV } from "../env/env";

@Injectable()
export class ContractService {

  constructor(private http: HttpClient) { }

  getContracts(accessId: string, accountId: string): Observable<Array<Contract>> {
    return this.http.get(`${ENV.api_url}/bankaccesses/${accessId}/accounts/${accountId}/contracts`)
      .map((res: any) => res._embedded.contractEntityList)
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
