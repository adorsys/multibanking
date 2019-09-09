import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { ResourceBankAccount } from 'src/multibanking-api/resourceBankAccount';
import { AbstractService } from './abstract.service';
import { map, catchError, finalize } from 'rxjs/operators';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root',
})
export class BankAccountService extends AbstractService {

  public bookingsChangedObservable = new Subject();

  getBankAccount(accessId: string, accountId: string): Observable<ResourceBankAccount> {
    return this.http.get(`${environment.api_url}/bankaccesses/${accessId}/accounts/${accountId}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  getBankAccounts(accessId: string): Observable<Array<ResourceBankAccount>> {
    return this.http.get(`${environment.api_url}/bankaccesses/${accessId}/accounts`)
      .pipe(
        map((res: any) => res._embedded.bankAccountList),
        catchError(this.handleError)
      );
  }

  syncBookings(accessId: string, accountId: string): Observable<any> {
    return this.http.put(`${environment.api_url}/bankaccesses/${accessId}/accounts/${accountId}/sync`, {})
      .pipe(
        catchError(this.handleError),
        finalize(() => this.bookingsChangedObservable.next(true))
      );
  }

}
