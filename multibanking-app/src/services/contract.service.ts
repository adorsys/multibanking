import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { HttpClient } from '@angular/common/http';
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

  handleError(error): Observable<any> {
    let errorJson = error.json();
    if (errorJson) {
      if (errorJson.message == "SYNC_IN_PROGRESS") {
        return Observable.throw(errorJson.message);
      } else {
        return Observable.throw(errorJson || 'Server error');
      }
    } else {
      return Observable.throw(error || 'Server error');
    }
  }
}
