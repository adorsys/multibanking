import { Injectable } from '@angular/core';
import * as Keycloak from 'keycloak-js';
import { AppConfig } from '../app/app.config';

const keycloak = Keycloak({
  url: AppConfig.auth_url,
  realm: 'multibanking',
  clientId: 'multibanking-client'
})

@Injectable()
export class KeycloakService {

  static init(options?: any): Promise<any> {

    return new Promise((resolve, reject) => {
      keycloak.init(options)
        .success(() => {
          resolve();
        })
        .error((errorData: any) => {
          reject(errorData);
        });
    });
  }

  authenticated(): boolean {
    return keycloak.authenticated;
  }

  login(): Promise<any> {
    return new Promise((resolve, reject) => {
      keycloak.login()
        .success(() => {
          resolve();
        })
        .error((errorData: any) => {
          reject(errorData);
        });
    })
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
    })
  }

  account() {
    keycloak.accountManagement();
  }

  register() {
    keycloak.register();
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
    })
  }

  getToken(): Promise<string> {
    return new Promise<string>((resolve, reject) => {
      if (keycloak.token) {
        keycloak.updateToken(5)
          .success(() => {
            resolve(<string>keycloak.token);
          })
          .error(() => {
            reject('Failed to refresh token');
          });
      } else {
        this.login();
      }
    });
  }

  getRoles(): string[] {
    if (!keycloak.tokenParsed || !keycloak.tokenParsed.realm_access) {
      return [];
    }
    return keycloak.tokenParsed.realm_access.roles;
  }

  getUsername(): string {
    return keycloak.tokenParsed.sub;
  }


}
