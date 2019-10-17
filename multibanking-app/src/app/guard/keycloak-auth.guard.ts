import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Injectable } from '@angular/core';
import { NavController } from '@ionic/angular';
import { environment } from 'src/environments/environment';
import { Storage } from '@ionic/storage';
import { Observable, Subscriber } from 'rxjs';
import { AndroidFingerprintAuth } from '@ionic-native/android-fingerprint-auth/ngx';
import { KeycloakService } from '../services/auth/keycloak.service';
import { AuthService } from '../services/rest/auth.service';
import { FingerPrintService } from '../services/fingerprint/fingerprint.service';

@Injectable({
  providedIn: 'root',
})
export class KeycloakAuthGuard implements CanActivate {

    constructor(private keycloakService: KeycloakService,
                private authService: AuthService,
                private router: Router,
                private storage: Storage,
                private fingerPrintService: FingerPrintService,
                public androidFingerprintAuth: AndroidFingerprintAuth,
                public navCtrl: NavController) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | boolean {
        console.log('auth guard canActivate target: ' + state.url);

        if (!this.keycloakService.authenticated()) {
            if (!environment.isApp) {
                this.keycloakService.login({ redirectUri: state.url });
                return false;
            } else {
                return this.handleNativeAppNotAuthenticated(state);
            }
        } else {
            return true;
        }
    }

    handleNativeAppNotAuthenticated(state: RouterStateSnapshot): Observable<boolean> {
        return new Observable((observer: Subscriber<boolean>) => {
            this.storage.get('activateFingerprint').then((fingerPrintActive) => {
                if (fingerPrintActive) {
                    this.fingerPrintService.fingerPrintAuthAvailable().subscribe((result) => {
                        if (result) {
                            this.fingerPrintAuth(observer, state);
                        } else {
                            observer.next(false);
                            this.storage.remove('activateFingerprint');
                            this.router.navigate(['login'], { queryParams: { success: state.url } });
                        }
                    });
                } else {
                    observer.next(false);
                    this.router.navigate(['login'], { queryParams: { success: state.url } });
                }
            });
        });
    }

    fingerPrintAuth(observer: Subscriber<boolean>, state: RouterStateSnapshot) {
        this.fingerPrintService.getKeycloakRefreshToken().subscribe((refreshToken: string) => {
            if (refreshToken) {
                console.log('init using preexisting token');
                this.authService.refreshToken(refreshToken)
                    .subscribe(
                        (data) => this.initKeycloakNativeApp(observer, data, state),
                        () => {
                            observer.next(false);
                            this.router.navigate(['login'], { queryParams: { success: state.url } });
                        }
                    );
            } else {
                observer.next(false);
                this.storage.remove('activateFingerprint');
                this.router.navigate(['login'], { queryParams: { success: state.url } });
            }
        });
    }

    initKeycloakNativeApp(observer: Subscriber<boolean>, tokens, state: RouterStateSnapshot) {
        this.keycloakService.init({ initOptions: { token: tokens.access_token, refreshToken: tokens.refresh_token } }).then(() => {
            console.log('Keycloak initialized, authenticated: ' + this.keycloakService.authenticated());
            if (this.keycloakService.authenticated()) {
                this.storage.set(this.keycloakService.getUserName(), this.keycloakService.getRefreshToken()).then(() => {
                    console.log('updated refreshToken in storage');
                });
                observer.next(true);
            } else {
                observer.next(false);
                this.router.navigate(['login'], { queryParams: { success: state.url } });
            }
        });
    }
}
