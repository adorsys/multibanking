import { Component, ViewChild } from '@angular/core';
import { NavController, NavParams, AlertController, Navbar } from 'ionic-angular';
import { RulesService } from '../../services/RulesService';
import { Rule } from '../../api/Rule';
import { RulesStaticAutoCompleteService } from '../../services/RulesStaticAutoCompleteService';
import { AutoCompleteComponent } from 'ionic2-auto-complete';
import { RuleEditPage } from '../rule-edit/ruleEdit';
import { RulesCustomAutoCompleteService } from '../../services/RulesCustomAutoCompleteService';


@Component({
  selector: 'page-rules-static',
  templateUrl: 'rulesStatic.html',
})
export class RulesStaticPage {

  @ViewChild(AutoCompleteComponent) autocomplete: AutoCompleteComponent;
  @ViewChild(Navbar) navBar: Navbar;
  selectedRule: Rule;
  rules: Rule[];
  customRules: boolean = false;

  constructor(public navCtrl: NavController,
    public navParams: NavParams,
    public rulesCustomAutoCompleteService: RulesCustomAutoCompleteService,
    public rulesStaticAutoCompleteService: RulesStaticAutoCompleteService,
    public alertCtrl: AlertController,
    public rulesService: RulesService) {

    this.registerRulesChangedListener();
  }

  ngOnInit() {
    this.autocomplete.itemSelected.subscribe(rule => {
      this.rules = [rule];
    });

    this.autocomplete.searchbarElem.ionClear.subscribe(() => {
      this.selectedRule = undefined;
      this.loadRules();
    });

    this.loadRules();
  }

  ionViewDidLoad() {
    this.navBar.backButtonClick = (e: UIEvent) => {
      this.navCtrl.parent.viewCtrl.dismiss();
    };
  }

  registerRulesChangedListener() {
    this.rulesService.rulesChangedObservable.subscribe(rule => {
      if (rule.released) {
        this.loadRules();
      }
    })
  }

  loadRules() {
    this.rulesService.getRules(this.customRules).subscribe(rules => {
      this.rules = rules;
      this.selectedRule = undefined;
    })
  }

  editRule(rule: Rule) {
    this.navCtrl.push(RuleEditPage, { rule: rule });
  }

  deleteRule(rule: Rule) {
    this.rulesService.deleteRule(rule.id, this.customRules).subscribe(rules => {
      this.loadRules();
    })
  }

  downloadRules() {
    this.rulesService.downloadRules(this.customRules).subscribe(data => {
      this.showFile(data);
    })
  }

  showFile(blob) {
    // It is necessary to create a new blob object with mime-type explicitly set
    // otherwise only Chrome works like it should
    var newBlob = new Blob([blob], { type: "application/pdf" })

    // IE doesn't allow using a blob object directly as link href
    // instead it is necessary to use msSaveOrOpenBlob
    if (window.navigator && window.navigator.msSaveOrOpenBlob) {
      window.navigator.msSaveOrOpenBlob(newBlob);
      return;
    }

    // For other browsers: 
    // Create a link pointing to the ObjectURL containing the blob.
    const data = window.URL.createObjectURL(newBlob);
    var link = document.createElement('a');
    link.href = data;
    link.download = "rules.yaml";
    link.click();
    setTimeout(() =>
      // For Firefox it is necessary to delay revoking the ObjectURL
      window.URL.revokeObjectURL(data), 100)
  }

  uploadRules(input) {
    let file: File = input.target.files[0];
    this.rulesService.uploadRules(this.customRules, file).subscribe(
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
