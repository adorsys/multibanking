import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HTTP_INTERCEPTORS, HttpHeaders } from '@angular/common/http';
import { Observable ,  Observer } from 'rxjs';
import { KeycloakService } from './keycloak.service';
import { mergeMap } from 'rxjs/operators';

@Injectable()
export class KeycloakHttpInterceptor implements HttpInterceptor {

  constructor(private keycloakService: KeycloakService) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!this.keycloakService.authenticated()) {
      return next.handle(request);
    }
    return Observable.create(async (observer: Observer<any>) => {
      let headers = request.headers;
      if (!headers) {
        headers = new HttpHeaders();
      }
      try {
        const token: string = await this.keycloakService.getToken();
        headers = headers.set('Authorization', 'Bearer ' + token);
        observer.next(headers);
        observer.complete();
      } catch (error) {
        console.log(error);
        this.keycloakService.login();
      }
    }).pipe(
      mergeMap((headersWithBearer: HttpHeaders) => {
        const kcReq = request.clone({ headers: headersWithBearer });
        return next.handle(kcReq);
      })
    );
  }
}

export const KEYCLOAK_HTTP_INTERCEPTOR = {
  provide: HTTP_INTERCEPTORS,
  useClass: KeycloakHttpInterceptor,
  multi: true
};
