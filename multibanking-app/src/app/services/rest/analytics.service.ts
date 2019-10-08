import { Injectable } from '@angular/core';
import { AbstractService } from './abstract.service';
import { Observable } from 'rxjs';
import { ResourceAnalyticsTO } from 'src/multibanking-api/resourceAnalyticsTO';
import { catchError } from 'rxjs/operators';


@Injectable({
  providedIn: 'root',
})
export class AnalyticsService extends AbstractService {

  getAnalytics(accessId: string, accountId: string): Observable<ResourceAnalyticsTO> {
    return this.http.get(`${this.settings.apiUrl}/bankaccesses/${accessId}/accounts/${accountId}/analytics`)
    .pipe(
      catchError(this.handleError)
    );
  }


}
