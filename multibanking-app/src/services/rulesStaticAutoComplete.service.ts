import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { AutoCompleteService } from "ionic2-auto-complete";
import { HttpClient } from '@angular/common/http';
import { ENV } from "../env/env";
import { Rule } from '../api/Rule';

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

  handleError(error) {
    console.error(error);
    let errorJson = error.json();
    if (errorJson) {
      return Observable.throw(errorJson || 'Server error');
    }
    return Observable.throw(error || 'Server error');
  }

  getItemLabel?(item: Rule): any {
    return item.ruleId + ' ' + (item.receiver ? item.receiver + ' ' : '') + (item.similarityMatchType ? item.expression : '')
  }
}
