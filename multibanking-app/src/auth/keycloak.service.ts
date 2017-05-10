import {Injectable} from '@angular/core';

import { AppConfig } from '../app/app.config';

declare var Keycloak: any;

@Injectable()
export class KeycloakService {
  static auth: any = {};

  static init(): Promise<any> {
    const keycloakAuth: any = Keycloak({
      url: AppConfig.auth_url,
      realm: 'multibanking',
      clientId: 'multibanking-client',
    });

    KeycloakService.auth.loggedIn = false;

    return new Promise((resolve, reject) => {
      keycloakAuth.init({ onLoad: 'login-required' })
        .success(() => {
          KeycloakService.auth.loggedIn = true;
          KeycloakService.auth.authz = keycloakAuth;
          KeycloakService.auth.logoutUrl = keycloakAuth.authServerUrl
            + '/realms/rezeptsuite-client/protocol/openid-connect/logout?redirect_uri='
            + document.baseURI;
          resolve();
        })
        .error(() => {
          reject();
        });
    });
  }

  logout() {
    console.log('*** LOGOUT');
    KeycloakService.auth.loggedIn = false;
    KeycloakService.auth.authz = null;

    window.location.href = KeycloakService.auth.logoutUrl;
  }

  getToken(): Promise<string> {
    return new Promise<string>((resolve, reject) => {
      if (KeycloakService.auth.authz.token) {
        KeycloakService.auth.authz
          .updateToken(5)
          .success(() => {
            resolve(<string>KeycloakService.auth.authz.token);
          })
          .error(() => {
            reject('Failed to refresh token');
          });
      } else {
        reject('Not loggen in');
      }
    });
  }

  getUsername(): Promise<string> {
    return KeycloakService.auth.authz.tokenParsed.sub;
  }
}
