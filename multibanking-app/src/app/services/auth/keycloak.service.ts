import { Injectable } from '@angular/core';
import * as Keycloak from 'keycloak-js';
import { KeycloakInitOptions, KeycloakLoginOptions } from 'keycloak-js';
import { Storage } from '@ionic/storage';
import { Router } from '@angular/router';
import { environment } from 'src/environments/environment';

const keycloak = Keycloak({
  url: environment.auth_url,
  realm: environment.realm,
  clientId: environment.client_id
});

@Injectable({
  providedIn: 'root',
})
export class KeycloakService {

  constructor(public storage: Storage,
              private router: Router) {
  }

  static init(options?: KeycloakInitOptions): Promise<any> {
    return new Promise((resolve, reject) => {
      keycloak.init(options ? options : {
        onLoad: 'check-sso',
        checkLoginIframe: false
      }).success(() => {
        console.log('Keycloak initialized, authenticated: ' + keycloak.authenticated);
        resolve();
      }).error((errorData: any) => {
        reject(errorData);
      });
    });
  }

  authenticated(): boolean {
    return keycloak.authenticated;
  }

  login(options: KeycloakLoginOptions = {}): Promise<any> {
    return new Promise((resolve, reject) => {
      if (!environment.isApp) {
        this.loginDesktopInternal(options, resolve, reject);
      } else {
        if (!options.redirectUri) {
          options.redirectUri = '/tabs/categories';
        }
        this.router.navigate(['login'], { queryParams: { success: options.redirectUri } });
      }
    });
  }

  loginDesktopInternal(options: KeycloakLoginOptions, resolve, reject) {
    if (options.redirectUri) {
      options.redirectUri = environment.base_url + options.redirectUri;
    }
    keycloak.login(options)
      .success(() => {
        resolve();
      })
      .error((errorData: any) => {
        reject(errorData);
      });
  }

  logout(): Promise<any> {
    return new Promise((resolve, reject) => {
      keycloak.logout()
        .success(() => {
          resolve();
        })
        .error((errorData: any) => {
          reject(errorData);
        });
    });
  }

  profile(): Promise<any> {
    return new Promise((resolve, reject) => {
      keycloak.loadUserProfile()
        .success((profile: any) => {
          resolve(profile);
        })
        .error((errorData: any) => {
          reject(errorData);
        });
    });
  }

  getToken(): Promise<string> {
    return new Promise<string>((resolve, reject) => {
      if (keycloak.token) {
        keycloak.updateToken(5)
          .success(() => {
            if (environment.isApp) {
              this.storage.set(this.getUserName(), keycloak.refreshToken as string);
            }
            resolve(keycloak.token as string);
          })
          .error(() => {
            keycloak.login();
          });
      } else {
        reject('Not loggen in');
      }
    });
  }

  getUserId(): string {
    return keycloak.tokenParsed.sub;
  }

  getUserName(): string {
    const token: any = keycloak.tokenParsed;
    return token.preferred_username;
  }

  getFirstName(): string {
    const token: any = keycloak.tokenParsed;
    return token.given_name;
  }

  getLastName(): string {
    const token: any = keycloak.tokenParsed;
    return token.family_name;
  }

  getIdToken() {
    return keycloak.idTokenParsed;
  }

  getRefreshToken() {
    return keycloak.refreshToken;
  }

  getRoles(): string[] {
    if (!keycloak.tokenParsed || !keycloak.tokenParsed.realm_access) {
      return [];
    }
    return keycloak.tokenParsed.realm_access.roles;
  }
}
