import { Injectable } from '@angular/core';
import { AbstractService } from './abstract.service';
import { ResourceConsentTO } from 'src/multibanking-api/resourceConsentTO';
import { environment } from 'src/environments/environment.dev';
import { catchError, finalize, take, mergeMap } from 'rxjs/operators';
import { Observable, Subject, of } from 'rxjs';
import { ResourceUpdateAuthResponseTO } from 'src/multibanking-api/resourceUpdateAuthResponseTO';
import { UpdatePsuAuthenticationRequestTO } from 'src/multibanking-api/updatePsuAuthenticationRequestTO';
import { ChangeEvent, EventType } from 'src/app/model/changeEvent';
import { SelectPsuAuthenticationMethodRequestTO } from 'src/multibanking-api/selectPsuAuthenticationMethodRequestTO';
import { ConsentTO } from 'src/multibanking-api/consentTO';

@Injectable({
  providedIn: 'root'
})
export class ConsentService extends AbstractService {

  public consentStatusChangedObservable = new Subject<ChangeEvent<ResourceUpdateAuthResponseTO>>();

  createConsent(consent: ConsentTO): Observable<any> {
    return this.http.post(`${environment.api_url}/consents`, consent)
      .pipe(
        catchError(this.handleError),
        finalize(() => this.consentStatusChangedObservable.next({ data: undefined, eventType: EventType.Delete }))
      );
  }

  getConsent(consentId: string): Observable<ResourceConsentTO> {
    return this.http.get(`${environment.api_url}/consents/${consentId}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  getConsentByRedirectId(redirectId: string): Observable<ResourceConsentTO> {
    return this.http.get(`${environment.api_url}/consents/redirect/${redirectId}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  getAuthorisationStatus(consentId: string, authorisationId: string): Observable<ResourceUpdateAuthResponseTO> {
    return this.http.get(`${environment.api_url}/consents/${consentId}/authorisations/${authorisationId}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  updateAuthentication(link: string, updatePsuAuthentication: UpdatePsuAuthenticationRequestTO): Observable<ResourceUpdateAuthResponseTO> {
    return this.http.put(link, updatePsuAuthentication)
      .pipe(
        take(1),
        mergeMap(newStatus => {
          this.consentStatusChangedObservable.next({ data: newStatus, eventType: EventType.Update });
          return of(newStatus);
        }),
        catchError(this.handleError)
      );
  }

  scaMethodSelection(link: string, scaMethodSelection: SelectPsuAuthenticationMethodRequestTO): Observable<ResourceUpdateAuthResponseTO> {
    return this.http.put(link, scaMethodSelection)
      .pipe(
        take(1),
        mergeMap(newStatus => {
          this.consentStatusChangedObservable.next({ data: newStatus, eventType: EventType.Update });
          return of(newStatus);
        }),
        catchError(this.handleError)
      );
  }

}
