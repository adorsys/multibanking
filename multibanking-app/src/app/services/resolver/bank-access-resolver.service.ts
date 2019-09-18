import { Injectable } from '@angular/core';
import { ResourceBankAccess } from 'src/multibanking-api/resourceBankAccess';
import { BankAccessService } from '../rest/bankAccess.service';
import { Router, ActivatedRouteSnapshot } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { take, mergeMap } from 'rxjs/operators';
import { EventType, ChangeEvent } from 'src/app/model/changeEvent';

@Injectable({
  providedIn: 'root'
})
export class BankAccessResolverService {

  private selectedBankAccess: ResourceBankAccess;

  constructor(private bankAccessService: BankAccessService,
              private router: Router) {
    this.bankAccessService.bankAccesscChangedObservable.subscribe((changeEvent: ChangeEvent<ResourceBankAccess>) => {
      if (changeEvent.eventType === EventType.Update) {
        this.selectedBankAccess = changeEvent.data;
      } else if (changeEvent.eventType === EventType.Delete
        && this.selectedBankAccess && this.selectedBankAccess.id === changeEvent.data.id) {
          this.selectedBankAccess = undefined;
      }
    });
  }

  resolve(route: ActivatedRouteSnapshot): Observable<ResourceBankAccess> | Observable<never> {
    return this.getBankAccess(route.paramMap.get('access-id'));
  }

  public getBankAccess(accessId: string) {
    if (this.selectedBankAccess && accessId === this.selectedBankAccess.id) {
      return of(this.selectedBankAccess);
    }

    if (!accessId) {
      return EMPTY;
    }

    console.log('load bank access');

    return this.bankAccessService.getBankAccess(accessId).pipe(
      take(1),
      mergeMap(bankAccess => {
        if (bankAccess) {
          this.selectedBankAccess = bankAccess;
          return of(bankAccess);
        } else { // id not found
          this.router.navigate(['/']);
          return EMPTY;
        }
      })
    );
  }
}
