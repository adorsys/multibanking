import { Component, ViewChild } from '@angular/core';
import { NavController, NavParams, AlertController, Navbar } from 'ionic-angular';
import { RulesService } from '../../services/RulesService';
import { Rule } from '../../api/Rule';
import { RulesCustomAutoCompleteService } from '../../services/RulesCustomAutoCompleteService';
import { AutoCompleteComponent } from 'ionic2-auto-complete';
import { RulesStaticPage } from '../rules-static/rulesStatic';
import { RulesStaticAutoCompleteService } from '../../services/RulesStaticAutoCompleteService';

@Component({
  selector: 'page-rules-custom',
  templateUrl: 'rulesCustom.html',
})
export class RulesCustomPage extends RulesStaticPage {

  @ViewChild(AutoCompleteComponent) autocomplete: AutoCompleteComponent;
  @ViewChild(Navbar) navBar: Navbar;
  selectedRule: Rule;
  rules: Rule[];
  customRules: boolean = true;

  constructor(public navCtrl: NavController,
    public navParams: NavParams,
    public rulesCustomAutoCompleteService: RulesCustomAutoCompleteService,
    public rulesStaticAutoCompleteService: RulesStaticAutoCompleteService,
    public alertCtrl: AlertController,
    public rulesService: RulesService) {

    super(navCtrl, navParams, rulesCustomAutoCompleteService, rulesStaticAutoCompleteService, alertCtrl, rulesService);
  }

  registerRulesChangedListener() {
    this.rulesService.rulesChangedObservable.subscribe(rule => {
      if (!rule.released) {
        this.loadRules();
      }
    })
  }

  releaseRule(rule: Rule) {
    rule.released = true;
    this.rulesService.updateRule(rule).subscribe(rules => {
      this.loadRules();
    })
  }

  
}
