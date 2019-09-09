import { Injectable } from '@angular/core';
import { Http, Request, XHRBackend, ConnectionBackend, RequestOptions, RequestOptionsArgs, Response, Headers } from '@angular/http';

import { KeycloakService } from './keycloak.service';
import { Observable, from } from 'rxjs';
import { map, concatMap } from 'rxjs/operators';

/**
 * This provides a wrapper over the ng2 Http class that insures tokens are refreshed on each request.
 */
@Injectable({
  providedIn: 'root',
})
export class KeycloakHttp extends Http {
  constructor(backend: ConnectionBackend, defaultOptions: RequestOptions, private keycloakService: KeycloakService) {
    super(backend, defaultOptions);
  }

  request(url: string | Request, options?: RequestOptionsArgs): Observable<Response> {
    if (!this.keycloakService.authenticated()) {
      return super.request(url, options);
    }

    const tokenPromise: Promise<string> = this.keycloakService.getToken();
    const tokenObservable: Observable<string> = from(tokenPromise);

    if (typeof url === 'string') {
      return tokenObservable.pipe(
        map(token => {
          const authOptions = new RequestOptions({ headers: new Headers({ Authorization: 'Bearer ' + token }) });
          return new RequestOptions().merge(options).merge(authOptions);
        }),
        concatMap(opts => super.request(url, opts))
      );
    } else if (url instanceof Request) {
      return tokenObservable.pipe(
        map(token => {
          url.headers.set('Authorization', 'Bearer ' + token);
          return url;
        }),
        concatMap(request => super.request(request))
      );
    }
  }
}

export function keycloakHttpFactory(backend: XHRBackend, defaultOptions: RequestOptions, keycloakService: KeycloakService) {
  return new KeycloakHttp(backend, defaultOptions, keycloakService);
}

export const KEYCLOAK_HTTP_PROVIDER = {
  provide: Http,
  useFactory: keycloakHttpFactory,
  deps: [XHRBackend, RequestOptions, KeycloakService]
};
