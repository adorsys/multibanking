import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ResourceBankAccount } from 'src/multibanking-api/resourceBankAccount';
import { AbstractService } from './abstract.service';
import { map, catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class BankAccountService extends AbstractService {

  getBankAccount(accessId: string, accountId: string): Observable<ResourceBankAccount> {
    return this.http.get(`${this.settings.apiUrl}/bankaccesses/${accessId}/accounts/${accountId}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  getBankAccounts(accessId: string): Observable<Array<ResourceBankAccount>> {
    return this.http.get(`${this.settings.apiUrl}/bankaccesses/${accessId}/accounts`)
      .pipe(
        map((res: any) => res._embedded.bankAccountList),
        catchError(this.handleError)
      );
  }

  syncBookings(accessId: string, accountId: string): Observable<any> {
    return this.http.put(`${this.settings.apiUrl}/bankaccesses/${accessId}/accounts/${accountId}/sync`, {})
      .pipe(
        catchError(this.handleError)
      );
  }

}
