import { Injectable } from '@angular/core';
import { RulesStaticAutoCompleteService } from './RulesStaticAutoCompleteService';

@Injectable()
export class RulesCustomAutoCompleteService extends RulesStaticAutoCompleteService {

  customRules: boolean = true;

}
