import { Injectable } from '@angular/core';
import { AbstractService } from './abstract.service';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ImagesService extends AbstractService {

  getImage(imageName: string) {
    if (!imageName) {
      return `${this.settings.smartanalyticsUrl}/images/keinlogo_256.png`;
    }
    return `${this.settings.smartanalyticsUrl}/images/${imageName}`;
  }

  uploadImages(file: File): Observable<any> {
    const formData: FormData = new FormData();
    formData.append('imagesFile', file, file.name);

    return this.http.post(`${this.settings.smartanalyticsUrl}/images/upload`, formData, { responseType: 'text' })
      .pipe(
        catchError(this.handleError)
      );
  }
}
