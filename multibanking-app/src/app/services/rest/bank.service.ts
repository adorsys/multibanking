import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ResourceBankTO } from 'src/multibanking-api/resourceBankTO';
import { catchError, map } from 'rxjs/operators';
import { AbstractService } from './abstract.service';
import { BankTO } from 'src/multibanking-api/bankTO';

@Injectable({
    providedIn: 'root',
})
export class BankService extends AbstractService {

    getBank(bankCode: string): Observable<ResourceBankTO> {
        return this.getBankByLink(`${this.settings.apiUrl}/banks/${bankCode}`);
    }

    getBankByLink(link: string): Observable<ResourceBankTO> {
        return this.http.get(link)
            .pipe(
                catchError(this.handleError)
            );
    }

    uploadBanks(file: File): Observable<any> {
        const formData: FormData = new FormData();
        formData.append('banksFile', file, file.name);

        return this.http.post(`${this.settings.apiUrl}/banks/upload`, formData, { responseType: 'text' })
            .pipe(
                catchError(this.handleError)
            );
    }

    searchBanks(keyword: string): Observable<BankTO[]> {
        return this.http.get(`${this.settings.apiUrl}/banks?query=${keyword}`)
            .pipe(
                map((res: any) => res && res._embedded ? res._embedded.bankList : []),
                catchError(this.handleError)
            );
    }
}
