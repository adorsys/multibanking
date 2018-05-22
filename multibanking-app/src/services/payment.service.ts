import { Injectable } from '@angular/core';
import { AppConfig } from '../app/app.config';
import { Observable } from 'rxjs/Observable';
import { PaymentCreate } from '../api/PaymentCreate';
import { PaymentSubmit } from '../api/PaymentSubmit';
import { Payment } from '../api/Payment';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class PaymentService {

  constructor(private http: HttpClient) {
  }

  getPayment(location: string): Observable<Payment> {
    return this.http.get(location)
      .catch(this.handleError);
  }

  createPayment(accessId: string, accountId: string, paymentCreate: PaymentCreate): Observable<string> {
    return this.http.post(`${AppConfig.api_url}/bankaccesses/${accessId}/accounts/${accountId}/payments`, paymentCreate, { responseType: 'text' })
      .map((res: any) => res.headers.get("Location"))
      .catch(this.handleError);
  }

  submitPayment(accessId: string, accountId: string, paymentId: string, paymentSubmit: PaymentSubmit): Observable<any> {
    return this.http.post(`${AppConfig.api_url}/bankaccesses/${accessId}/accounts/${accountId}/payments/${paymentId}/submit`, paymentSubmit, { responseType: 'text' })
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


}
