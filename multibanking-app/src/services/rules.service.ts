import { Injectable } from "@angular/core";
import { AppConfig } from "../app/app.config";
import { Http, Response, ResponseContentType } from "@angular/http";
import { Observable } from "rxjs/Observable";
import { RuleCategory } from "../api/RuleCategory";
import { Rule } from "../api/Rule";
import { Subject } from "rxjs";
import { Pageable } from "../api/Pageable";

@Injectable()
export class RulesService {

  public rulesChangedObservable = new Subject<Rule>();

  constructor(private http: Http) {
  }

  getAvailableCategories(): Observable<Array<RuleCategory>> {
    return this.http.get(`${AppConfig.api_url}/analytics/rules/categories`)
      .map((res: Response) => res.json()._embedded.ruleCategoryList)
      .catch(this.handleError);
  }

  createRule(rule: Rule, custom: boolean): Observable<Array<RuleCategory>> {
    let url = custom ? `${AppConfig.api_url}/analytics/rules`
      : `${AppConfig.smartanalytics_url}/rules`;

    return this.http.post(url, rule)
      .catch(this.handleError)
      .finally(() => {
        this.rulesChangedObservable.next(rule)
      })
  }

  updateRule(rule: Rule): Observable<Array<RuleCategory>> {
    let url = rule.released ? `${AppConfig.smartanalytics_url}/rules/${rule.id}`
      : `${AppConfig.api_url}/analytics/rules/${rule.id}`;

    return this.http.put(url, rule)
      .catch(this.handleError)
      .finally(() => {
        this.rulesChangedObservable.next(rule)
      })
  }

  getRules(custom: boolean): Observable<Pageable> {
    let url = custom ? `${AppConfig.api_url}/analytics/rules/` : `${AppConfig.smartanalytics_url}/rules/`;

    return this.http.get(url)
      .map(response => response.json())
      .catch(this.handleError);
  }

  getNextRules(url: string): Observable<Pageable> {
    return this.http.get(url)
      .map(response => response.json())
      .catch(this.handleError);
  }

  mapRulesResponse(res: Response, custom: boolean) {
    let json = res.json();
    if (json._embedded && json._embedded.customRuleEntityList) {
      return json._embedded.customRuleEntityList;
    } else if (json._embedded && json._embedded.ruleEntityList) {
      return json._embedded.ruleEntityList;
    }
    return [];

  }

  getRule(id: string): Observable<Rule> {
    let url = id.startsWith("custom") ? `${AppConfig.api_url}/analytics/rules/${id}`
      : `${AppConfig.smartanalytics_url}/rules/${id}`;

    return this.http.get(url)
      .map((res: Response) => res.json() != null ? res.json() : {})
      .catch(this.handleError);
  }

  deleteRule(id, custom): Observable<any> {
    let url = custom ? `${AppConfig.api_url}/analytics/rules/${id}`
      : `${AppConfig.smartanalytics_url}/rules/${id}`;

    return this.http.delete(url)
      .catch(this.handleError);
  }

  downloadRules(custom): Observable<any> {
    let url = custom ? `${AppConfig.api_url}/analytics/rules/download`
      : `${AppConfig.smartanalytics_url}/rules/download`;

    return this.http.get(url, { responseType: ResponseContentType.Blob })
      .map(res => {
        return new Blob([res.blob()], { type: 'application/yaml' })
      })
      .catch(this.handleError);
  }

  uploadRules(file: File): Observable<any> {
    let formData: FormData = new FormData();
    formData.append('rulesFile', file, 'rules.yml');

    return this.http.post(`${AppConfig.smartanalytics_url}/rules/upload`, formData)
      .catch(this.handleError);
  }

  handleError(error): Observable<any> {
    console.error(error);
    let errorJson = error.json();
    if (errorJson) {
      if (errorJson.message == "RESCOURCE_NOT_FOUND") {
        return Observable.of({});
      } else if (errorJson.message == "INVALID_RULES") {
        return Observable.throw(errorJson.message);
      }
      return Observable.throw(errorJson || 'Server error');
    }
    return Observable.throw(error || 'Server error');
  }

}
