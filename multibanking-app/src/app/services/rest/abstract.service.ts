
import { throwError as observableThrowError, Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { KeycloakService } from '../auth/keycloak.service';
import { SettingsService } from '../settings/settings.service';

@Injectable({
  providedIn: 'root',
})
export class AbstractService {

  constructor(public http: HttpClient, public settingsService: SettingsService, public keycloakService: KeycloakService) {
  }

  get settings() {
    return this.settingsService.settings;
  }

  handleError(error) {
    console.error(error);
    let result: Observable<any>;
    if (error.error) {
      if (error.error.messages) {
        result = observableThrowError(error.error.messages);
      } else {
        try {
          result = observableThrowError(JSON.parse(error.error).messages);
        } catch (e) {
          result = observableThrowError(error || 'Server error');
        }
      }
    } else {
      result = observableThrowError(error || 'Server error');
    }
    return result;
  }
}
