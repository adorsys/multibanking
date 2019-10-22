import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { ContractTO } from './../../../multibanking-api/contractTO';
import { AbstractService } from './abstract.service';

@Injectable({
  providedIn: 'root',
})
export class ContractService extends AbstractService {

  getContracts(accessId: string, accountId: string): Observable<Array<ContractTO>> {
    return this.http.get(`${this.settings.apiUrl}/bankaccesses/${accessId}/accounts/${accountId}/contracts`)
      .pipe(
        map((res: any) => res._embedded != null ? res._embedded.contractList : []),
        catchError(this.handleError)
      );
  }

}
