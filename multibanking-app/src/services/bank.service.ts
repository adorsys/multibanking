import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ENV } from "../env/env";
import { ResourceBank } from '../model/multibanking/resourceBank';

@Injectable()
export class BankService {

    constructor(private http: HttpClient) {
    }

    getBank(bankCode: string,): Observable<ResourceBank> {
        return this.http.get(`${ENV.api_url}/banks/${bankCode}`)
          .catch(this.handleError);
      }

    uploadBanks(file: File): Observable<any> {
        let formData: FormData = new FormData();
        formData.append('banksFile', file, file.name);

        return this.http.post(`${ENV.api_url}/banks/upload`, formData, { responseType: 'text' })
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