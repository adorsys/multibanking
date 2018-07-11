import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { AutoCompleteService } from "ionic2-auto-complete";
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ENV } from "../env/env";

@Injectable()
export class BankAutoCompleteService implements AutoCompleteService {

  labelAttribute = "name";

  constructor(private http: HttpClient) {
  }

  getResults(keyword: string) {
    return this.http.get(ENV.api_url + "/banks?query=" + keyword)
      .map((res: any) =>  {
        return res._embedded != null ? res._embedded.bankEntityList : [];
      })
      .catch(this.handleError);
  }

  handleError(error: HttpErrorResponse): Observable<any> {
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
