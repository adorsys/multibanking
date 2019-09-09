import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { ResourceBankAccess } from 'src/multibanking-api/resourceBankAccess';
import { environment } from 'src/environments/environment';
import { AbstractService } from './abstract.service';
import { map, catchError, finalize } from 'rxjs/operators';
import { ChangeEvent, EventType } from 'src/app/model/changeEvent';

@Injectable({
  providedIn: 'root',
})
export class BankAccessService extends AbstractService {

  public bankAccesscChangedObservable = new Subject<ChangeEvent<ResourceBankAccess>>();

  getBankAccesses(): Observable<ResourceBankAccess[]> {
    return this.http.get(`${environment.api_url}/bankaccesses`)
      .pipe(
        map((res: any) => {
          return res._embedded != null ? res._embedded.bankAccessList : [];
        }),
        catchError(this.handleError)
      );
  }

  getBankAccess(accessId: string): Observable<ResourceBankAccess> {
    return this.http.get(`${environment.api_url}/bankaccesses/${accessId}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  createBankAcccess(bankaccess: ResourceBankAccess): Observable<any> {
    return this.http.post(`${environment.api_url}/bankaccesses`, bankaccess)
      .pipe(
        catchError(this.handleError),
        finalize(() => this.bankAccesscChangedObservable.next({ data: bankaccess, eventType: EventType.Create }))
      );
  }

  updateBankAcccess(bankaccess: ResourceBankAccess): Observable<any> {
    return this.http.put(`${environment.api_url}/bankaccesses/${bankaccess.id}`, bankaccess)
      .pipe(
        catchError(this.handleError),
        finalize(() => this.bankAccesscChangedObservable.next({ data: bankaccess, eventType: EventType.Create }))
      );
  }

  deleteBankAccess(bankaccess: ResourceBankAccess): Observable<any> {
    return this.http.delete(`${environment.api_url}/bankaccesses/${bankaccess.id}`)
      .pipe(
        catchError(this.handleError),
        finalize(() => this.bankAccesscChangedObservable.next({ data: bankaccess, eventType: EventType.Create }))
      );
  }
}
