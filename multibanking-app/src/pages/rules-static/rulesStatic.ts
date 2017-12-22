import { Component, ViewChild } from '@angular/core';
import { NavController, NavParams, AlertController } from 'ionic-angular';
import { RulesService } from '../../services/RulesService';
import { Rule } from '../../api/Rule';
import { RulesAutoCompleteService } from '../../services/RulesAutoCompleteService';
import { AutoCompleteComponent } from 'ionic2-auto-complete';
import { RuleEditPage } from '../rule-edit/ruleEdit';


@Component({
  selector: 'page-rules-static',
  templateUrl: 'rulesStatic.html',
})
export class RulesStaticPage {

  @ViewChild('autocomplete')
  autocomplete: AutoCompleteComponent;
  selectedRule: Rule;

  constructor(public navCtrl: NavController,
    public navParams: NavParams,
    private alertCtrl: AlertController,
    public rulesAutoCompleteService: RulesAutoCompleteService,
    public rulesService: RulesService) {
  }

  ngOnInit() {
    this.autocomplete.itemSelected.subscribe(rule => {
      this.selectedRule = rule;
    });

    this.autocomplete.searchbarElem.ionClear.subscribe(() => {
      this.selectedRule = undefined;
    });
  }

  editRule() {
    this.navCtrl.push(RuleEditPage, {rule: this.selectedRule});
  }

  deleteRule() {
    this.rulesService.deleteRule(this.selectedRule.id, true).subscribe(rules => {
      this.selectedRule = undefined;
    })
  }

  downloadRules() {
    this.rulesService.downloadRules(false).subscribe(data => {
      window.open(window.URL.createObjectURL(data));
    })
  }

  uploadRules(input) {
    let file: File = input.target.files[0];
    this.rulesService.uploadRules(false, file).subscribe(
      data => {
      },
      error => {
        if (error && error.messages) {
          error.messages.forEach(message => {
            if (message.key == "INVALID_RULES") {
              this.alertCtrl.create({
                message: message.paramsMap.message,
                buttons: ['OK']
              }).present();
            }
          })
        }
      })
  }
}
