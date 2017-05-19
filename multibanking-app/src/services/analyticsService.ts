import {Injectable} from "@angular/core";
import {AppConfig} from "../app/app.config";
import {Http, Response} from "@angular/http";
import {Observable} from "rxjs/Observable";
import "rxjs/Rx";

@Injectable()
export class AnalyticsService {

  constructor(private http: Http) {
  }

  getAnalytics(userId, accessId, accountId) {
    return this.http.get(AppConfig.api_url + "/users/" + userId + "/bankaccesses/" + accessId + "/accounts/" + accountId + "/analytics")
      .map((res: Response) => res.json() != null ? res.json() : {})
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
