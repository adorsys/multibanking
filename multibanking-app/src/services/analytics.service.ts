import { Injectable } from "@angular/core";
import { Observable } from "rxjs/Observable";
import { AccountAnalytics } from "../api/AccountAnalytics";
import { HttpClient } from "@angular/common/http";
import { ENV } from "../env/env";


@Injectable()
export class AnalyticsService {

  constructor(private http: HttpClient) {
  }

  getAnalytics(accessId: string, accountId: string): Observable<AccountAnalytics> {
    return this.http.get(ENV.api_url + "/bankaccesses/" + accessId + "/accounts/" + accountId + "/analytics")
      .catch(this.handleError);
  }

  handleError(error): Observable<any> {
    console.error(error);
    let errorJson = error.json();
    if (errorJson) {
      if (errorJson.message == "RESCOURCE_NOT_FOUND") {
        return Observable.of({});
      } else if (errorJson.message == "SYNC_IN_PROGRESS") {
        return Observable.throw(errorJson.message);
      } else {
        return Observable.throw(errorJson || 'Server error');
      }
    } else {
      return Observable.throw(error || 'Server error');
    }
  }

}
