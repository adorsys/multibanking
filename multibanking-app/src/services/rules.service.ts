import { Injectable } from "@angular/core";
import { Observable } from "rxjs/Observable";
import { RuleCategory } from "../api/RuleCategory";
import { Rule } from "../api/Rule";
import { Subject } from "rxjs";
import { Pageable } from "../api/Pageable";
import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { ENV } from "../env/env";

@Injectable()
export class RulesService {

  public rulesChangedObservable = new Subject<Rule>();

  constructor(private http: HttpClient) {
  }

  getAvailableCategories(): Observable<Array<RuleCategory>> {
    return this.http.get(`${ENV.api_url}/analytics/rules/categories`)
      .map((res: any) => res._embedded.ruleCategoryList)
      .catch(this.handleError);
  }

  createRule(rule: Rule, custom: boolean): Observable<Array<RuleCategory>> {
    let url = custom ? `${ENV.api_url}/analytics/rules`
      : `${ENV.smartanalytics_url}/rules`;

    return this.http.post(url, rule, { responseType: 'text' })
      .catch(this.handleError)
      .finally(() => {
        this.rulesChangedObservable.next(rule)
      })
  }

  updateRule(rule: Rule): Observable<Array<RuleCategory>> {
    let url = rule.ruleId.startsWith("custom") ? `${ENV.api_url}/analytics/rules/${rule.id}`
      : `${ENV.smartanalytics_url}/rules/${rule.id}`;

    return this.http.put(url, rule)
      .catch(this.handleError)
      .finally(() => {
        this.rulesChangedObservable.next(rule)
      })
  }

  getRulesStatus() {
    return this.http.get(`${ENV.smartanalytics_url.replace("/api/v1", "")}/status`)
      .catch(this.handleError);
  }

  getRules(custom: boolean): Observable<Pageable> {
    let url = custom ? `${ENV.api_url}/analytics/rules/` : `${ENV.smartanalytics_url}/rules/`;

    return this.http.get(url)
      .catch(this.handleError);
  }

  getNextRules(url: string): Observable<Pageable> {
    return this.http.get(url)
      .catch(this.handleError);
  }

  getRule(id: string): Observable<Rule> {
    let url = id.startsWith("custom") ? `${ENV.api_url}/analytics/rules/${id}`
      : `${ENV.smartanalytics_url}/rules/${id}`;

    return this.http.get(url)
      .map((res: Response) => res.json() != null ? res.json() : {})
      .catch(this.handleError);
  }

  deleteRule(id, custom): Observable<any> {
    let url = custom ? `${ENV.api_url}/analytics/rules/${id}`
      : `${ENV.smartanalytics_url}/rules/${id}`;

    return this.http.delete(url)
      .catch(this.handleError);
  }

  downloadRules(custom): Observable<any> {
    let url = custom ? `${ENV.api_url}/analytics/rules/download`
      : `${ENV.smartanalytics_url}/rules/download`;

    return this.http.get(url, { responseType: 'blob' })
      .map(res => {
        return new Blob([res], { type: 'application/yaml' })
      })
      .catch(this.handleError);
  }

  uploadRules(file: File): Observable<any> {
    let formData: FormData = new FormData();
    formData.append('rulesFile', file, file.name);

    return this.http.post(`${ENV.smartanalytics_url}/rules/upload`, formData, { responseType: 'text' })
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
