import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap, take } from 'rxjs/operators';
import { ResourceBankAccount } from 'src/multibanking-api/resourceBankAccount';

import { BankAccountService } from '../rest/bankAccount.service';
import { getHierarchicalRouteParam } from '../../utils/utils';

@Injectable({
  providedIn: 'root'
})
export class BankAccountsResolverService {

  private accessId: string;
  private bankAccounts: ResourceBankAccount[];

  constructor(private bankAccountService: BankAccountService,
              private router: Router) {
  }

  resolve(route: ActivatedRouteSnapshot): Observable<ResourceBankAccount> | Observable<ResourceBankAccount[]> | Observable<never> {
    const accessId = getHierarchicalRouteParam(route, 'access-id');
    const accountId = getHierarchicalRouteParam(route, 'account-id');
    if (!accountId) {
      return this.getBankAccounts(accessId);
    } else {
      return this.getBankAccount(accessId, accountId);
    }
  }

  public getBankAccount(accessId: string, accountId: string): Observable<ResourceBankAccount> {
    if (this.accessId && this.accessId === accessId) {
      return of(this.bankAccounts.filter((account) => account.id === accountId)[0]);
    }

    return this.bankAccountService.getBankAccount(accessId, accountId);
  }

  public getBankAccounts(accessId: string): Observable<ResourceBankAccount[]> {
    if (this.accessId && this.accessId === accessId) {
      return of(this.bankAccounts);
    }

    if (!accessId) {
      return EMPTY;
    }

    console.log('load bank accounts');

    return this.bankAccountService.getBankAccounts(accessId).pipe(
      take(1),
      mergeMap(bankAccounts => {
        if (bankAccounts) {
          this.accessId = accessId;
          this.bankAccounts = bankAccounts;
          return of(bankAccounts);
        } else { // id not found
          this.router.navigate(['/']);
          return EMPTY;
        }
      })
    );
  }
}
