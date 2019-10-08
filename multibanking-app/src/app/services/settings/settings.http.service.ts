import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Settings } from '../../model/settings';
import { SettingsService } from './settings.service';

@Injectable({ providedIn: 'root' })
export class SettingsHttpService {

    constructor(private http: HttpClient, private settingsService: SettingsService) {}

    initializeApp(): Promise<any> {
        return new Promise(
            (resolve) => {
                this.http.get('assets/settings/settings.json')
                    .toPromise()
                    .then(async response => {
                        this.settingsService.settings = response as Settings;
                        resolve();
                    });
            }
        );
    }
}
