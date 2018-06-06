import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HTTP_INTERCEPTORS, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { KeycloakService } from './keycloak.service';
import { Observer } from 'rxjs/Observer';
import { mergeMap } from 'rxjs/operators';

@Injectable()
export class KeycloakHttpInterceptor implements HttpInterceptor {

  constructor(private _keycloakService: KeycloakService) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return Observable.create(async (observer: Observer<any>) => {
      let headers = request.headers;
      if (!headers) {
        headers = new HttpHeaders();
      }
      try {
        const token: string = await this._keycloakService.getToken();
        headers = headers.set('Authorization', 'Bearer ' + token);
        observer.next(headers);
        observer.complete();
      } catch (error) {
        this._keycloakService.login();
      }
    }).pipe(
      mergeMap((headersWithBearer: HttpHeaders) => {
        const kcReq = request.clone({ headers: headersWithBearer });
        return next.handle(kcReq);
      })
    )
  }

}

export const KEYCLOAK_HTTP_PROVIDER = {
  provide: HTTP_INTERCEPTORS,
  useClass: KeycloakHttpInterceptor,
  multi: true
};