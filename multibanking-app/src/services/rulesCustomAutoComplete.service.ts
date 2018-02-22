import { Injectable } from '@angular/core';
import { RulesStaticAutoCompleteService } from './rulesStaticAutoComplete.service';

@Injectable()
export class RulesCustomAutoCompleteService extends RulesStaticAutoCompleteService {
  customRules: boolean = true;
}
