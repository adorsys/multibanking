import { Injectable } from "@angular/core";
import { AppConfig } from "../app/app.config";
import { Http, Response, ResponseContentType } from "@angular/http";
import { Observable } from "rxjs/Observable";
import { RuleCategory } from "../api/RuleCategory";
import { Rule } from "../api/Rule";
import { Subject } from "rxjs";

@Injectable()
export class RulesService {

  public rulesChangedObservable = new Subject<Rule>();

  constructor(private http: Http) {
  }

  getAvailableCategories(): Observable<Array<RuleCategory>> {
    return this.http.get(`${AppConfig.api_url}/analytics/categories`)
      .map((res: Response) => res.json()._embedded.ruleCategoryList)
      .catch(this.handleError);
  }

  createRule(rule: Rule): Observable<Array<RuleCategory>> {
    return this.http.post(`${AppConfig.api_url}/analytics/rules`, rule)
      .catch(this.handleError)
      .finally(() => {
        this.rulesChangedObservable.next(rule)
      })
  }

  updateRule(rule: Rule): Observable<Array<RuleCategory>> {
    return this.http.put(`${AppConfig.api_url}/analytics/rules/${rule.id}`, rule)
      .catch(this.handleError)
      .finally(() => {
        this.rulesChangedObservable.next(rule)
      })
  }

  getRules(custom: boolean): Observable<Array<Rule>> {
    return this.http.get(`${AppConfig.api_url}/analytics/rules?custom=` + custom)
      .map((res: Response) => this.mapRulesResponse(res, custom))
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
    return this.http.get(`${AppConfig.api_url}/analytics/rules/${id}`)
      .map((res: Response) => res.json() != null ? res.json() : {})
      .catch(this.handleError);
  }

  deleteRule(id, custom): Observable<any> {
    return this.http.delete(`${AppConfig.api_url}/analytics/rules/${id}?custom=` + custom)
      .catch(this.handleError);
  }

  downloadRules(custom): Observable<any> {
    return this.http.get(`${AppConfig.api_url}/analytics/rules/download?custom=` + custom,
      { responseType: ResponseContentType.Blob })
      .map(res => {
        return new Blob([res.blob()], { type: 'application/yaml' })
      })
      .catch(this.handleError);
  }

  uploadRules(custom: boolean, file: File): Observable<any> {
    let formData: FormData = new FormData();
    formData.append('rules', file, 'rules.yml');

    return this.http.post(`${AppConfig.api_url}/analytics/rules/upload?custom=` + custom, formData)
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
      } else {
        return Observable.throw(errorJson || 'Server error');
      }
    } else {
      return Observable.throw(error || 'Server error');
    }
  }

}
