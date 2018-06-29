import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { PaymentCreate } from '../api/PaymentCreate';
import { PaymentSubmit } from '../api/PaymentSubmit';
import { Payment } from '../api/Payment';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ENV } from "../env/env";

@Injectable()
export class PaymentService {

  constructor(private http: HttpClient) {
  }

  getPayment(location: string): Observable<Payment> {
    return this.http.get(location)
      .catch(this.handleError);
  }

  createPayment(accessId: string, accountId: string, paymentCreate: PaymentCreate): Observable<string> {
    return this.http.post(`${ENV.api_url}/bankaccesses/${accessId}/accounts/${accountId}/payments`, paymentCreate, { responseType: 'text' })
      .map((res: any) => res.headers.get("Location"))
      .catch(this.handleError);
  }

  submitPayment(accessId: string, accountId: string, paymentId: string, paymentSubmit: PaymentSubmit): Observable<any> {
    return this.http.post(`${ENV.api_url}/bankaccesses/${accessId}/accounts/${accountId}/payments/${paymentId}/submit`, paymentSubmit, { responseType: 'text' })
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
