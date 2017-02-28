import {Injectable} from '@angular/core';
import {AppConfig} from '../app/app.config';
import {Http} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import 'rxjs/Rx';

@Injectable()
export class UserService {

  constructor(private http: Http, private appConfig: AppConfig) {
  }

  crateUser(user) {
    return this.http.post(this.appConfig.API_URL+"/users", user)
      .catch(this.handleError);
  }

  handleError(error) {
    console.error(error);
    return Observable.throw(error.json().error || 'Server error');
  }


}
