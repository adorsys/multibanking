import { Injectable } from '@angular/core';
import { Storage } from '@ionic/storage';
import { AndroidFingerprintAuth, AFAAuthOptions } from '@ionic-native/android-fingerprint-auth/ngx';
import { Observable, Subscriber } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AuthService } from 'src/app/services/rest/auth.service';
import { KeycloakService } from '../auth/keycloak.service';

@Injectable({
  providedIn: 'root',
})
export class FingerPrintService {

  fingerPrintAuthConfig: AFAAuthOptions = {
    clientId: environment.client_id,
    userAuthRequired: false,
    locale: 'de_DE'
  };

  constructor(public androidFingerprintAuth: AndroidFingerprintAuth,
              public authService: AuthService,
              public keycloak: KeycloakService,
              public storage: Storage) {
  }

  public fingerPrintAuthAvailable(): Observable<boolean> {
    return new Observable((observer: Subscriber<boolean>) => {
      this.androidFingerprintAuth.isAvailable()
        .then(result => {
          if (result.isAvailable && result.hasEnrolledFingerprints === true) {
            observer.next(true);
          } else {
            console.log('fingerprint not available');
            observer.next(false);
          }
        })
        .catch(error => {
          console.log(error);
          observer.next(false);
        });
    });
  }

  public getKeycloakRefreshToken(): Observable<string> {
    return new Observable((observer: Subscriber<string>) => {
      this.storage.get('token').then((token) => {
        if (token) {
          this.loadKeycloakToken(token).subscribe(
            response => {
              observer.next(response);
            }
          );
        } else {
          observer.next();
        }
      });
    });
  }

  private loadKeycloakToken(fingerprintToken: string): Observable<string> {
    return new Observable((observer: Subscriber<string>) => {
      this.fingerPrintAuthConfig.token = fingerprintToken;
      this.androidFingerprintAuth.decrypt(this.fingerPrintAuthConfig)
        .then(result => {
          this.storage.get(result.password).then((refreshToken) => {
            if (refreshToken) {
              observer.next(refreshToken);
            } else {
              this.onFingerPrintAuthTokenError();
              observer.next();
            }
          });
        })
        .catch(error => {
          if (error && error === 'FINGERPRINT_CANCELLED') {
            observer.next();
          } else {
            this.onFingerPrintAuthTokenError(error);
            observer.next();
          }
        });
    });
  }

  private onFingerPrintAuthTokenError(error?) {
    if (error) {
      console.log('fingerprint not available: ' + error);
    }
    this.androidFingerprintAuth.delete({ clientId: environment.client_id, username: '' })
      .then(() => {
        this.storage.remove('token');
      })
      .catch(deleteError => {
        console.log(deleteError);
        // ignore
      });
  }

  public activateFingerprint(activate: boolean) {
    return new Observable((observer: Subscriber<boolean>) => {
      if (activate) {
        const fingerPrintAuthConfig: AFAAuthOptions = {
          clientId: environment.client_id,
          password: this.keycloak.getUserName(),
          userAuthRequired: false,
          locale: 'de_DE'
        };

        this.androidFingerprintAuth.encrypt(fingerPrintAuthConfig)
          .then(result => {
            observer.next(true);
            this.storage.set('token', result.token);
            this.storage.set(this.keycloak.getUserName(), this.keycloak.getRefreshToken());
            this.storage.set('activateFingerprint', true);
            observer.next(true);
          })
          .catch(error => {
            console.log(error);
            observer.next(false);
          });
      } else {
        this.androidFingerprintAuth.delete({ clientId: environment.client_id, username: this.keycloak.getUserName() })
          .then(result => {
            this.storage.remove('token');
            this.storage.remove(this.keycloak.getUserName());
            this.storage.remove('activateFingerprint');
            observer.next(false);
            console.log(result);
          })
          .catch(error => {
            console.log(error);
            observer.next(false);
          });
      }
    });
  }

}
