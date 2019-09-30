import { Injectable } from '@angular/core';
import { Subject, Observable, of } from 'rxjs';
import { ResourceRuleEntity } from 'src/multibanking-api/resourceRuleEntity';
import { CategoriesTree } from 'src/multibanking-api/categoriesTree';
import { environment } from 'src/environments/environment';
import { AbstractService } from './abstract.service';
import { catchError, finalize, map, take, mergeMap } from 'rxjs/operators';
import { ResourceGroupConfig } from 'src/multibanking-api/resourceGroupConfig';
import { ResourceContractBlacklist } from 'src/multibanking-api/resourceContractBlacklist';
import { Pageable } from 'src/app/model/pageable';

@Injectable({
    providedIn: 'root'
})
export class RulesService extends AbstractService {

    public rulesChangedObservable = new Subject<ResourceRuleEntity>();

    getAvailableCategories(): Observable<CategoriesTree> {
        return this.http.get(`${environment.smartanalytics_url}/config/booking-categories`)
            .pipe(
                catchError(this.handleError)
            );
    }

    updateCategories(categoriesContainer): Observable<any> {
        return this.http.post(`${environment.smartanalytics_url}/config/booking-categories`, categoriesContainer)
            .pipe(
                catchError(this.handleError)
            );
    }

    uploadCategories(file: File): Observable<any> {
        const formData: FormData = new FormData();
        formData.append('categoriesFile', file, file.name);

        return this.http.post(`${environment.smartanalytics_url}/config/booking-categories/upload`, formData, { responseType: 'text' })
            .pipe(
                catchError(this.handleError)
            );
    }

    getBookingGroups(): Observable<ResourceGroupConfig> {
        return this.http.get(`${environment.smartanalytics_url}/config/booking-groups`)
            .pipe(
                catchError(this.handleError)
            );
    }

    uploadBookingGroups(file: File): Observable<any> {
        const formData: FormData = new FormData();
        formData.append('bookingGroupsFile', file, file.name);

        return this.http.post(`${environment.smartanalytics_url}/config/booking-groups/upload`, formData, { responseType: 'text' })
            .pipe(
                catchError(this.handleError)
            );
    }

    getContractBlacklist(): Observable<ResourceContractBlacklist> {
        return this.http.get(`${environment.smartanalytics_url}/config/contract-blacklist`)
            .pipe(
                catchError(this.handleError)
            );
    }

    uploadContractBlacklist(file: File): Observable<any> {
        const formData: FormData = new FormData();
        formData.append('contractBlacklistFile', file, file.name);

        return this.http.post(`${environment.smartanalytics_url}/config/contract-blacklist/upload`, formData, { responseType: 'text' })
            .pipe(
                catchError(this.handleError)
            );
    }

    createRule(rule: ResourceRuleEntity, custom: boolean): Observable<any> {
        const url = custom ? `${environment.api_url}/analytics/rules`
            : `${environment.smartanalytics_url}/config/booking-rules`;

        return this.http.post(url, rule, { responseType: 'text' })
            .pipe(
                catchError(this.handleError),
                finalize(() => {
                    this.rulesChangedObservable.next(rule);
                })
            );
    }

    updateRule(rule: ResourceRuleEntity): Observable<any> {
        const url = rule.ruleId.startsWith('custom') ? `${environment.api_url}/analytics/rules/${rule.id}`
            : `${environment.smartanalytics_url}/config/booking-rules/${rule.id}`;

        return this.http.put(url, rule)
            .pipe(
                catchError(this.handleError),
                finalize(() => {
                    this.rulesChangedObservable.next(rule);
                })
            );
    }

    getRulesStatus() {
        return this.http.get(`${environment.smartanalytics_url.replace('/api/v1', '')}/status`)
            .pipe(
                catchError(this.handleError)
            );
    }

    getRules(custom: boolean): Observable<Pageable> {
        const url = custom ? `${environment.api_url}/analytics/rules/` : `${environment.smartanalytics_url}/config/booking-rules/`;

        return this.http.get(url)
            .pipe(
                catchError(this.handleError)
            );
    }

    getNextRules(url: string): Observable<Pageable> {
        return this.http.get(url)
            .pipe(
                catchError(this.handleError)
            );
    }

    getRule(id: string): Observable<ResourceRuleEntity> {
        const url = id.startsWith('custom') ? `${environment.api_url}/analytics/rules/${id}`
            : `${environment.smartanalytics_url}/config/booking-rules/${id}`;

        return this.http.get(url)
            .pipe(
                map((res: Response) => res.json() != null ? res.json() : {}),
                catchError(this.handleError)
            );
    }

    deleteRule(id, custom): Observable<any> {
        const url = custom ? `${environment.api_url}/analytics/rules/${id}`
            : `${environment.smartanalytics_url}/config/booking-rules/${id}`;

        return this.http.delete(url)
            .pipe(
                catchError(this.handleError)
            );
    }

    searchRules(custom: boolean, keyword: string): Observable<ResourceRuleEntity[]> {
        if (keyword.length <= 1) {
            return of();
        }

        const url = custom ?
            `${environment.api_url}/analytics/rules/search?query=${keyword}&custom=${custom}` :
            `${environment.smartanalytics_url}/config/booking-rules/search?query=${keyword}&custom=${custom}`;

        return this.http.get(url)
            .pipe(
                map((res: any) => {
                    return res._embedded != null ? res._embedded.ruleEntityList : [];
                }),
                catchError(this.handleError)
            );

    }

    downloadRules(custom): Observable<any> {
        const url = custom ? `${environment.api_url}/analytics/rules/download`
            : `${environment.smartanalytics_url}/config/booking-rules/download`;

        return this.http.get(url, { responseType: 'blob' })
            .pipe(
                map(res => {
                    return new Blob([res], { type: 'application/yaml' });
                }),
                catchError(this.handleError)
            );
    }

    uploadRules(file: File): Observable<any> {
        const formData: FormData = new FormData();
        formData.append('rulesFile', file, file.name);

        return this.http.post(`${environment.smartanalytics_url}/config/booking-rules/upload`, formData, { responseType: 'text' })
            .pipe(
                catchError(this.handleError)
            );
    }



}
