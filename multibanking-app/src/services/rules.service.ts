import { Injectable } from "@angular/core";
import { Observable } from "rxjs/Observable";
import { Subject } from "rxjs";
import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { ENV } from "../env/env";
import { ResourceRuleEntity, ResourceContractBlacklist } from "../model/multibanking/models";
import { CategoriesTree } from "../model/multibanking/categoriesTree";
import { Pageable } from "../model/pageable";
import { ResourceGroupConfig } from "model/multibanking/resourceGroupConfig";

@Injectable()
export class RulesService {

  public rulesChangedObservable = new Subject<ResourceRuleEntity>();

  constructor(private http: HttpClient) {
  }

  getAvailableCategories(): Observable<CategoriesTree> {
    return this.http.get(`${ENV.smartanalytics_url}/config/booking-categories`)
      .catch(this.handleError);
  }

  updateCategories(categoriesContainer): Observable<any> {
    return this.http.post(`${ENV.smartanalytics_url}/config/booking-categories`, categoriesContainer)
      .catch(this.handleError);
  }

  uploadCategories(file: File): Observable<any> {
    let formData: FormData = new FormData();
    formData.append('categoriesFile', file, file.name);

    return this.http.post(`${ENV.smartanalytics_url}/config/booking-categories/upload`, formData, { responseType: 'text' })
      .catch(this.handleError);
  }

  getBookingGroups(): Observable<ResourceGroupConfig> {
    return this.http.get(`${ENV.smartanalytics_url}/config/booking-groups`)
      .catch(this.handleError);
  }

  uploadBookingGroups(file: File): Observable<any> {
    let formData: FormData = new FormData();
    formData.append('bookingGroupsFile', file, file.name);

    return this.http.post(`${ENV.smartanalytics_url}/config/booking-groups/upload`, formData, { responseType: 'text' })
      .catch(this.handleError);
  }

  getContractBlacklist(): Observable<ResourceContractBlacklist> {
    return this.http.get(`${ENV.smartanalytics_url}/config/contract-blacklist`)
      .catch(this.handleError);
  }

  uploadContractBlacklist(file: File): Observable<any> {
    let formData: FormData = new FormData();
    formData.append('contractBlacklistFile', file, file.name);

    return this.http.post(`${ENV.smartanalytics_url}/config/contract-blacklist/upload`, formData, { responseType: 'text' })
      .catch(this.handleError);
  }

  createRule(rule: ResourceRuleEntity, custom: boolean): Observable<any> {
    let url = custom ? `${ENV.api_url}/analytics/rules`
      : `${ENV.smartanalytics_url}/config/booking-rules`;

    return this.http.post(url, rule, { responseType: 'text' })
      .catch(this.handleError)
      .finally(() => {
        this.rulesChangedObservable.next(rule)
      })
  }

  updateRule(rule: ResourceRuleEntity): Observable<any> {
    let url = rule.ruleId.startsWith("custom") ? `${ENV.api_url}/analytics/rules/${rule.id}`
      : `${ENV.smartanalytics_url}/config/booking-rules/${rule.id}`;

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
    let url = custom ? `${ENV.api_url}/analytics/rules/` : `${ENV.smartanalytics_url}/config/booking-rules/`;

    return this.http.get(url)
      .catch(this.handleError);
  }

  getNextRules(url: string): Observable<Pageable> {
    return this.http.get(url)
      .catch(this.handleError);
  }

  getRule(id: string): Observable<ResourceRuleEntity> {
    let url = id.startsWith("custom") ? `${ENV.api_url}/analytics/rules/${id}`
      : `${ENV.smartanalytics_url}/config/booking-rules/${id}`;

    return this.http.get(url)
      .map((res: Response) => res.json() != null ? res.json() : {})
      .catch(this.handleError);
  }

  deleteRule(id, custom): Observable<any> {
    let url = custom ? `${ENV.api_url}/analytics/rules/${id}`
      : `${ENV.smartanalytics_url}/config/booking-rules/${id}`;

    return this.http.delete(url)
      .catch(this.handleError);
  }

  downloadRules(custom): Observable<any> {
    let url = custom ? `${ENV.api_url}/analytics/rules/download`
      : `${ENV.smartanalytics_url}/config/booking-rules/download`;

    return this.http.get(url, { responseType: 'blob' })
      .map(res => {
        return new Blob([res], { type: 'application/yaml' })
      })
      .catch(this.handleError);
  }

  uploadRules(file: File): Observable<any> {
    let formData: FormData = new FormData();
    formData.append('rulesFile', file, file.name);

    return this.http.post(`${ENV.smartanalytics_url}/config/booking-rules/upload`, formData, { responseType: 'text' })
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
