import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap, take } from 'rxjs/operators';
import { ChangeEvent } from 'src/app/model/changeEvent';
import { ResourceUpdateAuthResponseTO } from 'src/multibanking-api/resourceUpdateAuthResponseTO';
import { ConsentService } from '../rest/consent.service';

@Injectable({
  providedIn: 'root'
})
export class ConsentAuthstatusResolverService {

  private consentAuthStatus: ResourceUpdateAuthResponseTO;

  constructor(private consentService: ConsentService,
              private router: Router) {
    this.consentService.consentStatusChangedObservable.subscribe((changeEvent: ChangeEvent<ResourceUpdateAuthResponseTO>) => {
      this.consentAuthStatus = changeEvent.data;
    });
  }

  resolve(route: ActivatedRouteSnapshot): Observable<ResourceUpdateAuthResponseTO> | Observable<never> {
    return this.getConsentAuthorisation(route.paramMap.get('consent-id'), route.paramMap.get('authorisation-id'));
  }

  public setConsentAuthorisationStatus(consentAuthStatus: ResourceUpdateAuthResponseTO) {
    this.consentAuthStatus = consentAuthStatus;
  }

  public getConsentAuthorisation(consentId: string, authorisationId: string): Observable<ResourceUpdateAuthResponseTO> {
    if (!consentId) {
      return EMPTY;
    }

    if (this.consentAuthStatus) {
      return of(this.consentAuthStatus);
    }
    console.log('get consent auth status');

    consentId = decodeURIComponent(consentId).trim();
    authorisationId = decodeURIComponent(authorisationId).trim();

    return this.consentService.getAuthorisationStatus(consentId, authorisationId).pipe(
      take(1),
      mergeMap(consentAuthStatus => {
        if (consentAuthStatus) {
          this.consentAuthStatus = consentAuthStatus;
          return of(consentAuthStatus);
        } else { // id not found
          this.router.navigate(['/']);
          return EMPTY;
        }
      })
    );
  }
}
