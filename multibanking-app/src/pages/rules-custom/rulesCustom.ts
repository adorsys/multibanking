import { Component } from '@angular/core';
import { NavController, NavParams, AlertController } from 'ionic-angular';
import { RulesService } from '../../services/RulesService';
import { Rule } from '../../api/Rule';
import { RuleEditPage } from '../rule-edit/ruleEdit';

@Component({
  selector: 'page-rules-custom',
  templateUrl: 'rulesCustom.html',
})
export class RulesCustomPage {

  selectedRule: Rule;

  rules: Rule[];

  constructor(public navCtrl: NavController,
    public navParams: NavParams,
    private alertCtrl: AlertController,
    public rulesService: RulesService) {

    rulesService.rulesChangedObservable.subscribe(rule => {
      if (!rule.released) {
        this.loadRules();
      }
    })
  }

  ngOnInit() {
    this.loadRules();
  }

  loadRules() {
    this.rulesService.getRules(true).subscribe(rules => {
      this.rules = rules;
    })
  }

  releaseRule(rule: Rule) {
    rule.released = true;
    this.rulesService.updateRule(rule).subscribe(rules => {
      this.loadRules();
    })
  }

  createRule() {
    this.navCtrl.push(RuleEditPage, { rule: {} });
  }

  editRule(rule: Rule) {
    this.navCtrl.push(RuleEditPage, { rule: rule });
  }

  deleteRule(rule: Rule) {
    this.rulesService.deleteRule(rule.id, true).subscribe(rules => {
      this.loadRules();
    })
  }

  downloadRules() {
    this.rulesService.downloadRules(true).subscribe(data => {
      window.open(window.URL.createObjectURL(data));
    })
  }

  uploadRules(input) {
    let file: File = input.target.files[0];
    this.rulesService.uploadRules(true, file).subscribe(
      data => {
        this.loadRules();
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
