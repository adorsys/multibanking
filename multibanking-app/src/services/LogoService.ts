import { Injectable } from '@angular/core';

import { AppConfig } from "../app/app.config";

@Injectable()
export class LogoService {

  constructor() {}

  getLogo(logoId: string) {
    return `${AppConfig.api_url}/image/${logoId}`;
  }
}
