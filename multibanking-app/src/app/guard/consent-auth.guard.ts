import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, CanActivate, Router, ActivatedRoute } from '@angular/router';
import { Observable, of } from 'rxjs';
import { ConsentAuthstatusResolverService } from '../services/resolver/consent-authstatus-resolver.service';
import { take, mergeMap } from 'rxjs/operators';
import { ResourceUpdateAuthResponseTO } from 'src/multibanking-api/resourceUpdateAuthResponseTO';

@Injectable({
  providedIn: 'root'
})
export class ConsentAuthGuard implements CanActivate {

  constructor(private consentAuthstatusResolverService: ConsentAuthstatusResolverService,
              private router: Router) { }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot):
    boolean | UrlTree | Observable<boolean | UrlTree> | Promise<boolean | UrlTree> {
    const consentId = route.paramMap.get('consent-id');
    const authorisationId = route.paramMap.get('authorisation-id');

    if (!consentId) {
      return true;
    }
    return this.consentAuthstatusResolverService.getConsentAuthorisation(consentId, authorisationId)
      .pipe(
        take(1),
        mergeMap(consentAuthStatus => {
          if (consentAuthStatus.scaApproach === 'EMBEDDED') {
            return this.embedded(consentAuthStatus, state, consentId, authorisationId);
          } else if (consentAuthStatus.scaApproach === 'DECOUPLED') {
            return this.decoupled(consentAuthStatus, state, consentId, authorisationId);
          } else if (consentAuthStatus.scaApproach === 'REDIRECT') {
            return this.redirect(consentAuthStatus, state, consentId, authorisationId);
          } else {
            return of(false);
          }
        })
      );
  }

  private embedded(consentAuthStatus: ResourceUpdateAuthResponseTO, state: RouterStateSnapshot,
                   consentId: string, authorisationId: string): Observable<boolean> {
    if (consentAuthStatus.scaStatus === 'RECEIVED' || consentAuthStatus.scaStatus === 'STARTED') {
      if (state.url.startsWith('/update-auth')) {
        return of(true);
      } else {
        this.router.navigate(['update-auth/consents', consentId, 'authorisations', authorisationId],
          { skipLocationChange: true });
      }
    } else if (consentAuthStatus.scaStatus === 'SCAMETHODSELECTED') {
      if (state.url.startsWith('/authorisation')) {
        return of(true);
      } else {
        this.router.navigate(['authorisation/consents', consentId, 'authorisations', authorisationId],
          { skipLocationChange: true });
      }
    } else if (consentAuthStatus.scaStatus === 'PSUAUTHENTICATED') {
      if (consentAuthStatus.scaMethods) {
        if (state.url.startsWith('/sca-method-selection')) {
          return of(true);
        } else {
          this.router.navigate(['sca-method-selection/consents', consentId, 'authorisations', authorisationId],
            { skipLocationChange: true });
        }
      } else {
        if (state.url.startsWith('/authorisation')) {
          return of(true);
        } else {
          this.router.navigate(['authorisation/consents', consentId, 'authorisations', authorisationId],
            { skipLocationChange: true });
        }
      }
    }
    return of(false);
  }

  private decoupled(consentAuthStatus: ResourceUpdateAuthResponseTO, state: RouterStateSnapshot,
                    consentId: string, authorisationId: string): Observable<boolean> {
    this.router.navigate(['bankaccess-create/consents', consentId, 'authorisations', authorisationId], { skipLocationChange: true });
    return of(false);
  }

  private redirect(consentAuthStatus: ResourceUpdateAuthResponseTO, state: RouterStateSnapshot,
                   consentId: string, authorisationId: string): Observable<boolean> {
    return of(true);
  }

}
