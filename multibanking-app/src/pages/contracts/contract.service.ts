import { Injectable } from '@angular/core';
import { AppConfig } from '../../app/app.config';
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/Rx';
import { Contract } from './contract.model';

@Injectable()
export class ContractService {

  constructor(private http: Http) {}

  getContracts(accessId: string, accountId: string): Observable<Array<Contract>> {
    return this.http.get(`${AppConfig.api_url}/bankaccesses/${accessId}/accounts/${accountId}/contracts`)
      .map((res: Response) => res.json()._embedded.contractResponseList)
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
