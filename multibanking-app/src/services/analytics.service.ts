import { Injectable } from "@angular/core";
import { Observable } from "rxjs/Observable";
import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { ENV } from "../env/env";
import { ResourceAccountAnalyticsEntity } from "../model/multibanking/models";


@Injectable()
export class AnalyticsService {

  constructor(private http: HttpClient) {
  }

  getAnalytics(accessId: string, accountId: string): Observable<ResourceAccountAnalyticsEntity> {
    return this.http.get(`${ENV.api_url}/bankaccesses/${accessId}/accounts/${accountId}/analytics`)
      .catch(this.handleError);
  }

  handleError(error: HttpErrorResponse, ): Observable<any> {
    console.error(error);
    let result: Observable<any>;
    if (error.error) {
      if (error.error.messages) {
        result = Observable.throw(error.error.messages);
      } else {
        result = Observable.throw(JSON.parse(error.error).messages);
      }
    } else {
      result = Observable.throw(error || 'Server error');
    }
    return result;
  }

}
