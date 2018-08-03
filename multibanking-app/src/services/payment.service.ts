import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { ENV } from "../env/env";
import { ResourcePaymentEntity, CreatePaymentRequest, SubmitPaymentRequest } from '../model/multibanking/models';

@Injectable()
export class PaymentService {

  constructor(private http: HttpClient) {
  }

  getPayment(location: string): Observable<ResourcePaymentEntity> {
    return this.http.get(location)
      .catch(this.handleError);
  }

  createPayment(accessId: string, accountId: string, paymentCreate: CreatePaymentRequest): Observable<string> {
    return this.http.post(`${ENV.api_url}/bankaccesses/${accessId}/accounts/${accountId}/payments`, paymentCreate, { observe: 'response', responseType: 'text' })
      .map((res: HttpResponse<any>) => {
        return res.headers.get('Location');
      })
      .catch(this.handleError);
  }

  submitPayment(accessId: string, accountId: string, paymentId: string, paymentSubmit: SubmitPaymentRequest): Observable<any> {
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
