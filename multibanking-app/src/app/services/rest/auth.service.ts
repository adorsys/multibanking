import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpHeaders, HttpParams } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { AbstractService } from './abstract.service';

@Injectable({
  providedIn: 'root',
})
export class AuthService extends AbstractService {

  auth(user: string, password: string): Observable<any> {
    const formData = new HttpParams()
      .append('grant_type', 'password')
      .append('scope', 'offline_access')
      .append('client_id', this.settings.clientId)
      .append('username', user)
      .append('password', password);

    const options = {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
    };

    return this.http.post(
        `${this.settings.authUrl}/realms/${this.settings.realm}/protocol/openid-connect/token`,
        formData,
        options
      )
      .pipe(
        catchError(this.handleError)
      );
  }

  refreshToken(refreshToken: string): Observable<any> {
    const formData = new HttpParams()
      .append('grant_type', 'refresh_token')
      .append('client_id', this.settings.clientId)
      .append('refresh_token', refreshToken);

    const options = {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
    };

    return this.http.post(`${this.settings.authUrl}/realms/${this.settings.realm}/protocol/openid-connect/token`, formData, options)
      .pipe(
        catchError(this.handleError)
      );
  }
}

