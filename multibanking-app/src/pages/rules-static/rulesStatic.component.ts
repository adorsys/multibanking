import { Component, ViewChild } from '@angular/core';
import { NavController, NavParams, AlertController, Navbar } from 'ionic-angular';
import { RulesService } from '../../services/rules.service';
import { Rule } from '../../api/Rule';
import { RulesStaticAutoCompleteService } from '../../services/rulesStaticAutoComplete.service';
import { AutoCompleteComponent } from 'ionic2-auto-complete';
import { RuleEditPage } from '../rule-edit/ruleEdit.component';
import { RulesCustomAutoCompleteService } from '../../services/rulesCustomAutoComplete.service';
import { Pageable } from '../../api/Pageable';


@Component({
  selector: 'page-rules-static',
  templateUrl: 'rulesStatic.component.html',
})
export class RulesStaticPage {

  @ViewChild(AutoCompleteComponent) autocomplete: AutoCompleteComponent;
  @ViewChild(Navbar) navBar: Navbar;
  selectedRule: Rule;
  rules: Rule[];
  pageable: Pageable;
  custom: boolean = false;

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
    });
  }

  loadRules() {
    this.rulesService.getRules(this.custom).subscribe(response => {
      this.pageable = response;
      this.rules = response._embedded ? response._embedded.ruleEntityList : [];
      
      this.selectedRule = undefined;
    });
  }

  loadNextRules(infiniteScroll) {
    if (this.pageable._links.next) {
      this.rulesService.getNextRules(this.pageable._links.next.href).subscribe(response => {
        this.pageable = response;
        if (this.custom) {
          this.rules = this.rules.concat(response._embedded.customRuleEntityList);
        } else {
          this.rules = this.rules.concat(response._embedded.ruleEntityList);
        }
        
        infiniteScroll.complete();
      });
    } else {
      infiniteScroll.complete();
    }
  }

  doInfinite(infiniteScroll) {
    this.loadNextRules(infiniteScroll);
  }

  editRule(rule: Rule) {
    this.navCtrl.push(RuleEditPage, { rule: rule, customRule: false });
  }

  deleteRule(rule: Rule) {
    this.rulesService.deleteRule(rule.id, this.custom).subscribe(rules => {
      this.loadRules();
    });
  }

  downloadRules() {
    this.rulesService.downloadRules(this.custom).subscribe(data => {
      this.showFile(data);
    });
  }

  showFile(blob) {
    // It is necessary to create a new blob object with mime-type explicitly set
    // otherwise only Chrome works like it should
    let newBlob = new Blob([blob], { type: "application/x-yaml" })

    // IE doesn't allow using a blob object directly as link href
    // instead it is necessary to use msSaveOrOpenBlob
    if (window.navigator && window.navigator.msSaveOrOpenBlob) {
      window.navigator.msSaveOrOpenBlob(newBlob);
      return;
    }

    // For other browsers:
    // Create a link pointing to the ObjectURL containing the blob.
    const data = window.URL.createObjectURL(newBlob);
    let link = document.createElement('a');
    link.href = data;
    link.download = "rules.yaml";
    link.click();
    setTimeout(() =>
      // For Firefox it is necessary to delay revoking the ObjectURL
      window.URL.revokeObjectURL(data), 100)
  }

  uploadRules(input) {
    let file: File = input.target.files[0];
    this.rulesService.uploadRules(file).subscribe(
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
      });
  }
}
