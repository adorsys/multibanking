import { Injectable } from '@angular/core';
import { AppConfig } from '../app/app.config';
import { Observable } from 'rxjs/Observable';
import { AutoCompleteService } from "ionic2-auto-complete";
import { HttpClient } from '@angular/common/http';

@Injectable()
export class BankAutoCompleteService implements AutoCompleteService {

  labelAttribute = "name";

  constructor(private http: HttpClient) {
  }

  getResults(keyword: string) {
    return this.http.get(AppConfig.api_url + "/bank?query=" + keyword)
      .map((res: any) =>  {
        return res._embedded != null ? res._embedded.bankEntityList : [];
      })
      .catch(this.handleError);
  }

  handleError(error) {
    console.error(error);
    let errorJson = error.json();
    if (errorJson) {
      return Observable.throw(errorJson || 'Server error');
    }

    return Observable.throw(error || 'Server error');
  }

}
