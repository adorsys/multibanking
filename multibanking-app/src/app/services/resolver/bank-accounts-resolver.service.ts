import { Injectable } from '@angular/core';
import { ResourceBankAccount } from 'src/multibanking-api/resourceBankAccount';
import { BankAccessService } from '../rest/bankAccess.service';
import { BankAccountService } from '../rest/bankAccount.service';
import { Router, ActivatedRouteSnapshot } from '@angular/router';
import { ChangeEvent, EventType } from 'src/app/model/changeEvent';
import { ResourceBankAccess } from 'src/multibanking-api/resourceBankAccess';
import { Observable, EMPTY, of } from 'rxjs';
import { take, mergeMap } from 'rxjs/operators';

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
    const accessId = route.paramMap.get('access-id');
    const accountId = route.paramMap.get('account-id');
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
