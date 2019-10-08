import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { ResourceBankAccess } from 'src/multibanking-api/resourceBankAccess';
import { AbstractService } from './abstract.service';
import { map, catchError, finalize } from 'rxjs/operators';
import { ChangeEvent, EventType } from 'src/app/model/changeEvent';

@Injectable({
  providedIn: 'root',
})
export class BankAccessService extends AbstractService {

  public bankAccesscChangedObservable = new Subject<ChangeEvent<ResourceBankAccess>>();

  getBankAccesses(): Observable<ResourceBankAccess[]> {
    return this.http.get(`${this.settings.apiUrl}/bankaccesses`)
      .pipe(
        map((res: any) => {
          return res._embedded != null ? res._embedded.bankAccessList : [];
        }),
        catchError(this.handleError)
      );
  }

  getBankAccess(accessId: string): Observable<ResourceBankAccess> {
    return this.http.get(`${this.settings.apiUrl}/bankaccesses/${accessId}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  createBankAcccess(bankaccess: ResourceBankAccess): Observable<any> {
    return this.http.post(`${this.settings.apiUrl}/bankaccesses`, bankaccess)
      .pipe(
        catchError(this.handleError),
        finalize(() => this.bankAccesscChangedObservable.next({ data: bankaccess, eventType: EventType.Create }))
      );
  }

  updateBankAcccess(bankaccess: ResourceBankAccess): Observable<any> {
    return this.http.put(`${this.settings.apiUrl}/bankaccesses/${bankaccess.id}`, bankaccess)
      .pipe(
        catchError(this.handleError),
        finalize(() => this.bankAccesscChangedObservable.next({ data: bankaccess, eventType: EventType.Create }))
      );
  }

  deleteBankAccess(bankaccess: ResourceBankAccess): Observable<any> {
    return this.http.delete(`${this.settings.apiUrl}/bankaccesses/${bankaccess.id}`)
      .pipe(
        catchError(this.handleError),
        finalize(() => this.bankAccesscChangedObservable.next({ data: bankaccess, eventType: EventType.Create }))
      );
  }
}
