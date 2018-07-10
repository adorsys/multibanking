import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { AutoCompleteService } from "ionic2-auto-complete";
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ENV } from "../env/env";
import { Rule } from 'model/multibanking/rule';

@Injectable()
export class RulesStaticAutoCompleteService implements AutoCompleteService {

  labelAttribute = "receiver";
  customRules: boolean = false;

  constructor(private http: HttpClient) {
  }

  getResults(keyword: string) {
    if (keyword.length <= 1) {
      return {};
    }

    let url = this.customRules ?
      `${ENV.api_url}/analytics/rules/search?query=${keyword}&custom=${this.customRules}` :
      `${ENV.smartanalytics_url}/rules/search?query=${keyword}&custom=${this.customRules}`;

    return this.http.get(url)
      .map((res: any) => {
        return res._embedded != null ? res._embedded.ruleEntityList : [];
      })
      .catch(this.handleError);
  }

  getItemLabel?(item: Rule): any {
    return item.ruleId + ' ' + (item.receiver ? item.receiver + ' ' : '') + (item.similarityMatchType ? item.expression : '')
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
