import { Injectable } from '@angular/core';
import { ResourceConsentTO } from 'src/multibanking-api/resourceConsentTO';
import { Router, ActivatedRouteSnapshot } from '@angular/router';
import { ConsentService } from '../rest/consent.service';
import { Observable, of, EMPTY } from 'rxjs';
import { take, mergeMap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ConsentResolverService {

  private consent: ResourceConsentTO;

  constructor(private consentService: ConsentService,
              private router: Router) {
  }

  resolve(route: ActivatedRouteSnapshot): Observable<ResourceConsentTO> | Observable<never> {
    const redirectId: string = route.paramMap.get('redirect-id');
    if (!redirectId) {
      return this.getConsent(route.paramMap.get('consent-id'));
    } else {
      return this.consentService.getConsentByRedirectId(redirectId);
    }
  }

  public getConsent(consentId: string) {
    if (this.consent && consentId === this.consent.id) {
      return of(this.consent);
    }

    if (!consentId) {
      return EMPTY;
    }

    console.log('load bank access');

    return this.consentService.getConsent(decodeURIComponent(consentId).trim()).pipe(
      take(1),
      mergeMap(consent => {
        if (consent) {
          this.consent = consent;
          return of(consent);
        } else { // id not found
          this.router.navigate(['/']);
          return EMPTY;
        }
      })
    );
  }
}
