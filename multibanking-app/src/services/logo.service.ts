import { Injectable } from '@angular/core';

import { AppConfig } from "../app/app.config";

@Injectable()
export class LogoService {

  constructor() {}

  getLogo(logoId: string) {
    if (!logoId) {
      return `${AppConfig.api_url}/image/keinlogo_256`
    }
    return `${AppConfig.api_url}/image/${logoId}`;
  }
}
