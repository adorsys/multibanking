import { Injectable } from '@angular/core';
import { ENV } from "../env/env";


@Injectable()
export class LogoService {

  constructor() {}

  getLogo(logoId: string) {
    if (!logoId) {
      return `${ENV.api_url}/image/keinlogo_256`
    }
    return `${ENV.api_url}/image/${logoId}`;
  }
}
