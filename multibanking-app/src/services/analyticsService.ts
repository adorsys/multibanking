import {Injectable} from '@angular/core';
import {AppConfig} from '../app/app.config';
import {Http, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import 'rxjs/Rx';

@Injectable()
export class AnalyticsService {

  constructor(private http: Http) {
  }

  getAnalytics(userId, accessId, accountId) {
    return this.http.get(AppConfig.api_url + userId + "/bankaccesses/" + accessId + "/accounts/" + accountId + "/analytics")
      .map((res: Response) => res.json() != null ? res.json() : {})
      .catch((error: Response) => {
        if (error.json() && error.json().message == "RESCOURCE_NOT_FOUND") {
          return Observable.of({});
        } else {
          console.error(error);
          return Observable.throw(error.json().error || 'Server error');
        }
      });
  }

}
