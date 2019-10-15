import { Injectable } from '@angular/core';
import { Settings } from '../../model/settings';

@Injectable({ providedIn: 'root' })
export class SettingsService {
    public settings: Settings;

    constructor() {
        this.settings = new Settings();
    }
}
