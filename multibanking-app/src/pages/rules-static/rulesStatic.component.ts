import { Component, ViewChild, ElementRef } from '@angular/core';
import { NavController, NavParams, AlertController, Navbar, LoadingController } from 'ionic-angular';
import { RulesService } from '../../services/rules.service';
import { RulesStaticAutoCompleteService } from '../../services/rulesStaticAutoComplete.service';
import { AutoCompleteComponent } from 'ionic2-auto-complete';
import { RuleEditPage } from '../rule-edit/ruleEdit.component';
import { RulesCustomAutoCompleteService } from '../../services/rulesCustomAutoComplete.service';
import { ResourceRuleEntity } from '../../model/multibanking/models';
import { Pageable } from '../../model/pageable';


@Component({
  selector: 'page-rules-static',
  templateUrl: 'rulesStatic.component.html',
})
export class RulesStaticPage {

  @ViewChild(AutoCompleteComponent) autocomplete: AutoCompleteComponent;
  @ViewChild(Navbar) navBar: Navbar;
  @ViewChild('headerTag') headerTag: ElementRef;
  @ViewChild('scrollableTag') scrollableTag: ElementRef;
  rulesStatus;
  selectedRule: ResourceRuleEntity;
  rules: ResourceRuleEntity[];
  pageable: Pageable;
  custom: boolean = false;

  constructor(public navCtrl: NavController,
    public navParams: NavParams,
    public loadingCtrl: LoadingController,
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

  ionViewDidEnter() {
    if (this.headerTag) {
      let offset = this.headerTag.nativeElement.offsetHeight;
      (<HTMLDivElement>this.scrollableTag.nativeElement).style.marginTop = offset + 'px';
    }
  }

  ionViewDidLoad() {
    this.navBar.backButtonClick = (e: UIEvent) => {
      this.navCtrl.parent.viewCtrl.dismiss();
    };
  }

  registerRulesChangedListener() {
    this.rulesService.rulesChangedObservable.subscribe(rule => {
      this.loadRules();
    });
  }

  loadRules() {
    this.rulesService.getRules(this.custom).subscribe(response => {
      this.pageable = response;
      this.rules = response._embedded ? response._embedded.ruleEntityList : [];

      this.selectedRule = undefined;

      this.rulesService.getRulesStatus().subscribe(response => {
        this.rulesStatus = response;
      })
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

  createRule() {
    this.navCtrl.push(RuleEditPage, { customRule: false });
  }

  editRule(rule: ResourceRuleEntity) {
    this.navCtrl.push(RuleEditPage, { rule: rule, customRule: false });
  }

  deleteRule(rule: ResourceRuleEntity) {
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
    link.download = "rules.csv";
    link.click();
    setTimeout(() =>
      // For Firefox it is necessary to delay revoking the ObjectURL
      window.URL.revokeObjectURL(data), 100)
  }

  uploadRules(input) {
    let loading = this.loadingCtrl.create({
      content: 'Please wait...'
    });
    loading.present();

    let file: File = input.target.files[0];
    this.rulesService.uploadRules(file).subscribe(
      data => {
        this.loadRules();
        loading.dismiss();
      },
      messages => {
        loading.dismiss();
        if (messages instanceof Array) {
          messages.forEach(message => {
            if (message.key == "INVALID_RULES") {
              this.alertCtrl.create({
                message: "Invalid rules file",
                buttons: ['OK']
              }).present();
            }
          })
        }
      });
    input.target.value = null;
  }
}
