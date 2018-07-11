import { Injectable } from '@angular/core';
import { Observable } from "rxjs/Observable";
import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { ENV } from "../env/env";


@Injectable()
export class ImageService {

  constructor(private http: HttpClient) {}

  getImage(imageName: string) {
    if (!imageName) {
      return `${ENV.smartanalytics_url}/images/keinlogo_256.png`
    }
    return `${ENV.smartanalytics_url}/images/${imageName}`;
  }

  uploadImages(file: File): Observable<any> {
    let formData: FormData = new FormData();
    formData.append('imagesFile', file, file.name);

    return this.http.post(`${ENV.smartanalytics_url}/images/upload`, formData, { responseType: 'text' })
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
