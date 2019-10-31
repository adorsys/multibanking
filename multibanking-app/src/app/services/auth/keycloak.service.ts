import { Injectable, SkipSelf, Optional } from '@angular/core';
import * as Keycloak from 'keycloak-js';
import { KeycloakInitOptions, KeycloakLoginOptions } from 'keycloak-js';
import { Storage } from '@ionic/storage';
import { Router } from '@angular/router';
import { environment } from 'src/environments/environment';
import { SettingsService } from '../settings/settings.service';

@Injectable({
  providedIn: 'root',
})
export class KeycloakService {

  private keycloak: Keycloak.KeycloakInstance;

  constructor(public storage: Storage,
              public settingsService: SettingsService,
              @SkipSelf() @Optional() private router: Router
              ) {}

  init(options?: KeycloakOptions): Promise<any> {
    return new Promise((resolve, reject) => {
      const { config, initOptions } = options;
      this.keycloak = Keycloak(config ? config : {
        url: this.settingsService.settings.authUrl,
        realm: this.settingsService.settings.realm,
        clientId: this.settingsService.settings.clientId
      });
      this.keycloak.init(initOptions ? initOptions : {
        onLoad: 'check-sso',
        checkLoginIframe: false
      }).success(() => {
        console.log('Keycloak initialized, authenticated: ' + this.keycloak.authenticated);
        resolve();
      }).error((errorData: any) => {
        console.error('Keycloak initialized error: ' + errorData);
        reject(errorData);
        this.keycloak.logout();
      });
    });
  }

  authenticated(): boolean {
    try {
      if (!this.keycloak.authenticated) {
        return false;
      }
      return true;
    } catch (error) {
      return false;
    }
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
      options.redirectUri = this.settingsService.settings.baseUrl + options.redirectUri;
    }
    this.keycloak.login(options)
      .success(() => {
        resolve();
      })
      .error((errorData: any) => {
        reject(errorData);
      });
  }

  logout(): Promise<any> {
    return new Promise((resolve, reject) => {
      this.keycloak.logout()
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
      this.keycloak.loadUserProfile()
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
      if (this.keycloak.token) {
        this.keycloak.updateToken(5)
          .success(() => {
            if (environment.isApp) {
              this.storage.set(this.getUserName(), this.keycloak.refreshToken as string);
            }
            resolve(this.keycloak.token as string);
          })
          .error(() => {
            this.keycloak.login();
          });
      } else {
        reject('Not loggen in');
      }
    });
  }

  getUserId(): string {
    return this.keycloak.tokenParsed.sub;
  }

  getUserName(): string {
    const token: any = this.keycloak.tokenParsed;
    return token.preferred_username;
  }

  getFirstName(): string {
    const token: any = this.keycloak.tokenParsed;
    return token.given_name;
  }

  getLastName(): string {
    const token: any = this.keycloak.tokenParsed;
    return token.family_name;
  }

  getIdToken() {
    return this.keycloak.idTokenParsed;
  }

  getRefreshToken() {
    return this.keycloak.refreshToken;
  }

  getRoles(): string[] {
    if (!this.keycloak.tokenParsed || !this.keycloak.tokenParsed.realm_access) {
      return [];
    }
    return this.keycloak.tokenParsed.realm_access.roles;
  }
}

export interface KeycloakOptions {
  config?: KeycloakConfig;
  initOptions?: KeycloakInitOptions;
}

export interface KeycloakConfig {
  url: string;
  realm: string;
  clientId: string;
}
